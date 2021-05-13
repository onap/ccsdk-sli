/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.openstack.heat.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"status", "name", "stack_user_project_id", "environment", "template", "action", "project_id", "id", "resources"})
public class Data {

    @JsonProperty("status")
    private String status;
    @JsonProperty("name")
    private String name;
    @JsonProperty("stack_user_project_id")
    private String stackUserProjectId;
    @JsonProperty("environment")
    private Environment environment;
    @JsonProperty("template")
    private Template template;
    @JsonProperty("action")
    private String action;
    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("id")
    private String id;
    @JsonProperty("resources")
    private Resources__ resources;

    /**
     * @return The status
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The stackUserProjectId
     */
    @JsonProperty("stack_user_project_id")
    public String getStackUserProjectId() {
        return stackUserProjectId;
    }

    /**
     * @param stackUserProjectId The stack_user_project_id
     */
    @JsonProperty("stack_user_project_id")
    public void setStackUserProjectId(String stackUserProjectId) {
        this.stackUserProjectId = stackUserProjectId;
    }

    /**
     * @return The environment
     */
    @JsonProperty("environment")
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * @param environment The environment
     */
    @JsonProperty("environment")
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * @return The template
     */
    @JsonProperty("template")
    public Template getTemplate() {
        return template;
    }

    /**
     * @param template The template
     */
    @JsonProperty("template")
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * @return The action
     */
    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    /**
     * @param action The action
     */
    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return The projectId
     */
    @JsonProperty("project_id")
    public String getProjectId() {
        return projectId;
    }

    /**
     * @param projectId The project_id
     */
    @JsonProperty("project_id")
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * @return The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The resources
     */
    @JsonProperty("resources")
    public Resources__ getResources() {
        return resources;
    }

    /**
     * @param resources The resources
     */
    @JsonProperty("resources")
    public void setResources(Resources__ resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
