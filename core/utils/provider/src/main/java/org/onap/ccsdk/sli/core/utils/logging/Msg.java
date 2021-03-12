/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.utils.logging;

import com.att.eelf.i18n.EELFResolvableErrorEnum;
import com.att.eelf.i18n.EELFResourceManager;

/**
 * The messages issued by APPC components.
 * <p>
 * This message definition is shared by all APPC components.
 * </p>
 *
 */
@SuppressWarnings("nls")
public enum Msg implements EELFResolvableErrorEnum {

    /**
     * ECOMP Application Controller (APP-C) initialization started at {0}
     */
    CONFIGURATION_STARTED,

    /**
     * Prior configuration has been cleared
     */
    CONFIGURATION_CLEARED,

    /**
     * Loading configuration properties from file "{0}"
     */
    LOADING_CONFIGURATION_OVERRIDES,

    /**
     * Configuration defaults loaded from resource file "{0}"
     */
    LOADING_DEFAULTS,

    /**
     * No default property resource "{0}" was found!
     */
    NO_DEFAULTS_FOUND,

    /**
     * Property "{0}" ="{1}"
     */
    PROPERTY_VALUE,

    /**
     * No configuration file named [{0}] was found on the configuration search path [{1}]. \ If a configuration file
     * should have been loaded, check the file name and search path specified. CDP will proceed using the \ default
     * values and command-line overrides (if any).
     */
    NO_OVERRIDE_PROPERTY_FILE_LOADED,

    /**
     * Searching path "{0}" for configuration settings "{1}"
     */
    SEARCHING_CONFIGURATION_OVERRIDES,

    /**
     * Loading application-specific override properties
     */
    LOADING_APPLICATION_OVERRIDES,

    /**
     * No application-specific override properties were provided!
     */
    NO_APPLICATION_OVERRIDES,

    /**
     * Merging system properties into configuration
     */
    MERGING_SYSTEM_PROPERTIES,

    /**
     * Setting property "{0}={1}" in system properties
     */
    SETTING_SPECIAL_PROPERTY,

    /**
     * Loading resource bundle "{0}"
     */
    LOADING_RESOURCE_BUNDLE,

    /**
     * Logging has already been initialized, check the container logging definitions to ensure they represent your
     * desired logging configuration.
     */
    LOGGING_ALREADY_INITIALIZED,

    /**
     * Searching path "{0}" for log configuration file "{1}"
     */
    SEARCHING_LOG_CONFIGURATION,

    /**
     * Loading default logging configuration from system resource file "{0}"
     */
    LOADING_DEFAULT_LOG_CONFIGURATION,

    /**
     * No log configuration could be found or defaulted!
     */
    NO_LOG_CONFIGURATION,

    /**
     * An unsupported logging framework is bound to SLF4J. Only Logback or Log4J are supported.
     */
    UNSUPPORTED_LOGGING_FRAMEWORK,

    /**
     * Loading logging configuration from file "{0}"
     */
    LOADING_LOG_CONFIGURATION,

    /**
     * Provider {0} cannot be found or cannot be resolved to a valid provider.
     */
    UNKNOWN_PROVIDER,

    /**
     * Server name "{0}" with id "{1}" in tenant "{2}" and region "{3}" did not change state within the alloted time.
     * Current state is "{4}" and the desired state(s) are "{5}"
     */
    SERVER_STATE_CHANGE_TIMEOUT,

    /**
     * Server name "{0}" with id "{1}" in tenant "{2}" has a state of deleted and cannot be {3}.
     */
    SERVER_DELETED,

    /**
     * Server name "{0}" with id "{1}" in tenant "{2}" has an unknown state of "{3}".
     */
    UNKNOWN_SERVER_STATE,

    /**
     * {0} component {1} is being initialized...
     */
    COMPONENT_INITIALIZING,

    /**
     * {0} component {1} has completed initialization
     */
    COMPONENT_INITIALIZED,

    /**
     * {0} component {1} is terminating...
     */
    COMPONENT_TERMINATING,

    /**
     * {0} component {1} has terminated
     */
    COMPONENT_TERMINATED,

    /**
     * Operation {0} is not supported or implemented at this time.
     */
    IAAS_ADAPTER_UNSUPPORTED_OPERATION,

    /**
     * Operation {0} called. Input document:\n{1}
     */
    IAAS_ADAPTER_RPC_CALLED,

