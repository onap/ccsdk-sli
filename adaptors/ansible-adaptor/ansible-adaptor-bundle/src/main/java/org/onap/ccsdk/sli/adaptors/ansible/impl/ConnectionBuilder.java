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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResultCodes;
import org.onap.ccsdk.sli.core.utils.PathValidator;

/**
 * Returns a custom http client
 * - based on options
 * - can create one with ssl using an X509 certificate that does NOT have a known CA
 * - create one which trusts ALL SSL certificates
 * - return default httpclient (which only trusts known CAs from default cacerts file for process) this is the default
 * option
 **/

public class ConnectionBuilder implements Closeable {
    private static final String STATUS_CODE_KEY = "StatusCode";
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ConnectionBuilder.class);

    private final CloseableHttpClient httpClient;
    private final HttpClientContext httpContext = new HttpClientContext();

    /**
     * Constructor that initializes an http client based on certificate
     **/
    public ConnectionBuilder(String certFile, int timeout) throws KeyStoreException, CertificateException, IOException,
            KeyManagementException, NoSuchAlgorithmException {

        /* Point to the certificate */
        try (FileInputStream fs = new FileInputStream(certFile)) {
            /* Generate a certificate from the X509 */
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(fs);

            /* Create a keystore object and load the certificate there */
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, null);
            keystore.setCertificateEntry("cacert", cert);

            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(keystore).build();
            SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

            RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).build();
            httpClient = HttpClients.custom().setDefaultRequestConfig(config).setSSLSocketFactory(factory).build();
        }
    }

    /**
     * Constructor which trusts all certificates in a specific java keystore file (assumes a JKS
     * file)
     **/
    public ConnectionBuilder(String trustStoreFile, char[] trustStorePasswd, int timeout, String serverIP)
            throws KeyStoreException, IOException, KeyManagementException, NoSuchAlgorithmException,
            CertificateException {
        if (!PathValidator.isValidFilePath(trustStoreFile)) {
            throw new IOException("Invalid trust store file path");
        }

        /* Load the specified trustStore */
        KeyStore keystore = KeyStore.getInstance("JKS");
        FileInputStream readStream = new FileInputStream(trustStoreFile);
        keystore.load(readStream, trustStorePasswd);
        if (StringUtils.isNotBlank(serverIP)) {
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
            SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext, new NoopHostnameVerifier());

            RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).build();
            httpClient = HttpClients.custom().setDefaultRequestConfig(config).setSSLSocketFactory(factory).build();
        } else {
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(keystore).build();
            SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).build();
            httpClient = HttpClients.custom().setDefaultRequestConfig(config).setSSLSocketFactory(factory).build();
        }
    }

    /**
     * Constructor that trusts ALL SSl certificates (NOTE : ONLY FOR DEV TESTING) if Mode == 1 or
     * Default if Mode == 0
     */
    public ConnectionBuilder(int mode, int timeout)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(timeout).build();
        if (mode == 1) {
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
            SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

            httpClient = HttpClients.custom().setDefaultRequestConfig(config).setSSLSocketFactory(factory).build();
        } else {
            httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
        }
    }

    // Use to create an http context with auth headers
    public void setHttpContext(String user, String pswd) {

        // Are credential provided ? If so, set the context to be used
        if (user != null && !user.isEmpty() && pswd != null && !pswd.isEmpty()) {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, pswd);
            AuthScope authscope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);
            BasicCredentialsProvider credsprovider = new BasicCredentialsProvider();
            credsprovider.setCredentials(authscope, credentials);
            httpContext.setCredentialsProvider(credsprovider);
        }
    }

    // Method posts to the ansible server and writes out response to
    // Ansible result object
    public AnsibleResult post(String agentUrl, String payload) {

        AnsibleResult result = new AnsibleResult();
        try {

            HttpPost postObj = new HttpPost(agentUrl);
            StringEntity bodyParams = new StringEntity(payload, "UTF-8");
            postObj.setEntity(bodyParams);
            postObj.addHeader("Content-type", "application/json");

            HttpResponse response = httpClient.execute(postObj, httpContext);
            HttpEntity entity = response.getEntity();
            String responseOutput = entity != null ? EntityUtils.toString(entity) : null;
            int responseCode = response.getStatusLine().getStatusCode();
            result.setStatusCode(responseCode);
            result.setStatusMessage(responseOutput);
        } catch (IOException io) {
            logger.error("Caught IOException", io);
            result.setStatusCode(AnsibleResultCodes.IO_EXCEPTION.getValue());
            result.setStatusMessage(io.getMessage());
        }
        return result;
    }

    // Method gets information from an Ansible server and writes out response to
    // Ansible result object

    public AnsibleResult get(String agentUrl) {

        AnsibleResult result = new AnsibleResult();

        try {
            HttpGet getObj = new HttpGet(agentUrl);
            HttpResponse response = httpClient.execute(getObj, httpContext);
            HttpEntity entity = response.getEntity();
            String responseOutput = entity != null ? EntityUtils.toString(entity) : null;
            int responseCode = response.getStatusLine().getStatusCode();
            logger.info("GetResult response for ansible GET URL" + agentUrl + " returned " + responseOutput);
            JSONObject postResponse = new JSONObject(responseOutput);
            if (postResponse.has(STATUS_CODE_KEY)) {
                int codeStatus = postResponse.getInt(STATUS_CODE_KEY);
                if (codeStatus == AnsibleResultCodes.PENDING.getValue()) {
                    result.setStatusCode(codeStatus);
                } else {
                    result.setStatusCode(responseCode);
                }
            } else {
                result.setStatusCode(responseCode);
            }
            result.setStatusMessage(responseOutput);
        } catch (IOException io) {
            result.setStatusCode(AnsibleResultCodes.IO_EXCEPTION.getValue());
            result.setStatusMessage(io.getMessage());
            logger.error("Caught IOException", io);
        }
        return result;
    }

    @Override
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            logger.error("Caught IOException during httpClient close", e);
        }
    }

}
