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

import java.util.*;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class SvcLogicContext {

    private static final Logger LOG = LoggerFactory.getLogger(SvcLogicContext.class);
    private final SecurePrinter securePrinter = new SecurePrinter();
    public static final String CTX_NULL_VALUE="";
    private static final String LENGTH="_length";

    private HashMap<String, String> attributes;

    private String status = SvcLogicConstants.SUCCESS;

    public SvcLogicContext() {
        this.attributes = new HashMap<>();

    }

    public SvcLogicContext(Properties props) {
        this.attributes = new HashMap<>();

        if (props.containsKey(CommonConstants.SERVICE_LOGIC_STATUS)) {
            this.status = props.getProperty(CommonConstants.SERVICE_LOGIC_STATUS);
        }

        for (Object nameObj : props.keySet()) {
            String propName = (String) nameObj;
            attributes.put(propName, props.getProperty(propName));
        }
    }

    public String getAttribute(String name) {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        } else {
            return null;
        }
    }

    public void setAttribute(String name, String value) {
        if (value == null) {
            if (attributes.containsKey(name)) {
                attributes.remove(name);
            }
        } else {
            attributes.put(name, value);
        }
    }

    public Set<String> getAttributeKeySet() {
        return attributes.keySet();
    }

    public Boolean isSuccess() {
        return status.equals(SvcLogicConstants.SUCCESS);
    }

    @Deprecated
    public String getStatus() {
        return status;
    }

    @Deprecated
    public void setStatus(String status) {
        this.status = status;
    }

    public void markFailed() {
        this.status = SvcLogicConstants.FAILURE;
    }

    public void markSuccess() {
        this.status = SvcLogicConstants.SUCCESS;
    }

    public Properties toProperties() {
        Properties props = new Properties();

        if (status != null) {
            props.setProperty(CommonConstants.SERVICE_LOGIC_STATUS, status);
        }

        String attrName;
        String attrVal;
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            attrName = entry.getKey();
            attrVal = entry.getValue();
            if (attrVal == null) {
                LOG.warn("attribute {} value is null - setting to empty string", attrName);
                props.setProperty(attrName, "");
            } else {
                props.setProperty(attrName, attrVal);
            }
        }

        return props;
    }

    public void mergeDocument(String pfx, Document doc) {
        String prefix = "";

        if (pfx != null) {
            prefix = pfx;
        }

        Element root = doc.getDocumentElement();

        mergeElement(prefix, root, null);
    }

    public void mergeElement(String pfx, Element element, Map<String, Integer> nodeMap) {

        // In XML, cannot tell the difference between containers and lists.
        // So, have to treat each element as both (ugly but necessary).
        // We do this by passing a nodeMap to be used to count instance of each tag,
        // which will be used to set _length and to set index

        LOG.trace("mergeElement({},{},{})", pfx, element.getTagName(), nodeMap);

        String curTagName = element.getTagName();
        String prefix = curTagName;

        if (pfx != null) {
            prefix = pfx + "." + prefix;
        }

        int myIdx = 0;

        if (nodeMap != null) {
            if (nodeMap.containsKey(curTagName)) {
                myIdx = nodeMap.get(curTagName);
            }

            nodeMap.put(curTagName, myIdx + 1);
            this.setAttribute(prefix + "_length", Integer.toString(myIdx + 1));
        }

        NodeList children = element.getChildNodes();

        int numChildren = children.getLength();

        Map<String, Integer> childMap = new HashMap<>();
        Map<String, Integer> idxChildMap = new HashMap<>();

        for (int i = 0; i < numChildren; i++) {
            Node curNode = children.item(i);

            if (curNode instanceof Text) {
                Text curText = (Text) curNode;
                String curTextValue = curText.getTextContent();
                LOG.trace("Setting ctx variable {} = {}", prefix, curTextValue);
                this.setAttribute(prefix, curText.getTextContent());


            } else if (curNode instanceof Element) {
                mergeElement(prefix, (Element) curNode, childMap);
                if (nodeMap != null) {

                    mergeElement(prefix + "[" + myIdx + "]", (Element) curNode, idxChildMap);

                }
            }
        }

    }

    public void mergeJson(String pfx, String jsonString) {

        JsonParser jp = new JsonParser();
        JsonElement element = jp.parse(jsonString);
        String root = "";
        if ((pfx != null) && (pfx.length() > 0)) {
            root = pfx + ".";
        }
        if (element.isJsonObject()) {
            writeJsonObject(element.getAsJsonObject(), root);
        } else if (element.isJsonArray()) {
            handleJsonArray("", element.getAsJsonArray(), root);
        }
    }


    protected void writeJsonObject(JsonObject obj, String root) {
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().isJsonObject()) {
                writeJsonObject(entry.getValue().getAsJsonObject(), root + key + ".");
            } else if (entry.getValue().isJsonArray()) {
                JsonArray array = entry.getValue().getAsJsonArray();
                handleJsonArray(key, array, root);
            } else {
                //Handles when a JSON obj is nested within a JSON obj
                if(!root.endsWith(".")){
                    root = root + ".";
                }
                if(entry.getValue().isJsonNull()) {
                    this.setAttribute(root + key, CTX_NULL_VALUE);
                }else {
                    this.setAttribute(root + key, entry.getValue().getAsString());
                }
            }
        }
    }

    protected void handleJsonArray(String key, JsonArray array,  String root) {
        this.setAttribute(root + key + LENGTH, String.valueOf(array.size()));
        Integer arrayIdx = 0;
        for (JsonElement element : array) {
            String prefix = root + key + "[" + arrayIdx + "]";

            if (element.isJsonArray()) {
                handleJsonArray(key, element.getAsJsonArray(), prefix);
            } else if (element.isJsonObject()) {
                writeJsonObject(element.getAsJsonObject(),  prefix + ".");
            } else if (element.isJsonNull()) {
                this.setAttribute(prefix, CTX_NULL_VALUE);
            } else if (element.isJsonPrimitive()) {
                this.setAttribute(prefix, element.getAsString());
            }
            arrayIdx++;
        }
    }


    public String resolve(String ctxVarName) {

        if (ctxVarName.indexOf('[') == -1) {
            // Ctx variable contains no arrays
            return getAttribute(ctxVarName);
        }

        // Resolve any array references
        StringBuilder sbuff = new StringBuilder();
        String[] ctxVarParts = ctxVarName.split("\\[");
        sbuff.append(ctxVarParts[0]);
        for (int i = 1; i < ctxVarParts.length; i++) {
            if (ctxVarParts[i].startsWith("$")) {
                int endBracketLoc = ctxVarParts[i].indexOf(']');
                if (endBracketLoc == -1) {
                    // Missing end bracket ... give up parsing
                    LOG.warn("Variable reference {} seems to be missing a ']'", ctxVarName);
                    return getAttribute(ctxVarName);
                }

                String idxVarName = ctxVarParts[i].substring(1, endBracketLoc);
                String remainder = ctxVarParts[i].substring(endBracketLoc);

                sbuff.append("[");
                sbuff.append(this.getAttribute(idxVarName));
                sbuff.append(remainder);

            } else {
                // Index is not a variable reference
                sbuff.append("[");
                sbuff.append(ctxVarParts[i]);
            }
        }

        return getAttribute(sbuff.toString());
    }

    public String toJsonString(String pfx) {
        JsonParser jp = new JsonParser();

        String jsonString = this.toJsonString();
        JsonObject jsonRoot = (JsonObject) jp.parse(jsonString);
        JsonObject targetJson = jsonRoot.getAsJsonObject(pfx);
        return(targetJson.toString());
    }

    public String toJsonString() {
        JsonObject root = new JsonObject();
        JsonElement lastJsonObject = root;
        JsonElement currJsonLeaf = root;

        String attrName = null;
        String attrVal = null;

        // Sort properties so that arrays will be reconstructed in proper order
        TreeMap<String, String> sortedAttributes = new TreeMap<>();
        sortedAttributes.putAll(attributes);

        // Loop through properties, sorted by key
        for (Map.Entry<String, String> entry : sortedAttributes.entrySet()) {
            attrName = entry.getKey();
            attrVal = entry.getValue();

            currJsonLeaf = root;
            String curFieldName = null;
            JsonArray curArray = null;
            lastJsonObject = null;
            boolean addNeeded = false;

            // Split property names by period and iterate through parts
            for (String attrNamePart : attrName.split("\\.")) {

            	// Add last object found to JSON tree.  Need to handle
				// this way because last element found (leaf) needs to be
				// assigned the property value.
                if (lastJsonObject != null) {
                    if (addNeeded) {
                        if (currJsonLeaf.isJsonArray()) {
                            ((JsonArray) currJsonLeaf).add(lastJsonObject);
                        } else {
                            ((JsonObject) currJsonLeaf).add(curFieldName, lastJsonObject);
                        }
                    }
                    currJsonLeaf = (JsonObject) lastJsonObject;
                }
                addNeeded = false;
                // See if current level should be a JsonArray or JsonObject based on
				// whether name part contains square brackets.
                if (!attrNamePart.contains("[")) {
                	// This level should be inserted as a JsonObject
                    curFieldName = attrNamePart;
                    lastJsonObject = ((JsonObject) currJsonLeaf).get(curFieldName);
                    if (lastJsonObject == null) {
                        lastJsonObject = new JsonObject();
                        addNeeded = true;
                    } else if (!lastJsonObject.isJsonObject()) {
                        LOG.error("Unexpected condition - expecting to find JsonObject, but found " + lastJsonObject.getClass().getName());
                        lastJsonObject = new JsonObject();
                        addNeeded = true;
                    }
                } else {
                	// This level should be inserted as a JsonArray.

                    String[] curFieldNameParts = attrNamePart.split("[\\[\\]]");
                    curFieldName = curFieldNameParts[0];
                    int curIndex = Integer.parseInt(curFieldNameParts[1]);


                    curArray = ((JsonObject) currJsonLeaf).getAsJsonArray(curFieldName);

                    if (curArray == null) {
                    	// This is the first time we see this array.
						// Create a new JsonArray and add it to current
						// leaf
                        curArray = new JsonArray();
                        ((JsonObject) currJsonLeaf).add(curFieldName, curArray);
                    }

                    // Current leaf should point to the JsonArray for this level.
					// lastJsonObject should point to the array item entry to append
					// the next level to - which is a new one if the index value
					// isn't the end of the current array.
                    currJsonLeaf = curArray;
                    if (curArray.size() == curIndex + 1) {
                        lastJsonObject = curArray.get(curArray.size() - 1);
                    } else {
                        lastJsonObject = new JsonObject();
                        addNeeded = true;
                    }
                }
            }

            // Done parsing property name.  Add the value of this
			// property to the current json leaf, either as a property
			// or as a string (if the current leaf is a JsonArray)

            if (!curFieldName.endsWith("_length")) {
                if (currJsonLeaf.isJsonArray()) {
                    if ("true".equals(attrVal) || "false".equals(attrVal)) {
                        ((JsonArray) currJsonLeaf).add(Boolean.valueOf(attrVal));
                    } else if ("null".equals(attrVal)) {
                        ((JsonArray) currJsonLeaf).add(new JsonNull());
                    } else {
                        ((JsonArray) currJsonLeaf).add(attrVal);
                    }
                } else {
                    if (("true".equals(attrVal) || "false".equals(attrVal))) {
                        ((JsonObject) currJsonLeaf).addProperty(curFieldName, Boolean.valueOf(attrVal));
                    } else if ("null".equals(attrVal)){

                        ((JsonObject) currJsonLeaf).add(curFieldName, new JsonNull());
                    } else {
                        ((JsonObject) currJsonLeaf).addProperty(curFieldName, attrVal);
                    }
                }
            }
        }

        return (root.toString());
    }

    public void printProperties(Properties props) {
        securePrinter.printProperties(props);
    }

    public void printAttributes() {
        securePrinter.printAttributes(attributes);
    }

    public void printProperties(Properties props, String subpath) {
        securePrinter.printProperties(props, subpath);
    }

    public void printAttributes(String subpath) {
        securePrinter.printAttributes(attributes, subpath);
    }
}
