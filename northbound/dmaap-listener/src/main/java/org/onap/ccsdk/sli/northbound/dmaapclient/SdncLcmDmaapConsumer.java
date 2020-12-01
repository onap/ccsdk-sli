/*-
 * ============LICENSE_START=======================================================
 * ONAP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * Modifications Copyright © 2018 IBM.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdncLcmDmaapConsumer extends SdncDmaapConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(SdncLcmDmaapConsumer.class);

    private static final String BODY = "body";
    private static final String RPC = "rpc-name";
    
    @Override
    public void processMsg(String msg) throws InvalidMessageException {

        if (msg == null) {
            throw new InvalidMessageException("Null LCM message");
        }

        ObjectMapper oMapper = new ObjectMapper();
        JsonNode lcmRootNode;
        try {
            lcmRootNode = oMapper.readTree(msg);
        } catch (Exception e) {
            throw new InvalidMessageException("Cannot parse LCM json input", e);
        }        

        JsonNode bodyNode = lcmRootNode.get(BODY);
        if(bodyNode == null) {
            LOG.warn("Missing body in LCM message");
            return;
        }
        String rpcMsgbody;
        try {
            	ObjectMapper mapper = new ObjectMapper();
                rpcMsgbody = mapper.writeValueAsString(bodyNode);
                
        } catch (Exception e) {
            LOG.error("Unable to parse body in LCM message", e);
            return;
        }
      
        JsonNode rpcNode = lcmRootNode.get(RPC);
        if(rpcNode == null) {
            LOG.warn("Missing node in LCM message- " + RPC);
            return;
        }
        String rpc = rpcNode.textValue();
        String sdncEndpoint = "LCM:" + rpc;
              
        try {
            String odlUrlBase = getProperty("sdnc.odl.url-base");
            String odlUser = getProperty("sdnc.odl.user");
            String odlPassword = getProperty("sdnc.odl.password");
            LOG.info("POST LCM Request " + rpcMsgbody);
            if ((odlUrlBase != null) && (odlUrlBase.length() > 0)) {
                SdncOdlConnection conn = SdncOdlConnection.newInstance(odlUrlBase + "/" + sdncEndpoint, odlUser, odlPassword);

                conn.send("POST", "application/json", rpcMsgbody);
            } else {
                LOG.warn("Unable to POST LCM message. SDNC URL not available. body:\n" + rpcMsgbody);
            }
        } catch (Exception e) {
            LOG.error("Unable to process message", e);
        }
    }
}

