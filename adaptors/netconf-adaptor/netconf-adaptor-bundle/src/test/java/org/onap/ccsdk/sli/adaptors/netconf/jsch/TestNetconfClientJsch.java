/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Samsung
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
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

package org.onap.ccsdk.sli.adaptors.netconf.jsch;

import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfConnectionDetails;
import org.onap.ccsdk.sli.adaptors.netconf.internal.NetconfAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.powermock.reflect.Whitebox;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertEquals;

public class TestNetconfClientJsch {

    NetconfClientJsch netconfClientJsch;
    private ChannelSubsystem mockChannel;
    private NetconfAdaptor mockNetconfAdaptor;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void SetUp() {
        netconfClientJsch = Mockito.spy(new NetconfClientJsch());
    }

    private void setupForConnectTests() throws JSchException, IOException {
        Session mockSession = Mockito.mock(Session.class);
        JSch mockJSch = Mockito.mock(JSch.class);
        mockChannel = Mockito.mock(ChannelSubsystem.class);
        InputStream mockInputStream = Mockito.mock(InputStream.class);
        OutputStream mockOutputStream = Mockito.mock(OutputStream.class);
        mockNetconfAdaptor = Mockito.mock(NetconfAdaptor.class);
        Mockito.doReturn(mockJSch).when(netconfClientJsch).getJSch();
        Mockito.doReturn(mockSession).when(mockJSch).getSession(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("subsystem");
        Mockito.doReturn(mockInputStream).when(mockChannel).getInputStream();
        Mockito.doReturn(mockOutputStream).when(mockChannel).getOutputStream();
        Mockito.doReturn(mockNetconfAdaptor).when(netconfClientJsch)
                .getNetconfAdaptor(Mockito.any(InputStream.class), Mockito.any(OutputStream.class));
    }

    @Test
    public void testConnect() throws SvcLogicException, IOException, JSchException {
        setupForConnectTests();
        Mockito.doReturn("<hello>").when(mockNetconfAdaptor).receiveMessage();
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        connectionDetails.setHost("test");
        connectionDetails.setPort(8080);
        connectionDetails.setUsername("test");
        connectionDetails.setPassword("test");
        List<String> capabilities = Collections.singletonList(
                "<capability>urn:ietf:params:netconf:base:1.1</capability>\r\n");
        connectionDetails.setCapabilities(capabilities);
        Properties additionalProperties = new Properties();
        additionalProperties.setProperty("testKey1", "testParam1");
        connectionDetails.setAdditionalProperties(additionalProperties);
        netconfClientJsch.connect(connectionDetails);
        Mockito.verify(mockNetconfAdaptor).sendMessage(
                Mockito.contains("<capability>urn:ietf:params:netconf:base:1.1</capability>"));
    }

    @Test
    public void testConnectNullMessage() throws JSchException, IOException, SvcLogicException {
        setupForConnectTests();
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        expectedEx.expect(SvcLogicException.class);
        //expectedEx.expectMessage("Cannot establish connection to server");
        netconfClientJsch.connect(connectionDetails);
    }

    @Test
    public void testConnectNullMessageNonNullResponse()
            throws JSchException, IOException, SvcLogicException {
        setupForConnectTests();
        Mockito.doReturn("NOT NULL RESPONSE").when(mockNetconfAdaptor).receiveMessage();
        Mockito.doThrow(new JSchException()).when(mockChannel).connect(10000);
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectCause(allOf(isA(RuntimeException.class),
                hasProperty("message", is("Error closing netconf device"))));
        netconfClientJsch.connect(connectionDetails);
    }

    @Test
    public void testConnectErrorMessage() throws JSchException, IOException, SvcLogicException {
        setupForConnectTests();
        Mockito.doReturn("<rpc-error>").when(mockNetconfAdaptor).receiveMessage();
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        expectedEx.expect(SvcLogicException.class);
        expectedEx
                .expectCause(allOf(isA(RuntimeException.class),
                        hasProperty("cause", allOf(isA(IOException.class),
                                hasProperty("message",
                                        containsString("Error response from netconf device:")),
                                hasProperty("message", containsString("<rpc-error>"))
                        ))));
        netconfClientJsch.connect(connectionDetails);
    }

    @Test
    public void testConnectWithSuccessfulDisconnect()
            throws JSchException, IOException, SvcLogicException {
        setupForConnectTests();
        Mockito.doThrow(new JSchException()).when(mockChannel).connect(10000);
        Mockito.doReturn("<ok/>").when(mockNetconfAdaptor).receiveMessage();
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectCause(allOf(isA(SvcLogicException.class),
                hasProperty("message", is(JSchException.class.getName()))));
        netconfClientJsch.connect(connectionDetails);
    }

    @Test
    public void testGetConfiguration() throws IOException, SvcLogicException {
        mockNetconfAdaptor = Mockito.mock(NetconfAdaptor.class);
        Whitebox.setInternalState(netconfClientJsch, "netconfAdaptor", mockNetconfAdaptor);
        Mockito.doReturn("TEST RETURN VALUE").when(mockNetconfAdaptor).receiveMessage();
        assertEquals("TEST RETURN VALUE", netconfClientJsch.getConfiguration());
    }

    @Test
    public void testGetConfigurationExceptionFlow() throws IOException, SvcLogicException {
        mockNetconfAdaptor = Mockito.mock(NetconfAdaptor.class);
        Whitebox.setInternalState(netconfClientJsch, "netconfAdaptor", mockNetconfAdaptor);
        Mockito.doThrow(new IOException()).when(mockNetconfAdaptor).receiveMessage();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(IOException.class.getName());
        netconfClientJsch.getConfiguration();
    }

    @Test
    public void testConfigure() throws IOException, SvcLogicException {
        mockNetconfAdaptor = Mockito.mock(NetconfAdaptor.class);
        Whitebox.setInternalState(netconfClientJsch, "netconfAdaptor", mockNetconfAdaptor);
        Mockito.doReturn("<ok/>").when(mockNetconfAdaptor).receiveMessage();
        netconfClientJsch.configure(null);
        Mockito.verify(netconfClientJsch).exchangeMessage(null);
    }

    @Test
    public void testConfigureExceptionFlow() throws IOException, SvcLogicException {
        mockNetconfAdaptor = Mockito.mock(NetconfAdaptor.class);
        Whitebox.setInternalState(netconfClientJsch, "netconfAdaptor", mockNetconfAdaptor);
        Mockito.doThrow(new IOException()).when(mockNetconfAdaptor).receiveMessage();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(IOException.class.getName());
        netconfClientJsch.configure(null);
    }
}
