/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modification Copyright (C) 2019 IBM
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
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.core.utils.logging.LoggingConstants;
import org.onap.ccsdk.sli.core.utils.logging.LoggingUtils;
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
import org.slf4j.MDC;

public class MigrateServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MigrateServer.class);
    private static final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    /**
     * A list of valid initial VM statuses for a migrate operations
     */
    private final Collection<Server.Status> migratableStatuses = Arrays.asList(Server.Status.READY,
            Server.Status.RUNNING, Server.Status.SUSPENDED);

    private String getConnectionExceptionMessage(RequestContext rc, Context ctx, ZoneException e) throws ZoneException {
        return EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, ctx.getProvider().getName(),
                ctx.getComputeService().getURL(), ctx.getTenant().getName(), ctx.getTenant().getId(), e.getMessage(),
                Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                Integer.toString(rc.getRetryLimit()));
    }

    private void migrateServer(RequestContext rc, Server server, SvcLogicContext svcCtx)
            throws ZoneException, RequestFailedException {
        String msg;
        Context ctx = server.getContext();
        ComputeService service = ctx.getComputeService();
        // Init status will equal final status
        Server.Status initialStatus = server.getStatus();
        if (initialStatus == null) {
            throw new ZoneException("Failed to determine server's starting status");
        }
        // We can only migrate certain statuses
        if (!migratableStatuses.contains(initialStatus)) {
            throw new ZoneException(String.format("Cannot migrate server that is in %s state. Must be in one of [%s]",
                    initialStatus, migratableStatuses));
        }
        setTimeForMetricsLogger();
        // Is the skip Hypervisor check attribute populated?
        String skipHypervisorCheck = configuration.getProperty(Property.SKIP_HYPERVISOR_CHECK);
        if (skipHypervisorCheck == null && svcCtx != null) {
            skipHypervisorCheck = svcCtx.getAttribute(ProviderAdapter.SKIP_HYPERVISOR_CHECK);
        }
        // Always perform Hypervisor check
        // unless the skip is set to true
        if (skipHypervisorCheck == null || (!(("true").equalsIgnoreCase(skipHypervisorCheck)))) {
            // Check of the Hypervisor for the VM Server is UP and reachable
            checkHypervisor(server);
        }

        boolean inConfirmPhase = false;
        rc.reset();
        try {
            while (rc.attempt()) {
                try {
                    if (!inConfirmPhase) {
                        // Initial migrate request
                        service.migrateServer(server.getId());
                        // Wait for change to verify resize
                        waitForStateChange(rc, server, Server.Status.READY);
                        inConfirmPhase = true;
                    }
                    if (server.getStatus() != null && server.getStatus().equals(Server.Status.ERROR)) {
                        msg = "Cannot Perform 'processResize' in  vm_state " + Server.Status.ERROR;
                        logger.info(msg);
                        msg = EELFResourceManager.format(Msg.MIGRATE_SERVER_FAILED, service.getURL());
                        logger.error(msg);
                        logger.info(msg);
                        throw new RequestFailedException("Waiting for State Change", msg, HttpStatus.CONFLICT_409,
                                server);
                    } else {
                        // Verify resize
                        logger.debug("MigrateServer: Before  service.processResize");
                        service.processResize(server);
                        logger.debug("MigrateServer:before 2nd waitForStateChange Current Status:" + server.getStatus()
                                + " Initail Status: " + initialStatus);
                        // Wait for complete. will go back to init status
                        waitForStateChange(rc, server, initialStatus);
                        logger.info("Completed migrate request successfully");
                        metricsLogger.info("Completed migrate request successfully");
                        return;
                    }
                } catch (ContextConnectionException e) {
                    msg = getConnectionExceptionMessage(rc, ctx, e);
                    if (server.getStatus() != null && server.getStatus().equals(Server.Status.ERROR)) {
                        throw new RequestFailedException("Migrate Server", msg, HttpStatus.CONFLICT_409, server);
                    } else {
                        logger.info("Migrate Server: Going to delay in ConnectionException");
                        logger.debug("Server Status: " + server.getStatus());
                        rc.delay();
                    }
                }

            }

        } catch (ZoneException e) {
            String phase = inConfirmPhase ? "VERIFY MIGRATE" : "REQUEST MIGRATE";
            msg = EELFResourceManager.format(Msg.MIGRATE_SERVER_FAILED, server.getName(), server.getId(), phase,
                    e.getMessage());

            logger.error(msg, e);
            throw new RequestFailedException("Migrate Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
        }
    }

    /**
     * @see ProviderAdapter#migrateServer(java.util.Map,
     *      org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    private Server migrateServer(Map<String, String> params, SvcLogicContext ctx) {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        setTimeForMetricsLogger();
        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
            VMURL vm = VMURL.parseURL(vmUrl);
            if (validateVM(rc, appName, vmUrl, vm))
                return null;
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            server = conductServerMigration(rc, vmUrl, identStr, ctx);
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.MIGRATE_SERVICE.toString(), "App-C IaaS Adapter:Migrate", Constants.ADAPTER_NAME);
        logOperation(Msg.MIGRATING_SERVER, params, context);
        setTimeForMetricsLogger();
        metricsLogger.info("Executing Provider Operation: Migrate");
        return migrateServer(params, context);
    }

    private void setTimeForMetricsLogger() {
        String timestamp = LoggingUtils.generateTimestampStr((new Date()).toInstant());
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, timestamp);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, timestamp);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");
        MDC.put(LoggingConstants.MDCKeys.STATUS_CODE, LoggingConstants.StatusCodes.COMPLETE);
        MDC.put(LoggingConstants.MDCKeys.TARGET_ENTITY, "cdp");
        MDC.put(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME, "migrate server");
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME,
                "org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.MigrateServer");

    }

    private Server conductServerMigration(RequestContext rc, String vmUrl, String identStr, SvcLogicContext ctx)
            throws RequestFailedException {
        String msg;
        Context context = getContext(rc, vmUrl, identStr);
        VMURL vm = VMURL.parseURL(vmUrl);
        Server server = null;
        try {
            if (context != null) {
                server = lookupServer(rc, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vmUrl, context.getTenantName(), server.getStatus().toString());
                migrateServer(rc, server, ctx);
                server.refreshStatus();
                context.close();
                doSuccess(rc);
            }
        } catch (IOException | ZoneException e1) {
            msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1, e1.getClass().getSimpleName(),
                    Operation.MIGRATE_SERVICE.toString(), vmUrl, context.getTenantName());
            logger.error(msg, e1);
            metricsLogger.error(msg);
            doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
        }
        return server;
    }
}