    /**
     * Unable to locate the {0} service in the OSGi container
     */
    NO_SERVICE_FOUND,

    /**
     * Dump of context parameters for module {0}, RPC {1}, and version {2}
     */
    CONTEXT_PARAMETERS_DISPLAY,

    /**
     * Response properties from execution of module '{0}', RPC '{1}', and version '{2}' are:
     */
    RESPONSE_PARAMETERS_DISPLAY,

    /**
     * Service {0}:{1} was provided a null (empty) or invalid argument, '{2}' = '{3}'
     */
    NULL_OR_INVALID_ARGUMENT,

    /**
     * Service {0}:{1} is processing service '{2}' with request id '{3}'
     */
    PROCESSING_REQUEST,

    /**
     * Service {0}:{1} received request for service '{2}' but that service is invalid or unknown.
     */
    INVALID_SERVICE_REQUEST,

    /**
     * {0} registering service {1} using class {2}
     */
    REGISTERING_SERVICE,

    /**
     * {0} unregistering service {1}
     */
    UNREGISTERING_SERVICE,

    /**
     * {0} IAAS Adapter initializing provider {1} as {2}
     */
    LOADING_PROVIDER_DEFINITIONS,

    /**
     * {0} IAAS Adapter restart of server requested
     */
    RESTARTING_SERVER,

    /**
     * {0} IAAS Adapter rebuild of server requested
     */
    REBUILDING_SERVER,

    /**
     * {0} IAAS Adapter migrate of server requested
     */
    MIGRATING_SERVER,

    /**
     * {0} IAAS Adapter evacuate of server requested
     */
    EVACUATING_SERVER,

    /**
     * {0} IAAS Adapter create snapshot of server requested
     */
    SNAPSHOTING_SERVER,

    /**
     * {0} IAAS Adapter look for server requested
     */
    LOOKING_SERVER_UP,

    /**
     * {0} IAAS Adapter cannot perform requested service, VM url '{1}' is invalid
     */
    INVALID_SELF_LINK_URL,

    /**
     * Located server '{0}' on tenant '{1}' and in state '{2}'
     */
    SERVER_FOUND,

    /**
     * No server found in provider with self-link URL [{0}]
     */
    SERVER_NOT_FOUND,

    /**
     * Exception {0} was caught attempting {1} of server [{2}] on tenant [{3}]
     */
    SERVER_OPERATION_EXCEPTION,

    /**
     * One or more properties for [{0}] are missing, null, or empty. They are:
     */
    MISSING_REQUIRED_PROPERTIES,

    /**
     * The server [{0}] (id={1}) in tenant {2} is in error state, {3} is not allowed
     */
    SERVER_ERROR_STATE,

    /**
     * The image {0} could not be located for {1}
     */
    IMAGE_NOT_FOUND,

    /**
     * Time out waiting for {0} with name {1} (and id {2}) to reach one of {3} states, current state is {4}
     */
    STATE_CHANGE_TIMEOUT,

    /**
     * Exception {0} waiting for {1} with name {2} (and id {3}) to reach one of {4} states, current state is {5}
     * cause={6}
     */
    STATE_CHANGE_EXCEPTION,

    /**
     * Server {0} is being stopped...
     */
    STOP_SERVER,

    /**
     * Server {0} is being started...
     */
    START_SERVER,

    /**
     * Server {0} is being resumed...
     */
    RESUME_SERVER,

    /**
     * Server {0} is being unpaused...
     */
    UNPAUSE_SERVER,

    /**
     * Server {0} is being rebuilt...
     */
    REBUILD_SERVER,

    /**
     * Connection to provider {0} at identity {1} using tenant name {2} (id {3}) failed, reason={4}, retrying in {5}
     * seconds, attempt {6} of {7}.
     */
    CONNECTION_FAILED_RETRY,

    /**
     * Connection to provider {0} at service {1} failed after all retry attempts.
     */
    CONNECTION_FAILED,

    /**
     * {0} IAAS Adapter stop server requested
     */
    STOPPING_SERVER,

    /**
     * {0} IAAS Adapter start server requested
     */
    STARTING_SERVER,

    /**
     * Server {0} (id {1}) failed to rebuild, reason {2}
     */
    REBUILD_SERVER_FAILED,

