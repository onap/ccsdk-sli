/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.utils.logging;

import java.util.Arrays;
import java.util.List;

/**
 * Constant definition of logging
 */
public class LoggingConstants {
    private LoggingConstants() {
        throw new IllegalAccessError("LoggingConstants");
    }

    /**
     * Constants of MDC property keys
     */
    public static class MDCKeys {
        private MDCKeys() {
            throw new IllegalAccessError("MDCKeys");
        }

        public static final String REQUEST_ID = "RequestID";
        public static final String INSTANCE_ID = "InstanceUUID";
        public static final String SERVICE_INSTANCE_ID = "ServiceInstanceID";
        public static final String ERROR_CODE = "ErrorCode";
        public static final String ERROR_DESCRIPTION = "ErrorDescription";
        public static final String STATUS_CODE = "StatusCode";
        public static final String RESPONSE_CODE = "ResponseCode";
        public static final String RESPONSE_DESCRIPTION = "ResponseDescription";
        public static final String TARGET_ENTITY = "TargetEntity";
        public static final String TARGET_SERVICE_NAME = "TargetServiceName";
        public static final String PARTNER_NAME = "PartnerName";
        public static final String SERVER_NAME = "ServerName";
        public static final String SERVICE_NAME = "ServiceName";
        public static final String BEGIN_TIMESTAMP = "BeginTimestamp";
        public static final String END_TIMESTAMP = "EndTimestamp";
        public static final String ELAPSED_TIME = "ElapsedTime";
        public static final String MARKER = "Marker";
        public static final String METRIC_MARKER = "MetricMarker";
        public static final String MDC_STRING = "MDC";
        public static final String CUSTOM_FIELD1 = "CustomField1";
        public static final String CUSTOM_FIELD2 = "CustomField2";
        public static final String CLIENT_IP_ADDRESS = "ClientIPAddress";
        public static final String PROCESS_KEY = "ProcessKey";
        public static final String CLASS_NAME = "ClassName";
        public static final String TARGET_VIRTUAL_ENTITY = "TargetVirtualEntity";
        public static final String INVOCATION_ID = "InvocationID";

        public static final List<String> MDC_KEYS = Arrays.asList(
        	BEGIN_TIMESTAMP, END_TIMESTAMP, REQUEST_ID, SERVICE_INSTANCE_ID,
        	ERROR_CODE, SERVER_NAME, SERVICE_NAME, PARTNER_NAME, STATUS_CODE,
        	RESPONSE_CODE, RESPONSE_DESCRIPTION, INSTANCE_ID,
        	ERROR_DESCRIPTION, MARKER, PROCESS_KEY, INVOCATION_ID
        );
    }

    /**
     * Constants of status code values
     */
    public static class StatusCodes {
        private StatusCodes() {
            throw new IllegalAccessError("StatusCodes");
        }
        public static final String COMPLETE = "COMPLETE";
        public static final String ERROR = "ERROR";
    }

    /**
     * Constants of APPC target names
     */
    public static class TargetNames {
        private TargetNames() {
            throw new IllegalAccessError("TargetNames");
        }
        public static final String APPC = "APPC";
        public static final String AAI = "A&AI";
        public static final String DB = "DataBase";
        public static final String APPC_PROVIDER = "APPC Provider";
        public static final String APPC_OAM_PROVIDER = "APPC OAM Provider";
        public static final String STATE_MACHINE = "StateMachine";
        public static final String WORKFLOW_MANAGER = "WorkflowManager";
        public static final String REQUEST_VALIDATOR = "RequestValidator";
        public static final String LOCK_MANAGER = "LockManager";
        public static final String REQUEST_HANDLER = "RequestHandler";
    }

    /**
     * Constants of targeted service names
     */
    public static class TargetServiceNames {
        private TargetServiceNames() {
            throw new IllegalAccessError("TargetServiceNames");
        }

        /**
         * Constants of AAI service names
         */
        public static class AAIServiceNames {
            private AAIServiceNames() {
                throw new IllegalAccessError("AAIServiceNames");
            }
            public static final String QUERY = "/aai/v14/network/generic-vnfs/generic-vnf";
            public static final String GET_VNF_DATA = "getVnfData";
        }

    }

}
