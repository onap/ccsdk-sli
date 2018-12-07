/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

/**
 * 
 */
package org.onap.ccsdk.sli.core.sli;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicNode implements Serializable {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicExprListener.class);
	
	private static final long serialVersionUID = 2L;
	
	private String nodeName;
	private int nodeId;
	private String nodeType;
	private boolean visited;
	private SvcLogicGraph graph;


	private HashMap<String, SvcLogicExpression> attributes;
	private HashMap<String, SvcLogicNode> outcomes;
	private HashMap<String, SvcLogicExpression> parameters;
	
	public SvcLogicNode(int nodeId, String nodeType, SvcLogicGraph graph)
	{
		this.nodeId = nodeId;
		nodeName = "";
		this.nodeType = nodeType;
		this.graph = graph;
		attributes = new HashMap<> ();
		parameters = new HashMap<> ();
		outcomes = null;
		
	}
	
	public SvcLogicNode(int nodeId, String nodeType, String nodeName, SvcLogicGraph graph) throws DuplicateValueException
	{
		this.nodeId = nodeId;
		this.nodeName = nodeName;
		this.nodeType = nodeType;
		this.graph = graph;
		attributes = new HashMap<> ();
		parameters = new HashMap<> ();
		outcomes = null;
		graph.setNamedNode(nodeName, this);
	}
	
	
	public int getNodeId()
	{
		return nodeId;
	}
	
	public String getNodeName()
	{
		return nodeName;
	}
	
	public String getNodeType()
	{
		return nodeType;
	}
	
	public SvcLogicGraph getGraph()
	{
		return graph;
	}
	
	public int getNumOutcomes()
	{
		if (outcomes == null)
		{
			return 0;
		}
		else
		{
			return outcomes.size();
		}
	}
	
	public SvcLogicExpression getAttribute(String name)
	{
		if (attributes.containsKey(name))
		{
			return attributes.get(name);
		}
		else
		{
			return null;
		}
			
	}
	
	public void setAttribute(String name, String value) throws SvcLogicException
	{
		setAttribute(name, new SvcLogicAtom("STRING", value));
	}
	
	public void setAttribute(String name, SvcLogicExpression value) throws SvcLogicException
	{
		if (attributes.containsKey(name))
		{
			throw new DuplicateValueException("Duplicate attribute "+name);
		}
		
		attributes.put(name, value);
	}
	

	public void mapParameter(String name, String value) throws SvcLogicException
	{
		
		if (parameters.containsKey(name))
		{
			throw new DuplicateValueException("Duplicate parameter "+name);
		}
		try
		{
			SvcLogicExpression parmValue;
			if (value == null || value.length() == 0)
			{
				parmValue = new SvcLogicAtom("STRING", "");
			}
			else if (value.trim().startsWith("`"))
			{
				int lastParen = value.lastIndexOf("`");
				String evalExpr = value.trim().substring(1, lastParen);
				parmValue = SvcLogicExpressionFactory.parse(evalExpr);
				
			}
			else
			{
				if (Character.isDigit(value.charAt(0)))
				{
					parmValue = new SvcLogicAtom("NUMBER", value);
				}
				else
				{
					parmValue = new SvcLogicAtom("STRING", value);
				}
			}
			LOG.debug("Setting parameter "+name+" = "+value+" = "+parmValue.asParsedExpr());
			parameters.put(name, parmValue);
		}
		catch (IOException e) {

			LOG.error("Invalid parameter value expression ("+value+")");
			throw new SvcLogicException(e.getMessage());
		}
	}
	
	public SvcLogicExpression getParameter(String name)
	{
		if (parameters.containsKey(name))
		{
			return parameters.get(name);
		}
		else
		{
			return null;
		}
	}
	
	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited, boolean recursive) {
		this.visited = visited;
		
		if (recursive)
		{
			Set<Map.Entry<String, SvcLogicNode>> outcomeSet = getOutcomeSet();
			
			if (outcomeSet == null)
			{
				return;
			}
			
			for (Iterator<Map.Entry<String, SvcLogicNode>> iter = outcomeSet.iterator(); iter.hasNext();)
			{
				Map.Entry<String, SvcLogicNode> curOutcome = iter.next();
				SvcLogicNode outNode = curOutcome.getValue();
				outNode.setVisited(visited, recursive);
			}
		}
	}
	
	public void addOutcome(String outcomeValue, SvcLogicNode node) throws SvcLogicException
	{
		if (outcomes == null)
		{
			outcomes = new HashMap<>();
		}
		
		if (outcomeValue.length() == 0) {
			outcomeValue = "\"\"";
		}
		if (outcomes.containsKey(outcomeValue))
		{
			throw new DuplicateValueException("Duplicate outcome value "+outcomeValue);
		}
		
		outcomes.put(outcomeValue, node);
	}
	
	public Set<Map.Entry<String, SvcLogicNode>> getOutcomeSet()
	{
		if (outcomes == null)
		{
			return new HashSet<>();
		}
		
		return outcomes.entrySet();
		
	}
	
	public Set<Map.Entry<String, SvcLogicExpression>> getParameterSet()
	{
		if (parameters == null)
		{
			return new HashSet<>();
		}
		
		return parameters.entrySet();
		
	}
	
	public void printAsGv(PrintStream pstr)
	{
		
		if (visited)
		{
			return;
		}
		else
		{
			visited = true;
		}
		
		StringBuffer sbuff = new StringBuffer();
		
		sbuff.append("node");
		sbuff.append(nodeId);
		sbuff.append(" [ shape=none, margin=0, label=<<table border=\"0\" cellborder=\"1\" align=\"left\">");
		sbuff.append("<tr><td colspan=\"2\"><b>");
		sbuff.append(nodeId);
		sbuff.append(" : ");
		sbuff.append(nodeType);
		sbuff.append("</b></td></tr><th><td><i>Attribute</i></td><td><i>Value</i></td></th>");

		if (nodeName.length() > 0)
		{
			sbuff.append("<tr><td>name</td><td>");
			sbuff.append(nodeName);
			sbuff.append("</td></tr>");
		}
		
		Set<Map.Entry<String, SvcLogicExpression>> attrSet = attributes.entrySet();
		for (Iterator<Map.Entry<String, SvcLogicExpression>> iter = attrSet.iterator() ; iter.hasNext();)
		{
			Map.Entry<String, SvcLogicExpression> curAttr = iter.next();
			sbuff.append("<tr><td>");
			sbuff.append(curAttr.getKey());
			sbuff.append("</td><td>");
			sbuff.append(StringEscapeUtils.escapeHtml3(curAttr.getValue().toString()));
			sbuff.append("</td></tr>");
		}
		sbuff.append("</table>>];");
		
		pstr.println(sbuff.toString());
		
		
		if (outcomes != null)
		{
			TreeMap<String, SvcLogicNode> sortedOutcomes = new TreeMap<>(outcomes);
			Set<Map.Entry<String, SvcLogicNode>> outcomeSet = sortedOutcomes.entrySet();
			
			for (Iterator<Map.Entry<String, SvcLogicNode>> iter = outcomeSet.iterator(); iter.hasNext();)
			{
				Map.Entry<String, SvcLogicNode> curOutcome = iter.next();
				String outValue = curOutcome.getKey();
				SvcLogicNode outNode = curOutcome.getValue();
				pstr.println("node"+nodeId+" -> node"+outNode.getNodeId()+" [label=\""+outValue+"\"];");
				outNode.printAsGv(pstr);
			}
		}
	}
	
	public void printAsXml(PrintStream pstr, int indentLvl)
	{
		if (visited)
		{
			return;
		}
		// Print node tag
		for (int i = 0 ; i < indentLvl ; i++)
		{
			pstr.print("  ");
		}
		pstr.print("<");
		pstr.print(this.getNodeType());
		
		Set<Map.Entry<String, SvcLogicExpression>> attrSet = attributes.entrySet();
		for (Iterator<Map.Entry<String, SvcLogicExpression>> iter = attrSet.iterator() ; iter.hasNext();)
		{
			Map.Entry<String, SvcLogicExpression> curAttr = iter.next();
			pstr.print(" ");
			pstr.print(curAttr.getKey());
			pstr.print("='`");
			pstr.print(curAttr.getValue());
			pstr.print("'`");
		}
		
		if ((parameters == null || parameters.isEmpty()) &&
				(outcomes == null || outcomes.isEmpty()))
		{
			pstr.print("/>\n");
			pstr.flush();
			return;
		}
		else
		{
			pstr.print(">\n");
		}
		
		// Print parameters (if any)
		if (parameters != null)
		{
			Set<Map.Entry<String, SvcLogicExpression>> paramSet = parameters.entrySet();
			for (Iterator<Map.Entry<String, SvcLogicExpression>> iter = paramSet.iterator() ; iter.hasNext();)
			{
				for (int i = 0 ; i < indentLvl+1 ; i++)
				{
					pstr.print("  ");
				}
				pstr.print("<parameter");
				Map.Entry<String, SvcLogicExpression> curAttr = iter.next();
				pstr.print(" name='");
				pstr.print(curAttr.getKey());
				pstr.print("' value='`");
				pstr.print(curAttr.getValue().toString());
				pstr.print("`'/>\n");
			}
		}

		// Print outcomes (if any)
		if (outcomes != null)
		{
			Set<Map.Entry<String, SvcLogicNode>> outcomeSet = outcomes.entrySet();
			for (Iterator<Map.Entry<String, SvcLogicNode>> iter = outcomeSet.iterator() ; iter.hasNext();)
			{
				for (int i = 0 ; i < indentLvl+1 ; i++)
				{
					pstr.print("  ");
				}
				pstr.print("<outcome");
				Map.Entry<String, SvcLogicNode> curAttr = iter.next();
				pstr.print(" value='");
				pstr.print(curAttr.getKey());
				pstr.print("'>\n");
				SvcLogicNode outNode = curAttr.getValue();
				outNode.printAsXml(pstr, indentLvl+2);
				for (int i = 0 ; i < indentLvl+1 ; i++)
				{
					pstr.print("  ");
				}
				pstr.print("</outcome>\n");
			}
		}
		
		// Print node end tag
		for (int i = 0 ; i < indentLvl ; i++)
		{
			pstr.print("  ");
		}
		pstr.print("</");
		pstr.print(this.getNodeType());
		pstr.print(">\n");
		pstr.flush();
		
	}


	public SvcLogicNode getOutcomeValue(String value)
	{

		if (value.length() == 0) {
			value = "\"\"";
		}
		if (outcomes == null)
		{
			return null;
		}
		
		if (outcomes.containsKey(value))
		{
			return outcomes.get(value);
		}
		else
		{
			StringBuffer keyBuffer = new StringBuffer();
			keyBuffer.append("{");
			for (String key : outcomes.keySet()) {
				keyBuffer.append(" ("+key+")");
			}
			keyBuffer.append("}");
			LOG.info("Outcome (" + value + ") not found, keys are " + keyBuffer.toString());

			if (outcomes.containsKey("Other"))
			{
				return outcomes.get("Other");
			}
			else
			{
				return null;
			}
		}
	}
}
