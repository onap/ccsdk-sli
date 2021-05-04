/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 Samsung
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.rest.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.json.JSONObject;
import org.onap.ccsdk.sli.adaptors.rest.Constants;
import org.onap.ccsdk.sli.adaptors.rest.RequestFactory;
import org.onap.ccsdk.sli.adaptors.rest.RestAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

/**
 * This class implements the {@link RestAdaptor} interface. This interface defines the behaviors that our service
 * provides.
 */
public class RestAdaptorImpl implements RestAdaptor {

    /**
     * The constant for the status code for a failed outcome
     */
    @SuppressWarnings("nls")
    private static final String OUTCOME_FAILURE = "failure";

    /**
     * The constant for the status code for a successful outcome
     */
    @SuppressWarnings("nls")
    private static final String OUTCOME_SUCCESS = "success";

    /**
     * The logger to be used
     */
    private final EELFLogger logger = EELFManager.getInstance().getLogger(RestAdaptorImpl.class);

    /**
     * A reference to the adaptor configuration object.
     */
    private Configuration configuration;

    /**
     * This default constructor is used as a work around because the activator wasnt getting called
     */
    public RestAdaptorImpl() {
        initialize();
    }

    /**
     * Returns the symbolic name of the adaptor
     *
     * @return The adaptor name
     *
     * @see RestAdaptor#getAdaptorName()
     */
    @Override
    public String getAdaptorName() {
        return configuration.getProperty(Constants.PROPERTY_ADAPTER_NAME);
    }

    @Override
    public void commonGet(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run get method");

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        HttpGet httpGet = (HttpGet) createHttpRequest("GET", params, rc);
        executeHttpRequest(httpGet, rc);
    }

    @Override
    public void commonDelete(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run Delete method");

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        HttpDelete httpDelete = (HttpDelete) createHttpRequest("DELETE", params, rc);
        executeHttpRequest(httpDelete, rc);
    }

    @Override
    public void commonPost(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run post method");

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        HttpPost httpPost = (HttpPost) createHttpRequest("POST", params, rc);
        executeHttpRequest(httpPost, rc);
    }

    @Override
    public void commonPut(Map<String, String> params, SvcLogicContext ctx) {
        logger.info("Run put method");

        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        HttpPut httpPut = (HttpPut) createHttpRequest("PUT", params, rc);
        executeHttpRequest(httpPut, rc);
    }

    @SuppressWarnings("static-method")
    private void doFailure(RequestContext rc, HttpStatus code, String message) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        String msg = (message == null) ? code.getReasonPhrase() : message;
        if (msg.contains("\n")) {
            msg = msg.substring(msg.indexOf('\n'));
        }

        String status;
        try {
            status = Integer.toString(code.getStatusCode());
        } catch (Exception e) {
            logger.error("Exception occurred", e);
            status = "500";
        }
        svcLogic.setStatus(OUTCOME_FAILURE);
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_CODE, status);
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
        svcLogic.setAttribute(Constants.CONTEXT_ERROR_CODE, status);
        svcLogic.setAttribute(Constants.CONTEXT_ERROR_MESSAGE, msg);
    }

    /**
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     */
    @SuppressWarnings("static-method")
    private void doSuccess(RequestContext rc, int code, String message) {
        SvcLogicContext svcLogic = rc.getSvcLogicContext();
        svcLogic.setStatus(OUTCOME_SUCCESS);
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_CODE, Integer.toString(HttpStatus.OK_200.getStatusCode()));
        svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, message);
        svcLogic.setAttribute(Constants.CONTEXT_AGENT_ERROR_CODE, Integer.toString(code));
        svcLogic.setAttribute(Constants.CONTEXT_AGENT_ERROR_MESSAGE, message);
        svcLogic.setAttribute(Constants.CONTEXT_ERROR_CODE, Integer.toString(HttpStatus.OK_200.getStatusCode()));
    }

    private void executeHttpRequest(HttpRequestBase httpRequest, RequestContext rc) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpResponse response = httpClient.execute(httpRequest);
            int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseOutput = EntityUtils.toString(entity);
            if (responseCode == 200) {
                doSuccess(rc, responseCode, responseOutput);
            } else {
                doFailure(rc, HttpStatus.getHttpStatus(responseCode), response.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
            logger.error("An error occurred when executing http request", e);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, e.toString());
        }
    }

    public HttpRequestBase createHttpRequest(String method, Map<String, String> params, RequestContext rc) {
        HttpRequestBase httpRequest = null;
        try {
            String tUrl = params.get("org.onap.appc.instance.URI");
            String haveHeader = params.get("org.onap.appc.instance.haveHeader");
            String headers = params.get("org.onap.appc.instance.headers");

            Supplier<RequestFactory> requestFactory = RequestFactory :: new;
            httpRequest = requestFactory.get().getHttpRequest(method, tUrl);

            if ("true".equals(haveHeader)) {
                JSONObject jsonHeaders = new JSONObject(headers);
                Iterator keys = jsonHeaders.keys();
                while (keys.hasNext()) {
                    String string1 = (String) keys.next();
                    String string2 = jsonHeaders.getString(string1);
                    httpRequest.addHeader(string1, string2);
                }
            }
            if (params.containsKey("org.onap.appc.instance.requestBody")) {
                String body = params.get("org.onap.appc.instance.requestBody");
                StringEntity bodyParams = new StringEntity(body, "UTF-8");
                if ("PUT".equals(method)) {
                    HttpPut httpPut = (HttpPut) httpRequest;
                    httpPut.setEntity(bodyParams);
                }
                if ("POST".equals(method)) {
                    HttpPost httpPost = (HttpPost) httpRequest;
                    httpPost.setEntity(bodyParams);
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred when creating http request", e);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, e.toString());
        }
        return httpRequest;
    }

    /**
     * initialize the provider adaptor by building the context cache
     */
    private void initialize() {
        configuration = ConfigurationFactory.getConfiguration();

        logger.info("Rest adaptor has been initialized");
    }

}
