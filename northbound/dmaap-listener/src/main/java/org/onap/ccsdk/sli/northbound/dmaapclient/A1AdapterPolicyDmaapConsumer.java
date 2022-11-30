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
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSession;
import org.apache.commons.codec.binary.Base64;
import org.onap.ccsdk.sli.core.utils.common.AcceptIpAddressHostNameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class A1AdapterPolicyDmaapConsumer extends SdncDmaapConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(A1AdapterPolicyDmaapConsumer.class);

    private static final String BODY = "body";
    private static final String RPC = "rpc-name";
    private static final String INPUT = "input";
    private static final String PAYLOAD = "Payload";
    private static final String CORRELATION_ID = "correlation-id";
    private static final String COMMON_HEADER = "CommonHeader";


    @Override
    public void processMsg(String msg) throws InvalidMessageException {

        if (msg == null) {
            throw new InvalidMessageException("Null A1-ADAPTER-DMAAP message");
        }

        ObjectMapper oMapper = new ObjectMapper();
        JsonNode a1AdapterRootNode;
        try {
            a1AdapterRootNode = oMapper.readTree(msg);
        } catch (Exception e) {
            throw new InvalidMessageException("Cannot parse A1-ADAPTER-DMAAP json input", e);
        }

        JsonNode bodyNode = a1AdapterRootNode.get(BODY);
        if(bodyNode == null) {
            LOG.warn("Missing body in A1-ADAPTER-DMAAP message");
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

        LOG.info("Payloadnode  :"+payloadNode.textValue()+"\n");
        JsonNode payload_node_from_string;
        try {
         payload_node_from_string = oMapper.readTree(payloadNode.textValue());
        } catch (Exception e) {
            throw new InvalidMessageException("Cannot parse A1-ADAPTER-DMAAP json input", e);
        }

        JsonNode body_PayloadNode= payload_node_from_string.get(INPUT).get(BODY);
        if(body_PayloadNode == null) {
            LOG.warn("Missing body under Payload node in A1-ADAPTER-DMAAP message");
            return;
        }

        String body_payload_replaced_string=body_PayloadNode.toString().replaceAll("\"","\\\\\"");
        body_payload_replaced_string="\""+body_payload_replaced_string+"\"";
        LOG.info("Body Payload  :"+body_payload_replaced_string+"\n");

        JsonNode input_PayloadNode =payload_node_from_string.get(INPUT);
        if(input_PayloadNode == null) {
            LOG.warn("Missing body under Payload node in A1-ADAPTER-DMAAP message");
            return;
        }
        ((ObjectNode)input_PayloadNode).put(BODY,body_payload_replaced_string);
        ((ObjectNode)payload_node_from_string).set("input",input_PayloadNode);



        JsonNode CommonHeader = input.get(COMMON_HEADER);
        if(CommonHeader == null) {
            LOG.info("Missing CommonHeader node.");
            return;
        }



        JsonNode correlation_id = a1AdapterRootNode.get(CORRELATION_ID);
        if(correlation_id == null) {
          LOG.info("Missing correlation_id node.");
          return;
        }
        String correlationId= correlation_id.textValue();


        String rpcMsgbody;
        try {
            	ObjectMapper mapper = new ObjectMapper();
                rpcMsgbody = mapper.writeValueAsString(payloadNode);

        } catch (Exception e) {
            LOG.error("Unable to parse payload in A1-ADAPTER-DMAAP message", e);
            return;
        }
        rpcMsgbody= payload_node_from_string.toString().replaceAll("\\\\\\\\\\\\\"","\\\\\"");
        rpcMsgbody= rpcMsgbody.replaceAll(":\"\\\\\"",":\"");
        rpcMsgbody= rpcMsgbody.replaceAll("\\\\\"\"","\"");

        JsonNode rpcNode = a1AdapterRootNode.get(RPC);
        if(rpcNode == null) {
            LOG.warn("Missing node in A1-ADAPTER-DMAAP message- " + RPC);
            return;
        }
        String rpc = rpcNode.textValue();
       if(rpc.contains("puta1policy"))
        rpc = "putA1Policy";
        String sdncEndpoint = "A1-ADAPTER-API:" + rpc;

        try {
            String odlUrlBase = getProperty("sdnc.odl.url-base");
            String odlUser = getProperty("sdnc.odl.user");
            String odlPassword = getProperty("sdnc.odl.password");
            LOG.info("POST A1-ADAPTER-API Request " + rpcMsgbody);
            if ((odlUrlBase != null) && (odlUrlBase.length() > 0)) {
                SdncOdlConnection conn = SdncOdlConnection.newInstance(odlUrlBase + "/" + sdncEndpoint, odlUser, odlPassword);
                String resp=conn.send("POST", "application/json", rpcMsgbody);


                LOG.info("Recieved response code " + resp);
                int code;
                String value;
                if(resp.contains("200")|| resp.contains("201")) {
                 code=200;
                 value="SUCCESS";
                }else {
                 code=400;
                 value="FAILURE";
                }

             ObjectMapper mapper = new ObjectMapper();
             ObjectNode a1p_rsp_node = mapper.createObjectNode();

             a1p_rsp_node.put("version", "1.0");
             a1p_rsp_node.put("rpc-name", rpc );
             a1p_rsp_node.put("correlation-id", correlationId);
             a1p_rsp_node.put("type", "response");

             ObjectNode a1p_rsp_body_Status = mapper.createObjectNode();
             a1p_rsp_body_Status.put("Code", code);
             a1p_rsp_body_Status.put("Value", value);

             ObjectNode a1p_rsp_body_output = mapper.createObjectNode();
             a1p_rsp_body_output.set("CommonHeader", CommonHeader);
             a1p_rsp_body_output.set("Status", a1p_rsp_body_Status);
             a1p_rsp_body_output.put("Payload", "");

             ObjectNode a1p_rsp_body = mapper.createObjectNode();
             a1p_rsp_body.set("output", a1p_rsp_body_output);
             a1p_rsp_node.set("body", a1p_rsp_body);

             String jsonString = mapper.writer().writeValueAsString(a1p_rsp_node);
             System.out.println(jsonString);

             URL a1_p_Rspurl = new URL("http://message-router:3904/events/A1-P-RSP");
             HttpURLConnection httpConn = (HttpURLConnection) a1_p_Rspurl.openConnection();
             httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Content-Type", "application/json");
                httpConn.setRequestProperty("Accept", "application/json");
                httpConn.setDoInput(true);
                httpConn.setDoOutput(true);
                httpConn.setUseCaches(false);
                if (httpConn instanceof HttpsURLConnection) {

                 HostnameVerifier hostnameVerifier = new AcceptIpAddressHostNameVerifier();
                    ((HttpsURLConnection) httpConn).setHostnameVerifier(hostnameVerifier);
                }
                // Write message
                httpConn.setRequestProperty("Content-Length", Integer.toString(jsonString.length()));
                DataOutputStream outStr = new DataOutputStream(httpConn.getOutputStream());
                outStr.write(jsonString.getBytes());
                outStr.close();
                // Read response
                BufferedReader respRdr;
                LOG.info("A1-P_RSP Response: " + httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
                if (httpConn.getResponseCode() < 300) {
                    respRdr = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                } else {
                    respRdr = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
                }
                StringBuilder respBuff = new StringBuilder();
                String respLn;
                while ((respLn = respRdr.readLine()) != null) {
                    respBuff.append(respLn).append("\n");
                }
                respRdr.close();
                String respString = respBuff.toString();
                LOG.info(String.format("A1-P_RSP Response body :%n%s", respString));



            } else {
                LOG.warn("Unable to POST A1-ADAPTER-API message. SDNC URL not available. body:\n" + rpcMsgbody);
            }
        } catch (Exception e) {
            LOG.error("Unable to process message", e);
        }
    }
}
