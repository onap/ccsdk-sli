/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                      reserved.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpressionFactory;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetNodeExecutor extends AbstractSvcLogicNodeExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(SetNodeExecutor.class);
    protected final String arrayPattern = "\\[\\d*\\]";

    @Override
    public SvcLogicNode execute(SvcLogicServiceBase svc, SvcLogicNode node, SvcLogicContext ctx)
            throws SvcLogicException {
        execute(node,ctx);
        return null;
    }

    public void execute(SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {
        String ifunsetStr = SvcLogicExpressionResolver.evaluate(node.getAttribute("only-if-unset"), node, ctx);

        boolean ifunset = "true".equalsIgnoreCase(ifunsetStr);

        Set<Map.Entry<String, SvcLogicExpression>> parameterSet = node.getParameterSet();

        for (Iterator<Map.Entry<String, SvcLogicExpression>> iter = parameterSet.iterator(); iter.hasNext();) {
            Map.Entry<String, SvcLogicExpression> curEnt = iter.next();
            String curName = curEnt.getKey();
            String lhsVarName = curName;

            // Resolve LHS of assignment (could contain index variables)
            try {
                // Backticks symbolize the variable should be handled as an expression instead of as a variable
                if (curName.trim().startsWith("`")) {
                    int lastParen = curName.lastIndexOf("`");
                    String evalExpr = curName.trim().substring(1, lastParen);
                    SvcLogicExpression lhsExpr = SvcLogicExpressionFactory.parse(evalExpr);
                    lhsVarName = SvcLogicExpressionResolver.evaluate(lhsExpr, node, ctx);
                } else {
                    SvcLogicExpression lhsExpr = SvcLogicExpressionFactory.parse(curName);
                    lhsVarName = SvcLogicExpressionResolver.resolveVariableName(lhsExpr, node, ctx);
                }
            } catch (Exception e) {
                LOG.warn("Caught exception trying to resolve variable name (" + curName + ")", e);
            }

            boolean setValue = true;

            if (curName.endsWith(".")) {
                // Copy subtree - value should be a variable name
                SvcLogicExpression curValue = curEnt.getValue();

                if (curValue != null) {
                    String rhsRoot = curValue.toString();

                    if ((rhsRoot != null) && (rhsRoot.length() > 0)) {
                        if (rhsRoot.endsWith(".")) {
                            rhsRoot = rhsRoot.substring(0, rhsRoot.length() - 1);
                        }

                        // SDNGC-2321 : rhsRoot is variable name, possibly with subscript(s) to be resolved
                        try {
                            SvcLogicExpression rhsExpr = SvcLogicExpressionFactory.parse(rhsRoot);
                            rhsRoot = SvcLogicExpressionResolver.resolveVariableName(rhsExpr, node, ctx);
                        } catch (Exception e) {
                            LOG.warn("Caught exception trying to resolve variable name (" + rhsRoot + ")", e);
                        }

                        // See if the parameters are reversed (copying service-data to input) .. this
                        // was done as a workaround to earlier issue
                        if (curName.endsWith("-input.") && rhsRoot.startsWith("service-data")) {
                            LOG.warn("Arguments appear to be reversed .. will copy input to service-data instead");
                            lhsVarName = rhsRoot + ".";
                            rhsRoot = curName.substring(0, curName.length() - 1);
                        }

                        rhsRoot = rhsRoot + ".";
                        String lhsPrefix = lhsVarName;

                        if (lhsPrefix.endsWith(".")) {
                            lhsPrefix = lhsPrefix.substring(0, lhsPrefix.length() - 1);
                        }

                        HashMap<String, String> parmsToAdd = new HashMap<>();

                        for (String sourceVarName : ctx.getAttributeKeySet()) {
                            if (sourceVarName.startsWith(rhsRoot)) {
                                String targetVar = lhsPrefix + "." + sourceVarName.substring(rhsRoot.length());
                                LOG.debug("Copying {} value to {}", sourceVarName, targetVar);
                                parmsToAdd.put(targetVar, ctx.getAttribute(sourceVarName));
                            }
                        }
                        for (String newParmName : parmsToAdd.keySet()) {
                            ctx.setAttribute(newParmName, parmsToAdd.get(newParmName));
                        }
                    } else {
                        // If RHS is empty, unset attributes in LHS
                        LinkedList<String> parmsToRemove = new LinkedList<>();
                        String prefix = lhsVarName + ".";
                        String arrayPrefix = lhsVarName + "[";
                        //Clear length value in case an array exists with this prefix
                        String lengthParamName = lhsVarName + "_length";
                        LOG.debug("Unsetting {} because prefix {} is being cleared.", lengthParamName, prefix);

                        for (String curCtxVarname : ctx.getAttributeKeySet()) {
                            String curCtxVarnameMatchingValue = curCtxVarname;
                            //Special handling for reseting array values, strips out brackets and any numbers between the brackets
                            //when testing if a context memory value starts with a prefix
                            if(!prefix.contains("[") && curCtxVarnameMatchingValue.contains("[")) {
                                curCtxVarnameMatchingValue = curCtxVarname.replaceAll(arrayPattern, "") + ".";
                            }
                            if (curCtxVarnameMatchingValue.startsWith(prefix)) {
                                LOG.debug("Unsetting {} because matching value {} starts with the prefix {}", curCtxVarname, curCtxVarnameMatchingValue, prefix);
                                parmsToRemove.add(curCtxVarname);
                            }else if (curCtxVarnameMatchingValue.startsWith(lengthParamName)) {
                                LOG.debug("Unsetting {} because matching value {} starts with the lengthParamName {}", curCtxVarname, curCtxVarnameMatchingValue, lengthParamName);
                                parmsToRemove.add(curCtxVarname);
                            }else if (curCtxVarnameMatchingValue.startsWith(arrayPrefix)) {
                                LOG.debug("Unsetting {} because matching value {} starts with the arrayPrefix {}", curCtxVarname, curCtxVarnameMatchingValue, arrayPrefix);
                                parmsToRemove.add(curCtxVarname);
                            }
                        }
                        for (String parmName : parmsToRemove) {
                            ctx.setAttribute(parmName, null);
                        }
                    }
                }
            } else {
                if (ifunset) {
                    String ctxValue = ctx.getAttribute(lhsVarName);
                    if ((ctxValue != null) && (ctxValue.length() > 0)) {
                        setValue = false;
                        LOG.debug("Attribute {} already set and only-if-unset is true, so not overriding", lhsVarName);
                    }
                }
                if (setValue) {
                    String curValue = SvcLogicExpressionResolver.evaluate(curEnt.getValue(), node, ctx);

                    if (LOG.isDebugEnabled()) {
                        LOG.trace("Parameter value {} resolves to {}", curEnt.getValue().asParsedExpr(), curValue);
                        LOG.debug("Setting context attribute {} to {}", lhsVarName, curValue);
                    }
                    ctx.setAttribute(lhsVarName, curValue);
                }
            }
        }
    }
}

