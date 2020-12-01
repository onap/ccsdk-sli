/*-
 * ============LICENSE_START=======================================================
 * ONAP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * Modifications Copyright © 2019 IBM.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.northbound.dmaapclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMNotifyDmaapConsumer extends SdncDmaapConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(CMNotifyDmaapConsumer.class);

    private static final String BODY = "body";
    private static final String RPC = "rpc-name";
    private static final String INPUT = "input";
    private static final String PAYLOAD = "Payload";

    @Override
    public void processMsg(String msg) throws InvalidMessageException {

        if (msg == null) {
            throw new InvalidMessageException("Null CMNotify-DMAAP message");
        }

        ObjectMapper oMapper = new ObjectMapper();
        JsonNode CMNotifyRootNode;
        try {
            CMNotifyRootNode = oMapper.readTree(msg);
        } catch (Exception e) {
            throw new InvalidMessageException("Cannot parse CMNotify-DMAAP json input", e);
        }

        JsonNode bodyNode = CMNotifyRootNode.get(BODY);
        if(bodyNode == null) {
            LOG.warn("Missing body in CMNotify-DMAAP message");
            return;
        }

        JsonNode input = bodyNode.get(INPUT);
        if(input == null) {
        	 LOG.info("Missing input node.");
        	 return;
        }

        JsonNode payloadNode = input.get(PAYLOAD);
        if(payloadNode == null) {
            LOG.info("Missing payload node.");
            return;
        }

        String rpcMsgbody;
        try {
            	ObjectMapper mapper = new ObjectMapper();
                rpcMsgbody = "{\"input\":" + mapper.writeValueAsString(payloadNode) + "}";

        } catch (Exception e) {
            LOG.error("Unable to parse payload in CMNotify-DMAAP message", e);
            return;
        }

        JsonNode rpcNode = CMNotifyRootNode.get(RPC);
        if(rpcNode == null) {
            LOG.warn("Missing node in CMNotify-DMAAP message- " + RPC);
            return;
        }
        String rpc = rpcNode.textValue();
        String sdncEndpoint = "CM-NOTIFY-API:" + rpc;

        try {
            String odlUrlBase = getProperty("sdnc.odl.url-base");
            String odlUser = getProperty("sdnc.odl.user");
            String odlPassword = getProperty("sdnc.odl.password");
            LOG.info("POST CM-NOTIFY-API Request " + rpcMsgbody);
            if ((odlUrlBase != null) && (odlUrlBase.length() > 0)) {
                SdncOdlConnection conn = SdncOdlConnection.newInstance(odlUrlBase + "/" + sdncEndpoint, odlUser, odlPassword);

                conn.send("POST", "application/json", rpcMsgbody);
            } else {
                LOG.warn("Unable to POST CM-NOTIFY-API message. SDNC URL not available. body:\n" + rpcMsgbody);
            }
        } catch (Exception e) {
            LOG.error("Unable to process message", e);
        }
    }
}
