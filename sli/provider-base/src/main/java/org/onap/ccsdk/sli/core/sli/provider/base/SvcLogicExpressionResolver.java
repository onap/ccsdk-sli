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

package org.onap.ccsdk.sli.core.sli.provider.base;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicAtom;
import org.onap.ccsdk.sli.core.sli.SvcLogicBinaryExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicFunctionCall;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicVariableTerm;
import org.onap.ccsdk.sli.core.sli.SvcLogicAtom.AtomType;
import org.onap.ccsdk.sli.core.sli.SvcLogicBinaryExpression.OperatorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicExpressionResolver {

	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicExpressionResolver.class);
	private static final String INVALID_EXPRESSION_MSG= "Invalid expression (";
    private static final String EXPRESSION_DEBUG_PATTERN = "Expression: {} resolves to {}: which has the value {}";

	public static String evaluate(SvcLogicExpression expr, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {
		if (expr == null) {
			return (null);
		}

		if (expr instanceof SvcLogicAtom) {
			SvcLogicAtom atom = (SvcLogicAtom) expr;

			AtomType atomType = atom.getAtomType();
			switch (atomType) {
			case NUMBER:
			case STRING:
				return (atom.toString());
			case CONTEXT_VAR:
			case IDENTIFIER:
	
				String varName = resolveVariableName(atom, node, ctx);
				
				if (atomType == AtomType.CONTEXT_VAR)
				{
					
					String varValue = ctx.getAttribute(varName);
					
					if (varValue == null) {
						LOG.trace("Context variable $"+varName+" unset - treating as empty string");
						varValue = "";
					}
					LOG.trace(EXPRESSION_DEBUG_PATTERN,expr.toString(),varName,varValue);
					return (varValue);
				}
				SvcLogicExpression parm = node.getParameter(varName);
				if (parm != null) {				
					String value = evaluate(parm, node, ctx);
                    LOG.trace(EXPRESSION_DEBUG_PATTERN,expr.toString(),varName,value);
					return value;
				}
				else
				{
                    LOG.trace(EXPRESSION_DEBUG_PATTERN,expr.toString(),varName,varName);
					return(varName);
				}
			default:
				return(null);
			}

		} else if (expr instanceof SvcLogicBinaryExpression) {
			SvcLogicBinaryExpression binExpr = (SvcLogicBinaryExpression) expr;
			List<OperatorType> operators = binExpr.getOperators();
			if (operators.isEmpty())
			{
				List<SvcLogicExpression> operands = binExpr.getOperands();
				if (operands.size() == 1)
				{
					return(evaluate(operands.get(0), node, ctx));
				}
				else
				{
					if (operands.isEmpty())
					{
						LOG.error("SvcLogicBinaryExpression has no operators and no operands - evaluating value as null");
					}
					else
					{
						LOG.error("SvcLogicBinaryExpression has no operators and "+operands.size()+" operands - evaluating value as null");
					}
					return(null);
				}
			}
			switch (operators.get(0)) {
				case addOp:
				case subOp:
				case multOp:
				case divOp:
					return(evalArithExpression(binExpr, node, ctx));
				case equalOp:
				case neOp:
				case ltOp:
				case leOp:
				case gtOp:
				case geOp:
					return (evalCompareExpression(binExpr, node, ctx));
				case andOp:
				case orOp:
					return(evalLogicExpression(binExpr, node, ctx));

				default:
					return(null);
			}
		}
		else if (expr instanceof SvcLogicFunctionCall)
		{
			return(evalFunctionCall((SvcLogicFunctionCall)expr, node, ctx));
		}
		else
		{
			throw new SvcLogicException("Unrecognized expression type ["+expr+"]");
		}
	}

	private static String evalArithExpression(SvcLogicBinaryExpression binExpr, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {
		List<SvcLogicExpression> operands = binExpr.getOperands();
		List<OperatorType> operators = binExpr.getOperators();
		if (operands.size() != (operators.size()+1))
		{
			throw new SvcLogicException(INVALID_EXPRESSION_MSG+binExpr+")");
		}	
		String retval = evaluate(operands.get(0), node, ctx);
		String retsval = retval;
		long retlval = 0;
		boolean valueIsLong = false;

		int i = 1;
		try
		{
			
			if ((retval.length() > 0) && StringUtils.isNumeric(retval))
			{
				retlval = Long.parseLong(retval);
				valueIsLong = true;
			}
			for (OperatorType operator: operators)
			{
				String curOperandValue = evaluate(operands.get(i++), node, ctx);
				switch(operator) {
				case addOp:
					retsval = retsval + curOperandValue;
					if (valueIsLong)
					{
						if ((curOperandValue.length() > 0) && StringUtils.isNumeric(curOperandValue) )
						{
							retlval = retlval + Long.parseLong(curOperandValue);
						}
						else
						{
							valueIsLong = false;
						}
					}
					break;
				case subOp:
					retlval = retlval - Long.parseLong(curOperandValue);
					break;
				case multOp:
					retlval = retlval * Long.parseLong(curOperandValue);
					break;
				case divOp:
					retlval = retlval / Long.parseLong(curOperandValue);
					break;
				}

			}
		}
		catch (NumberFormatException e1)
		{
			throw new SvcLogicException("Illegal value in arithmetic expression", e1);
		}
		
		if (valueIsLong)
		{
			return("" + retlval);
		}
		else
		{
			return(retsval);
		}
		
	}


	
	private static String evalCompareExpression(SvcLogicBinaryExpression expr, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException
	{

		List<OperatorType> operators = expr.getOperators();
		List<SvcLogicExpression> operands = expr.getOperands();
		
		if ((operators.size() != 1) || (operands.size() != 2))
		{
			throw new SvcLogicException ("Invalid comparison expression : "+expr);
		}
		
		OperatorType operator = operators.get(0);
		String op1Value = evaluate(operands.get(0), node, ctx);
		String op2Value = evaluate(operands.get(1), node, ctx);
		
		if ((StringUtils.isNotEmpty(op1Value) && StringUtils.isNumeric(op1Value) && StringUtils.isNotEmpty(op2Value) && StringUtils.isNumeric(op2Value)))
		{
			try
			{
				double op1dbl = Double.parseDouble(op1Value);
				double op2dbl = Double.parseDouble(op2Value);
				
				switch(operator)
				{
				case equalOp:
					return(Boolean.toString(op1dbl == op2dbl));
				case neOp:
					return(Boolean.toString(op1dbl != op2dbl));
				case ltOp:
					return(Boolean.toString(op1dbl < op2dbl));
				case leOp:
					return(Boolean.toString(op1dbl <= op2dbl));
				case gtOp:
					return(Boolean.toString(op1dbl > op2dbl));
				case geOp:
					return(Boolean.toString(op1dbl >= op2dbl));
				default:
					return(null);
				}
			}
			catch (NumberFormatException e)
			{
				throw new SvcLogicException("Caught exception trying to compare numeric values", e);
			}
		}
		else
		{
			
			int compResult = 0;
			
			if (op1Value == null) {
				compResult = -1;
			} else if (op2Value == null ) {
				compResult = 1;
			} else {
				compResult = op1Value.compareToIgnoreCase(op2Value);
			}
			
			switch(operator)
			{
			case equalOp:
				return(Boolean.toString(compResult == 0));
			case neOp:
				return(Boolean.toString(compResult != 0));
			case ltOp:
				return(Boolean.toString(compResult < 0));
			case leOp:
				return(Boolean.toString(compResult <= 0));
			case gtOp:
				return(Boolean.toString(compResult > 0));
			case geOp:
				return(Boolean.toString(compResult >= 0));
			default:
				return(null);
			}
		}
		
	}
	
	private static String evalLogicExpression(SvcLogicBinaryExpression expr, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException
	{
		boolean retval;
		
		List<SvcLogicExpression> operands = expr.getOperands();
		List<OperatorType> operators = expr.getOperators();
		
		if (operands.size() != (operators.size()+1))
		{
			throw new SvcLogicException(INVALID_EXPRESSION_MSG+expr+")");
		}
		
		try
		{
			retval = Boolean.parseBoolean(evaluate(operands.get(0), node, ctx));
			int i = 1;
			for (OperatorType operator : operators)
			{
				if (operator == OperatorType.andOp)
				{
					retval = retval && Boolean.parseBoolean(evaluate(operands.get(i++), node, ctx));
				}
				else
				{

					retval = retval || Boolean.parseBoolean(evaluate(operands.get(i++), node, ctx));
				}
				
			}
		}
		catch (Exception e)
		{
			throw new SvcLogicException(INVALID_EXPRESSION_MSG+expr+")");
		}
		
		
		return(Boolean.toString(retval));
	}
	
	private static String evalFunctionCall(SvcLogicFunctionCall func, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException
	{
		String funcName = func.getFunctionName();
		List<SvcLogicExpression> operands = func.getOperands();
		
		if ("length".equalsIgnoreCase(funcName))
		{
			
			if (operands.size() == 1)
			{
				String opValue = evaluate(operands.get(0), node, ctx);
				return(""+opValue.length());
			}
			else
			{
				throw new SvcLogicException("Invalid call to length() function");
			}
		}
		else if ("substr".equalsIgnoreCase(funcName))
		{
			if (operands.size() == 3)
			{
				String op1Value = evaluate(operands.get(0), node, ctx);
				String op2Value = evaluate(operands.get(1), node, ctx);
				String op3Value = evaluate(operands.get(2), node, ctx);
				
				if (!StringUtils.isNumeric(op2Value) || !StringUtils.isNumeric(op3Value))
				{
					throw new SvcLogicException("Invalid arguments to substr() function");
				}
				
				try
				{
					return(op1Value.substring(Integer.parseInt(op2Value), Integer.parseInt(op3Value)));
				}
				catch (Exception e)
				{
					throw new SvcLogicException("Caught exception trying to take substring", e);
				}
			}
			else
			{

				throw new SvcLogicException("Invalid call to substr() function");
			}
			
		}
		else if ("toUpperCase".equalsIgnoreCase(funcName))
		{
			if (operands.size() == 1)
			{
				String opValue = evaluate(operands.get(0), node, ctx);
				if (opValue != null) {
					return(opValue.toUpperCase());
				} else {
					return("");
				}
			}
			else
			{
				throw new SvcLogicException("Invalid call to toUpperCase() function");
			}
		}
		else if ("toLowerCase".equalsIgnoreCase(funcName))
		{
			if (operands.size() == 1)
			{
				String opValue = evaluate(operands.get(0), node, ctx);
				if (opValue != null) {
					return(opValue.toLowerCase());
				} else {
					return("");
				}
			}
			else
			{
				throw new SvcLogicException("Invalid call to toLowerCase() function");
			}
		}
		else if ("convertBase".equalsIgnoreCase(funcName)) {
			int fromBase = 10;
			int toBase = 10;
			String srcString = "";
			
			if (operands.size() == 2)
			{
				fromBase = 10;
				srcString = evaluate(operands.get(0), node, ctx);
				toBase = Integer.parseInt(evaluate(operands.get(1), node, ctx));
			} else if (operands.size() == 3) {

				srcString = evaluate(operands.get(0), node, ctx);
				fromBase = Integer.parseInt(evaluate(operands.get(1), node, ctx));
				toBase = Integer.parseInt(evaluate(operands.get(2), node, ctx));
			} else {
				throw new SvcLogicException("Invalid call to convertBase() function");
			}
			
			long srcValue = Long.parseLong(srcString, fromBase);
			return(Long.toString(srcValue, toBase));
		}
		else
		{
			throw new SvcLogicException("Unrecognized function ("+funcName+")");
		}
		
	}
	
	public static String evaluateAsKey(SvcLogicExpression expr, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {
		if (expr == null) {
			return (null);
		}

	

		if (expr instanceof SvcLogicAtom) {
			SvcLogicAtom atom = (SvcLogicAtom) expr;

			AtomType atomType = atom.getAtomType();
			StringBuffer varNameBuff = new StringBuffer();
			switch (atomType) {
			case NUMBER:
				return (atom.toString());
			case STRING:
				return("'"+atom.toString()+"'");
			case CONTEXT_VAR:
			case IDENTIFIER:
				boolean needDot = false;
                for (SvcLogicExpression term : atom.getOperands())
                {
                	if (needDot)
                	{
                		varNameBuff.append(".");
                	}
                	if (term instanceof SvcLogicVariableTerm)
                	{
                		SvcLogicVariableTerm vterm = (SvcLogicVariableTerm) term;
                		varNameBuff.append(vterm.getName());
                		if (vterm.numOperands() > 0)
                		{
                			varNameBuff.append("[");
                			varNameBuff.append(evaluate(vterm.getSubscript(), node, ctx));
                			varNameBuff.append("]");
                			
                		}
                	}
                	else
                	{
                		varNameBuff.append(term.toString());
                	}
                	needDot = true;
                }

				String varName = varNameBuff.toString();
				String ctxValue = ctx.getAttribute(varName);
				if (ctxValue == null)
				{
					return(null);
				}
				if (StringUtils.isNumeric(ctxValue))
				{
					return(ctxValue);
				}
				else
				{
					return("'"+ctxValue+"'");
				}
	
			default:
				return(null);
			}

		} else if (expr instanceof SvcLogicBinaryExpression) {
			SvcLogicBinaryExpression binExpr = (SvcLogicBinaryExpression) expr;
			List<OperatorType> operators = binExpr.getOperators();
			List<SvcLogicExpression> operands = binExpr.getOperands();
			if (operators.isEmpty())
			{
				if (operands.size() == 1)
				{
					LOG.debug("SvcLogicBinaryExpression as no operator and one operand - evaluating its operand");
					return(evaluateAsKey(operands.get(0), node, ctx));
				}
				else
				{
					if (operands.isEmpty())
					{
						LOG.error("SvcLogicBinaryExpression has no operators and no operands - evaluating value as null");
					}
					else
					{
						LOG.error("SvcLogicBinaryExpression has no operators and "+operands.size()+" operands - evaluating value as null");
					}
					return(null);
				}
			}
			StringBuffer sbuff = new StringBuffer();
			sbuff.append(evaluateAsKey(operands.get(0), node, ctx));
			int i = 1;
			for (OperatorType operator : operators)
			{
				sbuff.append(" ");
				sbuff.append(operator.toString());
				sbuff.append(" ");
				sbuff.append(evaluateAsKey(operands.get(i++), node,ctx));
			}
			return(sbuff.toString());
		}
		else if (expr instanceof SvcLogicFunctionCall)
		{
			StringBuffer sbuff = new StringBuffer();
			SvcLogicFunctionCall funcCall = (SvcLogicFunctionCall) expr;
			sbuff.append(funcCall.getFunctionName());
			sbuff.append("(");
			boolean needComma = false;
			for (SvcLogicExpression operand : funcCall.getOperands())
			{
				if (needComma)
				{
					sbuff.append(",");
				}
				else
				{
					needComma = true;
				}
				sbuff.append(evaluateAsKey(operand, node, ctx));
			}
			sbuff.append(")");
			return(sbuff.toString());
		}
		else
		{
			throw new SvcLogicException("Unrecognized expression type ["+expr+"]");
		}
	}
	
	public static String resolveVariableName(SvcLogicExpression atom, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException
	{
		StringBuffer varNameBuff = new StringBuffer();
		
		boolean needDot = false;
		for (SvcLogicExpression term : atom.getOperands())
		{
			if (needDot)
			{
				varNameBuff.append(".");
			}
			if (term instanceof SvcLogicVariableTerm)
			{
				SvcLogicVariableTerm vterm = (SvcLogicVariableTerm) term;
				varNameBuff.append(vterm.getName());
				if (vterm.numOperands() > 0)
				{
					varNameBuff.append("[");
					varNameBuff.append(evaluate(vterm.getSubscript(), node, ctx));
					varNameBuff.append("]");
				}
			}
			else
			{
				varNameBuff.append(term.toString());
			}
			needDot = true;
		}
		return(varNameBuff.toString());
	}

}
