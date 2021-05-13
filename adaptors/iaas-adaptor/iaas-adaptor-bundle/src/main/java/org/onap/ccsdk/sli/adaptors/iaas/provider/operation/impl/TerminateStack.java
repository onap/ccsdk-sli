/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 *
 * Modifications Copyright (C) 2019 IBM.
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl;

import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.StackService;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Stack;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.Map;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.utils.logging.Msg;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base.ProviderStackOperation;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TerminateStack extends ProviderStackOperation {

    private static final String TERMINATE_STATUS = "TERMINATE_STATUS";
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(EvacuateServer.class);

    private void deleteStack(RequestContext rc, Stack stack) throws ZoneException, RequestFailedException {
        SvcLogicContext ctx = rc.getSvcLogicContext();
        Context context = stack.getContext();
        StackService stackService = context.getStackService();
        logger.debug("Deleting Stack: " + "id:{ " + stack.getId() + "}");
        stackService.deleteStack(stack);

        // wait for the stack deletion
        boolean success = waitForStackStatus(rc, stack, Stack.Status.DELETED);
        if (success) {
            ctx.setAttribute(TERMINATE_STATUS, "SUCCESS");
        } else {
            ctx.setAttribute(TERMINATE_STATUS, "ERROR");
            throw new RequestFailedException("Delete Stack failure : " + Msg.STACK_OPERATION_EXCEPTION.toString());
        }
    }

    @SuppressWarnings("nls")
    public Stack terminateStack(Map<String, String> params, SvcLogicContext ctx) {
        Stack stack = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        ctx.setAttribute(TERMINATE_STATUS, "STACK_NOT_FOUND");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        try {

            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME, ProviderAdapter.PROPERTY_STACK_ID);

            String stackId = params.get(ProviderAdapter.PROPERTY_STACK_ID);
            String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            Context context = resolveContext(rc, params, appName,vmUrl );

            try {
                if (context != null) {
                    rc.reset();
                    stack = lookupStack(rc, context, stackId);
                    logger.debug(Msg.STACK_FOUND, vmUrl, context.getTenantName(), stack.getStatus().toString());
                    logger.info(EELFResourceManager.format(Msg.TERMINATING_STACK, stack.getName()));
                    deleteStack(rc, stack);
                    logger.info(EELFResourceManager.format(Msg.TERMINATE_STACK, stack.getName()));
                    context.close();
                    doSuccess(rc);
                    String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "TerminateStack", vmUrl);
                    ctx.setAttribute(Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.STACK_NOT_FOUND, e, vmUrl);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Exception e1) {
                String msg =
                        EELFResourceManager.format(Msg.STACK_OPERATION_EXCEPTION, e1, e1.getClass().getSimpleName(),
                                Operation.TERMINATE_STACK.toString(), vmUrl, context.getTenantName());
                logger.error(msg, e1);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.TERMINATE_STACK_FAILED, appName, "n/a", "n/a"));
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return stack;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.TERMINATE_STACK.toString(), "App-C IaaS Adapter:Terminate-Stack", Constants.ADAPTER_NAME);
        logOperation(Msg.TERMINATING_STACK, params, context);
        return terminateStack(params, context);
    }
}