    /**
     * Application {0} graph {1} response did not set the {2} parameter. This parameter is required for synchronization
     * with the controller. Absence of this parameter is assumed to be a failure. Please correct the DG.
     */
    PARAMETER_IS_MISSING,

    /**
     * Application {0} graph {1} did not set parameter {2} to a valid numeric value ({3}). Please correct the DG.
     */
    PARAMETER_NOT_NUMERIC,

    /**
     * Application {0} graph {1} completed with failure: error code = {2}, message = {3}
     */
    DG_FAILED_RESPONSE,

    /**
     * Application {0} received exception {1} attempting to call graph {2}, exception message = {3}
     */
    EXCEPTION_CALLING_DG,

    /**
     * Application {0} was unable to locate graph {1}
     */
    GRAPH_NOT_FOUND,

    /**
     * Application {0} graph {1} responded with {3} properties
     */
    DEBUG_GRAPH_RESPONSE_HEADER,

    /**
     * {0}:{1} - {2} = {3}
     */
    DEBUG_GRAPH_RESPONSE_DETAIL,

    /**
     * Application {0} request {1} was supplied a property '{2}' with the value '{3}' that does not meet the required
     * form(s):
     */
    INVALID_REQUIRED_PROPERTY,

    /**
     * Server {0} (id {1}) failed to migrate during {2} phase, reason {3}
     */
    MIGRATE_SERVER_FAILED,

    /**
     * Server {0} (id {1}) failed to evacuate, reason {2}
     */
    EVACUATE_SERVER_FAILED,

    /**
     * Server {0} evacuate from host {1} to host {2} failed during the rebuild on host {2}, reason {3}
     */
    EVACUATE_SERVER_REBUILD_FAILED,

    /**
     * APP-C instance is too busy
     */
    APPC_TOO_BUSY,

    /**
     * Concurrent access to server "{0}"
     */
    VF_SERVER_BUSY,

    /**
     * Server "{0}" does not support command "{1}" in the current state "{2}"
     */
    VF_ILLEGAL_COMMAND,

    /**
     * Server "{0}" cannot handle command "{1}" because of its doubtful state
     */
    VF_UNDEFINED_STATE,

    /**
     * No resource found with ID "{0}" in A&AI system
     */
    APPC_NO_RESOURCE_FOUND,

    /**
     * The request "{0}" for server "{1}" has exceeded its TTL limit of "{3}" seconds
     */
    APPC_EXPIRED_REQUEST,

    /**
     * Workflow for vnfType = "{0}" and command = "{1}" not found.
     */
    APPC_WORKFLOW_NOT_FOUND,

    /**
     * Null vnfId and command provided
     */
    APPC_INVALID_INPUT,

    /**
     * Operation '{0}' for VNF type '{1}' from Source '{2}' with RequestID '{3}' was started at '{4}' and ended at '{5}'
     * with status code '{6}'
     */
    APPC_AUDIT_MSG,

    /**
     * APP-C is unable to communicate with A&AI
     */
    AAI_CONNECTION_FAILED,

    /**
     * APP-C is unable to update COMPONENT_ID {0} to {1} for reason {2}
     */
    AAI_UPDATE_FAILED,

    /**
     * APP-C is unable to retrieve VF/VFC {0} data for Transaction ID{1}as a result of A&AI communication failure or its
     * internal error.
     */
    AAI_GET_DATA_FAILED,

    /**
     * A&AI at identity {0} using VNF_ID {1} failed, reason={2}, retrying in {3} seconds, attempt {4} of {5}
     */
    AAI_CONNECTION_FAILED_RETRY,

    /**
     * APP-C is unable to delete COMPONENT_ID {0} for reason {1}
     */
    AAI_DELETE_FAILED,

    /**
     * APP-C is unable to query AAI for VNF_ID {0}
     */
    AAI_QUERY_FAILED,

    /**
     * VNF {0} is configured
     */
    VNF_CONFIGURED,

    /**
     * VNF {0} is being configured
     */
    VNF_CONFIGURATION_STARTED,

    /**
     * VNF {0} configuration failed for reason {1}
     */
    VNF_CONFIGURATION_FAILED,

    /**
     * VNF {0} is being tested
     */
    VNF_TEST_STARTED,

    /**
     * VNF {0} was tested
     */
    VNF_TESTED,

    /**
     * VNF {0} test failed for reason {1}
     */
    VNF_TEST_FAILED,

