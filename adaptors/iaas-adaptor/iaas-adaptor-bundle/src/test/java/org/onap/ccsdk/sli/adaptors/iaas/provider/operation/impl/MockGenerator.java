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
package org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl;

import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.openstack.OpenStackContext;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.ImageService;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Hypervisor;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.ProviderCache;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.TenantCache;
import org.onap.ccsdk.sli.adaptors.iaas.impl.VMURL;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.utils.pool.Pool;
import org.onap.ccsdk.sli.core.utils.pool.PoolDrainedException;
import org.onap.ccsdk.sli.core.utils.pool.PoolExtensionException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class MockGenerator {

    private Map<String, ProviderCache> providerCacheMap;
    private Map<String, String> params;
    private SvcLogicContext ctx;
    private Server server;
    private ImageService imageService;
    private OpenStackContext context;
    private ComputeService computeService;

    public static final String SERVER_ID = "12442";
    public static final String SERVER_NAME = "Server1";
    private final Configuration configuration = ConfigurationFactory.getConfiguration();

    /**
     * This method created a mocked up object representing the OpenStack objects which would be
     * gathered from remote systems during runtime, but which are not available during a unit test.
     *
     * @param serverStatus Most of the classes in the package we are testing have different actions
     *        depending on the status of the server. This allows a different set of mock data to be
     *        created depending on which status is being tested.
     */
    public MockGenerator(Status serverStatus) {
        configuration.setProperty(Constants.PROPERTY_STACK_STATE_CHANGE_TIMEOUT, "2");
        configuration.setProperty(Constants.PROPERTY_RETRY_LIMIT, "10");
        ctx = mock(SvcLogicContext.class);
        RequestContext requestContext = mock(RequestContext.class);
        server = mock(Server.class);
        doReturn(SERVER_NAME).when(server).getName();
        doReturn(SERVER_ID).when(server).getId();
        Status status = serverStatus;
        doReturn(status).when(server).getStatus();
        // the example base image that our fake server was built off of
        doReturn("linuxBase").when(server).getImage();
        Hypervisor hypervisor = mock(Hypervisor.class);
        com.att.cdp.zones.model.Hypervisor.Status hypervisorStatus =
                com.att.cdp.zones.model.Hypervisor.Status.ENABLED;
        doReturn(hypervisorStatus).when(hypervisor).getStatus();
        com.att.cdp.zones.model.Hypervisor.State hypervisorState =
                com.att.cdp.zones.model.Hypervisor.State.UP;
        doReturn(hypervisorState).when(hypervisor).getState();
        doReturn(hypervisor).when(server).getHypervisor();
        context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        imageService = mock(ImageService.class);
        computeService = mock(ComputeService.class);
        try {
            doReturn(server).when(computeService).getServer("abc12345-1234-5678-890a-abcdefb12345");
        } catch (ZoneException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        doReturn(context).when(server).getContext();
        doReturn(provider).when(context).getProvider();
        doReturn(imageService).when(context).getImageService();
        doReturn(computeService).when(context).getComputeService();
        doReturn(false).when(requestContext).attempt();
        doReturn(true).when(requestContext).isFailed();
        params = new HashMap<String, String>();
        params.put(ProviderAdapter.PROPERTY_INSTANCE_URL,
                "http://10.1.1.2:5000/v2/abc12345-1234-5678-890a-abcdefb12345/servers/abc12345-1234-5678-890a-abcdefb12345");
        params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, "provider1");
        params.put(ProviderAdapter.PROPERTY_IDENTITY_URL,
                "http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3");
        ProviderCache providerCache = mock(ProviderCache.class);
        TenantCache tenantCache = mock(TenantCache.class);
        doReturn("cloudowner_region").when(tenantCache).determineRegion(any(VMURL.class));
        doReturn("abc12345-1234-5678-890a-abcdefb12345").when(tenantCache).getTenantId();
        doReturn("abc12345-1234-5678-890a-abcdefb12345").when(tenantCache).getTenantName();
        Pool pool = mock(Pool.class);
        try {
            doReturn(context).when(pool).reserve();
        } catch (PoolExtensionException | PoolDrainedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Map<String, Pool> tenantCachePools = new HashMap<String, Pool>();
        tenantCachePools.put("cloudowner_region", pool);
        doReturn(tenantCachePools).when(tenantCache).getPools();
        doReturn(tenantCache).when(providerCache).getTenant("abc12345-1234-5678-890a-abcdefb12345");
        providerCacheMap = new HashMap<String, ProviderCache>();
        providerCacheMap.put(
                "http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3",
                providerCache);
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Map<String, ProviderCache> getProviderCacheMap() {
        return providerCacheMap;
    }

    public SvcLogicContext getSvcLogicContext() {
        return ctx;
    }

    public Server getServer() {
        return server;
    }

    public ImageService getImageService() {
        return imageService;
    }

    public OpenStackContext getContext() {
        return context;
    }

    public ComputeService getComputeService() {
        return computeService;
    }

}
