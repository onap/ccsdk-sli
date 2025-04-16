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
public class SiteHealthData {
    private List<AdminHealthData> adminHealth;
    private List<DatabaseHealthData> databaseHealth;
    private List<ClusterHealthData> clusterHealth;

    private Health health;
    private String id;
    private String role;

    public SiteHealthData() {
        adminHealth = new ArrayList<>();
        databaseHealth = new ArrayList<>();
        clusterHealth = new ArrayList<>();

        // Faulty by default, it's up to the health check to affirm the health
        health = Health.FAULTY;
    }

    public SiteHealthData withAdminHealth(AdminHealthData... health) {
        Collections.addAll(adminHealth, health);
        return this;
    }

    public SiteHealthData withDatabaseHealth(DatabaseHealthData... health) {
        Collections.addAll(databaseHealth, health);
        return this;
    }

    public SiteHealthData withClusterHealth(ClusterHealthData... health) {
        Collections.addAll(clusterHealth, health);
        return this;
    }

    public SiteHealthData withId(String id) {
        this.id = id;
        return this;
    }

    public SiteHealthData withRole(String role) {
        this.role = role;
        return this;
    }

    public Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public List<AdminHealthData> getAdminHealth() {
        return adminHealth;
    }

    public void setAdminHealth(List<AdminHealthData> adminHealth) {
        this.adminHealth = adminHealth;
    }

    public List<DatabaseHealthData> getDatabaseHealth() {
        return databaseHealth;
    }

    public void setDatabaseHealth(List<DatabaseHealthData> databaseHealth) {
        this.databaseHealth = databaseHealth;
    }

    public List<ClusterHealthData> getClusterHealth() {
        return clusterHealth;
    }

    public void setClusterHealth(List<ClusterHealthData> clusterHealth) {
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
