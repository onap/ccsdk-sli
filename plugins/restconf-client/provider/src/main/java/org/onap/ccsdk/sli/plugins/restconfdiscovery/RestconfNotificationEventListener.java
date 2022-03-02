package org.onap.ccsdk.sli.plugins.restconfdiscovery;

/**
 * Notifies providers about incoming RESTCONF notification events.
 */
public interface RestconfNotificationEventListener<T> {

    /**
     * Handles the notification event.
     *
     * @param deviceId restconf device identifier
     * @param event    event payload
     */
    void handleNotificationEvent(DeviceId deviceId, T event);
}
