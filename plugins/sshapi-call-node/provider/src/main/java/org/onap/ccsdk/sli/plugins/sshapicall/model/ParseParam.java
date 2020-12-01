/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics. All rights
 * 			reserved.
 * ================================================================================
 * Modifications Copyright © 2018 IBM
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

package org.onap.ccsdk.sli.plugins.sshapicall.model;

import com.google.common.base.Strings;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ParseParam {
    private static final Logger log = LoggerFactory.getLogger(ParseParam.class);
    /**
     * Default SSH command timeout
     */
    private long dEF_timeout = 12000;
    /**
     * Default SSH connection port.
     */
    private int dEF_port = 22;
    /**
     * Default SSH command timeout
     */
    private final String FILE_PARAMETERS_OPT_KEY = "FileParameters";
    public final String SSH_ResponsePrefix = "ResponsePrefix";
    /**
     * Default SSH connection port.
     */
    private final String ENV_PARAMETERS_OPT_KEY = "EnvParameters";
    private Parameters p = new Parameters();

    public Parameters getParameters(Map<String, String> paramMap) throws SvcLogicException {
        p.sshapiUrl = parseParam(paramMap, "Url", true, null);
        p.sshapiPort = Integer.parseInt(parseParam(paramMap, "Port", true, Integer.toString(dEF_port)));
        p.sshapiUser = parseParam(paramMap, "User", false, null);
        p.sshapiPassword = parseParam(paramMap, "Password", false, null);
        p.sshKey = parseParam(paramMap, "SshKey", false, null);
        p.sshExecTimeout = Long.parseLong(parseParam(paramMap, "ExecTimeout", false, Long.toString(dEF_timeout)));
        p.sshWithRetry = Boolean.valueOf(parseParam(paramMap, "Retry", false, "false"));
        p.cmd = parseParam(paramMap, "Cmd", true, null);
        p.responsePrefix = parseParam(paramMap, SSH_ResponsePrefix, false, null);
        p.responseType = Format.fromString(parseParam(paramMap, "ResponseType", false, "none"));
        p.listNameList = getListNameList(paramMap);
        p.convertResponse = Boolean.valueOf(parseParam(paramMap, "ConvertResponse", false, "true"));
        p.authtype = AuthType.fromString(parseParam(paramMap, "AuthType", false, "unspecified"));

        return p;
    }

    public String getStringParameters(Map<String, String> paramMap, String paramName) throws SvcLogicException {
        return parseParam(paramMap, SSH_ResponsePrefix, false, null);
    }

    public void parseOutput (SvcLogicContext ctx, String outMessage) throws SvcLogicException {
        if (p.convertResponse) {
            if (p.responseType == Format.NONE) {
                classifyOutput(ctx, outMessage);
            } else if (p.responseType == Format.JSON) {
                classifyOutput(ctx, outMessage);
            } else if (p.responseType == Format.XML) {
                classifyOutput(ctx, outMessage, p.listNameList);
            }
        }
    }

    private void classifyOutput(SvcLogicContext ctx, String outMessage, Set<String> listNameList) throws SvcLogicException {
        Map<String, String> mm = XmlParser.convertToProperties(outMessage, p.listNameList);
        toCtx (ctx, mm);
    }

    private void toCtx (SvcLogicContext ctx, Map<String, String> mm) {
        if (mm != null) {
            for (Map.Entry<String, String> entry : mm.entrySet()) {
                if (p.responsePrefix != null) {
                    ctx.setAttribute(p.responsePrefix + "." + entry.getKey(), entry.getValue());
                    log.info("+++ " + p.responsePrefix + "." + entry.getKey() + ": [" + entry.getValue() + "]");
                } else {
                    ctx.setAttribute(entry.getKey(), entry.getValue());
                    log.info("+++ " + entry.getKey() + ": [" + entry.getValue() + "]");
                }
            }
        }
    }

    private void classifyOutput(SvcLogicContext ctx, String outMessage) throws SvcLogicException {
        try {
            Map<String, String> mm = JsonParser.convertToProperties(outMessage);
            toCtx (ctx, mm);
        } catch (org.codehaus.jettison.json.JSONException e) {
            log.info("Output not in JSON format");
            putToProperties(ctx, p.responsePrefix);
        } catch (Exception e) {
            throw  new SvcLogicException("error parsing response file");
        }
    }

    private void putToProperties(SvcLogicContext ctx, String outMessage) throws SvcLogicException {

        try {
            Properties prop = new Properties();
            prop.load(new ByteArrayInputStream(outMessage.getBytes(StandardCharsets.UTF_8)));
            for (Object key : prop.keySet()) {
                String name = (String) key;
                String value = prop.getProperty(name);
                if (value != null && value.trim().length() > 0) {
                    if (p.responsePrefix != null) {
                        ctx.setAttribute(p.responsePrefix + "." + name, value.trim());
                        log.info("+++ " + p.responsePrefix + "." + name + ": [" + value + "]");
                    } else {
                        ctx.setAttribute(name, value.trim());
                        log.info("+++ " + name + ": [" + value + "]");
                    }
                }
            }
        } catch (Exception e) {
            throw  new SvcLogicException( "Error parsing response file.");
        }
    }

    public String makeCommand (Map<String, String> params) {
        JSONObject jsonPayload = new JSONObject();
        final String[] optionalTestParams = {ENV_PARAMETERS_OPT_KEY, FILE_PARAMETERS_OPT_KEY};
        parseParam(params, optionalTestParams, jsonPayload);
        JSONObject envParams = (JSONObject) jsonPayload.remove(ENV_PARAMETERS_OPT_KEY);
        JSONObject fileParams = (JSONObject) jsonPayload.remove(FILE_PARAMETERS_OPT_KEY);

        StringBuilder constructedCommand = new StringBuilder();
        constructedCommand.append(parseFileParam(fileParams)).append(p.cmd).append(" ").append(parseEnvParam(envParams));

        return constructedCommand.toString();
    }

    private String parseEnvParam(JSONObject envParams) {
        StringBuilder envParamBuilder = new StringBuilder();
        if (envParams != null) {
            for (Object key : envParams.keySet()) {
                if (envParamBuilder.length() > 0) {
                    envParamBuilder.append(", ");
                }
                envParamBuilder.append(key + "=" + envParams.get((String) key));
            }
        }
        return envParamBuilder.toString();
    }

    private String parseFileParam(JSONObject fileParams) {
        StringBuilder fileParamBuilder = new StringBuilder();
        if (fileParams != null) {
            for (Object key : fileParams.keySet()) {
                fileParamBuilder.append("echo -e \"" + fileParams.get((String) key) + "\" > /srv/salt/" + key).append("; ");
            }
        }
        return fileParamBuilder.toString();
    }

    private void parseParam(Map<String, String> params, String[] optionalTestParams, JSONObject jsonPayload)
            throws JSONException {

        Set<String> optionalParamsSet = new HashSet<>();
        Collections.addAll(optionalParamsSet, optionalTestParams);

        //@formatter:off
        params.entrySet()
                .stream()
                .filter(entry -> optionalParamsSet.contains(entry.getKey()))
                .filter(entry -> !Strings.isNullOrEmpty(entry.getValue()))
                .forEach(entry -> parseParam(entry, jsonPayload));
        //@formatter:on
    }

    private void parseParam(Map.Entry<String, String> params, JSONObject jsonPayload)
            throws JSONException {
        String key = params.getKey();
        String payload = params.getValue();

        switch (key) {
            case ENV_PARAMETERS_OPT_KEY:
                JSONObject paramsJson = new JSONObject(payload);
                jsonPayload.put(key, paramsJson);
                break;

            case FILE_PARAMETERS_OPT_KEY:
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

    private Set<String> getListNameList(Map<String, String> paramMap) {
        Set<String> ll = new HashSet<>();
        for (Map.Entry<String,String> entry : paramMap.entrySet())
            if (entry.getKey().startsWith("listName"))
                ll.add(entry.getValue());
        return ll;
    }

    private String parseParam(Map<String, String> paramMap, String name, boolean required, String def)
            throws SvcLogicException {
        String s = paramMap.get(name);

        if (s == null || s.trim().length() == 0) {
            if (!required)
                return def;
            throw new SvcLogicException("Parameter " + name + " is required in sshapiCallNode");
        }

        s = s.trim();
        StringBuilder value = new StringBuilder();
        int i = 0;
        int i1 = s.indexOf('%');
        while (i1 >= 0) {
            int i2 = s.indexOf('%', i1 + 1);
            if (i2 < 0)
                break;

            String varName = s.substring(i1 + 1, i2);
            String varValue = System.getenv(varName);
            if (varValue == null)
                varValue = "%" + varName + "%";

            value.append(s.substring(i, i1));
            value.append(varValue);

            i = i2 + 1;
            i1 = s.indexOf('%', i);
        }
        value.append(s.substring(i));

        log.info("Parameter {}: [{}]", name, value);
        return value.toString();
    }
}
