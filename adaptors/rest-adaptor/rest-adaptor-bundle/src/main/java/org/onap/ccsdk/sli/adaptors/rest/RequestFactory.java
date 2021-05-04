/*
 * ============LICENSE_START=============================================================================================================
 * Copyright (c) 2017 Intel Corp.  All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 * ============LICENSE_END===============================================================================================================
 *
 */

package org.onap.ccsdk.sli.adaptors.rest;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

public class RequestFactory {
    static final Map<String, Supplier<HttpRequestBase>> map = new HashMap<>();

    static {
        map.put("GET", HttpGet :: new);
        map.put("POST", HttpPost :: new);
        map.put("PUT", HttpPut :: new);
        map.put("DELETE", HttpDelete :: new);
    }

    private final EELFLogger logger = EELFManager.getInstance().getLogger(RequestFactory.class);

    public HttpRequestBase getHttpRequest(String method, String tUrl) {
        Supplier<HttpRequestBase> httpRequestSupplier = map.get(method.toUpperCase());
        URI uri = null;
        if (httpRequestSupplier != null) {
            try {
                uri = new URIBuilder(tUrl).build();
            } catch (URISyntaxException ex) {
                logger.error("URI Syntax Incorrect: " + tUrl, ex);
            }
            HttpRequestBase httpRequest = httpRequestSupplier.get();
            httpRequest.setURI(uri);
            return httpRequest;

        }
        throw new IllegalArgumentException("No method named: " + method.toUpperCase());
    }

}
