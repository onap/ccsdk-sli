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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * jax-rs based client to build message router consumers
 */
public class MessageRouterHttpClient implements SdncDmaapConsumer {
    private static final Logger Log = LoggerFactory.getLogger(MessageRouterHttpClient.class);

    protected Boolean isReady = false;
    protected Boolean isRunning = false;
    protected Client client;
    protected URI uri;
    protected Invocation getMessages;
    protected Integer fetchPause;
    protected Properties properties;
    protected final String DEFAULT_CONNECT_TIMEOUT_SECONDS = "30";
    protected final String DEFAULT_READ_TIMEOUT_MINUTES = "3";
    protected final String DEFAULT_TIMEOUT_QUERY_PARAM_VALUE = "15000";
    protected final String DEFAULT_LIMIT = null;
    protected final String DEFAULT_FETCH_PAUSE = "5000";

    public MessageRouterHttpClient() {

    }

	@Override
	public void run() {
		if (isReady) {
			isRunning = true;
			while (isRunning) {
				try {
					Response response = getMessages.invoke();
					Log.info("GET " + uri + " returned http status " + response.getStatus());
					String entity = response.readEntity(String.class);
					if (response.getStatus() < 300) {
						if (entity.contains("{")) {
							// Get rid of opening ["
							entity = entity.substring(2);
							// Get rid of closing "]
							entity = entity.substring(0, entity.length() - 2);
							// This replacement effectively un-escapes the JSON
							for (String message : entity.split("\",\"")) {
								try {
									processMsg(message.replace("\\\"", "\""));
								} catch (InvalidMessageException e) {
									Log.error("Message could not be processed", e);
								}
							}
						} else {
							if (entity.length() < 1) {
								Log.info("GET was successful, but the server returned an empty message body.");
							} else {
								Log.info(
										"GET was successful, but entity is not valid JSON. Message body will be logged, but not processed");
								Log.info(entity);
							}
						}
					} else {
						Log.info("GET failed, message body will be logged, but not processed.");
						Log.info(entity);
					}
				} catch (Exception e) {
					Log.error("GET " + uri + " failed.", e);
				} finally {
					Log.info("Pausing " + fetchPause + " milliseconds before fetching from " + uri + " again.");
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
    
    protected void processProperties(Properties properties) {
        this.properties = properties;
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String topic = properties.getProperty("topic");
        String group = properties.getProperty("group");
        String host = properties.getProperty("host");
        String id = properties.getProperty("id");

        String filter = properties.getProperty("filter");
        if (filter != null) {
            if (filter.length() > 0) {
                try {
                    filter = URLEncoder.encode(filter, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    Log.error("Filter could not be encoded, setting to null", e);
                    filter = null;
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
        Integer connectTimeoutSeconds = Integer
                .valueOf(properties.getProperty("connectTimeoutSeconds", DEFAULT_CONNECT_TIMEOUT_SECONDS));
        Integer readTimeoutMinutes =
                Integer.valueOf(properties.getProperty("readTimeoutMinutes", DEFAULT_READ_TIMEOUT_MINUTES));
        this.client = getClient(connectTimeoutSeconds, readTimeoutMinutes);
        this.uri = buildUri(topic, group, id, host, timeoutQueryParamValue, limit, filter);
        Builder builder = client.target(uri).request("application/json");
        if (username != null && password != null && username.length() > 0 && password.length() > 0) {
            String authorizationString = buildAuthorizationString(username, password);
            builder.header("Authorization", authorizationString);
        }

        this.getMessages = builder.buildGet();
        this.fetchPause = Integer.valueOf(properties.getProperty("fetchPause",DEFAULT_FETCH_PAUSE));
        this.isReady = true;
    }

    @Override
    public void processMsg(String msg) throws InvalidMessageException {
        System.out.println(msg);
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    protected String buildAuthorizationString(String userName, String password) {
        String basicAuthString = userName + ":" + password;
        basicAuthString = Base64.getEncoder().encodeToString(basicAuthString.getBytes());
        return "Basic " + basicAuthString;
    }

    protected Client getClient(Integer connectTimeoutSeconds, Integer readTimeoutMinutes) {
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS);
        clientBuilder.readTimeout(readTimeoutMinutes, TimeUnit.MINUTES);
        return clientBuilder.build();
    }

    protected URI buildUri(String topic, String consumerGroup, String consumerId, String host, Integer timeout,
            Integer limit, String filter) {
        UriBuilder builder = UriBuilder.fromPath("http://" + host + "/events/{topic}/{consumerGroup}/{consumderId}");
        builder.queryParam("timeout", timeout);
        if (limit != null) {
            builder.queryParam("limit", limit);
        }
        if (filter != null) {
            builder.queryParam("filter", filter);
        }
        return builder.build(topic, consumerGroup, consumerId);
    }

}
