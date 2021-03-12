/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
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

package org.onap.ccsdk.sli.adaptors.netconf.jsch;

import com.att.eelf.i18n.EELFResourceManager;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfClient;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfConnectionDetails;
import org.onap.ccsdk.sli.adaptors.netconf.internal.NetconfAdaptor;
import org.onap.ccsdk.sli.adaptors.netconf.internal.NetconfConstMessages;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.encryption.EncryptionTool;
import org.onap.ccsdk.sli.core.utils.logging.Msg;

/**
 * Implementation of NetconfClient interface based on JCraft jsch library.
 */
public class NetconfClientJsch implements NetconfClient {

    private static final int SESSION_CONNECT_TIMEOUT = 30000;
    private static final int CHANNEL_CONNECT_TIMEOUT = 10000;

    private Session session;
    private Channel channel;
    private NetconfAdaptor netconfAdaptor;


    @Override
    public void connect(NetconfConnectionDetails connectionDetails) throws SvcLogicException {
        String host = connectionDetails.getHost();
        int port = connectionDetails.getPort();
        String username = connectionDetails.getUsername();
        String password = connectionDetails.getPassword();
        try {
            JSch.setLogger(new JSchLogger());
            JSch jsch = getJSch();
            session = jsch.getSession(EncryptionTool.getInstance().decrypt(username), host, port);
            session.setPassword(EncryptionTool.getInstance().decrypt(password));
            session.setConfig("StrictHostKeyChecking", "no");

            Properties additionalProps = connectionDetails.getAdditionalProperties();
            if((additionalProps != null) && !additionalProps.isEmpty()) {
                session.setConfig(additionalProps);
            }

            session.connect(SESSION_CONNECT_TIMEOUT);
            session.setTimeout(10000);

            createConnection(connectionDetails);

        } catch(Exception e) {
            String message = EELFResourceManager.format(Msg.CANNOT_ESTABLISH_CONNECTION, host, String.valueOf(port), username);
            throw new SvcLogicException(message, e);
        }
    }

    @Override
    public String exchangeMessage(String message) throws SvcLogicException {
        try {
            netconfAdaptor.sendMessage(message);
            return netconfAdaptor.receiveMessage();
        } catch(IOException e) {
            throw new SvcLogicException(e.toString());
        }
    }

    @Override
    public void configure(String configuration) throws SvcLogicException {
        try {
            isOk(exchangeMessage(configuration));
        } catch(IOException e) {
            throw new SvcLogicException(e.toString());
        }
    }

    @Override
    public String getConfiguration() throws SvcLogicException {
        return exchangeMessage(NetconfConstMessages.GET_RUNNING_CONFIG);
    }

    @Override
    public void disconnect() {
        try {
            if((channel != null) && !channel.isClosed()) {
                netconfAdaptor.sendMessage(NetconfConstMessages.CLOSE_SESSION);
                isOk(netconfAdaptor.receiveMessage());
            }
        } catch(IOException e) {
            throw new RuntimeException("Error closing netconf device", e);
        } finally {
            netconfAdaptor = null;
            if(channel != null) {
                channel.disconnect();
                channel = null;
            }
            if(session != null) {
                session.disconnect();
                session = null;
            }
        }
    }

    private void createConnection(NetconfConnectionDetails connectionDetails) throws SvcLogicException {
        try {
            channel = session.openChannel("subsystem");
            ((ChannelSubsystem)channel).setSubsystem("netconf");
            netconfAdaptor = getNetconfAdaptor(channel.getInputStream(), channel.getOutputStream());
            channel.connect(CHANNEL_CONNECT_TIMEOUT);
            hello(connectionDetails.getCapabilities());
        } catch(Exception e) {
            disconnect();
            throw new SvcLogicException(e.toString());
        }
    }

    private void hello(List<String> capabilities) throws IOException {
        String helloIn = netconfAdaptor.receiveMessage();
        if(helloIn == null) {
            throw new IOException("Expected hello message, but nothing received from netconf device");
        }
        if(helloIn.contains("<rpc-error>")) {
            throw new IOException("Expected hello message, but received error from netconf device:\n" + helloIn);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(NetconfConstMessages.CAPABILITIES_START);
        sb.append(NetconfConstMessages.CAPABILITIES_BASE);
        if(capabilities != null) {
            for(String capability: capabilities) {
                sb.append("    ").append(capability).append("\n");
            }
        }
        sb.append(NetconfConstMessages.CAPABILITIES_END);
        String helloOut = sb.toString();
        netconfAdaptor.sendMessage(helloOut);
    }

    private void isOk(String response) throws IOException {
        if(response == null) {
            throw new IOException("No response from netconf device");
        }
        if(!response.contains("<ok/>")) {
            throw new IOException("Error response from netconf device: \n" + response);
        }
    }

    protected JSch getJSch() {
        return new JSch();
    }

    protected NetconfAdaptor getNetconfAdaptor(InputStream inputStream, OutputStream outputStream) throws IOException {
        return new NetconfAdaptor(inputStream, outputStream);
    }
}
