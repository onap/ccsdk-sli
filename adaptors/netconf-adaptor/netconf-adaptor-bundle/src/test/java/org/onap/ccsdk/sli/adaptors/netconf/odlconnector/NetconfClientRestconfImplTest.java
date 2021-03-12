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

package org.onap.ccsdk.sli.adaptors.netconf.odlconnector;

import java.util.Properties;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.adaptors.netconf.HttpClient;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfAdaptorConstants;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfConnectionDetails;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClient.class)
public class NetconfClientRestconfImplTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        PowerMockito.mockStatic(HttpClient.class);
    }
    @Test
    public void testConfigureNullDetails() throws SvcLogicException {
        NetconfClientRestconfImpl client = new NetconfClientRestconfImpl();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Invalid connection details - null value");
        client.configure(null);
    }

    @Test
    public void testConfigureNullProperties() throws SvcLogicException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Invalid properties!");
        Whitebox.setInternalState(client, "connectionDetails", Mockito.mock(NetconfConnectionDetails.class));
        client.configure(null);
    }

    @Test
    public void testConfigureWithError() throws SvcLogicException {
        PowerMockito.when(HttpClient.putMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                NetconfAdaptorConstants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error configuring node :NODE_NAME, of Module :MODULE_NAME, in device :null");
        client.configure(null);
    }

    @Test
    public void testConfigure4ArgWithError() throws SvcLogicException {
        PowerMockito.when(HttpClient.putMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                NetconfAdaptorConstants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error configuring node :NODE_NAME, of Module :MODULE_NAME, in device :null");
        client.configure(null, null, "MODULE_NAME", "NODE_NAME");
    }

    @Test
    public void testConnect() throws SvcLogicException {
        PowerMockito.when(HttpClient.putMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                NetconfAdaptorConstants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error connecting device :null");
        client.connect(details);
    }

    @Test
    public void testConnectWithNullDetails() throws SvcLogicException {
        PowerMockito.when(HttpClient.putMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                NetconfAdaptorConstants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Invalid connection details - null value");
        client.connect(null);
    }

    @Test
    public void testDisconnectNullDetails() throws SvcLogicException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Invalid connection details - null value");
        client.disconnect();
    }

    @Test
    public void testDisconnect() throws SvcLogicException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Disconnection of device null failed!");
        client.disconnect();
    }

    @Test
    public void testGetConfigurationNullDetails() throws SvcLogicException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Invalid connection details - null value");
        client.getConfiguration();
    }

    @Test
    public void testGetConfigurationNullProperties() throws SvcLogicException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Invalid properties!");
        Whitebox.setInternalState(client, "connectionDetails", Mockito.mock(NetconfConnectionDetails.class));
        client.getConfiguration();
    }

    @Test
    public void testGetConfigurationWithError() throws SvcLogicException {
        PowerMockito.when(HttpClient.putMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                NetconfAdaptorConstants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error getting configuration of node :NODE_NAME, of Module :MODULE_NAME, in device :null");
        client.getConfiguration();
    }

    @Test
    public void testGetConfigurationSuccess() throws SvcLogicException {
        PowerMockito.when(HttpClient.getMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                NetconfAdaptorConstants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", "application/json"))
                .thenReturn("TEST");
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        assertEquals("TEST", client.getConfiguration());
    }

    @Test
    public void testCheckConnection() throws SvcLogicException {
        NetconfClientRestconfImpl client = new NetconfClientRestconfImpl();
        assertFalse(client.checkConnection(null));

    }
}
