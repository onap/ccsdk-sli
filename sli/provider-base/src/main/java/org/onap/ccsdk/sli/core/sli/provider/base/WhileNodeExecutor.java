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

import org.onap.ccsdk.sli.core.sli.BreakNodeException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhileNodeExecutor extends AbstractSvcLogicNodeExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(WhileNodeExecutor.class);

    @Override
    public SvcLogicNode execute(SvcLogicServiceBase svc, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {

        String testResult = evaluateNodeTest(node, ctx);
        SvcLogicExpression silentFailureExpr = node.getAttribute("do");
        String doWhile = SvcLogicExpressionResolver.evaluate(silentFailureExpr, node, ctx);
        if ("true".equals(doWhile)) {
            LOG.debug("While loop will execute once regardless of expression because do is set to true");
        }

        try {
            while ("true".equals(testResult) || "true".equals(doWhile)) {
                if (!"true".equals(doWhile)) {
                    LOG.debug("Test expression (" + node.getAttribute("test") + ") evaluates to true, executing loop.");
                }
                int numOutcomes = node.getNumOutcomes() + 1;
                for (int i = 0; i < numOutcomes; i++) {
                    SvcLogicNode nextNode = node.getOutcomeValue("" + (i + 1));
                    if (nextNode != null) {
                        while (nextNode != null) {
                            nextNode = svc.executeNode(nextNode, ctx);
                        }
                    } else {
                        if ("true".equals(doWhile)) {
                            LOG.debug("Do executed, will only execute again if test expression is true.");
                            doWhile = "false";
                        }
                        testResult = evaluateNodeTest(node, ctx);
                        LOG.debug("test expression (" + node.getAttribute("test") + ") evaluates to " + testResult);
                    }
                }
            }
            LOG.debug("testResult was " + testResult + " which is not equal to true, exiting while loop.");
        } catch (BreakNodeException e) {
            LOG.debug("WhileNodeExecutor caught break");
        }
        return (null);
    }

}
