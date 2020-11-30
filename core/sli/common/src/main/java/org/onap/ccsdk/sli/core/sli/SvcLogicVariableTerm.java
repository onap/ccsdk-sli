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

public class SvcLogicVariableTerm extends SvcLogicExpression {
	
	private String name = null;
	
	public String getName() {
		return name;
	}

	
	public SvcLogicVariableTerm(String identifier)
	{
		this.name = identifier;
	}
	
	public SvcLogicExpression getSubscript()
	{
		if (numOperands() > 0)
		{
			return(getOperands().get(0));
		}
		else
		{
			return(null);
		}
	}

    @Override
	public String toString()
	{
		String retval;
		
		if (numOperands() > 0)
		{
			retval = name + "[" + getSubscript().toString() + "]";
		}
		else
		{
			retval = name;
		}
		return(retval);
	}

	@Override
	public String asParsedExpr() {
		if (numOperands() == 0) {
			return("(variable-term "+name+")");
		}
		else
		{
			return("(variable-term "+name+" "+getSubscript().asParsedExpr()+")");
		}
	}

}
