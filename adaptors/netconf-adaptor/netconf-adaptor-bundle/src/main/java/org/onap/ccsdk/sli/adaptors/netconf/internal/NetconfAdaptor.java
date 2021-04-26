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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides basic methods for exchanging netconf messages.
 */
public class NetconfAdaptor {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfAdaptor.class);
    private static final long MAX_WAITING_TIME = 1800000;
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    // device input stream
    private final InputStream in;
    // device output stream
    private final OutputStream out;
    private final long maxWaitingTime = ConfigurationFactory.getConfiguration().getLongProperty("org.onap.appc.netconf.recv.timeout", MAX_WAITING_TIME);

    /**
     * Constructor.
     *
     * @param in  InputStream this instance will read netconf messages from
     * @param out OutputStream this instance will write netconf messages to
     *
     * @throws IOException the io exception
     */
    public NetconfAdaptor(InputStream in, OutputStream out) throws IOException {
        this.in = in;
        this.out = out;
    }

    /**
     * Receives netconf message from InputStream and return it's text (without netconf frame characters).
     *
     * @return text of message received from netconf device
     *
     * @throws IOException the io exception
     */
    public String receiveMessage() throws IOException {

        final NetconfMessage message = new NetconfMessage();
        final byte[] buf = new byte[1024];

        // Read data with timeout
        Callable<Boolean> readTask = () -> {
            int c;
            while ((c = in.read(buf)) > 0) {
                message.append(buf, 0, c);
                if (message.isCompleted()) {
                    break;
                }
            }

            return c >= 0;
        };

        Future<Boolean> future = executor.submit(readTask);
        Boolean status;
        try {
            status = future.get(maxWaitingTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new IOException(e);
        }

        if (!status) {
            throw new IOException("Failed to read netconf message");
        }

        String text = message.getText();
        if (text != null) {
            text = text.trim();
        }
        if (LOG.isDebugEnabled()) {
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending message to netconf device:\n" + text);
        }
        out.write(new NetconfMessage(text).getFrame());
        out.flush();
    }

}
