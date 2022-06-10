/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2022 Samsung Electronics
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ansible.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TestResultCodes {

    @Test
    public void testGetValuePass() {
        assertEquals(611, AnsibleResultCodes.IO_EXCEPTION.getValue());
    }

    @Test
    public void testGetValueFail() {
        assertNotEquals(612, AnsibleResultCodes.IO_EXCEPTION.getValue());
    }

    @Test
    public void testCheckValidCode() {
        assertTrue(AnsibleResultCodes.CODE.checkValidCode(0, 101));
        assertFalse(AnsibleResultCodes.CODE.checkValidCode(0, 201));
    }

    @Test
    public void testGetValidCodes() {
        assertEquals("[ 100,101,]", AnsibleResultCodes.CODE.getValidCodes(0));
    }

    @Test
    public void testCheckValidMessage() {
        assertTrue(AnsibleResultCodes.MESSAGE.checkValidMessage("PENDING"));
        assertFalse(AnsibleResultCodes.MESSAGE.checkValidMessage("INVALID"));
    }

    @Test
    public void testGetValidMessages() {
        assertEquals("[ TERMINATED,PENDING,FINISHED,]", AnsibleResultCodes.MESSAGE.getValidMessages());
    }
}
