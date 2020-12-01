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

import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.AdminHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterActor;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.DatabaseHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.FailoverStatus;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.Health;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.SiteHealth;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.FailoverInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of {@code HealthResolver} for a single node controller
 * architecture.
 *
 * @author Anthony Haddox
 * @see HealthResolver
 */
public class SingleNodeHealthResolver extends HealthResolver {
    private final Logger log = LoggerFactory.getLogger(SingleNodeHealthResolver.class);

    /**
     * Constructs the health resolver used by the {@code GrToolkitProvider} to
     * determine the health of the application components.
     *
     * @param map a HashMap containing all of the nodes in the akka cluster
     * @param properties the properties passed ino the provider
     * @param dbLib a reference to the {@code DbLibService} of the provider
     * @see HealthResolver
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     */
    public SingleNodeHealthResolver(Map<String, ClusterActor> map, Properties properties, DbLibService dbLib) {
        super(map, properties, dbLib);
        resolveSites();
    }

    /**
     * Implementation of {@code getClusterHealth()}. Uses the
     * {@code ShardResolver} to gather health information about the controller.
     * This method assumes the cluster is always healthy since it is a single
     * node.
     *
     * @return an {@code ClusterHealth} object with health of the akka cluster
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     * @see HealthResolver
     * @see ClusterHealth
     * @see ShardResolver
     */
    @Override
    public ClusterHealth getClusterHealth() {
        log.info("getClusterHealth(): Getting cluster health...");
        shardResolver.getControllerHealth(memberMap);
        return new ClusterHealth().withHealth(Health.HEALTHY);
    }

    /**
     * Implementation of {@code getSiteHealth()}. Uses the results from
     * {@code getAdminHealth}, {@code getDatabaseHealth}, and
     * {@code getClusterHealth} to determine the health of the site. If all
     * components are healthy, the site is healthy.
     *
     * @return a List of {@code SiteHealth} objects with health of the site
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     * @see HealthResolver
     * @see SiteHealth
     * @see ShardResolver
     */
    @Override
    public List<SiteHealth> getSiteHealth() {
        log.info("getSiteHealth(): Getting site health...");
        AdminHealth adminHealth = getAdminHealth();
        DatabaseHealth databaseHealth = getDatabaseHealth();
        ClusterHealth clusterHealth = getClusterHealth();
        SiteHealth siteHealth = new SiteHealth()
                                        .withAdminHealth(adminHealth)
                                        .withDatabaseHealth(databaseHealth)
                                        .withClusterHealth(clusterHealth)
                                        .withRole("ACTIVE")
                                        .withId(getSiteIdentifier());
        log.info("getSiteHealth(): Admin Health: {}", adminHealth.getHealth().toString());
        log.info("getSiteHealth(): Database Health: {}", databaseHealth.getHealth().toString());
        log.info("getSiteHealth(): Cluster Health: {}", clusterHealth.getHealth().toString());
        if(isHealthy(adminHealth.getHealth()) && isHealthy(databaseHealth.getHealth()) && isHealthy(clusterHealth.getHealth())) {
            siteHealth.setHealth(Health.HEALTHY);
        }

        return Collections.singletonList(siteHealth);
    }

    /**
     * Implementation of {@code tryFailover()}. No controller-level failover
     * options are available in a single node architecture, so 400 Bad Request
     * is returned, and no action is taken.
     *
     * @return an {@code SiteHealth} object with health of the site
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     * @see HealthResolver
     * @see FailoverStatus
     * @see FailoverInput
     */
    @Override
    public FailoverStatus tryFailover(FailoverInput input) {
        log.info("tryFailover(): Failover not supported in the current configuration.");
        return new FailoverStatus().withStatusCode(400).withMessage("Failover not supported in current configuration.");
    }

    /**
     * Implementation of {@code resolveSites()}. Calls
     * {@code resolveSiteForMember()} to resolve which site a member belongs to.
     *
     * @see HealthResolver
     */
    @Override
    public void resolveSites() {
        log.info("Map contains {} entries", memberMap.size());
        memberMap.forEach((key, value) -> resolveSiteForMember(value));
    }

    /**
     * Resolves which site a member belongs to. Since this is a Single node
     * architecture, it is defaulted to <i>Site 1</i>.
     *
     * @see HealthResolver
     */
    private void resolveSiteForMember(ClusterActor actor) {
        actor.setSite("Site 1");
        log.info("resolveSiteForMember(): {} belongs to {}", actor.getNode(), actor.getSite());
    }
}
