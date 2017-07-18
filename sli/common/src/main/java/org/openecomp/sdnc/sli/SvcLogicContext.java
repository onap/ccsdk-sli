/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
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

package org.openecomp.sdnc.sli;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class SvcLogicContext {

	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicContext.class);
	
	private HashMap<String, String> attributes;
	
	private DOMDataBroker domDataBroker;
	
	private String status = "success";
	
	public SvcLogicContext()
	{
		this.attributes = new HashMap<String,String> ();
		
	}
	
	public SvcLogicContext(Properties props)
	{
		this.attributes = new HashMap<String, String> ();
		
		if (props.containsKey("SvcLogic.status"))
		{
			this.status = props.getProperty("SvcLogic.status");
		}
		
		for (Object nameObj : props.keySet())
		{
			String propName = (String) nameObj;
			attributes.put(propName, props.getProperty(propName));
		}
	}
	
	
	
	public DOMDataBroker getDomDataBroker() {
		return domDataBroker;
	}

	public void setDomDataBroker(DOMDataBroker domDataBroker) {
		this.domDataBroker = domDataBroker;
	}

	public String getAttribute(String name)
	{
		if (attributes.containsKey(name))
		{
			return(attributes.get(name));
		}
		else
		{
			return(null);
		}
	}
	
	public void setAttribute(String name, String value)
	{
		if (value == null) {
			if (attributes.containsKey(name)) {
				attributes.remove(name);
			}
		} else {
			attributes.put(name, value);
		}
	}
	
	public Set<String> getAttributeKeySet()
	{
		return(attributes.keySet());
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Properties toProperties()
	{
		Properties props = new Properties();
		
		if (status != null)
		{
			props.setProperty("SvcLogic.status", status);
		}
		
		for (String attrName : attributes.keySet())
		{
			String attrVal = attributes.get(attrName);
			if (attrVal == null) {
				LOG.warn("attribute " + attrName
						+ "null - setting to empty string");
				props.setProperty(attrName, "");
			} else {
				props.setProperty(attrName, attributes.get(attrName));
			}
		}
		
		return(props);
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
		
		LOG.trace("mergeElement("+pfx+","+element.getTagName()+","+nodeMap+")");

		String curTagName = element.getTagName();
		String prefix = curTagName;
		
		if (pfx != null) {
			prefix = pfx + "." + prefix;
		}
		
		int myIdx = 0;
		
		if (nodeMap != null) {
			if (nodeMap.containsKey(curTagName)) {
				myIdx = nodeMap.get(curTagName).intValue();
			}

			nodeMap.put(curTagName, new Integer(myIdx+1));
			this.setAttribute(prefix+"_length", ""+(myIdx+1));
		}
		
		NodeList children = element.getChildNodes();
		
		int numChildren  = children.getLength();
		
		Map<String, Integer> childMap = new HashMap<String, Integer>();
		Map<String, Integer> idxChildMap = new HashMap<String, Integer>();
		
		for (int i = 0 ; i < numChildren ; i++) {
			Node curNode = children.item(i);
			
			if (curNode instanceof Text) {
				Text curText = (Text) curNode;
				String curTextValue = curText.getTextContent();
				LOG.trace("Setting ctx variable "+prefix+" = "+curTextValue);
				this.setAttribute(prefix, curText.getTextContent());
				

			} else if (curNode instanceof Element) {
				mergeElement(prefix, (Element) curNode, childMap);
				if (nodeMap != null) {

					mergeElement(prefix+"["+myIdx+"]", (Element)curNode, idxChildMap);

				}
			}
		}
		
	}
	
	public String resolve(String ctxVarName) {

		if (ctxVarName.indexOf('[') == -1) {
			// Ctx variable contains no arrays
			return (this.getAttribute(ctxVarName));
		}

		// Resolve any array references
		StringBuffer sbuff = new StringBuffer();
		String[] ctxVarParts = ctxVarName.split("\\[");
		sbuff.append(ctxVarParts[0]);
		for (int i = 1; i < ctxVarParts.length; i++) {
			if (ctxVarParts[i].startsWith("$")) {
				int endBracketLoc = ctxVarParts[i].indexOf("]");
				if (endBracketLoc == -1) {
					// Missing end bracket ... give up parsing
					LOG.warn("Variable reference " + ctxVarName
							+ " seems to be missing a ']'");
					return (this.getAttribute(ctxVarName));
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

		return (this.getAttribute(sbuff.toString()));
	}

}
