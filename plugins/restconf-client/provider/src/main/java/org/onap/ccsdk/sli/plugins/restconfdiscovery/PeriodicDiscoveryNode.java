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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

import static org.onap.ccsdk.sli.plugins.restapicall.JsonParser.convertToProperties;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Representation of a plugin to subscribe for notification and then
 * to handle the received notifications.
 */
public class PeriodicDiscoveryNode implements RestConfSBController, SvcLogicDiscoveryPlugin {

    private static final Logger log = getLogger(PeriodicDiscoveryNode.class);

    private static final String ROOT_RESOURCE = "/restconf";
    private static final String SUBSCRIBER_ID = "subscriberId";
    private static final String RESPONSE_CODE = "response-code";
    private static final String RESPONSE_PREFIX = "responsePrefix";
    private static final String OUTPUT_IDENTIFIER = "ietf-subscribed-notif" +
            "ications:establish-subscription.output.identifier";
    private static final String OUTPUT_IDENTIFIER_NO_PREFIX = "output.identifier";
    private static final String RESPONSE_CODE_200 = "200";
    private static final String SSE_URL = "sseConnectURL";
    private static final String PERIODIC_PUL_URL = "periodicPullURL";
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
    private Map<DeviceId, PeriodicPullRunnable> periodicRunnableTable = new ConcurrentHashMap<>();
    private Map<DeviceId, String> subscribedDevicesTable = new ConcurrentHashMap<>();
    private Map<DeviceId, BlockingQueue<String>> eventQMap = new ConcurrentHashMap<>();
    private Map<DeviceId, InternalPeriodicPullingProcessorRunnable>
            processorRunnableTable = new ConcurrentHashMap<>();
    private final Map<DeviceId, RestSBDevice> deviceMap = new ConcurrentHashMap<>();
    private final Map<DeviceId, Client> clientMap = new ConcurrentHashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);

    /**
     * Creates an instance of RestconfDiscoveryNode and starts processing of
     * event.
     *
     * @param r restconf api call node
     */
    public PeriodicDiscoveryNode(RestconfApiCallNode r) {
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
        log.info("PeriodicDiscoveryNode::deactivate: Going to shutdown the executors.");
        if (executor != null) {
            executor.shutdown();
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
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
        log.info("RESTCONF SBI::getDevices");
        return ImmutableMap.copyOf(deviceMap);
    }

    @Override
    public RestSBDevice getDevice(DeviceId deviceInfo) {
        log.info("RESTCONF SBI::getDevice with deviceId");
        return deviceMap.get(deviceInfo);
    }

    @Override
    public RestSBDevice getDevice(String ip, int port) {
        log.info("RESTCONF SBI::getDevice with ip and port");
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
        log.info("RESTCONF SBI::addDevice");
        if (!deviceMap.containsKey(device.deviceId())) {
            if (device.username() != null) {
                String username = device.username();
                String password = device.password() == null ? "" : device.password();
    //                authenticate(client, username, password);
            }
            BlockingQueue<String> newBlockingQueue = new LinkedBlockingQueue<>();
            eventQMap.put(device.deviceId(), newBlockingQueue);
            InternalPeriodicPullingProcessorRunnable eventProcessorRunnable =
                    new InternalPeriodicPullingProcessorRunnable(device.deviceId());
            processorRunnableTable.put(device.deviceId(), eventProcessorRunnable);
            log.info("addDevice::restconf event processor runnable is created and is going for execute");
            if (executor.isShutdown()) {
                log.info("PeriodicPulDiscoveryNode::addDevice - executor was shutdown. Restarting it.");
                executor = Executors.newCachedThreadPool();
            }
            executor.execute(eventProcessorRunnable);
            log.info("addDevice::restconf event processor runnable was sent for execute");
            deviceMap.put(device.deviceId(), device);
        } else {
            log.warn("addDevice::Trying to add a device which already exists {}", device.deviceId());
        }
    }

    @Override
    public void removeDevice(DeviceId deviceId) {
        log.info("RESTCONF SBI::removeDevice");
        eventQMap.remove(deviceId);
        clientMap.remove(deviceId);
        deviceMap.remove(deviceId);
    }

    @Override
    public void enableNotifications(DeviceId device, String request, String mediaType,
                                    RestconfNotificationEventListener callBackListener) {

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
        log.info("establishSubscriptionOnly::Necessary 80 sec. delay for the hardware to finish creating the resource");
        try {
            Thread.sleep(80000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
//                String id = getOutputIdentifierNoPrefix(paramMap.get(RESPONSE_PREFIX), ctx);
//                log.info("establishSubscriptionOnly::Subscription is done successfully and " +
//                        "the output.identifier is: {}", id);
                String id = dev.ip();
                log.info("establishSubscriptionOnly::Subscription is done successfully and " +
                        "the device ip is: {}", id);
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
//        String id = getSubscriptionId(paramMap.get(SUBSCRIBER_ID));
//        if (id != null) {
//            subscriptionInfoMap.remove(id);
//        }

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
            stopNotifications(dev.deviceId());
            subscribedDevicesTable.remove(dev.deviceId());

//            String id = getSubscriptionId(paramMap.get(SUBSCRIBER_ID));
            if (subscriptionId != null) {
                subscriptionInfoMap.remove(subscriptionId);
            }
        } else {
            log.warn("deleteSubscriptionAndSseConnection::This device has already been unsubscribed");
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

    }

    @Override
    public void establishPeriodicPullConnection(Map<String, String> paramMap, SvcLogicContext ctx) throws SvcLogicException {
        String subscriberId = paramMap.get(SUBSCRIBER_ID);
        SvcLogicGraphInfo callbackDG = new SvcLogicGraphInfo(paramMap.get("module"),
                paramMap.get("rpc"),
                paramMap.get("version"),
                paramMap.get("mode"));
        SubscriptionInfo info = new SubscriptionInfo();
        info.callBackDG(callbackDG);
        info.subscriberId(subscriberId);

        String periodicPullUrlString = paramMap.get(PERIODIC_PUL_URL);
        URL periodicPullUrl = null;
        RestSBDevice dev = null;
        try {
            periodicPullUrl = new URL(periodicPullUrlString);
            dev = getDevice(periodicPullUrl.getHost(), periodicPullUrl.getPort());
        } catch (MalformedURLException e) {
            log.error("establishPersistentSseConnection::MalformedURLException happened. e: {}", e);
            return;
        }

        if (dev == null) {
            log.warn("establishPeriodicPullConnection::device does not exist in the map. Trying to add one now.");
            dev = new DefaultRestSBDevice(periodicPullUrl.getHost(),
                    periodicPullUrl.getPort(), "onos", "rocks", "http",
                    periodicPullUrl.getHost() + ":" + periodicPullUrl.getPort(), true);
            this.addDevice(dev);
        }

        if (isNotificationEnabled(dev.deviceId())) {
            log.warn("establishPeriodicPullConnection::notifications already enabled on device: {}",
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
        enableNotifications(dev.deviceId(),
                "ietf-service-pm:performance-monitoring/service-pm=" + paramMap.get("ethServiceName"),
                "json", myListener, paramMap, ctx);
//        enableNotifications(dev.deviceId(), periodicPullUrlString,
//                "json", myListener, paramMap, ctx);
    }

    public void enableNotifications(DeviceId device, String request,
                                    String mediaType,
                                    RestconfNotificationEventListener listener, Map<String, String> paramMap, SvcLogicContext ctx) {
        if (isNotificationEnabled(device)) {
            log.warn("enableNotifications::already enabled on device: {}", device);
            return;
        }

        request = discoverRootResource(device) + RESOURCE_PATH_PREFIX
                + request;

        addNotificationListener(device, listener);

        PeriodicPullRunnable periodicRunnable = new PeriodicPullRunnable(request, device, paramMap, ctx);
        periodicRunnableTable.put(device, periodicRunnable);
        if (scheduledExecutor.isShutdown()) {
            log.info("PeriodicPulDiscoveryNode::enableNotifications - scheduledExecutor was shutdown. Restarting it.");
            scheduledExecutor = Executors.newScheduledThreadPool(2);
        }
        scheduledExecutor.scheduleAtFixedRate(periodicRunnable, 0, 90, TimeUnit.SECONDS);
    }

    public void stopNotifications(DeviceId device) {
        try {
            periodicRunnableTable.get(device).terminate();
            processorRunnableTable.get(device).terminate();
        } catch (Exception ex) {
            log.error("stopNotifications::Exception happened when terminating, ex: {}", ex);
        }
        log.info("stopNotifications::Runnable is now terminated");
        periodicRunnableTable.remove(device);
        processorRunnableTable.remove(device);
        if (periodicRunnableTable.isEmpty()) {
            log.info("stopNotifications::periodicRunnableTable is empty. Going to shutdown the executors");
            this.deactivate();
            log.info("stopNotifications::Executors are now shutdown.");
        }
        log.info("stopNotifications::Stop sending notifications for device URI: " + device.uri().toString());
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
                ctx.setAttribute("subscriptionId", subscriptionId);
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
            stopNotifications(dev.deviceId());
            subscribedDevicesTable.remove(dev.deviceId());

            String id = getSubscriptionId(paramMap.get(SUBSCRIBER_ID));
            if (id != null) {
                subscriptionInfoMap.remove(id);
            }
        } else {
            log.warn("deleteSubscriptionAndSseConnection::This device has already been unsubscribed");
        }
    }

    public class PeriodicPullRunnable implements Runnable {
        private String request;
        private DeviceId deviceId;
        private Map<String, String> paramMap;
        private SvcLogicContext ctx;

        private volatile boolean running = true;

        public void terminate() {
            log.info("PeriodicPullRunnable.terminate()::threadID: {}",
                    Thread.currentThread().getId());
            running = false;
        }

        /**
         * @param request   request
         * @param deviceId    device identifier
         * @param paramMap
         * @param ctx
         */
        public PeriodicPullRunnable(String request, DeviceId deviceId, Map<String, String> paramMap, SvcLogicContext ctx) {
            this.request = request;
            this.deviceId = deviceId;
            this.paramMap = paramMap;
            this.ctx = ctx;
        }

        @Override
        public void run() {
            Parameters p;
            WebTarget target = null;

            log.info("PeriodicPullRunnable.run()::threadID is: {} ...., running is: {}",
                    Thread.currentThread().getId(), running);
            try {
//                    Client client = ClientBuilder.newBuilder().build();
//                    WebTarget target = client.target(getUrlString(deviceId, request));


                    log.info("PeriodicPullRunnable::sending periodic GET pm-data request to hardware");
                    RestapiCallNode restapi = restconfApiCallNode.getRestapiCallNode();
                    p = RestapiCallNode.getParameters(paramMap, new Parameters());
    //                Client client =  ignoreSslClient(p.disableHostVerification).register(SseFeature.class);
                    Client client = ClientBuilder.newBuilder().build();
                    target = restapi.addAuthType(client, p).target(getUrlString(deviceId, request));
//                    target = restapi.addAuthType(client, p).target(request);


                    log.info("PeriodicPullRunnable.run()::target URI is {}", target.getUri().toString());
                    Response response = null;
                    if (running) {
                        response = target.request().get();
                        String rcvdData = response.readEntity(String.class);
                        if (response.getStatus() == 200) {
                            log.info("PeriodicPullRunnable.run()::after readEntity");
                            BlockingQueue<String> eventQ = getEventQ(deviceId);
                            if (eventQ != null) {
                                eventQ.add(rcvdData);
                                eventQMap.put(deviceId, eventQ);
                                log.info("PeriodicPullRunnable.run()::eventQ got filled.");
                            } else {
                                log.error("PeriodicPullRunnable.run()::eventQ has not been initialized for this device {}",
                                        deviceId);
                            }
                        } else {
                            log.info("PeriodicPullRunnable.run():: GET pm-data did NOT return 200: {}", rcvdData);
                            log.info("PeriodicPullRunnable.run():: Status code is: {}", response.getStatus());
                            log.info("PeriodicPullRunnable.run():: response is: {}", response.toString());
                        }
                    } else {
                        log.info("PeriodicPullRunnable.run()::running is false! " +
                                "closing the client and the response, threadID: {}", Thread.currentThread().getId());
                        response.close();
                        client.close();
                        log.info("PeriodicPullRunnable.run()::eventInput is closed in run()");
                    }
            } catch (Exception ex) {
                log.info("PeriodicPullRunnable.run()::We got some exception: {}, threadID: {} ", ex,
                        Thread.currentThread().getId());
                executor.shutdown();
                scheduledExecutor.shutdown();
                log.info("PeriodicPullRunnable.run():: exceptions happened. So shutting down the executors");
            }
            log.info("PeriodicPullRunnable.run()::after Runnable Try Catch. threadID: {} ",
                    Thread.currentThread().getId());
        }

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

    public class InternalPeriodicPullingProcessorRunnable implements Runnable {

        private volatile boolean running = true;
        private DeviceId deviceId;

        public InternalPeriodicPullingProcessorRunnable(DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        public void terminate() {
            log.info("InternalPeriodicPullingProcessorRunnable.terminate()::threadID: {}",
                    Thread.currentThread().getId());
            running = false;
        }

        @Override
        public void run() {
            log.info("InternalPeriodicPullingProcessorRunnable::restconf event processor runnable inside run()");
            while (running) {
                try {
                    if (eventQMap != null && !eventQMap.isEmpty() && eventQMap.get(deviceId) != null) {
                        log.info("InternalPeriodicPullingProcessorRunnable::waiting for take()");
                        if (running) {
                            String eventJsonString = eventQMap.get(deviceId).take();
                            log.info("InternalPeriodicPullingProcessorRunnable::after take()");
                            log.info("InternalPeriodicPullingProcessorRunnable::eventJsonString is {}", eventJsonString);
                            Map<String, String> param = convertToProperties(eventJsonString);
//                            String idString = param.get("push-change-update.subscription-id");
                            String idString = getSubscriptionIdFromDeviceId(deviceId);
                            log.info("InternalPeriodicPullingProcessorRunnable::idString is {}", idString);
                            SubscriptionInfo info = subscriptionInfoMap().get(idString);
                            if (info != null) {
                                log.info("InternalPeriodicPullingProcessorRunnable::subscriptionInfo is not null; going to call the callback dg");
                                SvcLogicContext ctx = setContext(param);
                                SvcLogicGraphInfo callbackDG = info.callBackDG();
                                callbackDG.executeGraph(ctx);
                                log.info("InternalPeriodicPullingProcessorRunnable::The callback dg is called");
                            }
                        } else {
                            log.info("InternalPeriodicPullingProcessorRunnable.run()::running has changed to false " +
                                    "while eventQ was blocked to process new notifications");
                            log.info("InternalPeriodicPullingProcessorRunnable.run()::" +
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
    }

    @Override
    public void removeNotificationListener(DeviceId deviceId,
                                           RestconfNotificationEventListener listener) {
    }

    public boolean isNotificationEnabled(DeviceId deviceId) {
        return periodicRunnableTable.containsKey(deviceId);
    }

}
