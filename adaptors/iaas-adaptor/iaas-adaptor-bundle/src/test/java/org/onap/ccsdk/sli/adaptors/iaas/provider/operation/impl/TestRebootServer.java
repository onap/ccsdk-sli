/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import com.att.cdp.zones.model.Tenant;
import java.util.Properties;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.junit.Assert.assertEquals;

public class TestRebootServer {

    @Test
    public void should_returnNullAsServer() throws ZoneException,SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        RebootServer rbs = new RebootServer();
        mg.getParams().put(ProviderAdapter.PROPERTY_INSTANCE_URL, "url1");
        mg.getParams().put(ProviderAdapter.REBOOT_TYPE, "");
        mg.getParams().put("REBOOT_STATUS", "SUCCESS");
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        assertEquals("SUCCESS", mg.getParams().get("REBOOT_STATUS"));
    }

    @Test
    public void rebootServerTest() throws ZoneException,SvcLogicException, RequestFailedException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        SubclassRebootServer rbs = Mockito.spy(new SubclassRebootServer());
        mg.getParams().put(ProviderAdapter.PROPERTY_INSTANCE_URL, getURL());
        mg.getParams().put(ProviderAdapter.REBOOT_TYPE, "HARD");
        mg.getParams().put("REBOOT_STATUS", "SUCCESS");
        Tenant tenant = new Tenant();
        tenant.setName("TENANT_NAME");
        mg.getContext().setTenant(tenant);
        Mockito.doReturn(mg.getContext()).when(rbs).getContext(Mockito.any(RequestContext.class), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(mg.getServer()).when(rbs).lookupServer(Mockito.any(RequestContext.class), Mockito.any(Context.class),
                Mockito.anyString());
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        assertEquals("SUCCESS", mg.getParams().get("REBOOT_STATUS"));
    }

    private String getURL() {
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        return String.format("http://%s:%s/v2/%s/servers/%s", props.getProperty("test.ip"), props.getProperty("test.port"),
                "3b3d77e0-a79d-4c10-bfac-1b3914af1a14", "3b3d77e0-a79d-4c10-bfac-1b3914af1a14");
    }

    class SubclassRebootServer extends RebootServer {
        @Override
        public Context getContext(RequestContext rc, String selfLinkURL, String providerName) {
            return super.getContext(rc, selfLinkURL, providerName);
        }
        @Override
        public Server lookupServer(RequestContext rc, Context context, String id) throws ZoneException, RequestFailedException {
            return super.lookupServer(rc, context, id);
        }
    }
}
