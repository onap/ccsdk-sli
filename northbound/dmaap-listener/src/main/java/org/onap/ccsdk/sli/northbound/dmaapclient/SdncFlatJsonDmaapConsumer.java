/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class SdncFlatJsonDmaapConsumer extends SdncDmaapConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(SdncFlatJsonDmaapConsumer.class);

    private static final String DMAAPLISTENERROOT = "DMAAPLISTENERROOT";
    private static final String SDNC_ENDPOINT = "SDNC.endpoint";

    @Override
    public void processMsg(String msg) throws InvalidMessageException {

        processMsg(msg, null);
    }

    public void processMsg(String msg, String mapDirName) throws InvalidMessageException {

        if (msg == null) {
            throw new InvalidMessageException("Null message");
        }

        ObjectMapper oMapper = new ObjectMapper();
        JsonNode instarRootNode;
        ObjectNode sdncRootNode;

        String instarMsgName = null;

        try {
            instarRootNode = oMapper.readTree(msg);
        } catch (Exception e) {
            throw new InvalidMessageException("Cannot parse json object", e);
        }

        Iterator<Map.Entry<String, JsonNode>> instarFields = instarRootNode.fields();

        while (instarFields.hasNext()) {
            Map.Entry<String, JsonNode> entry = instarFields.next();

            instarMsgName = entry.getKey();
            instarRootNode = entry.getValue();
            break;
        }

        Map<String, String> fieldMap = loadMap(instarMsgName, mapDirName);

        if (fieldMap == null) {
            throw new InvalidMessageException("Unable to process message - cannot load field mappings");
        }

        if (!fieldMap.containsKey(SDNC_ENDPOINT)) {
            throw new InvalidMessageException("No SDNC endpoint known for message " + instarMsgName);
        }

        String sdncEndpoint = fieldMap.get(SDNC_ENDPOINT);

        sdncRootNode = oMapper.createObjectNode();
        ObjectNode inputNode = oMapper.createObjectNode();

        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {

            if (!SDNC_ENDPOINT.equals(entry.getKey())) {
                JsonNode curNode = instarRootNode.get(entry.getKey());
                if (curNode != null) {
                    String fromValue = curNode.textValue();

                    inputNode.put(entry.getValue(), fromValue);
                }
            }
        }
        sdncRootNode.put("input", inputNode);

        try {
            String rpcMsgbody = oMapper.writeValueAsString(sdncRootNode);
            String odlUrlBase = getProperty("sdnc.odl.url-base");
            String odlUser = getProperty("sdnc.odl.user");
            String odlPassword = getProperty("sdnc.odl.password");

            if ((odlUrlBase != null) && (odlUrlBase.length() > 0)) {
                SdncOdlConnection conn = SdncOdlConnection.newInstance(odlUrlBase + sdncEndpoint, odlUser, odlPassword);

                conn.send("POST", "application/json", rpcMsgbody);
            } else {
                LOG.info("POST message body would be:\n" + rpcMsgbody);
            }
        } catch (Exception e) {
            LOG.error("Unable to process message", e);
        }
    }

    private Map<String, String> loadMap(String msgType, String mapDirName) {
        Map<String, String> results = new HashMap<>();

        String dirName = mapDirName;

        if (mapDirName == null) {
            String rootdir = System.getenv(DMAAPLISTENERROOT);

            if ((rootdir == null) || (rootdir.length() == 0)) {
                rootdir = "/opt/app/dmaap-listener";
            }

            dirName = rootdir + "/lib";
        }

        String mapFilename = dirName + "/" + msgType + ".map";

        File mapFile = new File(mapFilename);

        if (!mapFile.canRead()) {
            LOG.error(String.format("Cannot read map file (%s)", mapFilename));
            return null;
        }

        try (BufferedReader mapReader = new BufferedReader(new FileReader(mapFile))) {

            String curLine;

            while ((curLine = mapReader.readLine()) != null) {
                curLine = curLine.trim();

                if ((curLine.length() > 0) && (!curLine.startsWith("#")) && curLine.contains("=>")) {
                    String[] entry = curLine.split("=>");
                    if (entry.length == 2) {
                        results.put(entry[0].trim(), entry[1].trim());
                    }
                }
            }
            mapReader.close();
        } catch (Exception e) {
            LOG.error("Caught exception reading map " + mapFilename, e);
            return null;
        }

        return results;
    }
}
