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

package org.onap.ccsdk.sli.core.sli.provider.base;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicExprListener;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpressionFactory;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicExpressionResolver;
import org.onap.ccsdk.sli.core.sli.SvcLogicParser;
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

    public void testSvcLogicExpressions() throws Exception {
	SwitchNodeExecutor switchNodeExecutor = new SwitchNodeExecutor();
	SetNodeExecutor setNodeExecutor = new SetNodeExecutor();
	SvcLogicContext ctx = new SvcLogicContext();
	SvcLogicParser slp = new SvcLogicParser();
	LinkedList<SvcLogicGraph> graph = slp.parse("src/test/resources/expressions.xml");
	SvcLogicNode root = graph.getFirst().getRootNode();
//Test a set node that makes use of arithmetic operations
/*	
<set>
	<parameter name='add' value='`1 + 1`' />
	<parameter name='sub' value='`2 - 1`' />
	<parameter name='div' value='`6 / 2`' />
	<parameter name='multi' value='`2 * 2`' />
	<parameter name='addDoubleQuotes' value="`1 + 1`" />
	<parameter name='subDoubleQuotes' value="`2 - 1`" />
	<parameter name='divDoubleQuotes' value="`6 / 2`" />
	<parameter name='multiDoubleQuotes' value="`2 * 2`" />
</set>
*/	
//the node matching outcome value 1 comes from parsing the block of xml above	
	ctx.setAttribute("a", "5");
	ctx.setAttribute("b", "3");
	setNodeExecutor.execute(root.getOutcomeValue("1"), ctx);
        assertEquals("2", ctx.getAttribute("add"));
        assertEquals("1", ctx.getAttribute("sub"));
        assertEquals("3", ctx.getAttribute("div"));
        assertEquals("4", ctx.getAttribute("multi"));
        assertEquals("2", ctx.getAttribute("addDoubleQuotes"));
        assertEquals("1", ctx.getAttribute("subDoubleQuotes"));
        assertEquals("3", ctx.getAttribute("divDoubleQuotes"));
        assertEquals("4", ctx.getAttribute("multiDoubleQuotes"));

//Test a set node that makes use of string concatenation        
/*
<set>
	<parameter name='varA' value='`$a + $b`' />
	<parameter name='varB' value='`$a + &apos;literal&apos; `' />
	<parameter name='varC' value='`&apos;literal&apos; + $b `' />
	<parameter name='varD' value='`&apos;too&apos; + &apos;literal&apos;`' />
	<parameter name='varADoubleQuotes' value="`$a + $b`" />
	<parameter name='varBDoubleQuotes' value="`$a +'literal' `" />
	<parameter name='varCDoubleQuotes' value="`'literal' + $b `" />
	<parameter name='varDDoubleQuotes' value="`'too' + 'literal'`" />
</set>        
*/
//the node matching outcome value 2 comes from parsing the block of xml above
	ctx.setAttribute("a", "cat");
	ctx.setAttribute("b", "dog");
        setNodeExecutor.execute(root.getOutcomeValue("2"), ctx);
        assertEquals("catdog", ctx.getAttribute("varA"));
        assertEquals("catliteral", ctx.getAttribute("varB"));
        assertEquals("literaldog", ctx.getAttribute("varC"));
        assertEquals("tooliteral", ctx.getAttribute("varD"));
        assertEquals("catdog", ctx.getAttribute("varADoubleQuotes"));
        assertEquals("catliteral", ctx.getAttribute("varBDoubleQuotes"));
        assertEquals("literaldog", ctx.getAttribute("varCDoubleQuotes"));
        assertEquals("tooliteral", ctx.getAttribute("varDDoubleQuotes"));

//Shows how backticks interact with + operator
/*
<set>
	<parameter name='testOne' value='`1 + 1`' />
	<parameter name='testThree' value='"1" +"1"' />
	<parameter name='testFour' value='`$portNumber + $slot + $shelf`' />
	<parameter name='testOneDoubleQuotes' value="`1 + 1`" />
	<parameter name='testThreeDoubleQuotes' value="'1' +'1'" />
	<parameter name='testFourDoubleQuotes' value="`$portNumber + $slot + $shelf`" />
</set>
*/
//the node matching outcome value 3 comes from parsing the block of xml above
	ctx.setAttribute("portNumber", "2");
	ctx.setAttribute("slot", "3");
	ctx.setAttribute("shelf", "1");

        setNodeExecutor.execute(root.getOutcomeValue("3"), ctx);
        assertEquals("2", ctx.getAttribute("testOne"));
        assertEquals("\"1\" +\"1\"", ctx.getAttribute("testThree"));
        assertEquals("6", ctx.getAttribute("testFour"));
        assertEquals("2", ctx.getAttribute("testOneDoubleQuotes"));
        assertEquals("'1' +'1'", ctx.getAttribute("testThreeDoubleQuotes")); 
        assertEquals("6", ctx.getAttribute("testFourDoubleQuotes"));

	ctx.setAttribute("a", "5");
	ctx.setAttribute("b", "3");

	// series of switch statements showing and or != > < >= == <=
	// the XML for the node is commented above the line that evaluates that node, the switch statements are single line	

	//<switch test="`'PIZZA' == 'NOTPIZZA' or $a != $b`" />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("4"), ctx));

	//<switch test="`'PIZZA' == 'PIZZA' and $a != $b`" />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("5"), ctx));

	//<switch test="`'PIZZA' == 'NOTPIZZA' or $a &gt;= $b`" />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("6"), ctx));

	//<switch test="`'PIZZA' == 'PIZZA' and $b &lt; $a`" />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("7"), ctx));

	//<switch test="`'PIZZA' == 'PIZZA'`" />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("8"), ctx));

	//<switch test="`$a == $b`" />
	assertEquals("false",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("9"), ctx));

	//<switch test="`'PIZZA' == 'NOTPIZZA'`" />
	assertEquals("false",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("10"), ctx));

	//<switch test="`'PIZZA' != 'PIZZA'`" />
	assertEquals("false",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("11"), ctx));

	//<switch test="`'PIZZA' != 'NOTPIZZA'`" />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("12"), ctx));

	//<switch test='`$a != $b`' />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("13"), ctx));

	//<switch test='`1 &lt; 2`' />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("14"), ctx));

	//<switch test='`2 &lt;= 2`' />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("15"), ctx));

	//<switch test='`3 &gt; 2`' />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("16"), ctx));

	//<switch test='`2 &gt;= 2`' />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("17"), ctx));

	// Series of switch statements that show the effect of using backticks
	
	ctx.setAttribute("literalStartingWithDollarSign", "DONT READ ME!");
	//<switch test='$literalStartingWithDollarSign'/>
	assertEquals("$literalStartingWithDollarSign",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("18"), ctx));

	ctx.setAttribute("dollarSignFollowedByVariableSurroundedinBackticks", "README");
	//<switch test='$literalStartingWithDollarSign'/>
	assertEquals("README",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("19"), ctx));

	ctx.setAttribute("a", "2");
	ctx.setAttribute("b", "2");
	//<switch test='`$a == $b`' />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("20"), ctx));

	//<switch test="`$a == $b`" />
	assertEquals("true",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("21"), ctx));
	
	//<switch test='$a == $b' />
	assertEquals("$a == $b",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("22"), ctx));

	//<switch test="$a == $b" />
	assertEquals("$a == $b",switchNodeExecutor.evaluateNodeTest(root.getOutcomeValue("23"), ctx));
    }
}
