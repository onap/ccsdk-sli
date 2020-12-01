/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.ccsdk.sli.plugins.restconfdiscovery;

import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.restapicall.Parameters;
import org.onap.ccsdk.sli.plugins.restapicall.RestapiCallNode;
import org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiCallNode;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Representation of a plugin to subscribe for notification and then
 * to handle the received notifications.
 */
public class RestconfDiscoveryNode implements SvcLogicDiscoveryPlugin {

    private static final Logger log = getLogger(RestconfDiscoveryNode.class);

    private ExecutorService executor = Executors.newCachedThreadPool();
    private Map<String, PersistentConnection> runnableInfo = new ConcurrentHashMap<>();
    private RestconfApiCallNode restconfApiCallNode;

    private volatile Map<String, SubscriptionInfo> subscriptionInfoMap = new ConcurrentHashMap<>();
    private volatile LinkedBlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();

    private static final String SUBSCRIBER_ID = "subscriberId";
    private static final String RESPONSE_CODE = "response-code";
    private static final String RESPONSE_PREFIX = "responsePrefix";
    private static final String OUTPUT_IDENTIFIER = "ietf-subscribed-notif" +
            "ications:establish-subscription.output.identifier";
    private static final String RESPONSE_CODE_200 = "200";
    private static final String SSE_URL = "sseConnectURL";

    /**
     * Creates an instance of RestconfDiscoveryNode and starts processing of
     * event.
     *
     * @param r restconf api call node
     */
    public RestconfDiscoveryNode(RestconfApiCallNode r) {
        this.restconfApiCallNode = r;
        ExecutorService e = Executors.newFixedThreadPool(20);
        EventProcessor p = new EventProcessor(this);
        for (int i = 0; i < 20; ++i) {
            e.execute(p);
        }
    }

    @Override
    public void establishSubscription(Map<String, String> paramMap,
                                      SvcLogicContext ctx) throws SvcLogicException {
        String subscriberId = paramMap.get(SUBSCRIBER_ID);
        if (subscriberId == null) {
            throw new SvcLogicException("Subscriber Id is null");
        }

        restconfApiCallNode.sendRequest(paramMap, ctx);

        if (getResponseCode(paramMap.get(RESPONSE_PREFIX), ctx).equals(RESPONSE_CODE_200)) {
            // TODO: save subscription id and subscriber in MYSQL

            establishPersistentConnection(paramMap, ctx, subscriberId);
        } else {
            log.info("Failed to subscribe {}", subscriberId);
            throw new SvcLogicException(ctx.getAttribute(RESPONSE_CODE));
        }
    }

    @Override
    public void modifySubscription(Map<String, String> paramMap, SvcLogicContext ctx) {
        // TODO: to be implemented
    }

    @Override
    public void deleteSubscription(Map<String, String> paramMap, SvcLogicContext ctx) {
        String id = getSubscriptionId(paramMap.get(SUBSCRIBER_ID));
        if (id != null) {
            PersistentConnection conn = runnableInfo.get(id);
            conn.terminate();
            runnableInfo.remove(id);
            subscriptionInfoMap.remove(id);
        }
    }

    class PersistentConnection implements Runnable {
        private String url;
        private volatile boolean running = true;
        private Map<String, String> paramMap;

        PersistentConnection(String url, Map<String, String> paramMap) {
            this.url = url;
            this.paramMap = paramMap;
        }

        private void terminate() {
            running = false;
        }

        @Override
        public void run() {
            Parameters p;
            WebTarget target = null;
            try {
                RestapiCallNode restapi = restconfApiCallNode.getRestapiCallNode();
                p = RestapiCallNode.getParameters(paramMap, new Parameters());
                Client client =  ignoreSslClient().register(SseFeature.class);
                target = restapi.addAuthType(client, p).target(url);
            } catch (SvcLogicException e) {
                log.error("Exception occured!", e);
                Thread.currentThread().interrupt();
            }

            target = addToken(target, paramMap.get("customHttpHeaders"));
            EventSource eventSource = EventSource.target(target).build();
            eventSource.register(new EventHandler(RestconfDiscoveryNode.this));
            eventSource.open();
            log.info("Connected to SSE source");
            while (running) {
                try {
                    log.info("SSE state " + eventSource.isOpen());
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    log.error("Interrupted!", e);
                    Thread.currentThread().interrupt();
                }
            }
            eventSource.close();
            log.info("Closed connection to SSE source");
        }

