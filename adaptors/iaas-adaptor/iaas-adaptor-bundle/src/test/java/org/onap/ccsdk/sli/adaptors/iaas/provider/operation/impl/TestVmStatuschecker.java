/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 IBM.
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

import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class TestVmStatuschecker {

    @Test
    public void vmStatuscheckerSuspended() {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = null;
        try {
            mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during VmStatuschecker.executeProviderOperation");
        }
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM, "suspended");
    }

    @Test
    public void vmStatuscheckerRunning() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = null;
        try {
            mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during VmStatuschecker.executeProviderOperation");
        }
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM, "running");
    }

    @Test
    public void vmStatuscheckerError() {
        MockGenerator mg = new MockGenerator(Status.ERROR);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = null;
        try {
            mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during VmStatuschecker.executeProviderOperation");
        }
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM, "error");
    }

    @Test
    public void vmDeletedStatuscheckerError() throws SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.DELETED);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM, "deleted");
    }

    @Test
    public void vmReadyStatuscheckerError() throws SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.READY);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM, "ready");
    }

    @Test
    public void vmPausedStatuscheckerError() throws SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.PAUSED);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM, "paused");
    }

    @Test
    public void vmPendingStatuscheckerError() throws SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.PENDING);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM, "pending");
    }
}