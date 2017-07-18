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

package org.openecomp.sdnc.sli.provider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicExprListener;
import org.openecomp.sdnc.sli.SvcLogicExpression;
import org.openecomp.sdnc.sli.SvcLogicExpressionFactory;
import org.openecomp.sdnc.sli.SvcLogicGraph;
import org.openecomp.sdnc.sli.SvcLogicNode;
import org.openecomp.sdnc.sli.provider.SvcLogicExpressionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SvcLogicExpressionResolverTest extends TestCase {
	

	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicExpressionResolver.class);
	
	public void testEvaluate()
	{
		InputStream testStr = getClass().getResourceAsStream("/expression.tests");
		BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
		
		try
		{
			SvcLogicContext ctx = new SvcLogicContext();
			SvcLogicGraph graph = new SvcLogicGraph();
			SvcLogicNode node = new SvcLogicNode(1, "return", graph);
			graph.setRootNode(node);

			String line = null;
			int lineNo = 0;
			while ((line = testsReader.readLine()) != null) {
				++lineNo;
				if (line.startsWith("#"))
				{
					String testExpr = line.trim().substring(1).trim();
					String[] nameValue = testExpr.split("=");
					String name = nameValue[0].trim();
					String value = nameValue[1].trim();

					if (name.startsWith("$"))
					{
						LOG.info("Setting context attribute "+name+" = "+value);
						ctx.setAttribute(name.substring(1), value);
					}
					else
					{

						LOG.info("Setting node attribute "+name+" = "+value);
						node.setAttribute(name, value);
						
					}
				}
				else
				{
					// if the line contains #, what comes before is the expression to evaluate, and what comes after
					// is the expected value
					String[] substrings = line.split("#");
					String expectedValue = substrings.length > 1 ? substrings[1].trim() : null;
					String testExpr = substrings[0].trim();

					LOG.info("Parsing expression "+testExpr);
					SvcLogicExpression expr = SvcLogicExpressionFactory.parse(testExpr);
					if (expr == null)
					{
						fail("Unable to parse expression "+testExpr);
					}
					else
					{
						LOG.info("Evaluating parsed expression "+expr.asParsedExpr());
						String exprValue = SvcLogicExpressionResolver.evaluate(expr,  node, ctx);
						if (exprValue == null)
						{
							fail("Unable to evaluate expression "+testExpr);
						}
						else
						{
							LOG.info("Expression " + testExpr + " evaluates to " + exprValue);
							if (expectedValue != null) {
								Assert.assertEquals("Line " + lineNo + ": " + testExpr, expectedValue, exprValue);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Caught exception", e);
			fail("Caught exception");
		}
	}

}
