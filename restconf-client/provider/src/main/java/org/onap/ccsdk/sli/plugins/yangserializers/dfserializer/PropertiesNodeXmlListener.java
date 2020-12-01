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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.DefaultPropertiesNodeWalker;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.LeafNode;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.Namespace;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNode;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNodeListener;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.RootNode;

import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;

import static java.lang.String.format;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.NODE_TYPE_ERR;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.UTF_HEADER;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.XML_PREFIX;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.getXmlWriter;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_HOLDER_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_LEAF_HOLDER_NODE;

/**
 * Representation of XML implementation of properties node listener.
 */
public class PropertiesNodeXmlListener implements PropertiesNodeListener {

    /**
     * XML data from the element.
     */
    private String xmlData;

    /**
     * Root element of the XML document.
     */
    private Element rootElement;

    /**
     * Writer to write the XML.
     */
    private Writer writer;

    /**
     * XML element stack to store the elements.
     */
    private final Stack<Element> elementStack = new Stack<>();

    /**
     * Creates the properties node XML listener.
     */
    public PropertiesNodeXmlListener() {
    }

    @Override
    public void start(PropertiesNode node) {
        rootElement = addElement(null, node);
        elementStack.push(rootElement);
    }

    @Override
    public void end(PropertiesNode node) throws SvcLogicException {
        xmlData = rootElement.asXML();
        xmlData = UTF_HEADER + xmlData;
        writer = getXmlWriter(xmlData, "4");
    }

    @Override
    public void enterPropertiesNode(PropertiesNode node)
            throws SvcLogicException {
        Element element = null;
        String ns = getNodeNamespace(node);
        switch (node.nodeType()) {
            case MULTI_INSTANCE_LEAF_HOLDER_NODE:
            case MULTI_INSTANCE_HOLDER_NODE:
                break;

            case SINGLE_INSTANCE_NODE:
            case MULTI_INSTANCE_NODE:
                element = addElement(ns, node);
                break;

            case MULTI_INSTANCE_LEAF_NODE:
            case SINGLE_INSTANCE_LEAF_NODE:
                element = addElement(ns, node);
                setValueWithNs(element, (LeafNode) node);
                break;

            default:
                throw new SvcLogicException(format(
                        NODE_TYPE_ERR, node.nodeType().toString()));
        }
        if (element != null) {
            elementStack.push(element);
        }
    }

    @Override
    public void exitPropertiesNode(PropertiesNode node)
            throws SvcLogicException {
        walkAugmentationNode(node);
        switch (node.nodeType()) {
            case MULTI_INSTANCE_LEAF_HOLDER_NODE:
            case MULTI_INSTANCE_HOLDER_NODE:
                break;

            case SINGLE_INSTANCE_NODE:
            case MULTI_INSTANCE_NODE:
            case MULTI_INSTANCE_LEAF_NODE:
            case SINGLE_INSTANCE_LEAF_NODE:
                if (!elementStack.isEmpty()) {
                    elementStack.pop();
                }
                break;

            default:
                throw new SvcLogicException(format(
                        NODE_TYPE_ERR, node.nodeType().toString()));
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
     * Adds an XML element to the stack with namespace if present. If the
     * stack is empty it creates new document and adds element else adds to
     * the parent element.
     *
     * @param ns   namespace of the element
     * @param node properties node
     * @return new added element
     */
    private Element addElement(String ns, PropertiesNode node) {
        Element element;
        if (elementStack.isEmpty()) {
            Document doc = DocumentHelper.createDocument();
            if (ns != null) {
                element = doc.addElement(node.name(), ns);
            } else {
                element = doc.addElement(node.name());
            }
        } else {
            element = elementStack.peek();
            if (ns != null) {
                element = element.addElement(node.name(), ns);
            } else {
                element = element.addElement(node.name());
            }
        }

        return element;
    }

    /**
     * Returns the abstract XML namespace to be used in XML data format from
     * the properties node.
     *
     * @param node properties node
     * @return abstract XML namespace
     */
    private String getNodeNamespace(PropertiesNode node) {
        PropertiesNode parent = node.parent();
        if (parent.nodeType() == MULTI_INSTANCE_HOLDER_NODE ||
                parent.nodeType() == MULTI_INSTANCE_LEAF_HOLDER_NODE) {
            parent = parent.parent();
        }
        if (parent instanceof RootNode || ! parent.namespace().moduleName()
                .equals(node.namespace().moduleName())) {
            return node.namespace().moduleNs().toString();
        }
        return null;
    }

    /**
     * Sets the value to the element for a leaf node and adds the value
     * namespace if required.
     *
     * @param element XML element
     * @param node leaf properties node
     */
    private void setValueWithNs(Element element, LeafNode node) {
        Namespace valNs = node.valueNs();
        URI modNs = (valNs == null) ? null : valNs.moduleNs();
        String val = node.value();
        if (modNs != null) {
            element.addNamespace(XML_PREFIX, modNs.toString());
            element.setText(XML_PREFIX + ":" + val);
        } else {
            element.setText(val);
        }
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
