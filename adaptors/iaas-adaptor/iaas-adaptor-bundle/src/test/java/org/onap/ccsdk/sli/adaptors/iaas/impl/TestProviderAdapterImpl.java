/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 Ericsson
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
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Stack;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.api.IProviderOperation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.api.ProviderOperationFactory;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.EvacuateServer;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

/**
 * This class is used to test methods and functions of the adapter implementation that do not
 * require and do not set up connections to any providers.
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
@Category(TestProviderAdapterImpl.class)
public class TestProviderAdapterImpl {

    @SuppressWarnings("nls")
    private static final String PROVIDER_NAME = "ILAB";

    @SuppressWarnings("nls")
    private static final String PROVIDER_TYPE = "OpenStackProvider";

    private static String IDENTITY_URL;

    private static String SERVER_URL;

    private static String SERVER_ID;

    private static Field configField;

    private static ProviderAdapterImpl adapter;

    /**
     * The Factory.
     */
    @Mock
    ProviderOperationFactory factory;

    /**
     * The Provider operation.
     */
    @Mock
    IProviderOperation providerOperation;

    /**
     * The Evacuate server.
     */
    @Mock
    EvacuateServer evacuateServer;

    private SvcLogicContext svcContext;

    private Map<String, String> params;

    private Image image;

    private Server server;

    private Stack stack;

    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test
     * to change the internal state accordingly.
     *
     * @throws NoSuchFieldException  if the field(s) dont exist
     * @throws SecurityException     if reflective access is not allowed
     */
    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException {
        Class<?> providerAdapterImplClass = ProviderAdapterImpl.class;
        Class<?> configurationFactoryClass = ConfigurationFactory.class;

        Field providerCacheField = providerAdapterImplClass.getDeclaredField("providerCache");
        providerCacheField.setAccessible(true);
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        IDENTITY_URL = props.getProperty("provider1.identity");
        SERVER_URL = props.getProperty("test.url");
        configField = configurationFactoryClass.getDeclaredField("config");
        configField.setAccessible(true);
        SERVER_ID = "server1";
    }

    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test
     * to change the internal state accordingly.
     *
     * @throws IllegalArgumentException if the specified object is not an instance of the class or         interface declaring the underlying field (or a subclass or implementor thereof), or         if an unwrapping conversion fails.
     * @throws IllegalAccessException   if this Field object is enforcing Java language access control         and the underlying field is either inaccessible or final.
     */
    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException {
        configField.set(null, null);
        Properties properties = new Properties();
        adapter = new ProviderAdapterImpl(properties);
        svcContext = new SvcLogicContext();
        params = new HashMap<>();
        params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
        params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
        server = new Server();
        server.setId(SERVER_ID);
        image = new Image();
        image.setStatus(Image.Status.ACTIVE);
        stack = new Stack();
        stack.setStatus(Stack.Status.ACTIVE);
        Map<String, Object> privateFields = ImmutableMap.<String, Object>builder().put("factory", factory)
                .put("providerCache", getProviderCache()).build();
        CommonUtility.injectMockObjects(privateFields, adapter);
    }

    private Map<String, ProviderCache> getProviderCache() {
        Map<String, ProviderCache> providerCache = new HashMap<>();
        ProviderCache cache = new ProviderCache();
        cache.setIdentityURL(IDENTITY_URL);
        cache.setProviderName(PROVIDER_NAME);
        cache.setProviderType(PROVIDER_TYPE);
        providerCache.put(cache.getIdentityURL(), cache);
        return providerCache;
    }

    /**
     * This test case is used to invoke the default constructor
     */
    @Test
    public void testDefaultConstructor() {
        ProviderAdapter adapter = new ProviderAdapterImpl();
        assertNotNull(adapter);
    }

    /**
     * This test case is used to invoke the argument constructor
     */
    @Test
    public void testArgumentedConstructor() {
        ProviderAdapter adapter = new ProviderAdapterImpl(true);
        assertNotNull(adapter);
    }

    /**
     * Tests that we get the Adapter name
     */
    //@Test
    public void testAdapterName() {
        assertNotNull(adapter.getAdapterName());
    }

    /**
     * This test case is used to restart the server
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the server cannot be restarted for some reason
     */
    @Test
    public void testRestartServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.RESTART_SERVICE, Server.Status.RUNNING);
        Server actualServer = adapter.restartServer(params, svcContext);
        assertEquals(Server.Status.RUNNING, actualServer.getStatus());
    }

    /**
     * This test case is used to start the server
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the server cannot be started for some reason
     */
    @Test
    public void testStartServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.START_SERVICE, Server.Status.RUNNING);
        Server actualServer = adapter.startServer(params, svcContext);
        assertEquals(Server.Status.RUNNING, actualServer.getStatus());
    }

    /**
     * This test case is used to stop the server
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the server cannot be stopped for some reason
     */
    @Test
    public void testStopServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.STOP_SERVICE, Server.Status.READY);
        Server actualServer = adapter.stopServer(params, svcContext);
        assertEquals(Server.Status.READY, actualServer.getStatus());
    }

    /**
     * Tests that the vmStatuschecker method works and returns the correct status of the VM
     * requested
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the vm status can not be verified
     */
    @Test
    public void testVmStatuschecker()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.VMSTATUSCHECK_SERVICE, Server.Status.READY);
        Server actualServer = adapter.vmStatuschecker(params, svcContext);
        assertEquals(Server.Status.READY, actualServer.getStatus());
    }

    /**
     * Tests that the terminate stack method works
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the stack cannot be terminated for some reason
     */
    @Test
    public void testTerminateStack()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.TERMINATE_STACK, null);
        stack.setStatus(Stack.Status.DELETED);
        Stack actualStack = adapter.terminateStack(params, svcContext);
        assertEquals(Stack.Status.DELETED, actualStack.getStatus());
    }

    /**
     * Tests that the snapshot method works and returns snapshot of the stack
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the stack snapshot can not be taken for some reason
     */
    @Test
    public void testSnapshotStack()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.SNAPSHOT_STACK, null);
        Stack actualStack = adapter.snapshotStack(params, svcContext);
        assertEquals(Stack.Status.ACTIVE, actualStack.getStatus());
    }

    /**
     * Tests that the restore method works and returns restored stack
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the stack cannot be restored for some reason
     */
    @Test
    public void testRestoreStack()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.RESTORE_STACK, null);
        Stack actualStack = adapter.restoreStack(params, svcContext);
        assertEquals(Stack.Status.ACTIVE, actualStack.getStatus());
    }

    /**
     * Tests that the lookup server will lookup for the server with server id
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the server cannot be found for some reason
     */
    @Test
    public void testLookupServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.LOOKUP_SERVICE, Server.Status.READY);
        Server actualServer = adapter.lookupServer(params, svcContext);
        assertEquals(SERVER_ID, actualServer.getId());
    }

    /**
     * Tests that the to create a snapshot and return a image
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the image snapshot can not be taken for some reason
     */
    @Test
    public void testCreateSnapshot()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.SNAPSHOT_SERVICE, Server.Status.READY);
        Image actualImage = adapter.createSnapshot(params, svcContext);
        assertEquals(image.getStatus(), actualImage.getStatus());
    }

    /**
     * Tests that the to calculate the server volume and attach it to the server
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the Server volume can not be calculated for some reason
     */
    @Test
    public void testAttachVolume()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.ATTACHVOLUME_SERVICE, Server.Status.READY);
        Server actualServer = adapter.attachVolume(params, svcContext);
        assertEquals(SERVER_ID, actualServer.getId());
    }

    /**
     * Tests that the to detach the calculated volume from the server
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid
     * @throws SvcLogicException        If the Server volume can not be detached for some reason
     */
    @Test
    public void testDettachVolume()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.DETACHVOLUME_SERVICE, Server.Status.READY);
        Server actualServer = adapter.dettachVolume(params, svcContext);
        assertEquals(SERVER_ID, actualServer.getId());
    }

    /**
     * Tests that we can restart a server that is already stopped
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid.
     * @throws SvcLogicException        If the server cannot be restarted for some reason
     */
    @Test
    public void testRestartStoppedServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.RESTART_SERVICE, Server.Status.RUNNING);
        Server actualServer = adapter.restartServer(params, svcContext);
        assertEquals(Server.Status.RUNNING, actualServer.getStatus());

    }

    /**
     * Tests that we can rebuild a server (not created from a bootable volume)
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid.
     * @throws SvcLogicException        If the provider cannot be found
     */
    @Test
    public void testRebuildServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.REBUILD_SERVICE, Server.Status.READY);
        Server actualServer = adapter.rebuildServer(params, svcContext);
        assertEquals(Server.Status.READY, actualServer.getStatus());
    }

    /**
     * Tests that we can terminate a running server
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid.
     * @throws SvcLogicException        If the provider cannot be found
     */
    @Test
    public void testTerminateServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.TERMINATE_SERVICE, Server.Status.DELETED);
        Server actualServer = adapter.terminateServer(params, svcContext);
        assertEquals(Server.Status.DELETED, actualServer.getStatus());
    }

    /**
     * Tests that we can evacuate a server to move it to non-pending state
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid.
     * @throws SvcLogicException        If the provider cannot be found
     */
    @Test
    public void testEvacuateServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.EVACUATE_SERVICE, Server.Status.READY);
        Server actualServer = adapter.evacuateServer(params, svcContext);
        assertEquals(Server.Status.READY, actualServer.getStatus());
    }

    /**
     * Tests that we can migrate a server. Migration can be done only on certain statuses like
     * READY, RUNNING & SUSPENDED
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid.
     * @throws SvcLogicException        If the provider cannot be found
     */
    @Test
    public void testMigrateServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.MIGRATE_SERVICE, Server.Status.READY);
        Server actualServer = adapter.migrateServer(params, svcContext);
        assertEquals(Server.Status.READY, actualServer.getStatus());
    }

    /**
     * Tests that we can reboot a server
     *
     * @throws IllegalStateException    If the identity service is not available or cannot be created
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if         the expected argument(s) are not defined or are invalid.
     * @throws SvcLogicException        If the provider cannot be found
     */
    @Test
    public void testRebootServer()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        prepareMock(Operation.REBOOT_SERVICE, Server.Status.READY);
        Server actualServer = adapter.rebootServer(params, svcContext);
        assertEquals(Server.Status.READY, actualServer.getStatus());
    }

    private void prepareMock(Operation operation, Server.Status serverStatus) throws SvcLogicException {
        IProviderOperation providerOperation = fetchOperation.apply(operation);
        ModelObject modelObject = fetchModelObject.apply(operation, serverStatus);
        when(factory.getOperationObject(operation)).thenReturn(providerOperation);
        when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(modelObject);

    }

    /**
     * The Fetch operation.
     */
    Function<Operation, IProviderOperation> fetchOperation = operation -> {
        if (operation.equals(Operation.EVACUATE_SERVICE))
            return evacuateServer;
        else
            return providerOperation;
    };

    /**
     * The Fetch server.
     */
    Function<Server.Status, Server> fetchServer = status -> {
        server.setStatus(status);
        return server;
    };

    /**
     * The Fetch model object.
     */
    BiFunction<Operation, Server.Status, ModelObject> fetchModelObject = (operation, status) -> {
        if (operation.equals(Operation.SNAPSHOT_SERVICE))
            return image;
        else if (operation == Operation.RESTORE_STACK || operation == Operation.SNAPSHOT_STACK
                || operation == Operation.TERMINATE_STACK)
            return stack;
        else
            return fetchServer.apply(status);
    };
}
