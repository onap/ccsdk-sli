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

public class SvcLogicFunctionCall extends SvcLogicExpression {
	
	private String functionName;
	
	public SvcLogicFunctionCall(String functionName)
	{
		this.functionName = functionName;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
	public String toString()
	{
		StringBuffer sbuff = new StringBuffer();
		
		sbuff.append(functionName);
		sbuff.append("(");
		boolean needComma = false;
		for (SvcLogicExpression operand: getOperands())
		{
			if (needComma)
			{
				sbuff.append(",");
			}
			else
			{
				needComma = true;
			}
			sbuff.append(operand.toString());
			
		}
		sbuff.append(")");
		return(sbuff.toString());
	}
	
	public String asParsedExpr()
	{
		StringBuffer sbuff = new StringBuffer();
		
		sbuff.append("(");
		sbuff.append(functionName);
		for (SvcLogicExpression operand: getOperands())
		{
			sbuff.append(" ");
			sbuff.append(operand.asParsedExpr());
		}
		sbuff.append(")");
		return(sbuff.toString());
	}

}
