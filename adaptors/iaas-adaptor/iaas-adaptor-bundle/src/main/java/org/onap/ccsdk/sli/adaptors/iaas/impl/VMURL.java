/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to parse the VM URL returned from OpenStack and extract all of the constituent parts.
 */
public class VMURL {

    /**
     * The regular expression pattern used to parse the URL. Capturing groups are used to identify and extract the
     * various component parts of the URL.
     */
    private static Pattern pattern =
            Pattern.compile("(\\p{Alnum}+)://([^/:]+)(?::([0-9]+))?(/.*)?/(v[0-9\\.]+)/([^/]+)/servers/([^/]+)");

    /**
     * The URL scheme or protocol, such as HTTP or HTTPS
     */
    private String scheme;

    /**
     * The host name or ip address
     */
    private String host;

    /**
     * The path, or null if no path is defined
     */
    private String path;

    /**
     * The port number, or null if no port is defined
     */
    private String port;

    /**
     * The tenant UUID
     */
    private String tenantId;

    /**
     * The server UUID
     */
    private String serverId;

    /**
     * The version of the service
     */
    private String version;

    /**
     * A private default constructor prevents instantiation by any method other than the factory method
     *
     * @see #parseURL(String)
     */
    private VMURL() {

    }

    /**
     * This static method is used to parse the provided server URL string and return a parse results object (VMURL)
     * which represents the state of the parse.
     *
     * @param serverUrl The server URL to be parsed
     * @return The VMURL parse results object, or null if the URL was not valid or null.
     */
    public static VMURL parseURL(String serverUrl) {
        VMURL obj = null;
        if (serverUrl != null) {
            Matcher matcher = pattern.matcher(serverUrl.trim());
            if (matcher.matches()) {
                obj = new VMURL();
                obj.scheme = matcher.group(1);
                obj.host = matcher.group(2);
                obj.port = matcher.group(3);
                obj.path = matcher.group(4);
                obj.version = matcher.group(5);
                obj.tenantId = matcher.group(6);
                obj.serverId = matcher.group(7);
            }
        }

        return obj;
    }

    /**
     * @return The URL scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @return The URL host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return THe URL path, or null if no path was defined
     */
    public String getPath() {
        return path;
    }

    /**
     * @return The URL port, or null if no port was defined
     */
    public String getPort() {
        return port;
    }

    /**
     * @return The tenant id
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @return The server ID
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * @return The version of the service
     */
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(scheme + "://" + host);
        if (port != null) {
            str.append(":" + port);
        }
        if (path != null) {
            str.append(path);
        }
        str.append("/" + version + "/" + tenantId + "/servers/" + serverId);
        return str.toString();
    }

}
