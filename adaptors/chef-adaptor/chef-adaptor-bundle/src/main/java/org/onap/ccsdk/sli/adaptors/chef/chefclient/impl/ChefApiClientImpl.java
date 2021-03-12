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

package org.onap.ccsdk.sli.adaptors.chef.chefclient.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefApiClient;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.ChefRequestBuilder.OngoingRequestBuilder;

public class ChefApiClientImpl implements ChefApiClient {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ChefApiClientImpl.class);
    private final HttpClient httpClient;
    private final String endpoint;
    private final String organization;
    private final org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.HttpHeaderFactory httpHeaderFactory;

    public ChefApiClientImpl(HttpClient httpClient, String endpoint, String organization,
                             org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.HttpHeaderFactory httpHeaderFactory) {
        this.httpClient = httpClient;
        this.endpoint = endpoint;
        this.organization = organization;
        this.httpHeaderFactory = httpHeaderFactory;
    }

    @Override
    public ChefResponse get(String path) {
        OngoingRequestBuilder requestBuilder = org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.ChefRequestBuilder.newRequestTo(endpoint)
                .httpGet()
                .withPath(getPath(path))
                .withHeaders(httpHeaderFactory.create("GET", path, ""));
        return execute(requestBuilder);
    }

    @Override
    public ChefResponse delete(String path) {
        OngoingRequestBuilder requestBuilder = org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.ChefRequestBuilder.newRequestTo(endpoint)
                .httpDelete()
                .withPath(getPath(path))
                .withHeaders(httpHeaderFactory.create("DELETE", path, ""));
        return execute(requestBuilder);
    }

    @Override
    public ChefResponse post(String path, String body) {
        OngoingRequestBuilder requestBuilder = org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.ChefRequestBuilder.newRequestTo(endpoint)
                .httpPost(body)
                .withPath(getPath(path))
                .withHeaders(httpHeaderFactory.create("POST", path, body));
        return execute(requestBuilder);
    }

    @Override
    public ChefResponse put(String path, String body) {
        OngoingRequestBuilder requestBuilder = org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.ChefRequestBuilder.newRequestTo(endpoint)
                .httpPut(body)
                .withPath(getPath(path))
                .withHeaders(httpHeaderFactory.create("PUT", path, body));
        logger.info("request: PATH: " + path + " body: " + body);
        return execute(requestBuilder);
    }

    private ChefResponse execute(OngoingRequestBuilder chefRequest) {
        try {
            if (httpClient == null) {
                return ChefResponse.create(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Could not create http client for chef");
            }
            HttpResponse response = httpClient.execute(chefRequest.build());
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity httpEntity = response.getEntity();
            String responseBody = EntityUtils.toString(httpEntity);
            return ChefResponse.create(statusCode, responseBody);
        } catch (IOException ex) {
            logger.error("Error occured while reading response", ex.getMessage(), ex);
            return ChefResponse.create(HttpStatus.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (URISyntaxException ex) {
            logger.error("Malformed URL", ex.getMessage(), ex);
            return ChefResponse.create(HttpStatus.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private String getPath(String path) {
        return "/organizations/" + organization + path;
    }

}

