/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.plugins.grtoolkit.connection;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ConnectionManagerTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);

    @Test
    public void getConnectionResponseWithInput() throws Exception {
        stubFor(post(urlEqualTo("/post"))
                        .willReturn(aResponse().withStatus(200)));
        ConnectionResponse response = ConnectionManager.getConnectionResponse("http://localhost:9999/post", ConnectionManager.HttpMethod.POST, "", "creds:creds");
        assertNotNull(response);
        assertEquals(200, response.statusCode);
    }

    @Test
    public void getConnectionResponseWithCredentials() throws Exception {
        stubFor(post(urlEqualTo("/post"))
                        .willReturn(aResponse().withStatus(200)));
        ConnectionResponse response = ConnectionManager.getConnectionResponse("http://localhost:9999/post", ConnectionManager.HttpMethod.POST, "", "creds:creds");
        assertNotNull(response);
        assertEquals(200, response.statusCode);
    }

    @Test
    public void getConnectionResponse() throws Exception {
        stubFor(get(urlEqualTo("/get"))
                        .willReturn(aResponse().withStatus(200)
                        .withBody("Multi\nLine\nResponse")));
        ConnectionResponse response = ConnectionManager.getConnectionResponse("http://localhost:9999/get", ConnectionManager.HttpMethod.GET, null, null);
        assertNotNull(response);
        assertEquals(200, response.statusCode);
        assertEquals("MultiLineResponse", response.content);
    }
}