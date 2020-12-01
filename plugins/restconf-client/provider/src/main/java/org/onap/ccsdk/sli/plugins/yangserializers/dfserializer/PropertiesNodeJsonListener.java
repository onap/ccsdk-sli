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

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.DefaultPropertiesNodeWalker;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.LeafNode;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.Namespace;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNode;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNodeListener;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.RootNode;

import static com.google.common.base.Strings.repeat;
import static java.lang.String.format;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.JSON_WRITE_ERR;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.NODE_TYPE_ERR;

/**
 * Representation of JSON implementation of properties node listener.
 */
public class PropertiesNodeJsonListener implements PropertiesNodeListener{

    /**
     * JSON writer to write the JSON data format.
     */
    private JsonWriter jsonWriter;

    /**
     * Writer to write the JSON.
     */
    private Writer writer;

    /**
     * Creates the properties node JSON listener by instantiating and
     * indenting the writer.
     */
    public PropertiesNodeJsonListener() {
        writer = new StringWriter();
        jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent(repeat(" ", 4));
    }

    @Override
    public void start(PropertiesNode node) throws SvcLogicException {
        try {
            jsonWriter.beginObject();
        } catch (IOException e) {
            throw new SvcLogicException(JSON_WRITE_ERR, e);
        }
    }

    @Override
    public void end(PropertiesNode node) throws SvcLogicException {
        try {
            jsonWriter.endObject();
            jsonWriter.flush();
        } catch (IOException e) {
            throw new SvcLogicException(JSON_WRITE_ERR, e);
        }
    }

    @Override
    public void enterPropertiesNode(PropertiesNode node)
            throws SvcLogicException {
        String val;
        String nodeName = getNodeName(node);
        try {
            switch (node.nodeType()) {
                case SINGLE_INSTANCE_NODE:
                    jsonWriter.name(nodeName);
                    jsonWriter.beginObject();
                    break;

                case MULTI_INSTANCE_NODE:
                    jsonWriter.beginObject();
                    break;

                case SINGLE_INSTANCE_LEAF_NODE:
                    val = getValueWithNs((LeafNode) node);
                    jsonWriter.name(nodeName).value(val);
                    break;

                case MULTI_INSTANCE_HOLDER_NODE:
                case MULTI_INSTANCE_LEAF_HOLDER_NODE:
                    jsonWriter.name(nodeName);
                    jsonWriter.beginArray();
                    break;

                case MULTI_INSTANCE_LEAF_NODE:
                    val = getValueWithNs((LeafNode) node);
                    jsonWriter.value(val);
                    break;

                case ANY_XML_NODE:
                    jsonWriter.name(nodeName);
                    val = ((LeafNode) node).value();
                    try {
                        jsonWriter.jsonValue(val);
                    } catch (IOException e) {
                        throw new SvcLogicException(JSON_WRITE_ERR, e);
                    }
                    break;

                default:
                    throw new SvcLogicException(format(
                            NODE_TYPE_ERR, node.nodeType().toString()));

            }
        } catch (IOException e) {
            throw new SvcLogicException(JSON_WRITE_ERR, e);
        }
    }

    @Override
    public void exitPropertiesNode(PropertiesNode node) throws SvcLogicException {
        walkAugmentationNode(node);
        try {
            switch (node.nodeType()) {
                case SINGLE_INSTANCE_NODE:
                case MULTI_INSTANCE_NODE:
                    jsonWriter.endObject();
                    break;

                case MULTI_INSTANCE_HOLDER_NODE:
                case MULTI_INSTANCE_LEAF_HOLDER_NODE:
                    jsonWriter.endArray();
                    break;

                case  SINGLE_INSTANCE_LEAF_NODE:
                case MULTI_INSTANCE_LEAF_NODE:
                case ANY_XML_NODE:
                    break;

                default:
                    throw new SvcLogicException(format(
                            NODE_TYPE_ERR, node.nodeType().toString()));
            }
        } catch (IOException e) {
            throw new SvcLogicException(JSON_WRITE_ERR, e);
        }
    }

    /**
     * Returns the writer.
     *
     * @return writer
     */
    public Writer getWriter() {
        return writer;
    }

    /**
     * Returns the abstract JSON node name to be used in JSON data format
     * from the properties node.
     *
     * @param node properties node
     * @return abstract JSON node
     */
    private String getNodeName(PropertiesNode node) {
        PropertiesNode parent = node.parent();
        if (parent instanceof RootNode || !parent.namespace().moduleName()
                .equals(node.namespace().moduleName())) {
            if (!parent.nonAppend()) {
                return node.namespace().moduleName() + ":" + node.name();
            }
        }
        return node.name();
    }

    /**
     * Returns the value of JSON leaf node with module name if required.
     *
     * @param node properties node
     * @return value with namespace
     */
    private String getValueWithNs(LeafNode node) {
        Namespace valNs = node.valueNs();
        String modName = (valNs == null) ? null : valNs.moduleName();
        if (modName != null) {
            return modName + ":" + node.value();
        }
        return node.value();
    }

    /**
     * Gets all the augmentation of the given node and walks through it.
     *
     * @param node properties node
     * @throws SvcLogicException when walking the properties node fails
     */
    private void walkAugmentationNode(PropertiesNode node)
            throws SvcLogicException {
        for (Map.Entry<Object, Collection<PropertiesNode>>
                augToChild : node.augmentations().asMap().entrySet()) {
            Collection<PropertiesNode> augChild = augToChild.getValue();
            if (!augChild.isEmpty()) {
                DefaultPropertiesNodeWalker walker = new
                        DefaultPropertiesNodeWalker();
                for (PropertiesNode p : augChild) {
                    enterPropertiesNode(p);
                    walker.walkChildNode(this, p);
                    exitPropertiesNode(p);
                }
            }
        }
    }
}
