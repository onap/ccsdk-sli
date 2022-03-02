package org.onap.ccsdk.sli.plugins.restconfdiscovery;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

public class DefaultRestSBDevice implements RestSBDevice{
    private static final String REST = "rest";
    private static final String COLON = ":";
    private final String ip;
    private final int port;
    private final String username;
    private final String password;
    private boolean isActive;
    private String protocol;
    private String url;
    private boolean isProxy;
    private final Optional<String> testUrl;
    private final Optional<String> manufacturer;
    private final Optional<String> hwVersion;
    private final Optional<String> swVersion;

    public DefaultRestSBDevice(String ip, int port, String name, String password,
                               String protocol, String url, boolean isActive) {
        this(ip, port, name, password, protocol, url, isActive, "", "", "", "");
    }

    public DefaultRestSBDevice(String ip, int port, String name, String password,
                               String protocol, String url, boolean isActive, String testUrl, String manufacturer,
                               String hwVersion,
                               String swVersion) {
        Preconditions.checkNotNull(ip, "IP address cannot be null");
        Preconditions.checkArgument(port > 0, "Port address cannot be negative");
        Preconditions.checkNotNull(protocol, "protocol address cannot be null");
        this.ip = ip;
        this.port = port;
        this.username = name;
        this.password = StringUtils.isEmpty(password) ? null : password;
        this.isActive = isActive;
        this.protocol = protocol;
        this.url = StringUtils.isEmpty(url) ? null : url;
        this.manufacturer = StringUtils.isEmpty(manufacturer) ?
                Optional.empty() : Optional.ofNullable(manufacturer);
        this.hwVersion = StringUtils.isEmpty(hwVersion) ?
                Optional.empty() : Optional.ofNullable(hwVersion);
        this.swVersion = StringUtils.isEmpty(swVersion) ?
                Optional.empty() : Optional.ofNullable(swVersion);
        this.testUrl = StringUtils.isEmpty(testUrl) ?
                Optional.empty() : Optional.ofNullable(testUrl);
        if (this.manufacturer.isPresent()
                && this.hwVersion.isPresent()
                && this.swVersion.isPresent()) {
            this.isProxy = true;
        } else {
            this.isProxy = false;
        }
    }

    @Override
    public String ip() {
        return ip;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public DeviceId deviceId() {
        try {
            return DeviceId.deviceId(new URI(REST, ip + COLON + port, null));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot create deviceID " +
                    REST + COLON + ip +
                    COLON + port, e);
        }
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public boolean isProxy() {
        return isProxy;
    }

    @Override
    public Optional<String> testUrl() {
        return testUrl;
    }

    @Override
    public Optional<String> manufacturer() {
        return manufacturer;
    }

    @Override
    public Optional<String> hwVersion() {
        return hwVersion;
    }

    @Override
    public Optional<String> swVersion() {
        return swVersion;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("url", url)
                .add("testUrl", testUrl)
                .add("protocol", protocol)
                .add("username", username)
                .add("port", port)
                .add("ip", ip)
                .add("manufacturer", manufacturer.orElse(null))
                .add("hwVersion", hwVersion.orElse(null))
                .add("swVersion", swVersion.orElse(null))
                .toString();

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RestSBDevice)) {
            return false;
        }
        RestSBDevice device = (RestSBDevice) obj;
        return this.username.equals(device.username()) && this.ip.equals(device.ip()) &&
                this.port == device.port();

    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
