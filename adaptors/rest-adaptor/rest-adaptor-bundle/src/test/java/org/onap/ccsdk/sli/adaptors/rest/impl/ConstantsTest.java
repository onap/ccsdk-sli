/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Samsung Electronics. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.rest.impl;

import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.rest.Constants;

import static org.junit.Assert.assertTrue;

public class ConstantsTest {
    @Test
    public void testContext() {
        assertTrue("error_code" == Constants.ATTRIBUTE_ERROR_CODE
                && "error-message" == Constants.ATTRIBUTE_ERROR_MESSAGE
                && "org.onap.rest.result.message".equals(Constants.CONTEXT_ERROR_MESSAGE)
                && "org.onap.rest.agent.result.message".equals(Constants.CONTEXT_AGENT_ERROR_MESSAGE)
                && "org.onap.rest.result.code".equals(Constants.CONTEXT_ERROR_CODE)
                && "org.onap.rest.agent.result.code".equals(Constants.CONTEXT_AGENT_ERROR_CODE)
                && "success-message".equals(Constants.ATTRIBUTE_SUCCESS_MESSAGE)
                && "SvcLogic.status".equals(Constants.DG_ATTRIBUTE_STATUS)
                && "output.status.code".equals(Constants.DG_OUTPUT_STATUS_CODE)
                && "output.status.message".equals(Constants.DG_OUTPUT_STATUS_MESSAGE)
        );
    }

    @Test
    public void testYang() {
        assertTrue( "YYYY-MM-DD" == Constants.YANG_REVISION_FORMAT
                && "vnf-config-repo".equals(Constants.YANG_BASE_CONTAINER)
                && "vnf-config-list".equals(Constants.YANG_VNF_CONFIG_LIST)
                && "vnf-config".equals(Constants.YANG_VNF_CONFIG)
        );
    }

    @Test
    public void testStatus() {
        assertTrue( "status-getter" == Constants.STATUS_GETTER
                && "fusion-vm-status-getter".equals(Constants.VM_FUSION_STATUS_GETTER)
                && "status-vm".equals(Constants.STATUS_OF_VM)
        );
    }
}
