/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (c) 2019 IBM
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
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.Date;
import java.util.Map;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.ccsdk.sli.adaptors.iaas.Property;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.IdentityURL;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.adaptors.iaas.impl.VMURL;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Outcome;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.logging.LoggingConstants;
import org.onap.ccsdk.sli.core.utils.logging.LoggingUtils;
import org.onap.ccsdk.sli.core.utils.logging.Msg;
import org.slf4j.MDC;

public class StopServer extends ProviderServerOperation {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(StopServer.class);
    private final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();

    @SuppressWarnings("nls")
    private Server stopServer(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        setTimeForMetricsLogger();
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            ctx.setAttribute("STOP_STATUS", "SUCCESS");
            VMURL vm = VMURL.parseURL(vmUrl);
            if (validateVM(rc, appName, vmUrl, vm))
                return null;
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            Context context = null;
            ctx.setAttribute("STOP_STATUS", "ERROR");
            // Is the skip Hypervisor check attribute populated?
            String skipHypervisorCheck = configuration.getProperty(Property.SKIP_HYPERVISOR_CHECK);
            if (skipHypervisorCheck == null && ctx != null) {
                skipHypervisorCheck = ctx.getAttribute(ProviderAdapter.SKIP_HYPERVISOR_CHECK);
            }
            try {
                context = getContext(rc, vmUrl, identStr);
                if (context != null) {
                    rc.reset();
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vmUrl, context.getTenantName(), server.getStatus().toString());
                    // Always perform Hypervisor check
                    // unless the skip is set to true
                    if (skipHypervisorCheck == null || (!skipHypervisorCheck.equalsIgnoreCase("true"))) {
                        // Check of the Hypervisor for the VM Server is UP and reachable
                        checkHypervisor(server);
                    }
                    String msg;
                    /*
                     * We determine what to do based on the current state of the server
                     */
                    /*
                     * Pending is a bit of a special case. If we find the server is in a pending
                     * state, then the provider is in the process of changing state of the server.
                     * So, lets try to wait a little bit and see if the state settles down to one we
                     * can deal with. If not, then we have to fail the request.
                     */
                    if (server.getStatus().equals(Server.Status.PENDING)) {
                        waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR,
                                Server.Status.SUSPENDED, Server.Status.PAUSED, Server.Status.DELETED);
                    }
                    switch (server.getStatus()) {
                    case DELETED:
                        // Nothing to do, the server is gone
                        msg = EELFResourceManager.format(Msg.SERVER_DELETED, server.getName(), server.getId(),
                                server.getTenantId(), "stopped");
                        generateEvent(rc, false, msg);
                        logger.error(msg);
                        metricsLogger.error(msg);
                        throw new RequestFailedException("Stop Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
                    case RUNNING:
                        // Attempt to stop the server
                        rc.reset();
                        stopServer(rc, server);
                        generateEvent(rc, true, Outcome.SUCCESS.toString());
                        break;
                    case ERROR:
                        // Server is in error state
                        msg = EELFResourceManager.format(Msg.SERVER_ERROR_STATE, server.getName(), server.getId(),
                                server.getTenantId(), "stop");
                        generateEvent(rc, false, msg);
                        logger.error(msg);
                        metricsLogger.error(msg);
                        throw new RequestFailedException("Stop Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
                    case READY:
                        // Nothing to do, the server was already stopped
                        logger.info("Server was already stopped");
                        break;
                    case PAUSED:
                        // if paused, un-pause it and then stop it
                        rc.reset();
                        unpauseServer(rc, server);
                        rc.reset();
                        stopServer(rc, server);
                        generateEvent(rc, true, Outcome.SUCCESS.toString());
                        break;
                    case SUSPENDED:
                        // Attempt to resume the suspended server and after that stop it
                        rc.reset();
                        resumeServer(rc, server);
                        rc.reset();
                        stopServer(rc, server);
                        generateEvent(rc, true, Outcome.SUCCESS.toString());
                        break;
                    default:
                        // Hmmm, unknown status, should never occur
                        msg = EELFResourceManager.format(Msg.UNKNOWN_SERVER_STATE, server.getName(), server.getId(),
                                server.getTenantId(), server.getStatus().name());
                        generateEvent(rc, false, msg);
                        logger.error(msg);
                        metricsLogger.error(msg);
                        throw new RequestFailedException("Stop Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
                    }
                    context.close();
                    doSuccess(rc);
                    ctx.setAttribute("STOP_STATUS", "SUCCESS");
                    msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "StopServer", vmUrl);
                    ctx.setAttribute(Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);
                } else {
                    ctx.setAttribute("STOP_STATUS", "CONTEXT_NOT_FOUND");
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Exception e1) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1,
                        e1.getClass().getSimpleName(), Operation.STOP_SERVICE.toString(), vmUrl,
                        context == null ? "Unknown" : context.getTenantName());
                logger.error(msg, e1);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            logger.error(EELFResourceManager.format(Msg.STOP_SERVER_FAILED, appName, "n/a", "n/a", e.getMessage()));
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.STOP_SERVICE.toString(), "App-C IaaS Adapter:Stop", Constants.ADAPTER_NAME);
        logOperation(Msg.STOPPING_SERVER, params, context);
        return stopServer(params, context);
    }

    private void setTimeForMetricsLogger() {
        String timestamp = LoggingUtils.generateTimestampStr((new Date()).toInstant());
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, timestamp);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, timestamp);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");
        MDC.put(LoggingConstants.MDCKeys.STATUS_CODE, LoggingConstants.StatusCodes.COMPLETE);
        MDC.put(LoggingConstants.MDCKeys.TARGET_ENTITY, "cdp");
        MDC.put(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME, "stop server");
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME, "org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.StopServer");
    }

}
