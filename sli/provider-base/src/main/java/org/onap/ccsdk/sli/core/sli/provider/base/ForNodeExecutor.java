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

public class ForNodeExecutor extends AbstractSvcLogicNodeExecutor {

	private static final Logger LOG = LoggerFactory
			.getLogger(ForNodeExecutor.class);

	@Override
	public SvcLogicNode execute(SvcLogicServiceBase svc, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {

		SvcLogicExpression atomicExpr = node.getAttribute("atomic");
		String atomicStr = SvcLogicExpressionResolver.evaluate(atomicExpr, node, ctx);
		boolean isAtomic = !("false".equalsIgnoreCase(atomicStr));

		int numOutcomes = node.getNumOutcomes();
		String idxVar = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("index"), node, ctx);
		String startVal = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("start"), node, ctx);
		String endVal = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("end"), node, ctx);

		LOG.debug("Executing "+ (isAtomic ? "atomic" : "non-atomic") + " for loop - for (int " + idxVar + " = " + startVal
				+ "; " + idxVar + " < " + endVal + "; " + idxVar + "++)");

		int startIdx = 0;
		int endIdx = 0;

		try {
			startIdx = Integer.parseInt(startVal);
			endIdx = Integer.parseInt(endVal);
		} catch (NumberFormatException e) {
			SvcLogicExpression silentFailureExpr = node.getAttribute("silentFailure");
			String silentFailure = SvcLogicExpressionResolver.evaluate(silentFailureExpr, node, ctx);
			boolean isSilentFailure = Boolean.parseBoolean(silentFailure);
			String message = "Invalid index values [" + startVal + "," + endVal + "]";
			if(!isSilentFailure){
			throw new SvcLogicException(message);
			}else{
			    LOG.debug(message + ". Not exiting because silentFailure was set to true.");
			    return(null);
			}
		}

        try {
		for (int ctr = startIdx; ctr < endIdx; ctr++) {

			ctx.setAttribute(idxVar, "" + ctr);

			for (int i = 0; i < numOutcomes; i++) {

				if ("failure".equals(ctx.getStatus()) && isAtomic) {
					LOG.info("For - stopped executing nodes due to failure status");
					return(null);
				}

				SvcLogicNode nextNode = node.getOutcomeValue("" + (i + 1));
				if (nextNode != null) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("For  - executing outcome " + (i + 1));
					}
					SvcLogicNode innerNextNode = nextNode;
					while (innerNextNode != null) {
						innerNextNode = svc.executeNode(innerNextNode, ctx);
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("For - done: no outcome " + (i + 1));
					}
				}
			}
		}
        } catch (BreakNodeException br) {
            LOG.error("ForNodeExecutor caught break",br);
        }
		return (null);
	}

}
