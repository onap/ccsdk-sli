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

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.common.AcceptIpAddressHostNameVerifier;
import org.onap.ccsdk.sli.plugins.restapicall.Parameters;
import org.onap.ccsdk.sli.plugins.restapicall.RestapiCallNode;
import org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiCallNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.*;

import static org.onap.ccsdk.sli.plugins.restapicall.JsonParser.convertToProperties;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Representation of a plugin to subscribe for notification and then
 * to handle the received notifications.
 */
public class RestconfDiscoveryNode implements RestConfSBController, SvcLogicDiscoveryPlugin {

    private static final Logger log = getLogger(RestconfDiscoveryNode.class);

    private static final String ROOT_RESOURCE = "/restconf";
    private static final String SUBSCRIBER_ID = "subscriberId";
    private static final String RESPONSE_CODE = "response-code";
    private static final String RESPONSE_PREFIX = "responsePrefix";
    private static final String OUTPUT_IDENTIFIER = "ietf-subscribed-notif" +
            "ications:establish-subscription.output.identifier";
    private static final String OUTPUT_IDENTIFIER_NO_PREFIX = "output.identifier";
    private static final String RESPONSE_CODE_200 = "200";
    private static final String SSE_URL = "sseConnectURL";
    private static final String REST_API_URL = "restapiUrl";
    private static final String RESOURCE_PATH_PREFIX = "/data/";
    private static final String NOTIFICATION_PATH_PREFIX = "/streams/";
    private static final String DEVICE_IP = "deviceIp";
    private static final String DEVICE_PORT = "devicePort";
    private static final String DOUBLESLASH = "//";
    private static final String COLON = ":";

    private RestconfApiCallNode restconfApiCallNode;
    private RestapiCallNode restapiCallNode = new RestapiCallNode();
    private volatile Map<String, SubscriptionInfo> subscriptionInfoMap = new ConcurrentHashMap<>();
    private volatile LinkedBlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();
    private Map<DeviceId, Set<RestconfNotificationEventListener>>
            restconfNotificationListenerMap = new ConcurrentHashMap<>();
    private Map<DeviceId, GetChunksRunnable>
            runnableTable = new ConcurrentHashMap<>();
    private Map<DeviceId, String> subscribedDevicesTable = new ConcurrentHashMap<>();
    private Map<DeviceId, BlockingQueue<String>> eventQMap = new ConcurrentHashMap<>();
    private Map<DeviceId, InternalRestconfEventProcessorRunnable>
            processorRunnableTable = new ConcurrentHashMap<>();
    private Map<String, PersistentConnection> runnableInfo = new ConcurrentHashMap<>();
    private final Map<DeviceId, RestSBDevice> deviceMap = new ConcurrentHashMap<>();
    private final Map<DeviceId, Client> clientMap = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Creates an instance of RestconfDiscoveryNode and starts processing of
     * event.
     *
     * @param r restconf api call node
     */
    public RestconfDiscoveryNode(RestconfApiCallNode r) {
        log.info("inside RestconfDiscoveryNode Constructor");
        this.restconfApiCallNode = r;
        this.activate();
//        ExecutorService e = Executors.newFixedThreadPool(20);
//        EventProcessor p = new EventProcessor(this);
//        for (int i = 0; i < 20; ++i) {
//            e.execute(p);
//        }
    }

    public void activate() {
        log.info("RESTCONF SBI Started");
    }

    public void deactivate() {
        log.info("RESTCONF SBI Stopped");
        executor.shutdown();
        this.getClientMap().clear();
        this.getDeviceMap().clear();
    }

    public Map<DeviceId, RestSBDevice> getDeviceMap() {
        return deviceMap;
    }

    public Map<DeviceId, Client> getClientMap() {
        return clientMap;
    }

    @Override
    public Map<DeviceId, RestSBDevice> getDevices() {
        log.trace("RESTCONF SBI::getDevices");
        return ImmutableMap.copyOf(deviceMap);
    }

    @Override
    public RestSBDevice getDevice(DeviceId deviceInfo) {
        log.trace("RESTCONF SBI::getDevice with deviceId");
        return deviceMap.get(deviceInfo);
    }

