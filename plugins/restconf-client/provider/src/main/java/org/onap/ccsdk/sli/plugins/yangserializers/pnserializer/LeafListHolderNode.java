/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
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
 * Representation of leaf list holder node which will hold multi instance leaf
 * node in properties data tree.
 */
public class LeafListHolderNode extends HolderNode<LeafListHolderChild> implements DataNodeChild {

    private static final String node = " holder node";

    /**
     * Creates an instance of LeafListHolderNode.
     *
     * @param name name of the leaf-list node
     * @param namespace namespace of the leaf-list node
     * @param uri uri of the leaf-list node
     * @param parent parent node of the leaf-list
     * @param appInfo application info
     * @param nodeType node type
     */
    public LeafListHolderNode(String name, Namespace namespace,
                              String uri, PropertiesNode parent,
                              Object appInfo, NodeType nodeType) {
        super(name, namespace, uri, parent, appInfo, nodeType);
    }

    @Override
    public PropertiesNode addChild(String name, Namespace namespace,
                                   NodeType type, String value,
                                   Namespace valueNs,
                                   Object appInfo) throws SvcLogicException {
        throw new SvcLogicException("Leaf cannot be child of leaf-list" +
                                                   node);
    }

    @Override
    public PropertiesNode addChild(String name, Namespace namespace,
                                   NodeType type,
                                   Object appInfo) throws SvcLogicException {
        throw new SvcLogicException("Container cannot be child of leaf-list" +
                                                   node);
    }

    @Override
    public PropertiesNode addChild(String index, String name,
                                   Namespace namespace,
                                   NodeType type,
                                   Object appInfo) throws SvcLogicException {
        throw new SvcLogicException("List cannot be child of leaf-list" +
                                                   node);
    }

    @Override
    public PropertiesNode addChild(String index, String name,
                                   Namespace namespace, NodeType type,
                                   String value, Namespace valueNs,
                                   Object appInfo) throws SvcLogicException {
        LeafNode node = ((LeafNode) children().get(index));
        if (index == null) {
            index = String.valueOf(children().size());
        }
        String uri = this.uri() + "[" + index + "]";
        node = (node != null) ? node : new LeafNode(name, namespace, uri,
                                                    this, appInfo, type, value);
        node.valueNs(valueNs);
        children().put(index, node);
        return node;
    }
}
