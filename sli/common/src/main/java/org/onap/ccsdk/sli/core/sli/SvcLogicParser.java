/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.onap.ccsdk.sli.core.sli;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author dt5972
 *
 */
public class SvcLogicParser {

    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static final String JAXP_DYNAMIC_VALIDATION = "http://apache.org/xml/features/validation/dynamic";
    static final String JAXP_SCHEMA_VALIDATION = "http://apache.org/xml/features/validation/schema";

    private static final String LOAD_MESSAGE = "Getting SvcLogicGraph from database - {}";
    private static final String LOAD_ERROR_MESSAGE = "SvcLogicGraph not found - {}";
    private static final String ACTIVATION_ERROR_MESSAGE = "Could not activate SvcLogicGraph - {}";
    private static final String PRINT_ERROR_MESSAGE = "Could not print SvcLogicGraph - {}";
    private static final String SVC_LOGIC_STORE_ERROR = "Could not get service logic store";

    private static final Logger LOGGER = LoggerFactory.getLogger(SvcLogicParser.class);
    private static final String SLI_VALIDATING_PARSER = "org.onap.ccsdk.sli.parser.validate";
    private static final String SVCLOGIC_XSD = "/svclogic.xsd";
    private SAXParser saxParser;

    private class SvcLogicHandler extends DefaultHandler {
        private Locator locator = null;
        private String module = null;
        private String version = null;
        private LinkedList<SvcLogicGraph> graphs = null;
        private SvcLogicGraph curGraph = null;
        private SvcLogicNode curNode = null;
        private LinkedList<SvcLogicNode> nodeStack = null;
        private int curNodeId = 0;
        private String outcomeValue = null;
        private LinkedList<String> outcomeStack = null;

        public SvcLogicHandler(LinkedList<SvcLogicGraph> graphs) {
            this.graphs = graphs;
            this.curNode = null;
            this.nodeStack = new LinkedList<>();
            this.outcomeStack = new LinkedList<>();
            this.curNodeId = 1;
            this.outcomeValue = null;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {

            // Handle service-logic (graph) tag
            if ("service-logic".equalsIgnoreCase(qName)) {

                module = attributes.getValue("module");
                if (module == null || module.length() == 0) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Missing 'module' attribute from service-logic tag");
                }

                version = attributes.getValue("version");
                if (version == null || version.length() == 0) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Missing 'version' attribute from service-logic tag");
                }

                return;
            }

            if ("method".equalsIgnoreCase(qName)) {

                if (curGraph != null) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Cannot nest module tags");
                }

                curGraph = new SvcLogicGraph();
                curGraph.setModule(module);
                curGraph.setVersion(version);
                this.curNodeId = 1;

                String attrValue = attributes.getValue("rpc");
                if (attrValue == null || attrValue.length() == 0) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Missing 'rpc' attribute for method tag");
                }
                curGraph.setRpc(attrValue);

