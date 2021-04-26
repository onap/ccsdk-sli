/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Samsung
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

package org.onap.ccsdk.sli.adaptors.netconf.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.junit.Assert;
import org.junit.Test;

public class TestNetconfAdaptor2 {

    private static final String EOM = "]]>]]>";

    @Test (expected = IOException.class)
    public void testReceiveMessage() throws IOException {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream(pos);

        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream os = new PipedOutputStream(pis);

        NetconfAdaptor2 netconfAdaptor = new NetconfAdaptor2(is, os);

        String request = "Hello, netconf!";
        pos.write(request.getBytes());
        pos.write(EOM.getBytes());
        String response = netconfAdaptor.receiveMessage();
        Assert.assertNotNull(response);
        Assert.assertEquals(request, response.trim());
    }

    @Test (expected = IOException.class)
    public void testSendMessage() throws IOException {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream(pos);

        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream os = new PipedOutputStream(pis);

        NetconfAdaptor2 netconfAdaptor = new NetconfAdaptor2(is, os);

        String request = "Hello, netconf!";
        netconfAdaptor.sendMessage(request);
        byte[] bytes = new byte[request.length()+EOM.length()+2];
        int count = pis.read(bytes);
        String response = new String(bytes, 0, count);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.endsWith(EOM));
        response = response.substring(0, response.length() - EOM.length()).trim();
        Assert.assertEquals(request, response);
    }

    @Test (expected = IOException.class)
    public void testSendReceive() throws IOException {
        PipedOutputStream os = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream(os);

        NetconfAdaptor2 netconfAdaptor = new NetconfAdaptor2(is, os);

        String request = "Hello, netconf!";
        netconfAdaptor.sendMessage(request);
        String response = netconfAdaptor.receiveMessage();
        Assert.assertNotNull(response);
        Assert.assertEquals(request, response.trim());
    }

    @Test
    public void testDefaultSendReceive() throws IOException {

        NetconfAdaptor2 netconfAdaptor = new NetconfAdaptor2();

        String request = "Hello, netconf!";
        netconfAdaptor.sendMessage(request);

        InputStream in = netconfAdaptor.getIn();
        OutputStream out = netconfAdaptor.getOut();

        Assert.assertNotNull(in);
        Assert.assertNotNull(out);
    }
}