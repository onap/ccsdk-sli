/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *             reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SdncAaiDmaapConsumer extends SdncDmaapConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(SdncAaiDmaapConsumer.class);
    private static final String SDNC_ENDPOINT = "SDNC.endpoint";
    private static final String TEMPLATE = "SDNC.template";
    private static final String DMAAPLISTENERROOT = "DMAAPLISTENERROOT";

    private static final String ESR_SYSTEM_INFO = "esr-system-info";
    private static final String RELATIONSHIP_LIST = "relationship-list";
    private static final String ESR_SYSTEM_INFO_LIST = "esr-system-info-list";
    private static final String AAI_EVENT = "AAI-EVENT";

    private static final String EVENT_TYPE = "event-type";
    private static final String ENTITY = "entity";
    private static final String ENTITY_TYPE = "entity-type";
    private static final String EVENT_HEADER = "event-header";

    private String rootDir;

    protected VelocityEngine velocityEngine;

    public SdncAaiDmaapConsumer() {
        velocityEngine = new VelocityEngine();
        Properties props = new Properties();
        rootDir = System.getenv(DMAAPLISTENERROOT);

        if ((rootDir == null) || (rootDir.length() == 0)) {
            rootDir = "/opt/onap/sdnc/dmaap-listener/lib/";
        }
        else {
            rootDir = rootDir + "/lib/";
        }

        props.put("file.resource.loader.path", rootDir);
        velocityEngine.init(props);
    }

    /*
     * for testing purposes
     */
    SdncAaiDmaapConsumer(Properties props) {
        velocityEngine = new VelocityEngine();
        velocityEngine.init(props);
    }

    protected String publish(String templatePath, String jsonString) throws IOException
    {
        if (templatePath.contains("esr-thirdparty-sdnc")){
            return publishEsrThirdPartySdnc(templatePath, jsonString);
        } else {
            return publishFullMessage(templatePath, jsonString);
        }
    }

    private String publishFullMessage(String templatePath, String jsonString) throws IOException
    {
        JSONObject jsonObj = new JSONObject(jsonString);
        VelocityContext context = new VelocityContext();
        for(Object key : jsonObj.keySet())
        {
            context.put((String)key, jsonObj.get((String)key));
        }

        String id = jsonObj.getJSONObject(EVENT_HEADER).get("id").toString();
        context.put("req_id", id);

        context.put("curr_time", Instant.now());

        ObjectMapper oMapper = new ObjectMapper();

        String rpcMsgbody = oMapper.writeValueAsString(jsonString);
        context.put("full_message", rpcMsgbody);

        Writer writer = new StringWriter();
        velocityEngine.mergeTemplate(templatePath, "UTF-8", context, writer);
        writer.flush();

        return writer.toString();
    }

    private String publishEsrThirdPartySdnc(String templatePath, String jsonString) throws IOException
    {
        JSONObject jsonObj = new JSONObject(jsonString);
        VelocityContext context = new VelocityContext();

        JSONObject eventHeader = jsonObj.getJSONObject(EVENT_HEADER);
        for(Object key : eventHeader.keySet())
        {
            if (!("action").equals(key)) {
                context.put(((String)key).replaceAll("-", ""), eventHeader.get((String)key));
            } else {
                String action = (String) eventHeader.get((String) key);
                if (("create").equalsIgnoreCase(action)) {
                    context.put((String)key,"Update");
                } else if (("delete").equalsIgnoreCase(action)) {
                    context.put((String) key, "Delete");
                } else {
                    throw new IOException("Action type not supported " + action);
                }
            }
        }

        JSONObject entityObj = jsonObj.getJSONObject(ENTITY);
        for(Object key : entityObj.keySet())
        {
            switch((String)key)
            {
                case ESR_SYSTEM_INFO_LIST :
                    JSONArray esrSystemInfo = entityObj.getJSONObject((String)key)
                                                       .getJSONArray(ESR_SYSTEM_INFO);

                    for (int i = 0; i < esrSystemInfo.length(); i++) {
                        JSONObject objects = esrSystemInfo.getJSONObject(i);

                        for (Object name : objects.keySet()) {
                            context.put(((String)name).replaceAll("-", ""),
                                        objects.get((String)name).toString());
                        }
                    }
                    break;

                case RELATIONSHIP_LIST :
                    //convertion not required for relationship
                    break;

                default :
                    context.put(((String)key).replaceAll("-", ""),
                                entityObj.get((String)key).toString());
                    break;
            }
        }

        Writer writer = new StringWriter();
        velocityEngine.mergeTemplate(templatePath, "UTF-8", context, writer);
        writer.flush();

        return writer.toString();
    }

    @Override
    public void processMsg(String msg) throws InvalidMessageException {

        if (msg == null) {
            throw new InvalidMessageException("Null message");
        }

        ObjectMapper oMapper = new ObjectMapper();
        JsonNode aaiRootNode;
        try {
            aaiRootNode = oMapper.readTree(msg);
        } catch (Exception e) {
            throw new InvalidMessageException("Cannot parse json object", e);
        }

        JsonNode eventHeaderNode = aaiRootNode.get(EVENT_HEADER);
        if(eventHeaderNode == null) {
            LOG.info("Missing Event Header node.");
            return;
        }
        JsonNode eventTypeNode = eventHeaderNode.get(EVENT_TYPE);
        String eventType = eventTypeNode.textValue();

        if(!AAI_EVENT.equals(eventType)) {
            LOG.info("Unknown Event Type {}", eventType);
            return;
        }

        JsonNode entityTypeNode = eventHeaderNode.get(ENTITY_TYPE);
        String entityType = entityTypeNode.textValue();

        String mapFilename = rootDir + entityType + ".map";
        Map<String, String> fieldMap = loadMap(mapFilename);
        if (fieldMap == null) {
            return;
        }

        if (!fieldMap.containsKey(SDNC_ENDPOINT)) {
            return;
        }
        String sdncEndpoint = fieldMap.get(SDNC_ENDPOINT);

        if (!fieldMap.containsKey(TEMPLATE)) {
            throw new InvalidMessageException("No SDNC template known for message " + entityType);
        }
        String templateName = fieldMap.get(TEMPLATE);

        try {
            String rpcMsgbody = publish(templateName, msg);
            String odlUrlBase = getProperty("sdnc.odl.url-base");
            String odlUser = getProperty("sdnc.odl.user");
            String odlPassword = getProperty("sdnc.odl.password");

            if ((odlUrlBase != null) && (odlUrlBase.length() > 0)) {
                SdncOdlConnection conn = SdncOdlConnection.newInstance(odlUrlBase + "/" + sdncEndpoint, odlUser, odlPassword);

                conn.send("POST", "application/json", rpcMsgbody);
            } else {
                LOG.info("POST message body would be:\n" + rpcMsgbody);
            }
        } catch (Exception e) {
            LOG.error("Unable to process message", e);
        }
    }

    private Map<String, String> loadMap(String mapFilename) {
        File mapFile = new File(mapFilename);

        if (!mapFile.canRead()) {
            LOG.error(String.format("Cannot read map file (%s)", mapFilename));
            return null;
        }

        Map<String, String> results = new HashMap<>();
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
