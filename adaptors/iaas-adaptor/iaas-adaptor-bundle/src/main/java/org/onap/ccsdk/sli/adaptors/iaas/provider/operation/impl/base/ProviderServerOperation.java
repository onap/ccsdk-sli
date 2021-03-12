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

package org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base;

import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.NotLoggedInException;
import com.att.cdp.exceptions.TimeoutException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.pal.util.StringHelper;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.ImageService;
import com.att.cdp.zones.NetworkService;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Hypervisor;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Network;
import com.att.cdp.zones.model.Port;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.ArrayList;
import java.util.List;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.utils.logging.Msg;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;

/**
 * @since September 29, 2016
 */
public abstract class ProviderServerOperation extends ProviderOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ProviderServerOperation.class);

    /**
     * Looks up the indicated server using the provided context and returns the server to the caller
     *
     * @param rc The request context
     * @param context The provider context
     * @param id The id of the server
     * @return The server, or null if there is a problem
     * @throws ZoneException If the server cannot be found
     * @throws RequestFailedException If the server cannot be found because we cant connect to the provider
     */
    @SuppressWarnings("nls")
    protected Server lookupServer(RequestContext rc, Context context, String id)
        throws ZoneException, RequestFailedException {
        ComputeService service = context.getComputeService();
        Server server = null;
        String msg;
        Provider provider = context.getProvider();

        while (rc.attempt()) {
            try {
                server = service.getServer(id);
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
            doFailure(rc, HttpStatus.BAD_GATEWAY_502, msg);
            throw new RequestFailedException("Lookup Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        return server;
    }


    /**
     * Resume a suspended server and wait for it to enter a running state
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param server The server to be resumed
     */
    @SuppressWarnings("nls")
    protected void resumeServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        logger.debug(Msg.RESUME_SERVER, server.getId());

        Context context = server.getContext();
        String msg;
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                server.resume();
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
            throw new RequestFailedException("Resume Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        waitForStateChange(rc, server, Server.Status.RUNNING);
    }


    protected boolean hasImageAccess(@SuppressWarnings("unused") RequestContext rc, Context context) {
        logger.info("Checking permissions for image service.");
        try {
            ImageService service = context.getImageService();
            service.getImageByName("CHECK_IMAGE_ACCESS");
            logger.info("Image service is accessible.");
            return true;
        } catch (ZoneException e) {
            logger.warn("Image service could not be accessed. Some operations may fail.", e);
            return false;
        }
    }


    /**
     * Enter a pool-wait loop checking the server state to see if it has entered one of the desired states or not. <p>
     * This method checks the state of the server periodically for one of the desired states. When the server enters one
     * of the desired states, the method returns a successful indication (true). If the server never enters one of the
     * desired states within the allocated timeout period, then the method returns a failed response (false). No
     * exceptions are thrown from this method. </p>
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param image The server to wait on
     * @param desiredStates A variable list of desired states, any one of which is allowed.
     * @throws RequestFailedException If the request times out or fails for some reason
     */
    @SuppressWarnings("nls")
    protected void waitForStateChange(RequestContext rc, Image image, Image.Status... desiredStates)
        throws RequestFailedException, NotLoggedInException {
        int pollInterval = configuration.getIntegerProperty(Constants.PROPERTY_OPENSTACK_POLL_INTERVAL);
        int timeout = configuration.getIntegerProperty(Constants.PROPERTY_SERVER_STATE_CHANGE_TIMEOUT);
        Context context = image.getContext();
        Provider provider = context.getProvider();
        ImageService service = context.getImageService();
        String msg;

        long endTime = System.currentTimeMillis() + (timeout * 1000); //

        while (rc.attempt()) {
            try {
                try {
                    image.waitForStateChange(pollInterval, timeout, desiredStates);
                    break;
                } catch (TimeoutException e) {
                    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
                    List<String> list = new ArrayList<>();
                    for (Image.Status desiredState : desiredStates) {
                        list.add(desiredState.name());
                    }
                    msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                        context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                        Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                        Integer.toString(rc.getRetryLimit()));
                    logger.error(msg, e);
                    rc.delay();
                }
            } catch (ZoneException e) {
                List<String> list = new ArrayList<>();
                for (Image.Status desiredState : desiredStates) {
                    list.add(desiredState.name());
                }
                String reason = EELFResourceManager.format(Msg.STATE_CHANGE_EXCEPTION, e.getClass().getSimpleName(),
                    "server", image.getName(), image.getId(), StringHelper.asList(list), image.getStatus().name(),
                    e.getMessage());
                logger.error(reason);
                logger.error(EELFResourceManager.format(e));

                // Instead of failing we are going to wait and try again.
                // Timeout is reduced by delay time
                logger.info(String.format("Retrying in %ds", rc.getRetryDelay()));
                rc.delay();
                timeout = (int) (endTime - System.currentTimeMillis()) / 1000;
                // throw new RequestFailedException(e, operation, reason,
            }
        }

        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Waiting for State Change", msg, HttpStatus.BAD_GATEWAY_502, new Server());
        }
        rc.reset();
    }


    /**
     * Enter a pool-wait loop checking the server state to see if it has entered one of the desired states or not. <p>
     * This method checks the state of the server periodically for one of the desired states. When the server enters one
     * of the desired states, the method returns a successful indication (true). If the server never enters one of the
     * desired states within the allocated timeout period, then the method returns a failed response (false). No
     * exceptions are thrown from this method. </p>
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param server The server to wait on
     * @param desiredStates A variable list of desired states, any one of which is allowed.
     * @throws RequestFailedException If the request times out or fails for some reason
     */
    @SuppressWarnings("nls")
    protected void waitForStateChange(RequestContext rc, Server server, Server.Status... desiredStates)
        throws RequestFailedException {
        int pollInterval = configuration.getIntegerProperty(Constants.PROPERTY_OPENSTACK_POLL_INTERVAL);
        int timeout = configuration.getIntegerProperty(Constants.PROPERTY_SERVER_STATE_CHANGE_TIMEOUT);
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        String msg;

        long endTime = System.currentTimeMillis() + (timeout * 1000); //

        while (rc.attempt()) {
            try {
                try {
                    server.waitForStateChange(pollInterval, timeout, desiredStates);
                    break;
                } catch (TimeoutException e) {
                    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
                    List<String> list = new ArrayList<>();
                    for (Server.Status desiredState : desiredStates) {
                        list.add(desiredState.name());
                    }
                    msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, provider.getName(), service.getURL(),
                        context.getTenant().getName(), context.getTenant().getId(), e.getMessage(),
                        Long.toString(rc.getRetryDelay()), Integer.toString(rc.getAttempts()),
                        Integer.toString(rc.getRetryLimit()));
                    logger.error(msg, e);
                    rc.delay();
                }
            } catch (ZoneException e) {
                List<String> list = new ArrayList<>();
                for (Server.Status desiredState : desiredStates) {
                    list.add(desiredState.name());
                }
                String reason = EELFResourceManager.format(Msg.STATE_CHANGE_EXCEPTION, e.getClass().getSimpleName(),
                    "server", server.getName(), server.getId(), StringHelper.asList(list),
                    server.getStatus().name(), e.getMessage());
                logger.error(reason);
                logger.error(EELFResourceManager.format(e));

                // Instead of failing we are going to wait and try again.
                // Timeout is reduced by delay time
                logger.info(String.format("Retrying in %ds", rc.getRetryDelay()));
                rc.delay();
                timeout = (int) (endTime - System.currentTimeMillis()) / 1000;
                // throw new RequestFailedException(e, operation, reason,
            }
        }

        if (rc.isFailed()) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED, provider.getName(), service.getURL());
            logger.error(msg);
            throw new RequestFailedException("Waiting for State Change", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
    }

    /**
     * Stop the specified server and wait for it to stop
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param server The server to be stopped
     */
    @SuppressWarnings("nls")
    protected void stopServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        logger.debug(Msg.STOP_SERVER, server.getId());

        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                server.stop();
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
            throw new RequestFailedException("Stop Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        waitForStateChange(rc, server, Server.Status.READY, Server.Status.ERROR);
    }

    /**
     * Start the server and wait for it to enter a running state
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param server The server to be started
     */
    @SuppressWarnings("nls")
    protected void startServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        logger.debug(Msg.START_SERVER, server.getId());
        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                server.start();
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
            throw new RequestFailedException("Start Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        waitForStateChange(rc, server, Server.Status.RUNNING);
    }


    /**
     * Un-Pause a paused server and wait for it to enter a running state
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param server The server to be un-paused
     */
    @SuppressWarnings("nls")
    protected void unpauseServer(RequestContext rc, Server server) throws ZoneException, RequestFailedException {
        logger.debug(Msg.UNPAUSE_SERVER, server.getId());

        String msg;
        Context context = server.getContext();
        Provider provider = context.getProvider();
        ComputeService service = context.getComputeService();
        while (rc.attempt()) {
            try {
                server.unpause();
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
            throw new RequestFailedException("Unpause Server", msg, HttpStatus.BAD_GATEWAY_502, server);
        }
        rc.reset();
        waitForStateChange(rc, server, Server.Status.RUNNING, Server.Status.READY);
    }


    /**
     * Generates the event indicating what happened
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param success True if the event represents a successful outcome
     * @param msg The detailed message
     */
    protected void generateEvent(@SuppressWarnings("unused") RequestContext rc,
        @SuppressWarnings("unused") boolean success, @SuppressWarnings("unused") String msg) {
        // indication to the DG to generate the event?
    }

    /**
     * Checks if the VM is connected to the Virtual Network and reachable
     *
     * @param rc The request context that manages the state and recovery of the request for the life of its processing.
     * @param server The server object representing the server we want to operate on
     * @param context The interface cloud service provider to access services or the object model, or both
     */
    protected void checkVirtualMachineNetworkStatus(RequestContext rc, Server server, Context context)
        throws ZoneException, RequestFailedException {

        logger.info("Performing the VM Server networking status checks...");
        List<Port> ports = server.getPorts();

        NetworkService netSvc = context.getNetworkService();

        String msg;
        for (Port port : ports) {
            switch (port.getPortState().toString().toUpperCase()) {
                case "ONLINE":
                    /* The port is connected, configured, and usable for communication */
                    Network network = netSvc.getNetworkById(port.getNetwork());
                    validateNetwork(rc, server, port, network);
                    break;
                case "OFFLINE":
                    /* The port is disconnected or powered-off and cannot be used for communication */
                    msg = createErrorMessage(rc, server, port);
                    throw new RequestFailedException("VM Server Port status is OFFLINE", msg,
                        HttpStatus.PRECONDITION_FAILED_412, server);
                case "PENDING":
                    /* The port's status is changing because of some event or operation. The final state is yet to be determined. */
                    msg = createErrorMessage(rc, server, port);
                    throw new RequestFailedException("VM Server Port status is PENDING", msg,
                        HttpStatus.PRECONDITION_FAILED_412, server);
                case "UNKNOWN":
                    /* The port is in an unknown state and cannot be used. */
                    msg = createErrorMessage(rc, server, port);
                    throw new RequestFailedException("VM Server Port status is UNKNOWN", msg,
                        HttpStatus.PRECONDITION_FAILED_412, server);
                default:
                    logger.error("Invalid port state");
                    break;
            }

        }
        logger.info("Passed the VM Server the Hypervisor status checks..");
    }

    private String createErrorMessage(RequestContext rc, Server server, Port port) {
        String msg;
        msg = EELFResourceManager.format(Msg.SERVER_NETWORK_ERROR, server.getName(), port.getId());
        logger.error(msg);
        doFailure(rc, HttpStatus.PRECONDITION_FAILED_412, msg);
        return msg;
    }

    private void validateNetwork(RequestContext rc, Server server, Port port, Network network)
        throws RequestFailedException {
        String msg;
        if (!network.getStatus().equals(Network.Status.ACTIVE.toString())) {
            msg = createErrorMessage(rc, server, port);
            throw new RequestFailedException("VM Server Network is DOWN", msg,
                HttpStatus.PRECONDITION_FAILED_412, server);
        }
    }

    /**
     * Checks if the VM is connected to the Virtual Network and reachable
     *
     * @param server The server object representing the server we want to operate on
     */
    protected void checkHypervisor(Server server) throws ZoneException, RequestFailedException {

        logger.info("Performing the Hypervisor status checks..");

        String msg;
        if (server.getHypervisor() != null && server.getHypervisor().getStatus() != null
            && server.getHypervisor().getState() != null) {
            String status;
            String state;

            status = server.getHypervisor().getStatus().toString();
            state = server.getHypervisor().getState().toString();

            if (!status.equals(Hypervisor.Status.ENABLED.toString()) || !state.equals(Hypervisor.State.UP.toString())) {
                msg = EELFResourceManager.format(Msg.HYPERVISOR_DOWN_ERROR, server.getHypervisor().getHostName(),
                    server.getName());
                logger.error(msg);
                throw new RequestFailedException("Hypervisor status DOWN or NOT ENABLED", msg,
                    HttpStatus.PRECONDITION_FAILED_412, server);
            }
        } else {
            msg = EELFResourceManager.format(Msg.HYPERVISOR_STATUS_UKNOWN, server.getName());
            logger.error(msg);

            throw new RequestFailedException("Unable to determine Hypervisor status", msg,
                HttpStatus.PRECONDITION_FAILED_412, server);
        }
        logger.info("Passed the Hypervisor status checks..");
    }
}
