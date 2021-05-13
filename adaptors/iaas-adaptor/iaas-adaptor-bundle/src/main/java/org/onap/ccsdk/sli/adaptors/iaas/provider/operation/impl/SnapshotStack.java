/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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
import java.util.Date;
import java.util.Map;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base.ProviderStackOperation;
import org.onap.ccsdk.sli.adaptors.openstack.heat.SnapshotResource;
import org.onap.ccsdk.sli.adaptors.openstack.heat.StackResource;
import org.onap.ccsdk.sli.adaptors.openstack.heat.model.CreateSnapshotParams;
import org.onap.ccsdk.sli.adaptors.openstack.heat.model.Snapshot;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.logging.LoggingConstants;
import org.onap.ccsdk.sli.core.utils.logging.LoggingUtils;
import org.onap.ccsdk.sli.core.utils.logging.Msg;
import org.slf4j.MDC;

public class SnapshotStack extends ProviderStackOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(SnapshotStack.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();

    private Snapshot snapshotStack(@SuppressWarnings("unused") RequestContext rc, Stack stack)
            throws ZoneException, RequestFailedException {
        Snapshot snapshot = new Snapshot();
        Context context = stack.getContext();
        OpenStackContext osContext = (OpenStackContext) context;
        final HeatConnector heatConnector = osContext.getHeatConnector();
        ((OpenStackContext) context).refreshIfStale(heatConnector);
        trackRequest(context);
        RequestState.put("SERVICE", "Orchestration");
        RequestState.put("SERVICE_URL", heatConnector.getEndpoint());
        Heat heat = heatConnector.getClient();
        SnapshotResource snapshotResource = new SnapshotResource(heat);
        setTimeForMetricsLogger();
        try {
            snapshot = snapshotResource.create(stack.getName(), stack.getId(), new CreateSnapshotParams()).execute();
            // wait for the stack deletion
            StackResource stackResource = new StackResource(heat);
            if (!waitForStack(stack, stackResource, "SNAPSHOT_COMPLETE")) {
                throw new RequestFailedException("Stack Snapshot failed.");
            }
        } catch (OpenStackBaseException e) {
            ExceptionMapper.mapException(e);
        }
        return snapshot;
    }

    public Stack snapshotStack(Map<String, String> params, SvcLogicContext ctx)
            throws IllegalArgumentException, SvcLogicException {
        Stack stack = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        ctx.setAttribute("SNAPSHOT_STATUS", "STACK_NOT_FOUND");
        setTimeForMetricsLogger();
        String vmUrl = null;
        Context context = null;
        String tenantName = "Unknown";// to be used also in case of exception
        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME, ProviderAdapter.PROPERTY_STACK_ID);
            String stackId = params.get(ProviderAdapter.PROPERTY_STACK_ID);
            String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
            vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            context = resolveContext(rc, params, appName, vmUrl);
            if (context != null) {
                tenantName = context.getTenantName();// this varaible also is used in case of exception
                stack = lookupStack(rc, context, stackId);
                logger.debug(Msg.STACK_FOUND, vmUrl, tenantName, stack.getStatus().toString());
                logger.info(EELFResourceManager.format(Msg.SNAPSHOTING_STACK, stack.getName()));
                metricsLogger.info(EELFResourceManager.format(Msg.SNAPSHOTING_STACK, stack.getName()));
                Snapshot snapshot = snapshotStack(rc, stack);
                ctx.setAttribute(ProviderAdapter.DG_OUTPUT_PARAM_NAMESPACE + ProviderAdapter.PROPERTY_SNAPSHOT_ID,
                        snapshot.getId());
                logger.info(EELFResourceManager.format(Msg.STACK_SNAPSHOTED, stack.getName(), snapshot.getId()));
                metricsLogger.info(EELFResourceManager.format(Msg.STACK_SNAPSHOTED, stack.getName(), snapshot.getId()));
                context.close();
                doSuccess(rc);
            } else {
                ctx.setAttribute(Constants.DG_ATTRIBUTE_STATUS, "failure");
            }
        } catch (ResourceNotFoundException e) {
            String msg = EELFResourceManager.format(Msg.STACK_NOT_FOUND, e, vmUrl);
            logger.error(msg);
            metricsLogger.error(msg);
            doFailure(rc, HttpStatus.NOT_FOUND_404, msg, e);
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.MISSING_PARAMETER_IN_REQUEST, e.getReason(), "snapshotStack"));
            metricsLogger.error(
                    EELFResourceManager.format(Msg.MISSING_PARAMETER_IN_REQUEST, e.getReason(), "snapshotStack"));
            doFailure(rc, e.getStatus(), e.getMessage(), e);
        } catch (Exception e1) {
            String msg = EELFResourceManager.format(Msg.STACK_OPERATION_EXCEPTION, e1, e1.getClass().getSimpleName(),
                    "snapshotStack", vmUrl, tenantName);
            logger.error(msg, e1);
            metricsLogger.error(msg);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg, e1);
        }
        return stack;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.SNAPSHOT_STACK.toString(), "App-C IaaS Adapter:Snapshot-Stack", Constants.ADAPTER_NAME);
        logOperation(Msg.SNAPSHOTING_STACK, params, context);
        setTimeForMetricsLogger();
        metricsLogger.info("Executing Provider Operation: Snapshot Stack");
        return snapshotStack(params, context);
    }

    private void setTimeForMetricsLogger() {
        String timestamp = LoggingUtils.generateTimestampStr(((Date) new Date()).toInstant());
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, timestamp);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, timestamp);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");
        MDC.put(LoggingConstants.MDCKeys.STATUS_CODE, LoggingConstants.StatusCodes.COMPLETE);
        MDC.put(LoggingConstants.MDCKeys.TARGET_ENTITY, "cdp");
        MDC.put(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME, "snapshot stack");
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME,
                "org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.SnapshotStack");
    }
}
