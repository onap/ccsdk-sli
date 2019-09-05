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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSvcLogicNodeExecutor {
	protected SvcLogicResolver resolver;
	public abstract SvcLogicNode execute(SvcLogicServiceBase svc, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException;

	private static final Logger LOG = LoggerFactory.getLogger(AbstractSvcLogicNodeExecutor.class);
	protected static final String PARAMETER_DEBUG_PATTERN = "Parameter: {} resolves to: {} which came from the expression: {}";
    protected static final String SETTING_DEBUG_PATTERN = "Setting context attribute: {} to: {} which came from the expression: {}";

    protected String evaluateNodeTest(SvcLogicNode node, SvcLogicContext ctx)
			throws SvcLogicException {
		if (node == null) {
			return null;
		}

		return (SvcLogicExpressionResolver.evaluate(node.getAttribute("test"),
				node, ctx));

	}

    public void setResolver(SvcLogicResolver resolver) {
		this.resolver = resolver;
	}

	protected SvcLogicAdaptor getAdaptor(String adaptorName) {
    	return resolver.getSvcLogicAdaptor(adaptorName);
    }

    protected SvcLogicResource getSvcLogicResource(String resourceName) {
        return resolver.getSvcLogicResource(resourceName);
    }

    protected SvcLogicRecorder getSvcLogicRecorder(String recorderName) {
        return resolver.getSvcLogicRecorder(recorderName);
    }

    protected SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName){
        return resolver.getSvcLogicJavaPlugin(pluginName);
    }

    protected SvcLogicNode getNextNode(SvcLogicNode node, String outValue) {
        SvcLogicNode nextNode = node.getOutcomeValue(outValue);
        if (nextNode != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("about to execute " + outValue + " branch");
            }
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
        return (nextNode);
    }
    
    protected Map<String, String> getResolvedParameters(SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException{
        Map<String, String> parmMap = new HashMap<>();

        Set<Map.Entry<String, SvcLogicExpression>> parmSet = node
                .getParameterSet();

        for (Iterator<Map.Entry<String, SvcLogicExpression>> iter = parmSet
                .iterator(); iter.hasNext();) {
            Map.Entry<String, SvcLogicExpression> curEnt = iter.next();
            String curName = curEnt.getKey();
            SvcLogicExpression curExpr = curEnt.getValue();
            String curExprValue = SvcLogicExpressionResolver.evaluate(curExpr, node, ctx);
            LOG.trace(PARAMETER_DEBUG_PATTERN, curName, curExprValue, curExpr.toString());
            parmMap.put(curName,curExprValue);
        }
        
        return parmMap;
    }
    
}
