/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights
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

package org.onap.ccsdk.sli.plugins.grtoolkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.apache.commons.lang.StringUtils;

import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.plugins.grtoolkit.connection.ConnectionManager;
import org.onap.ccsdk.sli.plugins.grtoolkit.connection.ConnectionResponse;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.AdminHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterActor;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.DatabaseHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.FailoverStatus;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.Health;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.MemberBuilder;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.PropertyKeys;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.SiteHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.resolver.HealthResolver;
import org.onap.ccsdk.sli.plugins.grtoolkit.resolver.SingleNodeHealthResolver;
import org.onap.ccsdk.sli.plugins.grtoolkit.resolver.SixNodeHealthResolver;
import org.onap.ccsdk.sli.plugins.grtoolkit.resolver.ThreeNodeHealthResolver;

import org.json.JSONArray;
import org.json.JSONObject;

import org.opendaylight.controller.cluster.datastore.DistributedDataStoreInterface;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.AdminHealthInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.AdminHealthOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.AdminHealthOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.ClusterHealthInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.ClusterHealthOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.ClusterHealthOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.DatabaseHealthInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.DatabaseHealthOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.DatabaseHealthOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.FailoverInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.FailoverOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.FailoverOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.GrToolkitService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.HaltAkkaTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.HaltAkkaTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.HaltAkkaTrafficOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.Member;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.ResumeAkkaTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.ResumeAkkaTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.ResumeAkkaTrafficOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.Site;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.SiteHealthInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.SiteHealthOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.SiteHealthOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.SiteIdentifierInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.SiteIdentifierOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.SiteIdentifierOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.site.health.output.SitesBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API implementation of the {@code GrToolkitService} interface generated from
 * the gr-toolkit.yang model. The RPCs contained within this class are meant to
 * run in an architecture agnostic fashion, where the response is repeatable
 * and predictable across any given node configuration. To facilitate this,
 * health checking and failover logic has been abstracted into the
 * {@code HealthResolver} classes.
 * <p>
 * Anyone who wishes to write a custom resolver for use with GR Toolkit should
 * extend the {@code HealthResolver} class. The currently provided resolvers
 * are useful references for further implementation.
 *
 * @author Anthony Haddox
 * @see GrToolkitService
 * @see HealthResolver
 * @see SingleNodeHealthResolver
 * @see ThreeNodeHealthResolver
 * @see SixNodeHealthResolver
 */
public class GrToolkitProvider implements AutoCloseable, GrToolkitService, DataTreeChangeListener {
    private static final String APP_NAME = "gr-toolkit";
    private static final String PROPERTIES_FILE = System.getenv("SDNC_CONFIG_DIR") + "/gr-toolkit.properties";
    private String akkaConfig;
    private String httpProtocol;
    private String siteIdentifier = System.getenv("SITE_NAME");
    private final Logger log = LoggerFactory.getLogger(GrToolkitProvider.class);
    private final ExecutorService executor;
    protected DataBroker dataBroker;
    protected NotificationPublishService notificationService;
    protected RpcProviderRegistry rpcRegistry;
    protected BindingAwareBroker.RpcRegistration<GrToolkitService> rpcRegistration;
    protected DbLibService dbLib;
    private String member;
    private ClusterActor self;
    private HashMap<String, ClusterActor> memberMap;
    private Properties properties;
    private DistributedDataStoreInterface configDatastore;
    private HealthResolver resolver;

