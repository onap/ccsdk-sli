/*-
x * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Modifications copyright (C) 2017 AT&T Intellectual Property. All rights
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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SvcLogicGraph implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String module = null;
	private String rpc = null;
	private String mode = null;
	private String version = null;

  private String md5sum = null;

	private Map<String, Serializable> attributes;
	private Map<String, SvcLogicNode> namedNodes;
	private SvcLogicNode rootNode;
	
	public SvcLogicGraph()
	{
		attributes = new HashMap<>();
		namedNodes = new HashMap<>();
		rootNode = null;
	}

  public String getMd5sum() {
    return md5sum;
  }


  public void setMd5sum(String md5sum) {
    this.md5sum = md5sum;
  }

	
	
	public String getModule() {
		return module;
	}


	public void setModule(String module) {
		this.module = module;
	}


	public String getRpc() {
		return rpc;
	}


	public void setRpc(String rpc) {
		this.rpc = rpc;
	}

	


	public String getMode() {
		return mode;
	}


	public void setMode(String mode) {
		this.mode = mode;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	public void setRootNode(SvcLogicNode rootNode)
	{
		this.rootNode = rootNode;
	}
	
	public SvcLogicNode getRootNode()
	{
		return(rootNode);
	}
	
	public Serializable getAttribute(String name)
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
	
	public void setAttribute(String name, Serializable value) throws DuplicateValueException
	{
		if (attributes.containsKey(name))
		{
			throw new DuplicateValueException("Duplicate attribute "+name);
		}
		
		attributes.put(name, value);
	}
	
	public SvcLogicNode getNamedNode(String nodeName)
	{
		if (namedNodes.containsKey(nodeName))
		{
			return(namedNodes.get(nodeName));
		}
		else
		{
			return(null);
		}
	}

	public void setNamedNode(String nodeName, SvcLogicNode node) throws DuplicateValueException
	{
		if (namedNodes.containsKey(nodeName))
		{
			throw new DuplicateValueException("Duplicate node name "+nodeName);
		}
		
		namedNodes.put(nodeName, node);
	}
	
	
	
	public void printAsGv(PrintStream pstr)
	{
		pstr.println("digraph g {");
		pstr.println("START [label=\"START\\n"+module+":"+rpc+"\"];");
		
		if (rootNode != null)
		{
			pstr.println("START -> node"+rootNode.getNodeId()+";");
			rootNode.setVisited(false, true);
			rootNode.printAsGv(pstr);
		}
		pstr.println("}");
	}
	
	public void printAsXml(PrintStream pstr)
	{
		pstr.println("<service-logic module='"+getModule()+"' version='"+getVersion()+"'>");
		pstr.println("  <method rpc='"+getRpc()+"' mode='"+getMode()+"'>");
		if (rootNode != null)
		{
			rootNode.setVisited(false, true);
			rootNode.printAsXml(pstr, 2);
		}
		pstr.println("  </method>");
		pstr.println("</service-logic>");
	}
	
	@Override
	public String toString() {
	    return "SvcLogicGraph [module=" + module + ", rpc=" + rpc + ", mode=" + mode + ", version=" + version + ", md5sum=" + md5sum + "]";
	}
	
}
