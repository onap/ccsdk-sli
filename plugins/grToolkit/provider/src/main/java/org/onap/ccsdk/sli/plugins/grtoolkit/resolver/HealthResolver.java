/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights
 * 			reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.plugins.grtoolkit.resolver;

import org.json.JSONException;
import org.json.JSONObject;

import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.plugins.grtoolkit.connection.ConnectionManager;
import org.onap.ccsdk.sli.plugins.grtoolkit.connection.ConnectionResponse;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.AdminHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterActor;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.DatabaseHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.FailoverStatus;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.Health;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.PropertyKeys;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.SiteHealth;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.FailoverInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.onap.ccsdk.sli.plugins.grtoolkit.data.Health.HEALTHY;

/**
 * Abstract class for the Health Resolver system, which allows for custom logic
 * to be implemented, while leaving inputs/outputs generic and architecture
 * agnostic. This class provides some simple implementations of both Admin and
 * Database health checking, but leaves cluster and site health determinations
 * up to the implementer. Useful implementation examples can be found in the
 * {@code SingleNodeHealthResolver}, {@code ThreeNodeHealthResolver}, and
 * {@code SixNodeHealthResolver} classes.
 *
 * @author Anthony Haddox
 * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
 * @see SingleNodeHealthResolver
 * @see ThreeNodeHealthResolver
 * @see SixNodeHealthResolver
 */
public abstract class HealthResolver {
    private final Logger log = LoggerFactory.getLogger(HealthResolver.class);
    static final String OUTPUT = "output";
    final String httpProtocol;
    final String controllerPort;
    final String credentials;
    final Map<String, ClusterActor> memberMap;
    private DbLibService dbLib;
    final ShardResolver shardResolver;
    private String adminPath;
    private String siteIdentifier;

    /**
     * Constructs the health resolver used by the {@code GrToolkitProvider} to
     * determine the health of the application components.
     *
     * @param map a HashMap containing all of the nodes in the akka cluster
     * @param properties the properties passed ino the provider
     * @param dbLib a reference to the {@code DbLibService} of the provider
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     */
    HealthResolver(Map<String, ClusterActor> map, Properties properties, DbLibService dbLib) {
        log.info("Creating {}", this.getClass().getCanonicalName());
        this.memberMap = map;
        this.dbLib = dbLib;
        shardResolver = ShardResolver.getInstance(properties);

        String adminProtocol = "true".equals(properties.getProperty(PropertyKeys.ADM_USE_SSL)) ? "https://" : "http://";
        String adminPort = "true".equals(properties.getProperty(PropertyKeys.ADM_USE_SSL)) ? properties.getProperty(PropertyKeys.ADM_PORT_SSL) : properties.getProperty(PropertyKeys.ADM_PORT_HTTP);
        adminPath = adminProtocol + properties.getProperty(PropertyKeys.ADM_FQDN) + ":" + adminPort + properties.getProperty(PropertyKeys.ADM_HEALTHCHECK);
        siteIdentifier = properties.getProperty(PropertyKeys.SITE_IDENTIFIER).trim();

        controllerPort = "true".equals(properties.getProperty(PropertyKeys.CONTROLLER_USE_SSL).trim()) ? properties.getProperty(PropertyKeys.CONTROLLER_PORT_SSL).trim() : properties.getProperty(PropertyKeys.CONTROLLER_PORT_HTTP).trim();
        httpProtocol = "true".equals(properties.getProperty(PropertyKeys.CONTROLLER_USE_SSL).trim()) ? "https://" : "http://";
        if(siteIdentifier == null || siteIdentifier.isEmpty()) {
            siteIdentifier = properties.getProperty(PropertyKeys.SITE_IDENTIFIER).trim();
        }
        credentials = properties.getProperty(PropertyKeys.CONTROLLER_CREDENTIALS).trim();
    }

