/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright (c) 2019 IBM
* ================================================================================
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

public class TestVolume_ {
    private Volume_ volume_;
    private Metadata metadata;
    private ResourceData resourceData;

    @Before
    public void setUp() {
        volume_ = new Volume_();
        metadata = new Metadata();
        resourceData = new ResourceData();
    }

    @Test
    public void testGetStatus() {
        volume_.setStatus("Success");
        assertEquals("Success",volume_.getStatus());
    }

    @Test
    public void testGetName() {
        volume_.setName("XYZ");
        assertNotNull(volume_.getName());
        assertEquals("XYZ",volume_.getName());
    }

    @Test
    public void testGetResourceId() {
        volume_.setResourceId("333");
        assertNotNull(volume_.getResourceId());
        assertEquals("333",volume_.getResourceId());
    }

    @Test
    public void testGetAction() {
        volume_.setAction("action");
        assertNotNull(volume_.getAction());
        assertEquals("action",volume_.getAction());
    }

    @Test
    public void testGetType() {
        volume_.setType("A");
        assertEquals("A",volume_.getType());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals("",volume_.toString());
        assertNotEquals(null,volume_.toString());
    }

    @Test
    public void testGetResourceData() {
        volume_.setResourceData(resourceData);
        assertSame(resourceData,volume_.getResourceData());
    }

    @Test
    public void testGetMetadata() {
        volume_.setMetadata(metadata);
        assertSame(metadata,volume_.getMetadata());
    }
}