    @Override
    public RestSBDevice getDevice(String ip, int port) {
        log.trace("RESTCONF SBI::getDevice with ip and port");
        try {
            if (!deviceMap.isEmpty()) {
                return deviceMap.values().stream().filter(v -> v.ip().equals(ip) && v.port() == port).findFirst().get();
            }
        } catch (NoSuchElementException noSuchElementException) {
            log.error("getDevice::device {}:{} does not exist in deviceMap", ip, port);
        }
        return null;
    }

    @Override
    public void addDevice(RestSBDevice device) {
        log.trace("RESTCONF SBI::addDevice");
        if (!deviceMap.containsKey(device.deviceId())) {
            if (device.username() != null) {
                String username = device.username();
                String password = device.password() == null ? "" : device.password();
    //                authenticate(client, username, password);
            }
            BlockingQueue<String> newBlockingQueue = new LinkedBlockingQueue<>();
            eventQMap.put(device.deviceId(), newBlockingQueue);
            InternalRestconfEventProcessorRunnable eventProcessorRunnable =
                    new InternalRestconfEventProcessorRunnable(device.deviceId());
            processorRunnableTable.put(device.deviceId(), eventProcessorRunnable);
            log.trace("addDevice::restconf event processor runnable is created and is going for execute");
            executor.execute(eventProcessorRunnable);
            log.trace("addDevice::restconf event processor runnable was sent for execute");
            deviceMap.put(device.deviceId(), device);
        } else {
            log.warn("addDevice::Trying to add a device which already exists {}", device.deviceId());
        }
    }

