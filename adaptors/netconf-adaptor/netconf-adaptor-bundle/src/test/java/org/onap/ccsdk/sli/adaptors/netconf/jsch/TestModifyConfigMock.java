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

package org.onap.ccsdk.sli.adaptors.netconf.jsch;

import java.util.Collections;
import java.util.List;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfConnectionDetails;

public class TestModifyConfigMock {

    private static final String HOST = "192.168.1.2";
    private static final String USER = "test";
    private static final String PSWD = "test123";
    private static final int PORT = 830;
    private static final List<String> CAPABILITIES = Collections.singletonList("<capability>urn:org:onap:appc:capability:1.1.0</capability>");
    private static final String CONFIG =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running />\n" +
            "    </target>\n" +
            "    <default-operation>merge</default-operation>\n" +
            "    <config xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "      <ManagedElement xmlns=\"urn:org.onap.appc:Test\">\n" +
            "        <managedElementId>1</managedElementId>\n" +
            "        <VnfFunction xmlns=\"urn:org:onap:appc:VnfFunction\">\n" +
            "          <id>1</id>\n" +
            "          <Interfaces>\n" +
            "            <id>1</id>\n" +
            "            <DiaRealmRf>\n" +
            "              <realm>example.com</realm>\n" +
            "              <reconnectTimer>60</reconnectTimer>\n" +
            "            </DiaRealmRf>\n" +
            "          </Interfaces>\n" +
            "        </VnfFunction>\n" +
            "      </ManagedElement>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>";

    public static void main(String[] args) {
        try {
            NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
            connectionDetails.setHost(HOST);
            connectionDetails.setPort(PORT);
            connectionDetails.setUsername(USER);
            connectionDetails.setPassword(PSWD);
            connectionDetails.setCapabilities(CAPABILITIES);
            NetconfClientJsch netconfClientJsch = new NetconfClientJsch();
            netconfClientJsch.connect(connectionDetails);
            try {
                System.out.println("=> Running get configuration...");
                String configuration = netconfClientJsch.getConfiguration();
                System.out.println("=> Configuration:\n" + configuration);

                System.out.println("=> Reconfiguring device...");
                String outMessage = netconfClientJsch.exchangeMessage(CONFIG);
                System.out.println("=> Reconfiguration response:\n" + outMessage);

                System.out.println("=> Running get configuration...");
                configuration = netconfClientJsch.getConfiguration();
                System.out.println("=> Configuration:\n" + configuration);
            } finally {
                netconfClientJsch.disconnect();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
