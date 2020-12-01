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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * Abstraction of properties node data tree. This intermediate representation
 * will enable data format serializers to be agnostic of DG context memory
 * nuances and thereby will enable faster development of new data format
 * serializers.
 */
public abstract class PropertiesNode {

    private String name;
    private Namespace namespace;
    private String uri;
    private PropertiesNode parent;
    private Object appInfo;
    private NodeType nodeType;
    private boolean nonAppend;
    private Multimap<Object, PropertiesNode> augmentations = ArrayListMultimap.create();

    /**
     * Creates an instance of properties node.
     *
     * @param name name of node
     * @param namespace namespace of node, null indicates parent namespace
     * @param uri URI of this node, if null its calculated based on parent and
     * current value of name and namespace
     * @param parent parent's node
     * @param appInfo application related information
     * @param nodeType node type
     */
    protected PropertiesNode(String name, Namespace namespace, String uri,
                             PropertiesNode parent, Object appInfo, NodeType nodeType) {
        this.name = name;
        this.namespace = namespace;
        this.uri = uri;
        this.parent = parent;
        this.appInfo = appInfo;
        this.nodeType = nodeType;
    }

    /**
     * Sets name.
     *
     * @param name name of the node
     */
    public void name(String name) {
        this.name = name;
    }

    /**
     * Sets namespace.
     *
     * @param namespace namespace of the node
     */
    public void namespace(Namespace namespace) {
        this.namespace = namespace;
    }

    /**
     * Sets uri.
     *
     * @param uri uri of the node
     */
    public void uri(String uri) {
        this.uri = uri;
    }

    /**
     * Sets parent node.
     *
     * @param parent parent node
     */
    public void parent(PropertiesNode parent) {
        this.parent = parent;
    }

    /**
     * Sets application info.
     *
     * @param appInfo application info
     */
    public void appInfo(Object appInfo) {
        this.appInfo = appInfo;
    }

    /**
     * Sets to true if module name is required in forming a request; false
     * otherwise.
     *
     * @param isNotReq true if required; false otherwise
     */
    public void nonAppend(boolean isNotReq) {
        this.nonAppend = isNotReq;
    }

    /**
     * Returns parent.
     *
     * @return parent node
     */
    public PropertiesNode parent() {
        return parent;
    }

    /**
     * Returns name.
     *
     * @return name of the node
     */
    public String name() {
        return name;
    }

    /**
     * Returns namespace.
     *
     * @return namespace of the node
     */
    public Namespace namespace() {
        return namespace;
    }

    /**
     * Returns uri.
     *
     * @return uri of the node
     */
    public String uri() {
        return uri;
    }

    /**
     * Returns application info.
     *
     * @return application info
     */
    public Object appInfo() {
        return appInfo;
    }

    /**
     * Returns node type.
     *
     * @return node type
     */
    public NodeType nodeType() {
        return nodeType;
    }

    /**
     * Returns if module name is required.
     *
     * @return status of module name if required
     */
    public boolean nonAppend() {
        return nonAppend;
    }

    /**
     * Returns augmentations.
     *
     * @return augmentations
     */
    public Multimap<Object, PropertiesNode> augmentations() {
        return augmentations;
    }

    /**
     * Sets augmentations.
     *
     * @param augmentations augmentations of the node
     */
    public void augmentations(Multimap<Object, PropertiesNode> augmentations) {
        this.augmentations = augmentations;
    }

    /**
     * Adds a child to a current node.
     *
     * @param name name of child
     * @param namespace namespace of child, null represents parent namespace
     * @param type type of node
     * @param appInfo application info
     * @return added properties node
     */
    public abstract PropertiesNode addChild(String name, Namespace namespace,
                                            NodeType type,
                                            Object appInfo) throws SvcLogicException;

    /**
     * Adds a child with value to a current node.
     *
     * @param name name of child
     * @param namespace namespace of child, null represents parent namespace
     * @param type type of node
     * @param value value of node
     * @param valueNs value namespace
     * @param appInfo application info
     * @throws SvcLogicException if failed to add child
     * @return added properties node
     */
    public abstract PropertiesNode addChild(String name, Namespace namespace,
                                            NodeType type, String value,
                                            Namespace valueNs,
                                            Object appInfo) throws SvcLogicException;

    /**
     * Adds a child at a given index to a current node. To be used in case of
     * leaf holder child's which is multi instance node.
     *
     * @param index index at which node is to be added
     * @param name name of child
     * @param namespace namespace of child, null represents parent namespace
     * @param type type of node
     * @param appInfo application info
     * @throws SvcLogicException if failed to add child
     * @return added properties node
     */
    public abstract PropertiesNode addChild(String index, String name,
                                            Namespace namespace,
                                            NodeType type,
                                            Object appInfo) throws SvcLogicException;

    /**
     * Adds a child at a given index to a current node. To be used in case of
     * leaf holder child's which is multi instance node.
     *
     * @param index index at which node is to be added
     * @param name name of child
     * @param namespace namespace of child, null represents parent namespace
     * @param type type of node
     * @param value value of node
     * @param valueNs value namespace
     * @param appInfo application info
     * @throws SvcLogicException if failed to add child
     * @return added properties node
     */
    public abstract PropertiesNode addChild(String index, String name,
                                            Namespace namespace, NodeType type,
                                            String value, Namespace valueNs,
                                            Object appInfo) throws SvcLogicException;

    /**
     * Returns root node.
     *
     * @return root node
     */
    public PropertiesNode endNode() {
        PropertiesNode node = this;
        while (node.parent() != null){
            node = node.parent();
        }
        return node;
    }
}

