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

public class TestModifyConfigRouterMock {

    private static final String HOST = "10.147.27.50"; // yuma netconf simulator
    private static final int PORT = 830;
    private static final String USER = "admin";
    private static final String PSWD = "admin";
    private static final List<String> CAPABILITIES = Collections.emptyList();
    private static final String CONFIG =
            "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <candidate/>\n" +
            "    </target>\n" +
            "    <config xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "      <router xmlns=\"urn:sdnhub:odl:tutorial:router\">\n" +
            "        <ospf>\n" +
            "          <process-id>1</process-id>\n" +
            "          <networks>\n" +
            "            <subnet-ip>100.100.100.0/24</subnet-ip>\n" +
            "            <area-id>10</area-id>\n" +
            "          </networks>\n" +
            "        </ospf>\n" +
            "        <bgp>\n" +
            "          <as-number>1000</as-number>\n" +
            "          <router-id>10.10.1.1</router-id>\n" +
            "          <neighbors>\n" +
            "            <as-number>2000</as-number>\n" +
            "            <peer-ip>10.10.1.2</peer-ip>\n" +
            "          </neighbors>\n" +
            "        </bgp>\n" +
            "      </router>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>\n";

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
            } finally {
                netconfClientJsch.disconnect();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
