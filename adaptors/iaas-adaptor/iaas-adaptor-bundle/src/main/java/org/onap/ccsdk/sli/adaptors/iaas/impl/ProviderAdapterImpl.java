/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 IBM.
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

import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Stack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.adaptors.iaas.Property;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.api.IProviderOperation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.api.ProviderOperationFactory;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.EvacuateServer;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.StructuredPropertyHelper;
import org.onap.ccsdk.sli.core.utils.StructuredPropertyHelper.Node;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

/**
 * This class implements the {@link ProviderAdapter} interface. This interface defines the behaviors that our service
 * provides.
 *
 * @since Aug 12, 2015
 * @version $Id$
 */
@SuppressWarnings("javadoc")
public class ProviderAdapterImpl implements ProviderAdapter {
    /**
     * The default domain name for authentication
     */
    public static final String DEFAULT_DOMAIN_NAME = "Default";
    /**
     * A reference to the adapter configuration object.
     */
    private Configuration configuration;
    /**
     * reference to operation factory
     */
    private ProviderOperationFactory factory;
    /**
     * A cache of providers that are predefined.
     */
    private Map<String /* provider name */, ProviderCache> providerCache;
    /**
     * The username, password, and domain to use for dynamically created connections
     */
    private static String defaultUser;
    private static String defaultPass;
    private static String defaultDomain;

