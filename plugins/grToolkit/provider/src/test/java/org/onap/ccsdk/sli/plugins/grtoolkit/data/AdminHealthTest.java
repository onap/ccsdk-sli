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

public class AdminHealthTest {
    @Test
    public void constructorTest() {
        AdminHealth health = new AdminHealth(Health.HEALTHY);
        assertEquals(Health.HEALTHY, health.getHealth());
    }

    @Test
    public void constructor2Test() {
        AdminHealth health = new AdminHealth(Health.FAULTY, 500);
        assertEquals(Health.FAULTY, health.getHealth());
        assertEquals(500, health.getStatusCode());
    }

    @Test
    public void setHealth() {
        AdminHealth health = new AdminHealth(Health.HEALTHY, 201);
        assertEquals(Health.HEALTHY, health.getHealth());
        assertEquals(201, health.getStatusCode());
        health.setHealth(Health.FAULTY);
        assertEquals(Health.FAULTY, health.getHealth());
    }

    @Test
    public void setStatusCode() {
        AdminHealth health = new AdminHealth(Health.HEALTHY, 200);
        assertEquals(Health.HEALTHY, health.getHealth());
        assertEquals(200, health.getStatusCode());
        health.setStatusCode(409);
        assertEquals(409, health.getStatusCode());
    }
}