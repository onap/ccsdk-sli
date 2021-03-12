/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
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

public class TestResourceData {
    private ResourceData resourceData;

    @Before
    public void setUp() {
        resourceData=new ResourceData();
    }
    @Test
    public void testGetBackupId() {
        resourceData.setBackupId("111");
        assertNotNull(resourceData.getBackupId());
        assertEquals(resourceData.getBackupId(),"111");
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(resourceData.toString(), "");
        assertNotEquals(resourceData.toString(), null);
    }
}