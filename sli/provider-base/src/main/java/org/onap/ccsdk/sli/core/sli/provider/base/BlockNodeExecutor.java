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

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockNodeExecutor extends AbstractSvcLogicNodeExecutor {

	private static final Logger LOG = LoggerFactory
			.getLogger(BlockNodeExecutor.class);
	
	@Override
	public SvcLogicNode execute(SvcLogicServiceBase svc, SvcLogicNode node, SvcLogicContext ctx)
			throws SvcLogicException {

		SvcLogicExpression atomicExpr = node.getAttribute("atomic");
		String atomicStr = SvcLogicExpressionResolver.evaluate(atomicExpr, node, ctx);
		boolean isAtomic = "true".equalsIgnoreCase(atomicStr);
		
		// Initialize status to success so that at least one outcome will execute
		ctx.setStatus("success");
		
		int numOutcomes = node.getNumOutcomes();

		for (int i = 0; i < numOutcomes; i++) {
			if ("failure".equals(ctx.getStatus()) && isAtomic) {
				LOG.info("Block - stopped executing nodes due to failure status");
				return(null);
			}
			
			SvcLogicNode nextNode = node.getOutcomeValue("" + (i + 1));
			if (nextNode != null) {

				while (nextNode != null)
				{
				       nextNode = svc.executeNode(nextNode, ctx);
				}
			}
		}

		return (null);
	}



}
