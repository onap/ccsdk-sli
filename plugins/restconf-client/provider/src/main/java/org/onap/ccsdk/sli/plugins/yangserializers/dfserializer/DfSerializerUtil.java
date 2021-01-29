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
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.Namespace;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import static javax.xml.transform.OutputKeys.INDENT;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.XmlNodeType.OBJECT_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.XmlNodeType.TEXT_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.getRevision;

/**
 * Utilities for data format serializer.
 */
public final class DfSerializerUtil {

    static final String JSON_WRITE_ERR = "Unable to write to JSON from " +
            "properties.";

    static final String NODE_TYPE_ERR = "The node type %s is not supported.";

    static final String JSON_LIS_ERR = "The JSON serializer doesn't have " +
            "JSON listener";

    static final String XML_LIS_ERR = "The XML serializer doesn't have XML " +
            "listener";

    static final String PROP_NODE_ERR = "The property node doesn't have " +
            "schema node bound to it.";

    static final String DF_ERR = "Type mismatch for the node %s. The schema " +
            "node does not match with the data format node type %s.";

    static final String XML_PREFIX = "yangid";

    private static final String YES = "yes";

    private static final String INDENT_XMLNS = "{http://xml.apache" +
            ".org/xslt}indent-amount";

    private static final String XML_PARSE_ERR = "Unable to parse the xml to " +
            "document : \n";

    private static final String URI_ERR = "Unable to parse the URI";

    /**
     * Data format error message for unsupported types.
     */
    public static final String FORMAT_ERR = "Only JSON and XML formats are " +
            "supported. %s is not supported";

    /**
     * UTF header message for XML data format message.
     */
    public static final String UTF_HEADER = "<?xml version=\"1.0\" " +
            "encoding=\"UTF-8\"?>";

    /**
     * Error message when a JSON tree creation fails.
     */
    public static final String JSON_TREE_ERR = "Unable to form JSON tree " +
            "object from the JSON body provided.";

    /**
     * Error message when a XML tree creation fails.
     */
    public static final String XML_TREE_ERR = "Unable to form XML tree object" +
            " from the XML body provided.";

    //No instantiation.
    private DfSerializerUtil() {
    }

    /**
     * Returns the writer which contains the pretty formatted XML string.
     *
     * @param input  input XML
     * @param indent indentation level
     * @return writer with XML
     * @throws SvcLogicException when transformation of source fails
     */
    public static Writer getXmlWriter(String input, String indent)
            throws SvcLogicException {
        try {
            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer();
            transformer.setOutputProperty(INDENT, YES);
            transformer.setOutputProperty(INDENT_XMLNS, indent);
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(parseXml(input));
            transformer.transform(source, result);
            return result.getWriter();
        } catch (TransformerException e) {
            throw new SvcLogicException(XML_PARSE_ERR + input, e);
        }
    }

    /**
     * Parses the XML and converts it into dom document which can be used for
     * formatting the XML.
     *
     * @param in input XML
     * @return dom document of XML
     * @throws SvcLogicException when document building fails
     */
    private static Document parseXml(String in) throws SvcLogicException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new SvcLogicException(XML_PARSE_ERR + in, e);
        }
    }

    /**
     * Returns the resolved namespace object from the input received from the
     * abstract data format.
     *
     * @param mName  module name
     * @param mUri   module URI
     * @param ctx    schema context
     * @param parent parent properties node
     * @return namespace
     * @throws SvcLogicException when resolving namespace fails
     */
    static Namespace getResolvedNamespace(String mName, String mUri,
                                          SchemaContext ctx,
                                          PropertiesNode parent)
            throws SvcLogicException {
        if (mName == null && mUri == null) {
            Namespace parentNs = parent.namespace();
            return new Namespace(parentNs.moduleName(), parentNs.moduleNs(),
                                 parentNs.revision());
        }

        Iterator<Module> it;
        Module mod;
        if (mName != null) {
            it = ctx.findModules(mName).iterator();
        } else {
            URI modUri = null;
            try {
               modUri = new URI(mUri);
            } catch (URISyntaxException e) {
                throw new SvcLogicException(URI_ERR, e);
            }
            it = ctx.findModules(modUri).iterator();
        }

        if (!it.hasNext()) {
            return null;
        }
        mod = it.next();

        return new Namespace(mod.getName(), mod.getQNameModule().getNamespace(),
                             getRevision(mod.getRevision()));
    }

    /**
     * Returns the node type of a XML element.
     *
     * @param element XML element
     * @return node type of the XML element
     */
    static XmlNodeType getXmlNodeType(Element element) {
        Element newElement = element.createCopy();
        newElement.remove(element.getNamespace());
        return newElement.hasContent() && newElement.isTextOnly() ?
                TEXT_NODE : OBJECT_NODE;
    }

    /**
     * Resolves the super type to the base type from type definition.
     *
     * @param type super type
     * @return base type definition
     */
    static TypeDefinition<?> resolveBaseTypeFrom(TypeDefinition<?> type) {
        TypeDefinition superType = type;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }
        return superType;
    }
}
