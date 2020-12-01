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

import org.junit.Test;

import static org.junit.Assert.*;

public class FailoverStatusTest {
    @Test
    public void constructorTest() {
        FailoverStatus status = new FailoverStatus();
        assertEquals(200, status.getStatusCode());
        assertEquals("Failover complete.", status.getMessage());
    }
    @Test
    public void withStatusCode() {
        FailoverStatus status = new FailoverStatus().withStatusCode(500);
        assertEquals(500, status.getStatusCode());
    }

    @Test
    public void withMessage() {
        FailoverStatus status = new FailoverStatus().withMessage("Test");
        assertEquals("Test", status.getMessage());
    }

    @Test
    public void setStatusCode() {
        FailoverStatus status = new FailoverStatus();
        status.setStatusCode(500);
        assertEquals(500, status.getStatusCode());
    }

    @Test
    public void setMessage() {
        FailoverStatus status = new FailoverStatus();
        status.setMessage("Test");
        assertEquals("Test", status.getMessage());
    }
}