    /**
     * VNF {0} test failed for reason {1}
     */
    VNF_NOT_FOUND,

    /**
     * VNF {0} Healthcheck operation failed for reason {1}
     */
    VNF_HEALTHCECK_FAILED,

    /**
     * VM {0} Healthcheck operation failed for reason {1}
     */
    VM_HEALTHCECK_FAILED,

    /**
     * Server {0} (id {1}) failed to stop during {2} phase, reason {3}
     */
    STOP_SERVER_FAILED,

    /**
     * Server {0} (id {1}) failed to terminate during {2} phase, reason {3}
     */
    TERMINATE_SERVER_FAILED,

    /**
     * {0} IAAS Adapter terminate server requested
     */
    TERMINATING_SERVER,

    /**
     * Server {0} is being terminated...
     */
    TERMINATE_SERVER,

    /**
     * Migrate {0} finished with status {1}. Start Time: {2}. End Time: {3}. Request ID: {4}. Reason:{5}...
     */
    MIGRATE_COMPLETE,

    /**
     * Restart {0} finished with status {1}. Start Time: {2}. End Time: {3}. Request ID: {4}. Reason:{5}...
     */
    RESTART_COMPLETE,

    /**
     * Rebuild {0} finished with status {1}. Start Time: {2}. End Time: {3}. Request ID: {4}. Reason:{5}...
     */
    REBUILD_COMPLETE,

    /**
     * Located stack '{0}' on tenant '{1}' and in state '{2}'
     */
    STACK_FOUND,

    /**
     * {0} IAAS Adapter terminate stack requested
     */

    TERMINATING_STACK,

    /**
     * stack {0} is being terminated...
     */
    TERMINATE_STACK,
    /**
     * No stack found in provider with self-link URL [{0}]
     */

    STACK_NOT_FOUND,

    /**
     * Exception {0} was caught attempting {1} of stack [{2}] on tenant [{3}]
     */
    STACK_OPERATION_EXCEPTION,

    /**
     * Stack {0} (id {1}) failed to terminate during {2} phase, reason {3}
     */

    TERMINATE_STACK_FAILED,

    /**
     * Exception {0} was caught attempting to close provider context for {1}.
     */

    CLOSE_CONTEXT_FAILED,

    /**
     * {0} IAAS Adapter snapshoting stack
     */
    SNAPSHOTING_STACK,

    /**
     * Stack {0} snapshoted, snapshot ID = [{1}].
     */
    STACK_SNAPSHOTED,

    /**
     * {0} IAAS Adapter restoring stack
     */
    RESTORING_STACK,

    /**
     * Stack {0} is restored to snapshot {1}.
     */
    STACK_RESTORED,

    /**
     * {0} IAAS Adapter checking server
     */
    CHECKING_SERVER,

    /**
     * Parameter {0} is missing in svc request of {1}.
     */
    MISSING_PARAMETER_IN_REQUEST,

    /**
     * Cannot establish connection to server {0} port {1} with user {2}.
     */
    CANNOT_ESTABLISH_CONNECTION,

    /**
     * Operation '{0}' for VNF type '{1}' from Source '{2}' with RequestID '{3}' on '{4}' with action '{5}'
     * ended in {6}ms with result '{7}'
     */
    APPC_METRIC_MSG,

    /**
     * Parsing failied for{0}
     */
    INPUT_PAYLOAD_PARSING_FAILED,

    /**
     * Error occurred for due to {0}
     */
    APPC_EXCEPTION,

    /**
     * SSH Data Exception occurred due to {0}
     */
    SSH_DATA_EXCEPTION,

    /**
     * Json processing exception occurred due to {0}
     */
    JSON_PROCESSING_EXCEPTION,

   /**
     * Operation {0} succeed for {1}
     */
    SUCCESS_EVENT_MESSAGE,

    /**
     * Dependency model not found for VNF type {0} due to {1}
     */
    DEPENDENCY_MODEL_NOT_FOUND,

    /**
     * Invalid Dependency model for VNF Type {0} due to {1}
     */
    INVALID_DEPENDENCY_MODEL,

    /**
     * Failed to retrieve VNFC DG
     */
    FAILURE_RETRIEVE_VNFC_DG,

    /**
     * Network check for Server {0} failed for Port {1}
     *
     */
    SERVER_NETWORK_ERROR,

