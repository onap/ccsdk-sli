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

import org.antlr.v4.runtime.tree.TerminalNode;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.AddExprContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.AtomContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.CompareExprContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.ConstantContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.ExprContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.FuncExprContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.MultExprContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.ParenExprContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.RelExprContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.VariableContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.VariableLeadContext;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.VariableTermContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicExprListener extends ExprGrammarBaseListener 
{




	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicExprListener.class);
	
	private SvcLogicExpression curExpr;
	private SvcLogicExpression topExpr;
	private LinkedList<SvcLogicExpression> exprStack;
	
	public SvcLogicExprListener()
	{
		exprStack = new LinkedList<>();
	}
	
	public SvcLogicExpression getParsedExpr()
	{
		return(curExpr);
	}

	private void pushOperand(SvcLogicExpression operand)
	{
		if (curExpr == null)
		{
			curExpr = operand;
		}
		else
		{
			curExpr.addOperand(operand);
		}
	}
	
	private void pushExpr(SvcLogicExpression expr)
	{
		LOG.trace("Pushing expression ["+expr.getClass().getName()+"]");
		if (curExpr != null)
		{
			exprStack.push(curExpr);
		}
		curExpr = expr;
	}
	
	private void popExpr()
	{
		if (exprStack.isEmpty())
		{
			LOG.trace("Popping last expression");
			topExpr = curExpr;
		}
		else
		{
			SvcLogicExpression lastExpr = curExpr;
			curExpr = exprStack.pop();
			curExpr.addOperand(lastExpr);
			LOG.trace("New curExpr is ["+curExpr.getClass().getName()+"]");
		}
		
	}
	
	@Override
	public void enterAtom(AtomContext ctx) {
		
		String atomText = ctx.getText();
		
		LOG.trace("enterAtom: text = "+atomText);

		
		SvcLogicAtom newAtom = new SvcLogicAtom(atomText);
		
		pushExpr(newAtom);
	}


	@Override
	public void enterMultExpr(MultExprContext ctx) {
		LOG.trace("enterMultExpr: text = "+ctx.getText());
		
		SvcLogicBinaryExpression curBinExpr = new SvcLogicBinaryExpression();
		pushExpr(curBinExpr);
		
		List<TerminalNode> opList = ctx.MULTOP();
		
		for (TerminalNode nd : opList)
		{
			LOG.trace("enterMultExpr: operator - "+nd.getText());
			curBinExpr.addOperator(nd.getText());
		}

	}

	@Override
	public void exitMultExpr(MultExprContext ctx) {

		LOG.trace("exitMultExpr: text = "+ctx.getText());

		popExpr();
		
	}

	@Override
	public void exitAtom(AtomContext ctx) {
		LOG.trace("exitAtom: text = "+ctx.getText());
		popExpr();
	}

	@Override
	public void enterAddExpr(AddExprContext ctx) {
		LOG.trace("enterAddExpr: text = "+ctx.getText());
		List<TerminalNode> opList = ctx.ADDOP();
		

		SvcLogicBinaryExpression curBinExpr = new SvcLogicBinaryExpression();
		pushExpr(curBinExpr);

		
		for (TerminalNode nd : opList)
		{
			LOG.trace("enterAddExpr: operator - "+nd.getText());
			curBinExpr.addOperator(nd.getText());
		}
		
	}

	@Override
	public void exitAddExpr(AddExprContext ctx) {
		LOG.trace("exitAddExpr: text = "+ctx.getText());
		
		popExpr();
	}

	@Override
	public void enterFuncExpr(FuncExprContext ctx) {
		LOG.trace("enterFuncExpr: text = "+ctx.getText());
		LOG.trace("enterFuncExpr - IDENTIFIER : "+ctx.IDENTIFIER().getText());
		
		for (ExprContext expr: ctx.expr())
		{
			LOG.trace("enterFuncExpr - expr = "+expr.getText());
		}
		

		pushExpr(new SvcLogicFunctionCall(ctx.IDENTIFIER().getText()));
	}

	@Override
	public void exitFuncExpr(FuncExprContext ctx) {
		LOG.trace("exitFuncExpr: text = "+ctx.getText());
		
		popExpr();
	}

	@Override
	public void enterParenExpr(ParenExprContext ctx) {
		LOG.trace("enterParenExpr: text = "+ctx.getText());
		LOG.trace("enterParenExpr: expr = "+ctx.expr().getText());
	}

	@Override
	public void exitParenExpr(ParenExprContext ctx) {
		LOG.trace("exitParenExpr: text = "+ctx.getText());
	}

	@Override
	public void enterRelExpr(RelExprContext ctx) {
		LOG.trace("enterRelExpr: text = "+ctx.getText());
		
		List<TerminalNode> opList = ctx.RELOP();
		

		SvcLogicBinaryExpression curBinExpr = new SvcLogicBinaryExpression();
		pushExpr(curBinExpr);

		
		for (TerminalNode nd : opList)
		{
			LOG.trace("enterRelExpr: operator - "+nd.getText());
			curBinExpr.addOperator(nd.getText());
		}
		
	}

	@Override
	public void exitRelExpr(RelExprContext ctx) {
		LOG.trace("exitRelExpr: text = "+ctx.getText());
		
		popExpr();
	}

	@Override
	public void enterCompareExpr(CompareExprContext ctx) {
		LOG.trace("enterCompareExpr: text = "+ctx.getText());
		
		TerminalNode nd = ctx.COMPAREOP();

		SvcLogicBinaryExpression curBinExpr = new SvcLogicBinaryExpression();
		pushExpr(curBinExpr);

		LOG.trace("enterCompareExpr: operator - "+nd.getText());
		curBinExpr.addOperator(nd.getText());

	}

	@Override
	public void exitCompareExpr(CompareExprContext ctx) {
		LOG.trace("exitCompareExpr : text = "+ctx.getText());
		
		popExpr();
	}


	
	@Override 
	public void enterConstant(ConstantContext ctx) {
		LOG.trace("enterConstant: text = "+ctx.getText());
	}

	@Override
	public void exitConstant(ConstantContext ctx) {
		LOG.trace("exitConstant: text = "+ctx.getText());
	}


	@Override
	public void enterVariable(VariableContext ctx) {
		LOG.trace("enterVariable: text = "+ctx.getText());
		
		
	}

	@Override
	public void exitVariable(VariableContext ctx) {
		LOG.debug("exitVariable: text ="+ctx.getText());
		
	}
	

	@Override
	public void enterVariableLead(VariableLeadContext ctx) {

		LOG.debug("enterVariableLead: text ="+ctx.getText());
		

	}

	@Override
	public void exitVariableLead(VariableLeadContext ctx) {

		LOG.trace("exitVariableLead: text ="+ctx.getText());
	}

	@Override
	public void enterVariableTerm(VariableTermContext ctx) {
		LOG.trace("enterVariableTerm: text ="+ctx.getText());
		
		String name = ctx.getText();
		
		int subscrStart = name.indexOf("[");
		if (subscrStart > -1)
		{
			name = name.substring(0, subscrStart);
		}
		SvcLogicVariableTerm vterm = new SvcLogicVariableTerm(name);
		pushExpr(vterm);
	}

	@Override
	public void exitVariableTerm(VariableTermContext ctx) {
		LOG.trace("exitVariableTerm: text="+ctx.getText());
		popExpr();
	}
}