    /**
     * Constructs the provider for the GR Toolkit API. Dependencies are
     * injected using the GrToolkit.xml blueprint.
     *
     * @param dataBroker The Data Broker
     * @param notificationProviderService The Notification Service
     * @param rpcProviderRegistry The RPC Registry
     * @param configDatastore The Configuration Data Store provided by the controller
     * @param dbLibService Reference to the controller provided DbLibService
     */
    public GrToolkitProvider(DataBroker dataBroker,
                             NotificationPublishService notificationProviderService,
                             RpcProviderRegistry rpcProviderRegistry,
                             DistributedDataStoreInterface configDatastore,
                             DbLibService dbLibService) {
        log.info("Creating provider for {}", APP_NAME);
        this.executor = Executors.newFixedThreadPool(1);
        this.dataBroker = dataBroker;
        this.notificationService = notificationProviderService;
        this.rpcRegistry = rpcProviderRegistry;
        this.configDatastore = configDatastore;
        this.dbLib = dbLibService;
        initialize();
    }

    /**
     * Initializes some structures necessary to hold health check information
     * and perform failovers.
     */
    private void initialize() {
        log.info("Initializing provider for {}", APP_NAME);
        createContainers();
        setProperties();
        defineMembers();
        rpcRegistration = rpcRegistry.addRpcImplementation(GrToolkitService.class, this);
        log.info("Initialization complete for {}", APP_NAME);
    }

    /**
     * Creates the {@code Properties} object with the contents of
     * gr-toolkit.properties, found at the {@code SDNC_CONFIG_DIR} directory,
     * which should be set as an environment variable. If the properties file
     * is not found, GR Toolkit will not function.
     */
    private void setProperties() {
        log.info("Loading properties from {}", PROPERTIES_FILE);
        properties = new Properties();
        File propertiesFile = new File(PROPERTIES_FILE);
        if(!propertiesFile.exists()) {
            log.warn("setProperties(): Properties file not found.");
        } else {
            try(FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
                properties.load(fileInputStream);
                if(!properties.containsKey(PropertyKeys.SITE_IDENTIFIER)) {
                    properties.put(PropertyKeys.SITE_IDENTIFIER, "Unknown Site");
                }
                httpProtocol = "true".equals(properties.getProperty(PropertyKeys.CONTROLLER_USE_SSL).trim()) ? "https://" : "http://";
                akkaConfig = properties.getProperty(PropertyKeys.AKKA_CONF_LOCATION).trim();
                if(StringUtils.isEmpty(siteIdentifier)) {
                    siteIdentifier = properties.getProperty(PropertyKeys.SITE_IDENTIFIER).trim();
                }
                log.info("setProperties(): Loaded properties.");
            } catch(IOException e) {
                log.error("setProperties(): Error loading properties.", e);
            }
        }
    }

