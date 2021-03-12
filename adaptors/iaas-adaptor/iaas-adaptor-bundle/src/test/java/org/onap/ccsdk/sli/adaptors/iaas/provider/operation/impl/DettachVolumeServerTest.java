/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl;

import com.att.cdp.exceptions.TimeoutException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.VolumeService;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import com.att.cdp.zones.model.Volume;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;



public class DettachVolumeServerTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void detachVolumeTest() throws ZoneException, SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        VolumeService volumeService = mock(VolumeService.class);
        List<Volume> volumeList = new ArrayList<>();
        doReturn(volumeList).when(volumeService).getVolumes();
        doReturn(mg.getContext()).when(server).getContext();
        doReturn(volumeService).when(mg.getContext()).getVolumeService();
        DettachVolumeServer rbs = new DettachVolumeServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        assertTrue(rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext()) instanceof ModelObject);
    }

    @Test
    public void validateDetachTest() throws RequestFailedException, ZoneException {
        DettachVolumeServer rbs = Mockito.spy(new DettachVolumeServer());
        RequestContext rc = Mockito.mock(RequestContext.class);
        ComputeService ser = Mockito.mock(ComputeService.class);
        Mockito.doReturn(true).doReturn(false).when(rc).attempt();
        Map<String, String> attachments = new HashMap<>();
        attachments.put("VOLUME_ID", "VOLUME_ID");
        Mockito.doReturn(attachments).when(ser).getAttachments(Mockito.anyString());
        assertTrue(rbs.validateDetach(rc, ser, "VM", "VOLUME_ID"));
    }

    @Test
    public void validateDetachTestTimeoutException() throws RequestFailedException, ZoneException {
        DettachVolumeServer rbs = Mockito.spy(new DettachVolumeServer());
        RequestContext rc = Mockito.mock(RequestContext.class);
        ComputeService ser = Mockito.mock(ComputeService.class);
        Mockito.doReturn(true).doReturn(false).when(rc).attempt();
        Mockito.doReturn(30).when(rc).getAttempts();
        expectedEx.expect(TimeoutException.class);
        rbs.validateDetach(rc, ser, "VM", "VOLUME_ID");
    }

    @Test
    public void validateDetachTest3Arg() throws RequestFailedException, ZoneException {
        DettachVolumeServer rbs = Mockito.spy(new DettachVolumeServer());
        ComputeService ser = Mockito.mock(ComputeService.class);
        Map<String, String> attachments = new HashMap<>();
        attachments.put("VOLUME_ID", "VOLUME_ID");
        Mockito.doReturn(attachments).when(ser).getAttachments(Mockito.anyString());
        assertTrue(rbs.validateDetach(ser, "VM", "VOLUME_ID"));
    }
}
