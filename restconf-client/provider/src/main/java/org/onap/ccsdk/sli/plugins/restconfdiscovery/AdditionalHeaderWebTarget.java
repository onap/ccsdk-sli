/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Modifications Copyright © 2018 IBM
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

package org.onap.ccsdk.sli.plugins.restconfdiscovery;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

/**
 * Adds additional headers for SSE request.
 */
class AdditionalHeaderWebTarget implements WebTarget {
    private WebTarget base;
    private String token;

    private String accessToken = "X-ACCESS-TOKEN";

    public AdditionalHeaderWebTarget(WebTarget target, String token) {
        base = target;
        this.token = token;
    }

    @Override
    public Invocation.Builder request() {
        return base.request().header(accessToken, token);
    }

    @Override
    public Invocation.Builder request(String... acceptedResponseTypes) {
        return base.request().header(accessToken, token);
    }

    @Override
    public Invocation.Builder request(MediaType... acceptedResponseTypes) {
        return base.request().header(accessToken, token);
    }

    @Override
    public Configuration getConfiguration() {
        return base.getConfiguration();
    }

    @Override
    public URI getUri() {
        return base.getUri();
    }

    @Override
    public UriBuilder getUriBuilder() {
        return base.getUriBuilder();
    }

    @Override
    public WebTarget path(String path) {
        return base.path(path);
    }

    @Override
    public WebTarget resolveTemplate(String name, Object value) {
        return base.resolveTemplate(name, value);
    }

    @Override
    public WebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
        return base.resolveTemplate(name, value, encodeSlashInPath);
    }

    @Override
    public WebTarget resolveTemplateFromEncoded(String name, Object value) {
        return base.resolveTemplateFromEncoded(name, value);
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> templateValues) {
        return base.resolveTemplates(templateValues);
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath) {
        return base.resolveTemplates(templateValues, encodeSlashInPath);
    }

    @Override
    public WebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        return base.resolveTemplatesFromEncoded(templateValues);
    }

    @Override
    public WebTarget matrixParam(String name, Object... values) {
        return base.matrixParam(name, values);
    }

    @Override
    public WebTarget queryParam(String name, Object... values) {
        return base.queryParam(name, values);
    }

    @Override
    public WebTarget property(String name, Object value) {
        return base.property(name, value);
    }

    @Override
    public WebTarget register(Class<?> componentClass) {
        return base.register(componentClass);
    }

    @Override
    public WebTarget register(Class<?> componentClass, int priority) {
        return base.register(componentClass, priority);
    }

    @Override
    public WebTarget register(Class<?> componentClass, Class<?>... contracts) {
        return base.register(componentClass, contracts);
    }

    @Override
    public WebTarget register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        return base.register(componentClass, contracts);
    }

    @Override
    public WebTarget register(Object component) {
        return base.register(component);
    }

    @Override
    public WebTarget register(Object component, int priority) {
        return base.register(component, priority);
    }

    @Override
    public WebTarget register(Object component, Class<?>... contracts) {
        return base.register(component, contracts);
    }

    @Override
    public WebTarget register(Object component, Map<Class<?>, Integer> contracts) {
        return base.register(component, contracts);
    }
}
