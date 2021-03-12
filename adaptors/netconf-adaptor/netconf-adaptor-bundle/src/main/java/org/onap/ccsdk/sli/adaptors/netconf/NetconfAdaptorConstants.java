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

package org.onap.ccsdk.sli.adaptors.netconf;

public class NetconfAdaptorConstants {

    public static final String CONFIGURE_PATH = "/restconf/config/opendaylight-inventory:nodes/node/";
    public static final String CONNECT_PATH = "/restconf/config/opendaylight-inventory:nodes/node/controller-config/yang-ext:mount/config:modules";
    public static final String CHECK_CONNECTION_PATH = "/restconf/operational/opendaylight-inventory:nodes/node/";
    public static final String DISCONNECT_PATH = "/restconf/config/opendaylight-inventory:nodes/node/controller-config/yang-ext:mount/config:modules/module/odl-sal-netconf-connector-cfg:sal-netconf-connector/";

    public static final String CONTROLLER_IP = "127.0.0.1";
    public static final int CONTROLLER_PORT = 8181;
    public static final String PROTOCOL = "http";

    // tables and fields
    public static final String NETCONF_SCHEMA = "sdnctl";
    public static final String DEVICE_AUTHENTICATION_TABLE_NAME = "DEVICE_AUTHENTICATION";
    public static final String CONFIGFILES_TABLE_NAME = "CONFIGFILES";
    public static final String DEVICE_INTERFACE_LOG_TABLE_NAME = "DEVICE_INTERFACE_LOG";
    public static final String FILE_CONTENT_TABLE_FIELD_NAME = "FILE_CONTENT";
    public static final String FILE_NAME_TABLE_FIELD_NAME = "FILE_NAME";
    public static final String USER_NAME_TABLE_FIELD_NAME = "USER_NAME";
    public static final String PASSWORD_TABLE_FIELD_NAME = "PASSWORD";
    public static final String VM_HOST_TABLE_FIELD_NAME = "VM_HOST";
    public static final String VM_NAME_TABLE_FIELD_NAME = "VM_NAME";
    public static final String PORT_NUMBER_TABLE_FIELD_NAME = "PORT_NUMBER";
    public static final String VNF_TYPE_TABLE_FIELD_NAME = "VNF_TYPE";
    public static final String SERVICE_INSTANCE_ID_FIELD_NAME = "SERVICE_INSTANCE_ID";
    public static final String REQUEST_ID_FIELD_NAME = "REQUEST_ID";
    public static final String CREATION_DATE_FIELD_NAME = "CREATION_DATE";
    public static final String LOG_FIELD_NAME = "LOG";

    // input fields names
    public static final String VNF_TYPE_FIELD_NAME = "org.onap.appc.vftype";
    public static final String TARGET_VNF_TYPE = "target-vnf-type";
    public static final String FILE_CONTENT_FIELD_NAME = "file-content";
    public static final String CONNECTION_DETAILS_FIELD_NAME = "connection-details";
    public static final String CONFIGURATION_FILE_FIELD_NAME = "configuration-file-name";
    public static final String VNF_HOST_IP_ADDRESS_FIELD_NAME = "vnf-host-ip-address";
    public static final String DG_ERROR_FIELD_NAME = "org.onap.appc.dg.error";
    public static final String RESOURCEKEY = "resourceKey";
    public static final String ATTRIBUTE_ERROR_MESSAGE = "error-message";
    public static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";

    private NetconfAdaptorConstants() {
    }
}
