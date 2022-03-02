package org.onap.ccsdk.sli.plugins.restconfdiscovery;

import java.util.Map;

/**
 * Abstraction of a RESTCONF controller. Serves as a one stop shop for obtaining
 * RESTCONF southbound devices and (un)register listeners.
 */
public interface RestConfSBController {

    /**
     * Returns all the devices known to this controller.
     *
     * @return map of devices
     */
    Map<DeviceId, RestSBDevice> getDevices();

    /**
     * Returns a device by node identifier.
     *
     * @param deviceInfo node identifier
     * @return RestSBDevice rest device
     */
    RestSBDevice getDevice(DeviceId deviceInfo);

    /**
     * Returns a device by Ip and Port.
     *
     * @param ip device ip
     * @param port device port
     * @return RestSBDevice rest device
     */
    RestSBDevice getDevice(String ip, int port);

    /**
     * Adds a device to the device map.
     *
     * @param device to be added
     */
    void addDevice(RestSBDevice device);

    /**
     * Removes the device from the devices map.
     *
     * @param deviceId to be removed
     */
    void removeDevice(DeviceId deviceId);

    /**
     * This method is to be called by whoever is interested to receive
     * Notifications from a specific device. It does a REST GET request
     * with specified parameters to the device, and calls the provided
     * callBackListener upon receiving notifications to notify the requester
     * about notifications.
     *
     * @param device           device to make the request to
     * @param request          url of the request
     * @param mediaType        format to retrieve the content in
     * @param callBackListener method to call when notifications arrives
     */
    void enableNotifications(DeviceId device, String request, String mediaType,
                             RestconfNotificationEventListener callBackListener);

    /**
     * Registers a listener for notification events that occur to restconf
     * devices.
     *
     * @param deviceId identifier of the device to which the listener is attached
     * @param listener the listener to notify
     */
    void addNotificationListener(DeviceId deviceId,
                                 RestconfNotificationEventListener listener);

    /**
     * Unregisters the listener for the device.
     *
     * @param deviceId identifier of the device for which the listener
     *                 is to be removed
     * @param listener listener to be removed
     */
    void removeNotificationListener(DeviceId deviceId,
                                    RestconfNotificationEventListener listener);

    /**
     * Returns true if a listener has been installed to listen to RESTCONF
     * notifications sent from a particular device.
     *
     * @param deviceId identifier of the device from which the notifications
     *                 are generated
     * @return true if listener is installed; false otherwise
     */
    boolean isNotificationEnabled(DeviceId deviceId);
}
