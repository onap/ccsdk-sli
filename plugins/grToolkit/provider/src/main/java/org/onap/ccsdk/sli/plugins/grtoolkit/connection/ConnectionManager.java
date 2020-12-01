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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Handles the process for getting HTTP connections to resources. Has the
 * ability to send JSON payloads. Only supports basic authorization when
 * sending credentials.
 *
 * @author Anthony Haddox
 * @see ConnectionResponse
 */
public interface ConnectionManager {
    Logger log = LoggerFactory.getLogger(ConnectionManager.class);
    int CONNECTION_TIMEOUT = 5000; // 5 second timeout
    enum HttpMethod {
        GET("GET"),
        POST("POST");

        private final String method;
        HttpMethod(String method) {
            this.method = method;
        }
        String getMethod() {
            return method;
        }
    }

    /**
     * Writes a JSON payload to an {@code HTTPURLConnection OutputStream}.
     *
     * @param input the JSON payload to send
     * @param connection the {@code HTTPURLConnection} to write to
     * @throws IOException if there is a problem writing to the output stream
     */
    static void sendPayload(String input, HttpURLConnection connection) throws IOException {
        byte[] out = input.getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        connection.setFixedLengthStreamingMode(length);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.connect();
        try(OutputStream os = connection.getOutputStream()) {
            os.write(out);
        }
    }

    /**
     * Gets an {@code HTTPURLConnection} to a {@code host}.
     *
     * @param host the host to connect to
     * @return an {@code HTTPURLConnection}
     * @throws IOException if a connection cannot be opened
     */
    static HttpURLConnection getConnection(String host) throws IOException {
        log.info("getConnection(): Getting connection to: {}", host);
        URL url = new URL(host);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Proxy-Connection", "keep-alive");
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(CONNECTION_TIMEOUT);
        return connection;
    }

    /**
     * Gets an {@code HTTPURLConnection} to a {@code host} and sets the
     * Authorization header with the supplied credentials. Only supports basic
     * authentication.
     *
     * @param host the host to connect to
     * @param credentials the authorization credentials
     * @return an {@code HTTPURLConnection} with Authorization header set
     * @throws IOException if a connection cannot be opened
     */
    static HttpURLConnection getConnection(String host, String credentials) throws IOException {
        String auth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes());
        HttpURLConnection connection = getConnection(host);
        connection.addRequestProperty("Authorization", auth);
        credentials = null;
        auth = null;
        return connection;
    }

    /**
     * Opens a connection to a path, sends a payload (if supplied with one),
     * and returns the response.
     * @param path the host to connect to
     * @param method the {@code HttpMethod} to use
     * @param input the payload to send
     * @param credentials the credentials to use
     * @return a {@code ConnectionResponse} containing the response body and
     *         status code of the operation
     * @throws IOException if a connection cannot be opened or if the payload
     *                     cannot be sent
     * @see HttpMethod
     */
    static ConnectionResponse getConnectionResponse(String path, HttpMethod method, String input, String credentials) throws IOException {
        HttpURLConnection connection = (StringUtils.isEmpty(credentials)) ? getConnection(path) : getConnection(path, credentials);
        credentials = null;
        connection.setRequestMethod(method.getMethod());
        connection.setDoInput(true);

        if(!StringUtils.isEmpty(input)) {
            sendPayload(input, connection);
        }

        StringBuilder content = new StringBuilder();
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while((inputLine = bufferedReader.readLine()) != null) {
                content.append(inputLine);
            }
        } finally {
            connection.disconnect();
        }

        ConnectionResponse connectionResponse = new ConnectionResponse();
        connectionResponse.content = content.toString();
        connectionResponse.statusCode = connection.getResponseCode();
        log.info("getConnectionResponse(): {} response code from {}", connectionResponse.statusCode, path);
        log.debug("getConnectionResponse(): Response:\n{}", connectionResponse.content);
        return connectionResponse;
    }
}
