/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2022 Samsung Electronics
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


package org.onap.ccsdk.sli.adaptors.rest.impl;

import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.rest.Constants;
import org.onap.ccsdk.sli.adaptors.rest.RequestFactory;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the RequestFactory object
 * <p>
 * The request context is used to track retries, recovery attempts, and time to live of the
 * processing of a request.
 * </p>
 */

public class RequestFactoryTest {

    @Test
    public void testGetHttpRequestGet() throws Exception {
        RequestFactory httpRequest = new RequestFactory();
        HttpRequestBase get = httpRequest.getHttpRequest("get", "http://test/url.com");
        assertEquals(get.getMethod(), "GET");
        assertEquals(get.getURI().toString(), "http://test/url.com");
    }

    @Test
    public void testGetHttpRequestPost() throws Exception {
        RequestFactory httpRequest = new RequestFactory();
        HttpRequestBase get = httpRequest.getHttpRequest("post", "http://test/url.com");
        assertEquals(get.getMethod(), "POST");
        assertEquals(get.getURI().toString(), "http://test/url.com");
    }

    @Test
    public void testGetHttpRequestPut() throws Exception {
        RequestFactory httpRequest = new RequestFactory();
        HttpRequestBase get = httpRequest.getHttpRequest("put", "http://test/url.com");
        assertEquals(get.getMethod(), "PUT");
        assertEquals(get.getURI().toString(), "http://test/url.com");
    }

    @Test
    public void testGetHttpRequestDelete() throws Exception {
        RequestFactory httpRequest = new RequestFactory();
        HttpRequestBase get = httpRequest.getHttpRequest("delete", "http://test/url.com");
        assertEquals(get.getMethod(), "DELETE");
        assertEquals(get.getURI().toString(), "http://test/url.com");
    }

    @Test
    public void testConstructorNoArgument() throws Exception {
        RequestFactory httpRequest = new RequestFactory();
        HttpRequestBase get = httpRequest.getHttpRequest("get", "http://test/url.com");
        assertEquals(get.getMethod(), "GET");
        assertEquals(get.getURI().toString(), "http://test/url.com");
    }
}

