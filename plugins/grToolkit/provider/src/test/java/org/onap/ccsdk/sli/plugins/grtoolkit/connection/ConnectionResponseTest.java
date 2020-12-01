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

import org.junit.Test;

import static org.junit.Assert.*;

public class ConnectionResponseTest {
    @Test
    public void constructorTest() {
        ConnectionResponse response = new ConnectionResponse();
        assertNotNull(response);
        assertEquals(0, response.statusCode);
        assertNull(response.content);
        assertTrue(response.toString().length() > 0);
    }
    @Test
    public void withStatusCode() {
        ConnectionResponse response = new ConnectionResponse().withStatusCode(123);
        assertEquals(123, response.statusCode);
    }
}