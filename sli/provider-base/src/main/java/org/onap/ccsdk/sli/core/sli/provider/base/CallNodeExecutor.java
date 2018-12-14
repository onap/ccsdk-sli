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

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallNodeExecutor extends AbstractSvcLogicNodeExecutor {

	private static final Logger LOG = LoggerFactory
			.getLogger(CallNodeExecutor.class);
	private static final String CURRENT_GRAPH="currentGraph";

	@Override
	public SvcLogicNode execute(SvcLogicServiceBase svc, SvcLogicNode node, SvcLogicContext ctx)
			throws SvcLogicException {

		String outValue = "not-found";

		SvcLogicGraph myGraph = node.getGraph();

		if (myGraph == null)
		{
			LOG.debug("execute: getGraph returned null");
		}
		else
		{
			LOG.debug("execute: got SvcLogicGraph");
		}

		SvcLogicExpression moduleExpr = null;

		String module = null;

		moduleExpr = node.getAttribute("module");
		if (moduleExpr != null)
		{
			module  = SvcLogicExpressionResolver.evaluate(moduleExpr, node, ctx);
		}

		if ((module == null) || (module.length() == 0))
		{
			if (myGraph != null)
			{
				module = myGraph.getModule();
				LOG.debug("myGraph.getModule() returned "+module);
			}
		}

		SvcLogicExpression rpcExpr = null;
		String rpc = null;
		rpcExpr = node.getAttribute("rpc");
		if (rpcExpr != null)
		{
			rpc  = SvcLogicExpressionResolver.evaluate(rpcExpr, node, ctx);
		}

		if ((rpc == null) || (rpc.length() == 0))
		{
			if (myGraph != null)
			{
				rpc = myGraph.getRpc();
				LOG.debug("myGraph.getRpc() returned "+rpc);
			}
		}

		String mode = null;

		moduleExpr = node.getAttribute("mode");
		if (moduleExpr != null)
		{
			mode  = SvcLogicExpressionResolver.evaluate(moduleExpr, node, ctx);
		}

		if ((mode == null) || (mode.length() == 0))
		{
			if (myGraph != null)
			{
				mode = myGraph.getMode();

				LOG.debug("myGraph.getMode() returned "+mode);
			}
		}

		String version = null;

		moduleExpr = node.getAttribute("version");
		if (moduleExpr != null)
		{
			version  = SvcLogicExpressionResolver.evaluate(moduleExpr, node, ctx);
		}

		String parentGraph = ctx.getAttribute(CURRENT_GRAPH);
        ctx.setAttribute("parentGraph", parentGraph);

		SvcLogicStore store = svc.getStore();
		String errorMessage = "Parent " + parentGraph + " failed to call child [" + module + "," + rpc + "," + version + "," + mode + "] because the graph could not be found";
		boolean graphWasCalled = false;
		if (store != null) {
            SvcLogicGraph calledGraph = store.fetch(module, rpc, version, mode);
            if (calledGraph != null) {
                LOG.debug("Parent " + parentGraph + " is calling child " + calledGraph.toString());
                ctx.setAttribute(CURRENT_GRAPH, calledGraph.toString());
                svc.execute(calledGraph, ctx);
                outValue = ctx.getStatus();
                graphWasCalled = true;
            } else {
                LOG.debug(errorMessage);
            }
        } else {
            LOG.debug("Could not get SvcLogicStore reference");
        }

		SvcLogicNode nextNode = node.getOutcomeValue(outValue);
		if (nextNode != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("about to execute " + outValue + " branch");
			}
            ctx.setAttribute(CURRENT_GRAPH, parentGraph);
			return (nextNode);
		}

		nextNode = node.getOutcomeValue("Other");
		if (nextNode != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("about to execute Other branch");
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("no " + outValue + " or Other branch found");
			}
		}
		
		if (graphWasCalled == false) {
			if (node.getOutcomeValue("catch") != null) {
				nextNode = node.getOutcomeValue("catch");
				LOG.debug("graph could not be called, but catch node was found and will be executed");
			} else {
				LOG.debug("graph could not be called and no catch node was found, throwing exception");
				throw new SvcLogicException(errorMessage);
			}
		}

		ctx.setAttribute(CURRENT_GRAPH, parentGraph);
        ctx.setAttribute("parentGraph", null);

		return (nextNode);

	}

}
