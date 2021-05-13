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
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.ProviderAdapterImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class TestEvacuateServer {

    @Test
    public void evacuateServerRunning() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        EvacuateServer rbs = new EvacuateServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        mg.getParams().put(ProviderAdapter.PROPERTY_TARGETHOST_ID, "newServer1");
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during EvacuateServer.executeProviderOperation");
        }
        verify(mg.getComputeService()).moveServer(server.getId(), "newServer1");
    }

    @Test
    public void evacuateServerPaImpl() throws ZoneException, SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        EvacuateServer rbs = new EvacuateServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        mg.getParams().put(ProviderAdapter.PROPERTY_TARGETHOST_ID, "newServer1");
        List<Image> images = new ArrayList<>();
        images.add(Mockito.mock(Image.class));
        Mockito.doReturn(images).when(mg.getServer()).getSnapshots();
        ProviderAdapterImpl paImpl = Mockito.mock(ProviderAdapterImpl.class);
        Whitebox.setInternalState(rbs, "paImpl", paImpl);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("error_code", "300");
        rbs.executeProviderOperation(mg.getParams(), ctx);
        verify(mg.getComputeService()).moveServer("12442", "newServer1");
    }

    @Test
    public void evacuateServerSvcLogicException() throws ZoneException, SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        EvacuateServer rbs = new EvacuateServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        mg.getParams().put(ProviderAdapter.PROPERTY_TARGETHOST_ID, "newServer1");
        List<Image> images = new ArrayList<>();
        images.add(Mockito.mock(Image.class));
        Mockito.doReturn(images).when(mg.getServer()).getSnapshots();
        ProviderAdapterImpl paImpl = Mockito.mock(ProviderAdapterImpl.class);
        Mockito.doThrow(new SvcLogicException()).when(paImpl).rebuildServer(Mockito.anyMap(),
                Mockito.any(SvcLogicContext.class));
        Whitebox.setInternalState(rbs, "paImpl", paImpl);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("error_code", "300");
        rbs.executeProviderOperation(mg.getParams(), ctx);
        assertEquals("Internal Server Error", ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
    }

    @Test
    public void evacuateServerZoneException() throws ZoneException, SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        EvacuateServer rbs = new EvacuateServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        mg.getParams().put(ProviderAdapter.PROPERTY_TARGETHOST_ID, "newServer1");
        List<Image> images = new ArrayList<>();
        images.add(Mockito.mock(Image.class));
        Mockito.doReturn(images).when(mg.getServer()).getSnapshots();
        Mockito.doThrow(new ZoneException("TEST")).when(mg.getServer()).refreshAll();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("error_code", "300");
        rbs.executeProviderOperation(mg.getParams(), ctx);
        assertEquals("TEST", ctx.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE));
    }
}