    public abstract ClusterHealth getClusterHealth();
    public abstract List<SiteHealth> getSiteHealth();
    public abstract FailoverStatus tryFailover(FailoverInput input);
    public abstract void resolveSites();

    /**
     * Gets a connection to the admin portal. If the status code is 200, the
     * admin portal is assumed to be healthy.
     *
     * @return an {@code AdminHealth} object with health of the admin portal
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     * @see AdminHealth
     */
    public AdminHealth getAdminHealth() {
        log.info("getAdminHealth(): Requesting health check from {}", adminPath);
        try {
            ConnectionResponse response = ConnectionManager.getConnectionResponse(adminPath, ConnectionManager.HttpMethod.GET, null, null);
            Health health = (response.statusCode == 200) ? HEALTHY : Health.FAULTY;
            AdminHealth adminHealth = new AdminHealth(health, response.statusCode);
            log.info("getAdminHealth(): Response: {}", response);
            return adminHealth;
        } catch(IOException e) {
            log.error("getAdminHealth(): Problem getting ADM health.", e);
            return new AdminHealth(Health.FAULTY, 500);
        }
    }

    /**
     * Uses {@code DbLibService} to get a connection to the database. If
     * {@code DbLibService} is active and the connection it returns is not read
     * only, the database(s) is assumed to be healthy.
     *
     * @return an {@code DatabaseHealth} object with health of the database
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     * @see DatabaseHealth
     */
    public DatabaseHealth getDatabaseHealth() {
        log.info("getDatabaseHealth(): Determining database health...");
        try (Connection connection = dbLib.getConnection()){
            log.debug("getDatabaseHealth(): DBLib isActive(): {}", dbLib.isActive());
            log.debug("getDatabaseHealth(): DBLib isReadOnly(): {}", connection.isReadOnly());
            log.debug("getDatabaseHealth(): DBLib isClosed(): {}", connection.isClosed());
            if(!dbLib.isActive() || connection.isClosed() || connection.isReadOnly()) {
                log.warn("getDatabaseHealth(): Database is FAULTY");
                return new DatabaseHealth(Health.FAULTY);
            }
            log.info("getDatabaseHealth(): Database is HEALTHY");
        } catch(SQLException e) {
            log.error("getDatabaseHealth(): Database is FAULTY");
            log.error("getDatabaseHealth(): Error", e);
            return new DatabaseHealth(Health.FAULTY);
        }

        return new DatabaseHealth(HEALTHY);
    }

    /**
     * Utility method to see if an input is healthy.
     *
     * @return true if the input is healthy
     * @see Health
     */
    boolean isHealthy(Health h) {
        return HEALTHY == h;
    }

    public String getSiteIdentifier() {
        return siteIdentifier;
    }

    public void setSiteIdentifier(String siteIdentifier) {
        this.siteIdentifier = siteIdentifier;
    }

    /**
     * Used to invoke the admin-health or database-health RPC to check if that
     * component is healthy.
     *
     * @param path the path to the admin-health or database-health RPCs
     * @return true if the component is healthy
     * @throws IOException if a connection cannot be obtained
     */
    boolean isRemoteComponentHealthy(String path) throws IOException {
        String content = ConnectionManager.getConnectionResponse(path, ConnectionManager.HttpMethod.POST, null, credentials).content;
        try {
            JSONObject responseJson = new JSONObject(content);
            JSONObject responseValue = responseJson.getJSONObject(OUTPUT);
            return HEALTHY.toString().equals(responseValue.getString("health"));
        } catch(JSONException e) {
            log.error("Error parsing JSON", e);
            throw new IOException();
        }
    }

    /**
     * Checks a {@code ClusterActor} object to see if the node is healthy.
     *
     * @param controller the controller to check
     * @return true if the controller is up and reachable
     * @see ClusterActor
     */
    public boolean isControllerHealthy(ClusterActor controller) {
        return (controller.isUp() && ! controller.isUnreachable());
    }
}
