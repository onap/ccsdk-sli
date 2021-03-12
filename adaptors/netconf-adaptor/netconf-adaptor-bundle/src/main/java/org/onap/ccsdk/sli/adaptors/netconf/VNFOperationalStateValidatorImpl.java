/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.netconf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class VNFOperationalStateValidatorImpl implements OperationalStateValidator {
    private static final String OPERATIONAL_STATE_ELEMENT_NAME = "operationalState";

    @Override
    public VnfType getVnfType() {
        return VnfType.VNF;
    }

    @Override
    public String getConfigurationFileName() {
        String configFileName = OperationalStateValidatorFactory.configuration
                .getProperty(this.getClass().getCanonicalName() + CONFIG_FILE_PROPERTY_SUFFIX);
        configFileName = configFileName == null ? "VnfGetOperationalStates" : configFileName;
        return configFileName;
    }

    @Override
    public void validateResponse(String response) throws SvcLogicException {
        if(StringUtils.isEmpty(response)) {
            throw new SvcLogicException("empty response");
        }
        try {
            List<Map.Entry> operationalStateList = getOperationalStateList(response).orElseThrow(() ->
                    new SvcLogicException("response without any "+OPERATIONAL_STATE_ELEMENT_NAME+" element"));

            if(operationalStateList.stream().anyMatch(this::isNotEnabled)) {
                throw new SvcLogicException("at least one "+OPERATIONAL_STATE_ELEMENT_NAME+" is not in valid state. "
                        +operationalStateList.toString());
            }

        } catch (Exception e) {
            throw new SvcLogicException(e.toString());
        }
    }

    private boolean isNotEnabled(Map.Entry stateEntry) {
        return !("ENABLED").equalsIgnoreCase((String)stateEntry.getValue());
    }

    private static Optional<List<Map.Entry>> getOperationalStateList(String xmlText) throws IOException, ParserConfigurationException, SAXException {
        List<Map.Entry> entryList = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Remediate XML external entity vulnerabilty - prohibit the use of all protocols by external entities:
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlText.getBytes(StandardCharsets.UTF_8)));

        if(document != null) {
            Element rootElement = document.getDocumentElement();
            NodeList nodeList = rootElement.getElementsByTagName(OPERATIONAL_STATE_ELEMENT_NAME);
            if (nodeList != null && nodeList.getLength() > 0) {
                entryList = new ArrayList<>();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    String text = node.getTextContent();
                    String id = getElementID(node);
                    Map.Entry entry = new AbstractMap.SimpleEntry<>(id, text);
                    entryList.add(entry);
                }
            }
        }
        return Optional.ofNullable(entryList);
    }

    private static String getElementID(Node node) {
        String id = null;
        Node parentNode = node.getParentNode();
        if (parentNode != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NodeList nodeList = ((Element) parentNode).getElementsByTagName("id");
                if (nodeList != null && nodeList.getLength() > 0) {
                    Node idNode = nodeList.item(0);
                    id = idNode != null ? idNode.getTextContent() : null;
                }
            }else {
                id = parentNode.getNodeValue()+"|"+parentNode.getTextContent();
            }
        }

        id = StringUtils.isEmpty(id) ? null : StringUtils.normalizeSpace(id);
        id = StringUtils.isBlank(id) ? null : id;
        id = id != null ? id : "unknown-id";
        return id;
    }

}
