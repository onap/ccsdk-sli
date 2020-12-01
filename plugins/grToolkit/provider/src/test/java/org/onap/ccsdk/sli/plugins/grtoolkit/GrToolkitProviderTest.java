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
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import com.google.common.util.concurrent.ListenableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import org.onap.ccsdk.sli.core.dblib.DBLibConnection;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterActor;

import org.opendaylight.controller.cluster.access.concepts.MemberName;
import org.opendaylight.controller.cluster.datastore.DistributedDataStoreInterface;
import org.opendaylight.controller.cluster.datastore.utils.ActorUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.AdminHealthOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.ClusterHealthOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.DatabaseHealthOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.FailoverOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.HaltAkkaTrafficInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.HaltAkkaTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.ResumeAkkaTrafficInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.ResumeAkkaTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.SiteHealthOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.SiteIdentifierOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class GrToolkitProviderTest {
    GrToolkitProvider provider;
    GrToolkitProvider providerSpy;
    DataBroker dataBroker;
    NotificationPublishService notificationProviderService;
    RpcProviderRegistry rpcProviderRegistry;
    DistributedDataStoreInterface configDatastore;
    DbLibService dbLibService;
    DBLibConnection connection;
    Properties properties;

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);

    @Before
    public void setup() {
        environmentVariables.set("SDNC_CONFIG_DIR","src/test/resources");
        dataBroker = mock(DataBroker.class);
        notificationProviderService = mock(NotificationPublishService.class);
        rpcProviderRegistry = mock(RpcProviderRegistry.class);
        configDatastore = mock(DistributedDataStoreInterface.class);
        dbLibService = mock(DbLibService.class);
        connection = mock(DBLibConnection.class);

        ActorUtils actorContext = mock(ActorUtils.class);
        MemberName memberName = MemberName.forName("Test");

        when(actorContext.getCurrentMemberName()).thenReturn(memberName);
        when(configDatastore.getActorUtils()).thenReturn(actorContext);

        try {
            when(connection.isReadOnly()).thenReturn(false);
            when(connection.isClosed()).thenReturn(false);
            when(dbLibService.isActive()).thenReturn(true);
            when(dbLibService.getConnection()).thenReturn(connection);
        } catch(SQLException e) {
            fail();
        }

        provider = new GrToolkitProvider(dataBroker, notificationProviderService,
                rpcProviderRegistry, configDatastore, dbLibService);
        providerSpy = spy(provider);
        stubController();
    }

    @Test
    public void closeTest() {
        try {
            provider.close();
        } catch(Exception e) {
            // Exception expected
        }
    }

    @Test
    public void onDataTreeChangedTest() {
        provider.onDataTreeChanged(new ArrayList());
        // onDataTreeChanged is an empty stub
    }

    private void stubController() {
        String clusterBody = null;
        String shardManagerBody = null;
        String shardDefaultBody = null;
        String shardOperationalBody = null;
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/three/cluster.json"))) {
            clusterBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/three/shard-manager.json"))) {
            shardManagerBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/three/default-config.json"))) {
            shardDefaultBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/three/default-operational.json"))) {
            shardOperationalBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }

        if(clusterBody == null || shardManagerBody == null || shardDefaultBody == null || shardOperationalBody == null) {
            fail();
        }
        stubFor(get(urlEqualTo("/adm/healthcheck")).willReturn(aResponse().withStatus(200)));
        stubFor(get(urlEqualTo("/jolokia/read/akka:type=Cluster")).willReturn(aResponse().withStatus(200).withBody(clusterBody)));
        stubFor(get(urlEqualTo("/jolokia/read/org.opendaylight.controller:Category=ShardManager,name=shard-manager-config,type=DistributedConfigDatastore")).inScenario("testing").willReturn(aResponse().withStatus(200).withBody(shardManagerBody)).willSetStateTo("next"));
        stubFor(get(urlEqualTo("/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-default-config,type=DistributedConfigDatastore")).inScenario("testing").willReturn(aResponse().withStatus(200).withBody(shardDefaultBody)).willSetStateTo("next"));
        stubFor(get(urlEqualTo("/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-default-operational,type=DistributedOperationalDatastore")).willReturn(aResponse().withStatus(200).withBody(shardOperationalBody)));
    }

    @Test
    public void clusterHealthTest() {
        ListenableFuture<RpcResult<ClusterHealthOutput>> result = provider.clusterHealth(null);
        try {
            assertEquals("0", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void siteHealthTest() {
        ListenableFuture<RpcResult<SiteHealthOutput>> result = provider.siteHealth(null);
        try {
            assertEquals("200", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void databaseHealthTest() {
        ListenableFuture<RpcResult<DatabaseHealthOutput>> result = provider.databaseHealth(null);
        try {
            assertEquals("200", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void databaseHealthWhenROTest() {
        try {
            when(connection.isReadOnly()).thenReturn(true);
        } catch(SQLException e) {
            fail();
        }
        ListenableFuture<RpcResult<DatabaseHealthOutput>> result = provider.databaseHealth(null);
        try {
            assertEquals("500", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void databaseHealthWhenExceptionTest() {
        try {
            when(connection.isReadOnly()).thenThrow(new SQLException());
        } catch(SQLException e) {
            //expected
        }
        ListenableFuture<RpcResult<DatabaseHealthOutput>> result = provider.databaseHealth(null);
        try {
            assertEquals("500", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void adminHealthTest() {
        ListenableFuture<RpcResult<AdminHealthOutput>> result = provider.adminHealth(null);
        try {
            assertEquals("200", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void siteIdentifierTest() {
        ListenableFuture<RpcResult<SiteIdentifierOutput>> result = provider.siteIdentifier(null);
        try {
            assertEquals("200", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void failoverTest() {
        ListenableFuture<RpcResult<FailoverOutput>> result = provider.failover(null);
        try {
            assertEquals("400", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void haltTrafficTest() {
        HaltAkkaTrafficInputBuilder builder = new HaltAkkaTrafficInputBuilder();
        builder.setNodeInfo(new ArrayList<>());
        ListenableFuture<RpcResult<HaltAkkaTrafficOutput>> result = provider.haltAkkaTraffic(builder.build());
        try {
            assertEquals("200", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void resumeTrafficTest() {
        ResumeAkkaTrafficInputBuilder builder = new ResumeAkkaTrafficInputBuilder();
        builder.setNodeInfo(new ArrayList<>());
        ListenableFuture<RpcResult<ResumeAkkaTrafficOutput>> result = provider.resumeAkkaTraffic(builder.build());
        try {
            assertEquals("200", result.get().getResult().getStatus());
        } catch(InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void executeCommandTest() {
        try {
            Method method = provider.getClass().getDeclaredMethod("executeCommand", String.class);
            method.setAccessible(true);
            method.invoke(provider, "ls");
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail();
        }
    }

    @Test
    public void isolateSiteFromClusterTest() {
        try {
            ClusterActor actor = new ClusterActor();
            actor.setNode("some-node");
            actor.setAkkaPort("2550");
            ArrayList<ClusterActor> activeList = new ArrayList<>();
            activeList.add(actor);
            ArrayList<ClusterActor> standbyList = new ArrayList<>();
            standbyList.add(actor);
            Method method = provider.getClass().getDeclaredMethod("isolateSiteFromCluster", ArrayList.class, ArrayList.class, String.class);
            method.setAccessible(true);
            method.invoke(provider, activeList, standbyList, "80");
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail();
        }
    }

    @Test
    public void downUnreachableNodesTest() {
        try {
            ClusterActor actor = new ClusterActor();
            actor.setNode("some-node");
            actor.setAkkaPort("2550");
            ArrayList<ClusterActor> activeList = new ArrayList<>();
            activeList.add(actor);
            ArrayList<ClusterActor> standbyList = new ArrayList<>();
            standbyList.add(actor);
            Method method = provider.getClass().getDeclaredMethod("downUnreachableNodes", ArrayList.class, ArrayList.class, String.class);
            method.setAccessible(true);
            method.invoke(provider, activeList, standbyList, "80");
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail();
        }
    }

    @Test
    public void backupMdSalTest() {
        try {
            ClusterActor actor = new ClusterActor();
            actor.setNode("some-Node");
            actor.setAkkaPort("2550");
            ArrayList<ClusterActor> activeList = new ArrayList<>();
            activeList.add(actor);
            Method method = provider.getClass().getDeclaredMethod("backupMdSal", ArrayList.class, String.class);
            method.setAccessible(true);
            method.invoke(provider, activeList, "80");
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail();
        }
    }
}
