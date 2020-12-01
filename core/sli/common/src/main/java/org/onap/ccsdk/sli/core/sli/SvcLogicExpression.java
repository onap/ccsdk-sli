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

package org.onap.ccsdk.sli.core.sli;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


public abstract class SvcLogicExpression implements Serializable {
	
	private List<SvcLogicExpression> operands = new LinkedList<>();
	
	
	public void addOperand(SvcLogicExpression expr)
	{
		operands.add(expr);
	}

	public List<SvcLogicExpression> getOperands() {
		return operands;
	}
	
	public int numOperands()
	{
		return(operands.size());
	}
	
	public abstract String asParsedExpr();

}
