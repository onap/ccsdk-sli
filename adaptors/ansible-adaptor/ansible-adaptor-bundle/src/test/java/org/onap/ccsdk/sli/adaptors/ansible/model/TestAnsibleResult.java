/*-
 * ============LICENSE_START=======================================================
 * ONAP : SLI
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ansible.model;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;

import static org.junit.Assert.assertEquals;

public class TestAnsibleResult {
    private AnsibleResult ansibleResult;

    @Before
    public void setUp() {
        ansibleResult = new AnsibleResult(10, "message", "result", "outputData");
    }

    @Test
    public void testServerIp() {
        ansibleResult.setServerIp("10.0.9.87");
        assertEquals("10.0.9.87", ansibleResult.getServerIp());
    }

}
