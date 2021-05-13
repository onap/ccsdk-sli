/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestCreateSnapshot {

    @Test
    public void createSnapshotRunning() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        Image image = mock(Image.class);
        doReturn("1234567").when(image).getId();
        doReturn(mg.getContext()).when(image).getContext();
        doReturn("wrong image name").when(image).getName();
        doReturn(com.att.cdp.zones.model.Image.Status.ACTIVE).when(image).getStatus();
        doReturn(image).when(mg.getImageService()).getImageByName(any());
        CreateSnapshot rbs = new CreateSnapshot();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (SvcLogicException e) {
            Assert.fail("Exception during CreateSnapshot.executeProviderOperation");
        }
        ArgumentCaptor<String> createSnapshotCaptor = ArgumentCaptor.forClass(String.class);
        verify(server).createSnapshot(createSnapshotCaptor.capture());
        ArgumentCaptor<String> getImageNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(mg.getImageService(), atLeastOnce()).getImageByName(getImageNameCaptor.capture());
        assertEquals("in:\"" + createSnapshotCaptor.getValue() + "\"", getImageNameCaptor.getValue());
    }
}