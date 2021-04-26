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

package org.onap.ccsdk.sli.adaptors.ansible.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptor;
import org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptorPropertiesProvider;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleMessageParser;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResultCodes;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleServerEmulator;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.encryption.EncryptionTool;

import static org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptorConstants.*;

/**
 * This class implements the {@link AnsibleAdaptor} interface. This interface defines the behaviors
 * that our service provides.
 */
public class AnsibleAdaptorImpl implements AnsibleAdaptor {

    /**
     * Adaptor Name
     */
    private static final String Adaptor_NAME = "Ansible Adaptor";
    private static final String SVC_LOGIC_EXCEPTION_CAUGHT = "SvcLogicException caught";

    /**
     * The logger to be used
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AnsibleAdaptorImpl.class);
    private int defaultTimeout = 600 * 1000;
    private int defaultSocketTimeout = 60 * 1000;
    private int defaultPollInterval = 60 * 1000;
    /**
     * Ansible API Message Handlers
     **/
    private AnsibleMessageParser messageProcessor;

    /**
     * indicator whether in test mode
     **/
    private boolean testMode = false;

    /**
     * server emulator object to be used if in test mode
     **/
    private AnsibleServerEmulator testServer;

    /**
     * This default constructor is used as a work around because the activator wasn't getting called
     */
    public AnsibleAdaptorImpl() {
        initialize(new AnsibleAdaptorPropertiesProviderImpl());
    }

    /**
     * Instantiates a new Ansible adaptor.
     *
     * @param propProvider the prop provider
     */
    public AnsibleAdaptorImpl(AnsibleAdaptorPropertiesProvider propProvider) {
        initialize(propProvider);
    }

    /**
     * Used for jUnit test and testing interface
     *
     * @param mode the mode
     */
    public AnsibleAdaptorImpl(boolean mode) {
        testMode = mode;
        testServer = new AnsibleServerEmulator();
        messageProcessor = new AnsibleMessageParser();
    }

    /**
     * Returns the symbolic name of the adaptor
     *
     * @return The adaptor name
     */
    @Override
    public String getAdaptorName() {
        return Adaptor_NAME;
    }

