/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2021 AT&T
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

 package org.onap.ccsdk.sli.core.utils.common;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * HostnameVerifier that accepts IP addresses without verification, but 
 * does default host name verification on true FQDNs
 */
public class AcceptIpAddressHostNameVerifier implements HostnameVerifier {

    public static final String DISABLE_HOSTNAME_VERIFICATION = "org.onap.ccsdk.host.verification.disable";

    public static final String IPV4_REGEX = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
    public static final String IPV6_HEX4DECCOMPRESSED_REGEX = "\\A((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?) ::((?:[0-9A-Fa-f]{1,4}:)*)(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
    public static final String IPV6_6HEX4DEC_REGEX = "\\A((?:[0-9A-Fa-f]{1,4}:){6,6})(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
    public static final String IPV6_HEXCOMPRESSED_REGEX = "\\A((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)\\z";
    public static final String IPV6_REGEX = "\\A(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\\z";

    boolean disableHostVerification = false;

    public AcceptIpAddressHostNameVerifier() {
        // Allow for host name verification to be disabled if
        // necessary (for example, if self-signed certificates must
        // be supported)
        String disableHostVerification = System.getProperty(DISABLE_HOSTNAME_VERIFICATION, "false");

        if ("true".equalsIgnoreCase(disableHostVerification)) {
            this.disableHostVerification = true;
        } else {
            this.disableHostVerification = false;
        }
    }

    public AcceptIpAddressHostNameVerifier(boolean disableHostVerification) {
        this.disableHostVerification = disableHostVerification;
    }
    
    @Override
    public boolean verify(String hostName, SSLSession session) {

        if (disableHostVerification) {
            return true;
        }

        // Null host name should never happen, but better to be safe
        if (hostName == null) {
            return false;
        }

        // If "hostName" is an IP address, accept it
        if (hostName.matches(IPV4_REGEX) ||
            hostName.matches(IPV6_REGEX) ||
            hostName.matches(IPV6_HEX4DECCOMPRESSED_REGEX) ||
            hostName.matches(IPV6_6HEX4DEC_REGEX) || 
            hostName.matches(IPV6_HEXCOMPRESSED_REGEX))
        {
            return true;
        }

        // Handle localhost as special case
        if (hostName.equals("localhost")) {
            return(true);
        }

        // Host name is not an IP address - perform default host
        // name verification.
        HostnameVerifier defaultHv = HttpsURLConnection.getDefaultHostnameVerifier();
        return(defaultHv.verify(hostName, session));
    }

    
}
