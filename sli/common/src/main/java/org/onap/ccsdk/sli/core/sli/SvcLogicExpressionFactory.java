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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.onap.ccsdk.sli.core.sli.ExprGrammarParser.ExprContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SvcLogicExpressionFactory {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicExpressionFactory.class);

	
	public static SvcLogicExpression parse(String exprStr) throws IOException
	{
		InputStream exprStream = new ByteArrayInputStream(exprStr.getBytes());
		CharStream input = new ANTLRInputStream(exprStream);
		ExprGrammarLexer lexer = new ExprGrammarLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ExprGrammarParser parser = new ExprGrammarParser(tokens);

		lexer.removeErrorListeners();
		lexer.addErrorListener(SvcLogicExprParserErrorListener.getInstance());		
		parser.removeErrorListeners();
		parser.addErrorListener(SvcLogicExprParserErrorListener.getInstance());

		ExprContext expression = null;
		
		try {
			expression = parser.expr();
		} catch (Exception e) {
			String errorMsg = e.getMessage();
			
			LOG.error(errorMsg);
			throw new SvcLogicParserException(errorMsg);
		}
		
	
		ParseTreeWalker walker = new ParseTreeWalker();
		SvcLogicExprListener listener = new SvcLogicExprListener();
		walker.walk(listener, expression);
		
		
		return(listener.getParsedExpr());
	}
	
	public static void main(String argv[]) {


		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");

		StringBuffer sbuff = new StringBuffer();
		
		for (int i = 0 ; i < argv.length ; i++)
		{
			if (sbuff.length() > 0)
			{
				sbuff.append(" ");
			}
			sbuff.append(argv[i]);
		}
		
		try {
			SvcLogicExpressionFactory.parse(sbuff.toString());
		} catch (IOException e) {
			LOG.error("Exception in SvcLogicExpressionFactory.parse",e);
		}
	}
}
