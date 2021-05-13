/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ContextFactory;
import com.google.common.collect.ImmutableMap;
import com.woorea.openstack.base.client.OpenStackClientConnector;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.api.TokensResource;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Access.Service;
import com.woorea.openstack.keystone.model.Access.Service.Endpoint;
import com.woorea.openstack.keystone.model.Tenant;
import com.woorea.openstack.keystone.model.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * This class tests the service catalog against a known provider.
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class TestServiceCatalogV2 {

    // Number
    private static int EXPECTED_REGIONS = 1;
    private static int EXPECTED_ENDPOINTS = 1;

    private static String PRINCIPAL;
    private static String CREDENTIAL;
    private static String TENANT_NAME;
    private static String TENANT_ID;
    private static String IDENTITY_URL;
    private static String REGION_NAME;
    private static String PUBLIC_URL;

    private static String IP;
    private static String PORT;
    private static String TENANTID;
    private static String VMID;
    private static String URL;

    private ServiceCatalogV2 catalog;

    private Properties properties;

    @Mock
    private Tenant tenant;

    private final Set<String> regions = new HashSet<>(Arrays.asList("RegionOne"));

    private Map<String, Service> serviceTypes;

    private Map<String, List<Service.Endpoint>> serviceEndpoints;

    @BeforeClass
    public static void before() {
        final Properties props = ConfigurationFactory.getConfiguration().getProperties();
        IDENTITY_URL = props.getProperty("provider1.identity", "appc");
        PRINCIPAL = props.getProperty("provider1.tenant1.userid", "appc");
        CREDENTIAL = props.getProperty("provider1.tenant1.password", "appc");
        TENANT_NAME = props.getProperty("provider1.tenant1.name", "appc");
        TENANT_ID = props.getProperty("provider1.tenant1.id",
                props.getProperty("test.tenantid", "abcde12345fghijk6789lmnopq123rst"));
        REGION_NAME = props.getProperty("provider1.tenant1.region", "RegionOne");

        IP = props.getProperty("test.ip");
        PORT = props.getProperty("test.port");
        TENANTID = props.getProperty("test.tenantid");
        VMID = props.getProperty("test.vmid");

        EXPECTED_REGIONS = Integer.valueOf(props.getProperty("test.expected-regions", "0"));
        EXPECTED_ENDPOINTS = Integer.valueOf(props.getProperty("test.expected-endpoints", "0"));

        PUBLIC_URL =
                "http://192.168.1.2:5000/v2/abcde12345fghijk6789lmnopq123rst/servers/abc12345-1234-5678-890a-abcdefg12345";
    }

    /**
     * Setup the test environment by loading a new service catalog for each test Use reflection to
     * locate fields and methods so that they can be manipulated during the test to change the
     * internal state accordingly.
     *
     */
    @Before
    public void setup() {
        URL = String.format("http://%s:%s/v2/%s/servers/%s", IP, PORT, TENANTID, VMID);
        properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_PROXY_HOST, "PROXY_HOST");
        properties.setProperty(ContextFactory.PROPERTY_PROXY_PORT, "PROXY_PORT");
        catalog = new ServiceCatalogV2(IDENTITY_URL, TENANT_NAME, PRINCIPAL, CREDENTIAL, properties);
        final Service service = new Service();
        serviceTypes = ImmutableMap.<String, Service>builder().put(ServiceCatalog.COMPUTE_SERVICE, service)
                .put(ServiceCatalog.IDENTITY_SERVICE, service).put(ServiceCatalog.IMAGE_SERVICE, service)
                .put(ServiceCatalog.NETWORK_SERVICE, service).put(ServiceCatalog.VOLUME_SERVICE, service).build();
        Map<String, Object> endpointPrivateFields =
                ImmutableMap.<String, Object>builder().put("publicURL", PUBLIC_URL).put("region", REGION_NAME).build();
        Service.Endpoint endpoint = new Service.Endpoint();
        CommonUtility.injectMockObjects(endpointPrivateFields, endpoint);
        final List<Service.Endpoint> endpoints = Arrays.asList(endpoint);
        serviceEndpoints = ImmutableMap.<String, List<Service.Endpoint>>builder()
                .put(ServiceCatalog.COMPUTE_SERVICE, endpoints).build();
        Map<String, Object> privateFields =
                ImmutableMap.<String, Object>builder().put("regions", regions).put("tenant", tenant)
                        .put("serviceTypes", serviceTypes).put("serviceEndpoints", serviceEndpoints).build();
        CommonUtility.injectMockObjects(privateFields, catalog);
        CommonUtility.injectMockObjectsInBaseClass(privateFields, catalog);

    }

    /**
     * Ensure that we get the Tenant Name & Tenant Id property are returned correctly
     */
    @Test
    public void testKnownTenant() {
        when(tenant.getName()).thenReturn(TENANT_NAME);
        when(tenant.getId()).thenReturn(TENANT_ID);
        assertEquals(TENANT_NAME, catalog.getProjectName());
        assertEquals(TENANT_ID, catalog.getProjectId());
    }

    /**
     * Ensure that we set up the Region property correctly
     */
    @Test
    public void testKnownRegions() {
        assertEquals(EXPECTED_REGIONS, catalog.getRegions().size());
        assertEquals(REGION_NAME, catalog.getRegions().toArray()[0]);
    }

    /**
     * Ensure that that we can check for published services correctly
     */
    @Test
    public void testServiceTypesPublished() {
        assertTrue(catalog.isServicePublished("compute"));
        assertFalse(catalog.isServicePublished("bogus"));
    }

    /**
     * Ensure that we can get the list of published services
     */
    @Test
    public void testPublishedServicesList() {
        final List<String> services = catalog.getServiceTypes();
        assertTrue(services.contains(ServiceCatalog.COMPUTE_SERVICE));
        assertTrue(services.contains(ServiceCatalog.IDENTITY_SERVICE));
        assertTrue(services.contains(ServiceCatalog.IMAGE_SERVICE));
        assertTrue(services.contains(ServiceCatalog.NETWORK_SERVICE));
        assertTrue(services.contains(ServiceCatalog.VOLUME_SERVICE));
    }

    /**
     * Ensure that we can get the endpoint(s) for a service
     */
    @Test
    public void testEndpointList() {
        List<Endpoint> endpoints = catalog.getEndpoints(ServiceCatalog.COMPUTE_SERVICE);
        assertNotNull(endpoints);
        assertFalse(endpoints.isEmpty());
        assertEquals(EXPECTED_ENDPOINTS, endpoints.size());
    }

    /**
     * Ensure that we override the toString method
     */
    @Test
    public void testToString() {
        when(tenant.getId()).thenReturn(TENANT_ID);
        when(tenant.getDescription()).thenReturn("Tenant one");
        final String testString = catalog.toString();
        assertNotNull(testString);
    }

    /**
     * Ensure that we can get the VM Region
     */
    @Test
    public void testGetVMRegion() {
        VMURL url = VMURL.parseURL(URL);
        String region = catalog.getVMRegion(url);
        assertEquals(REGION_NAME, region);
    }

    /**
     * Ensure that we can get the null region when no URL is passed
     */
    @Test
    public void testGetVMRegionWithoutURL() {
        String region = catalog.getVMRegion(null);
        assertNull(region);
    }

    @Ignore
    @Test
    public void liveConnectionTest() {
        // this test should only be used by developers when testing against a live Openstack
        // instance, otherwise it should be ignored
        properties = new Properties();
        String identity = "http://192.168.0.1:5000/v2.0";
        String tenantName = "Tenant";
        String user = "user";
        String pass = "pass";

        ServiceCatalogV2 catalog = new ServiceCatalogV2(identity, tenantName, user, pass, properties);

        try {
            catalog.init();
        } catch (ZoneException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String out = catalog.toString();
        System.out.println(out);
        assertNotNull(catalog);
    }

    @Test
    public void testInit() throws ZoneException, ClassNotFoundException, InstantiationException, IllegalAccessException, OpenStackConnectException, OpenStackResponseException {
        ServiceCatalogV2 catalogSpy = Mockito.spy(catalog);
        Class<?> connectorClass = Class.forName(ServiceCatalogV2.CLIENT_CONNECTOR_CLASS);
        OpenStackClientConnector connector = (OpenStackClientConnector) connectorClass.newInstance();
        Keystone keystone = Mockito.spy(new Keystone(IDENTITY_URL, connector));
        TokensResource tokens = Mockito.mock(TokensResource.class);
        TokensResource.Authenticate authenticate = Mockito.mock(TokensResource.Authenticate.class);
        Mockito.when(keystone.tokens()).thenReturn(tokens);
        Mockito.when(tokens.authenticate(Mockito.any())).thenReturn(authenticate);
        Access access = Mockito.mock(Access.class);

        Token token = new Token();
        Mockito.when(access.getToken()).thenReturn(token);
        Mockito.when(authenticate.execute()).thenReturn(access);
        Mockito.when(authenticate.withTenantName(Mockito.anyString())).thenReturn(authenticate);
        Mockito.when(catalogSpy.getKeystone(Mockito.anyString(), Mockito.any())).thenReturn(keystone);
        Access.Service service = new Access.Service();
        Endpoint endpoint = new Endpoint();
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(endpoint);
        Whitebox.setInternalState(service, "endpoints", endpointList);
        List<Service> serviceList = new ArrayList<>();
        serviceList.add(service);
        Mockito.when(access.getServiceCatalog()).thenReturn(serviceList);
        catalogSpy.init();
        Mockito.verify(access).getServiceCatalog();
    }
}
