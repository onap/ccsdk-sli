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

import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.Map;
import org.glassfish.grizzly.http.util.HttpStatus;
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
import org.onap.ccsdk.sli.core.utils.logging.Msg;

public class TerminateServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(EvacuateServer.class);

    /**
     * Start the server and wait for it to enter a running state
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param server The server to be started
     * @throws ZoneException when error occurs
     * @throws RequestFailedException when request failed
     */
    @SuppressWarnings("nls")
    private void deleteServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                logger.info("deleting SERVER");
                server.delete();
                break;
            } catch (ContextConnectionException e) {
                msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                        context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                        Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                        Integer.toString(rc.getRetryLimit()));
                logger.error(msg, e);
                rc.delay();
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Delete Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
    }

    /**
     * This method handles the case of restarting a server once we have found the server and have obtained the abstract
     * representation of the server via the context (i.e., the "Server" object from the CDP-Zones abstraction).
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param server The server object representing the server we want to operate on
     * @throws ZoneException when error occurs
     */
    @SuppressWarnings("nls")
    private void terminateServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        /*
         * Pending is a bit of a special case. If we find the server is in a pending state, then the provider is in the
         * process of changing state of the server. So, lets try to wait a little bit and see if the state settles down
         * to one we can deal with. If not, then we have to fail the request.
         */
        String msg;
        if (server.getStatus().equals(Server.Status.PENDING)) {
            waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR,
                    Server.Status.SUSPENDED, Server.Status.PAUSED);
        }

        /*
         * We determine what to do based on the current state of the server
         */
        switch (server.getStatus()) {
            case DELETED:
                // Nothing to do, the server is gone
                msg = EELFResourceManager.format(Msg.SERVER_DELETED, server.getName(), server.getId(),
                        server.getTenantId(), "restarted");
                generateEvent(rc, false, msg);
                logger.error(msg);
                break;

            case RUNNING:
                // Attempt to stop and start the server
                logger.info("stopping SERVER");
                stopServer(rc, server);
                deleteServer(rc, server);
                logger.info("after delete SERVER");
                generateEvent(rc, true, Outcome.SUCCESS.toString());
                break;

            case ERROR:

            case READY:

            case PAUSED:

            case SUSPENDED:
                // Attempt to delete the suspended server
                deleteServer(rc, server);
                generateEvent(rc, true, Outcome.SUCCESS.toString());
                break;

            default:
                // Hmmm, unknown status, should never occur
                msg = EELFResourceManager.format(Msg.UNKNOWN_SERVER_STATE, server.getName(), server.getId(),
                        server.getTenantId(), server.getStatus().name());
                generateEvent(rc, false, msg);
                logger.error(msg);
                break;
        }

    }

    /**
     * This method is used to delete an existing virtual machine given the fully qualified URL of the machine.
     * <p>
     * The fully qualified URL contains enough information to locate the appropriate server. The URL is of the form
     *
     * <pre>
     *  [scheme]://[host[:port]] / [path] / [tenant_id] / servers / [vm_id]
     * </pre>
     *
     * Where the various parts of the URL can be parsed and extracted and used to locate the appropriate service in the
     * provider service catalog. This then allows us to open a context using the CDP abstraction, obtain the server by
     * its UUID, and then perform the restart.
     * </p>
     *
     * @throws SvcLogicException If the provider cannot be found
     * @throws IllegalArgumentException if the expected argument(s) are not defined or are invalid
     * @see ProviderAdapter#terminateServer(java.util.Map,
     *      org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    public Server terminateServer(Map<String, String> params, SvcLogicContext ctx)
            throws SvcLogicException, IllegalArgumentException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);

            String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            ctx.setAttribute("TERMINATE_STATUS", "SUCCESS");

            VMURL vm = VMURL.parseURL(vmUrl);
            if (validateVM(rc, appName, vmUrl, vm))
                return null;

            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();

            Context context = null;
            String tenantName = "Unknown";//to be used also in case of exception
            try {
                context = getContext(rc, vmUrl, identStr);
                if (context != null) {
                    tenantName = context.getTenantName();//this varaible also is used in case of exception
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vmUrl, tenantName, server.getStatus().toString());
                    logger.info(EELFResourceManager.format(Msg.TERMINATING_SERVER, server.getName()));
                    terminateServer(rc, server);
                    logger.info(EELFResourceManager.format(Msg.TERMINATE_SERVER, server.getName()));
                    context.close();
                    doSuccess(rc);
                } else {
                    ctx.setAttribute("TERMINATE_STATUS", "SERVER_NOT_FOUND");
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
                ctx.setAttribute("TERMINATE_STATUS", "SERVER_NOT_FOUND");
            } catch (Exception e1) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1,
                        e1.getClass().getSimpleName(), Operation.RESTART_SERVICE.toString(), vmUrl,
                        tenantName);
                logger.error(msg, e1);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            logger.error(
                    EELFResourceManager.format(Msg.TERMINATE_SERVER_FAILED, appName, "n/a", "n/a", e.getMessage()));
            doFailure(rc, e.getStatus(), e.getMessage());
            ctx.setAttribute("TERMINATE_STATUS", "ERROR");
        }

        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.TERMINATE_SERVICE.toString(), "App-C IaaS Adapter:Terminate", Constants.ADAPTER_NAME);
        logOperation(Msg.TERMINATING_SERVER, params, context);
        return terminateServer(params, context);
    }
}
