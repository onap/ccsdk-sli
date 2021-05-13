/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;


public class TestRebuildServer {
    protected static final Configuration configuration = ConfigurationFactory.getConfiguration();

    @Test
    public void rebuildServerRunning() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).stop();
        inOrderTest.verify(server).rebuild("linuxBase");
        inOrderTest.verify(server).start();

    }

    @Test
    public void rebuildServerReady() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.READY);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).rebuild("linuxBase");
        inOrderTest.verify(server).start();
    }

    @Test
    public void rebuildServerPause() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.PAUSED);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).unpause();
        inOrderTest.verify(server).stop();
        inOrderTest.verify(server).rebuild("linuxBase");
        inOrderTest.verify(server).start();
    }

    @Test
    public void rebuildServerError() {
        MockGenerator mg = new MockGenerator(Status.ERROR);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        verify(mg.getSvcLogicContext()).setAttribute(Constants.ATTRIBUTE_ERROR_CODE,
                "405");
    }

    @Test
    public void rebuildServerSuspended() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).resume();
        inOrderTest.verify(server).stop();
        inOrderTest.verify(server).rebuild("linuxBase");
        inOrderTest.verify(server).start();
    }

    @Test
    public void rebuildServerException() throws ZoneException, SvcLogicException {
        MockGenerator mg = new MockGenerator(null);
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        SvcLogicContext context = new SvcLogicContext();
        rbs.executeProviderOperation(mg.getParams(), context);
        assertEquals("ERROR", context.getAttribute("REBUILD_STATUS"));
    }

    @Test
    public void rebuildServerDeleted() throws ZoneException, SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.DELETED);
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        SvcLogicContext context = new SvcLogicContext();
        rbs.executeProviderOperation(mg.getParams(), context);
        assertEquals("ERROR", context.getAttribute("REBUILD_STATUS"));
    }

}