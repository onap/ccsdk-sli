/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
import com.att.cdp.openstack.OpenStackContext;
import com.att.cdp.openstack.connectors.HeatConnector;
import com.att.cdp.openstack.util.ExceptionMapper;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Stack;
import com.att.cdp.zones.spi.RequestState;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.heat.Heat;
import java.util.Map;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base.ProviderStackOperation;
import org.onap.ccsdk.sli.adaptors.openstack.heat.SnapshotResource;
import org.onap.ccsdk.sli.adaptors.openstack.heat.StackResource;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.logging.Msg;

public class RestoreStack extends ProviderStackOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RestoreStack.class);

    private void restoreStack(Stack stack, String snapshotId) throws ZoneException, RequestFailedException {
        Context context = stack.getContext();

        OpenStackContext osContext = (OpenStackContext) context;

        final HeatConnector heatConnector = osContext.getHeatConnector();
        ((OpenStackContext) context).refreshIfStale(heatConnector);

        trackRequest(context);
        RequestState.put("SERVICE", "Orchestration");
        RequestState.put("SERVICE_URL", heatConnector.getEndpoint());

        Heat heat = heatConnector.getClient();

        SnapshotResource snapshotResource = new SnapshotResource(heat);

        try {

            snapshotResource.restore(stack.getName(), stack.getId(), snapshotId).execute();

            // wait for the snapshot restore
            StackResource stackResource = new StackResource(heat);
            if (!waitForStack(stack, stackResource, "RESTORE_COMPLETE")) {
                throw new RequestFailedException("Snapshot restore failed.");
            }

        } catch (OpenStackBaseException e) {
            ExceptionMapper.mapException(e);
        }

    }

    public Stack restoreStack(Map<String, String> params, SvcLogicContext ctx)
            throws SvcLogicException {
        Stack stack = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        ctx.setAttribute("SNAPSHOT_STATUS", "STACK_NOT_FOUND");
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        String vmUrl = null;
        Context context = null;
        String tenantName = "Unknown";//to be used also in case of exception
        try {

            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME, ProviderAdapter.PROPERTY_STACK_ID,
                    ProviderAdapter.PROPERTY_INPUT_SNAPSHOT_ID);

            String stackId = params.get(ProviderAdapter.PROPERTY_STACK_ID);
            vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            String snapshotId = params.get(ProviderAdapter.PROPERTY_INPUT_SNAPSHOT_ID);

            context = resolveContext(rc, params, appName, vmUrl);

            if (context != null) {
                tenantName = context.getTenantName();//this varaible also is used in case of exception
                stack = lookupStack(rc, context, stackId);
                logger.debug(Msg.STACK_FOUND, vmUrl, tenantName, stack.getStatus().toString());
                logger.info(EELFResourceManager.format(Msg.TERMINATING_STACK, stack.getName()));
                restoreStack(stack, snapshotId);
                logger.info(EELFResourceManager.format(Msg.TERMINATE_STACK, stack.getName()));
                context.close();
                doSuccess(rc);
            } else {
                ctx.setAttribute(Constants.DG_ATTRIBUTE_STATUS, "failure");
            }

        } catch (ResourceNotFoundException e) {
            String msg = EELFResourceManager.format(Msg.STACK_NOT_FOUND, e, vmUrl);
            logger.error(msg);
            doFailure(rc, HttpStatus.NOT_FOUND_404, msg, e);
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.MISSING_PARAMETER_IN_REQUEST, e.getReason(), "restoreStack"));
            doFailure(rc, e.getStatus(), e.getMessage(), e);
        } catch (Exception e1) {
            String msg = EELFResourceManager.format(Msg.STACK_OPERATION_EXCEPTION, e1, e1.getClass().getSimpleName(),
                    "restoreStack", vmUrl, tenantName);
            logger.error(msg, e1);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg, e1);
        }
        return stack;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.RESTORE_STACK.toString(), "App-C IaaS Adapter:Restore-Stack", Constants.ADAPTER_NAME);
        logOperation(Msg.RESTORING_STACK, params, context);
        return restoreStack(params, context);
    }
}