    /**
     * Parses the akka.conf file used by the controller to define an akka
     * cluster. This method requires the <i>seed-nodes</i> definition to exist
     * on a single line.
     */
    private void defineMembers() {
        member = configDatastore.getActorUtils().getCurrentMemberName().getName();
        log.info("defineMembers(): Cluster member: {}", member);

        log.info("defineMembers(): Parsing akka.conf for cluster memberMap...");
        try {
            File akkaConfigFile = new File(this.akkaConfig);
            try(FileReader fileReader = new FileReader(akkaConfigFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                while((line = bufferedReader.readLine()) != null) {
                    if(line.contains("seed-nodes =")) {
                        parseSeedNodes(line);
                        break;
                    }
                }
            }
        } catch(IOException e) {
            log.error("defineMembers(): Couldn't load akka", e);
        } catch(NullPointerException e) {
            log.error("defineMembers(): akkaConfig is null. Check properties file and restart {} bundle.", APP_NAME);
            log.error("defineMembers(): NullPointerException", e);
        }
        log.info("self:\n{}", self);
    }

    /**
     * Sets up the {@code InstanceIdentifier}s for Data Store transactions.
     */
    private void createContainers() {
        // Replace with MD-SAL write for FailoverStatus
    }

    /**
     * Shuts down the {@code ExecutorService} and closes the RPC Provider Registry.
     */
    @Override
    public void close() throws Exception {
        log.info("Closing provider for {}", APP_NAME);
        executor.shutdown();
        rpcRegistration.close();
        log.info("close(): Successfully closed provider for {}", APP_NAME);
    }

    /**
     * Listens for changes to the Data tree.
     *
     * @param changes Data tree changes.
     */
    @Override
    public void onDataTreeChanged(@Nonnull Collection changes) {
        log.info("onDataTreeChanged(): No changes.");
    }

    /**
     * Makes a call to {@code resolver.getClusterHealth()} to determine the
     * health of the akka clustered controllers.
     *
     * @param input request body adhering to the model for
     *        {@code ClusterHealthInput}
     * @return response adhering to the model for {@code ClusterHealthOutput}
     * @see HealthResolver
     * @see ClusterHealthInput
     * @see ClusterHealthOutput
     */
    @Override
    public ListenableFuture<RpcResult<ClusterHealthOutput>> clusterHealth(ClusterHealthInput input) {
        log.info("{}:cluster-health invoked.", APP_NAME);
        resolver.getClusterHealth();
        return buildClusterHealthOutput();
    }

    /**
     * Makes a call to {@code resolver.getSiteHealth()} to determine the health
     * of all of the application components of a site. In a multi-site config,
     * this will gather the health of all sites.
     *
     * @param input request body adhering to the model for
     *        {@code SiteHealthInput}
     * @return response adhering to the model for {@code SiteHealthOutput}
     * @see HealthResolver
     * @see SiteHealthInput
     * @see SiteHealthOutput
     */
    @Override
    public ListenableFuture<RpcResult<SiteHealthOutput>> siteHealth(SiteHealthInput input) {
        log.info("{}:site-health invoked.", APP_NAME);
        List<SiteHealth> sites = resolver.getSiteHealth();
        return buildSiteHealthOutput(sites);
    }

    /**
     * Makes a call to {@code resolver.getDatabaseHealth()} to determine the
     * health of the database(s) used by the controller.
     *
     * @param input request body adhering to the model for
     *        {@code DatabaseHealthInput}
     * @return response adhering to the model for {@code DatabaseHealthOutput}
     * @see HealthResolver
     * @see DatabaseHealthInput
     * @see DatabaseHealthOutput
     */
    @Override
    public ListenableFuture<RpcResult<DatabaseHealthOutput>> databaseHealth(DatabaseHealthInput input) {
        log.info("{}:database-health invoked.", APP_NAME);
        DatabaseHealthOutputBuilder outputBuilder = new DatabaseHealthOutputBuilder();
        DatabaseHealth health = resolver.getDatabaseHealth();
        outputBuilder.setStatus(health.getHealth().equals(Health.HEALTHY) ? "200" : "500");
        outputBuilder.setHealth(health.getHealth().toString());
        outputBuilder.setServedBy(member);
        log.info("databaseHealth(): Health: {}", health.getHealth());
        return Futures.immediateFuture(RpcResultBuilder.<DatabaseHealthOutput>status(true).withResult(outputBuilder.build()).build());
    }

    /**
     * Makes a call to {@code resolver.getAdminHealth()} to determine the
     * health of the administrative portal(s) used by the controller.
     *
     * @param input request body adhering to the model for
     *        {@code AdminHealthInput}
     * @return response adhering to the model for {@code AdminHealthOutput}
     * @see HealthResolver
     * @see AdminHealthInput
     * @see AdminHealthOutput
     */
    @Override
    public ListenableFuture<RpcResult<AdminHealthOutput>> adminHealth(AdminHealthInput input) {
        log.info("{}:admin-health invoked.", APP_NAME);
        AdminHealthOutputBuilder outputBuilder = new AdminHealthOutputBuilder();
        AdminHealth adminHealth = resolver.getAdminHealth();
        outputBuilder.setStatus(Integer.toString(adminHealth.getStatusCode()));
        outputBuilder.setHealth(adminHealth.getHealth().toString());
        outputBuilder.setServedBy(member);
        log.info("adminHealth(): Status: {} | Health: {}", adminHealth.getStatusCode(), adminHealth.getHealth());
        return Futures.immediateFuture(RpcResultBuilder.<AdminHealthOutput>status(true).withResult(outputBuilder.build()).build());
    }

    /**
     * Places IP Tables rules in place to drop akka communications traffic with
     * one or mode nodes. This method does not not perform any checks to see if
     * rules currently exist, and assumes success.
     *
     * @param input request body adhering to the model for
     *        {@code HaltAkkaTrafficInput}
     * @return response adhering to the model for {@code HaltAkkaTrafficOutput}
     * @see HaltAkkaTrafficInput
     * @see HaltAkkaTrafficOutput
     */
    @Override
    public ListenableFuture<RpcResult<HaltAkkaTrafficOutput>> haltAkkaTraffic(HaltAkkaTrafficInput input) {
        log.info("{}:halt-akka-traffic invoked.", APP_NAME);
        HaltAkkaTrafficOutputBuilder outputBuilder = new HaltAkkaTrafficOutputBuilder();
        outputBuilder.setStatus("200");
        modifyIpTables(IpTables.ADD, input.getNodeInfo().toArray());
        outputBuilder.setServedBy(member);

        return Futures.immediateFuture(RpcResultBuilder.<HaltAkkaTrafficOutput>status(true).withResult(outputBuilder.build()).build());
    }

    /**
     * Removes IP Tables rules in place to permit akka communications traffic
     * with one or mode nodes. This method does not not perform any checks to
     * see if rules currently exist, and assumes success.
     *
     * @param input request body adhering to the model for
     *        {@code ResumeAkkaTrafficInput}
     * @return response adhering to the model for {@code ResumeAkkaTrafficOutput}
     * @see ResumeAkkaTrafficInput
     * @see ResumeAkkaTrafficOutput
     */
    @Override
    public ListenableFuture<RpcResult<ResumeAkkaTrafficOutput>> resumeAkkaTraffic(ResumeAkkaTrafficInput input) {
        log.info("{}:resume-akka-traffic invoked.", APP_NAME);
        ResumeAkkaTrafficOutputBuilder outputBuilder = new ResumeAkkaTrafficOutputBuilder();
        outputBuilder.setStatus("200");
        modifyIpTables(IpTables.DELETE, input.getNodeInfo().toArray());
        outputBuilder.setServedBy(member);

        return Futures.immediateFuture(RpcResultBuilder.<ResumeAkkaTrafficOutput>status(true).withResult(outputBuilder.build()).build());
    }

    /**
     * Returns a canned response containing the identifier for this
     * controller's site.
     *
     * @param input request body adhering to the model for
     *        {@code SiteIdentifierInput}
     * @return response adhering to the model for {@code SiteIdentifierOutput}
     * @see SiteIdentifierInput
     * @see SiteIdentifierOutput
     */
    @Override
    public ListenableFuture<RpcResult<SiteIdentifierOutput>> siteIdentifier(SiteIdentifierInput input) {
        log.info("{}:site-identifier invoked.", APP_NAME);
        SiteIdentifierOutputBuilder outputBuilder = new SiteIdentifierOutputBuilder();
        outputBuilder.setStatus("200");
        outputBuilder.setId(siteIdentifier);
        outputBuilder.setServedBy(member);
        return Futures.immediateFuture(RpcResultBuilder.<SiteIdentifierOutput>status(true).withResult(outputBuilder.build()).build());
    }

    /**
     * Makes a call to {@code resolver.tryFailover()} to try a failover defined
     * by the active {@code HealthResolver}.
     *
     * @param input request body adhering to the model for
     *        {@code FailoverInput}
     * @return response adhering to the model for {@code FailoverOutput}
     * @see HealthResolver
     * @see FailoverInput
     * @see FailoverOutput
     */
    @Override
    public ListenableFuture<RpcResult<FailoverOutput>> failover(FailoverInput input) {
        log.info("{}:failover invoked.", APP_NAME);
        FailoverOutputBuilder outputBuilder = new FailoverOutputBuilder();
        FailoverStatus failoverStatus = resolver.tryFailover(input);
        outputBuilder.setServedBy(member);
        outputBuilder.setMessage(failoverStatus.getMessage());
        outputBuilder.setStatus(Integer.toString(failoverStatus.getStatusCode()));
        log.info("{}:{}.", APP_NAME, failoverStatus.getMessage());
        return Futures.immediateFuture(RpcResultBuilder.<FailoverOutput>status(true).withResult(outputBuilder.build()).build());
    }

    /**
     * Performs an akka traffic isolation of the active site from the standby
     * site in an Active/Standby architecture. Invokes the
     * {@code halt-akka-traffic} RPC against the standby site nodes using the
     * information of the active site nodes.
     *
     * @param activeSite list of nodes in the active site
     * @param standbySite list of nodes in the standby site
     * @param port http or https port of the controller
     * @deprecated No longer used since the refactor to use the HealthResolver
     *             pattern. Retained so the logic can be replicated later.
     */
    @Deprecated
    private void isolateSiteFromCluster(ArrayList<ClusterActor> activeSite, ArrayList<ClusterActor> standbySite, String port) {
        log.info("isolateSiteFromCluster(): Halting Akka traffic...");
        for(ClusterActor actor : standbySite) {
            try {
                log.info("Halting Akka traffic for: {}", actor.getNode());
                // Build JSON with activeSite actor Node and actor AkkaPort
                JSONObject akkaInput = new JSONObject();
                JSONObject inputBlock = new JSONObject();
                JSONArray votingStateArray = new JSONArray();
                JSONObject nodeInfo;
                for(ClusterActor node : activeSite) {
                    nodeInfo = new JSONObject();
                    nodeInfo.put("node", node.getNode());
                    nodeInfo.put("port", node.getAkkaPort());
                    votingStateArray.put(nodeInfo);
                }
                inputBlock.put("node-info", votingStateArray);
                akkaInput.put("input", inputBlock);
                ConnectionResponse response = ConnectionManager.getConnectionResponse(httpProtocol + actor.getNode() + ":" + port + "/restconf/operations/gr-toolkit:halt-akka-traffic", ConnectionManager.HttpMethod.POST, akkaInput.toString(), "");
            } catch(IOException e) {
                log.error("isolateSiteFromCluster(): Could not halt Akka traffic for: " + actor.getNode(), e);
            }
        }
    }

    /**
     * Invokes the down unreachable action through the Jolokia mbean API.
     *
     * @param activeSite list of nodes in the active site
     * @param standbySite list of nodes in the standby site
     * @param port http or https port of the controller
     * @deprecated No longer used since the refactor to use the HealthResolver
     *             pattern. Retained so the logic can be replicated later.
     */
    @Deprecated
    private void downUnreachableNodes(ArrayList<ClusterActor> activeSite, ArrayList<ClusterActor> standbySite, String port) {
        log.info("downUnreachableNodes(): Setting site unreachable...");
        JSONObject jolokiaInput = new JSONObject();
        jolokiaInput.put("type", "EXEC");
        jolokiaInput.put("mbean", "akka:type=Cluster");
        jolokiaInput.put("operation", "down");
        JSONArray arguments = new JSONArray();
        for(ClusterActor actor : activeSite) {
            // Build Jolokia input
            // May need to change from akka port to actor.getAkkaPort()
            arguments.put("akka.tcp://opendaylight-cluster-data@" + actor.getNode() + ":" + properties.getProperty(PropertyKeys.CONTROLLER_PORT_AKKA));
        }
        jolokiaInput.put("arguments", arguments);
        log.debug("downUnreachableNodes(): {}", jolokiaInput);
        try {
            log.info("downUnreachableNodes(): Setting nodes unreachable");
            ConnectionResponse response = ConnectionManager.getConnectionResponse(httpProtocol + standbySite.get(0).getNode() + ":" + port + "/jolokia", ConnectionManager.HttpMethod.POST, jolokiaInput.toString(), "");
        } catch(IOException e) {
            log.error("downUnreachableNodes(): Error setting nodes unreachable", e);
        }
    }

    /**
     * Triggers a data backup and export sequence of MD-SAL data. Invokes the
     * {@code data-export-import:schedule-export} RPC to schedule a data export
     * and subsequently the {@code daexim-offsite-backup:backup-data} RPC
     * against the active site to export and backup the data. Assumes the
     * controllers have the org.onap.ccsdk.sli.northbound.daeximoffsitebackup
     * bundle installed.
     *
     * @param activeSite list of nodes in the active site
     * @param port http or https port of the controller
     * @deprecated No longer used since the refactor to use the HealthResolver
     *             pattern. Retained so the logic can be replicated later.
     */
    @Deprecated
    private void backupMdSal(ArrayList<ClusterActor> activeSite, String port) {
        log.info("backupMdSal(): Backing up data...");
        try {
            log.info("backupMdSal(): Scheduling backup for: {}", activeSite.get(0).getNode());
            ConnectionResponse response = ConnectionManager.getConnectionResponse(httpProtocol + activeSite.get(0).getNode() + ":" + port + "/restconf/operations/data-export-import:schedule-export", ConnectionManager.HttpMethod.POST, "{ \"input\": { \"run-at\": \"30\" } }", "");
        } catch(IOException e) {
            log.error("backupMdSal(): Error backing up MD-SAL", e);
        }
        for(ClusterActor actor : activeSite) {
            try {
                // Move data offsite
                log.info("backupMdSal(): Backing up data for: {}", actor.getNode());
                ConnectionResponse response = ConnectionManager.getConnectionResponse(httpProtocol + actor.getNode() + ":" + port + "/restconf/operations/daexim-offsite-backup:backup-data", ConnectionManager.HttpMethod.POST, null, "");
            } catch(IOException e) {
                log.error("backupMdSal(): Error backing up data.", e);
            }
        }
    }

    /**
     * Builds a response object for {@code clusterHealth()}. Sorts and iterates
     * over the contents of the {@code memberMap}, which contains the health
     * information of the cluster, and adds them to the {@code outputBuilder}.
     * If the ClusterActor is healthy, according to
     * {@code resolver.isControllerHealthy()}, the {@code ClusterHealthOutput}
     * status has a {@code 0} appended, otherwise a {@code 1} is appended. A
     * status of all zeroes denotes a healthy cluster. This status should be
     * easily decoded by tools which use the output.
     *
     * @return future containing a completed {@code ClusterHealthOutput}
     * @see ClusterActor
     * @see ClusterHealthOutput
     * @see HealthResolver
     */
    @SuppressWarnings("unchecked")
    private ListenableFuture<RpcResult<ClusterHealthOutput>> buildClusterHealthOutput() {
        ClusterHealthOutputBuilder outputBuilder = new ClusterHealthOutputBuilder();
        outputBuilder.setServedBy(member);
        List memberList = new ArrayList<Member>();
        StringBuilder stat = new StringBuilder();
        memberMap.values()
                .stream()
                .sorted(Comparator.comparingInt(member -> Integer.parseInt(member.getMember().split("-")[1])))
                .forEach(member -> {
                    memberList.add(new MemberBuilder(member).build());
                    // 0 is a healthy controller, 1 is unhealthy.
                    // The list is sorted so users can decode to find unhealthy nodes
                    // This will also let them figure out health on a per-site basis
                    // Depending on any tools they use with this API
                    if(resolver.isControllerHealthy(member)) {
                        stat.append("0");
                    } else {
                        stat.append("1");
                    }
                });
        outputBuilder.setStatus(stat.toString());
        outputBuilder.setMembers(memberList);
        RpcResult<ClusterHealthOutput> rpcResult = RpcResultBuilder.<ClusterHealthOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(rpcResult);
    }

    /**
     * Builds a response object for {@code siteHealth()}. Iterates over a list
     * of {@code SiteHealth} objects and populates the {@code SiteHealthOutput}
     * with the information.
     *
     * @param sites list of sites
     * @return future containing a completed {@code SiteHealthOutput}
     * @see SiteHealth
     * @see HealthResolver
     */
    @SuppressWarnings("unchecked")
    private ListenableFuture<RpcResult<SiteHealthOutput>> buildSiteHealthOutput(List<SiteHealth> sites) {
        SiteHealthOutputBuilder outputBuilder = new SiteHealthOutputBuilder();
        SitesBuilder siteBuilder = new SitesBuilder();
        outputBuilder.setStatus("200");
        outputBuilder.setSites((List) new ArrayList<Site>());

        for(SiteHealth site : sites) {
            siteBuilder.setHealth(site.getHealth().toString());
            siteBuilder.setRole(site.getRole());
            siteBuilder.setId(site.getId());
            outputBuilder.getSites().add(siteBuilder.build());
            log.info("buildSiteHealthOutput(): Health for {}: {}", site.getId(), site.getHealth().getHealth());
        }

        outputBuilder.setServedBy(member);
        RpcResult<SiteHealthOutput> rpcResult = RpcResultBuilder.<SiteHealthOutput>status(true).withResult(outputBuilder.build()).build();
        return Futures.immediateFuture(rpcResult);
    }

    /**
     * Parses a line containing the akka networking information of the akka
     * controller cluster. Assumes entries of the format:
     * <p>
     * akka.tcp://opendaylight-cluster-data@<FQDN>:<AKKA_PORT>
     * <p>
     * The information is stored in a {@code ClusterActor} object, and then
     * added to the memberMap HashMap, with the {@code FQDN} as the key. The
     * final step is a call to {@code createHealthResolver} to create the
     * health resolver for the provider.
     *
     * @param line the line containing all of the seed nodes
     * @see ClusterActor
     * @see HealthResolver
     */
    private void parseSeedNodes(String line) {
        memberMap = new HashMap<>();
        line = line.substring(line.indexOf("[\""), line.indexOf(']'));
        String[] splits = line.split(",");

        for(int ndx = 0; ndx < splits.length; ndx++) {
            String nodeName = splits[ndx];
            int delimLocation = nodeName.indexOf('@');
            String port = nodeName.substring(splits[ndx].indexOf(':', delimLocation) + 1, splits[ndx].indexOf('"', splits[ndx].indexOf(':')));
            splits[ndx] = nodeName.substring(delimLocation + 1, splits[ndx].indexOf(':', delimLocation));
            log.info("parseSeedNodes(): Adding node: {}:{}", splits[ndx], port);
            ClusterActor clusterActor = new ClusterActor();
            clusterActor.setNode(splits[ndx]);
            clusterActor.setAkkaPort(port);
            clusterActor.setMember("member-" + (ndx + 1));
            if(member.equals(clusterActor.getMember())) {
                self = clusterActor;
            }
            memberMap.put(clusterActor.getNode(), clusterActor);
            log.info("parseSeedNodes(): {}", clusterActor);
        }

        createHealthResolver();
    }

    /**
     * Creates the specific health resolver requested by the user, as specified
     * in the gr-toolkit.properties file. If a resolver is not specified, or
     * there is an issue creating the resolver, it will use a fallback resolver
     * based on how many nodes are added to the memberMap HashMap.
     *
     * @see HealthResolver
     * @see SingleNodeHealthResolver
     * @see ThreeNodeHealthResolver
     * @see SixNodeHealthResolver
     */
    private void createHealthResolver() {
        log.info("createHealthResolver(): Creating health resolver...");
        try {
            Class resolverClass = null;
            String userDefinedResolver = properties.getProperty(PropertyKeys.RESOLVER);
            if(StringUtils.isEmpty(userDefinedResolver)) {
                throw new InstantiationException();
            }
            resolverClass = Class.forName(userDefinedResolver);
            Class[] types = { Map.class , properties.getClass(), DbLibService.class };
            Constructor<HealthResolver> constructor = resolverClass.getConstructor(types);
            Object[] parameters = { memberMap, properties, dbLib };
            resolver = constructor.newInstance(parameters);
            log.info("createHealthResolver(): Created resolver from name {}", resolver.toString());
        } catch(ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.warn("createHealthResolver(): Could not create user defined resolver", e);
            if(memberMap.size() == 1) {
                log.info("createHealthResolver(): FALLBACK: Initializing SingleNodeHealthResolver...");
                resolver  = new SingleNodeHealthResolver(memberMap, properties, dbLib);
            } else if(memberMap.size() == 3) {
                log.info("createHealthResolver(): FALLBACK: Initializing ThreeNodeHealthResolver...");
                resolver  = new ThreeNodeHealthResolver(memberMap, properties, dbLib);
            } else if(memberMap.size() == 6) {
                log.info("createHealthResolver(): FALLBACK: Initializing SixNodeHealthResolver...");
                resolver  = new SixNodeHealthResolver(memberMap, properties, dbLib);
            }
        }
    }

    /**
     * Adds or drops IPTables rules to block or resume akka traffic for a node
     * in the akka cluster. Assumes that the user or group that the controller
     * is run as has the ability to run sudo /sbin/iptables without requiring a
     * password. This method will run indefinitely if that assumption is not
     * correct. This method does not check to see if any rules around the node
     * are preexisting, so multiple uses will result in multiple additions and
     * removals from IPTables.
     *
     * @param task the operation to be performed against IPTables
     * @param nodeInfo array containing the nodes to be added or dropped from
     *                 IPTables
     */
    private void modifyIpTables(IpTables task, Object[] nodeInfo) {
        log.info("modifyIpTables(): Modifying IPTables rules...");
        if(task == IpTables.ADD) {
            for(Object node : nodeInfo) {
                org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.halt.akka.traffic.input.NodeInfo n =
                        (org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.halt.akka.traffic.input.NodeInfo) node;
                log.info("modifyIpTables(): Isolating {}", n.getNode());
                executeCommand(String.format("sudo /sbin/iptables -A INPUT -p tcp --destination-port %s -j DROP -s %s", properties.get(PropertyKeys.CONTROLLER_PORT_AKKA), n.getNode()));
                executeCommand(String.format("sudo /sbin/iptables -A OUTPUT -p tcp --destination-port %s -j DROP -d %s", n.getPort(), n.getNode()));
            }
        } else if(task == IpTables.DELETE) {
            for(Object node : nodeInfo) {
                org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.resume.akka.traffic.input.NodeInfo n =
                        (org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.resume.akka.traffic.input.NodeInfo) node;
                log.info("modifyIpTables(): De-isolating {}", n.getNode());
                executeCommand(String.format("sudo /sbin/iptables -D INPUT -p tcp --destination-port %s -j DROP -s %s", properties.get(PropertyKeys.CONTROLLER_PORT_AKKA), n.getNode()));
                executeCommand(String.format("sudo /sbin/iptables -D OUTPUT -p tcp --destination-port %s -j DROP -d %s", n.getPort(), n.getNode()));
            }
        }
        if(nodeInfo.length > 0) {
            executeCommand("sudo /sbin/iptables -L");
        }
    }

    /**
     * Opens a shell session and executes a command.
     *
     * @param command the shell command to execute
     */
    private void executeCommand(String command) {
        log.info("executeCommand(): Executing command: {}", command);
        String[] cmd = command.split(" ");
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while((inputLine = bufferedReader.readLine()) != null) {
                content.append(inputLine);
            }
            bufferedReader.close();
            log.info("executeCommand(): {}", content);
        } catch(IOException e) {
            log.error("executeCommand(): Error executing command", e);
        }
    }

    /**
     * The IPTables operations this module can perform.
     */
    enum IpTables {
        ADD,
        DELETE
    }
}