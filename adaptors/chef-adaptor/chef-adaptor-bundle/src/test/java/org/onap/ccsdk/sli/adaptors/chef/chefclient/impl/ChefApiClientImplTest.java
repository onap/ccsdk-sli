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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.ChefApiClientFactory;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefApiClient;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ChefApiClientImplTest {

    private static final String END_POINT = "https://chefServer";
    private static final String ORGANIZATIONS_PATH = "onap";
    private static final String USER_ID = "testUser";
    private static final String REQUEST_PATH = "/test/path";
    private static final String BODY = "SOME BODY STRING";
    private static final String PEM_FILEPATH = "path/to/pemFile";
    private static final ImmutableMap<String, String> HEADERS = ImmutableMap.<String, String>builder()
            .put("Content-type", "application/json")
            .put("Accept", "application/json")
            .put("X-Ops-Timestamp", "1970-01-15T06:56:07Z")
            .put("X-Ops-UserId", USER_ID)
            .put("X-Chef-Version", "12.4.1")
            .put("X-Ops-Content-Hash", BODY)
            .put("X-Ops-Sign", "version=1.0").build();

    @Mock
    private HttpClient httpClient;
    @Mock
    private org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.ChefApiHeaderFactory chefHttpHeaderFactory;

    @InjectMocks
    private ChefApiClientFactory chefApiClientFactory;
    private ChefApiClient chefApiClient;

    @Before
    public void setUp() {
        chefApiClient = chefApiClientFactory.create(
                END_POINT,
                ORGANIZATIONS_PATH,
                USER_ID,
                PEM_FILEPATH);
    }

    @Test
    public void execute_HttpGet_shouldReturnResponseObject_whenRequestIsSuccessful() throws IOException {
        // GIVEN
        String methodName = "GET";
        String body = "";
        Supplier<ChefResponse> chefClientApiCall = () -> chefApiClient.get(REQUEST_PATH);

        // WHEN //THEN
        assertChefApiClientCall(methodName, body, chefClientApiCall);
    }

    @Test
    public void execute_HttpDelete_shouldReturnResponseObject_whenRequestIsSuccessful() throws IOException {
        // GIVEN
        String methodName = "DELETE";
        String body = "";
        Supplier<ChefResponse> chefClientApiCall = () -> chefApiClient.delete(REQUEST_PATH);

        // WHEN //THEN
        assertChefApiClientCall(methodName, body, chefClientApiCall);
    }

    @Test
    public void execute_HttpPost_shouldReturnResponseObject_whenRequestIsSuccessful() throws IOException {
        // GIVEN
        String methodName = "POST";
        Supplier<ChefResponse> chefClientApiCall = () -> chefApiClient.post(REQUEST_PATH, BODY);

        // WHEN //THEN
        assertChefApiClientCall(methodName, BODY, chefClientApiCall);
    }

    @Test
    public void execute_HttpPut_shouldReturnResponseObject_whenRequestIsSuccessful() throws IOException {
        // GIVEN
        String methodName = "PUT";
        Supplier<ChefResponse> chefClientApiCall = () -> chefApiClient.put(REQUEST_PATH, BODY);

        // WHEN //THEN
        assertChefApiClientCall(methodName, BODY, chefClientApiCall);
    }

    private void assertChefApiClientCall(String methodName, String body, Supplier<ChefResponse> httpMethod)
            throws IOException {
        // GIVEN
        given(chefHttpHeaderFactory.create(methodName, REQUEST_PATH, body, USER_ID, ORGANIZATIONS_PATH, PEM_FILEPATH))
                .willReturn(HEADERS);

        StatusLine statusLine = mock(StatusLine.class);
        given(statusLine.getStatusCode()).willReturn(HttpStatus.SC_OK);
        HttpResponse httpResponse = mock(HttpResponse.class);
        given(httpResponse.getStatusLine()).willReturn(statusLine);
        given(httpResponse.getEntity()).willReturn(new StringEntity("Successful Response String"));
        given(httpClient.execute(argThat(new HttpRequestBaseMatcher(methodName))))
                .willReturn(httpResponse);

        // WHEN
        ChefResponse chefResponse = httpMethod.get();

        // THEN
        assertEquals("Successful Response String", chefResponse.getBody());
        assertEquals(HttpStatus.SC_OK, chefResponse.getStatusCode());
    }

    @Test
    public void execute_shouldHandleException_whenHttpClientExecutionFails() throws IOException {

        // GIVEN
        given(chefHttpHeaderFactory.create("GET", REQUEST_PATH, "", USER_ID, ORGANIZATIONS_PATH, PEM_FILEPATH))
                .willReturn(HEADERS);

        String expectedErrorMsg = "HttpClient call failed";
        given(httpClient.execute(argThat(new HttpRequestBaseMatcher("GET"))))
                .willThrow(new IOException(expectedErrorMsg));

        // WHEN
        ChefResponse chefResponse = chefApiClient.get(REQUEST_PATH);

        // THEN
        assertEquals(expectedErrorMsg, chefResponse.getBody());
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, chefResponse.getStatusCode());
    }

    @Test
    public void execute_shouldHandleException_whenEndpointURIisMalformed() {
        // GIVEN
        String expectedErrorMsg = "Malformed escape pair at index 1: /%#@/";

        // WHEN
        ChefApiClient chefApiClient = chefApiClientFactory.create(
                "/%#@/",
                ORGANIZATIONS_PATH,
                USER_ID,
                PEM_FILEPATH);
        ChefResponse chefResponse = chefApiClient.get(REQUEST_PATH);

        // THEN
        assertEquals(expectedErrorMsg, chefResponse.getBody());
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, chefResponse.getStatusCode());
    }

    private class HttpRequestBaseMatcher extends ArgumentMatcher<HttpRequestBase> {

        private final String methodName;

        public HttpRequestBaseMatcher(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public boolean matches(Object arguments) {
            HttpRequestBase httpRequestBase = (HttpRequestBase) arguments;
            try {
                return methodName.equals(httpRequestBase.getMethod())
                   && new URI(END_POINT + "/organizations/" + ORGANIZATIONS_PATH + REQUEST_PATH).equals(httpRequestBase.getURI())
                   && checkIfBodyMatches(httpRequestBase)
                   && checkIfHeadersMatch(httpRequestBase);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return false;
            }
        }

        public boolean checkIfBodyMatches(HttpRequestBase httpRequestBase) {
            if (httpRequestBase instanceof HttpEntityEnclosingRequestBase) {
                HttpEntityEnclosingRequestBase requestBaseWithBody = (HttpEntityEnclosingRequestBase) httpRequestBase;
                StringEntity stringEntity = new StringEntity(BODY, "UTF-8");
                stringEntity.setContentType("application/json");
                return stringEntity.toString().equals(requestBaseWithBody.getEntity().toString());
            }
            return true;
        }

        private boolean checkIfHeadersMatch(HttpRequestBase httpRequestBase) {
            Header[] generatedHeaders = httpRequestBase.getAllHeaders();
            return generatedHeaders.length > 0
               && generatedHeaders.length == HEADERS.size()
               && HEADERS.entrySet().stream().allMatch(p -> httpRequestBase.getFirstHeader(p.getKey()).getValue().equals(p.getValue()));
        }

    }

}