                attrValue = attributes.getValue("mode");
                if (attrValue == null || attrValue.length() == 0) {
                    throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                            + "Missing 'mode' attribute for method tag");
                }
                curGraph.setMode(attrValue);

                return;
            }

            // Handle outcome (edge) tag
            if ("outcome".equalsIgnoreCase(qName)) {
                String refValue = attributes.getValue("ref");

                if (refValue != null) {
                    SvcLogicNode refNode = curGraph.getNamedNode(refValue);

                    if (refNode != null) {
                        try {
                            curNode.addOutcome(attributes.getValue("value"), refNode);
                        } catch (SvcLogicException e) {
                            throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber()
                                    + " " + "Cannot add outcome", e);
                        }
                    } else {
                        throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                                + "ref to unknown node " + refValue);
                    }
                    return;
                }

                if (outcomeValue != null) {
                    outcomeStack.push(outcomeValue);
                }
                outcomeValue = attributes.getValue("value");

                return;
            }

            // Handle parameter tag
            if ("parameter".equalsIgnoreCase(qName)) {
                String parmName = attributes.getValue("name");
                String parmValue = attributes.getValue("value");

                if (parmName != null && parmName.length() > 0 && parmValue != null) {
                    try {

                        curNode.mapParameter(parmName, parmValue);
                    } catch (Exception e) {
                        throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                                + " cannot set parameter " + parmName + " to " + parmValue + " [" + e.getMessage()
                                + "]");
                    }
                }

                return;
            }

            // Handle node tags
            String nodeName = attributes.getValue("name");
            SvcLogicNode thisNode;


            try {
                if (nodeName != null && nodeName.length() > 0) {
                    thisNode = new SvcLogicNode(curNodeId++, qName, nodeName, curGraph);
                } else {
                    thisNode = new SvcLogicNode(curNodeId++, qName, curGraph);
                }

                if (curGraph.getRootNode() == null) {
                    curGraph.setRootNode(thisNode);
                }
            } catch (SvcLogicException e) {
                throw new SAXException(
                        "line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " " + e.getMessage());

            }

            int numAttributes = attributes.getLength();

            for (int i = 0; i < numAttributes; i++) {
                String attrName = attributes.getQName(i);
                if (!"name".equalsIgnoreCase(attrName)) {
                    try {

                        String attrValueStr = attributes.getValue(i);
                        SvcLogicExpression attrValue;
                        if (attrValueStr.trim().startsWith("`")) {
                            int lastParen = attrValueStr.lastIndexOf('`');
                            String evalExpr = attrValueStr.trim().substring(1, lastParen);
                            attrValue = SvcLogicExpressionFactory.parse(evalExpr);

                        } else {
                            if (Character.isDigit(attrValueStr.charAt(0))) {
                                attrValue = new SvcLogicAtom("NUMBER", attrValueStr);
                            } else {
                                attrValue = new SvcLogicAtom("STRING", attrValueStr);
                            }
                        }
                        thisNode.setAttribute(attrName, attrValue);
                    } catch (Exception e) {
                        throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " "
                                + "Cannot set attribute " + attrName, e);
                    }
                }
            }

            if (curNode != null) {
                try {
                    if ("block".equalsIgnoreCase(curNode.getNodeType()) || "for".equalsIgnoreCase(curNode.getNodeType())
                            || "while".equalsIgnoreCase(curNode.getNodeType())) {
                        curNode.addOutcome(Integer.toString(curNode.getNumOutcomes() + 1), thisNode);
                    } else {
                        if (outcomeValue == null) {
                            throw new SAXException("line " + locator.getLineNumber() + ":" + locator.getColumnNumber()
                                    + " " + curNode.getNodeType() + " node expects outcome, instead found "
                                    + thisNode.getNodeType());
                        }
                        curNode.addOutcome(outcomeValue, thisNode);
                    }
                } catch (SvcLogicException e) {
                    throw new SAXException(
                            "line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " " + e.getMessage());
                }
                nodeStack.push(curNode);
            }
            curNode = thisNode;

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            // Handle close of service-logic tag
            if ("service-logic".equalsIgnoreCase(qName)) {
                // Nothing more to do
                return;
            }

            // Handle close of method tag
            if ("method".equalsIgnoreCase(qName)) {
                graphs.add(curGraph);
                curGraph = null;
                return;
            }

            // Handle close of outcome tag
            if ("outcome".equalsIgnoreCase(qName)) {
                // Finished this outcome - pop the outcome stack
                if (outcomeStack.isEmpty()) {
                    outcomeValue = null;
                } else {
                    outcomeValue = outcomeStack.pop();
                }
                return;
            }

            // Handle close of parameter tag - do nothing
            if ("parameter".equalsIgnoreCase(qName)) {
                return;
            }

            // Handle close of a node tag
            if (nodeStack.isEmpty()) {
                curNode = null;
            } else {
                curNode = nodeStack.pop();
            }
        }

        @Override
        public void error(SAXParseException arg0) throws SAXException {
            throw new SAXException(
                    "line " + locator.getLineNumber() + ":" + locator.getColumnNumber() + " " + arg0.getMessage());
        }

    }

    public LinkedList<SvcLogicGraph> parse(String fileName) throws SvcLogicException {
        LinkedList<SvcLogicGraph> graphs;

        try {
            if (saxParser == null) {
                saxParser = initParser();
            }

            graphs = new LinkedList<>();
            saxParser.parse(fileName, new SvcLogicHandler(graphs));

            try {
                for (SvcLogicGraph graph : graphs) {
                    graph.setMd5sum(CheckSumHelper.md5SumFromFile(fileName));
                }
            } catch (Exception exc) {
                LOGGER.error("Couldn't set md5sum on graphs", exc);
            }
        } catch (Exception e) {
            LOGGER.error("Parsing failed ", e);
            String msg = e.getMessage();
            if (msg != null) {
                throw new SvcLogicException("Compiler error: " + fileName + " @ " + msg);
            } else {
                throw new SvcLogicException("Compiler error: " + fileName, e);
            }
        }
        return graphs;
    }

    public static void main(String argv[]) {

        if (argv.length == 0) {
            SvcLogicParser.usage();
        }

        if ("load".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                String xmlfile = argv[1];
                String propfile = argv[2];

                SvcLogicStore store = SvcLogicParser.getStore(propfile);
                try {
                    SvcLogicParser.load(xmlfile, store);
                } catch (Exception e) {
                    LOGGER.error("Load failed ", e);
                }
            } else {
                SvcLogicParser.usage();
            }
        } else if ("print".equalsIgnoreCase(argv[0])) {
            String version = null;
            String propfile = null;

            switch (argv.length) {
                case 6:
                    version = argv[4];
                    propfile = argv[5];
                case 5:
                    if (propfile == null) {
                        propfile = argv[4];
                    }
                    SvcLogicStore store = SvcLogicParser.getStore(propfile);
                    SvcLogicParser.print(argv[1], argv[2], argv[3], version, store);
                    break;
                default:
                    SvcLogicParser.usage();
            }
        } else if ("get-source".equalsIgnoreCase(argv[0])) {

            if (argv.length == 6) {
                SvcLogicStore store = SvcLogicParser.getStore(argv[5]);
                SvcLogicParser.getSource(argv[1], argv[2], argv[3], argv[4], store);
            } else {
                SvcLogicParser.usage();
            }
        } else if ("activate".equalsIgnoreCase(argv[0])) {
            if (argv.length == 6) {
                SvcLogicStore store = SvcLogicParser.getStore(argv[5]);
                SvcLogicParser.activate(argv[1], argv[2], argv[3], argv[4], store);
            } else {
                SvcLogicParser.usage();
            }
        } else if ("validate".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                String xmlfile = argv[1];
                String propfile = argv[2];

                System.setProperty(SLI_VALIDATING_PARSER, "true");
                SvcLogicStore store = SvcLogicParser.getStore(propfile);
                try {
                    SvcLogicParser.validate(xmlfile, store);
                } catch (Exception e) {
                    LOGGER.error("Validate failed", e);
                }
            } else {
                SvcLogicParser.usage();
            }
        } else if ("install".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                SvcLogicLoader loader = new SvcLogicLoader(argv[1], argv[2]);
                try {
                    loader.loadAndActivate();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                SvcLogicParser.usage();
            }
        } else if ("bulkActivate".equalsIgnoreCase(argv[0])) {
            if (argv.length == 3) {
                SvcLogicLoader loader = new SvcLogicLoader(argv[1], argv[2]);
                try {
                    loader.bulkActivate();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } else {
                SvcLogicParser.usage();
            }
        }

        System.exit(0);
    }

    protected static SvcLogicStore getStore(String propfile) {

        SvcLogicStore store = null;

        try {
            store = SvcLogicStoreFactory.getSvcLogicStore(propfile);
        } catch (Exception e) {
            LOGGER.error(SVC_LOGIC_STORE_ERROR, e);
            System.exit(1);
        }

        return store;

    }


    public static void load(String xmlfile, SvcLogicStore store) throws SvcLogicException {
        File xmlFile = new File(xmlfile);
        if (!xmlFile.canRead()) {
            throw new ConfigurationException("Cannot read xml file (" + xmlfile + ")");
        }

        SvcLogicParser parser = new SvcLogicParser();
        LinkedList<SvcLogicGraph> graphs;
        try {
            LOGGER.info("Loading {}", xmlfile);
            graphs = parser.parse(xmlfile);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage(), e);
        }

        if (graphs == null) {
            throw new SvcLogicException("Could not parse " + xmlfile);
        }

        for (SvcLogicGraph graph : graphs) {

            try {
                LOGGER.info("Saving " + graph.toString() + " to database.");
                store.store(graph);
            } catch (Exception e) {
                throw new SvcLogicException(e.getMessage(), e);
            }

        }

    }

    public static void validate(String xmlfile, SvcLogicStore store) throws SvcLogicException {
        File xmlFile = new File(xmlfile);
        if (!xmlFile.canRead()) {
            throw new ConfigurationException("Cannot read xml file (" + xmlfile + ")");
        }

        SvcLogicParser parser = new SvcLogicParser();
        LinkedList<SvcLogicGraph> graphs;
        try {
            LOGGER.info("Validating {}", xmlfile);
            graphs = parser.parse(xmlfile);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage(), e);
        }

        if (graphs == null) {
            throw new SvcLogicException("Could not parse " + xmlfile);
        } else {
            LOGGER.info("Compilation successful for {}", xmlfile);
        }

    }

    private static void print(String module, String rpc, String mode, String version, SvcLogicStore store) {
        String details = "(module:" + module + ", rpc:" + rpc + ", version:" + version + ", mode:" + mode + ")";

        try {
            LOGGER.info(LOAD_MESSAGE, details);

            SvcLogicGraph graph = store.fetch(module, rpc, version, mode);
            if (graph == null) {
                LOGGER.error(LOAD_ERROR_MESSAGE, details);
                System.exit(1);
            }
            graph.printAsGv(System.out);
        } catch (Exception e) {
            LOGGER.error(PRINT_ERROR_MESSAGE, details, e);
            System.exit(1);
        }

    }

    private static void getSource(String module, String rpc, String mode, String version, SvcLogicStore store) {
        String details = "(module:" + module + ", rpc:" + rpc + ", version:" + version + ", mode:" + mode + ")";

        try {
            LOGGER.info(LOAD_MESSAGE, details);

            SvcLogicGraph graph = store.fetch(module, rpc, version, mode);
            if (graph == null) {
                LOGGER.error(LOAD_ERROR_MESSAGE, details);
                System.exit(1);
            }
            graph.printAsXml(System.out);
        } catch (Exception e) {
            LOGGER.error(PRINT_ERROR_MESSAGE, details, e);
            System.exit(1);
        }

    }

    public static void activate(String module, String rpc, String version, String mode, SvcLogicStore store) {
        String details = "(module:" + module + ", rpc:" + rpc + ", version:" + version + ", mode:" + mode + ")";

        try {
            LOGGER.info(LOAD_MESSAGE, details);

            SvcLogicGraph graph = store.fetch(module, rpc, version, mode);
            if (graph == null) {
                LOGGER.error(LOAD_ERROR_MESSAGE, details);
                System.exit(1);
            }
            store.activate(graph);
        } catch (Exception e) {
            LOGGER.error(ACTIVATION_ERROR_MESSAGE, details, e);
            System.exit(1);
        }

    }

    private static void usage() {
        System.err.println("Usage: SvcLogicParser load <xml-file> <prop-file>");
        System.err.println(" OR    SvcLogicParser print <module> <rpc> <mode> [<version>] <prop-file>");
        System.err.println(" OR    SvcLogicParser get-source <module> <rpc> <mode> <version> <prop-file>");
        System.err.println(" OR    SvcLogicParser activate <module> <rpc> <version> <mode>");
        System.err.println(" OR    SvcLogicParser validate <file path to graph> <prop-file>");
        System.err.println(" OR    SvcLogicParser install <service-logic directory path> <prop-file>");
        System.err.println(" OR    SvcLogicParser bulkActivate <path to activation file> <prop-file>");
        System.exit(1);
    }

    protected SAXParser initParser() throws ParserConfigurationException, SAXException {
        URL xsdUrl = null;
        Schema schema = null;
        String validateSchema = System.getProperty(SLI_VALIDATING_PARSER, "true");

        if ("true".equalsIgnoreCase(validateSchema)) {
            xsdUrl = getClass().getResource(SVCLOGIC_XSD);
        }

        if (xsdUrl != null) {
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                schema = schemaFactory.newSchema(xsdUrl);
                LOGGER.info("Schema path {}", xsdUrl.getPath());
            } catch (Exception e) {
                LOGGER.warn("Could not validate using schema {}", xsdUrl.getPath(), e);
            }
        } else {
            LOGGER.warn("Could not find resource {}", SVCLOGIC_XSD);
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();

        if (schema != null) {
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
        }

        SAXParser saxParser = factory.newSAXParser();
        if (saxParser.isValidating()) {
            LOGGER.info("Parser configured to validate XML {}", (xsdUrl != null ? xsdUrl.getPath() : null));
        }
        return saxParser;
    }


}
