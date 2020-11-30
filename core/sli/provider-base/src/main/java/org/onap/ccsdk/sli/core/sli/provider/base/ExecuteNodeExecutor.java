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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteNodeExecutor extends AbstractSvcLogicNodeExecutor {
	private static final Logger LOG = LoggerFactory
			.getLogger(ExecuteNodeExecutor.class);
	private static final String FAILURE=SvcLogicConstants.FAILURE;

	private static final String pluginErrorMessage = "Could not execute plugin. SvcLogic status will be set to failure.";
	public SvcLogicNode execute(SvcLogicServiceBase svc, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {

		String pluginName = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("plugin"), node, ctx);
		String outValue = FAILURE;

		if (LOG.isDebugEnabled()) {
			LOG.debug("execute node encountered - looking for plugin "
					+ pluginName);
		}

        SvcLogicJavaPlugin plugin  = getSvcLogicJavaPlugin(pluginName);

		if (plugin == null) {
			outValue = "not-found";
		} else {

			String methodName = evaluate(node.getAttribute("method"),  node, ctx);

			Class pluginClass = plugin.getClass();

			Method pluginMethod = null;

			try {
				LOG.debug("executing method {} on plugin {}", methodName, pluginName);
				pluginMethod = pluginClass.getMethod(methodName, Map.class, SvcLogicContext.class);
			} catch (NoSuchMethodException e) {
				LOG.error(pluginErrorMessage, e);
			}

			if (pluginMethod == null) {
				outValue = "unsupported-method";
			} else {
				try {


				    Map<String, String> parmMap = getResolvedParameters(node,ctx);
					Object o = pluginMethod.invoke(plugin, parmMap, ctx);
			        String emitsOutcome = SvcLogicExpressionResolver.evaluate(node.getAttribute("emitsOutcome"),  node, ctx);

					outValue = mapOutcome(o, emitsOutcome);

				} catch (InvocationTargetException e) {
				    if(e.getCause() != null){
	                    LOG.error(pluginErrorMessage, e.getCause());
				    }else{
					LOG.error(pluginErrorMessage, e);
				    }
					outValue = FAILURE;
                    ctx.markFailed();
				} catch (IllegalAccessException e) {
                    LOG.error(pluginErrorMessage, e);
                    outValue = FAILURE;
                    ctx.markFailed();
                } catch (IllegalArgumentException e) {
                    LOG.error(pluginErrorMessage, e);
                    outValue = FAILURE;
                    ctx.markFailed();
                }
			}

		}
        return (getNextNode(node, outValue));
	}

	protected String evaluate(SvcLogicExpression expr, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {
        return SvcLogicExpressionResolver.evaluate(node.getAttribute("method"), node, ctx);
    }

    public String mapOutcome(Object o, String emitsOutcome) {
        if (emitsOutcome != null) {
            Boolean nodeEmitsOutcome = Boolean.valueOf(emitsOutcome);
            if (nodeEmitsOutcome) {
                return (String) o;
            } else {
                return SvcLogicConstants.SUCCESS;
            }
        } else {
            return SvcLogicConstants.SUCCESS;
        }
    }

}
