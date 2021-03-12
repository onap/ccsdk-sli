/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 IBM
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.chef.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.sli.adaptors.chef.ChefAdaptor;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.ChefApiClientFactory;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefApiClient;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * This class implements the {@link ChefAdaptor} interface. This interface
 * defines the behaviors that our service provides.
 */
public class ChefAdaptorImpl implements ChefAdaptor {

    @SuppressWarnings("nls")
    public static final String MDC_ADAPTOR = "adaptor";
    @SuppressWarnings("nls")
    public static final String MDC_SERVICE = "service";
    @SuppressWarnings("nls")
    public static final String OUTCOME_FAILURE = "failure";
    @SuppressWarnings("nls")
    public static final String OUTCOME_SUCCESS = "success";
    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER = "provider";
    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_IDENTITY = "identity";
    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_NAME = "name";
    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TENANT = "tenant";
    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TENANT_NAME = "name";
    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TENANT_PASSWORD = "password"; // NOSONAR
    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TENANT_USERID = "userid";
    @SuppressWarnings("nls")
    public static final String PROPERTY_PROVIDER_TYPE = "type";
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ChefAdaptorImpl.class);
    private static final String CANNOT_FIND_PRIVATE_KEY_STR = "Cannot find the private key in the APPC file system, please load the private key to ";
    private static final String POSTING_REQUEST_JSON_ERROR_STR = "Error posting request due to invalid JSON block: ";
    private static final String POSTING_REQUEST_ERROR_STR = "Error posting request: ";
    private static final String CHEF_CLIENT_RESULT_CODE_STR = "chefClientResult.code";
    private static final String CHEF_SERVER_RESULT_CODE_STR = "chefServerResult.code";
    private static final String CHEF_CLIENT_RESULT_MSG_STR = "chefClientResult.message";
    private static final String CHEF_SERVER_RESULT_MSG_STR = "chefServerResult.message";
    private static final String CHEF_ACTION_STR = "chefAction";
    private static final String NODE_LIST_STR = "NodeList";
    private static final Integer STATUS_OK = 200;
    private static final Integer STATUS_PUSHJOBCHECK = 201;
    private static final Integer PUSHJOBSTATUS = 202;
    private static final Integer KEY_NOTFOUND = 500;
    private static final Integer NO_ENVIRONMENT = 404;
    private static final Integer APPC_ERRORCODE = 401;
    private final ChefApiClientFactory chefApiClientFactory;
    private final org.onap.ccsdk.sli.adaptors.chef.impl.PrivateKeyChecker privateKeyChecker;
    // chef server Initialize variable
    private String username = StringUtils.EMPTY;
    private String clientPrivatekey = StringUtils.EMPTY;
    private String chefserver = StringUtils.EMPTY;
    private String serverAddress = StringUtils.EMPTY;
    private String organizations = StringUtils.EMPTY;

    ChefAdaptorImpl(ChefApiClientFactory chefApiClientFactory, org.onap.ccsdk.sli.adaptors.chef.impl.PrivateKeyChecker privateKeyChecker) {
        this.chefApiClientFactory = chefApiClientFactory;
        this.privateKeyChecker = privateKeyChecker;
        logger.info("Initialize Chef Adaptor");
    }

    @SuppressWarnings("nls")
    @Override
    public void vnfcEnvironment(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        logger.info("environment of VNF-C");
        chefInfo(params, ctx);
        String env = params.get("Environment");
        logger.info("Environmnet" + env);
        if (env.equals(StringUtils.EMPTY)) {
            chefServerResult(ctx, STATUS_OK, "Skip Environment block ");
        } else {
            String message;
            if (privateKeyChecker.doesExist(clientPrivatekey)) {
                try {
                    JSONObject envJ = new JSONObject(env);
                    String envName = envJ.getString("name");
                    // update the details of an environment on the Chef server.
                    ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username,
                            clientPrivatekey);
                    ChefResponse chefResponse = chefApiClient.put("/environments/" + envName, env);
                    code = chefResponse.getStatusCode();
                    message = chefResponse.getBody();
                    if (code == NO_ENVIRONMENT) {
                        // need create a new environment
                        chefResponse = chefApiClient.post("/environments", env);
                        code = chefResponse.getStatusCode();
                        message = chefResponse.getBody();
                        logger.info("requestbody {}", chefResponse.getBody());
                    }
                    chefServerResult(ctx, code, message);
                } catch (JSONException e) {
                    code = APPC_ERRORCODE;
                    logger.error(POSTING_REQUEST_JSON_ERROR_STR, e);
                    doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + e.getMessage());
                } catch (Exception e) {
                    code = APPC_ERRORCODE;
                    logger.error(POSTING_REQUEST_ERROR_STR + "vnfcEnvironment", e);
                    doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + "vnfcEnvironment" + e.getMessage());
                }
            } else {
                code = KEY_NOTFOUND;
                message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
                doFailure(ctx, code, message);
            }
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void vnfcNodeobjects(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        logger.info("update the nodeObjects of VNF-C");
        int code;
        final String LOG_ERR_METHOD_STR = "vnfcNodeobjects";
        try {
            chefInfo(params, ctx);
            String nodeListS = params.get(NODE_LIST_STR);
            String nodeS = params.get("Node");
            if (StringUtils.isNotBlank(nodeListS) && StringUtils.isNotBlank(nodeS)) {
                nodeListS = nodeListS.replace("[", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("]", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("\"", StringUtils.EMPTY);
                nodeListS = nodeListS.replace(" ", StringUtils.EMPTY);
                List<String> nodes = Arrays.asList(nodeListS.split("\\s*,\\s*"));
                code = STATUS_OK;
                String message = null;
                if (privateKeyChecker.doesExist(clientPrivatekey)) {
                    ChefApiClient cac = chefApiClientFactory.create(chefserver, organizations, username,
                            clientPrivatekey);

                    for (String nodeName : nodes) {
                        JSONObject nodeJ = new JSONObject(nodeS);
                        nodeJ.remove("name");
                        nodeJ.put("name", nodeName);
                        String nodeObject = nodeJ.toString();
                        logger.info(nodeObject);
                        ChefResponse chefResponse = cac.put("/nodes/" + nodeName, nodeObject);
                        code = chefResponse.getStatusCode();
                        message = chefResponse.getBody();
                        if (code != STATUS_OK) {
                            break;
                        }
                    }
                } else {
                    code = KEY_NOTFOUND;
                    message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
                    doFailure(ctx, code, message);
                }
                chefServerResult(ctx, code, message);
            } else {
                throw new SvcLogicException("Missing Mandatory param(s) Node , NodeList ");
            }
        } catch (JSONException e) {
            code = APPC_ERRORCODE;
            logger.error(POSTING_REQUEST_JSON_ERROR_STR + LOG_ERR_METHOD_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + LOG_ERR_METHOD_STR + e.getMessage());
        } catch (Exception e) {
            code = APPC_ERRORCODE;
            logger.error(POSTING_REQUEST_ERROR_STR + LOG_ERR_METHOD_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + LOG_ERR_METHOD_STR + e.getMessage());
        }
    }

    @Override
    public void vnfcPushJob(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            chefInfo(params, ctx);
            String nodeList = params.get(NODE_LIST_STR);
            if (StringUtils.isNotBlank(nodeList)) {
                String isCallback = params.get("CallbackCapable");
                String chefAction = "/pushy/jobs";
                // need work on this
                String pushRequest;
                if ("true".equals(isCallback)) {
                    String requestId = params.get("RequestId");
                    String callbackUrl = params.get("CallbackUrl");
                    pushRequest = "{" + "\"command\": \"chef-client\"," + "\"run_timeout\": 300," + "\"nodes\":"
                                  + nodeList + "," + "\"env\": {\"RequestId\": \"" + requestId + "\", \"CallbackUrl\": \""
                                  + callbackUrl + "\"}," + "\"capture_output\": true" + "}";
                } else {
                    pushRequest = "{" + "\"command\": \"chef-client\"," + "\"run_timeout\": 300," + "\"nodes\":"
                                  + nodeList + "," + "\"env\": {}," + "\"capture_output\": true" + "}";
                }
                ChefApiClient cac = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);
                ChefResponse chefResponse = cac.post(chefAction, pushRequest);
                code = chefResponse.getStatusCode();
                logger.info("pushRequest:" + pushRequest);
                logger.info("requestbody: {}", chefResponse.getBody());
                String message = chefResponse.getBody();
                if (code == STATUS_PUSHJOBCHECK) {
                    int startIndex = message.indexOf("jobs") + 5;
                    int endIndex = message.length() - 2;
                    String jobID = message.substring(startIndex, endIndex);
                    ctx.setAttribute("jobID", jobID);
                    logger.info(jobID);
                }
                chefServerResult(ctx, code, message);
            } else {
                throw new SvcLogicException("Missing Mandatory param(s)  NodeList ");
            }
        } catch (Exception e) {
            code = APPC_ERRORCODE;
            logger.error(POSTING_REQUEST_ERROR_STR + "vnfcPushJob", e);
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + "vnfcPushJob" + e.getMessage());
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void fetchResults(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code = STATUS_OK;
        final String LOG_STR = "fetchResults";
        try {
            chefInfo(params, ctx);
            String nodeListS = params.get(NODE_LIST_STR);
            if (StringUtils.isNotBlank(nodeListS)) {
                nodeListS = nodeListS.replace("[", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("]", StringUtils.EMPTY);
                nodeListS = nodeListS.replace("\"", StringUtils.EMPTY);
                nodeListS = nodeListS.replace(" ", StringUtils.EMPTY);
                List<String> nodes = Arrays.asList(nodeListS.split("\\s*,\\s*"));
                JSONObject result = new JSONObject();
                String returnMessage = StringUtils.EMPTY;

                for (String node : nodes) {
                    String chefAction = "/nodes/" + node;
                    String message;
                    if (privateKeyChecker.doesExist(clientPrivatekey)) {
                        ChefResponse chefResponse = getApiMethod(chefAction);
                        code = chefResponse.getStatusCode();
                        message = chefResponse.getBody();
                    } else {
                        code = KEY_NOTFOUND;
                        message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
                        doFailure(ctx, code, message);
                    }
                    if (code == STATUS_OK) {
                        JSONObject nodeResult = new JSONObject();
                        JSONObject allNodeData = new JSONObject(message);
                        allNodeData = allNodeData.getJSONObject("normal");
                        String attribute = "PushJobOutput";

                        String resultData = allNodeData.optString(attribute, null);
                        if (resultData == null) {
                            resultData = Optional.ofNullable(allNodeData.optJSONObject(attribute))
                                    .map(p -> p.toString()).orElse(null);
                            if (resultData == null) {
                                resultData = Optional.ofNullable(allNodeData.optJSONArray(attribute))
                                        .map(p -> p.toString()).orElse(null);

                                if (resultData == null) {
                                    code = KEY_NOTFOUND;
                                    returnMessage = "Cannot find " + attribute;
                                    break;
                                }
                            }
                        }
                        nodeResult.put(attribute, resultData);
                        result.put(node, nodeResult);
                        returnMessage = result.toString();
                    } else {
                        code = KEY_NOTFOUND;
                        returnMessage = message + " Cannot access: " + node;
                        doFailure(ctx, code, message);
                        break;
                    }
                }

                chefServerResult(ctx, code, returnMessage);
            } else {
                throw new SvcLogicException("Missing Mandatory param(s)  NodeList ");
            }
        } catch (JSONException e) {
            code = APPC_ERRORCODE;
            logger.error(POSTING_REQUEST_JSON_ERROR_STR + LOG_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_JSON_ERROR_STR + LOG_STR + e.getMessage());
        } catch (Exception e) {
            code = APPC_ERRORCODE;
            logger.error(POSTING_REQUEST_ERROR_STR + LOG_STR, e);
            doFailure(ctx, code, POSTING_REQUEST_ERROR_STR + LOG_STR + e.getMessage());
        }
    }

    private ChefResponse getApiMethod(String chefAction) {
        ChefApiClient cac = chefApiClientFactory.create(chefserver, organizations, username, clientPrivatekey);
        return cac.get(chefAction);
    }

    /**
     * build node object
     */
    @SuppressWarnings("nls")
    @Override
    public void nodeObejctBuilder(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("nodeObejctBuilder");
        String name = params.get("nodeobject.name");
        String normal = params.get("nodeobject.normal");
        String overrides = params.get("nodeobject.overrides");
        String defaults = params.get("nodeobject.defaults");
        String runList = params.get("nodeobject.run_list");
        String chefEnvironment = params.get("nodeobject.chef_environment");
        String nodeObject = "{\"json_class\":\"Chef::Node\",\"default\":{" + defaults
                            + "},\"chef_type\":\"node\",\"run_list\":[" + runList + "],\"override\":{" + overrides
                            + "},\"normal\": {" + normal + "},\"automatic\":{},\"name\":\"" + name + "\",\"chef_environment\":\""
                            + chefEnvironment + "\",}";
        logger.info(nodeObject);
        ctx.setAttribute("chef.nodeObject", nodeObject);
    }

    /**
     * send get request to chef server
     */
    private void chefInfo(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        username = params.get("username");
        serverAddress = params.get("serverAddress");
        organizations = params.get("organizations");
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(serverAddress)
            && StringUtils.isNotBlank(organizations)) {
            chefserver = "https://" + serverAddress + "/organizations/" + organizations;
            clientPrivatekey = "/opt/onap/appc/chef/" + serverAddress + "/" + organizations + "/" + username + ".pem";
            logger.info(" clientPrivatekey  " + clientPrivatekey);
        } else {
            doFailure(ctx, APPC_ERRORCODE, "Missing mandatory param(s) such as username, serverAddress, organizations");
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void retrieveData(Map<String, String> params, SvcLogicContext ctx) {
        String allConfigData = params.get("allConfig");
        String key = params.get("key");
        String dgContext = params.get("dgContext");
        JSONObject jsonConfig = new JSONObject(allConfigData);
        String contextData = fetchContextData(key, jsonConfig);
        ctx.setAttribute(dgContext, contextData);
    }

    private String fetchContextData(String key, JSONObject jsonConfig) {
        try {
            return jsonConfig.getString(key);
        } catch (Exception e) {
            logger.error("Failed getting string value corresponding to " + key + ". Trying to fetch nested json object",
                    e);
            try {
                return jsonConfig.getJSONObject(key).toString();
            } catch (Exception ex) {
                logger.error("Failed getting json object corresponding to " + key + ". Trying to fetch array", ex);
                return jsonConfig.getJSONArray(key).toString();
            }
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void combineStrings(Map<String, String> params, SvcLogicContext ctx) {
        String string1 = params.get("String1");
        String string2 = params.get("String2");
        String dgContext = params.get("dgContext");
        String contextData = string1 + string2;
        ctx.setAttribute(dgContext, contextData);
    }

    /**
     * Send GET request to chef server
     */
    @SuppressWarnings("nls")

    @Override
    public void chefGet(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        logger.info("chef get method");
        chefInfo(params, ctx);
        String chefAction = params.get(CHEF_ACTION_STR);
        int code;
        String message;
        if (privateKeyChecker.doesExist(clientPrivatekey)) {
            ChefResponse chefResponse = getApiMethod(chefAction);
            code = chefResponse.getStatusCode();
            message = chefResponse.getBody();
        } else {
            code = KEY_NOTFOUND;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        chefServerResult(ctx, code, message);
    }

    /**
     * Send PUT request to chef server
     */
    @SuppressWarnings("nls")

    @Override
    public void chefPut(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        chefInfo(params, ctx);
        String chefAction = params.get(CHEF_ACTION_STR);
        String chefNodeStr = params.get("chefRequestBody");
        int code;
        String message;
        if (privateKeyChecker.doesExist(clientPrivatekey)) {
            ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username,
                    clientPrivatekey);
            ChefResponse chefResponse = chefApiClient.put(chefAction, chefNodeStr);
            code = chefResponse.getStatusCode();
            message = chefResponse.getBody();
        } else {
            code = KEY_NOTFOUND;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(ctx, code, message);
    }

    /**
     * send Post request to chef server
     */
    @Override
    public void chefPost(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        chefInfo(params, ctx);
        logger.info("chef Post method");
        logger.info(username + " " + clientPrivatekey + " " + chefserver + " " + organizations);
        String chefNodeStr = params.get("chefRequestBody");
        String chefAction = params.get(CHEF_ACTION_STR);
        int code;
        String message;
        // should load pem from somewhere else
        if (privateKeyChecker.doesExist(clientPrivatekey)) {
            ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username,
                    clientPrivatekey);
            // need pass path into it
            // "/nodes/testnode"
            ChefResponse chefResponse = chefApiClient.post(chefAction, chefNodeStr);
            code = chefResponse.getStatusCode();
            message = chefResponse.getBody();
        } else {
            code = KEY_NOTFOUND;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(ctx, code, message);
    }

    /**
     * send delete request to chef server
     */
    @Override
    public void chefDelete(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        logger.info("chef delete method");
        chefInfo(params, ctx);
        String chefAction = params.get(CHEF_ACTION_STR);
        int code;
        String message;
        if (privateKeyChecker.doesExist(clientPrivatekey)) {
            ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username,
                    clientPrivatekey);
            ChefResponse chefResponse = chefApiClient.delete(chefAction);
            code = chefResponse.getStatusCode();
            message = chefResponse.getBody();
        } else {
            code = KEY_NOTFOUND;
            message = CANNOT_FIND_PRIVATE_KEY_STR + clientPrivatekey;
        }
        logger.info(code + "   " + message);
        chefServerResult(ctx, code, message);
    }

    /**
     * Trigger target vm run chef
     */
    @Override
    public void trigger(Map<String, String> params, SvcLogicContext svcLogicContext) {
        logger.info("Run trigger method");
        String tVmIp = params.get("ip");
        try {
            ChefResponse chefResponse = chefApiClientFactory.create(tVmIp, organizations).get("");
            chefClientResult(svcLogicContext, chefResponse.getStatusCode(), chefResponse.getBody());
            svcLogicContext.setAttribute("chefAgent.code", STATUS_OK.toString());
        } catch (Exception e) {
            logger.error("An error occurred when executing trigger method", e);
            svcLogicContext.setAttribute("chefAgent.code", KEY_NOTFOUND.toString());
            svcLogicContext.setAttribute("chefAgent.message", e.toString());
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void checkPushJob(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            chefInfo(params, ctx);
            String jobID = params.get("jobid");
            String retry = params.get("retryTimes");
            String intrva = params.get("retryInterval");
            if (StringUtils.isNotBlank(jobID) && StringUtils.isNotBlank(retry) && StringUtils.isNotBlank(intrva)) {

                int retryTimes = Integer.parseInt(params.get("retryTimes"));
                int retryInterval = Integer.parseInt(params.get("retryInterval"));
                String chefAction = "/pushy/jobs/" + jobID;
                String message = StringUtils.EMPTY;
                String status = StringUtils.EMPTY;
                for (int i = 0; i < retryTimes; i++) {
                    sleepFor(retryInterval);
                    ChefResponse chefResponse = getApiMethod(chefAction);
                    code = chefResponse.getStatusCode();
                    message = chefResponse.getBody();
                    JSONObject obj = new JSONObject(message);
                    status = obj.getString("status");
                    if (!"running".equals(status)) {
                        logger.info(i + " time " + code + "   " + status);
                        break;
                    }
                }
                resolveSvcLogicAttributes(ctx, message, status);
            } else {
                throw new SvcLogicException("Missing Mandatory param(s) retryTimes , retryInterval ");
            }
        } catch (Exception e) {
            code = APPC_ERRORCODE;
            logger.error("An error occurred when executing checkPushJob method", e);
            doFailure(ctx, code, e.getMessage());
        }
    }

    private void resolveSvcLogicAttributes(SvcLogicContext svcLogic, String message, String status) {
        if ("complete".equals(status)) {
            if (hasFailedNode(message)) {
                String finalMessage = "PushJob Status Complete but check failed nodes in the message :" + message;
                svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, APPC_ERRORCODE.toString());
                svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, finalMessage);
            } else {
                svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, STATUS_OK.toString());
                svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, message);
            }
        } else if ("running".equals(status)) {
            svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, PUSHJOBSTATUS.toString());
            svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, "chef client runtime out");
        } else {
            svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, KEY_NOTFOUND.toString());
            svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, message);
        }
    }

    private Boolean hasFailedNode(String message) {
        try {
            JSONObject messageJson = new JSONObject(message);
            JSONObject node = messageJson.getJSONObject("nodes");
            final String failed = "failed";
            if (node == null) {
                logger.debug("Status Complete but node details in the message is null : " + message);
                return Boolean.TRUE;
            }
            if (node.has(failed) && !(node.isNull(failed)) && (node.getJSONArray(failed).length() != 0)) {
                logger.debug("Status Complete but one or more Failed nodes ....FAILURE " + message);
                return Boolean.TRUE;
            }
            logger.debug("Status Complete and no failed nodes ....SUCCESS " + message);
            return Boolean.FALSE;
        } catch (JSONException e) {
            logger.error("Exception occured in hasFailedNode", e);
            throw new JSONException("Exception occured in hasFailedNode" + e.getMessage());
        }

    }

    private void sleepFor(int retryInterval) {
        try {
            Thread.sleep(retryInterval); // 1000 milliseconds is one second.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void pushJob(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        int code;
        try {
            chefInfo(params, ctx);
            String pushRequest = params.get("pushRequest");
            String chefAction = "/pushy/jobs";
            ChefApiClient chefApiClient = chefApiClientFactory.create(chefserver, organizations, username,
                    clientPrivatekey);
            ChefResponse chefResponse = chefApiClient.post(chefAction, pushRequest);
            code = chefResponse.getStatusCode();
            String message = chefResponse.getBody();
            if (code == STATUS_PUSHJOBCHECK) {
                int startIndex = message.indexOf("jobs") + 6;
                int endIndex = message.length() - 2;
                String jobID = message.substring(startIndex, endIndex);
                ctx.setAttribute("jobID", jobID);
                logger.info(jobID);
            }
            chefServerResult(ctx, code, message);
        } catch (Exception e) {
            code = APPC_ERRORCODE;
            logger.error("An error occurred when executing pushJob method", e);
            doFailure(ctx, code, e.getMessage());
        }
    }

    @SuppressWarnings("static-method")
    private void chefServerResult(SvcLogicContext svcLogicContext, int code, String message) {
        initSvcLogic(svcLogicContext, code, message, "server");
    }

    @SuppressWarnings("static-method")
    private void chefClientResult(SvcLogicContext svcLogicContext, int code, String message) {
        initSvcLogic(svcLogicContext, code, message, "client");
    }

    private void initSvcLogic(SvcLogicContext svcLogicContext, int code, String message, String target) {

        String codeStr = "server".equals(target) ? CHEF_SERVER_RESULT_CODE_STR : CHEF_CLIENT_RESULT_CODE_STR;
        String messageStr = "client".equals(target) ? CHEF_CLIENT_RESULT_MSG_STR : CHEF_SERVER_RESULT_MSG_STR;
        svcLogicContext.setStatus(OUTCOME_SUCCESS);
        svcLogicContext.setAttribute(codeStr, Integer.toString(code));
        svcLogicContext.setAttribute(messageStr, message);
        logger.info(codeStr + ": " + svcLogicContext.getAttribute(codeStr));
        logger.info(messageStr + ": " + svcLogicContext.getAttribute(messageStr));
    }

    @SuppressWarnings("static-method")
    private void doFailure(SvcLogicContext svcLogic, int code, String message) throws SvcLogicException {

        String cutMessage = message.contains("\n") ? message.substring(message.indexOf('\n')) : message;
        svcLogic.setStatus(OUTCOME_FAILURE);
        svcLogic.setAttribute(CHEF_SERVER_RESULT_CODE_STR, Integer.toString(code));
        svcLogic.setAttribute(CHEF_SERVER_RESULT_MSG_STR, cutMessage);
        throw new SvcLogicException("Chef Adaptor error:" + cutMessage);
    }

}
