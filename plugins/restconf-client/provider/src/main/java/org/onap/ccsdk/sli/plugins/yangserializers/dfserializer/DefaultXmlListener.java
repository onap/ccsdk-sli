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

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import java.util.List;

import static java.lang.String.format;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.NODE_TYPE_ERR;

/**
 * Representation of default implementation of XML listener.
 */
public class DefaultXmlListener implements XmlListener {

    /**
     * Serializer helper to convert to properties node.
     */
    private SerializerHelper serializerHelper;

    /**
     * Creates an instance of default XML listener with its serializer helper.
     *
     * @param serializerHelper serializer helper
     */
    public DefaultXmlListener(SerializerHelper serializerHelper) {
        this.serializerHelper = serializerHelper;
    }

    @Override
    public void enterXmlElement(Element element, XmlNodeType nodeType)
            throws SvcLogicException {
        switch (nodeType) {
            case TEXT_NODE:
                serializerHelper.addNode(element.getName(),
                                         element.getNamespace().getURI(),
                                         element.getText(), null, null);
                break;

            case OBJECT_NODE:
                List cont = element.content();
                if (cont != null && cont.size() == 2 &&
                        isValueNsForLeaf(cont, element)) {
                    return;
                }
                serializerHelper.addNode(element.getName(),
                                         element.getNamespace().getURI(),
                                         null, null, null);
                break;

            default:
                throw new SvcLogicException(format(NODE_TYPE_ERR,
                                                   nodeType.toString()));
        }
    }

    /**
     * Returns true if element has value namespace and adds the node to
     * property tree; false otherwise.
     *
     * @param cont    content of the element
     * @param element element
     * @return true if element has value namespace; false otherwise
     * @throws SvcLogicException
     */
    private boolean isValueNsForLeaf(List cont, Element element)
            throws SvcLogicException {
        for (Object c : cont) {
            if (c instanceof Namespace) {
                String value = element.getText();
                if (value != null) {
                    String[] val = value.split(":");
                    String valPrefix = val[0];
                    String actVal = val[1];
                    if (valPrefix != null && actVal != null &&
                            valPrefix.equals(((Namespace) c).getPrefix())) {
                        serializerHelper.addNode(
                                element.getName(),
                                element.getNamespace().getURI(),
                                actVal,
                                ((Namespace) c).getURI(), null);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void exitXmlElement(Element element) throws SvcLogicException {
        serializerHelper.exitNode();
    }

    @Override
    public SerializerHelper serializerHelper() {
        return serializerHelper;
    }
}