    @SuppressWarnings("static-method")
    private void doFailure(SvcLogicContext svcLogic, int code, String message) throws SvcLogicException {
        svcLogic.markFailed();
        svcLogic.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(code));
        svcLogic.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);
        throw new SvcLogicException("Ansible Adaptor Error = " + message);
    }

    /**
     * initialize the Ansible adaptor based on default and over-ride configuration data
     */
    private void initialize(AnsibleAdaptorPropertiesProvider propProvider) {
        Properties props = propProvider.getProperties();
        // Create the message processor instance
        messageProcessor = new AnsibleMessageParser();

        //continuing for checking defaultTimeout
        try {
            String timeoutStr = props.getProperty(TIMEOUT_PROPERTY_NAME);
            defaultTimeout = Integer.parseInt(timeoutStr) * 1000;
        } catch (Exception e) {
            defaultTimeout = 600 * 1000;
            logger.error("Error while reading time out property", e);
        }
        //continuing for checking defaultSocketTimeout
        try {
            String timeoutStr = props.getProperty(SOCKET_TIMEOUT_PROPERTY_NAME);
            defaultSocketTimeout = Integer.parseInt(timeoutStr) * 1000;
        } catch (Exception e) {
            defaultSocketTimeout = 60 * 1000;
            logger.error("Error while reading socket time out property", e);
        }
        //continuing for checking defaultPollInterval
        try {
            String timeoutStr = props.getProperty(POLL_INTERVAL_PROPERTY_NAME);
            defaultPollInterval = Integer.parseInt(timeoutStr) * 1000;
        } catch (Exception e) {
            defaultPollInterval = 60 * 1000;
            logger.error("Error while reading poll interval property", e);
        }
        logger.info("Initialized Ansible Adaptor");
    }

    private ConnectionBuilder getHttpConn(int timeout, String serverIP) {
        String path = PROPDIR + APPC_PROPS;
        File propFile = new File(path);
        Properties props = new Properties();
        InputStream input;
        try {
            input = new FileInputStream(propFile);
            props.load(input);
        } catch (Exception ex) {
            logger.error("Error while reading appc.properties file {}", ex.getMessage());
        }
        // Create the http client instance
        // type of client is extracted from the property file parameter
        // org.onap.appc.adaptor.ansible.clientType
        // It can be :
        // 1. TRUST_ALL (trust all SSL certs). To be used ONLY in dev
        // 2. TRUST_CERT (trust only those whose certificates have been stored in the trustStore file)
        // 3. DEFAULT (trust only well known certificates). This is standard behavior to which it will
        // revert. To be used in PROD
        ConnectionBuilder httpClientLocal = null;
        try {
            String clientType = props.getProperty(CLIENT_TYPE_PROPERTY_NAME);
            logger.info("Ansible http client type set to {}", clientType);
            if ("TRUST_ALL".equals(clientType)) {
                logger.info("Creating http client to trust ALL ssl certificates. WARNING. This should be done only in dev environments");
                httpClientLocal = new ConnectionBuilder(1, timeout);
            } else if ("TRUST_CERT".equals(clientType)) {
                // set path to keystore file
                String trustStoreFile = props.getProperty(TRUSTSTORE_PROPERTY_NAME);
                String key = props.getProperty(TRUSTSTORE_PASS_PROPERTY_NAME);
                char[] trustStorePasswd = EncryptionTool.getInstance().decrypt(key).toCharArray();
                logger.info("Creating http client with trust manager from {}", trustStoreFile);
                httpClientLocal = new ConnectionBuilder(trustStoreFile, trustStorePasswd, timeout, serverIP);
            } else {
                logger.info("Creating http client with default behaviour");
                httpClientLocal = new ConnectionBuilder(0, timeout);
            }
        } catch (Exception e) {
            logger.error("Error Getting HTTP Connection Builder due to Unknown Exception", e);
        }

        logger.info("Got HTTP Connection Builder");
        return httpClientLocal;
    }

    // Public Method to post request to execute playbook. Posts the following back
    // to Svc context memory
    //  org.onap.appc.adaptor.ansible.req.code : 100 if successful
    //  org.onap.appc.adaptor.ansible.req.messge : any message
    //  org.onap.appc.adaptor.ansible.req.Id : a unique uuid to reference the request
    @Override
    public void reqExec(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String playbookName = StringUtils.EMPTY;
        String payload = StringUtils.EMPTY;
        String agentUrl = StringUtils.EMPTY;
        String user = StringUtils.EMPTY;
        String pswd = StringUtils.EMPTY;
        String id = StringUtils.EMPTY;

        try {
            // create json object to send request
            JSONObject jsonPayload = messageProcessor.reqMessage(params);
            logger.info("Initial Payload = {}", jsonPayload.toString());

            agentUrl = (String) jsonPayload.remove("AgentUrl");
            id = jsonPayload.getString("Id");
            user = (String) jsonPayload.remove(USER);
            pswd = (String) jsonPayload.remove(PSWD);
            if (StringUtils.isNotBlank(pswd)) {
                pswd = EncryptionTool.getInstance().decrypt(pswd);
            }
            String timeout = jsonPayload.getString("Timeout");
            if (StringUtils.isBlank(timeout)) {
                timeout = "600";
            }

            String autoNodeList = (String) jsonPayload.remove("AutoNodeList");
            if (Boolean.parseBoolean(autoNodeList)) {
                JSONArray generatedNodeList = generateNodeList(params, ctx);
                if (generatedNodeList.length() > 0) {
                    jsonPayload.put("NodeList", generatedNodeList);
                    jsonPayload.put("InventoryNames", "VM");
                } else {
                    doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                            "Auto generation of Node List Failed - no elements on the list");
                }
            } else {
                logger.debug("Auto Node List is DISABLED");
            }

            payload = jsonPayload.toString();
            ctx.setAttribute("AnsibleTimeout", timeout);
            logger.info("Updated Payload = {} timeout = {}", payload, timeout);
        } catch (SvcLogicException e) {
            logger.error(SVC_LOGIC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request for execution of playbook due to missing mandatory parameters. Reason = "
                    + e.getMessage());
        } catch (JSONException e) {
            logger.error("JSONException caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request for execution of playbook due to invalid JSON block. Reason = "
                    + e.getMessage());
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request for execution of playbook due to invalid parameter values. Reason = "
                    + e.getMessage());
        }

        int code = -1;
        String message = StringUtils.EMPTY;

        try {
            // post the test request
            logger.info("Posting ansible request = {} to url = {}", payload, agentUrl);
            AnsibleResult testResult = postExecRequest(agentUrl, payload, user, pswd);
            if (testResult != null) {
                logger.info("Received response on ansible post request {}", testResult.getStatusMessage());
                // Process if HTTP was successful
                if (testResult.getStatusCode() == 200) {
                    testResult = messageProcessor.parsePostResponse(testResult.getStatusMessage());
                } else {
                    doFailure(ctx, testResult.getStatusCode(),
                            "Error posting request. Reason = " + testResult.getStatusMessage());
                }

                code = testResult.getStatusCode();
                message = testResult.getStatusMessage();
                ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, testResult.getOutput());
                ctx.setAttribute(SERVERIP, StringUtils.defaultIfBlank(testResult.getServerIp(), ""));
                // Check status of test request returned by Agent
                if (code == AnsibleResultCodes.PENDING.getValue()) {
                    logger.info("Submission of Test {} successful.", playbookName);
                    // test request accepted. We are in asynchronous case
                } else {
                    doFailure(ctx, code, "Request for execution of playbook rejected. Reason = " + message);
                }
            } else {
                doFailure(ctx, code, "Ansible Test result is null");
            }
        } catch (SvcLogicException e) {
            logger.error(SVC_LOGIC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    "Exception encountered when posting request for execution of playbook. Reason = " + e.getMessage());
        }

        ctx.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(code));
        ctx.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);
        ctx.setAttribute(ID_ATTRIBUTE_NAME, id);
    }

    /**
     * Method is used to automatically generate NodeList section base on the svc context
     */
    private JSONArray generateNodeList(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String vfModuleId = StringUtils.trimToNull(params.get("vf-module-id"));
        String vnfcName = StringUtils.trimToNull(params.get("vnfc-name"));
        String vServerId = StringUtils.trimToNull(params.get("vserver-id"));
        String vnfcType = StringUtils.trimToNull(params.get("vnfc-type"));

        JSONArray result = new JSONArray();
        logger.info("GENERATING NODE LIST");
        logger.debug("Auto Node List filtering parameter vserver-id {} | vnfc-name {} | vnfc-type {} | vf-module-id {}",
                vServerId, vnfcName, vnfcType, vfModuleId);

        Map<String, JSONObject> candidates = new HashMap<>();
        for (int i = 0; ; i++) {
            String vmKey = "tmp.vnfInfo.vm[" + i + "]";
            logger.info("Looking for attributes of: {}", vmKey);
            if (ctx.getAttribute(vmKey + ".vnfc-name") != null) {
                String debugText = "Auto Node List candidate ";
                String vmVnfcName = ctx.getAttribute(vmKey + ".vnfc-name");
                String vmVnfcIpv4Address = ctx.getAttribute(vmKey + ".vnfc-ipaddress-v4-oam-vip");
                String vmVnfcType = ctx.getAttribute(vmKey + ".vnfc-type");

                if (vmVnfcName != null && vmVnfcIpv4Address != null && vmVnfcType != null
                    && !vmVnfcName.equals("") && !vmVnfcIpv4Address.equals("") && !vmVnfcType.equals("")) {
                    if (vServerId != null) {
                        String vmVserverId = ctx.getAttribute(vmKey + ".vserver-id");
                        if (!vServerId.equals(vmVserverId)) {
                            logger.debug("{}{} dropped. vserver-id mismatch", debugText, vmVnfcName);
                            continue;
                        }
                    }
                    if (vfModuleId != null) {
                        String vmVfModuleId = ctx.getAttribute(vmKey + ".vf-module-id");
                        if (!vfModuleId.equals(vmVfModuleId)) {
                            logger.debug("{}{} dropped. vf-module-id mismatch", debugText, vmVnfcName);
                            continue;
                        }
                    }
                    if (vnfcName != null && !vmVnfcName.equals(vnfcName)) {
                        logger.debug("{}{} dropped. vnfc-name mismatch", debugText, vmVnfcName);
                        continue;
                    }
                    if (vnfcType != null && !vmVnfcType.equals(vnfcType)) {
                        logger.debug("{}{} dropped. vnfc-type mismatch", debugText, vmVnfcType);
                        continue;
                    }

                    logger.info("{}{} [{},{}]", debugText, vmVnfcName, vmVnfcIpv4Address, vmVnfcType);

                    JSONObject vnfTypeCandidates;
                    JSONArray vmList;
                    if (!candidates.containsKey(vmVnfcType)) {
                        vnfTypeCandidates = new JSONObject();
                        vmList = new JSONArray();
                        vnfTypeCandidates.put("site", "site");
                        vnfTypeCandidates.put("vnfc-type", vmVnfcType);
                        vnfTypeCandidates.put("vm-info", vmList);
                        candidates.put(vmVnfcType, vnfTypeCandidates);
                    } else {
                        vnfTypeCandidates = candidates.get(vmVnfcType);
                        vmList = (JSONArray) vnfTypeCandidates.get("vm-info");
                    }

                    JSONObject candidate = new JSONObject();
                    candidate.put("ne_id", vmVnfcName);
                    candidate.put("fixed_ip_address", vmVnfcIpv4Address);
                    vmList.put(candidate);
                } else {
                    logger.warn("Incomplete information for Auto Node List candidate {}", vmKey);
                }
            } else {
                break;
            }
        }

        for (JSONObject vnfcCandidates : candidates.values()) {
            result.put(vnfcCandidates);
        }

        logger.info("GENERATING NODE LIST COMPLETED");
        return result;
    }

    /**
     * Public method to query status of a specific request It blocks till the Ansible Server
     * responds or the session times out (non-Javadoc)
     *
     * @see org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptor#reqExecResult(java.util.Map,
     * org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public void reqExecResult(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        // Get URI
        String reqUri;

        try {
            String serverIp = ctx.getAttribute(SERVERIP);
            if (StringUtils.isNotBlank(serverIp)) {
                reqUri = messageProcessor.reqUriResultWithIP(params, serverIp);
            } else {
                reqUri = messageProcessor.reqUriResult(params);
            }
            logger.info("Got uri {}", reqUri);
        } catch (SvcLogicException e) {
            logger.error(SVC_LOGIC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request to retrieve result due to missing parameters. Reason = "
                    + e.getMessage());
            return;
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request to retrieve result due to invalid parameters value. Reason = "
                    + e.getMessage());
            return;
        }

        int code;
        String message;
        String output;
        String configData;
        String results = StringUtils.EMPTY;
        String finalResponse = StringUtils.EMPTY;
        try {
            // Try to retrieve the test results (modify the URL for that)
            AnsibleResult testResult = queryServer(reqUri, params.get(USER),
                    EncryptionTool.getInstance().decrypt(params.get(PSWD)), ctx);
            code = testResult.getStatusCode();
            message = testResult.getStatusMessage();

            if (code == 200 || code == 400 || "FINISHED".equalsIgnoreCase(message)) {
                logger.info("Parsing response from ansible Server = {}", message);
                // Valid HTTP. process the Ansible message
                testResult = messageProcessor.parseGetResponse(message);
                code = testResult.getStatusCode();
                message = testResult.getStatusMessage();
                results = testResult.getResults();
                output = testResult.getOutput();
                configData = testResult.getConfigData();
                if ((StringUtils.isBlank(output)) || (output.trim().equalsIgnoreCase("{}"))) {
                    finalResponse = results;
                } else {
                    finalResponse = output;
                }
                logger.info("configData from ansible's response = {}", configData);
                ctx.setAttribute("device-running-config", configData);
            }
            logger.info("Request response = " + message);
        } catch (SvcLogicException e) {
            logger.error(SVC_LOGIC_EXCEPTION_CAUGHT, e);
            ctx.setAttribute(RESULTS_ATTRIBUTE_NAME, results);
            ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, finalResponse);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    "Exception encountered retrieving result : " + e.getMessage());
            return;
        }

        // We were able to get and process the results. Determine if playbook succeeded

        if (code == AnsibleResultCodes.FINAL_SUCCESS.getValue()) {
            message = String.format("Ansible Request  %s finished with Result = %s, Message = %s", params.get("Id"),
                    SUCCESS, message);
            logger.info(message);
        } else {
            logger.info(String.format("Ansible Request  %s finished with Result %s, Message = %s", params.get("Id"),
                    FAILURE, message));
            ctx.setAttribute(RESULTS_ATTRIBUTE_NAME, results);
            ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, finalResponse);
            doFailure(ctx, code, message);
            return;
        }

        // In case of 200, 400, FINISHED return 400
        ctx.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(400));
        ctx.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);
        ctx.setAttribute(RESULTS_ATTRIBUTE_NAME, results);
        ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, finalResponse);
        ctx.markSuccess();
    }

    /**
     * Public method to get logs from playbook execution for a specific request
     * <p>
     * It blocks till the Ansible Server responds or the session times out very similar to
     * reqExecResult logs are returned in the DG context variable org.onap.appc.adaptor.ansible.log
     */
    @Override
    public void reqExecLog(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String reqUri = StringUtils.EMPTY;
        try {
            reqUri = messageProcessor.reqUriLog(params);
            logger.info("Retrieving results from {}", reqUri);
        } catch (Exception e) {
            logger.error("Exception caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), e.getMessage());
        }

        queryServerAndProcessResult(params, ctx, reqUri, LOG_ATTRIBUTE_NAME);
    }

    /**
     * Public method to get output from playbook execution for a specific request
     * <p>
     * It blocks till the Ansible Server responds or the session times out very similar to
     * reqExecResult and output is returned in the DG context variable org.onap.appc.adaptor.ansible.output
     */
    @Override
    public void reqExecOutput(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String reqUri = StringUtils.EMPTY;
        try {
            reqUri = messageProcessor.reqUriOutput(params);
            logger.info("Retrieving results from {}", reqUri);
        } catch (Exception e) {
            logger.error("Exception caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), e.getMessage());
        }

        queryServerAndProcessResult(params, ctx, reqUri, OUTPUT_ATTRIBUTE_NAME);
    }

    /**
     * Method that posts the request
     */
    private AnsibleResult postExecRequest(String agentUrl, String payload, String user, String pswd) {
        AnsibleResult testResult = null;
        ConnectionBuilder httpClientLocal = getHttpConn(defaultSocketTimeout, "");
        if (!testMode) {
            if (httpClientLocal != null) {
                httpClientLocal.setHttpContext(user, pswd);
                testResult = httpClientLocal.post(agentUrl, payload);
                httpClientLocal.close();
            }
        } else {
            testResult = testServer.post(payload);
        }
        return testResult;
    }

    private void queryServerAndProcessResult(Map<String, String> params, SvcLogicContext ctx, String reqUri, String attributeName)
            throws SvcLogicException {
        try {
            // Try to retrieve the test results (modify the url for that)
            AnsibleResult testResult = queryServer(reqUri, params.get(USER),
                    EncryptionTool.getInstance().decrypt(params.get(PSWD)), ctx);
            String message = testResult.getStatusMessage();
            logger.info("Request output = {}", message);
            ctx.setAttribute(attributeName, message);
            ctx.markSuccess();
        } catch (Exception e) {
            logger.error("Exception caught: {}", e.getMessage(), e);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    String.format("Exception encountered retrieving output: %s", e.getMessage()));
        }
    }

    /**
     * Method to query Ansible server
     */
    private AnsibleResult queryServer(String agentUrl, String user, String pswd, SvcLogicContext ctx) {
        AnsibleResult testResult = new AnsibleResult();
        int timeout;
        try {
            timeout = Integer.parseInt(ctx.getAttribute("AnsibleTimeout")) * 1000;
        } catch (Exception e) {
            timeout = defaultTimeout;
        }
        long endTime = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < endTime) {
            String serverIP = ctx.getAttribute(SERVERIP);
            ConnectionBuilder httpClientLocal = getHttpConn(defaultSocketTimeout, serverIP);
            logger.info("Querying ansible GetResult URL = {}", agentUrl);

            if (!testMode) {
                if (httpClientLocal != null) {
                    httpClientLocal.setHttpContext(user, pswd);
                    testResult = httpClientLocal.get(agentUrl);
                    httpClientLocal.close();
                }
            } else {
                testResult = testServer.get(agentUrl);
            }
            if (testResult.getStatusCode() != AnsibleResultCodes.IO_EXCEPTION.getValue()
                && testResult.getStatusCode() != AnsibleResultCodes.PENDING.getValue()) {
                break;
            }

            try {
                Thread.sleep(defaultPollInterval);
            } catch (InterruptedException ex) {
                logger.error("Thread Interrupted Exception", ex);
                Thread.currentThread().interrupt();
            }

        }
        if (testResult.getStatusCode() == AnsibleResultCodes.PENDING.getValue()) {
            testResult.setStatusCode(AnsibleResultCodes.IO_EXCEPTION.getValue());
            testResult.setStatusMessage("Request timed out");
        }

        return testResult;
    }

}
