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

import java.util.HashMap;
import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.addToAugmentations;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.createNode;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getAugmentationNode;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getUri;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.isNamespaceAsParent;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.resolveName;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_HOLDER_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_LEAF_HOLDER_NODE;
import static org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils.findCorrespondingAugment;

/**
 * Abstraction of an entity that represents an inner node to properties data
 * tree.
 *
 * @param <T> type of child
 */
public abstract class InnerNode<T extends NodeChild> extends PropertiesNode {

    private Map<String, T> children = new HashMap<String, T>();

    protected InnerNode(String name, Namespace namespace, String uri,
                        PropertiesNode parent, Object appInfo, NodeType nodeType) {
        super(name, namespace, uri, parent, appInfo, nodeType);
    }

    /**
     * Returns children.
     *
     * @return children
     */
    public Map<String, T> children() {
        return children;
    }

    /**
     * Sets children.
     *
     * @param children child nodes
     */
    public void children(Map<String, T> children) {
        this.children = children;
    }

    @Override
    public PropertiesNode addChild(String name, Namespace namespace,
                                   NodeType type,
                                   Object appInfo) throws SvcLogicException {
        PropertiesNode node = ((PropertiesNode) children.get(name));
        if (node != null) {
            return node;
        }

        // get augment schema, if it is augmented node
        AugmentationSchemaNode augSchema = null;
        if (((DataSchemaNode) appInfo).isAugmenting()) {
            augSchema = findCorrespondingAugment(((DataSchemaNode) this.appInfo()), ((DataSchemaNode) appInfo));
            node = getAugmentationNode(augSchema, this, name);
        }

        // create node based on type
        if (node == null) {
            String uri = getUri(this, name, namespace);
            node = createNode(name, namespace, uri, this, appInfo, type);
        }

        // If namespace is not same as parent then it is augmented node
        if (augSchema != null && !isNamespaceAsParent(this, node)) {
            addToAugmentations(augSchema, this, node);
        } else {
            children.put(name, ((T) node));
        }
        return node;
    }

    @Override
    public PropertiesNode addChild(String name, Namespace namespace,
                                   NodeType type, String value,
                                   Namespace valueNs,
                                   Object appInfo) throws SvcLogicException {
        LeafNode node = ((LeafNode) children.get(name));
        if (node != null) {
            return  node;
        }

        AugmentationSchemaNode augSchema = null;
        String uri = getUri(this, name, namespace);
        node = new LeafNode(name, namespace, uri, this,
                            appInfo, type, value);

        if (type != NodeType.ANY_XML_NODE) {
            if (((DataSchemaNode) appInfo).isAugmenting()) {
                augSchema = findCorrespondingAugment(
                        ((DataSchemaNode) this.appInfo()),
                        ((DataSchemaNode) appInfo));
            }
            node.valueNs(valueNs);
        }

        if (augSchema != null && !isNamespaceAsParent(this, node)) {
            addToAugmentations(augSchema, this, node);
        } else {
            children.put(name, ((T) node));
        }
        return node;
    }

    @Override
    public PropertiesNode addChild(String index, String name,
                                   Namespace namespace, NodeType type,
                                   Object appInfo) throws SvcLogicException {
        String localname = resolveName(name);
        PropertiesNode node = ((PropertiesNode) children.get(localname));

        if (node == null) {
            AugmentationSchemaNode augSchema = null;
            if (((DataSchemaNode) appInfo).isAugmenting()) {
                augSchema = findCorrespondingAugment(((DataSchemaNode) this.appInfo()),
                                                     ((DataSchemaNode) appInfo));
                node = getAugmentationNode(augSchema, this, localname);
            }

            if (node == null) {
                String uri = getUri(this, name, namespace);
                node = new ListHolderNode(localname, namespace, uri,
                                          this, appInfo, MULTI_INSTANCE_HOLDER_NODE);
            }

            if (augSchema != null && !isNamespaceAsParent(this, node)) {
                addToAugmentations(augSchema, this, node);
            } else {
                children.put(localname, ((T) node));
            }

            node = node.addChild(index, localname, namespace, type, appInfo);
        } else if (node instanceof ListHolderNode) {
            ListHolderChild child = ((ListHolderNode) node).child(index);
            node = (child != null ? ((MultiInstanceNode) child) :
                    node.addChild(index, localname, namespace, type, appInfo));
        } else {
            throw new SvcLogicException("Duplicate node exist with same node");
        }
        return node;
    }

    @Override
    public PropertiesNode addChild(String index, String name,
                                   Namespace namespace, NodeType type,
                                   String value, Namespace valueNs,
                                   Object appInfo) throws SvcLogicException {
        String localName = resolveName(name);
        PropertiesNode node = ((PropertiesNode) children.get(localName));

        if (node == null) {

            AugmentationSchemaNode augSchema = null;
            if (((DataSchemaNode) appInfo).isAugmenting()) {
                augSchema = findCorrespondingAugment(((DataSchemaNode) this.appInfo()),
                                                     ((DataSchemaNode) appInfo));
                node = getAugmentationNode(augSchema, this, localName);
            }

            if (node == null) {
                String uri = getUri(this, name, namespace);
                node = new LeafListHolderNode(localName, namespace, uri, this,
                                              appInfo, MULTI_INSTANCE_LEAF_HOLDER_NODE);
            }

            if (augSchema != null && !isNamespaceAsParent(this, node)) {
                addToAugmentations(augSchema, this, node);
            } else {
                children.put(localName, ((T) node));
            }

            node = node.addChild(index, localName, namespace, type, value, valueNs, appInfo);
        } else if (node instanceof LeafListHolderNode) {
            LeafNode child = ((LeafNode) ((HolderNode) node).child(index));
            node = (child != null ? child : node.addChild(index, localName,
                                                          namespace, type,
                                                          value, valueNs,
                                                          appInfo));
        } else {
            throw new SvcLogicException("Duplicate node exist with same node");
        }
        return node;
    }
}
