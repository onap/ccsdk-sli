package org.onap.ccsdk.sli.plugins.restconfdiscovery;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class RestconfNotificationEventListenerImpl implements
        RestconfNotificationEventListener<String> {

    private final Logger log = getLogger(getClass());
    SubscriptionInfo info;

    public RestconfNotificationEventListenerImpl(SubscriptionInfo info) {
        this.info = info;
    }

    @Override
    public void handleNotificationEvent(DeviceId deviceId, String eventJsonString) {
        log.info("New notification: {} for device: {}",
                eventJsonString, deviceId.toString());
    }
}
