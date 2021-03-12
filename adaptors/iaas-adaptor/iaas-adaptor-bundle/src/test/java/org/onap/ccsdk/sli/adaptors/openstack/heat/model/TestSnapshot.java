/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright 2019 IBM.
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.ccsdk.sli.adaptors.openstack.heat.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class TestSnapshot {
    private Snapshot snapshot;

    @Before
    public void setUp() {
        snapshot = new Snapshot();
    }

    @Test
    public void testGetBackupId() {
        snapshot.setId("222");
        assertNotNull(snapshot.getId());
        assertEquals(snapshot.getId(), "222");
    }

    @Test
    public void testGetName() {
        snapshot.setName("ABC");
        assertNotNull(snapshot.getName());
        assertEquals(snapshot.getName(), "ABC");
    }

    @Test
    public void testGetStatus() {
        snapshot.setStatus("status");
        assertNotNull(snapshot.getStatus());
        assertEquals(snapshot.getStatus(), "status");
    }

    @Test
    public void testGetStatusReason() {
        snapshot.setStatusReason("statusReason");
        assertNotNull(snapshot.getStatusReason());
        assertEquals(snapshot.getStatusReason(), "statusReason");
    }

    @Test
    public void testGetCreationTime() {
        snapshot.setCreationTime("01-March-2018");
        assertEquals("01-March-2018",snapshot.getCreationTime());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals("",snapshot.toString());
        assertNotEquals(null,snapshot.toString());
    }

    @Test
    public void testData() {
        Data data = new Data();
        snapshot.setData(data);
        assertSame(data, snapshot.getData());
    }
}
