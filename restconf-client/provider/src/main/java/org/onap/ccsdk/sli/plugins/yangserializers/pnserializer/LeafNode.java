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

package org.onap.ccsdk.sli.plugins.yangserializers.pnserializer;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * Representation of leaf node in properties data tree.
 */
public class LeafNode extends PropertiesNode implements LeafListHolderChild, DataNodeChild {

    private String value;
    private Namespace valueNs;
    private static final String svcLogicException = "Leaf cannot hold child nodes";

    /**
     * Creates an instance of leaf node.
     *
     * @param name name of the leaf node
     * @param namespace namespace of the leaf node
     * @param uri uri of the leaf node
     * @param parent parent of the leaf node
     * @param appInfo application info
     * @param nodeType node type
     * @param value value of the leaf
     */
    public LeafNode(String name, Namespace namespace,
                    String uri, PropertiesNode parent,
                    Object appInfo, NodeType nodeType,
                    String value) {
        super(name, namespace, uri, parent, appInfo, nodeType);
        this.value = value;
    }

    /**
     * Returns value of the leaf.
     *
     * @return value of the leaf
     */
    public String value() {
        return value;
    }

    /**
     * Sets value of the leaf.
     *
     * @param value value of the leaf
     */
    public void value(String value) {
        this.value = value;
    }

    /**
     * Returns value namespace.
     *
     * @return value namespace
     */
    public Namespace valueNs() {
        return valueNs;
    }

    /**
     * Sets value namespace.
     *
     * @param valueNs value namespace
     */
    public void valueNs(Namespace valueNs) {
        this.valueNs = valueNs;
    }

    @Override
    public PropertiesNode addChild(String name, Namespace namespace,
                                   NodeType type,
                                   Object appInfo) throws SvcLogicException {
        throw new SvcLogicException(svcLogicException);
    }

    @Override
    public PropertiesNode addChild(String name, Namespace namespace,
                                   NodeType type, String value,
                                   Namespace valueNs,
                                   Object appInfo) throws SvcLogicException {
        throw new SvcLogicException(svcLogicException);
    }

    @Override
    public PropertiesNode addChild(String index, String name,
                                   Namespace namespace,
                                   NodeType type,
                                   Object appInfo) throws SvcLogicException {
        throw new SvcLogicException(svcLogicException);
    }

    @Override
    public PropertiesNode addChild(String index, String name,
                                   Namespace namespace,
                                   NodeType type, String value,
                                   Namespace valueNs,
                                   Object appInfo) throws SvcLogicException {
        throw new SvcLogicException("Leaf cannot hold child nodes");
    }
}
