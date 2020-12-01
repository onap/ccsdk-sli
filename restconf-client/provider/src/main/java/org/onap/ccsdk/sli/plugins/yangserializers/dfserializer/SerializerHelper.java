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

package org.onap.ccsdk.sli.plugins.yangserializers.dfserializer;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNode;

/**
 * Abstraction of an entity which helps the data format serializers to obtain
 * schema context details and to build properties from data.
 *
 * @param <T> type of schema node
 * @param <P> type of schema context
 */
public abstract class SerializerHelper<T, P> {

    /**
     * Schema node of the last element in the URI.
     */
    protected T schemaNode;

    /**
     * Root schema context.
     */
    protected P schemaCtx;

    /**
     * Root URI.
     */
    protected String rootUri;

    /**
     * Creates an instance of the serializer helper with the schema node,
     * schema context and the URI.
     *
     * @param t schema node
     * @param p schema context
     * @param u root URI
     */
    protected SerializerHelper(T t, P p, String u) {
        schemaNode = t;
        schemaCtx = p;
        rootUri = u;
    }

    /**
     * Returns schema node of the last element in the URI.
     *
     * @return schema node
     */
    protected abstract T getSchemaNode();

    /**
     * Returns the root schema context.
     *
     * @return schema context
     */
    protected abstract P getSchemaCtx();

    /**
     * Returns the current schema context node.
     *
     * @return current schema context node
     */
    protected abstract T getCurSchema();

    /**
     * Adds a node to the properties node tree.
     *
     * @param name         name of the node
     * @param nameSpace    name space of the node, it can be either module
     *                     name or namespace; null indicates parent namespace
     * @param value        value of the node; applicable for leaf/leaf-list node
     * @param valNameSpace value namespace for identityref, could be module
     *                     name or namespace
     * @param type         type of node if known like in case of JSON
     * @throws SvcLogicException when adding node fails
     */
    protected abstract void addNode(String name, String nameSpace, String value,
                                    String valNameSpace, NodeType type)
            throws SvcLogicException;

    /**
     * Exits the node, in case if it's leaf node then it adds to the properties
     * map.
     *
     * @throws SvcLogicException when properties node tree is improper
     */
    protected abstract void exitNode() throws SvcLogicException;

    /**
     * Returns the built properties corresponding to the data.
     *
     * @return properties node.
     */
    protected abstract PropertiesNode getPropertiesNode();
}
