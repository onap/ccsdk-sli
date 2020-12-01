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

package org.onap.ccsdk.sli.plugins.grtoolkit.data;

/**
 * A data container for the status of a controller-level failover.
 *
 * @author Anthony Haddox
 * @see org.onap.ccsdk.sli.plugins.grtoolkit.resolver.HealthResolver
 */
public class FailoverStatus {
    private int statusCode;
    private String message;

    public FailoverStatus() {
        this.statusCode = 200;
        this.message = "Failover complete.";
    }

    public FailoverStatus withStatusCode(int code) {
        this.statusCode = code;
        return this;
    }

    public FailoverStatus withMessage(String message) {
        this.message = message;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
