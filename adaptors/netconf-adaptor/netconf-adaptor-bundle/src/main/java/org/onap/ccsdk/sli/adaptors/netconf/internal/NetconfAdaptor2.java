/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.ccsdk.sli.adaptors.netconf.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides basic methods for exchanging netconf messages.
 */
public class NetconfAdaptor2 {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfAdaptor2.class);

    // device input pipe
    private final PipedOutputStream pipedOutIn = new PipedOutputStream();
    private final PipedInputStream in;
    // device output pipe
    private final PipedInputStream pipedInOut = new PipedInputStream();
    private final PipedOutputStream out;

    /**
     * Constructor.
     *
     * @throws IOException the io exception
     */
    public NetconfAdaptor2() throws IOException {
        in = new PipedInputStream(pipedOutIn);
        out = new PipedOutputStream(pipedInOut);
    }

    /**
     * Constructor.
     *
     * @param in  InputStream this instance will read netconf messages from
     * @param out OutputStream this instance will write netconf messages to
     *
     * @throws IOException the io exception
     */
    public NetconfAdaptor2(PipedInputStream in, PipedOutputStream out) throws IOException {
        this.in = in;
        this.out = out;

    }

    /**
     * Gets in.
     *
     * @return InputStream this instance will read netconf messages from.
     */
    public InputStream getIn() {
        return in;
    }

    /**
     * Gets out.
     *
     * @return OutputStream this instance will write netconf messages to.
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * Receives netconf message from InputStream and return it's text (without netconf frame characters).
     *
     * @return text of message received from netconf device
     *
     * @throws IOException the io exception
     */
    public String receiveMessage() throws IOException {
        NetconfMessage message = new NetconfMessage();
        byte[] buf = new byte[1024];
        int c;
        while((c = pipedInOut.read(buf)) > 0) {
                message.append(buf, 0, c);
                if (message.isCompleted()) {
                    break;
                }
        }
        String text = message.getText();
        if(LOG.isDebugEnabled()) {
            LOG.debug("Received message from netconf device:\n" + text);
        }
        return text;
    }

    /**
     * Sends netconf message with provided text (adds netconf frame characters and sends the message).
     *
     * @param text text of message to be sent to netconf device
     *
     * @throws IOException the io exception
     */
    public void sendMessage(final String text) throws IOException {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Sending message to netconf device:\n" + text);
        }
        pipedOutIn.write(new NetconfMessage(text).getFrame());
    }
}
