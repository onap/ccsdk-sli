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

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.spi.AbstractService.State;
import com.att.cdp.zones.spi.RequestState;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used to capture and cache the service catalog for a specific OpenStack provider.
 * <p>
 * This is needed because the way the servers are represented in the ECOMP product is as their fully qualified URL's.
 * This is very problematic, because we cant identify their region from the URL, URL's change, and we cant identify the
 * versions of the service implementations. In otherwords, the URL does not provide us enough information.
 * </p>
 * <p>
 * The zone abstraction layer is designed to detect the versions of the services dynamically, and step up or down to
 * match those reported versions. In order to do that, we need to know before hand what region we are accessing (since
 * the supported versions may be different by regions). We will need to authenticate to the identity service in order to
 * do this, plus we have to duplicate the code supporting proxies and trusted hosts that exists in the abstraction
 * layer, but that cant be helped.
 * </p>
 * <p>
 * What we do to circumvent this is connect to the provider using the lowest supported identity api, and read the entire
 * service catalog into this object. Then, we parse the vm URL to extract the host and port and match that to the
 * compute services defined in the catalog. When we find a compute service that has the same host name and port,
 * whatever region that service is supporting is the region for that server.
 * </p>
 * <p>
 * While we really only need to do this for compute nodes, there is no telling what other situations may arise where the
 * full service catalog may be needed. Also, there is very little additional cost (additional RAM) associated with
 * caching the full service catalog since there is no way to list only a portion of it.
 * </p>
 */
public abstract class ServiceCatalog {
    /**
     * The openstack connector version to use
     */
    public static final String CLIENT_CONNECTOR_CLASS = "com.woorea.openstack.connector.JaxRs20Connector";

    /**
     * The service name for the compute service endpoint
     */
    public static final String COMPUTE_SERVICE = "compute"; //$NON-NLS-1$

    /**
     * The default domain for authentication
     */
    public static final String DEFAULT_DOMAIN = "Default";
    /**
     * The service name for the identity service endpoint
     */
    public static final String IDENTITY_SERVICE = "identity"; //$NON-NLS-1$

    /**
     * The service name for the compute service endpoint
     */
    public static final String IMAGE_SERVICE = "image"; //$NON-NLS-1$

    /**
     * The service name for the metering service endpoint
     */
    public static final String METERING_SERVICE = "metering"; //$NON-NLS-1$

    /**
     * The service name for the network service endpoint
     */
    public static final String NETWORK_SERVICE = "network"; //$NON-NLS-1$

    /**
     * The service name for the persistent object service endpoint
     */
    public static final String OBJECT_SERVICE = "object-store"; //$NON-NLS-1$

    /**
     * The service name for the orchestration service endpoint
     */
    public static final String ORCHESTRATION_SERVICE = "orchestration"; //$NON-NLS-1$

    /**
     * The service name for the volume service endpoint
     */
    public static final String VOLUME_SERVICE = "volume"; //$NON-NLS-1$

    /**
     * The logger to be used
     */
    protected static final EELFLogger logger = EELFManager.getInstance().getLogger(ServiceCatalog.class);

    /**
     * The password for authentication
     */
    protected String credential;

    /**
     * The domain for authentication
     */
    protected String domain;
    /**
     * The time (local) that the token expires and we need to re-authenticate
     */
    protected long expiresLocal;

    /**
     * The url of the identity service
     */
    protected String identityURL;

    /**
     * The user id for authentication
     */
    protected String principal;

    /**
     * The project or tenant identifier
     */
    protected String projectIdentifier;

    /**
     * Properties for proxy information
     */
    protected Properties properties;

    /**
     * The set of all regions that have been defined
     */
    protected Set<String> regions;

    /**
     * The read/write lock used to protect the cache contents
     */
    protected ReadWriteLock rwLock;

    /**
     * Create the ServiceCatalog cache
     *
     * @param identityURL The identity service URL to connect to
     * @param projectIdentifier The name or id of the tenant to authenticate with. If the ID is a UUID format
     *        (32-character hexadecimal string), then the authentication is done using the tenant ID, otherwise it is
     *        done using the name.
     * @param principal The user id to authenticate to the provider
     * @param credential The password to authenticate to the provider
     * @param properties Additional properties used to configure the connection, such as proxy and trusted hosts lists
     * @throws ZoneException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public ServiceCatalog(String identityURL, String projectIdentifier, String principal, String credential,
            String domain, Properties properties) {
        this.identityURL = identityURL;
        this.projectIdentifier = projectIdentifier;
        this.principal = principal;
        this.credential = credential;
        this.domain = domain;
        this.properties = properties;
        rwLock = new ReentrantReadWriteLock();
        regions = new HashSet<>();
    }

    /**
     * Returns the list of service endpoints for the published service type
     *
     * @param serviceType The service type to obtain the endpoints for
     * @return The list of endpoints for the service type, or null if none exist
     */
    public abstract List<?> getEndpoints(String serviceType);

    /**
     * @return The project or tenant id
     */
    public abstract String getProjectId();

    /**
     * @return The project or tenant name
     */
    public abstract String getProjectName();

    /**
     * @return The set of all regions that are defined
     */
    public Set<String> getRegions() {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return regions;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @return A list of service types that are published
     */
    public abstract List<String> getServiceTypes();

    /**
     * This method accepts a fully qualified compute node URL and uses that to determine which region of the provider
     * hosts that compute node.
     *
     * @param url The parsed URL of the compute node
     * @return The region name, or null if no region of this tenant hosts that compute node.
     */
    public abstract String getVMRegion(VMURL url);

    /**
     * Returns an indication if the specified service type is published by this provider
     *
     * @param serviceType The service type to check for
     * @return True if a service of that type is published
     */
    public abstract boolean isServicePublished(String serviceType);

    /**
     * Load the Service Catalog from the specified provider
     *
     * @throws ZoneException
     */
    public abstract void init() throws ZoneException;

    /**
     * This method is used to provide a diagnostic listing of the service catalog
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public abstract String toString();

    /**
     * Initializes the request state for the current requested service.
     * <p>
     * This method is used to track requests made to the various service implementations and to provide additional
     * information for diagnostic purposes. The <code>RequestState</code> class stores the state in thread-local storage
     * and is available to all code on that thread.
     * </p>
     * <p>
     * This method first obtains the stack trace and scans the stack backward for the call to this method. It then backs
     * up one more call and assumes that method is the request that we are "tracking".
     * </p>
     *
     * @param states A variable argument list of additional state values that the caller wants to add to the request
     *        state thread-local object to track the context.
     */
    protected void trackRequest(State... states) {
        RequestState.clear();

        for (State state : states) {
            RequestState.put(state.getName(), state.getValue());
        }

        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stack = currentThread.getStackTrace();
        if (stack != null && stack.length > 0) {
            int index = 0;
            StackTraceElement element = null;
            for (; index < stack.length; index++) {
                element = stack[index];
                if ("trackRequest".equals(element.getMethodName())) { //$NON-NLS-1$
                    break;
                }
            }
            index++;

            if (index < stack.length) {
                element = stack[index];
                RequestState.put(RequestState.METHOD, element.getMethodName());
                RequestState.put(RequestState.CLASS, element.getClassName());
                RequestState.put(RequestState.LINE_NUMBER, Integer.toString(element.getLineNumber()));
                RequestState.put(RequestState.THREAD, currentThread.getName());
                // RequestState.put(RequestState.PROVIDER, context.getProvider().getName());
                // RequestState.put(RequestState.TENANT, context.getTenantName());
                // RequestState.put(RequestState.PRINCIPAL, context.getPrincipal());
            }
        }
    }
}