    @Override
    public void removeDevice(DeviceId deviceId) {
        log.trace("RESTCONF SBI::removeDevice");
        eventQMap.remove(deviceId);
        clientMap.remove(deviceId);
        deviceMap.remove(deviceId);
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
    public void establishSubscriptionOnly(Map<String, String> paramMap, SvcLogicContext ctx)
            throws SvcLogicException {
        String subscriberId = paramMap.get(SUBSCRIBER_ID);
        if (subscriberId == null) {
            throw new SvcLogicException("Subscriber Id is null");
        }

        String subscribeUrlString = paramMap.get(REST_API_URL);
        URL subscribeUrl = null;
        RestSBDevice dev = null;
        try {
            subscribeUrl = new URL(subscribeUrlString);
            dev = getDevice(subscribeUrl.getHost(), subscribeUrl.getPort());
        } catch (MalformedURLException e) {
            log.error("establishSubscriptionOnly::MalformedURLException happened. e: {}", e);
            return;
        }

        if (dev == null) {
            log.warn("establishSubscriptionOnly::device does not exist in the map. Trying to create one now.");
            //FIXME: TODO: create a new RestSBDevice and add it to the map, as well as a client and clientMap
            dev = new DefaultRestSBDevice(subscribeUrl.getHost(),
                    subscribeUrl.getPort(), "onos", "rocks", "http",
                    subscribeUrl.getHost() + ":" + subscribeUrl.getPort(), true);
            this.addDevice(dev);
        }

        if (!subscribedDevicesTable.containsKey(dev.deviceId())) {
            log.info("establishSubscriptionOnly::The device {} has not been subscribed yet. " +
                    "Trying to subscribe it now...");
            restapiCallNode.sendRequest(paramMap, ctx);
            if (getResponseCode(paramMap.get(RESPONSE_PREFIX), ctx).equals(RESPONSE_CODE_200)) {
                // TODO: save subscription id and subscriber in MYSQL
                String id = getOutputIdentifierNoPrefix(paramMap.get(RESPONSE_PREFIX), ctx);
                log.info("establishSubscriptionOnly::Subscription is done successfully and " +
                        "the output.identifier is: {}", id);
                log.info("establishSubscriptionOnly::The subscriptionID returned by the server " +
                        "does not exist in the map. Adding it now...");
                subscribedDevicesTable.put(dev.deviceId(), id);

                SvcLogicGraphInfo callbackDG = new SvcLogicGraphInfo(paramMap.get("module"),
                        paramMap.get("rpc"),
                        paramMap.get("version"),
                        paramMap.get("mode"));
                SubscriptionInfo info = new SubscriptionInfo();
                info.callBackDG(callbackDG);
                info.subscriptionId(id);
                info.subscriberId(subscriberId);
                subscriptionInfoMap.put(id, info);

            }
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
                Client client =  ignoreSslClient(p.disableHostVerification).register(SseFeature.class);
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

        // Note: Sonar complains about host name verification being 
        // disabled here.  This is necessary to handle devices using self-signed
        // certificates (where CA would be unknown) - so we are leaving this code as is.
        private Client ignoreSslClient(boolean disableHostVerification) {
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

            return ClientBuilder.newBuilder().sslContext(sslcontext).hostnameVerifier(new AcceptIpAddressHostNameVerifier(disableHostVerification)).build();
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

    String getOutputIdentifierNoPrefix(String prefix, SvcLogicContext ctx) {
        return ctx.getAttribute(getPrefix(prefix) + OUTPUT_IDENTIFIER_NO_PREFIX);
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

    private String getUrlString(DeviceId deviceId, String request) {
        RestSBDevice restSBDevice = deviceMap.get(deviceId);
        if (restSBDevice == null) {
            log.warn("getUrlString::restSbDevice cannot be NULL!");
            return "";
        }
        if (restSBDevice.url() != null) {
            return restSBDevice.protocol() + COLON + DOUBLESLASH + restSBDevice.url() + request;
        } else {
            return restSBDevice.protocol() + COLON + DOUBLESLASH + restSBDevice.ip().toString()
                    + COLON + restSBDevice.port() + request;
        }
    }

    private String getSubscriptionIdFromDeviceId(DeviceId deviceId) {
        if (subscribedDevicesTable.containsKey(deviceId)) {
            return subscribedDevicesTable.get(deviceId);
        }
        return null;
    }

    private BlockingQueue<String> getEventQ(DeviceId deviceId) {
        if (eventQMap.containsKey(deviceId)) {
            return eventQMap.get(deviceId);
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

    /**
     * Establishes a persistent SSE connection between the client and the server.
     *
     * @param paramMap input paramter map
     * @param ctx service logic context
     */
    @Override
    public void establishPersistentSseConnection(Map<String, String> paramMap, SvcLogicContext ctx) throws SvcLogicException {

        //TODO: FIXME: remove the instantiation of info; not useful
        String subscriberId = paramMap.get(SUBSCRIBER_ID);
        SvcLogicGraphInfo callbackDG = new SvcLogicGraphInfo(paramMap.get("module"),
                paramMap.get("rpc"),
                paramMap.get("version"),
                paramMap.get("mode"));
        SubscriptionInfo info = new SubscriptionInfo();
        info.callBackDG(callbackDG);
        info.subscriberId(subscriberId);

        String sseUrlString = paramMap.get(SSE_URL);
        URL sseUrl = null;
        RestSBDevice dev = null;
        try {
            sseUrl = new URL(sseUrlString);
            dev = getDevice(sseUrl.getHost(), sseUrl.getPort());
        } catch (MalformedURLException e) {
            log.error("establishPersistentSseConnection::MalformedURLException happened. e: {}", e);
            return;
        }

        if (dev == null) {
            log.warn("establishPersistentSseConnection::device does not exist in the map. Trying to add one now.");
            dev = new DefaultRestSBDevice(sseUrl.getHost(),
                    sseUrl.getPort(), "onos", "rocks", "http",
                    sseUrl.getHost() + ":" + sseUrl.getPort(), true);
            this.addDevice(dev);
        }

        if (isNotificationEnabled(dev.deviceId())) {
            log.warn("establishPersistentSseConnection::notifications already enabled on device: {}",
                    dev.deviceId());
            return;
        }

        if (getSubscriptionIdFromDeviceId(dev.deviceId()) == null) {
            log.warn("This device {} has not yet been subscribed to receive notifications.",
                    dev.deviceId());
            return;
        }

        RestconfNotificationEventListenerImpl myListener =
                new RestconfNotificationEventListenerImpl(info);
        enableNotifications(dev.deviceId(), "yang-push-json", "json", myListener);
    }

    @Override
    public void enableNotifications(DeviceId device, String request,
                                    String mediaType,
                                    RestconfNotificationEventListener listener) {
        if (isNotificationEnabled(device)) {
            log.warn("enableNotifications::already enabled on device: {}", device);
            return;
        }

        request = discoverRootResource(device) + NOTIFICATION_PATH_PREFIX
                + request;

        addNotificationListener(device, listener);

        GetChunksRunnable runnable = new GetChunksRunnable(request, mediaType,
                device);
        runnableTable.put(device, runnable);
        executor.execute(runnable);
    }

    public void stopNotifications(DeviceId device) {
        try {
            runnableTable.get(device).terminate();
            processorRunnableTable.get(device).terminate();
        } catch (Exception ex) {
            log.error("stopNotifications::Exception happened when terminating, ex: {}", ex);
        }
        log.info("stopNotifications::Runnable is now terminated");
        runnableTable.remove(device);
        processorRunnableTable.remove(device);
        restconfNotificationListenerMap.remove(device);
        log.debug("stopNotifications::Stop sending notifications for device URI: " + device.uri().toString());
    }

    @Override
    public void deleteSubscriptionAndSseConnection(Map<String, String> paramMap, SvcLogicContext ctx) {
        String deleteSubscribeUrlString = paramMap.get(REST_API_URL);
        URL deleteSubscribeUrl = null;
        RestSBDevice dev = null;
        try {
            deleteSubscribeUrl = new URL(deleteSubscribeUrlString);
            dev = getDevice(deleteSubscribeUrl.getHost(), deleteSubscribeUrl.getPort());
        } catch (MalformedURLException e) {
            log.error("establishSubscriptionOnly::MalformedURLException happened. e: {}", e);
            return;
        }

        String deviceIp = deleteSubscribeUrl.getHost();
        String devicePort = String.valueOf(deleteSubscribeUrl.getPort());
        log.info("deleteSubscriptionAndSseConnection::Trying to unsubscribe device {}:{}",
                deviceIp, devicePort);
        if (dev == null) {
            log.error("deleteSubscriptionAndSseConnection::device does not exist in the map");
            return;
        }
        String subscriptionId = getSubscriptionIdFromDeviceId(dev.deviceId());

        if (subscriptionId != null) {
            log.info("deleteSubscriptionAndSseConnection::SubscriptionID is found {}", subscriptionId);
            log.info("deleteSubscriptionAndSseConnection::About to send unsubscribe request");
            try {
                restapiCallNode.sendRequest(paramMap, ctx);
                if (getResponseCode(paramMap.get(RESPONSE_PREFIX), ctx).equals(RESPONSE_CODE_200)) {
                    log.info("deleteSubscriptionAndSseConnection::Successfully unsubscribed");
                    stopNotifications(dev.deviceId());
                    subscribedDevicesTable.remove(dev.deviceId());

                    String id = getSubscriptionId(paramMap.get(SUBSCRIBER_ID));
                    if (id != null) {
                        subscriptionInfoMap.remove(id);
                    }

                } else {
                    log.info("deleteSubscriptionAndSseConnection::Unsubscription was NOT successfull");
                }
            } catch (SvcLogicException e) {
                log.error("deleteSubscriptionAndSseConnection::Exception happened ex: {}", e);
            }
        } else {
            log.warn("deleteSubscriptionAndSseConnection::This device has already been unsubscribed");
        }
    }

    /**
     * Notifies providers about incoming RESTCONF notification events.
     */public class GetChunksRunnable implements Runnable {
        private String request;
        private String mediaType;
        private DeviceId deviceId;

        private volatile boolean running = true;

        public void terminate() {
            log.info("GetChunksRunnable.terminate()::threadID: {}",
                    Thread.currentThread().getId());
            running = false;
        }

        /**
         * @param request   request
         * @param mediaType media type
         * @param deviceId    device identifier
         */
        public GetChunksRunnable(String request, String mediaType,
                                 DeviceId deviceId) {
            this.request = request;
            this.mediaType = mediaType;
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            log.trace("GetChunksRunnable.run()::threadID is: {} ...., running is: {}",
                    Thread.currentThread().getId(), running);
            try {
                Client client = ClientBuilder.newBuilder()
                        .register(SseFeature.class).build();
                WebTarget target = client.target(getUrlString(deviceId, request));
                log.trace("GetChunksRunnable.run()::target URI is {}", target.getUri().toString());
                Response response = target.request().get();
                EventInput eventInput = response.readEntity(EventInput.class);
                log.trace("GetChunksRunnable.run()::after eventInput");
                String rcvdData = "";
                while (!eventInput.isClosed() && running) {
                    log.trace("GetChunksRunnable.run()::inside while ...");
                    final InboundEvent inboundEvent = eventInput.read();
                    log.trace("GetChunksRunnable.run()::after eventInput.read() ...");
                    if (inboundEvent == null) {
                        // connection has been closed
                        log.info("GetChunksRunnable.run()::connection has been closed ...");
                        break;
                    }
                    if (running) {
                        rcvdData = inboundEvent.readData(String.class);
                        BlockingQueue<String> eventQ = getEventQ(deviceId);
                        if (eventQ != null) {
                            eventQ.add(rcvdData);
                            eventQMap.put(deviceId, eventQ);
                            log.trace("GetChunksRunnable.run()::eventQ got filled.");
                        } else {
                            log.error("GetChunksRunnable.run()::eventQ has not been initialized for this device {}",
                                    deviceId);
                        }
                    } else {
                        log.info("GetChunksRunnable.run()::running has changed to false while eventInput.read() " +
                                "was blocked to receive new notifications");
                        log.info("GetChunksRunnable.run()::the client is no longer interested to " +
                                "receive notifications.");
                        break;
                    }
                }
                if (!running) {
                    log.trace("GetChunksRunnable.run()::running is false! " +
                                    "closing eventInput, threadID: {}", Thread.currentThread().getId());
                    eventInput.close();
                    response.close();
                    client.close();
                    log.info("GetChunksRunnable.run()::eventInput is closed in run()");
                }
            } catch (Exception ex) {
                log.info("GetChunksRunnable.run()::We got some exception: {}, threadID: {} ", ex,
                        Thread.currentThread().getId());
            }
            log.trace("GetChunksRunnable.run()::after Runnable Try Catch. threadID: {} ",
                    Thread.currentThread().getId());
        }
    }

    public class InternalRestconfEventProcessorRunnable implements Runnable {

        private volatile boolean running = true;
        private DeviceId deviceId;

        public InternalRestconfEventProcessorRunnable(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        public void terminate() {
            log.info("InternalRestconfEventProcessorRunnable.terminate()::threadID: {}",
                    Thread.currentThread().getId());
            running = false;
        }

        @Override
        public void run() {
            log.trace("InternalRestconfEventProcessorRunnable::restconf event processor runnable inside run()");
            while (running) {
                try {
                    if (eventQMap != null && !eventQMap.isEmpty() && eventQMap.get(deviceId) != null) {
                        log.trace("InternalRestconfEventProcessorRunnable::waiting for take()");
                        if (running) {
                            String eventJsonString = eventQMap.get(deviceId).take();
                            log.trace("InternalRestconfEventProcessorRunnable::after take()");
                            log.info("InternalRestconfEventProcessorRunnable::eventJsonString is {}", eventJsonString);
                            Map<String, String> param = convertToProperties(eventJsonString);
                            String idString = param.get("push-change-update.subscription-id");
                            SubscriptionInfo info = subscriptionInfoMap().get(idString);
                            if (info != null) {
                                SvcLogicContext ctx = setContext(param);
                                SvcLogicGraphInfo callbackDG = info.callBackDG();
                                callbackDG.executeGraph(ctx);
                            }
                        } else {
                            log.info("InternalRestconfEventProcessorRunnable.run()::running has changed to false " +
                                    "while eventQ was blocked to process new notifications");
                            log.info("InternalRestconfEventProcessorRunnable.run()::" +
                                    "the client is no longer interested to receive notifications.");
                            break;
                        }
                    }
                } catch (InterruptedException | SvcLogicException e) {
                    e.printStackTrace();
                }
            }
        }
        private SvcLogicContext setContext(Map<String, String> param) {
            SvcLogicContext ctx = new SvcLogicContext();
            for (Map.Entry<String, String> entry : param.entrySet()) {
                ctx.setAttribute(entry.getKey(), entry.getValue());
            }
            return ctx;
        }
    }

    public String discoverRootResource(DeviceId device) {
        return ROOT_RESOURCE;
    }

    @Override
    public void addNotificationListener(DeviceId deviceId,
                                        RestconfNotificationEventListener listener) {
        Set<RestconfNotificationEventListener> listeners =
                restconfNotificationListenerMap.get(deviceId);
        if (listeners == null) {
            listeners = new HashSet<>();
        }

        listeners.add(listener);

        this.restconfNotificationListenerMap.put(deviceId, listeners);
    }

    @Override
    public void removeNotificationListener(DeviceId deviceId,
                                           RestconfNotificationEventListener listener) {
        Set<RestconfNotificationEventListener> listeners =
                restconfNotificationListenerMap.get(deviceId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public boolean isNotificationEnabled(DeviceId deviceId) {
        return runnableTable.containsKey(deviceId);
    }

}