    /**
     * This default constructor is used as a work around because the activator wasnt getting called
     */
    @SuppressWarnings("all")
    public ProviderAdapterImpl() {
        initialize();

    }
    /**
     * This constructor is used primarily in the test cases to bypass initialization of the adapter for isolated,
     * disconnected testing
     *
     * @param initialize True if the adapter is to be initialized, can false if not
     */
    @SuppressWarnings("all")
    public ProviderAdapterImpl(boolean initialize) {
        configuration = ConfigurationFactory.getConfiguration();
        if (initialize) {
            initialize();
        }
    }
    /**
     * @param props not used
     */
    public ProviderAdapterImpl(@SuppressWarnings("unused") Properties props) {
        initialize();

    }
    @Override
    public Server restartServer(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.RESTART_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Server) op.doOperation(params, context);
    }
    @Override
    public Server stopServer(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.STOP_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Server) op.doOperation(params, context);
    }
    @Override
    public Server startServer(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.START_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Server) op.doOperation(params, context);
    }
    @Override
    public Server rebuildServer(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.REBUILD_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Server) op.doOperation(params, context);
    }
    @Override
    public Server terminateServer(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.TERMINATE_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Server) op.doOperation(params, context);
    }
    @Override
    public Server evacuateServer(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.EVACUATE_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        // pass this object's reference to EvacuateServer to allow rebuild after evacuate
        ((EvacuateServer) op).setProvideAdapterRef(this);
        return (Server) op.doOperation(params, context);
    }
    @Override
    public Server migrateServer(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.MIGRATE_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Server) op.doOperation(params, context);
    }
    @Override
    public Server vmStatuschecker(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.VMSTATUSCHECK_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Server) op.doOperation(params, context);
    }
    @Override
    public Stack terminateStack(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.TERMINATE_STACK);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Stack) op.doOperation(params, context);
    }
    @Override
    public Stack snapshotStack(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.SNAPSHOT_STACK);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Stack) op.doOperation(params, context);
    }
    @Override
    public Stack restoreStack(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.RESTORE_STACK);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Stack) op.doOperation(params, context);
    }
    @Override
    public Server lookupServer(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.LOOKUP_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Server) op.doOperation(params, context);
    }
    @Override
    public Image createSnapshot(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.SNAPSHOT_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Image) op.doOperation(params, context);
    }
    /**
     * Returns the symbolic name of the adapter
     *
     * @return The adapter name
     * @see ProviderAdapter#getAdapterName()
     */
    @Override
    public String getAdapterName() {
        return configuration.getProperty(Constants.PROPERTY_ADAPTER_NAME);
    }
    /**
     * initialize the provider adapter by building the context cache
     */
    private void initialize() {
        configuration = ConfigurationFactory.getConfiguration();
        /*
         * Initialize the provider cache for all defined providers. The definition of the providers uses a structured
         * property set, where the names form a hierarchical name space (dotted notation, such as one.two.three). Each
         * name in the name space can also be serialized by appending a sequence number. All nodes at the same level
         * with the same serial number are grouped together in the namespace hierarchy. This allows a hierarchical
         * multi-valued property to be defined, which can then be used to setup the provider and tenant caches. <p> For
         * example, the following definitions show how the namespace hierarchy is defined for two providers, with two
         * tenants on the first provider and a single tenant for the second provider. <pre>
         * provider1.type=OpenStackProvider provider1.name=ILAB provider1.identity=http://provider1:5000/v2.0
         * provider1.tenant1.name=CDP-ONAP-APPC provider1.tenant1.userid=testUser
         * provider1.tenant1.password=testPassword provider1.tenant2.name=TEST-TENANT provider1.tenant2.userid=testUser
         * provider1.tenant2.password=testPassword provider2.type=OpenStackProvider provider2.name=PDK1
         * provider2.identity=http://provider2:5000/v2.0 provider2.tenant1.name=someName
         * provider2.tenant1.userid=someUser provider2.tenant1.password=somePassword </pre> </p>
         */
        factory = ProviderOperationFactory.getInstance();
        providerCache = new HashMap<>();
        Properties properties = configuration.getProperties();
        List<Node> providers = StructuredPropertyHelper.getStructuredProperties(properties, Property.PROVIDER);
        for (Node provider : providers) {
            ProviderCache cache = new ProviderCache();
            List<Node> providerNodes = provider.getChildren();
            for (Node node : providerNodes) {
                if (node.getName().equals(Property.PROVIDER_TYPE)) {
                    cache.setProviderType(node.getValue());
                } else if (node.getName().equals(Property.PROVIDER_IDENTITY)) {
                    cache.setIdentityURL(node.getValue());
                    cache.setProviderName(node.getValue());
                } else if (node.getName().startsWith(Property.PROVIDER_TENANT)) {
                    String tenantName = null;
                    String userId = null;
                    String password = null;
                    // domain is not required so set a default
                    String domain = ProviderAdapterImpl.DEFAULT_DOMAIN_NAME;
                    for (Node node2 : node.getChildren()) {
                        switch (node2.getName()) {
                            case Property.PROVIDER_TENANT_NAME:
                                tenantName = node2.getValue();
                                break;
                            case Property.PROVIDER_TENANT_USERID:
                                userId = node2.getValue();
                                ProviderAdapterImpl.defaultUser = node2.getValue();
                                break;
                            case Property.PROVIDER_TENANT_PASSWORD:
                                //The passwords are automatically decrpyted by the appc Configuration
                                //class and require no additional method calls to decrypt.
                                password = node2.getValue();
                                ProviderAdapterImpl.defaultPass = node2.getValue();
                                break;
                            case Property.PROVIDER_TENANT_DOMAIN:
                                domain = node2.getValue();
                                ProviderAdapterImpl.defaultDomain = node2.getValue();
                                break;
                            default:
                                break;
                        }
                    }
                    cache.addTenant(null, tenantName, userId, password, domain);
                }
            }
            /*
             * Add the provider to the set of providers cached
             */
            if (cache.getIdentityURL() != null && cache.getProviderType() != null) {
                providerCache.put(null, cache);
                providerCache.put(cache.getIdentityURL(), cache);
            }
            /*
             * Now, initialize the cache for the loaded provider
             */
            cache.initialize();
        }
    }
    @Override
    public Server attachVolume(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
          IProviderOperation op = factory.getOperationObject(Operation.ATTACHVOLUME_SERVICE);
            op.setProviderCache(this.providerCache);
            op.setDefaultPassword(defaultPass);
            op.setDefaultUser(defaultUser);
            return (Server) op.doOperation(params, ctx);
    }
    @Override
    public Server dettachVolume(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
         IProviderOperation op = factory.getOperationObject(Operation.DETACHVOLUME_SERVICE);
            op.setProviderCache(this.providerCache);
            op.setDefaultPassword(defaultPass);
            op.setDefaultUser(defaultUser);
            return (Server) op.doOperation(params, ctx);
    }
    @Override
    public Server rebootServer(Map<String, String> params, SvcLogicContext context) throws SvcLogicException {
        IProviderOperation op = factory.getOperationObject(Operation.REBOOT_SERVICE);
        op.setProviderCache(this.providerCache);
        op.setDefaultPassword(defaultPass);
        op.setDefaultUser(defaultUser);
        op.setDefaultDomain(defaultDomain);
        return (Server) op.doOperation(params, context);
    }

}
