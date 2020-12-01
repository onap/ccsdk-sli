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
import org.opendaylight.restconf.common.errors.RestconfDocumentedException;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.DOT_REGEX;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.SLASH;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getChildSchemaNode;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getIndex;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getListName;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getNamespace;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getNodeType;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getParsedValue;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getProcessedPath;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getRevision;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getValueNamespace;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.resolveName;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.ANY_XML_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_LEAF_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.SINGLE_INSTANCE_LEAF_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.SINGLE_INSTANCE_NODE;

/**
 * Representation of mdsal based properties node serializer implementation.
 */
public class MdsalPropertiesNodeSerializer extends PropertiesNodeSerializer<SchemaNode, SchemaContext> {

    private static final Logger log = LoggerFactory.getLogger(
            MdsalPropertiesNodeSerializer.class);
    private SchemaNode curSchema;
    private PropertiesNode node;

    /**
     * Creates the properties node serializer.
     *
     * @param schemaNode schema node.
     * @param schemaCtx  schema context
     * @param uri        URL of the request
     */
    public MdsalPropertiesNodeSerializer(SchemaNode schemaNode,
                                         SchemaContext schemaCtx, String uri) {
        super(schemaNode, schemaCtx, uri);
    }

    @Override
    public PropertiesNode encode(Map<String, String> paramMap) throws SvcLogicException {
        curSchema = schemaNode();
        String nodeInUri[] = uri().split("\\/");
        String lastNodeName = nodeInUri[nodeInUri.length - 1];
        String rootUri = uri().replaceAll("\\/", "\\.");
        node = createRootNode(lastNodeName, rootUri);

        paramMap = convertToValidParam(paramMap);

        updateModNameReq(paramMap, rootUri);

        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            String[] names = entry.getKey().split("\\.");
            for (int i = 0; i < names.length; i++) {
                if (i < nodeInUri.length) {
                    if (!(nodeInUri[i].equals(names[i]))) {
                        break;
                    }
                } else {
                    createPropertyNode(i, names.length, names[i],
                                       entry.getValue());
                }
            }
        }
        return node;
    }

    private void updateModNameReq(Map<String, String> paramMap,
                                  String rootUri) {
        String isReqStr = rootUri + "." + "isNonAppend";
        String val = paramMap.get(isReqStr);
        if (val != null && val.equals("true")) {
            node.nonAppend(true);
        }
    }

    /**
     * Converts all the params in the svc logic context into a valid param by
     * replacing the underscore in module name to colon at necessary places.
     *
     * @param paramMap list of invalid parameters
     * @return list of partially valid parameters
     */
    private Map<String, String> convertToValidParam(Map<String, String> paramMap) {
        Map<String, String> fixedParams = new HashMap<>();
        for(Map.Entry<String, String> entry : paramMap.entrySet()) {
            String key = entry.getKey().replaceAll(DOT_REGEX, SLASH);
            try {
                SchemaPathHolder fixedUrl = getProcessedPath(key, schemaCtx());
                String fixedUri = fixedUrl.getUri().replaceAll(
                        SLASH, DOT_REGEX);
                fixedParams.put(fixedUri, entry.getValue());
            } catch (IllegalArgumentException | RestconfDocumentedException
                    | NullPointerException e) {
                log.info("Exception while processing properties by replacing " +
                    "underscore with colon. Process the properties as it is." + e);
                fixedParams.put(entry.getKey(), entry.getValue());
            }
        }
        return fixedParams;
    }

    @Override
    public Map<String, String> decode(PropertiesNode propertiesNode)
            throws SvcLogicException {
        PropertiesNodeWalker walker = new DefaultPropertiesNodeWalker<>();
        DefaultPropertiesNodeListener listener = new DefaultPropertiesNodeListener();
        walker.walk(listener, propertiesNode);
        return listener.params();
    }

    private RootNode createRootNode(String lastNodeName, String rootUri) {
        Module m = SchemaContextUtil.findParentModule(schemaCtx(), curSchema);
        Namespace ns = new Namespace(m.getName(), m.getNamespace(),
                                     getRevision(m.getRevision()));
        return new RootNode(lastNodeName, ns, schemaNode(), rootUri);
    }

    private void createPropertyNode(int index, int length, String name,
                                    String value) throws SvcLogicException {

        Namespace ns = getNamespace(getListName(name), schemaCtx(),
                                    node, curSchema);
        String localName = resolveName(ns, name);
        SchemaNode schema = getChildSchemaNode(curSchema, localName, ns);
        if (schema == null) {
            return;
        }

        switch (getNodeType(index, length, name, schema)) {
            case SINGLE_INSTANCE_NODE:
                node = node.addChild(localName, ns,
                                     SINGLE_INSTANCE_NODE, schema);
                curSchema = schema;
                break;

            case MULTI_INSTANCE_NODE:
                node = node.addChild(getIndex(name), localName, ns,
                                     MULTI_INSTANCE_NODE, schema);
                curSchema = schema;
                break;

            case SINGLE_INSTANCE_LEAF_NODE:
                addLeafNode(value, SINGLE_INSTANCE_LEAF_NODE, localName,
                               ns, schema, name);
                break;

            case MULTI_INSTANCE_LEAF_NODE:
                addLeafNode(value, MULTI_INSTANCE_LEAF_NODE, localName,
                               ns, schema, name);
                break;

            case ANY_XML_NODE:
                node = node.addChild(localName, ns, ANY_XML_NODE,
                                     value, null, schema);
                node = node.endNode();
                curSchema = ((SchemaNode) node.appInfo());
                break;

            default:
                throw new SvcLogicException("Invalid node type");
        }
    }

    /**
     * Adds leaf property node to the current node.
     *
     * @param value value of the leaf node
     * @param type single instance or multi instance leaf node
     * @param localName name of the leaf node
     * @param ns namespace of the leaf node
     * @param schema schema of the leaf node
     * @param name name of the leaf in properties
     * @throws SvcLogicException exception while adding leaf node
     */
    private void addLeafNode(String value, NodeType type,
                                String localName, Namespace ns,
                                SchemaNode schema, String name) throws SvcLogicException {
        Namespace valNs = getValueNamespace(value, schemaCtx());
        value = getParsedValue(valNs, value);
        if (SINGLE_INSTANCE_LEAF_NODE == type) {
            node = node.addChild(localName, ns, SINGLE_INSTANCE_LEAF_NODE,
                                 value, valNs, schema);
        } else {
            node = node.addChild(getIndex(name), localName, ns,
                                 MULTI_INSTANCE_LEAF_NODE, value,
                                 valNs, schema);
        }
        node = node.endNode();
        curSchema = ((SchemaNode) node.appInfo());
    }
}
