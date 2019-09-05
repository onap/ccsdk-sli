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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveNodeExecutor extends AbstractSvcLogicNodeExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(SaveNodeExecutor.class);
    private static final String FAILURE= "failure";

    @Override
    public SvcLogicNode execute(SvcLogicServiceBase svc, SvcLogicNode node, SvcLogicContext ctx)
            throws SvcLogicException {

        String plugin = SvcLogicExpressionResolver.evaluate(node.getAttribute("plugin"), node, ctx);
        String resourceType = SvcLogicExpressionResolver.evaluate(node.getAttribute("resource"), node, ctx);
        String key = SvcLogicExpressionResolver.evaluateAsKey(node.getAttribute("key"), node, ctx);
        String forceStr = SvcLogicExpressionResolver.evaluate(node.getAttribute("force"), node, ctx);
        String localOnlyStr = SvcLogicExpressionResolver.evaluate(node.getAttribute("local-only"), node, ctx);
        String pfx = SvcLogicExpressionResolver.evaluate(node.getAttribute("pfx"), node, ctx);

        boolean force = "true".equalsIgnoreCase(forceStr);
        boolean localOnly = "true".equalsIgnoreCase(localOnlyStr);

        Map<String, String> parmMap = getResolvedParameters(node,ctx);

        String outValue = FAILURE;

        if (LOG.isDebugEnabled()) {
            LOG.debug("save node encountered - looking for resource class " + plugin);
        }



        SvcLogicResource resourcePlugin = getSvcLogicResource(plugin);

        if (resourcePlugin != null) {

            try {
                switch (resourcePlugin.save(resourceType, force, localOnly, key, parmMap, pfx, ctx)) {
                    case SUCCESS:
                        outValue = "success";
                        break;
                    case NOT_FOUND:
                        outValue = "not-found";
                        break;
                    case FAILURE:
                    default:
                        outValue = FAILURE;
                }
            } catch (SvcLogicException e) {
                LOG.error("Caught exception from resource plugin", e);
                outValue = FAILURE;
            }
        } else {
            LOG.warn("Could not find SvcLogicResource object for plugin " + plugin);
        }
        return (getNextNode(node, outValue));
    }

}
