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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A data container for Site health.
 *
 * @author Anthony Haddox
 * @see org.onap.ccsdk.sli.plugins.grtoolkit.resolver.HealthResolver
 */
public class SiteHealth {
    private List<AdminHealth> adminHealth;
    private List<DatabaseHealth> databaseHealth;
    private List<ClusterHealth> clusterHealth;

    private Health health;
    private String id;
    private String role;

    public SiteHealth() {
        adminHealth = new ArrayList<>();
        databaseHealth = new ArrayList<>();
        clusterHealth = new ArrayList<>();

        // Faulty by default, it's up to the health check to affirm the health
        health = Health.FAULTY;
    }

    public SiteHealth withAdminHealth(AdminHealth... health) {
        Collections.addAll(adminHealth, health);
        return this;
    }

    public SiteHealth withDatabaseHealth(DatabaseHealth... health) {
        Collections.addAll(databaseHealth, health);
        return this;
    }

    public SiteHealth withClusterHealth(ClusterHealth... health) {
        Collections.addAll(clusterHealth, health);
        return this;
    }

    public SiteHealth withId(String id) {
        this.id = id;
        return this;
    }

    public SiteHealth withRole(String role) {
        this.role = role;
        return this;
    }

    public Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public List<AdminHealth> getAdminHealth() {
        return adminHealth;
    }

    public void setAdminHealth(List<AdminHealth> adminHealth) {
        this.adminHealth = adminHealth;
    }

    public List<DatabaseHealth> getDatabaseHealth() {
        return databaseHealth;
    }

    public void setDatabaseHealth(List<DatabaseHealth> databaseHealth) {
        this.databaseHealth = databaseHealth;
    }

    public List<ClusterHealth> getClusterHealth() {
        return clusterHealth;
    }

    public void setClusterHealth(List<ClusterHealth> clusterHealth) {
        this.clusterHealth = clusterHealth;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
