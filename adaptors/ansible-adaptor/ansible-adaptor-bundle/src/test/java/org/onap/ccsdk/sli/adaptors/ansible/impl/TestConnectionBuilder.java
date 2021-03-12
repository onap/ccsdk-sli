/*-
 * ============LICENSE_START=======================================================
 * ONAP : SLI
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ansible.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdaptorPropertiesProviderImpl;
import org.onap.ccsdk.sli.adaptors.ansible.impl.ConnectionBuilder;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResultCodes;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptorConstants.*;

@RunWith(MockitoJUnitRunner.class)
public class TestConnectionBuilder {

    private static String KEYSTORE_FILE;
    private static String KEYSTORE_PSWD;
    private static String KEYSTORE_CERTIFICATE;
    private static String USER;
    private static String PSWD;
    private static String URL;

    private final int SUCCESS_STATUS = 200;
    private ConnectionBuilder connectionBuilder;

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private HttpClientContext httpClientContext;

    @Mock
    private CloseableHttpResponse response;

    @Mock
    private HttpEntity entity;

    @Mock
    private StatusLine statusLine;

    /**
     * Load the configuration properties
     */
    @BeforeClass
    public static void once() {
        final String configFilePath = "src/test/resources/properties/ansible-adaptor-test.properties".replace("/", File.separator);
        Properties properties = new AnsibleAdaptorPropertiesProviderImpl(configFilePath).getProperties();

        KEYSTORE_FILE = properties.getProperty(TRUSTSTORE_PROPERTY_NAME);
        KEYSTORE_PSWD = properties.getProperty(TRUSTSTORE_PASS_PROPERTY_NAME);
        KEYSTORE_CERTIFICATE = properties.getProperty("org.onap.appc.adaptor.ansible.cert");
        USER = properties.getProperty("org.onap.appc.adaptor.ansible.username");
        PSWD = properties.getProperty("org.onap.appc.adaptor.ansible.password");
        URL = properties.getProperty("org.onap.appc.adaptor.ansible.identity");
    }

    @Before
    public void setup() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        connectionBuilder = new ConnectionBuilder(1, 2000);
        Whitebox.setInternalState(connectionBuilder, "httpClient", httpClient);
        Whitebox.setInternalState(connectionBuilder, "httpContext", httpClientContext);
        HttpResponse httpResponse = response;
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(SUCCESS_STATUS);
    }

    @After
    public void tearDown() {
        connectionBuilder = null;
    }

    @Test
    public void testConnectionBuilder() throws KeyManagementException, KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException {
        char[] trustStorePassword = KEYSTORE_PSWD.toCharArray();
        ConnectionBuilder connectionBuilder = new ConnectionBuilder(KEYSTORE_FILE, trustStorePassword, 600000, "");
        assertNotNull(connectionBuilder);
    }

    @Test
    public void testConnectionBuilderWithFilePath() throws KeyManagementException, KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException {
        new ConnectionBuilder(KEYSTORE_CERTIFICATE, 600000);
    }

    @Test
    public void testSetHttpContext() {
        ConnectionBuilder spyConnectionBuilder = Mockito.spy(connectionBuilder);
        spyConnectionBuilder.setHttpContext(USER, PSWD);
        verify(spyConnectionBuilder, times(1)).setHttpContext(USER, PSWD);
    }

    @Test
    public void testPost() throws IOException {
        when(httpClient.execute(anyObject(), eq(httpClientContext))).thenReturn(response);
        AnsibleResult result = connectionBuilder.post(URL, "appc");
        assertNull(result.getStatusMessage());
        assertEquals(SUCCESS_STATUS, result.getStatusCode());
        assertEquals("UNKNOWN", result.getResults());
    }

    @Test
    public void testPostWithException() throws IOException {
        when(httpClient.execute(anyObject(), eq(httpClientContext))).thenThrow(new IOException());
        AnsibleResult result = connectionBuilder.post(URL, "appc");
        assertEquals(AnsibleResultCodes.IO_EXCEPTION.getValue(), result.getStatusCode());
    }

    @Ignore
    @Test
    public void testGet() throws IOException {
        when(httpClient.execute(anyObject(), eq(httpClientContext))).thenReturn(response);
        AnsibleResult result = connectionBuilder.get(URL);
        assertNull(result.getStatusMessage());
        assertEquals(SUCCESS_STATUS, result.getStatusCode());
        assertEquals("UNKNOWN", result.getResults());
    }

    @Test
    public void testGetWithException() throws IOException {
        when(httpClient.execute(anyObject(), eq(httpClientContext))).thenThrow(new IOException());
        AnsibleResult result = connectionBuilder.get(URL);
        assertEquals(AnsibleResultCodes.IO_EXCEPTION.getValue(), result.getStatusCode());
    }

    @Test
    public void testClose() {
        connectionBuilder.close();
    }

    @Test
    public void testGetMode() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        connectionBuilder = new ConnectionBuilder(2, 2000);
        connectionBuilder.setHttpContext(USER, PSWD);
        AnsibleResult result = connectionBuilder.get("test.server.com");

        assertEquals(611, result.getStatusCode());
        assertNull(result.getStatusMessage());
        assertEquals("UNKNOWN", result.getResults());
    }

    @Test (expected = FileNotFoundException.class)
    public void testGetModeNoCert() throws KeyStoreException, CertificateException, IOException,
            KeyManagementException, NoSuchAlgorithmException {
        String certFile = "testCert";

        connectionBuilder = new ConnectionBuilder(certFile, 2000);
        connectionBuilder.setHttpContext(USER, PSWD);
        AnsibleResult result = connectionBuilder.get(URL);

        assertEquals(611, result.getStatusCode());
        assertNull(result.getStatusMessage());
        assertEquals("UNKNOWN", result.getResults());
    }

    @Test
    public void testGetModeCert() throws KeyStoreException, CertificateException, IOException,
            KeyManagementException, NoSuchAlgorithmException {
        String certFile = "src/test/resources/cert";

        connectionBuilder = new ConnectionBuilder(certFile, 2000);
        connectionBuilder.setHttpContext(USER, PSWD);
        AnsibleResult result = connectionBuilder.get("test.server.com");

        assertEquals(611, result.getStatusCode());
        assertNull(result.getStatusMessage());
        assertEquals("UNKNOWN", result.getResults());
    }

    @Test (expected = IOException.class)
    public void testGetModeStore() throws KeyStoreException, CertificateException, IOException,
            KeyManagementException, NoSuchAlgorithmException {
        String store = "src/test/resources/cert";

        connectionBuilder = new ConnectionBuilder(store, new char['t'], 2000, "1.1.1.1" );
        connectionBuilder.setHttpContext(USER, PSWD);
        AnsibleResult result = connectionBuilder.get(URL);

        assertEquals(611, result.getStatusCode());
        assertNull(result.getStatusMessage());
        assertEquals("UNKNOWN", result.getResults());
    }

}
