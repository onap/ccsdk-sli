/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
* ================================================================================
* Copyright (C) 2017 Amdocs
* =============================================================================
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

import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.exceptions.StateException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.ImageService;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.ServerBootSource;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
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
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.core.utils.logging.LoggingConstants;
import org.onap.ccsdk.sli.core.utils.logging.LoggingUtils;
import org.onap.ccsdk.sli.core.utils.logging.Msg;
import org.slf4j.MDC;

public class RebuildServer extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RebuildServer.class);
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();
    // the sleep time used by thread.sleep to give "some time for OpenStack to start
    // processing the request"
    private long rebuildSleepTime = 10L * 1000L;

    /**
     * Rebuild the indicated server with the indicated image. This method assumes
     * the server has been determined to be in the correct state to do the rebuild.
     *
     * @param rc
     *            The request context that manages the state and recovery of the
     *            request for the life of its processing.
     * @param server
     *            the server to be rebuilt
     * @param image
     *            The image to be used (or snapshot)
     * @throws RequestFailedException
     *             if the server does not change state in the allotted time
     */
    @SuppressWarnings("nls")
    private void rebuildServer(RequestContext rc, Server server, String image) throws RequestFailedException {
        logger.debug(Msg.REBUILD_SERVER, server.getId());
        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        /*
         * Set Time for Metrics Logger
         */
        setTimeForMetricsLogger();
        try {
            while (rc.attempt()) {
                try {
                    server.rebuild(image);
                    break;
                } catch (ContextConnectionException e) {
                    msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                            context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                            Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                            Integer.toString(rc.getRetryLimit()));
                    logger.error(msg, e);
                    metricsLogger.error(msg, e);
                    rc.delay();
                }
            }
            /*
             * We need to provide some time for OpenStack to start processing the request.
             */
            try {
                Thread.sleep(rebuildSleepTime);
            } catch (InterruptedException e) {
                logger.trace("Sleep threw interrupted exception, should never occur");
                metricsLogger.trace("Sleep threw interrupted exception, should never occur");
                Thread.currentThread().interrupt();
            }
        } catch (ZoneException e) {
            msg = EELFResourceManager.format(Msg.REBUILD_SERVER_FAILED, server.getName(), server.getId(),
                    e.getMessage());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        /*
         * Once we have started the process, now we wait for the final state of stopped.
         * This should be the final state (since we started the rebuild with the server
         * stopped).
         */
        waitForStateChange(rc, server, Server.Status.READY);
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
    }

    /**
     * This method is called to rebuild the provided server.
     * <p>
     * If the server was booted from a volume, then the request is failed
     * immediately and no action is taken. Rebuilding a VM from a bootable volume,
     * where the bootable volume itself is not rebuilt, serves no purpose.
     * </p>
     *
     * @param rc
     *            The request context that manages the state and recovery of the
     *            request for the life of its processing.
     * @param server
     *            The server to be rebuilt
     * @throws ZoneException
     *             When error occurs
     * @throws RequestFailedException
     *             When server status is error
     */
    @SuppressWarnings("nls")
    private void rebuildServer(RequestContext rc, Server server, SvcLogicContext ctx)
            throws ZoneException, RequestFailedException {
        ServerBootSource builtFrom = server.getBootSource();
        /*
         * Set Time for Metrics Logger
         */
        setTimeForMetricsLogger();
        String msg;
        // Throw error if boot source is unknown
        if (ServerBootSource.UNKNOWN.equals(builtFrom)) {
            logger.debug("Boot Source Unknown");
            msg = String.format("Error occured when retrieving server boot source [%s]!!!", server.getId());
            logger.error(msg);
            generateEvent(rc, false, msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.INTERNAL_SERVER_ERROR_500, server);
        }

        // Throw exception for non image/snap boot source
        if (ServerBootSource.VOLUME.equals(builtFrom)) {
            logger.debug("Boot Source Not Supported built from bootable volume");
            msg = String.format("Rebuilding is currently not supported for servers built from bootable volumes [%s]",
                    server.getId());
            generateEvent(rc, false, msg);
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.FORBIDDEN_403, server);
        }
        /*
         * Pending is a bit of a special case. If we find the server is in a pending
         * state, then the provider is in the process of changing state of the server.
         * So, lets try to wait a little bit and see if the state settles down to one we
         * can deal with. If not, then we have to fail the request.
         */
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        if (server.getStatus().equals(Server.Status.PENDING)) {
            rc.reset();
            waitForStateChange(rc, server, Server.Status.READY, Server.Status.RUNNING, Server.Status.ERROR,
                    Server.Status.SUSPENDED, Server.Status.PAUSED);
        }
        // Is the skip Hypervisor check attribute populated?
        String skipHypervisorCheck = configuration.getProperty(Property.SKIP_HYPERVISOR_CHECK);
        if (skipHypervisorCheck == null && ctx != null) {
            skipHypervisorCheck = ctx.getAttribute(ProviderAdapter.SKIP_HYPERVISOR_CHECK);
        }
        // Always perform Hypervisor Status checks
        // unless the skip is set to true
        if (skipHypervisorCheck == null || (!"true".equalsIgnoreCase(skipHypervisorCheck))) {
            // Check of the Hypervisor for the VM Server is UP and reachable
            checkHypervisor(server);
        }
        /*
         * Get the image to use in this priority order: (1) If snapshot-id provided in
         * the request, use this (2) If any snapshots exist, then the latest snapshot is
         * used (3) Otherwise the image used to construct the VM is used.
         */
        String imageToUse = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            String payloadStr = configuration.getProperty(Property.PAYLOAD);
            if (payloadStr == null || payloadStr.isEmpty()) {
                payloadStr = ctx.getAttribute(ProviderAdapter.PAYLOAD);
            }
            JsonNode payloadNode = mapper.readTree(payloadStr);
            imageToUse = payloadNode.get(ProviderAdapter.PROPERTY_REQUEST_SNAPSHOT_ID).textValue();
            logger.debug("Pulled snapshot-id " + imageToUse + " from the payload");
        } catch (Exception e) {
            logger.debug("Exception attempting to pull snapshot-id from the payload: " + e.toString());
        }
        List<Image> snapshots = server.getSnapshots();
        ImageService imageService = server.getContext().getImageService();
        List<Image> imageList = imageService.listImages();
        if (!imageToUse.isEmpty()) {
            logger.debug("Using snapshot-id " + imageToUse + " for the rebuild request");
            boolean imgFound = validateSnapshotId(imageToUse, snapshots, imageList);

            if (!imgFound) {
                logger.debug("Image Snapshot Not Found");
                msg = EELFResourceManager.format(Msg.REBUILD_SERVER_FAILED, server.getName(), server.getId(),
                        "Invalid Snapshot-Id");
                logger.error(msg);
                metricsLogger.error(msg);
                throw new RequestFailedException("Rebuild Server", msg, HttpStatus.FORBIDDEN_403, server);
            }
        } else if (snapshots != null && !snapshots.isEmpty()) {
            logger.debug("Using snapshot-id when image is Empty" + imageToUse + " for the rebuild request");
            imageToUse = snapshots.get(0).getId();
        } else {
            imageToUse = server.getImage();
            rc.reset();
            try {
                while (rc.attempt()) {
                    try {
                        /*
                         * We are just trying to make sure that the image exists. We arent interested in
                         * the details at this point.
                         */
                        imageService.getImage(imageToUse);
                        break;
                    } catch (ContextConnectionException e) {
                        msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(),
                                imageService.getURL(), context.getTenant().getName(), context.getTenant().getId(),
                                e.getMessage(), Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                                Integer.toString(rc.getRetryLimit()));
                        logger.error(msg, e);
                        metricsLogger.error(msg);
                        rc.delay();
                    }
                }
            } catch (ZoneException e) {
                msg = EELFResourceManager.format(Msg.IMAGE_NOT_FOUND, imageToUse, "rebuild");
                generateEvent(rc, false, msg);
                logger.error(msg);
                metricsLogger.error(msg);
                throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
            }
        }
        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        /*
         * We determine what to do based on the current state of the server
         */
        switch (server.getStatus()) {
        case DELETED:
            // Nothing to do, the server is gone
            msg = EELFResourceManager.format(Msg.SERVER_DELETED, server.getName(), server.getId(), server.getTenantId(),
                    "rebuilt");
            generateEvent(rc, false, msg);
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
        case RUNNING:
            // Attempt to stop the server, then rebuild it
            stopServer(rc, server);
            rc.reset();
            rebuildServer(rc, server, imageToUse);
            rc.reset();
            startServer(rc, server);
            generateEvent(rc, true, Outcome.SUCCESS.toString());
            metricsLogger.info("Server status: RUNNING");
            break;
        case ERROR:
            msg = EELFResourceManager.format(Msg.SERVER_ERROR_STATE, server.getName(), server.getId(),
                    server.getTenantId(), "rebuild");
            generateEvent(rc, false, msg);
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
        case READY:
            // Attempt to rebuild the server
            rebuildServer(rc, server, imageToUse);
            rc.reset();
            startServer(rc, server);
            generateEvent(rc, true, Outcome.SUCCESS.toString());
            metricsLogger.info("Server status: READY");
            break;
        case PAUSED:
            // if paused, un-pause it, stop it, and rebuild it
            unpauseServer(rc, server);
            rc.reset();
            stopServer(rc, server);
            rc.reset();
            rebuildServer(rc, server, imageToUse);
            rc.reset();
            startServer(rc, server);
            generateEvent(rc, true, Outcome.SUCCESS.toString());
            metricsLogger.info("Server status: PAUSED");
            break;
        case SUSPENDED:
            // Attempt to resume the suspended server, stop it, and rebuild it
            resumeServer(rc, server);
            rc.reset();
            stopServer(rc, server);
            rc.reset();
            rebuildServer(rc, server, imageToUse);
            rc.reset();
            startServer(rc, server);
            generateEvent(rc, true, Outcome.SUCCESS.toString());
            metricsLogger.info("Server status: SUSPENDED");
            break;
        default:
            // Hmmm, unknown status, should never occur
            msg = EELFResourceManager.format(Msg.UNKNOWN_SERVER_STATE, server.getName(), server.getId(),
                    server.getTenantId(), server.getStatus().name());
            generateEvent(rc, false, msg);
            logger.error(msg);
            metricsLogger.error(msg);
            throw new RequestFailedException("Rebuild Server", msg, HttpStatus.METHOD_NOT_ALLOWED_405, server);
        }
    }

    /**
     * @see ProviderAdapter#rebuildServer(java.util.Map,
     *      org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @SuppressWarnings("nls")
    public Server rebuildServer(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();
        setTimeForMetricsLogger();
        String msg;
        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);
            String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
            String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
            VMURL vm = VMURL.parseURL(vmUrl);
            if (validateVM(rc, appName, vmUrl, vm))
                return null;
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            ctx.setAttribute("REBUILD_STATUS", "ERROR");
            Context context = null;
            String tenantName = "Unknown";// to be used also in case of exception
            try {
                context = getContext(rc, vmUrl, identStr);
                if (context != null) {
                    tenantName = context.getTenantName();// this varaible also is used in case of exception
                    rc.reset();
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vmUrl, tenantName, server.getStatus().toString());
                    // Manually checking image service until new PAL release
                    if (hasImageAccess(rc, context)) {
                        rebuildServer(rc, server, ctx);
                        doSuccess(rc);
                        ctx.setAttribute("REBUILD_STATUS", "SUCCESS");
                    } else {
                        msg = EELFResourceManager.format(Msg.REBUILD_SERVER_FAILED, server.getName(), server.getId(),
                                "Accessing Image Service Failed");
                        logger.error(msg);
                        metricsLogger.error(msg);
                        doFailure(rc, HttpStatus.FORBIDDEN_403, msg);
                    }
                    context.close();
                } else {
                    ctx.setAttribute("REBUILD_STATUS", "CONTEXT_NOT_FOUND");
                }
            } catch (StateException ex) {
                logger.error(ex.getMessage());
                ctx.setAttribute("REBUILD_STATUS", "ERROR");
                doFailure(rc, HttpStatus.CONFLICT_409, ex.getMessage());
            } catch (RequestFailedException e) {
                doFailure(rc, e.getStatus(), e.getMessage());
                ctx.setAttribute("REBUILD_STATUS", "ERROR");
            } catch (ResourceNotFoundException e) {
                msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
                ctx.setAttribute("REBUILD_STATUS", "ERROR");
                logger.error(msg);
                metricsLogger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Exception e1) {
                msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1, e1.getClass().getSimpleName(),
                        Operation.STOP_SERVICE.toString(), vmUrl, tenantName);
                ctx.setAttribute("REBUILD_STATUS", "ERROR");
                logger.error(msg, e1);
                metricsLogger.error(msg);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            ctx.setAttribute("REBUILD_STATUS", "ERROR");
            doFailure(rc, e.getStatus(), e.getMessage());
        }
        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.REBUILD_SERVICE.toString(), "App-C IaaS Adapter:Rebuild", Constants.ADAPTER_NAME);
        logOperation(Msg.REBUILDING_SERVER, params, context);
        setTimeForMetricsLogger();
        metricsLogger.info("Executing Provider Operation: Rebuild");
        return rebuildServer(params, context);
    }

    private void setTimeForMetricsLogger() {
        String timestamp = LoggingUtils.generateTimestampStr(((Date) new Date()).toInstant());
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, timestamp);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, timestamp);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");
        MDC.put(LoggingConstants.MDCKeys.STATUS_CODE, LoggingConstants.StatusCodes.COMPLETE);
        MDC.put(LoggingConstants.MDCKeys.TARGET_ENTITY, "cdp");
        MDC.put(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME, "rebuild server");
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME,
                "org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.RebuildServer");

    }

    /**
     * Sets the sleep time used by thread.sleep to give "some time for OpenStack to
     * start processing the request".
     *
     * @param millis
     *            Time to sleep in milliseconds
     */
    public void setRebuildSleepTime(long millis) {
        this.rebuildSleepTime = millis;
    }

    private boolean validateSnapshotId(String imageToUse, List<Image> snapshotList, List<Image> imageList) {

        logger.debug("Validating snapshot-id " + imageToUse + " for the rebuild request from payload");
        boolean imageFound = false;
        // If image is empty , the validation si not required . Hence return false.
        // Ideally function should not be called with empty image Id
        if (imageToUse.isEmpty()) {
            return imageFound;
        } else {
            // The supplied snapshot id can be a snapshot id or an image Id. Check both
            // available for the vnf.
            // Check against snapshot id list and image list
            return findImageExists(snapshotList, imageToUse, "snapshotidList")
                    || findImageExists(imageList, imageToUse, "imageidList");
        }
    }

    boolean findImageExists(List<Image> list, String imageToUse, String source) {
        boolean imageExists = false;
        logger.debug("Available Image-ids from :" + source + "Start\n");
        for (Image img : list) {
            String imgId = img.getId();
            logger.debug("Image Id - " + imgId + "\n");
            if (imgId.equals(imageToUse)) {
                logger.debug("Image found in available " + source);
                imageExists = true;
                break;
            }
        }
        return imageExists;

    }
}
