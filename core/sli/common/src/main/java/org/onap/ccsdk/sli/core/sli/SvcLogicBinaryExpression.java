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

import java.util.LinkedList;
import java.util.List;

public class SvcLogicBinaryExpression extends SvcLogicExpression {
	

	public enum OperatorType {
		addOp("+"),
		subOp("-"),
		multOp("*"),
		divOp("/"),
		equalOp("=="),
		ltOp("<"),
		leOp("<="),
		gtOp(">"),
		geOp(">="),
		neOp("!="),
		andOp("and"),
		orOp("or");
		
		private String text;
		
		private OperatorType(String text)
		{
			this.text = text;
		}
		
		public String getText()
		{
			return(text);
		}
		
		public static OperatorType fromString(String text)
		{
			if (text != null)
			{
				for (OperatorType t : OperatorType.values())
				{
					if (text.equalsIgnoreCase(t.getText())) {
						
						return(t);
					}
				}
			}
			return(null);
		}
		
		public String toString()
		{
			return(text);
		}
	}
	private List<OperatorType> operators;
	
	public List<OperatorType> getOperators() {
		return operators;
	}

	public SvcLogicBinaryExpression()
	{
		operators = new LinkedList<>();
	}
	
	public void addOperator(String operator)
	{
		operators.add(OperatorType.fromString(operator));
	}

	
	public String toString()
	{
		
		List<SvcLogicExpression>operands = getOperands();
		StringBuffer sbuff = new StringBuffer();

		sbuff.append(operands.get(0).toString());
		for (int i = 0 ; i < operators.size(); i++)
		{
			sbuff.append(" ");
			sbuff.append(operators.get(i));
			sbuff.append(" ");
			if (i + 1 < operands.size()) {
				sbuff.append(operands.get(i + 1).toString());
			} else {
				// expression incomplete; operand not bound yet
				sbuff.append("?");
			}
		}
		
		return(sbuff.toString());

	}
	
	public String asParsedExpr() {

		List<SvcLogicExpression> operands = getOperands();

		if (operators.isEmpty()) {
			return operands.get(0).asParsedExpr();
		} else {
			StringBuffer sbuff = new StringBuffer();
			// operators in reverse order for left associativity
			for (int i = operators.size() - 1; i >= 0; --i) {
				sbuff.append("(");
				sbuff.append(operators.get(i).getText());
				sbuff.append(" ");
			}
			for (int i = 0; i < operators.size() + 1; ++i) {
				if (i < operands.size()) {
					sbuff.append(operands.get(i).asParsedExpr());
				} else {
					// expression incomplete; operand not bound yet
					sbuff.append("?");
				}
				if (i != 0) {
					sbuff.append(")");
				}
				if (i < operators.size()) {
					sbuff.append(" ");
				}
			}
			return sbuff.toString();
		}
	}

}
