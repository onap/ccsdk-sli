/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
* ================================================================================
* Modifications Copyright (C) 2019 Ericsson
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
package org.onap.ccsdk.sli.adaptors.netconf;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NetconfConnectionDetailsTest {
    private NetconfConnectionDetails netconfConnectionDetails;
    private List<String> capabilities;
    private Properties additionalProperties;

    @Before
    public void SetUp() {
        netconfConnectionDetails = new NetconfConnectionDetails();
    }

    @Test
    public void testGetHost() {
        netconfConnectionDetails.setHost("host1");
        Assert.assertNotNull(netconfConnectionDetails.getHost());
        Assert.assertEquals("host1", netconfConnectionDetails.getHost());
    }

    @Test
    public void testGetPort() {
        netconfConnectionDetails.setPort(123);
        Assert.assertNotNull(netconfConnectionDetails.getPort());
        Assert.assertEquals(123, netconfConnectionDetails.getPort());
    }

    @Test
    public void testGetUsername() {
        netconfConnectionDetails.setUsername("ABC");
        Assert.assertNotNull(netconfConnectionDetails.getUsername());
        Assert.assertEquals("ABC", netconfConnectionDetails.getUsername());
    }

    @Test
    public void testGetPassword() {
        netconfConnectionDetails.setPassword("pass1");
        Assert.assertNotNull(netconfConnectionDetails.getPassword());
        Assert.assertEquals("pass1", netconfConnectionDetails.getPassword());
    }

    @Test
    public void testNullCapabilities() {
        capabilities = new ArrayList<String>();
        Assert.assertNull(netconfConnectionDetails.getCapabilities());
    }

    @Test
    public void testCapabilitiesWithValues() {
        capabilities = new ArrayList<String>();
        capabilities.add("capabilities1");
        capabilities.add("capabilities2");
        netconfConnectionDetails.setCapabilities(capabilities);
        Assert.assertTrue(capabilities.contains("capabilities2"));
    }

    @Test
    public void testCapabilities_Size() {
        capabilities = new ArrayList<String>();
        capabilities.add("capabilities1");
        capabilities.add("capabilities2");
        netconfConnectionDetails.setCapabilities(capabilities);
        Assert.assertEquals(2, capabilities.size());
    }

    @Test
    public void testAdditionalProperties() {
        additionalProperties = new Properties();
        Assert.assertNull(netconfConnectionDetails.getAdditionalProperties());
    }

    @Test
    public void testAdditionalPropertiesWithValues() {
        additionalProperties = new Properties();
        additionalProperties.put("A", "a");
        additionalProperties.put("B", "b");
        netconfConnectionDetails.setAdditionalProperties(additionalProperties);
        Assert.assertEquals("a", netconfConnectionDetails.getAdditionalProperties().get("A"));
    }

    @Test
    public void testAdditionalProperties_Size() {
        additionalProperties = new Properties();
        additionalProperties.put("A", "a");
        additionalProperties.put("B", "b");
        additionalProperties.put("C", "c");
        netconfConnectionDetails.setAdditionalProperties(additionalProperties);
        Assert.assertNotNull(netconfConnectionDetails.getAdditionalProperties());
        Assert.assertEquals(3, additionalProperties.size());
    }
}
