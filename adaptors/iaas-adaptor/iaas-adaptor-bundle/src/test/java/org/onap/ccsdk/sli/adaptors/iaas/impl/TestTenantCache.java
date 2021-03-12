/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2016-2018 Ericsson. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2019 IBM.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.Context;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
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
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.core.utils.pool.Pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * This class is used to test methods and functions of the Tenant cache
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class TestTenantCache {

    private TenantCache tenantCache;

    private VMURL url;

    @Mock
    private ServiceCatalog catalog;

    @Mock
    private Context context;

    @Mock
    Pool<Context> pool;

    private ProviderCache provider;

    private static String TENANT_NAME;
    private static String TENANT_ID;
    private static String IDENTITY_URL;
    private static String REGION_NAME;
    private static String CREDENTIAL;
    private static String DOMAIN;

    protected Set<String> regions = new HashSet<>(Arrays.asList("RegionOne"));

    @BeforeClass
    public static void before() {
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        IDENTITY_URL = props.getProperty("provider1.identity");
        TENANT_NAME = props.getProperty("provider1.tenant1.name", "appc");
        TENANT_ID = props.getProperty("provider1.tenant1.id",
                props.getProperty("test.tenantid", "abcde12345fghijk6789lmnopq123rst"));
        DOMAIN = props.getProperty("provider1.tenant1.domain", "Default");
        CREDENTIAL = props.getProperty("provider1.tenant1.password", "appc");
        REGION_NAME = props.getProperty("provider1.tenant1.region", "RegionOne");
    }

    @Before
    public void setup() {
        Configuration props = ConfigurationFactory.getConfiguration();
        props.setProperty(Constants.PROPERTY_RETRY_LIMIT, "3");
        provider = new ProviderCache();
        provider.setIdentityURL(IDENTITY_URL);
        tenantCache = new TenantCache(provider);
        tenantCache.setDomain(DOMAIN);
        tenantCache.setPassword(CREDENTIAL);
        tenantCache.setTenantId(TENANT_ID);
        tenantCache.setTenantName(TENANT_NAME);
        tenantCache.setUserid(CREDENTIAL);
        props.setProperty(Constants.PROPERTY_RETRY_DELAY, "1");
        Map<String, Object> privateFields = ImmutableMap.<String, Object>builder().put("catalog", catalog).build();
        CommonUtility.injectMockObjects(privateFields, tenantCache);
    }

    @Test
    public void testDetermineRegion() {
        when(catalog.getVMRegion(url)).thenReturn(REGION_NAME);
        assertEquals(REGION_NAME, tenantCache.determineRegion(url));
    }

    @Test
    public void testDestroy() {
        TenantCache spy = Mockito.spy(tenantCache);
        spy.destroy(context, pool);
        assertNotNull(spy);
    }

    @Test
    public void testDestroyWithException() throws IOException {
        doThrow(new IOException("I/O Exception occured while closing context")).when(context).close();
        TenantCache spy = Mockito.spy(tenantCache);
        spy.destroy(context, pool);
        assertNotNull(spy);
    }

    @Test
    public void testInitialize() {
        TenantCache spyTenant = Mockito.spy(tenantCache);
        when(catalog.getRegions()).thenReturn(regions);
        when(catalog.getProjectId()).thenReturn(TENANT_ID);
        when(catalog.getProjectName()).thenReturn(TENANT_NAME);
        spyTenant.initialize();
        assertNotNull(spyTenant);
    }

    @Test
    public void testInitializeWithOverLimit() {
        Configuration props = ConfigurationFactory.getConfiguration();
        props.setProperty(Constants.PROPERTY_RETRY_LIMIT, "1");
        TenantCache spyTenant = Mockito.spy(tenantCache);
        when(spyTenant.getServiceCatalogFactory(anyString(), anyString(), anyObject())).thenReturn(catalog);
        when(spyTenant.getTenantName()).thenReturn(TENANT_NAME);
        when(catalog.getRegions()).thenReturn(regions);
        spyTenant.initialize();
        assertNotNull(props);
    }

    @Test
    public void testInitializeWithContextConnectionException() throws ZoneException {
        Configuration props = ConfigurationFactory.getConfiguration();
        props.setProperty(Constants.PROPERTY_RETRY_LIMIT, "2");
        props.setProperty(Constants.PROPERTY_RETRY_DELAY, "1");
        doThrow(new ContextConnectionException("Contex Connection Exception")).when(catalog).init();
        TenantCache spyTenant = Mockito.spy(tenantCache);
        spyTenant.initialize();
        assertNotNull(props);
    }

    @Test
    public void testInitializeWithZoneException() throws ZoneException {
        Configuration props = ConfigurationFactory.getConfiguration();
        props.setProperty(Constants.PROPERTY_RETRY_LIMIT, "2");
        props.setProperty(Constants.PROPERTY_RETRY_DELAY, "1");
        doThrow(new ZoneException("Zone Exception")).when(catalog).init();
        TenantCache spyTenant = Mockito.spy(tenantCache);
        when(spyTenant.getServiceCatalogFactory(anyString(), anyString(), anyObject())).thenReturn(catalog);
        spyTenant.initialize();
        assertNotNull(props);
    }

    /**
     * Ensure that we set up the Domain property correctly
     */
    @Test
    public void testDomainProperty() {
        assertEquals(DOMAIN, tenantCache.getDomain());
    }

    /**
     * Ensure that we set up the Provider property correctly
     */
    @Test
    public void testProviderProperty() {
        assertEquals(provider, tenantCache.getProvider());
    }

    /**
     * Ensure that we set up the Password property correctly
     */
    @Test
    public void testPasswordProperty() {
        assertEquals(CREDENTIAL, tenantCache.getPassword());
    }

    /**
     * Ensure that we set up the Tenant Id property correctly
     */
    @Test
    public void testTenantIdProperty() {
        assertEquals(TENANT_ID, tenantCache.getTenantId());
    }

    /**
     * Ensure that we set up the Tenant Name property correctly
     */
    @Test
    public void testTenantNameProperty() {
        assertEquals(TENANT_NAME, tenantCache.getTenantName());
    }

    /**
     * Ensure that we set up the User Id property correctly
     */
    @Test
    public void testUserIdProperty() {
        assertEquals(CREDENTIAL, tenantCache.getUserid());
    }

    /**
     * Ensure that we set up the Pools property correctly
     */
    @Test
    public void testPoolsProperty() {
        assertNotNull(tenantCache.getPools());
    }

    @Test
    public void testProvider() {
        tenantCache.setProvider(provider);
        assertEquals(provider, tenantCache.getProvider());
    }

    @Test
    public void testServiceCatalog() {
        assertTrue(tenantCache.getServiceCatalog() instanceof ServiceCatalog);
    }
}
