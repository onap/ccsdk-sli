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

package org.onap.ccsdk.sli.adaptors.rest;

import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

/**
 * This interface defines the operations that the provider adaptor exposes.
 * <p>
 * This interface defines static constant property values that can be used to configure the adaptor. These constants are
 * prefixed with the name PROPERTY_ to indicate that they are configuration properties. These properties are read from
 * the configuration file for the adaptor and are used to define the providers, identity service URLs, and other
 * information needed by the adaptor to interface with an IaaS provider.
 * </p>
 */
public interface RestAdaptor extends SvcLogicJavaPlugin {

    /**
     * The type of provider to be accessed to locate and operate on a virtual machine instance. This is used to load the
     * correct provider support through the CDP IaaS abstraction layer and can be OpenStackProvider, BareMetalProvider,
     * or any other supported provider type.
     */
    static final String PROPERTY_PROVIDER_TYPE = "org.onap.appc.provider.type";

    /**
     * The adaptor maintains a cache of providers organized by the name of the provider, not its type. This is
     * equivalent to the system or installation name. All regions within the same installation are assumed to be the
     * same type.
     */
    static final String PROPERTY_PROVIDER_NAME = "org.onap.appc.provider.name";

    /**
     * The fully-qualified URL of the instance to be manipulated as it is known to the provider.
     */
    static final String PROPERTY_INSTANCE_URL = "org.onap.appc.instance.url";

    /**
     * The fully-qualified URL of the instance to be manipulated as it is known to the provider.
     */
    static final String PROPERTY_IDENTITY_URL = "org.onap.appc.identity.url";

    /**
     * This method is used to restart an existing virtual machine given the fully qualified URL of the machine.
     * <p>
     * This method is invoked from a directed graph as an <code>Executor</code> node. This means that the parameters
     * passed to the method are passed as properties in a map. This method expects the following properties to be
     * defined:
     * <dl>
     * <dt>org.onap.appc.provider.type</dt>
     * <dd>The appropriate provider type, such as <code>OpenStackProvider</code>. This is used by the CDP IaaS
     * abstraction layer to dynamically load and open a connection to the appropriate provider type. All CDP supported
     * provider types are legal.</dd>
     * <dt>org.onap.appc.instance.url</dt>
     * <dd>The fully qualified URL of the instance to be restarted, as it is known to the provider (i.e., the self-link
     * URL of the server)</dd>
     * </dl>
     * </p>
     *
     * @param properties
     *            A map of name-value pairs that supply the parameters needed by this method. The properties needed are
     *            defined above.
     * @param context
     *            The service logic context of the graph being executed.
     * @return The <code>Server</code> object that represents the VM being restarted. The returned server object can be
     *         inspected for the final state of the server once the restart has been completed. The method does not
     *         return until the restart has either completed or has failed.
     * @throws SvcLogicException
     *             If the server cannot be restarted for some reason
     */
    //  Server restartServer(Map<String, String> properties, SvcLogicContext context) throws SvcLogicException;

    /**
     * This method is used to stop the indicated server
     * <p>
     * This method is invoked from a directed graph as an <code>Executor</code> node. This means that the parameters
     * passed to the method are passed as properties in a map. This method expects the following properties to be
     * defined:
     * <dl>
     * <dt>org.onap.appc.provider.type</dt>
     * <dd>The appropriate provider type, such as <code>OpenStackProvider</code>. This is used by the CDP IaaS
     * abstraction layer to dynamically load and open a connection to the appropriate provider type. All CDP supported
     * provider types are legal.</dd>
     * <dt>org.onap.appc.instance.url</dt>
     * <dd>The fully qualified URL of the instance to be stopped, as it is known to the provider (i.e., the self-link
     * URL of the server)</dd>
     * </dl>
     * </p>
     *
     * @param properties
     *            A map of name-value pairs that supply the parameters needed by this method. The properties needed are
     *            defined above.
     * @param context
     *            The service logic context of the graph being executed.
     * @return The <code>Server</code> object that represents the VM being stopped. The returned server object can be
     *         inspected for the final state of the server once the stop has been completed. The method does not return
     *         until the stop has either completed or has failed.
     * @throws SvcLogicException
     *             If the server cannot be stopped for some reason
     */
    //Server stopServer(Map<String, String> properties, SvcLogicContext context) throws SvcLogicException;

    /**
     * This method is used to start the indicated server
     * <p>
     * This method is invoked from a directed graph as an <code>Executor</code> node. This means that the parameters
     * passed to the method are passed as properties in a map. This method expects the following properties to be
     * defined:
     * <dl>
     * <dt>org.onap.appc.provider.type</dt>
     * <dd>The appropriate provider type, such as <code>OpenStackProvider</code>. This is used by the CDP IaaS
     * abstraction layer to dynamically load and open a connection to the appropriate provider type. All CDP supported
     * provider types are legal.</dd>
     * <dt>org.onap.appc.instance.url</dt>
     * <dd>The fully qualified URL of the instance to be started, as it is known to the provider (i.e., the self-link
     * URL of the server)</dd>
     * </dl>
     * </p>
     *
     * @param properties
     *            A map of name-value pairs that supply the parameters needed by this method. The properties needed are
     *            defined above.
     * @param context
     *            The service logic context of the graph being executed.
     * @return The <code>Server</code> object that represents the VM being started. The returned server object can be
     *         inspected for the final state of the server once the start has been completed. The method does not return
     *         until the start has either completed or has failed.
     * @throws SvcLogicException
     *             If the server cannot be started for some reason
     */
    // Server startServer(Map<String, String> properties, SvcLogicContext context) throws SvcLogicException;

    /**
     * This method is used to rebuild the indicated server
     * <p>
     * This method is invoked from a directed graph as an <code>Executor</code> node. This means that the parameters
     * passed to the method are passed as properties in a map. This method expects the following properties to be
     * defined:
     * <dl>
     * <dt>org.onap.appc.provider.type</dt>
     * <dd>The appropriate provider type, such as <code>OpenStackProvider</code>. This is used by the CDP IaaS
     * abstraction layer to dynamically load and open a connection to the appropriate provider type. All CDP supported
     * provider types are legal.</dd>
     * <dt>org.onap.appc.instance.url</dt>
     * <dd>The fully qualified URL of the instance to be rebuilt, as it is known to the provider (i.e., the self-link
     * URL of the server)</dd>
     * </dl>
     * </p>
     *
     * @param properties
     *            A map of name-value pairs that supply the parameters needed by this method. The properties needed are
     *            defined above.
     * @param context
     *            The service logic context of the graph being executed.
     * @return The <code>Server</code> object that represents the VM being rebuilt. The returned server object can be
     *         inspected for the final state of the server once the rebuild has been completed. The method does not
     *         return until the rebuild has either completed or has failed.
     * @throws SvcLogicException
     *             If the server cannot be rebuilt for some reason
     */
    //   Server rebuildServer(Map<String, String> properties, SvcLogicContext context) throws SvcLogicException;

    /**
     * Returns the symbolic name of the adaptor
     *
     * @return The adaptor name
     */
    String getAdaptorName();

    // Server evacuateServer(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    //Server migrateServer(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    void commonGet(Map<String, String> params, SvcLogicContext ctx);

    void commonPost(Map<String, String> params, SvcLogicContext ctx);

    void commonPut(Map<String, String> params, SvcLogicContext ctx);

    void commonDelete(Map<String, String> params, SvcLogicContext ctx);

}
