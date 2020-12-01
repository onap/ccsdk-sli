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
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.Namespace;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNode;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.RootNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

import java.util.Deque;

import static java.lang.String.format;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.DF_ERR;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.NODE_TYPE_ERR;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.PROP_NODE_ERR;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.getResolvedNamespace;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.resolveBaseTypeFrom;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getRevision;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_HOLDER_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_LEAF_HOLDER_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_LEAF_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.SINGLE_INSTANCE_LEAF_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.SINGLE_INSTANCE_NODE;
import static org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils.findDataChildSchemaByQName;
import static org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils.findSchemaForChild;
import static org.opendaylight.yangtools.yang.data.util.ParserStreamUtils.findSchemaNodeByNameAndNamespace;

/**
 * Representation of MDSAL based serializer helper, which adds properties
 * node to the properties tree based on its types.
 */
public class MdsalSerializerHelper extends SerializerHelper<SchemaNode, SchemaContext> {

    /**
     * Current properties node.
     */
    private PropertiesNode propNode;

    /**
     * Current schema node.
     */
    private SchemaNode curSchemaNode;


    /**
     * Creates MDSAL serializer helper with root schema node, schema context
     * and URI.
     *
     * @param n schema node of the URI's last node
     * @param c schema context
     * @param u URI of the request
     */
    public MdsalSerializerHelper(SchemaNode n, SchemaContext c,
                                 String u) {
        super(n, c, u);
        Namespace ns = new Namespace(n.getQName().getLocalName(),
                                     n.getQName().getNamespace(),
                                     getRevision(n.getQName().getRevision()));
        propNode = new RootNode<>(n.getQName().getLocalName(), ns,
                                  getSchemaNode(), u);
        curSchemaNode = getSchemaNode();
    }

    @Override
    protected SchemaNode getSchemaNode() {
        return schemaNode;
    }

    @Override
    protected SchemaContext getSchemaCtx() {
        return schemaCtx;
    }

    @Override
    protected SchemaNode getCurSchema() {
        return curSchemaNode;
    }

    @Override
    protected void addNode(String name, String nameSpace, String value,
                           String valNameSpace, NodeType type)
            throws SvcLogicException {
        Namespace ns;
        if (type == null) {
            ns = getResolvedNamespace(null, nameSpace,
                                      getSchemaCtx(), propNode);
        } else {
            ns = getResolvedNamespace(nameSpace, null,
                                      getSchemaCtx(), propNode);
        }
        if (isChildPresent(name, ns)) {
            addNodeToProperty(name, ns, value, valNameSpace, type);
        } else {
            throw new SvcLogicException(format(
                    "Unable to add the node %s", name));
        }
    }

    @Override
    protected void exitNode() throws SvcLogicException {
        propNode = propNode.parent();
        if (propNode != null) {
            NodeType type = propNode.nodeType();
            if (type == MULTI_INSTANCE_HOLDER_NODE ||
                    type == MULTI_INSTANCE_LEAF_HOLDER_NODE) {
                propNode = propNode.parent();
            }
        }
        if (propNode == null || propNode.appInfo() == null
                || !(propNode.appInfo() instanceof SchemaNode)) {
            throw new SvcLogicException(PROP_NODE_ERR);
        }
        curSchemaNode = (SchemaNode) propNode.appInfo();
    }

    @Override
    protected PropertiesNode getPropertiesNode() {
        return propNode;
    }

    /**
     * Adds the node to property node based on the type of the schema node,
     * which is decided based on the name and namespace of the input
     * information.
     *
     * @param name         name of the node
     * @param ns           namespace of the node
     * @param value        value of the node if its a leaf/leaf-list
     * @param valNamespace namespace of the value
     * @param type         type of the node
     * @throws SvcLogicException when adding child fails
     */
    private void addNodeToProperty(String name, Namespace ns, String value,
                                   String valNamespace, NodeType type)
            throws SvcLogicException {
        Namespace valueNs;
        if (type != null) {
            validateNodeType(type);
        }
        if (curSchemaNode instanceof LeafSchemaNode) {
            valueNs = getValueNs(curSchemaNode, valNamespace, type);
            propNode = propNode.addChild(name, ns,
                                         SINGLE_INSTANCE_LEAF_NODE,
                                         value, valueNs, curSchemaNode);
        } else if (curSchemaNode instanceof LeafListSchemaNode) {
            valueNs = getValueNs(curSchemaNode, valNamespace, type);
            propNode = propNode.addChild(null, name, ns,
                                         MULTI_INSTANCE_LEAF_NODE, value,
                                         valueNs, curSchemaNode);
        } else if (curSchemaNode instanceof ListSchemaNode) {
            propNode = propNode.addChild(null, name, ns, MULTI_INSTANCE_NODE,
                                         curSchemaNode);
        } else {
            propNode = propNode.addChild(name, ns, SINGLE_INSTANCE_NODE,
                                         curSchemaNode);
        }
    }

