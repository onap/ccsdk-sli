package org.onap.ccsdk.sli.plugins.restconfdiscovery;

import java.net.URI;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

public class DeviceId {
    /**
     * Represents either no device, or an unspecified device.
     */
    public static final DeviceId NONE = deviceId("none:none");

    private static final int DEVICE_ID_MAX_LENGTH = 1024;

    private final URI uri;
    private final String str;

    // Public construction is prohibited
    private DeviceId(URI uri) {
        this.uri = uri;
        this.str = uri.toString().toLowerCase();
    }


    // Default constructor for serialization
    protected DeviceId() {
        this.uri = null;
        this.str = null;
    }

    /**
     * Creates a device id using the supplied URI.
     *
     * @param uri device URI
     * @return DeviceId
     */
    public static DeviceId deviceId(URI uri) {
        return new DeviceId(uri);
    }

    /**
     * Creates a device id using the supplied URI string.
     *
     * @param string device URI string
     * @return DeviceId
     */
    public static DeviceId deviceId(String string) {
        checkArgument(string.length() <= DEVICE_ID_MAX_LENGTH,
                "deviceId exceeds maximum length " + DEVICE_ID_MAX_LENGTH);
        return deviceId(URI.create(string));
    }

    /**
     * Returns the backing URI.
     *
     * @return backing URI
     */
    public URI uri() {
        return uri;
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DeviceId) {
            final DeviceId that = (DeviceId) obj;
            return this.getClass() == that.getClass() &&
                    Objects.equals(this.str, that.str);
        }
        return false;
    }

    @Override
    public String toString() {
        return str;
    }

}
