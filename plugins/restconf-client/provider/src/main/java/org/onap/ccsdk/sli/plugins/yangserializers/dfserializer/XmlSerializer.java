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
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.DefaultPropertiesNodeWalker;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNode;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNodeWalker;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormat.XML;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.XML_LIS_ERR;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.XML_TREE_ERR;

/**
 * Representation of XML serializer which encodes properties to XML and
 * decodes properties from XML with the data format serializer.
 */
public class XmlSerializer extends DataFormatSerializer {

    /**
     * Creates an instance of XML serializer.
     *
     * @param serializerContext data format serializer context
     */
    protected XmlSerializer(DataFormatSerializerContext serializerContext) {
        super(XML, serializerContext);
    }

    @Override
    public String encode(Map<String, String> param,
                         Map<String, List<Annotation>> annotations)
            throws SvcLogicException {
        PropertiesNode propNode = serializerContext().getPropNodeSerializer()
                .encode(param);
        PropertiesNodeWalker nodeWalker = new DefaultPropertiesNodeWalker<>();
        PropertiesNodeXmlListener xmlListener = new PropertiesNodeXmlListener();
        nodeWalker.walk(xmlListener, propNode);
        Writer writer = xmlListener.getWriter();
        return removeRootNode(writer.toString(), propNode.name());
    }

    @Override
    public Map<String, String> decode(String dataFormatBody)
            throws SvcLogicException {
        if (!(serializerContext().listener() instanceof XmlListener)) {
            throw new SvcLogicException(XML_LIS_ERR);
        }

        XmlListener listener = (XmlListener) serializerContext().listener();
        XmlWalker walker = new DefaultXmlWalker();
        Document document;

        try {
            document = DocumentHelper.parseText(dataFormatBody);
        } catch (DocumentException e) {
            throw new SvcLogicException(XML_TREE_ERR, e);
        }
        walker.walk(listener, document.getRootElement());

        return serializerContext().getPropNodeSerializer().decode(
                listener.serializerHelper().getPropertiesNode());
    }

    /**
     * Removes root node from the XML data format message and makes the
     * string to be pretty print.
     *
     * @param xml      XML data format message
     * @param rootName root node name
     * @return pretty print format XML message
     */
    private static String removeRootNode(String xml, String rootName) {
        xml = xml.replace("\n<" + rootName + ">", "\n");
        xml = xml.replace("</" + rootName + ">" + "\n", "");
        xml = xml.replaceAll("\n" + "    ", "\n");
        xml = xml.replaceFirst("\n", "");
        return xml;
    }
}