/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

final class ChefRequestBuilder {

    private ChefRequestBuilder() {
    }

    static OngoingRequestBuilder newRequestTo(String endPoint) {
        return new OngoingRequestBuilder(endPoint);
    }

    static class OngoingRequestBuilder {

        private HttpRequestBase httpRequestBase;
        private String endPoint;
        private String path;
        private ImmutableMap<String, String> headers;

        private OngoingRequestBuilder(String endPoint) {
            this.endPoint = endPoint;
        }

        public OngoingRequestBuilder withPath(String path) {
            this.path = path;
            return this;
        }

        public OngoingRequestBuilder httpGet() {
            httpRequestBase = new HttpGet();
            return this;
        }

        public OngoingRequestBuilder httpDelete() {
            httpRequestBase = new HttpDelete();
            return this;
        }

        public OngoingRequestBuilder httpPost(String body) {
            HttpPost httpPost = new HttpPost();
            httpPost.setEntity(toEntity(body));
            httpRequestBase = httpPost;
            return this;
        }

        public OngoingRequestBuilder httpPut(String body) {
            HttpPut httpPut = new HttpPut();
            httpPut.setEntity(toEntity(body));
            httpRequestBase = httpPut;
            return this;
        }

        private StringEntity toEntity(String body) {
            StringEntity stringEntity = new StringEntity(body, "UTF-8");
            stringEntity.setContentType("application/json");
            return stringEntity;
        }

        public OngoingRequestBuilder withHeaders(ImmutableMap<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public HttpRequestBase build() throws URISyntaxException {
            setRequestUri();
            setRequestHeaders();
            return httpRequestBase;
        }

        private void setRequestUri() throws URISyntaxException {
            URI fullPath = new URIBuilder(endPoint).setPath(path).build();
            httpRequestBase.setURI(fullPath);
        }

        private void setRequestHeaders() {
            for (Entry<String, String> entry : headers.entrySet()) {
                httpRequestBase.addHeader(entry.getKey(), entry.getValue());
            }
        }

    }

}
