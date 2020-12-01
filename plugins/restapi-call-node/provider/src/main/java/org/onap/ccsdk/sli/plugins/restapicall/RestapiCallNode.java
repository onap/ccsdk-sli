/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
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

package org.onap.ccsdk.sli.plugins.restapicall;

import static java.lang.Boolean.valueOf;
import static javax.ws.rs.client.Entity.entity;
import static org.onap.ccsdk.sli.plugins.restapicall.AuthType.fromString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth1.ConsumerCredentials;
import org.glassfish.jersey.client.oauth1.OAuth1ClientSupport;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.logging.filter.base.HttpURLConnectionMetricUtil;
import org.onap.logging.filter.base.MetricLogClientFilter;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class RestapiCallNode implements SvcLogicJavaPlugin {

    protected static final String PARTNERS_FILE_NAME = "partners.json";
    protected static final String UEB_PROPERTIES_FILE_NAME = "ueb.properties";
    protected static final String DEFAULT_PROPERTIES_DIR = "/opt/onap/ccsdk/data/properties";
    protected static final String PROPERTIES_DIR_KEY = "SDNC_CONFIG_DIR";
    protected static final int DEFAULT_HTTP_CONNECT_TIMEOUT_MS = 30000; // 30 seconds
    protected static final int DEFAULT_HTTP_READ_TIMEOUT_MS = 600000; // 10 minutes

    private static final Logger log = LoggerFactory.getLogger(RestapiCallNode.class);
    private String uebServers;
    private String defaultUebTemplateFileName = "/opt/bvc/restapi/templates/default-ueb-message.json";

    private String responseReceivedMessage = "Response received. Time: {}";
    private String responseHttpCodeMessage = "HTTP response code: {}";
    private String requestPostingException = "Exception while posting http request to client ";
    protected static final String skipSendingMessage = "skipSending";
    protected static final String responsePrefix = "responsePrefix";
    protected static final String restapiUrlString = "restapiUrl";
    protected static final String restapiUserKey = "restapiUser";
    protected static final String restapiPasswordKey = "restapiPassword";
    protected Integer httpConnectTimeout;
    protected Integer httpReadTimeout;

    protected HashMap<String, PartnerDetails> partnerStore;
    private static final Pattern retryPattern = Pattern.compile(".*,(http|https):.*");

    public RestapiCallNode() {
        String configDir = System.getProperty(PROPERTIES_DIR_KEY, DEFAULT_PROPERTIES_DIR);
        try {
            String jsonString = readFile(configDir + "/" + PARTNERS_FILE_NAME);
            JSONObject partners = new JSONObject(jsonString);
            partnerStore = new HashMap<>();
            loadPartners(partners);
            log.info("Partners support enabled");
        } catch (Exception e) {
            log.warn("Partners file could not be read, Partner support will not be enabled. " + e.getMessage());
        }

        try (FileInputStream in = new FileInputStream(configDir + "/" + UEB_PROPERTIES_FILE_NAME)) {
            Properties props = new Properties();
            props.load(in);
            uebServers = props.getProperty("servers");
            log.info("UEB support enabled");
        } catch (Exception e) {
            log.warn("UEB properties could not be read, UEB support will not be enabled. " + e.getMessage());
        }
        httpConnectTimeout = readOptionalInteger("HTTP_CONNECT_TIMEOUT_MS",DEFAULT_HTTP_CONNECT_TIMEOUT_MS);
        httpReadTimeout = readOptionalInteger("HTTP_READ_TIMEOUT_MS",DEFAULT_HTTP_READ_TIMEOUT_MS);
    }

    @SuppressWarnings("unchecked")
    protected void loadPartners(JSONObject partners) {
        Iterator<String> keys = partners.keys();
        String partnerUserKey = "user";
        String partnerPasswordKey = "password";
        String partnerUrlKey = "url";

        while (keys.hasNext()) {
            String partnerKey = keys.next();
            try {
                JSONObject partnerObject = (JSONObject) partners.get(partnerKey);
                if (partnerObject.has(partnerUserKey) && partnerObject.has(partnerPasswordKey)) {
                    String url = null;
                    if (partnerObject.has(partnerUrlKey)) {
                        url = partnerObject.getString(partnerUrlKey);
                    }
                    String userName = partnerObject.getString(partnerUserKey);
                    String password = partnerObject.getString(partnerPasswordKey);
                    PartnerDetails details = new PartnerDetails(userName, getObfuscatedVal(password), url);
                    partnerStore.put(partnerKey, details);
                    log.info("mapped partner using partner key " + partnerKey);
                } else {
                    log.info("Partner " + partnerKey + " is missing required keys, it won't be mapped");
                }
            } catch (JSONException e) {
                log.info("Couldn't map the partner using partner key " + partnerKey, e);
            }
        }
    }

    /* Unobfuscate param value */
    private static String getObfuscatedVal(String paramValue) {
        String resValue = paramValue;
        if (paramValue != null && paramValue.startsWith("${") && paramValue.endsWith("}"))
        {
            String paramStr = paramValue.substring(2, paramValue.length()-1);
            if (paramStr  != null && paramStr.length() > 0)
            {
                String val = System.getenv(paramStr);
                if (val != null && val.length() > 0)
                {
                    resValue=val;
                    log.info("Obfuscated value RESET for param value:" + paramValue);
                }
            }
        }
        return resValue;
    }

    /**
     * Returns parameters from the parameter map.
     *
     * @param paramMap parameter map
     * @param p parameters instance
     * @return parameters filed instance
     * @throws SvcLogicException when svc logic exception occurs
     */
    public static Parameters getParameters(Map<String, String> paramMap, Parameters p) throws SvcLogicException {

        p.templateFileName = parseParam(paramMap, "templateFileName", false, null);
        p.requestBody = parseParam(paramMap, "requestBody", false, null);
        p.restapiUrl = parseParam(paramMap, restapiUrlString, true, null);
        p.restapiUrlSuffix = parseParam(paramMap, "restapiUrlSuffix", false, null);
        if (p.restapiUrlSuffix != null) {
            p.restapiUrl = p.restapiUrl + p.restapiUrlSuffix;
        }

        p.restapiUrl = UriBuilder.fromUri(p.restapiUrl).toTemplate();
        validateUrl(p.restapiUrl);

        p.restapiUser = parseParam(paramMap, restapiUserKey, false, null);
        p.restapiPassword = parseParam(paramMap, restapiPasswordKey, false, null);
        p.oAuthConsumerKey = parseParam(paramMap, "oAuthConsumerKey", false, null);
        p.oAuthConsumerSecret = parseParam(paramMap, "oAuthConsumerSecret", false, null);
        p.oAuthSignatureMethod = parseParam(paramMap, "oAuthSignatureMethod", false, null);
        p.oAuthVersion = parseParam(paramMap, "oAuthVersion", false, null);
        p.contentType = parseParam(paramMap, "contentType", false, null);
        p.format = Format.fromString(parseParam(paramMap, "format", false, "json"));
        p.authtype = fromString(parseParam(paramMap, "authType", false, "unspecified"));
        p.httpMethod = HttpMethod.fromString(parseParam(paramMap, "httpMethod", false, "post"));
        p.responsePrefix = parseParam(paramMap, responsePrefix, false, null);
        p.listNameList = getListNameList(paramMap);
        String skipSendingStr = paramMap.get(skipSendingMessage);
        p.skipSending = "true".equalsIgnoreCase(skipSendingStr);
        p.convertResponse = valueOf(parseParam(paramMap, "convertResponse", false, "true"));
        p.keyStoreFileName = parseParam(paramMap, "keyStoreFileName", false, null);
        p.keyStorePassword = parseParam(paramMap, "keyStorePassword", false, null);
        p.ssl = p.keyStoreFileName != null && p.keyStorePassword != null;
        p.customHttpHeaders = parseParam(paramMap, "customHttpHeaders", false, null);
        p.partner = parseParam(paramMap, "partner", false, null);
        p.dumpHeaders = valueOf(parseParam(paramMap, "dumpHeaders", false, null));
        p.returnRequestPayload = valueOf(parseParam(paramMap, "returnRequestPayload", false, null));
        p.accept = parseParam(paramMap, "accept", false, null);
        p.multipartFormData = valueOf(parseParam(paramMap, "multipartFormData", false, "false"));
        p.multipartFile = parseParam(paramMap, "multipartFile", false, null);
        p.targetEntity = parseParam(paramMap, "targetEntity", false, null);
        return p;
    }

    /**
     * Validates the given URL in the parameters.
     *
     * @param restapiUrl rest api URL
     * @throws SvcLogicException when URL validation fails
     */
    private static void validateUrl(String restapiUrl) throws SvcLogicException {
        if (containsMultipleUrls(restapiUrl)) {
            String[] urls = getMultipleUrls(restapiUrl);
            for (String url : urls) {
                validateUrl(url);
            }
        } else {
            try {
                URI.create(restapiUrl);
            } catch (IllegalArgumentException e) {
                throw new SvcLogicException("Invalid input of url " + e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Returns the list of list name.
     *
     * @param paramMap parameters map
     * @return list of list name
     */
    private static Set<String> getListNameList(Map<String, String> paramMap) {
        Set<String> ll = new HashSet<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (entry.getKey().startsWith("listName")) {
                ll.add(entry.getValue());
            }
        }
        return ll;
    }

    /**
     * Parses the parameter string map of property, validates if required, assigns default value if
     * present and returns the value.
     *
     * @param paramMap string param map
     * @param name name of the property
     * @param required if value required
     * @param def default value
     * @return value of the property
     * @throws SvcLogicException if required parameter value is empty
     */
    public static String parseParam(Map<String, String> paramMap, String name, boolean required, String def)
        throws SvcLogicException {
        String s = paramMap.get(name);

        if (s == null || s.trim().length() == 0) {
            if (!required) {
                return def;
            }
            throw new SvcLogicException("Parameter " + name + " is required in RestapiCallNode");
        }

        s = s.trim();
        StringBuilder value = new StringBuilder();
        int i = 0;
        int i1 = s.indexOf('%');
        while (i1 >= 0) {
            int i2 = s.indexOf('%', i1 + 1);
            if (i2 < 0) {
                break;
            }

            String varName = s.substring(i1 + 1, i2);
            String varValue = System.getenv(varName);
            if (varValue == null) {
                varValue = "%" + varName + "%";
            }

            value.append(s.substring(i, i1));
            value.append(varValue);

            i = i2 + 1;
            i1 = s.indexOf('%', i);
        }
        value.append(s.substring(i));

        log.info("Parameter {}: [{}]", name, maskPassword(name, value));

        return value.toString();
    }

    private static Object maskPassword(String name, Object value) {
        String[] pwdNames = {"pwd", "passwd", "password", "Pwd", "Passwd", "Password"};
        for (String pwdName : pwdNames) {
            if (name.contains(pwdName)) {
                return "**********";
            }
        }
        return value;
    }

    /**
     * Allows Directed Graphs the ability to interact with REST APIs.
     *
     * @param paramMap HashMap<String,String> of parameters passed by the DG to this function
     *        <table border="1">
     *        <thead>
     *        <th>parameter</th>
     *        <th>Mandatory/Optional</th>
     *        <th>description</th>
     *        <th>example values</th></thead> <tbody>
     *        <tr>
     *        <td>templateFileName</td>
     *        <td>Optional</td>
     *        <td>full path to template file that can be used to build a request</td>
     *        <td>/sdncopt/bvc/restapi/templates/vnf_service-configuration-operation_minimal.json</td>
     *        </tr>
     *        <tr>
     *        <td>restapiUrl</td>
     *        <td>Mandatory</td>
     *        <td>url to send the request to</td>
     *        <td>https://sdncodl:8543/restconf/operations/L3VNF-API:create-update-vnf-request</td>
     *        </tr>
     *        <tr>
     *        <td>restapiUser</td>
     *        <td>Optional</td>
     *        <td>user name to use for http basic authentication</td>
     *        <td>sdnc_ws</td>
     *        </tr>
     *        <tr>
     *        <td>restapiPassword</td>
     *        <td>Optional</td>
     *        <td>unencrypted password to use for http basic authentication</td>
     *        <td>plain_password</td>
     *        </tr>
     *        <tr>
     *        <td>oAuthConsumerKey</td>
     *        <td>Optional</td>
     *        <td>Consumer key to use for http oAuth authentication</td>
     *        <td>plain_key</td>
     *        </tr>
     *        <tr>
     *        <td>oAuthConsumerSecret</td>
     *        <td>Optional</td>
     *        <td>Consumer secret to use for http oAuth authentication</td>
     *        <td>plain_secret</td>
     *        </tr>
     *        <tr>
     *        <td>oAuthSignatureMethod</td>
     *        <td>Optional</td>
     *        <td>Consumer method to use for http oAuth authentication</td>
     *        <td>method</td>
     *        </tr>
     *        <tr>
     *        <td>oAuthVersion</td>
     *        <td>Optional</td>
     *        <td>Version http oAuth authentication</td>
     *        <td>version</td>
     *        </tr>
     *        <tr>
     *        <td>contentType</td>
     *        <td>Optional</td>
     *        <td>http content type to set in the http header</td>
     *        <td>usually application/json or application/xml</td>
     *        </tr>
     *        <tr>
     *        <td>format</td>
     *        <td>Optional</td>
     *        <td>should match request body format</td>
     *        <td>json or xml</td>
     *        </tr>
     *        <tr>
     *        <td>httpMethod</td>
     *        <td>Optional</td>
     *        <td>http method to use when sending the request</td>
     *        <td>get post put delete patch</td>
     *        </tr>
     *        <tr>
     *        <td>responsePrefix</td>
     *        <td>Optional</td>
     *        <td>location the response will be written to in context memory</td>
     *        <td>tmp.restapi.result</td>
     *        </tr>
     *        <tr>
     *        <td>listName[i]</td>
     *        <td>Optional</td>
     *        <td>Used for processing XML responses with repeating
     *        elements.</td>vpn-information.vrf-details
     *        <td></td>
     *        </tr>
     *        <tr>
     *        <td>skipSending</td>
     *        <td>Optional</td>
     *        <td></td>
     *        <td>true or false</td>
     *        </tr>
     *        <tr>
     *        <td>convertResponse</td>
     *        <td>Optional</td>
     *        <td>whether the response should be converted</td>
     *        <td>true or false</td>
     *        </tr>
     *        <tr>
     *        <td>customHttpHeaders</td>
     *        <td>Optional</td>
     *        <td>a list additional http headers to be passed in, follow the format in the example</td>
     *        <td>X-CSI-MessageId=messageId,headerFieldName=headerFieldValue</td>
     *        </tr>
     *        <tr>
     *        <td>dumpHeaders</td>
     *        <td>Optional</td>
     *        <td>when true writes http header content to context memory</td>
     *        <td>true or false</td>
     *        </tr>
     *        <tr>
     *        <td>partner</td>
     *        <td>Optional</td>
     *        <td>used to retrieve username, password and url if partner store exists</td>
     *        <td>aaf</td>
     *        </tr>
     *        <tr>
     *        <td>returnRequestPayload</td>
     *        <td>Optional</td>
     *        <td>used to return payload built in the request</td>
     *        <td>true or false</td>
     *        </tr>
     *        </tbody>
     *        </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     * @see String#split(String, int)
     */
    public void sendRequest(Map<String, String> paramMap, SvcLogicContext ctx) throws SvcLogicException {
        sendRequest(paramMap, ctx, null);
    }

    protected void sendRequest(Map<String, String> paramMap, SvcLogicContext ctx, RetryPolicy retryPolicy)
        throws SvcLogicException {

    	HttpResponse r = new HttpResponse();
        try {
            handlePartner(paramMap);
            Parameters p = getParameters(paramMap, new Parameters());
            if(p.targetEntity != null && !p.targetEntity.isEmpty()) {
                MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, p.targetEntity);
            }
            if (containsMultipleUrls(p.restapiUrl) && retryPolicy == null) {
                String[] urls = getMultipleUrls(p.restapiUrl);
                retryPolicy = new RetryPolicy(urls, urls.length * 2);
                p.restapiUrl = urls[0];
            }
            String pp = p.responsePrefix != null ? p.responsePrefix + '.' : "";

            String req = null;
            if (p.templateFileName != null) {
                String reqTemplate = readFile(p.templateFileName);
                req = buildXmlJsonRequest(ctx, reqTemplate, p.format);
            } else if (p.requestBody != null) {
                req = p.requestBody;
            }
            r = sendHttpRequest(req, p);
            setResponseStatus(ctx, p.responsePrefix, r);

            if (p.dumpHeaders && r.headers != null) {
                for (Entry<String, List<String>> a : r.headers.entrySet()) {
                    ctx.setAttribute(pp + "header." + a.getKey(), StringUtils.join(a.getValue(), ","));
                }
            }

            if (p.returnRequestPayload && req != null) {
                ctx.setAttribute(pp + "httpRequest", req);
            }

            if (r.body != null && r.body.trim().length() > 0) {
                ctx.setAttribute(pp + "httpResponse", r.body);

                if (p.convertResponse) {
                    Map<String, String> mm = null;
                    if (p.format == Format.XML) {
                        mm = XmlParser.convertToProperties(r.body, p.listNameList);
                    } else if (p.format == Format.JSON) {
                        mm = JsonParser.convertToProperties(r.body);
                    }

                    if (mm != null) {
                        for (Map.Entry<String, String> entry : mm.entrySet()) {
                            ctx.setAttribute(pp + entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        } catch (SvcLogicException e) {
            boolean shouldRetry = false;
            if (e.getCause().getCause() instanceof SocketException) {
                shouldRetry = true;
            }

            log.error("Error sending the request: " + e.getMessage(), e);
            String prefix = parseParam(paramMap, responsePrefix, false, null);
            if (retryPolicy == null || !shouldRetry) {
                setFailureResponseStatus(ctx, prefix, e.getMessage(), r);
            } else {
                log.debug(retryPolicy.getRetryMessage());
                try {
                    // calling getNextHostName increments the retry count so it should be called before shouldRetry
                    String retryString = retryPolicy.getNextHostName();
                    if (retryPolicy.shouldRetry()) {
                        paramMap.put(restapiUrlString, retryString);
                        log.debug("retry attempt {} will use the retry url {}", retryPolicy.getRetryCount(),
                            retryString);
                        sendRequest(paramMap, ctx, retryPolicy);
                    } else {
                        log.debug("Maximum retries reached, won't attempt to retry. Calling setFailureResponseStatus.");
                        setFailureResponseStatus(ctx, prefix, e.getMessage(), r);
                    }
                } catch (Exception ex) {
                    String retryErrorMessage = "Retry attempt " + retryPolicy.getRetryCount()
                        + "has failed with error message " + ex.getMessage();
                    setFailureResponseStatus(ctx, prefix, retryErrorMessage, r);
                }
            }
        }

        if (r != null && r.code >= 300) {
            throw new SvcLogicException(String.valueOf(r.code) + ": " + r.message);
        }
    }

    protected void handlePartner(Map<String, String> paramMap) {
        String partner = paramMap.get("partner");
        if (partner != null && partner.length() > 0) {
            PartnerDetails details = partnerStore.get(partner);
            paramMap.put(restapiUserKey, details.username);
            paramMap.put(restapiPasswordKey, details.password);
            if (paramMap.get(restapiUrlString) == null) {
                paramMap.put(restapiUrlString, details.url);
            }
        }
    }

    protected String buildXmlJsonRequest(SvcLogicContext ctx, String template, Format format) throws SvcLogicException {
        log.info("Building {} started", format);
        long t1 = System.currentTimeMillis();
        String originalTemplate = template;

        template = expandRepeats(ctx, template, 1);

        Map<String, String> mm = new HashMap<>();
        for (String s : ctx.getAttributeKeySet()) {
            mm.put(s, ctx.getAttribute(s));
        }

        StringBuilder ss = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            int i1 = template.indexOf("${", i);
            if (i1 < 0) {
                ss.append(template.substring(i));
                break;
            }

            int i2 = template.indexOf('}', i1 + 2);
            if (i2 < 0) {
                throw new SvcLogicException("Template error: Matching } not found");
            }

            String var1 = template.substring(i1 + 2, i2);
            String value1 = format == Format.XML ? XmlJsonUtil.getXml(mm, var1) : XmlJsonUtil.getJson(mm, var1);
            if (value1 == null || value1.trim().length() == 0) {
                // delete the whole element (line)
                int i3 = template.lastIndexOf('\n', i1);
                if (i3 < 0) {
                    i3 = 0;
                }
                int i4 = template.indexOf('\n', i1);
                if (i4 < 0) {
                    i4 = template.length();
                }

                if (i < i3) {
                    ss.append(template.substring(i, i3));
                }
                i = i4;
            } else {
                ss.append(template.substring(i, i1)).append(value1);
                i = i2 + 1;
            }
        }

        String req = format == Format.XML ? XmlJsonUtil.removeEmptyStructXml(ss.toString())
            : XmlJsonUtil.removeEmptyStructJson(originalTemplate, ss.toString());

        if (format == Format.JSON) {
            req = XmlJsonUtil.removeLastCommaJson(req);
        }

        long t2 = System.currentTimeMillis();
        log.info("Building {} completed. Time: {}", format, t2 - t1);

        return req;
    }

    protected String expandRepeats(SvcLogicContext ctx, String template, int level) throws SvcLogicException {
        StringBuilder newTemplate = new StringBuilder();
        int k = 0;
        while (k < template.length()) {
            int i1 = template.indexOf("${repeat:", k);
            if (i1 < 0) {
                newTemplate.append(template.substring(k));
                break;
            }

            int i2 = template.indexOf(':', i1 + 9);
            if (i2 < 0) {
                throw new SvcLogicException(
                    "Template error: Context variable name followed by : is required after repeat");
            }

            // Find the closing }, store in i3
            int nn = 1;
            int i3 = -1;
            int i = i2;
            while (nn > 0 && i < template.length()) {
                i3 = template.indexOf('}', i);
                if (i3 < 0) {
                    throw new SvcLogicException("Template error: Matching } not found");
                }
                int i32 = template.indexOf('{', i);
                if (i32 >= 0 && i32 < i3) {
                    nn++;
                    i = i32 + 1;
                } else {
                    nn--;
                    i = i3 + 1;
                }
            }

            String var1 = template.substring(i1 + 9, i2);
            String value1 = ctx.getAttribute(var1);
            log.info("     {}:{}", var1, value1);
            int n = 0;
            try {
                n = Integer.parseInt(value1);
            } catch (NumberFormatException e) {
                log.info("value1 not set or not a number, n will remain set at zero");
            }

            newTemplate.append(template.substring(k, i1));

            String rpt = template.substring(i2 + 1, i3);

            for (int ii = 0; ii < n; ii++) {
                String ss = rpt.replaceAll("\\[\\$\\{" + level + "\\}\\]", "[" + ii + "]");
                if (ii == n - 1 && ss.trim().endsWith(",")) {
                    int i4 = ss.lastIndexOf(',');
                    if (i4 > 0) {
                        ss = ss.substring(0, i4) + ss.substring(i4 + 1);
                    }
                }
                newTemplate.append(ss);
            }

            k = i3 + 1;
        }

        if (k == 0) {
            return newTemplate.toString();
        }

        return expandRepeats(ctx, newTemplate.toString(), level + 1);
    }

    protected String readFile(String fileName) throws SvcLogicException {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            return new String(encoded, "UTF-8");
        } catch (IOException | SecurityException e) {
            throw new SvcLogicException("Unable to read file " + fileName + e.getLocalizedMessage(), e);
        }
    }

    protected Client addAuthType(Client c, FileParam fp) throws SvcLogicException {
        Parameters p = new Parameters();
        p.restapiUser = fp.user;
        p.restapiPassword = fp.password;
        p.oAuthConsumerKey = fp.oAuthConsumerKey;
        p.oAuthVersion = fp.oAuthVersion;
        p.oAuthConsumerSecret = fp.oAuthConsumerSecret;
        p.oAuthSignatureMethod = fp.oAuthSignatureMethod;
        p.authtype = fp.authtype;
        return addAuthType(c, p);
    }

    public Client addAuthType(Client client, Parameters p) throws SvcLogicException {
        if (p.authtype == AuthType.Unspecified) {
            if (p.restapiUser != null && p.restapiPassword != null) {
                client.register(HttpAuthenticationFeature.basic(p.restapiUser, p.restapiPassword));
            } else if (p.oAuthConsumerKey != null && p.oAuthConsumerSecret != null && p.oAuthSignatureMethod != null) {
                Feature oAuth1Feature =
                    OAuth1ClientSupport.builder(new ConsumerCredentials(p.oAuthConsumerKey, p.oAuthConsumerSecret))
                        .version(p.oAuthVersion).signatureMethod(p.oAuthSignatureMethod).feature().build();
                client.register(oAuth1Feature);

            }
        } else {
            if (p.authtype == AuthType.DIGEST) {
                if (p.restapiUser != null && p.restapiPassword != null) {
                    client.register(HttpAuthenticationFeature.digest(p.restapiUser, p.restapiPassword));
                } else {
                    throw new SvcLogicException(
                        "oAUTH authentication type selected but all restapiUser and restapiPassword "
                            + "parameters doesn't exist",
                        new Throwable());
                }
            } else if (p.authtype == AuthType.BASIC) {
                if (p.restapiUser != null && p.restapiPassword != null) {
                    client.register(HttpAuthenticationFeature.basic(p.restapiUser, p.restapiPassword));
                } else {
                    throw new SvcLogicException(
                        "oAUTH authentication type selected but all restapiUser and restapiPassword "
                            + "parameters doesn't exist",
                        new Throwable());
                }
            } else if (p.authtype == AuthType.OAUTH) {
                if (p.oAuthConsumerKey != null && p.oAuthConsumerSecret != null && p.oAuthSignatureMethod != null) {
                    Feature oAuth1Feature = OAuth1ClientSupport
                        .builder(new ConsumerCredentials(p.oAuthConsumerKey, p.oAuthConsumerSecret))
                        .version(p.oAuthVersion).signatureMethod(p.oAuthSignatureMethod).feature().build();
                    client.register(oAuth1Feature);
                } else {
                    throw new SvcLogicException(
                        "oAUTH authentication type selected but all oAuthConsumerKey, oAuthConsumerSecret "
                            + "and oAuthSignatureMethod parameters doesn't exist",
                        new Throwable());
                }
            }
        }
        return client;
    }

    /**
     * Receives the http response for the http request sent.
     *
     * @param request request msg
     * @param p parameters
     * @return HTTP response
     * @throws SvcLogicException when sending http request fails
     */
    public HttpResponse sendHttpRequest(String request, Parameters p) throws SvcLogicException {

        SSLContext ssl = null;
        if (p.ssl && p.restapiUrl.startsWith("https")) {
            ssl = createSSLContext(p);
        }
        Client client;
        if (ssl != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
            client = ClientBuilder.newBuilder().sslContext(ssl).hostnameVerifier((s, sslSession) -> true).build();
        } else {
            client = ClientBuilder.newBuilder().hostnameVerifier((s, sslSession) -> true).build();
        }

        setClientTimeouts(client);
        // Needed to support additional HTTP methods such as PATCH
        client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
        client.register(new MetricLogClientFilter());
        WebTarget webTarget = addAuthType(client, p).target(p.restapiUrl);

        long t1 = System.currentTimeMillis();

        HttpResponse r = new HttpResponse();
        r.code = 200;
        String accept = p.accept;
        if (accept == null) {
            accept = p.format == Format.XML ? "application/xml" : "application/json";
        }

        String contentType = p.contentType;
        if (contentType == null) {
            contentType = accept + ";charset=UTF-8";
        }

        if (!p.skipSending && !p.multipartFormData) {

            Invocation.Builder invocationBuilder = webTarget.request(contentType).accept(accept);

            if (p.format == Format.NONE) {
                invocationBuilder.header("", "");
            }

            if (p.customHttpHeaders != null && p.customHttpHeaders.length() > 0) {
                String[] keyValuePairs = p.customHttpHeaders.split(",");
                for (String singlePair : keyValuePairs) {
                    int equalPosition = singlePair.indexOf('=');
                    invocationBuilder.header(singlePair.substring(0, equalPosition),
                        singlePair.substring(equalPosition + 1, singlePair.length()));
                }
            }

            invocationBuilder.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);

            Response response;

            try {
                // When the HTTP operation has no body do not set the content-type
                //setting content-type has caused errors with some servers when no body is present
                if (request == null) {
                    response = invocationBuilder.method(p.httpMethod.toString());
                } else {
                    log.info("Sending request below to url " + p.restapiUrl);
                    log.info(request);
                    response = invocationBuilder.method(p.httpMethod.toString(), entity(request, contentType));
                }
            } catch (ProcessingException | IllegalStateException e) {
                throw new SvcLogicException(requestPostingException + e.getLocalizedMessage(), e);
            }

            r.code = response.getStatus();
            r.headers = response.getStringHeaders();
            EntityTag etag = response.getEntityTag();
            if (etag != null) {
                r.message = etag.getValue();
            }
            if (response.hasEntity() && r.code != 204) {
                r.body = response.readEntity(String.class);
            }
        } else if (!p.skipSending && p.multipartFormData) {

            WebTarget wt = client.register(MultiPartFeature.class).target(p.restapiUrl);

            MultiPart multiPart = new MultiPart();
            multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

            FileDataBodyPart fileDataBodyPart =
                new FileDataBodyPart("file", new File(p.multipartFile), MediaType.APPLICATION_OCTET_STREAM_TYPE);
            multiPart.bodyPart(fileDataBodyPart);


            Invocation.Builder invocationBuilder = wt.request(contentType).accept(accept);

            if (p.format == Format.NONE) {
                invocationBuilder.header("", "");
            }

            if (p.customHttpHeaders != null && p.customHttpHeaders.length() > 0) {
                String[] keyValuePairs = p.customHttpHeaders.split(",");
                for (String singlePair : keyValuePairs) {
                    int equalPosition = singlePair.indexOf('=');
                    invocationBuilder.header(singlePair.substring(0, equalPosition),
                        singlePair.substring(equalPosition + 1, singlePair.length()));
                }
            }

            Response response;

            try {
                response =
                    invocationBuilder.method(p.httpMethod.toString(), entity(multiPart, multiPart.getMediaType()));
            } catch (ProcessingException | IllegalStateException e) {
                throw new SvcLogicException(requestPostingException + e.getLocalizedMessage(), e);
            }

            r.code = response.getStatus();
            r.headers = response.getStringHeaders();
            EntityTag etag = response.getEntityTag();
            if (etag != null) {
                r.message = etag.getValue();
            }
            if (response.hasEntity() && r.code != 204) {
                r.body = response.readEntity(String.class);
            }

        }

        long t2 = System.currentTimeMillis();
        log.info(responseReceivedMessage, t2 - t1);
        log.info(responseHttpCodeMessage, r.code);
        log.info("HTTP response message: {}", r.message);
        logHeaders(r.headers);
        log.info("HTTP response: {}", r.body);

        return r;
    }

    protected SSLContext createSSLContext(Parameters p) {
        try (FileInputStream in = new FileInputStream(p.keyStoreFileName)) {
            HttpsURLConnection.setDefaultHostnameVerifier((string, ssls) -> true);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance("PKCS12");
            char[] pwd = p.keyStorePassword.toCharArray();
            ks.load(in, pwd);
            kmf.init(ks, pwd);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), null, null);
            return ctx;
        } catch (Exception e) {
            log.error("Error creating SSLContext: {}", e.getMessage(), e);
        }
        return null;
    }

    protected void setFailureResponseStatus(SvcLogicContext ctx, String prefix, String errorMessage,
        HttpResponse resp) {
        resp.code = 500;
        resp.message = errorMessage;
        String pp = prefix != null ? prefix + '.' : "";
        ctx.setAttribute(pp + "response-code", String.valueOf(resp.code));
        ctx.setAttribute(pp + "response-message", resp.message);
    }

    protected void setResponseStatus(SvcLogicContext ctx, String prefix, HttpResponse r) {
        String pp = prefix != null ? prefix + '.' : "";
        ctx.setAttribute(pp + "response-code", String.valueOf(r.code));
        ctx.setAttribute(pp + "response-message", r.message);
    }

    public void sendFile(Map<String, String> paramMap, SvcLogicContext ctx) throws SvcLogicException {
        HttpResponse r = null;
        try {
            FileParam p = getFileParameters(paramMap);
            byte[] data = Files.readAllBytes(Paths.get(p.fileName));

            r = sendHttpData(data, p);

            for (int i = 0; i < 10 && r.code == 301; i++) {
                String newUrl = r.headers2.get("Location").get(0);

                log.info("Got response code 301. Sending same request to URL: " + newUrl);

                p.url = newUrl;
                r = sendHttpData(data, p);
            }

            setResponseStatus(ctx, p.responsePrefix, r);

        } catch (SvcLogicException | IOException e) {
            log.error("Error sending the request: {}", e.getMessage(), e);

            r = new HttpResponse();
            r.code = 500;
            r.message = e.getMessage();
            String prefix = parseParam(paramMap, responsePrefix, false, null);
            setResponseStatus(ctx, prefix, r);
        }

        if (r != null && r.code >= 300) {
            throw new SvcLogicException(String.valueOf(r.code) + ": " + r.message);
        }
    }

    private FileParam getFileParameters(Map<String, String> paramMap) throws SvcLogicException {
        FileParam p = new FileParam();
        p.fileName = parseParam(paramMap, "fileName", true, null);
        p.url = parseParam(paramMap, "url", true, null);
        p.user = parseParam(paramMap, "user", false, null);
        p.password = parseParam(paramMap, "password", false, null);
        p.httpMethod = HttpMethod.fromString(parseParam(paramMap, "httpMethod", false, "post"));
        p.responsePrefix = parseParam(paramMap, responsePrefix, false, null);
        String skipSendingStr = paramMap.get(skipSendingMessage);
        p.skipSending = "true".equalsIgnoreCase(skipSendingStr);
        p.oAuthConsumerKey = parseParam(paramMap, "oAuthConsumerKey", false, null);
        p.oAuthVersion = parseParam(paramMap, "oAuthVersion", false, null);
        p.oAuthConsumerSecret = parseParam(paramMap, "oAuthConsumerSecret", false, null);
        p.oAuthSignatureMethod = parseParam(paramMap, "oAuthSignatureMethod", false, null);
        p.authtype = fromString(parseParam(paramMap, "authType", false, "unspecified"));
        return p;
    }

    public void postMessageOnUeb(Map<String, String> paramMap, SvcLogicContext ctx) throws SvcLogicException {
        HttpResponse r;
        try {
            UebParam p = getUebParameters(paramMap);

            String pp = p.responsePrefix != null ? p.responsePrefix + '.' : "";

            String req;

            if (p.templateFileName == null) {
                log.info("No template file name specified. Using default UEB template: {}", defaultUebTemplateFileName);
                p.templateFileName = defaultUebTemplateFileName;
            }

            String reqTemplate = readFile(p.templateFileName);
            reqTemplate = reqTemplate.replaceAll("rootVarName", p.rootVarName);
            req = buildXmlJsonRequest(ctx, reqTemplate, Format.JSON);

            r = postOnUeb(req, p);
            setResponseStatus(ctx, p.responsePrefix, r);
            if (r.body != null) {
                ctx.setAttribute(pp + "httpResponse", r.body);
            }

        } catch (SvcLogicException e) {
            log.error("Error sending the request: {}", e.getMessage(), e);

            r = new HttpResponse();
            r.code = 500;
            r.message = e.getMessage();
            String prefix = parseParam(paramMap, responsePrefix, false, null);
            setResponseStatus(ctx, prefix, r);
        }

        if (r.code >= 300) {
            throw new SvcLogicException(String.valueOf(r.code) + ": " + r.message);
        }
    }

    protected HttpResponse sendHttpData(byte[] data, FileParam p) throws IOException {
        URL url = new URL(p.url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        log.info("Connection: " + con.getClass().getName());

        con.setRequestMethod(p.httpMethod.toString());
        con.setRequestProperty("Content-Type", "application/octet-stream");
        con.setRequestProperty("Accept", "*/*");
        con.setRequestProperty("Expect", "100-continue");
        con.setFixedLengthStreamingMode(data.length);
        con.setInstanceFollowRedirects(false);

        if (p.user != null && p.password != null) {
            String authString = p.user + ":" + p.password;
            String authStringEnc = Base64.getEncoder().encodeToString(authString.getBytes());
            con.setRequestProperty("Authorization", "Basic " + authStringEnc);
        }

        con.setDoInput(true);
        con.setDoOutput(true);

        log.info("Sending file");
        long t1 = System.currentTimeMillis();

        HttpResponse r = new HttpResponse();
        r.code = 200;

        if (!p.skipSending) {
            HttpURLConnectionMetricUtil util = new HttpURLConnectionMetricUtil();
            util.logBefore(con, ONAPComponents.DMAAP);

            con.connect();

            boolean continue100failed = false;
            try {
                OutputStream os = con.getOutputStream();
                os.write(data);
                os.flush();
                os.close();
            } catch (ProtocolException e) {
                continue100failed = true;
            }

            r.code = con.getResponseCode();
            r.headers2 = con.getHeaderFields();

            if (r.code != 204 && !continue100failed) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                r.body = response.toString();
            }

            util.logAfter(con);

            con.disconnect();
        }

        long t2 = System.currentTimeMillis();
        log.info("Response received. Time: {}", t2 - t1);
        log.info("HTTP response code: {}", r.code);
        log.info("HTTP response message: {}", r.message);
        logHeaders(r.headers2);
        log.info("HTTP response: {}", r.body);

        return r;
    }

    private UebParam getUebParameters(Map<String, String> paramMap) throws SvcLogicException {
        UebParam p = new UebParam();
        p.topic = parseParam(paramMap, "topic", true, null);
        p.templateFileName = parseParam(paramMap, "templateFileName", false, null);
        p.rootVarName = parseParam(paramMap, "rootVarName", false, null);
        p.responsePrefix = parseParam(paramMap, responsePrefix, false, null);
        String skipSendingStr = paramMap.get(skipSendingMessage);
        p.skipSending = "true".equalsIgnoreCase(skipSendingStr);
        return p;
    }

    protected void logProperties(Map<String, Object> mm) {
        List<String> ll = new ArrayList<>();
        for (Object o : mm.keySet()) {
            ll.add((String) o);
        }
        Collections.sort(ll);

        log.info("Properties:");
        for (String name : ll) {
            log.info("--- {}:{}", name, String.valueOf(mm.get(name)));
        }
    }

    protected void logHeaders(MultivaluedMap<String, String> mm) {
        log.info("HTTP response headers:");

        if (mm == null) {
            return;
        }

        List<String> ll = new ArrayList<>();
        for (Object o : mm.keySet()) {
            ll.add((String) o);
        }
        Collections.sort(ll);

        for (String name : ll) {
            log.info("--- {}:{}", name, String.valueOf(mm.get(name)));
        }
    }

    private void logHeaders(Map<String, List<String>> mm) {
        if (mm == null || mm.isEmpty()) {
            return;
        }

        List<String> ll = new ArrayList<>();
        for (String s : mm.keySet()) {
            if (s != null) {
                ll.add(s);
            }
        }
        Collections.sort(ll);

        for (String name : ll) {
            List<String> v = mm.get(name);
            log.info("--- {}:{}", name, String.valueOf(mm.get(name)));
            log.info("--- " + name + ": " + (v.size() == 1 ? v.get(0) : v));
        }
    }

    protected HttpResponse postOnUeb(String request, UebParam p) throws SvcLogicException {
        String[] urls = uebServers.split(" ");
        for (int i = 0; i < urls.length; i++) {
            if (!urls[i].endsWith("/")) {
                urls[i] += "/";
            }
            urls[i] += "events/" + p.topic;
        }

        Client client = ClientBuilder.newBuilder().build();
        setClientTimeouts(client);
        WebTarget webTarget = client.target(urls[0]);

        log.info("UEB URL: {}", urls[0]);
        log.info("Sending request:");
        log.info(request);
        long t1 = System.currentTimeMillis();

        HttpResponse r = new HttpResponse();
        r.code = 200;

        if (!p.skipSending) {
            String tt = "application/json";
            String tt1 = tt + ";charset=UTF-8";

            Response response;
            Invocation.Builder invocationBuilder = webTarget.request(tt1).accept(tt);

            try {
                response = invocationBuilder.post(Entity.entity(request, tt1));
            } catch (ProcessingException e) {
                throw new SvcLogicException(requestPostingException + e.getLocalizedMessage(), e);
            }
            r.code = response.getStatus();
            r.headers = response.getStringHeaders();
            if (response.hasEntity()) {
                r.body = response.readEntity(String.class);
            }
        }

        long t2 = System.currentTimeMillis();
        log.info(responseReceivedMessage, t2 - t1);
        log.info(responseHttpCodeMessage, r.code);
        logHeaders(r.headers);
        log.info("HTTP response:\n {}", r.body);

        return r;
    }

    public void setUebServers(String uebServers) {
        this.uebServers = uebServers;
    }

    public void setDefaultUebTemplateFileName(String defaultUebTemplateFileName) {
        this.defaultUebTemplateFileName = defaultUebTemplateFileName;
    }

    protected void setClientTimeouts(Client client) {
        client.property(ClientProperties.CONNECT_TIMEOUT, httpConnectTimeout);
        client.property(ClientProperties.READ_TIMEOUT, httpReadTimeout);
    }

    protected Integer readOptionalInteger(String propertyName, Integer defaultValue) {
        String stringValue = System.getProperty(propertyName);
        if (stringValue != null && stringValue.length() > 0) {
            try {
                return Integer.valueOf(stringValue);
            } catch (NumberFormatException e) {
                log.warn("property " + propertyName + " had the value " + stringValue + " that could not be converted to an Integer, default " + defaultValue + " will be used instead", e);
            }
        }
        return defaultValue;
    }

    protected static String[] getMultipleUrls(String restapiUrl) {
        List<String> urls = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < restapiUrl.length(); i++) {
            if (restapiUrl.charAt(i) == ',') {
                if (i + 9 < restapiUrl.length()) {
                    String part = restapiUrl.substring(i + 1, i + 9);
                    if (part.equals("https://") || part.startsWith("http://")) {
                        urls.add(restapiUrl.substring(start, i));
                        start = i + 1;
                    }
                }
            } else if (i == restapiUrl.length() - 1) {
                urls.add(restapiUrl.substring(start, i + 1));
            }
        }
        String[] arr = new String[urls.size()];
        return urls.toArray(arr);
    }

    protected static boolean containsMultipleUrls(String restapiUrl) {
        Matcher m = retryPattern.matcher(restapiUrl);
        return m.matches();
    }

    private static class FileParam {

        public String fileName;
        public String url;
        public String user;
        public String password;
        public HttpMethod httpMethod;
        public String responsePrefix;
        public boolean skipSending;
        public String oAuthConsumerKey;
        public String oAuthConsumerSecret;
        public String oAuthSignatureMethod;
        public String oAuthVersion;
        public AuthType authtype;
    }

    private static class UebParam {

        public String topic;
        public String templateFileName;
        public String rootVarName;
        public String responsePrefix;
        public boolean skipSending;
    }
}