    /**
     * Hypervisor check for Server {0} failed. Status is DOWN or UNKNOWN
     *
     */
    HYPERVISOR_DOWN_ERROR,

    /**
     * Unable to determine Hypervisor status for Server {0}. failed.
     *
     */
    HYPERVISOR_STATUS_UKNOWN,

    /**
     * Hypervisor Network check for Server {0} failed. Not reachable by APPC
     *
     */
    HYPERVISOR_NETWORK_ERROR,

    /**
     * Restart application operation failed on server : {0}, reason {1}
     */
    APPLICATION_RESTART_FAILED,

    /**
     * Start application operation failed on server : {0}, reason {1}
     */
    APPLICATION_START_FAILED,

    /**
     * Start application operation failed on server : {0}, reason {1}
     */
    APPLICATION_STOP_FAILED,

    /**
     * Application on server {0} is being restarted...
     */
    RESTART_APPLICATION,

    /**
     * Application on server {0} is being started...
     */
    START_APPLICATION,

    /**
     * Application on server {0} is being started...
     */
    STOP_APPLICATION,

    /**
     * APPC LCM operations are disabled
     */
    LCM_OPERATIONS_DISABLED,

    /**
     * Application {0} received exception {1} while attempting to execute oam operation {2}, exception message = {3}|\
     */
    OAM_OPERATION_EXCEPTION,

    /**
     *   Application {0} is {1}
     */
    OAM_OPERATION_ENTERING_MAINTENANCE_MODE,

    /**
     * Application {0} is in {1}
     */
    OAM_OPERATION_MAINTENANCE_MODE,

    /**
     * Application {0} is {1}
     */
    OAM_OPERATION_STARTING,

    /**
     * Application {0} is {1}
     */
    OAM_OPERATION_STARTED,

    /**
     * Application {0} is {1}
     */
    OAM_OPERATION_STOPPING,

    /**
     * Application {0} is {1}
     */
    OAM_OPERATION_STOPPED,
    /**
     * A {1} API is not allowed when {0} is in the {2} state
     */
    INVALID_STATE_TRANSITION,

    /**
     * Application {0} was unable to find the Request Handler service
     */
    REQUEST_HANDLER_UNAVAILABLE,

    /**
     * Application {0} is {1}
     */
    OAM_OPERATION_RESTARTING,

    /**
     * Application {0} is {1} for restart
     */
    OAM_OPERATION_RESTARTED,

    /**
     * {0}
     */
    OAM_OPERATION_INVALID_INPUT,

    ATTACHINGVOLUME_SERVER,

    DETTACHINGVOLUME_SERVER,

    REBOOT_SERVER,
    /**
     * {0} IAAS Adapter reboot  of server requested
     */
    /**
     * Unsupported identity service version, unable to retrieve ServiceCatalog
     * for identity service {0}
     */
    IAAS_UNSUPPORTED_IDENTITY_SERVICE,

    /**
     * Sftp data transfer failed on connection to host {0} with user {1} for {2} operation, reason : {3}
     */
    SFTP_TRANSFER_FAILED,
    /**
     * Ssh session with host {0} has timed out during command {1} execution
     */
    SSH_CONNECTION_TIMEOUT,
     /**
     * Exception {0} was caught while  posting  action {1}  to service  {2}
     */
    POST_OPERATION_EXCEPTION,
    /**
     *Connecting to image service {0}failed
     */
    IMAGE_SERVICE_FAILED,
    /**
     * Time out waiting for volume {0} with device {1} (and server {2}) after retry one of {3} times, retry interval is {4}
     */
    VOLUME_ATTACH_TIMEOUT,
    /**
     * Time out waiting for volume {0} (from server {1}) after retry one of {2} times, retry interval is {3}
     */
    VOLUME_DETACH_TIMEOUT,
    /**
     * MSO throwing error from openstack{0}
     */
    MSO_EXCEPTION,
 /**
     * Server [{0}], ID [{1}], is in state [{2}] but is expected to be in one of [{3}]
     */
    OS_INVALID_SERVER_STATE,
    /**
     * Could not configure existing ssh session, reason: {0}
     */
    SSH_SESSION_CONFIG_ERROR
    ;
    /*
     * Static initializer to ensure the resource bundles for this class are loaded...
     */
    static {
        EELFResourceManager.loadMessageBundle("org/onap/appc/i18n/MessageResources");
    }
}
