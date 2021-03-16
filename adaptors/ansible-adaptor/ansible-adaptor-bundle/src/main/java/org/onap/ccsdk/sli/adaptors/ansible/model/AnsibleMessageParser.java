/*-
 * ============LICENSE_START=======================================================
 * ONAP : SLI
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ansible.model;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptorConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptorConstants.*;

/**
 * Class that validates and constructs requests sent/received from
 * Ansible Server
 */
public class AnsibleMessageParser {


    private static final String JSON_ERROR_MESSAGE = "JSONException: Error parsing response";

    private static final Logger LOGGER = LoggerFactory.getLogger(AnsibleMessageParser.class);

    /**
     * Accepts a map of strings and
     * a) validates if all parameters are appropriate (else, throws an exception) and
     * b) if correct returns a JSON object with appropriate key-value pairs to send to the server.
     * <p>
     * Mandatory parameters, that must be in the supplied information to the Ansible Adaptor
     * 1. URL to connect to
     * 2. credentials for URL (assume user pswd for now)
     * 3. Playbook name
     */
    public JSONObject reqMessage(Map<String, String> params) throws SvcLogicException {
        final String[] mandatoryTestParams = {AGENT_URL, PLAYBOOK_NAME, USER, PSWD};
        final String[] optionalTestParams = {ENV_PARAMETERS, NODE_LIST, LOCAL_PARAMETERS, TIMEOUT, VERSION, FILE_PARAMETERS,
                                             ACTION, INVENTORY_NAMES, AUTO_NODE_LIST};
        JSONObject jsonPayload = new JSONObject();

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
            jsonPayload.put(key, params.get(key));
        }

        parseOptionalParams(params, optionalTestParams, jsonPayload);

