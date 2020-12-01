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

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.onap.ccsdk.sli.core.dblib.DBLibConnection;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.AdminHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterActor;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.DatabaseHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.FailoverStatus;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.Health;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.SiteHealth;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SixNodeHealthResolverTest {
    private Map<String, ClusterActor> memberMap;
    private DbLibService dbLibService;
    private DBLibConnection connection;
    private SixNodeHealthResolver resolver;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);

    @Before
    public void setUp() {
        memberMap = generateMemberMap(6);
        Properties properties = new Properties();
        try(FileInputStream fileInputStream = new FileInputStream("src/test/resources/six/gr-toolkit.properties")) {
            properties.load(fileInputStream);
        } catch(IOException e) {
            fail();
        }

        dbLibService = mock(DbLibService.class);
        connection = mock(DBLibConnection.class);
        resolver = new SixNodeHealthResolver(memberMap, properties, dbLibService);
    }

    private Map<String, ClusterActor> generateMemberMap(int memberCount) {
        Map<String, ClusterActor> map = new HashMap<>();
        ClusterActor actor;
        for(int ndx = 0; ndx < memberCount; ndx++) {
            actor = new ClusterActor();
            actor.setNode("127.0.1." + (ndx + 1));
            actor.setAkkaPort("2550");
            actor.setMember("member-" + (ndx + 1));
            actor.setUp(true);
            actor.setUnreachable(false);

            map.put(actor.getNode(),  actor);
        }
        return map;
    }

    @Test
    public void getAdminHealthFaulty() {
        stubFor(get(urlEqualTo("/adm/healthcheck")).willReturn(aResponse().withStatus(500)));
        AdminHealth health = resolver.getAdminHealth();
        assertNotNull(health);
        assertEquals(500, health.getStatusCode());
        assertEquals(Health.FAULTY, health.getHealth());
    }

    @Test
    public void getAdminHealthHealthy() {
        stubFor(get(urlEqualTo("/adm/healthcheck")).willReturn(aResponse().withStatus(200)));
        AdminHealth health = resolver.getAdminHealth();
        assertNotNull(health);
        assertEquals(200, health.getStatusCode());
        assertEquals(Health.HEALTHY, health.getHealth());
    }

    @Test
    public void getDatabaseHealth() {
        try {
            when(connection.isReadOnly()).thenReturn(false);
            when(connection.isClosed()).thenReturn(false);
            when(dbLibService.isActive()).thenReturn(true);
            when(dbLibService.getConnection()).thenReturn(connection);
        } catch(SQLException e) {
            fail();
        }
        DatabaseHealth health = resolver.getDatabaseHealth();
        assertEquals(Health.HEALTHY, health.getHealth());
    }

    @Test
    public void getDatabaseHealthFaulty() {
        try {
            when(connection.isReadOnly()).thenReturn(true);
            when(connection.isClosed()).thenReturn(true);
            when(dbLibService.isActive()).thenReturn(false);
            when(dbLibService.getConnection()).thenReturn(connection);
        } catch(SQLException e) {
            fail();
        }
        DatabaseHealth health = resolver.getDatabaseHealth();
        assertEquals(Health.FAULTY, health.getHealth());
    }

    @Test
    public void getDatabaseHealthException() {
        try {
            when(connection.isReadOnly()).thenThrow(new SQLException());
            when(connection.isClosed()).thenReturn(true);
            when(dbLibService.isActive()).thenReturn(false);
            when(dbLibService.getConnection()).thenReturn(connection);
        } catch(SQLException e) {
            fail();
        }
        DatabaseHealth health = resolver.getDatabaseHealth();
        assertEquals(Health.FAULTY, health.getHealth());
    }

    @Test
    public void siteIdentifier() {
        assertEquals("TestODL", resolver.getSiteIdentifier());
        resolver.setSiteIdentifier("NewTestODL");
        assertEquals("NewTestODL", resolver.getSiteIdentifier());
    }

    @Test
    public void getClusterHealth() {
        stubController();
        ClusterHealth health = resolver.getClusterHealth();
        assertEquals(Health.HEALTHY, health.getHealth());
    }

    private void stubController() {
        String clusterBody = null;
        String shardManagerBody = null;
        String shardDefaultBody = null;
        String shardOperationalBody = null;
        String componentBody = null;
        String identifierBody = null;
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/six/cluster.json"))) {
            clusterBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/six/shard-manager.json"))) {
            shardManagerBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/six/default-config.json"))) {
            shardDefaultBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/six/default-operational.json"))) {
            shardOperationalBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/six/component-health.json"))) {
            componentBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }
        try(Stream<String> stream = Files.lines(Paths.get("src/test/resources/six/site-identifier.json"))) {
            identifierBody = stream.collect(Collectors.joining());
        } catch(IOException e) {
            fail();
        }

        if(clusterBody == null || shardManagerBody == null || shardDefaultBody == null || shardOperationalBody == null
            || componentBody == null || identifierBody == null) {
            fail();
        }
        stubFor(get(urlEqualTo("/jolokia/read/akka:type=Cluster")).willReturn(aResponse().withStatus(200).withBody(clusterBody)));
        stubFor(get(urlEqualTo("/jolokia/read/org.opendaylight.controller:Category=ShardManager,name=shard-manager-config,type=DistributedConfigDatastore")).inScenario("testing").willReturn(aResponse().withStatus(200).withBody(shardManagerBody)).willSetStateTo("next"));
        stubFor(get(urlEqualTo("/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-default-config,type=DistributedConfigDatastore")).inScenario("testing").willReturn(aResponse().withStatus(200).withBody(shardDefaultBody)).willSetStateTo("next"));
        stubFor(get(urlEqualTo("/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-default-operational,type=DistributedOperationalDatastore")).willReturn(aResponse().withStatus(200).withBody(shardOperationalBody)));
        stubFor(post(urlEqualTo("/restconf/operations/gr-toolkit:site-identifier")).willReturn(aResponse().withStatus(200).withBody(identifierBody)));
        stubFor(post(urlEqualTo("/restconf/operations/gr-toolkit:admin-health")).inScenario("testing").willReturn(aResponse().withStatus(200).withBody(componentBody)).willSetStateTo("next"));
        stubFor(post(urlEqualTo("/restconf/operations/gr-toolkit:database-health")).willReturn(aResponse().withStatus(200).withBody(componentBody)));
    }

    @Test
    public void getSiteHealth() {
        stubController();
        stubFor(get(urlEqualTo("/adm/healthcheck")).willReturn(aResponse().withStatus(200)));
        try {
            when(connection.isReadOnly()).thenReturn(false);
            when(connection.isClosed()).thenReturn(false);
            when(dbLibService.isActive()).thenReturn(true);
            when(dbLibService.getConnection()).thenReturn(connection);
        } catch(SQLException e) {
            fail();
        }
        List<SiteHealth> health = resolver.getSiteHealth();
        assertNotNull(health);
        assertNotEquals(0, health.size());
        assertEquals(2, health.size());
        assertEquals(Health.HEALTHY, health.get(0).getHealth());
    }

    @Test
    public void getSiteHealthFaulty() {
        stubController();
        stubFor(get(urlEqualTo("/adm/healthcheck")).willReturn(aResponse().withStatus(200)));
        try {
            when(connection.isReadOnly()).thenReturn(false);
            when(connection.isClosed()).thenReturn(false);
            when(dbLibService.isActive()).thenReturn(true);
            when(dbLibService.getConnection()).thenReturn(connection);
        } catch(SQLException e) {
            fail();
        }
        stubFor(get(urlEqualTo("/jolokia/read/org.opendaylight.controller:Category=ShardManager,name=shard-manager-config,type=DistributedConfigDatastore")).inScenario("testing").whenScenarioStateIs("next").willReturn(aResponse().withBodyFile("nonexistent")));
        List<SiteHealth> health = resolver.getSiteHealth();
        assertNotNull(health);
        assertNotEquals(0, health.size());
        assertEquals(2, health.size());
        assertEquals(Health.FAULTY, health.get(0).getHealth());
    }

    @Test
    public void getSiteHealthFaultyShard() {
        stubController();
        stubFor(get(urlEqualTo("/adm/healthcheck")).willReturn(aResponse().withStatus(200)));
        try {
            when(connection.isReadOnly()).thenReturn(false);
            when(connection.isClosed()).thenReturn(false);
            when(dbLibService.isActive()).thenReturn(true);
            when(dbLibService.getConnection()).thenReturn(connection);
        } catch(SQLException e) {
            fail();
        }
        stubFor(get(urlEqualTo("/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-default-config,type=DistributedConfigDatastore")).inScenario("testing").willReturn(aResponse().withBodyFile("nonexistent")).willSetStateTo("next"));
        List<SiteHealth> health = resolver.getSiteHealth();
        assertNotNull(health);
        assertNotEquals(0, health.size());
        assertEquals(2, health.size());
        assertEquals(Health.FAULTY, health.get(0).getHealth());
    }

    @Test
    public void getSiteHealthFaultyCluster() {
        stubController();
        stubFor(get(urlEqualTo("/adm/healthcheck")).willReturn(aResponse().withStatus(200)));
        try {
            when(connection.isReadOnly()).thenReturn(false);
            when(connection.isClosed()).thenReturn(false);
            when(dbLibService.isActive()).thenReturn(true);
            when(dbLibService.getConnection()).thenReturn(connection);
        } catch(SQLException e) {
            fail();
        }
        stubFor(get(urlEqualTo("/jolokia/read/akka:type=Cluster")).willReturn(aResponse().withStatus(200).withBodyFile("nonexistent")));
        List<SiteHealth> health = resolver.getSiteHealth();
        assertNotNull(health);
        assertNotEquals(0, health.size());
        assertEquals(2, health.size());
        assertEquals(Health.FAULTY, health.get(0).getHealth());
    }

    @Test
    public void getSiteHealthFaultyAdmin() {
        stubController();
        stubFor(post(urlEqualTo("/restconf/operations/gr-toolkit:admin-health")).inScenario("testing").willReturn(aResponse().withBodyFile("nonexistent")).willSetStateTo("next"));
        stubFor(get(urlEqualTo("/restconf/operations/gr-toolkit:admin-health")).inScenario("testing").whenScenarioStateIs("next").willReturn(aResponse().withBodyFile("nonexistent")));
        try {
            when(connection.isReadOnly()).thenReturn(false);
            when(connection.isClosed()).thenReturn(false);
            when(dbLibService.isActive()).thenReturn(true);
            when(dbLibService.getConnection()).thenReturn(connection);
        } catch(SQLException e) {
            fail();
        }
        List<SiteHealth> health = resolver.getSiteHealth();
        assertNotNull(health);
        assertNotEquals(0, health.size());
        assertEquals(2, health.size());
        assertEquals(Health.FAULTY, health.get(0).getHealth());
        assertEquals(Health.FAULTY, health.get(1).getHealth());
    }

    @Test
    public void tryFailover() {
        stubController();
        stubFor(get(urlEqualTo("/restconf/operations/cluster-admin:change-member-voting-states-for-all-shards")).willReturn(aResponse().withStatus(200)));
        FailoverStatus status = resolver.tryFailover(null);
        assertEquals(500, status.getStatusCode());
    }
}