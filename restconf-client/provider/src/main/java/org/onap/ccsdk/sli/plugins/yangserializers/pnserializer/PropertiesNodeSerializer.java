/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.ccsdk.sli.plugins.yangserializers.pnserializer;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import java.util.Map;

/**
 * Abstraction of an entity to enable encoding and decoding of properties
 * to an abstract properties node tree using YANG based schema.
 * This serializer will be used by other data format serializers and will keep
 * them abstract from properties nuances thereby enabling quick addition of any
 * new data format serializer.
 *
 * @param <T> type of schema node
 * @param <P> schema context of the model
 */
public abstract class PropertiesNodeSerializer<T, P> {

    /**
     * Schema node from which the property is made.
     */
    private T schemaNode;

    /**
     * Schema context of the model.
     */
    private P schemaCtx;

    /**
     * URL pointing to the schema node.
     */
    private String uri;

    /**
     * Creates the properties node serializer.
     *
     * @param schemaNode schema node.
     * @param schemaCtx schema context
     * @param uri URL of the request
     */
    public PropertiesNodeSerializer(T schemaNode, P schemaCtx, String uri) {
        this.schemaNode = schemaNode;
        this.schemaCtx = schemaCtx;
        this.uri = uri;
    }

    /**
     * Encodes from properties to properties-node tree.
     *
     * @param paramMap parameter map
     * @throws SvcLogicException fails to encode properties to properties node
     * @return properties node
     */
    public abstract PropertiesNode encode(Map<String, String> paramMap) throws SvcLogicException;

    /**
     * Decodes from properties-node to properties map.
     *
     * @param propertiesNode properties-node
     * @return parameter map
     * @throws SvcLogicException fails to decode properties node to properties
     */
    public abstract Map<String, String> decode(PropertiesNode propertiesNode)
            throws SvcLogicException;

    /**
     * Returns the schema node of the property
     *
     * @return schema node
     */
    public T schemaNode(){
        return schemaNode;
    }

    /**
     * Returns the schema context
     *
     * @return schema node
     */
    public P schemaCtx() {
        return schemaCtx;
    }

    /**
     * Returns the URI.
     *
     * @return uri
     */
    public String uri() {
        return uri;
    }
}
