/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2016-2018 Ericsson. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import com.att.cdp.zones.Context;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.core.utils.pool.Pool;

import static org.junit.Assert.assertNotNull;

/**
 * This class is used to test methods and functions of the provider cache
 */
@RunWith(MockitoJUnitRunner.class)
public class TestProviderCache {

    private ProviderCache providerCache;

    @Mock
    private TenantCache tenantCache;

    @Mock
    private ServiceCatalog catalog;

    @Mock
    private Context context;

    @Mock
    Pool<Context> pool;

    @SuppressWarnings("nls")
    private static final String PROVIDER_NAME = "ILAB";

    @SuppressWarnings("nls")
    private static final String PROVIDER_TYPE = "OpenStackProvider";

    private static String TENANT_ID;

    protected Set<String> regions = new HashSet<>(Arrays.asList("RegionOne"));

    private Map<String, TenantCache> tenants = new HashMap<String, TenantCache>();

    @BeforeClass
    public static void before() {
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        TENANT_ID = props.getProperty("provider1.tenant1.id",
                props.getProperty("test.tenantid", "abcde12345fghijk6789lmnopq123rst"));
    }

    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test
     * to change the internal state accordingly.
     *
     */
    @Before
    public void setup() {
        Configuration props = ConfigurationFactory.getConfiguration();
        props.setProperty(Constants.PROPERTY_RETRY_LIMIT, "10");
        providerCache = new ProviderCache();
        providerCache.setIdentityURL("http://192.168.1.1:5000/v2.0/");
        providerCache.setProviderName(PROVIDER_NAME);
        providerCache.setProviderType(PROVIDER_TYPE);
        tenantCache = new TenantCache(providerCache);
        tenants.put(TENANT_ID, tenantCache);
        Map<String, Object> privateFields = ImmutableMap.<String, Object>builder().put("tenants", tenants).build();
        CommonUtility.injectMockObjects(privateFields, providerCache);
    }

    /**
     * Ensure that we set up the Tenants property correctly
     */
    @Test
    public void testTenantsProperty() {
        assertNotNull(providerCache.getTenants());
    }

    /**
     * Ensure that we set up the Tenant Id property correctly
     */
    @Test
    public void testTenantIdProperty() {
        assertNotNull(providerCache.getTenant(TENANT_ID));
    }

}
