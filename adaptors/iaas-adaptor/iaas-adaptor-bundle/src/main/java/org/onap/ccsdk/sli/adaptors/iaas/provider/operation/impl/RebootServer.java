/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * Modifications Copyright (C) 2019 IBM
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
import com.att.cdp.exceptions.StateException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.core.utils.logging.Msg;
import org.onap.ccsdk.sli.adaptors.iaas.Property;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.IdentityURL;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.adaptors.iaas.impl.VMURL;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class RebootServer extends ProviderServerOperation {
    private final EELFLogger logger = EELFManager.getInstance().getLogger(RebootServer.class);
    private static final Configuration config = ConfigurationFactory.getConfiguration();
    private static final Integer NO_OF_ATTEMPTS = 30;
    private static final Integer RETRY_INTERVAL = 10;

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.REBOOT_SERVICE.toString(), "App-C IaaS Adapter:rebootServer", Constants.ADAPTER_NAME);
        return rebootServer(params, context);
    }

    private Server rebootServer(Map<String, String> params, SvcLogicContext ctx){
        Server server = null;
        RequestContext requestContext = new RequestContext(ctx);
        requestContext.isAlive();
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
        String rebooType = params.get(ProviderAdapter.REBOOT_TYPE);
        String tenantName = "Unknown";
        if (rebooType.isEmpty()) {
            rebooType = "SOFT";
        }
        logger.info("reboot type" + rebooType);
        if (ctx != null) {
        try {
            VMURL vm = VMURL.parseURL(vmUrl);
            Context context;
            // to be used also in case of exception
            if (validateVM(requestContext, appName, vmUrl, vm)) {
                return null;
            }
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            context = getContext(requestContext, vmUrl, identStr);
            // Is the skip Hypervisor check attribute populated?
            String skipHypervisorCheck = configuration.getProperty(Property.SKIP_HYPERVISOR_CHECK);
            if (skipHypervisorCheck == null && ctx != null) {
                skipHypervisorCheck = ctx.getAttribute(ProviderAdapter.SKIP_HYPERVISOR_CHECK);
            }
            if (context != null) {
                tenantName = context.getTenantName();// this variable also is
                // used in case of exception
                requestContext.reset();
                server = lookupServer(requestContext, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vmUrl, context.getTenantName(), server.getStatus().toString());
                // Always perform Hypervisor check
                // unless the skip is set to true
                if (skipHypervisorCheck == null || (!skipHypervisorCheck.equalsIgnoreCase("true"))) {
                    // Check of the Hypervisor for the VM Server is UP and reachable
                    checkHypervisor(server);
                }
                Context contx = server.getContext();
                ComputeService service = contx.getComputeService();
                logger.info("performing reboot action for " + server.getId() + " rebootype " + rebooType);
                service.rebootServer(server.getId(), rebooType);
                if (waitForServerStatusChange(requestContext, server, vmUrl, Server.Status.RUNNING)) {
                    ctx.setAttribute("REBOOT_STATUS", "SUCCESS");
                    doSuccess(requestContext);
                } else {
                    ctx.setAttribute("REBOOT_STATUS", "FAILURE");
                }
                context.close();
            } else {
                ctx.setAttribute("REBOOT_STATUS", "FAILURE");
            }

        } catch (ResourceNotFoundException | StateException ex) {
            ctx.setAttribute("REBOOT_STATUS", "FAILURE");
            if (ex instanceof ResourceNotFoundException) {
                doFailure(requestContext, HttpStatus.NOT_FOUND_404, ex.getMessage());
            } else {
                doFailure(requestContext, HttpStatus.CONFLICT_409, ex.getMessage());
            }
        } catch (Exception ex) {
            ctx.setAttribute("REBOOT_STATUS", "FAILURE");
            doFailure(requestContext, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.getMessage());
        }
        }
        return server;
    }

    private boolean waitForServerStatusChange(RequestContext rc, Server server, String vmUrl,
            Server.Status... desiredStates) throws Exception {
        int pollInterval = RETRY_INTERVAL.intValue();
        int timeout = configuration.getIntegerProperty(Constants.PROPERTY_SERVER_STATE_CHANGE_TIMEOUT);
        config.setProperty(Constants.PROPERTY_RETRY_DELAY, RETRY_INTERVAL.toString());
        config.setProperty(Constants.PROPERTY_RETRY_LIMIT, NO_OF_ATTEMPTS.toString());
        String msg;
        boolean status = false;
        while (rc.attempt()) {
            if ( Server.Status.ERROR.equals(server.getStatus())) {
                msg = "Device status " + Server.Status.ERROR + " cannot proceed further";
                throw new RequestFailedException("Waiting for State Change", msg, HttpStatus.CONFLICT_409, server);
            } else {
                server.waitForStateChange(pollInterval, timeout, desiredStates);
                if (Server.Status.RUNNING.equals(server.getStatus())
                        || Server.Status.READY.equals(server.getStatus())) {
                    status = true;
                }
                logger.info(server.getStatus() + " status ");
            }
            if (status) {
                logger.info("Done Trying " + rc.getAttempts() + " attempts");
                break;
            } else {
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, vmUrl);
            logger.info("waitForStateChange Failed");
            logger.error(msg);
            throw new RequestFailedException("Waiting for State Change", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        if ((rc.getAttempts() == NO_OF_ATTEMPTS) && (!status)) {

            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, Long.toString(rc.getRetryDelay()),
                    Integer.toString(rc.getAttempts()), Integer.toString(rc.getRetryLimit()));
            logger.error(msg);
            throw new TimeoutException(msg);
        }
        rc.reset();
        logger.info("Reboot server status flag --> " + status);
        return status;
    }
}
