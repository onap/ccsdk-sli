/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modification Copyright (C) 2019 IBM
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

package org.onap.ccsdk.sli.adaptors.iaas;

/**
 * @since September 26, 2016
 */
public class Property {

    public static final String PROVIDER = "provider";
    public static final String PROVIDER_IDENTITY = "identity";
    public static final String PROVIDER_TENANT = "tenant";
    public static final String PROVIDER_TENANT_DOMAIN = "domain";
    public static final String PROVIDER_TENANT_NAME = "name";
    public static final String PROVIDER_TENANT_PASSWORD = "password";
    public static final String PROVIDER_TENANT_USERID = "userid";
    public static final String PROVIDER_TYPE = "type";
    public static final String SKIP_HYPERVISOR_CHECK = "org.onap.appc.iaas.skiphypervisorcheck";
    public static final String PAYLOAD = "org.onap.appc.payload";

    private Property() {
    }

}
