/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.onap.ccsdk.sli.northbound.dmaapclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * java.net based client to build message router consumers
 */
public class MessageRouterHttpClientJdk implements SdncDmaapConsumer {
    private static final Logger Log = LoggerFactory.getLogger(MessageRouterHttpClientJdk.class);

    protected Boolean isReady = false;
    protected Boolean isRunning = false;
    protected URL url;
    protected Integer fetchPause;
    protected Properties properties;
    protected final String DEFAULT_CONNECT_TIMEOUT = "30000";
    protected final String DEFAULT_READ_TIMEOUT = "180000";
    protected final String DEFAULT_TIMEOUT_QUERY_PARAM_VALUE = "15000";
    protected final String DEFAULT_LIMIT = null;
    protected final String DEFAULT_FETCH_PAUSE = "5000";

    private String authorizationString;
    protected Integer connectTimeout;
    protected Integer readTimeout;
    protected String topic;

    public MessageRouterHttpClientJdk() {}

    @Override
    public void run() {
        if (isReady) {
            isRunning = true;
            while (isRunning) {
                HttpURLConnection httpUrlConnection = null;
                try {
                    httpUrlConnection = buildHttpURLConnection();
                    httpUrlConnection.connect();
                    int status = httpUrlConnection.getResponseCode();
                    Log.info("GET " + url + " returned http status " + status);
                    if (status < 300) {
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        String responseBody = sb.toString();
                        if (responseBody.contains("{")) {
                            // Get rid of opening [" entity =
                            responseBody = responseBody.substring(2);
                            // Get rid of closing "]
                            responseBody = responseBody.substring(0, responseBody.length() - 2);
                            // Split the json array into individual elements to process
                            for (String message : responseBody.split("\",\"")) {
                                // unescape the json
                                message = message.replace("\\\"", "\"");
                                // Topic names cannot contain periods
                                processMsg(message);
                            }
                        } else {
                            Log.info("Entity doesn't appear to contain JSON elements, logging body");
                            Log.info(responseBody);
                        }
                    }
                } catch (Exception e) {
                    Log.error("GET " + url + " failed.", e);
                } finally {
                    if (httpUrlConnection != null) {
                        httpUrlConnection.disconnect();
                    }
                    Log.info("Pausing " + fetchPause + " milliseconds before fetching from " + url + " again.");
                    try {
                        Thread.sleep(fetchPause);
                    } catch (InterruptedException e) {
                        Log.error("Could not sleep thread", e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    @Override
    public void init(Properties baseProperties, String consumerPropertiesPath) {
        try {
            baseProperties.load(new FileInputStream(new File(consumerPropertiesPath)));
            processProperties(baseProperties);
        } catch (FileNotFoundException e) {
            Log.error("FileNotFoundException while reading consumer properties", e);
        } catch (IOException e) {
            Log.error("IOException while reading consumer properties", e);
        }
    }

    protected void processProperties(Properties properties) throws MalformedURLException {
        this.properties = properties;
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        topic = properties.getProperty("topic");
        String group = properties.getProperty("group");
        String host = properties.getProperty("host");
        String id = properties.getProperty("id");

        String filter = properties.getProperty("filter");
        if (filter != null) {
            if (filter.length() > 0) {
                try {
                    filter = URLEncoder.encode(filter, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    Log.error("Couldn't encode filter string", e);
                }
            } else {
                filter = null;
            }
        }

        String limitString = properties.getProperty("limit", DEFAULT_LIMIT);
        Integer limit = null;
        if (limitString != null && limitString.length() > 0) {
            limit = Integer.valueOf(limitString);
        }

        Integer timeoutQueryParamValue =
                Integer.valueOf(properties.getProperty("timeout", DEFAULT_TIMEOUT_QUERY_PARAM_VALUE));
        connectTimeout = Integer.valueOf(properties.getProperty("connectTimeoutSeconds", DEFAULT_CONNECT_TIMEOUT));
        readTimeout = Integer.valueOf(properties.getProperty("readTimeoutMinutes", DEFAULT_READ_TIMEOUT));
        if (username != null && password != null && username.length() > 0 && password.length() > 0) {
            authorizationString = buildAuthorizationString(username, password);
        }
        String urlString = buildlUrlString(topic, group, id, host, timeoutQueryParamValue, limit, filter);
        this.url = new URL(urlString);
        this.fetchPause = Integer.valueOf(properties.getProperty("fetchPause", DEFAULT_FETCH_PAUSE));
        this.isReady = true;
    }

    public void processMsg(String msg) {
        Log.info(msg);
    }

    protected String buildAuthorizationString(String userName, String password) {
        String basicAuthString = userName + ":" + password;
        basicAuthString = Base64.getEncoder().encodeToString(basicAuthString.getBytes());
        return "Basic " + basicAuthString;
    }

    protected String buildlUrlString(String topic, String consumerGroup, String consumerId, String host,
            Integer timeout, Integer limit, String filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://" + host + "/events/" + topic + "/" + consumerGroup + "/" + consumerId);
        sb.append("?timeout=" + timeout);

        if (limit != null) {
            sb.append("&limit=" + limit);
        }
        if (filter != null) {
            sb.append("&filter=" + filter);
        }
        return sb.toString();
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    protected HttpURLConnection buildHttpURLConnection() throws IOException {
        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
        if (authorizationString != null) {
            httpUrlConnection.setRequestProperty("Authorization", authorizationString);
        }
        httpUrlConnection.setRequestMethod("GET");
        httpUrlConnection.setRequestProperty("Accept", "application/json");
        httpUrlConnection.setUseCaches(false);
        httpUrlConnection.setConnectTimeout(connectTimeout);
        httpUrlConnection.setReadTimeout(readTimeout);
        return httpUrlConnection;
    }

}
