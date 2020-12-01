/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved. *
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.sli.plugins.prop;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class XmlParser {

    private static final Logger log = LoggerFactory.getLogger(XmlParser.class);

    private XmlParser() {
        // Preventing instantiation of the same.
    }

    public static Map<String, String> convertToProperties(String s, Set<String> listNameList)
        throws SvcLogicException {

        checkNotNull(s, "Input should not be null.");

        Handler handler = new Handler(listNameList);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            SAXParser saxParser = factory.newSAXParser();
            InputStream in = new ByteArrayInputStream(s.getBytes());
            saxParser.parse(in, handler);
        } catch (ParserConfigurationException | IOException | SAXException | NumberFormatException e) {
            throw new SvcLogicException("Unable to convert XML to properties" + e.getLocalizedMessage(), e);
        }
        return handler.getProperties();
    }

    private static class Handler extends DefaultHandler {

        private Set<String> listNameList;

        private Map<String, String> properties = new HashMap<>();

        StringBuilder currentName = new StringBuilder();
        StringBuilder currentValue = new StringBuilder();

        public Handler(Set<String> listNameList) {
            super();
            this.listNameList = listNameList;
            if (this.listNameList == null)
                this.listNameList = new HashSet<>();
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            String name = localName;
            if (name == null || name.trim().length() == 0)
                name = qName;
            int i2 = name.indexOf(':');
            if (i2 >= 0)
                name = name.substring(i2 + 1);

            if (currentName.length() > 0)
                currentName.append(Character.toString('.'));
            currentName.append(name);

            String listName = removeIndexes(currentName.toString());

            if (listNameList.contains(listName)) {
                String n = currentName.toString() + "_length";
                int len = getInt(properties, n);
                properties.put(n, String.valueOf(len + 1));
                currentName.append("[").append(len).append("]");
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            String name = localName;
            if (name == null || name.trim().length() == 0)
                name = qName;
            int i2 = name.indexOf(':');
            if (i2 >= 0)
                name = name.substring(i2 + 1);

            String s = currentValue.toString().trim();
            if (s.length() > 0) {
                properties.put(currentName.toString(), s);

                log.info("Added property: {} : {}", currentName, s);
                currentValue = new StringBuilder();
            }

            int i1 = currentName.lastIndexOf("." + name);
            if (i1 <= 0)
                currentName = new StringBuilder();
            else
                currentName = new StringBuilder(currentName.substring(0, i1));
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            String value = new String(ch, start, length);
            currentValue.append(value);
        }

        private static int getInt(Map<String, String> mm, String name) {
            String s = mm.get(name);
            if (s == null)
                return 0;
            return Integer.parseInt(s);
        }

        private String removeIndexes(String currentName) {
            StringBuilder b = new StringBuilder();
            boolean add = true;
            for (int i = 0; i < currentName.length(); i++) {
                char c = currentName.charAt(i);
                if (c == '[')
                    add = false;
                else if (c == ']')
                    add = true;
                else if (add)
                    b.append(Character.toString(c));
            }
            return b.toString();
        }
    }
}