        private Client ignoreSslClient() {
            SSLContext sslcontext = null;

            try {
                sslcontext = SSLContext.getInstance("TLS");
                sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                } }, new java.security.SecureRandom());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new IllegalStateException(e);
            }

            return ClientBuilder.newBuilder().sslContext(sslcontext).hostnameVerifier((s1, s2) -> true).build();
        }
    }

    protected String getTokenId(String customHttpHeaders) {
        if (customHttpHeaders.contains("=")) {
            String[] s = customHttpHeaders.split("=");
            return s[1];
        }
        return customHttpHeaders;
    }

    protected WebTarget addToken(WebTarget target, String customHttpHeaders) {
        if (customHttpHeaders == null) {
            return target;
        }

        return new AdditionalHeaderWebTarget(
                target, getTokenId(customHttpHeaders));
    }

    /**
     * Establishes a persistent between the client and server.
     *
     * @param paramMap input paramter map
     * @param ctx service logic context
     * @param subscriberId subscriber identifier
     */
    void establishPersistentConnection(Map<String, String> paramMap, SvcLogicContext ctx,
                                              String subscriberId) {
        String id = getOutputIdentifier(paramMap.get(RESPONSE_PREFIX), ctx);
        SvcLogicGraphInfo callbackDG = new SvcLogicGraphInfo(paramMap.get("module"),
                                                             paramMap.get("rpc"),
                                                             paramMap.get("version"),
                                                             paramMap.get("mode"));
        SubscriptionInfo info = new SubscriptionInfo();
        info.callBackDG(callbackDG);
        info.subscriptionId(id);
        info.subscriberId(subscriberId);
        subscriptionInfoMap.put(id, info);

        String url = paramMap.get(SSE_URL);
        PersistentConnection connection = new PersistentConnection(url, paramMap);
        runnableInfo.put(id, connection);
        executor.execute(connection);
    }

    /**
     * Returns response code.
     *
     * @param prefix prefix given in input parameter
     * @param ctx service logic context
     * @return response code
     */
    String getResponseCode(String prefix, SvcLogicContext ctx) {
        return ctx.getAttribute(getPrefix(prefix) + RESPONSE_CODE);
    }

    /**
     * Returns subscription id from event.
     *
     * @param prefix prefix given in input parameter
     * @param ctx service logic context
     * @return subscription id from event
     */
    String getOutputIdentifier(String prefix, SvcLogicContext ctx) {
        return ctx.getAttribute(getPrefix(prefix) + OUTPUT_IDENTIFIER);
    }

    private String getPrefix(String prefix) {
        return prefix != null ? prefix + "." : "";
    }

    private String getSubscriptionId(String subscriberId) {
        for (Map.Entry<String,SubscriptionInfo> entry
                : subscriptionInfoMap.entrySet()) {
            if (entry.getValue().subscriberId()
                    .equals(subscriberId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns restconfApiCallNode.
     *
     * @return restconfApiCallNode
     */
    protected RestconfApiCallNode restconfapiCallNode() {
        return restconfApiCallNode;
    }

    /**
     * Sets restconfApiCallNode.
     *
     * @param node restconfApiCallNode
     */
    void restconfapiCallNode(RestconfApiCallNode node) {
        restconfApiCallNode = node;
    }

    Map<String, SubscriptionInfo> subscriptionInfoMap() {
        return subscriptionInfoMap;
    }

    void subscriptionInfoMap(Map<String, SubscriptionInfo> subscriptionInfoMap) {
        this.subscriptionInfoMap = subscriptionInfoMap;
    }

    LinkedBlockingQueue<String> eventQueue() {
        return eventQueue;
    }

    void eventQueue(LinkedBlockingQueue<String> eventQueue) {
        this.eventQueue = eventQueue;
    }
}