        // Generate a unique uuid for the test
        String reqId = UUID.randomUUID().toString();
        jsonPayload.put(ID, reqId);
        return jsonPayload;
    }

    /**
     * Method that validates that the Map has enough information
     * to query Ansible server for a result. If so, it returns
     * the appropriate url, else an empty string.
     */
    public String reqUriResult(Map<String, String> params) throws SvcLogicException {
        final String[] mandatoryTestParams = {AGENT_URL, ID, USER, PSWD};
        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }
        return params.get(AGENT_URL) + "?Id=" + params.get(ID) + "&Type=GetResult";
    }

    /**
     * Method that validates that the Map has enough information to query Ansible
     * server for a result. If so, it returns the appropriate url, else an empty
     * string.
     */
    public String reqUriResultWithIP(Map<String, String> params, String serverIP) throws SvcLogicException {
        final String[] mandatoryTestParams = {AGENT_URL, ID, USER, PSWD};
        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }
        String[] arr1 = params.get(AGENT_URL).split("//", 2);
        String[] arr2 = arr1[1].split(":", 2);
        return arr1[0] + "//" + serverIP + ":" + arr2[1] + "?Id=" + params.get(ID) + "&Type=GetResult";
    }

    /**
     * Method that validates that the Map has enough information to query Ansible
     * server for logs. If so, it populates the appropriate returns the appropriate
     * url, else an empty string.
     */
    public String reqUriLog(Map<String, String> params) throws SvcLogicException {
        final String[] mandatoryTestParams = {AGENT_URL, ID, USER, PSWD};
        for (String mandatoryParam : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, mandatoryParam);
        }
        return params.get(AGENT_URL) + "?Id=" + params.get(ID) + "&Type=GetLog";
    }

    /**
     * Method that validates that the Map has enough information
     * to query Ansible server for an output. If so, it returns
     * the appropriate url, else an empty string.
     */
    public String reqUriOutput(Map<String, String> params) throws SvcLogicException {
        final String[] mandatoryTestParams = {AGENT_URL, ID, USER, PSWD};
        for (String mandatoryParam : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, mandatoryParam);
        }
        return params.get(AGENT_URL) + "?Id=" + params.get(ID) + "&Type=GetOutput";
    }

    /**
     * This method parses response from the Ansible Server when we do a post
     * and returns an AnsibleResult object.
     */
    public AnsibleResult parsePostResponse(String input) throws SvcLogicException {
        AnsibleResult ansibleResult;
        try {
            JSONObject postResponse = new JSONObject(input);
            int code = postResponse.getInt(STATUS_CODE);
            int initResponseValue = AnsibleResultCodes.INITRESPONSE.getValue();
            boolean validCode = AnsibleResultCodes.CODE.checkValidCode(initResponseValue, code);
            if (!validCode) {
                throw new SvcLogicException(String.format("Invalid InitResponse code = %s received. MUST be one of %s",
                        code, AnsibleResultCodes.CODE.getValidCodes(initResponseValue)));
            }

            ansibleResult = new AnsibleResult(code, postResponse.getString(STATUS_MESSAGE));
            if (postResponse.has(ANSIBLE_SERVER) && StringUtils.isNotBlank(postResponse.getString(ANSIBLE_SERVER))) {
                ansibleResult.setServerIp(postResponse.getString(ANSIBLE_SERVER));
            }
            if (!postResponse.isNull(OUTPUT)) {
                LOGGER.info("Processing results-output in post response");
                JSONObject output = postResponse.getJSONObject(OUTPUT);
                ansibleResult.setOutput(output.toString());
            }
        } catch (JSONException e) {
            LOGGER.error(JSON_ERROR_MESSAGE, e);
            ansibleResult = new AnsibleResult(600, "Error parsing response = " + input + ". Error = " + e.getMessage());
        }
        return ansibleResult;
    }

    /**
     * This method parses response from an Ansible server when we do a GET for a result
     * and returns an AnsibleResult object.
     **/
    public AnsibleResult parseGetResponse(String input) throws SvcLogicException {
        AnsibleResult ansibleResult = new AnsibleResult();
        try {
            JSONObject postResponse = new JSONObject(input);
            parseGetResponseNested(ansibleResult, postResponse);
        } catch (JSONException e) {
            LOGGER.error(JSON_ERROR_MESSAGE, e);
            ansibleResult = new AnsibleResult(AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error parsing response = " + input + ". Error = " + e.getMessage(), "");
        }
        return ansibleResult;
    }

    private void parseGetResponseNested(AnsibleResult ansibleResult, JSONObject postRsp) throws SvcLogicException {
        String messageStatus = postRsp.getString(STATUS_MESSAGE);
        int codeStatus = postRsp.getInt(STATUS_CODE);
        int finalCode = AnsibleResultCodes.FINAL_SUCCESS.getValue();
        boolean valCode = AnsibleResultCodes.CODE.checkValidCode(AnsibleResultCodes.FINALRESPONSE.getValue(), codeStatus);
        if (!valCode) {
            throw new SvcLogicException(String.format("Invalid InitResponse code = %s received. MUST be one of %s",
                    codeStatus, AnsibleResultCodes.CODE.getValidCodes(AnsibleResultCodes.FINALRESPONSE.getValue())));
        }

        ansibleResult.setStatusCode(codeStatus);
        ansibleResult.setStatusMessage(messageStatus);
        ansibleResult.setConfigData("UNKNOWN");
        LOGGER.info("Received response with code = {}, Message = {}", codeStatus, messageStatus);

        if (!postRsp.isNull("Results")) {

            // Results are available. process them
            // Results is a dictionary of the form

            LOGGER.info("Processing results in response");
            JSONObject results = postRsp.getJSONObject("Results");

            LOGGER.info("Get JSON dictionary from Results by Iterating through hosts");
            Iterator<String> hosts = results.keys();
            while (hosts.hasNext()) {
                String host = hosts.next();
                LOGGER.info("Processing host = {}",
                        (host.matches("^[\\w\\-.]+$")) ? host : "[unexpected value, logging suppressed]");
                try {
                    JSONObject hostResponse = results.getJSONObject(host);
                    int subCode = hostResponse.getInt(STATUS_CODE);
                    String message = hostResponse.getString(STATUS_MESSAGE);

                    LOGGER.info("Code = {}, Message = {}", subCode, message);

                    if (subCode != 200 || !"SUCCESS".equals(message)) {
                        finalCode = AnsibleResultCodes.REQ_FAILURE.getValue();
                    }
                    if ((hostResponse.optJSONObject(OUTPUT)) != null) {
                        JSONObject hostResponseObjectInfo = hostResponse.optJSONObject(OUTPUT).optJSONObject("info");
                        JSONObject hostResponseConfigData = hostResponseObjectInfo.optJSONObject("configData");
                        if (hostResponseConfigData != null) {
                            ansibleResult.setConfigData(hostResponseConfigData.toString());
                        }
                    }
                } catch (JSONException e) {
                    LOGGER.error(JSON_ERROR_MESSAGE, e);
                    ansibleResult.setStatusCode(AnsibleResultCodes.INVALID_RESPONSE.getValue());
                    ansibleResult.setStatusMessage(String.format("Error processing response message = %s from host %s",
                            results.getString(host), host));
                    break;
                }
            }
            ansibleResult.setStatusCode(finalCode);

            // We return entire Results object as message
            ansibleResult.setResults(results.toString());
        } else {
            ansibleResult.setStatusCode(AnsibleResultCodes.INVALID_RESPONSE.getValue());
            ansibleResult.setStatusMessage("Results not found in GET for response");
        }
        if (!postRsp.isNull(OUTPUT)) {
            LOGGER.info("Processing results-output in response");
            JSONObject output = postRsp.getJSONObject(OUTPUT);
            ansibleResult.setOutput(output.toString());
        }
    }

    private void parseOptionalParams(Map<String, String> params, String[] optionalTestParams, JSONObject jsonPayload) {

        Set<String> optionalParamsSet = new HashSet<>();
        Collections.addAll(optionalParamsSet, optionalTestParams);

        //@formatter:off
        params.entrySet().stream().filter(entry -> optionalParamsSet.contains(entry.getKey()))
                .filter(entry -> !Strings.isNullOrEmpty(entry.getValue()))
                .forEach(entry -> parseOptionalParam(entry, jsonPayload));
        //@formatter:on
    }

    private void parseOptionalParam(Map.Entry<String, String> params, JSONObject jsonPayload) {
        String key = params.getKey();
        String payload = params.getValue();

        switch (key) {
            case TIMEOUT:
                if (dataIsVariable(payload)) {
                    break;
                }
                int timeout = Integer.parseInt(payload);
                if (timeout < 0) {
                    throw new NumberFormatException(" : specified negative integer for timeout = " + payload);
                }
                jsonPayload.put(key, payload);
                break;
            case AUTO_NODE_LIST:
                if (payload.equalsIgnoreCase("true") || payload.equalsIgnoreCase("false")) {
                    jsonPayload.put(key, payload);
                } else {
                    throw new IllegalArgumentException(" : specified invalid boolean value of AutoNodeList = " + payload);
                }
                break;
            case VERSION:
                if (dataIsVariable(payload)) {
                    break;
                }
            case INVENTORY_NAMES:
                jsonPayload.put(key, payload);
                break;

            case LOCAL_PARAMETERS:
            case ENV_PARAMETERS:
            case EXTRA_VARS:
                JSONObject paramsJson = new JSONObject(payload);
                jsonDataIsVariable(paramsJson);
                jsonPayload.put(key, paramsJson);
                break;

            case NODE_LIST:
                if (payload.startsWith("$")) {
                    break;
                }
                JSONArray paramsArray = new JSONArray(payload);
                jsonPayload.put(key, paramsArray);
                break;

            case FILE_PARAMETERS:
                if (dataIsVariable(payload)) {
                    break;
                }
                jsonPayload.put(key, getFilePayload(payload));
                break;

            default:
                break;
        }
    }

    /**
     * Return payload with escaped newlines
     */
    private JSONObject getFilePayload(String payload) {
        String formattedPayload = payload.replace("\n", "\\n").replace("\r", "\\r");
        return new JSONObject(formattedPayload);
    }

    private void throwIfMissingMandatoryParam(Map<String, String> params, String key) throws SvcLogicException {
        if (!params.containsKey(key)) {
            throw new SvcLogicException(String.format(
                    "Ansible: Mandatory AnsibleAdaptor key %s not found in parameters provided by calling agent !", key));
        }
        if (Strings.isNullOrEmpty(params.get(key))) {
            throw new SvcLogicException(String.format(
                    "Ansible: Mandatory AnsibleAdaptor key %s not found in parameters provided by calling agent !", key));
        }
        if (StringUtils.startsWith(params.get(key), "$")) {
            throw new SvcLogicException(String.format(
                    "Ansible: Mandatory AnsibleAdaptor key %s is a variable", key));
        }
    }

    private boolean dataIsVariable(String payload) {
        return StringUtils.startsWith(payload, "$") || StringUtils.isEmpty(payload);
    }

    private void jsonDataIsVariable(JSONObject paramsJson) {
        LOGGER.info("input json is " + paramsJson);
        String[] keys = JSONObject.getNames(paramsJson);
        for (String k : keys) {
            Object a = paramsJson.get(k);
            if (a instanceof String) {
                if (StringUtils.startsWith(a.toString(), "$") || StringUtils.isEmpty(a.toString())) {
                    LOGGER.info("removing key " + k);
                    paramsJson.remove(k);
                }
            }
        }
        LOGGER.info("returning json as {}", paramsJson);
    }

}