    /**
     * Returns the namespace of the value namespace in case of identity ref.
     *
     * @param schemaNode schema node
     * @param valNs      value name space
     * @param nodeType   node type
     * @return namespace of value namespace
     * @throws SvcLogicException when namespace resolution fails for identityref
     */
    private Namespace getValueNs(SchemaNode schemaNode, String valNs,
                                 NodeType nodeType) throws SvcLogicException {
        Namespace ns = null;
        if (valNs != null) {
            TypeDefinition type;
            if (schemaNode instanceof LeafSchemaNode) {
                type = ((LeafSchemaNode) schemaNode).getType();
            } else {
                type = ((LeafListSchemaNode) schemaNode).getType();
            }
            TypeDefinition<?> baseType = resolveBaseTypeFrom(type);
            if (baseType instanceof IdentityrefTypeDefinition) {
                if (nodeType == null) {
                    ns = getResolvedNamespace(null, valNs, getSchemaCtx(),
                                              propNode);
                } else {
                    ns = getResolvedNamespace(valNs, null, getSchemaCtx(),
                                              propNode);
                }
            }
        }
        return ns;
    }

    /**
     * Validates that the node type from the data format matches with that of
     * the corresponding schema node.
     *
     * @param type node type from the abstract data format
     * @throws SvcLogicException when the node type is wrong
     */
    private void validateNodeType(NodeType type) throws SvcLogicException {
        boolean verify;
        switch (type) {
            case SINGLE_INSTANCE_LEAF_NODE:
                verify = curSchemaNode instanceof LeafSchemaNode;
                break;

            case MULTI_INSTANCE_LEAF_NODE:
                verify = curSchemaNode instanceof LeafListSchemaNode;
                break;

            case MULTI_INSTANCE_NODE:
                verify = curSchemaNode instanceof ListSchemaNode;
                break;

            case SINGLE_INSTANCE_NODE:
                verify = (!(curSchemaNode instanceof LeafSchemaNode) &&
                        !(curSchemaNode instanceof LeafListSchemaNode) &&
                        !(curSchemaNode instanceof ListSchemaNode));
                break;

            default:
                throw new SvcLogicException(format(NODE_TYPE_ERR,
                                                   type.toString()));
        }
        if (!verify) {
            throw new SvcLogicException(format(DF_ERR, curSchemaNode
                    .getQName().getLocalName(), type.toString()));
        }
    }

    /**
     * Returns true if the child schema is present with the name and
     * namespace inside the current schema node, if present updates the
     * current schema node; false otherwise.
     *
     * @param name      name of the child schema node
     * @param namespace namespace of the child schema node
     * @return returns true if the child schema is available; false otherwise
     */
    private boolean isChildPresent(String name, Namespace namespace) {
        QName qname =  QName.create(namespace.moduleNs(),
                                    Revision.of(namespace.revision()), name);
        SchemaNode childNode = null;
        if (curSchemaNode instanceof DataSchemaNode) {
            Deque<DataSchemaNode> dataSchema = findSchemaNodeByNameAndNamespace(
                    (DataSchemaNode) curSchemaNode, name, namespace.moduleNs());

            if (dataSchema != null && !dataSchema.isEmpty()) {
                childNode = dataSchema.pop();
            }

            if (dataSchema != null && !dataSchema.isEmpty()) {
                childNode = findSchemaForChild(((ChoiceSchemaNode) childNode),
                                               qname);
            }

        } else {
            childNode = findDataChildSchemaByQName(curSchemaNode, qname);
        }

        if (childNode != null) {
            curSchemaNode = childNode;
            return true;
        }
        return false;
    }
}
