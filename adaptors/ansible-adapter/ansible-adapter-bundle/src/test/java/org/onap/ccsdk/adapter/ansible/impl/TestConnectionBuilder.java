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

package org.onap.ccsdk.adapter.ansible.impl;

import java.io.File;
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
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdapterPropertiesProviderImpl;
import org.onap.ccsdk.sli.adaptors.ansible.impl.ConnectionBuilder;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResultCodes;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestConnectionBuilder {

    private static String KEYSTORE_FILE;
    private static String KEYSTORE_PASSWORD;
    private static String KEYSTORE_CERTIFICATE;
    private static String USERNAME;
    private static String PASSWORD;
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
        final String configFilePath = "src/test/resources/properties/ansible-adapter-test.properties".replace("/", File.separator);
        Properties properties = new AnsibleAdapterPropertiesProviderImpl(configFilePath).getProperties();

        KEYSTORE_FILE = properties.getProperty("org.onap.appc.adapter.ansible.trustStore");
        KEYSTORE_PASSWORD = properties.getProperty("org.onap.appc.adapter.ansible.trustStore.trustPasswd");
        KEYSTORE_CERTIFICATE = properties.getProperty("org.onap.appc.adapter.ansible.cert");
        USERNAME = properties.getProperty("org.onap.appc.adapter.ansible.username");
        PASSWORD = properties.getProperty("org.onap.appc.adapter.ansible.password");
        URL = properties.getProperty("org.onap.appc.adapter.ansible.identity");
    }

    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test
     * to change the internal state accordingly.
     *
     * @throws KeyManagementException   If unable to manage the key
     * @throws NoSuchAlgorithmException If an algorithm is found to be used but is unknown
     * @throws KeyStoreException        If any issues accessing the keystore
     */
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

    /**
     * This test case is used to invoke the constructor with keystore file and trust store password.
     *
     * @throws KeyManagementException   If unable to manage the key
     * @throws KeyStoreException        If any issues accessing the keystore
     * @throws CertificateException     If the certificate is tampared
     * @throws NoSuchAlgorithmException If an algorithm is found to be used but is unknown
     * @throws IOException              Signals that an I/O exception has occurred.
     */
    @Test
    public void testConnectionBuilder() throws KeyManagementException, KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException {
        char[] trustStorePassword = KEYSTORE_PASSWORD.toCharArray();
        ConnectionBuilder connectionBuilder = new ConnectionBuilder(KEYSTORE_FILE, trustStorePassword, 600000, "");
        assertNotNull(connectionBuilder);
    }

    /**
     * This test case is used to invoke the constructor with keystore certificate
     *
     * @throws KeyManagementException   If unable to manage the key
     * @throws KeyStoreException        If any issues accessing the keystore
     * @throws CertificateException     If the certificate is tampared
     * @throws NoSuchAlgorithmException If an algorithm is found to be used but is unknown
     * @throws IOException              Signals that an I/O exception has occurred.
     */
    @Test
    public void testConnectionBuilderWithFilePath() throws KeyManagementException, KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException {
        new ConnectionBuilder(KEYSTORE_CERTIFICATE, 600000);
    }

    /**
     * This test case is used to set the http context with username and password
     */
    @Test
    public void testSetHttpContext() {
        ConnectionBuilder spyConnectionBuilder = Mockito.spy(connectionBuilder);
        spyConnectionBuilder.setHttpContext(USERNAME, PASSWORD);
        verify(spyConnectionBuilder, times(1)).setHttpContext(USERNAME, PASSWORD);
    }

    /**
     * This test case is used to test the post method
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testPost() throws
            IOException {
        when(httpClient.execute(anyObject(), eq(httpClientContext))).thenReturn(response);
        AnsibleResult result = connectionBuilder.post(URL, "appc");
        assertEquals(SUCCESS_STATUS, result.getStatusCode());
    }

    /**
     * This test case is used to test the post method with exception
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testPostWithException() throws IOException {
        when(httpClient.execute(anyObject(), eq(httpClientContext))).thenThrow(new IOException());
        AnsibleResult result = connectionBuilder.post(URL, "appc");
        assertEquals(AnsibleResultCodes.IO_EXCEPTION.getValue(), result.getStatusCode());
    }

    /**
     * This test case is used to test the get method
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Ignore
    @Test
    public void testGet() throws IOException {
        when(httpClient.execute(anyObject(), eq(httpClientContext))).thenReturn(response);
        AnsibleResult result = connectionBuilder.get(URL);
        assertEquals(SUCCESS_STATUS, result.getStatusCode());
    }

    /**
     * This test case is used to test the get method with exception
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
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

}
