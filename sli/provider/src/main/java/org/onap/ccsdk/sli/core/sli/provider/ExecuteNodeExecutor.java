/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 ONAP
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

package org.onap.ccsdk.sli.core.sli.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteNodeExecutor extends SvcLogicNodeExecutor {
	private static final Logger LOG = LoggerFactory
			.getLogger(ExecuteNodeExecutor.class);

	private static final String pluginErrorMessage = "Could not execute plugin. SvcLogic status will be set to failure.";
	public SvcLogicNode execute(SvcLogicServiceImpl svc, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {

		String pluginName = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("plugin"), node, ctx);
		String outValue = "failure";

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
				pluginMethod = pluginClass.getMethod(methodName, Map.class, SvcLogicContext.class);
			} catch (NoSuchMethodException e) {
				LOG.error(pluginErrorMessage, e);
			}

			if (pluginMethod == null) {
				outValue = "unsupported-method";
			} else {
				try {

					Map<String, String> parmMap = new HashMap<String, String>();

					Set<Map.Entry<String, SvcLogicExpression>> parmSet = node
							.getParameterSet();

					for (Iterator<Map.Entry<String, SvcLogicExpression>> iter = parmSet
							.iterator(); iter.hasNext();) {
						Map.Entry<String, SvcLogicExpression> curEnt = iter.next();
						String curName = curEnt.getKey();
						SvcLogicExpression curExpr = curEnt.getValue();
						String curExprValue = SvcLogicExpressionResolver.evaluate(curExpr, node, ctx);

						LOG.debug("Parameter "+curName+" = "+curExpr.asParsedExpr()+" resolves to "+curExprValue);

						parmMap.put(curName,curExprValue);
					}

					Object o = pluginMethod.invoke(plugin, parmMap, ctx);
			        String emitsOutcome = SvcLogicExpressionResolver.evaluate(node.getAttribute("emitsOutcome"),  node, ctx);

					outValue = mapOutcome(o, emitsOutcome);

				} catch (InvocationTargetException e) {
				    if(e.getCause() != null){
	                    LOG.error(pluginErrorMessage, e.getCause());
				    }else{
					LOG.error(pluginErrorMessage, e);
				    }
					outValue = "failure";
					ctx.setStatus("failure");
				} catch (IllegalAccessException e) {
                    LOG.error(pluginErrorMessage, e);
                    outValue = "failure";
                    ctx.setStatus("failure");
                } catch (IllegalArgumentException e) {
                    LOG.error(pluginErrorMessage, e);
                    outValue = "failure";
                    ctx.setStatus("failure");
                }
			}

		}

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

	protected String evaluate(SvcLogicExpression expr, SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {
        return SvcLogicExpressionResolver.evaluate(node.getAttribute("method"), node, ctx);
    }

    public String mapOutcome(Object o, String emitsOutcome) {
        if (emitsOutcome != null) {
            Boolean nodeEmitsOutcome = Boolean.valueOf(emitsOutcome);
            if (nodeEmitsOutcome) {
                return (String) o;
            } else {
                return "success";
            }

        } else {
            return "success";
        }
    }

}
