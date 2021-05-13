/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modification Copyright (C) 2017 IBM
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

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Stack;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * This class is used to capture the exact cause and point of failure for the processing of a request. It is then used
 * to encode the reason for the failure, status code, and anything else that needs to be captured and reported for
 * diagnostic purposes.
 */
public class RequestFailedException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The operation that was being requested or performed at the time of the failure.
     */
    private String operation;

    /**
     * A message that details the reason for the failure
     */
    private String reason;

    /**
     * The server that was being operated upon
     */
    private Server server;

    /**
     * The stack that was being operated upon
     */
    private Stack stack;
    /**
     * The id of the server being operated upon if the server object is not available (such as the server was not found)
     */
    private String serverId;

    /**
     * The id of the stack being operated upon if the stack object is not available (such as the stack was not found)
     */
    private String stackId;
    /**
     * The most appropriate Http Status code that reflects the error
     */
    private transient HttpStatus status;

    /**
     *
     */
    public RequestFailedException() {
        // intentionally empty
    }

    /**
     * @param message The error message
     */
    public RequestFailedException(String message) {
        super(message);
    }

    /**
     * Construct the request failed exception with the operation being performed, reason for the failure, http status
     * code that is most appropriate, and the server we were processing.
     *
     * @param operation The operation being performed
     * @param reason The reason that the operation was failed
     * @param status The http status code that is most appropriate
     * @param server The server that we were processing
     */
    @SuppressWarnings("nls")
    public RequestFailedException(String operation, String reason, HttpStatus status, Server server) {
        super(operation + ":" + reason);
        this.operation = operation;
        this.reason = reason;
        this.status = status;
        this.server = server;
        if (server != null) {
            this.serverId = server.getId();
        }
    }


    /**
     * Construct the request failed exception with the operation being performed, reason for the failure, http status
     * code that is most appropriate, and the stack we were processing.
     *
     * @param operation The operation being performed
     * @param reason The reason that the operation was failed
     * @param status The http status code that is most appropriate
     * @param stack The stack that we were processing
     */
    @SuppressWarnings("nls")
    public RequestFailedException(String operation, String reason, HttpStatus status, Stack stack) {
        super(operation + ":" + reason);
        this.operation = operation;
        this.reason = reason;
        this.status = status;
        this.stack = stack;
        if (stack != null) {
            this.stackId = stack.getId();
        }
    }

    /**
     * Construct the request failed exception with the operation being performed, reason for the failure, http status
     * code that is most appropriate, and the server we were processing.
     *
     * @param ex The exception that we are wrapping
     * @param operation The operation being performed
     * @param reason The reason that the operation was failed
     * @param status The http status code that is most appropriate
     * @param server The server that we were processing
     */
    @SuppressWarnings("nls")
    public RequestFailedException(Throwable ex, String operation, String reason, HttpStatus status, Server server) {
        super(operation + ":" + reason, ex);
        this.operation = operation;
        this.reason = reason;
        this.status = status;
        this.server = server;
        if (server != null) {
            this.serverId = server.getId();
        }
    }

    /**
     * @param message The error message
     * @param cause A nested exception
     */
    public RequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message The error message
     * @param cause A nested exception
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public RequestFailedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *        permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public RequestFailedException(Throwable cause) {
        super(cause);
    }

    /**
     * @return The operation being performed
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @return The reason for the failure
     */
    public String getReason() {
        return reason;
    }

    /**
     * @return The server being operated upon
     */
    public Server getServer() {
        return server;
    }

    /**
     * @return The id of the server being operated upon
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * @return The status code from the operation
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * @param operation The operation being performed
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * @param reason The reason for the failure
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @param server The server being operated upon
     */
    public void setServer(Server server) {
        this.server = server;
        if (server != null) {
            setServerId(server.getId());
        }
    }

    /**
     * @param serverId The id of the server being operated upon
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * @param status The status of the request
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

}
