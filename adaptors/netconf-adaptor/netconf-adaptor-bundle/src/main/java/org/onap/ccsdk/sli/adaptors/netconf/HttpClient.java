/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.netconf;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

public class HttpClient {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(HttpClient.class);

    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    @SuppressWarnings("deprecation")
    public static int postMethod(String protocol, String ip, int port, String path, String payload,
            String contentType) throws SvcLogicException {

        logger.info("Sending POST request to " + path);

        HttpPost post;
        try {

            URL serviceUrl = new URL(protocol, ip, port, path);
            post = new HttpPost(serviceUrl.toExternalForm());
            post.setHeader("Content-Type", contentType);

            StringEntity entity = new StringEntity(payload);
            entity.setContentType(contentType);
            post.setEntity(new StringEntity(payload));
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new SvcLogicException(e.toString(), e);
        }

        logger.debug("Sending request " + post);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(ip, port), new UsernamePasswordCredentials(
                configuration.getProperty("username"), configuration.getProperty("password")));
        CloseableHttpClient client =
                HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        int httpCode;
        try {
            HttpResponse response = client.execute(post);
            httpCode = response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new SvcLogicException(e.toString());
        }
        return httpCode;
    }

    @SuppressWarnings("deprecation")
    public static int putMethod(String protocol, String ip, int port, String path, String payload,
            String contentType) throws SvcLogicException {

        logger.info("Sending PUT request to " + path);

        HttpPut put;
        try {

            URL serviceUrl = new URL(protocol, ip, port, path);
            put = new HttpPut(serviceUrl.toExternalForm());
            put.setHeader("Content-Type", contentType);

            StringEntity entity = new StringEntity(payload);
            entity.setContentType(contentType);
            put.setEntity(new StringEntity(payload));
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new SvcLogicException(e.toString());
        }

        logger.debug("Sending request " + put);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(ip, port), new UsernamePasswordCredentials(
                configuration.getProperty("username"), configuration.getProperty("password")));
        CloseableHttpClient client =
                HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        int httpCode;
        try {
            HttpResponse response = client.execute(put);
            httpCode = response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new SvcLogicException(e.toString());
        }
        return httpCode;
    }

    @SuppressWarnings("deprecation")
    public static String getMethod(String protocol, String ip, int port, String path,
            String contentType) throws SvcLogicException {

        logger.info("Sending GET request to " + path);

        HttpGet get;
        try {

            URL serviceUrl = new URL(protocol, ip, port, path);
            get = new HttpGet(serviceUrl.toExternalForm());
            get.setHeader("Content-Type", contentType);
        } catch (MalformedURLException e) {
            throw new SvcLogicException(e.toString());
        }

        logger.debug("Sending request " + get);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(ip, port), new UsernamePasswordCredentials(
                configuration.getProperty("username"), configuration.getProperty("password")));
        CloseableHttpClient client =
                HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        int httpCode;
        String result;

        try {
            HttpResponse response = client.execute(get);
            httpCode = response.getStatusLine().getStatusCode();
            result = (httpCode == HttpStatus.SC_OK) ? response.getEntity().toString() : null;
        } catch (IOException e) {
            throw new SvcLogicException(e.toString());
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    public static int deleteMethod(String protocol, String ip, int port, String path,
            String contentType) throws SvcLogicException {

        logger.info("Sending DELETE request to " + path);

        HttpDelete delete;
        try {

            URL serviceUrl = new URL(protocol, ip, port, path);
            delete = new HttpDelete(serviceUrl.toExternalForm());
            delete.setHeader("Content-Type", contentType);
        } catch (MalformedURLException e) {
            throw new SvcLogicException(e.toString());
        }

        logger.debug("Sending request " + delete);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(ip, port), new UsernamePasswordCredentials(
                configuration.getProperty("username"), configuration.getProperty("password")));
        CloseableHttpClient client =
                HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        int httpCode;

        try {
            HttpResponse response = client.execute(delete);
            httpCode = response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new SvcLogicException(e.toString());
        }

        return httpCode;
    }
}
