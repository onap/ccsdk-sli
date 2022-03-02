package org.onap.ccsdk.sli.plugins.restconfdiscovery;

import java.util.Optional;

public interface RestSBDevice {

    /**
     * Returns the ip of this device.
     *
     * @return ip
     */
    String ip();

    /**
     * Returns the password of this device.
     *
     * @return port
     */
    int port();

    /**
     * Returns the username of this device.
     *
     * @return username
     */
    String username();

    /**
     * Returns the password of this device.
     *
     * @return password
     */
    String password();

    /**
     * Returns the ONOS deviceID for this device.
     *
     * @return DeviceId
     */
    DeviceId deviceId();

    /**
     * Sets or unsets the state of the device.
     *
     * @param active boolean
     */
    void setActive(boolean active);

    /**
     * Returns the state of this device.
     *
     * @return state
     */
    boolean isActive();

    /**
     * Returns the protocol for the REST request, usually HTTP o HTTPS.
     *
     * @return protocol
     */
    String protocol();

    /**
     * Returns the url for the REST requests, to be used instead of IP and PORT.
     *
     * @return url
     */
    String url();

    /**
     * Returns the proxy state of this device
     * (if true, the device is proxying multiple ONOS devices).
     * @return proxy state
     */
    boolean isProxy();

    /**
     * Returns the url for the REST TEST requests.
     *
     * @return testUrl
     */
    Optional<String> testUrl();

    /**
     * The manufacturer of the rest device.
     *
     * @return the name of the manufacturer
     */
    Optional<String> manufacturer();

    /**
     * The hardware version of the rest device.
     *
     * @return the hardware version
     */
    Optional<String> hwVersion();

    /**
     * The software version of rest device.
     *
     * @return the software version.
     */
    Optional<String> swVersion();
}
