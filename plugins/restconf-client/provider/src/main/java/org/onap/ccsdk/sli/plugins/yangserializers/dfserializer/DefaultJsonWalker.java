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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType;

import java.util.Iterator;
import java.util.Map;

import static com.fasterxml.jackson.databind.node.JsonNodeType.NUMBER;
import static com.fasterxml.jackson.databind.node.JsonNodeType.STRING;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_LEAF_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.SINGLE_INSTANCE_LEAF_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.SINGLE_INSTANCE_NODE;

/**
 * Implementation of JSON walker to walk through the nodes and process it.
 */
public class DefaultJsonWalker implements JsonWalker {

    @Override
    public void walk(JsonListener listener, JsonNode jsonNode) throws
            SvcLogicException {
        Iterator<Map.Entry<String, JsonNode>> children = jsonNode.fields();
        while (children.hasNext()) {
            Map.Entry<String, JsonNode> child = children.next();
            JsonNode value = child.getValue();
            String key = child.getKey();
            if (value.isArray()) {
                processMultiNodes(key, value, listener);
            } else {
                processSingleNode(key, value, listener);
            }
        }
    }

    /**
     * Processes single instance node or leaf, by adding the node to from
     * JSON and walking through all its children recursively.
     *
     * @param key      JSON name
     * @param value    JSON node
     * @param listener JSON listener
     * @throws SvcLogicException when processing the node fails
     */
    private void processSingleNode(String key, JsonNode value,
                                   JsonListener listener)
            throws SvcLogicException {
        NodeType nodeType;
        if (!value.isContainerNode()) {
            nodeType = SINGLE_INSTANCE_LEAF_NODE;
        } else {
            nodeType = SINGLE_INSTANCE_NODE;
        }
        processNode(key, value, nodeType, listener);
    }

    /**
     * Processes multi instance node or leaf, by adding the node to from JSON
     * and walking through all its instance recursively.
     *
     * @param key      JSON name
     * @param value    JSON node
     * @param listener JSON listener
     * @throws SvcLogicException when processing a single instance fails
     */
    private void processMultiNodes(String key, JsonNode value,
                                   JsonListener listener)
            throws SvcLogicException {
        NodeType nodeType;
        Iterator<JsonNode> multiNodes = value.elements();
        while (multiNodes.hasNext()) {
            if (isLeafListNode((ArrayNode) value)) {
                nodeType = MULTI_INSTANCE_LEAF_NODE;
            } else {
                nodeType = MULTI_INSTANCE_NODE;
            }
            JsonNode multiNode = multiNodes.next();
            processNode(key, multiNode, nodeType, listener);
        }
    }

    /**
     * Processes each node by first entering the JSON node through JSON
     * listener, second a call back to walking the rest of the tree of the
     * node and finally exiting the node.
     *
     * @param key      JSON name
     * @param node     JSON node
     * @param nodeType JSON node type
     * @param listener JSON listener
     * @throws SvcLogicException when entering a JSON node fails
     */
    private void processNode(String key, JsonNode node, NodeType nodeType,
                             JsonListener listener) throws SvcLogicException {
        listener.enterJsonNode(key, node, nodeType);
        walk(listener, node);
        listener.exitJsonNode(node);
    }

    /**
     * Returns true if the node corresponds to a leaf-list node; false
     * otherwise.
     *
     * @param node JSON node
     * @return true if node corresponds to leaf-list node; false otherwise
     */
    private boolean isLeafListNode(ArrayNode node) {
        Iterator<JsonNode> children = node.elements();
        while (children.hasNext()) {
            JsonNodeType type = children.next().getNodeType();
            if (type != STRING && type != NUMBER) {
                return false;
            }
        }
        return true;
    }
}
