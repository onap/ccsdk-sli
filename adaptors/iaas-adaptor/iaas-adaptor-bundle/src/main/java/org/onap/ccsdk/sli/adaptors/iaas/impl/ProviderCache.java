/*
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

import java.util.HashMap;
import java.util.Map;

/**
 * This class maintains a cache of information by provider, where a provider is identified by both a type and an
 * identity URL used to connect to that provider.
 * <p>
 * Providers may be multi-tenant, such as OpenStack, where the available services and resources vary from one tenant to
 * another. Therefore, the provider cache maintains a cache of tenants and the service catalogs for each, as well as the
 * credentials used to access the tenants, and a pool of Context objects for each tenant. The context pool allows use of
 * the CDP abstraction layer to access the services of the provider within the specific tenant.
 * </p>
 */
public class ProviderCache {

    /**
     * The type of provider (e.g., OpenStackProvider) used to setup the CDP abstraction layer and load the appropriate
     * support
     */
    private String providerType;

    /**
     * The URL of the provider's identity service or whatever service is used to login and authenticate to the provider
     */
    private String identityURL;

    /**
     * A string used to identify the provider instance
     */
    private String providerName;

    /**
     * The map of tenant cache objects by tenant id
     */
    private Map<String /* tenant id */, TenantCache> tenants = new HashMap<>();

    /**
     * @return the value of providerType
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * This method is called to initialize the provider cache, set up the context pools for each of the tenants,
     * discover all of the regions supported on the provider, and load all of the service catalogs for each provider.
     */
    public void initialize() {
        for (Map.Entry<String, TenantCache> entry : tenants.entrySet()) {
            entry.getValue().initialize();
        }
    }

    /**
     * @param providerType the value for providerType
     */
    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    /**
     * @return the value of identityURL
     */
    public String getIdentityURL() {
        return identityURL;
    }

    /**
     * @param identityURL the value for identityURL
     */
    public void setIdentityURL(String identityURL) {
        this.identityURL = identityURL;
    }

    /**
     * @return the value of providerName
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * @param providerName the value for providerName
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * @return the value of tenants
     */
    public Map<String, TenantCache> getTenants() {
        return tenants;
    }

    /**
     * This method is a helper to return a specific TenantCache
     *
     * @param tenantId
     * @return
     */
    public TenantCache getTenant(String tenantId) {
        return tenants.get(tenantId);
    }

    // Previously there was no way to add additional tenants to the tenant cache
    /**
     * This method is used to add a tenant to the provider cache
     *
     * @param tenantId
     * @param tenantName
     * @param userId
     * @param password
     * @param domain
     * @return the new initialized TenantCache or null if unsuccessful
     */
    public TenantCache addTenant(String tenantId, String tenantName, String userId, String password, String domain) {
        if (tenantId != null || tenantName != null && userId != null && password != null) {
            TenantCache tenant = new TenantCache(this);
            if (tenantId != null) {
                tenant.setTenantId(tenantId);
            }
            if (tenantName != null) {
                tenant.setTenantName(tenantName);
            }
            tenant.setUserid(userId);
            tenant.setPassword(password);
            tenant.setDomain(domain);

            if (identityURL != null) {
                tenant.initialize();
            }

            if (tenant.isInitialized()) {
                tenants.put(tenant.getTenantId(), tenant);
                return tenant;
            }
        }
        return null;
    }
}
