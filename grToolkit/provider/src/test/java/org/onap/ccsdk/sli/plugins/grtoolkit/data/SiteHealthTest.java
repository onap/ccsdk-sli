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

public class SiteHealthTest {
    @Test
    public void constructorTest() {
        SiteHealth health = new SiteHealth();
        assertNotNull(health.getAdminHealth());
        assertNotNull(health.getDatabaseHealth());
        assertNotNull(health.getClusterHealth());
        assertEquals(Health.FAULTY, health.getHealth());
    }
    @Test
    public void withAdminHealth() {
        SiteHealth health = new SiteHealth().withAdminHealth(new AdminHealth(Health.HEALTHY));
        assertEquals(Health.HEALTHY, health.getAdminHealth().get(0).getHealth());
    }

    @Test
    public void withDatabaseHealth() {
        SiteHealth health = new SiteHealth().withDatabaseHealth(new DatabaseHealth(Health.HEALTHY));
        assertEquals(Health.HEALTHY, health.getDatabaseHealth().get(0).getHealth());
    }

    @Test
    public void withClusterHealth() {
        SiteHealth health = new SiteHealth().withClusterHealth(new ClusterHealth());
        assertEquals(Health.FAULTY, health.getClusterHealth().get(0).getHealth());
    }

    @Test
    public void withId() {
        SiteHealth health = new SiteHealth().withId("My_ID");
        assertEquals("My_ID", health.getId());
    }

    @Test
    public void withRole() {
        SiteHealth health = new SiteHealth().withRole("My_role");
        assertEquals("My_role", health.getRole());
    }

    @Test
    public void setHealth() {
        SiteHealth health = new SiteHealth();
        health.setHealth(Health.HEALTHY);
        assertEquals(Health.HEALTHY, health.getHealth());
    }

    @Test
    public void setAdminHealth() {
        SiteHealth health = new SiteHealth().withAdminHealth(new AdminHealth(Health.HEALTHY));
        health.setAdminHealth(null);
        assertNull(health.getAdminHealth());
    }

    @Test
    public void setDatabaseHealth() {
        SiteHealth health = new SiteHealth().withDatabaseHealth(new DatabaseHealth(Health.HEALTHY));
        health.setDatabaseHealth(null);
        assertNull(health.getDatabaseHealth());
    }

    @Test
    public void setClusterHealth() {
        SiteHealth health = new SiteHealth().withClusterHealth(new ClusterHealth());
        health.setClusterHealth(null);
        assertNull(health.getClusterHealth());
    }

    @Test
    public void setId() {
        SiteHealth health = new SiteHealth().withId("My_ID");
        health.setId("My_new_ID");
        assertEquals("My_new_ID", health.getId());
    }

    @Test
    public void setRole() {
        SiteHealth health = new SiteHealth().withRole("My_role");
        health.setRole("My_new_role");
        assertEquals("My_new_role", health.getRole());
    }
}