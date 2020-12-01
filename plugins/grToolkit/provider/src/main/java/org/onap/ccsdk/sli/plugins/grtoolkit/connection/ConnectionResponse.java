/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.plugins.grtoolkit.connection;

/**
 * A data container for HTTP connection requests.
 *
 * @author Anthony Haddox
 * @see ConnectionManager
 */
public class ConnectionResponse {
    public int statusCode;
    public String content;

    public ConnectionResponse withStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public String toString() {
        return "Status: " + statusCode + "\nContent: " + content;
    }
}
