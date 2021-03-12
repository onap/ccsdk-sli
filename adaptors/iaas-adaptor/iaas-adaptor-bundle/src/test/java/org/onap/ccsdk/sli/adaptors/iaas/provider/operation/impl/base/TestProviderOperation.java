/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base;

import com.att.cdp.openstack.OpenStackContext;
import com.att.cdp.zones.model.Server.Status;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.adaptors.iaas.impl.ProviderCache;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.adaptors.iaas.impl.TenantCache;
import org.onap.ccsdk.sli.adaptors.iaas.impl.VMURL;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.AttachVolumeServer;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.MockGenerator;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.pool.Pool;
import org.onap.ccsdk.sli.core.utils.pool.PoolDrainedException;
import org.onap.ccsdk.sli.core.utils.pool.PoolExtensionException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestProviderOperation {

    ProviderServerOperation underTest = spy(new AttachVolumeServer());

    @Test
    public void testDoFailureRequestContextHttpStatusString() throws SvcLogicException {
        RequestContext rc = mock(RequestContext.class);
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        HttpStatus code = HttpStatus.NOT_FOUND_404;
        String message = "PALOS\n";
        underTest.doFailure(rc, code, message);
        verify(underTest).doFailure(rc, code, message, null);
    }

    @Test(expected = SvcLogicException.class)
    public void testDoFailureRequestContextHttpStatusStringException() throws SvcLogicException {
        RequestContext rc = mock(RequestContext.class);
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        HttpStatus code = spy(HttpStatus.NOT_FOUND_404);
        doThrow(new RuntimeException("TEST")).when(code).getStatusCode();
        String message = "PALOS\n";
        underTest.doFailure(rc, code, message, new Throwable("TEST"));
        verify(underTest).doFailure(rc, code, message, null);
    }

    @Test
    public void testDoFailureRequestContextHttpStatusStringSvcLogicException() throws SvcLogicException {
        RequestContext rc = mock(RequestContext.class);
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        HttpStatus code = HttpStatus.NOT_FOUND_404;
        String message = "PALOS\n";
        doThrow(new SvcLogicException("TEST")).when(underTest).doFailure(rc, code, message, null);
        underTest.doFailure(rc, code, message);
        verify(underTest).doFailure(rc, code, message, null);
    }

    @Test(expected = SvcLogicException.class)
    public void testDoFailureRequestContextHttpStatusStringThrowableSvcLogicException() throws SvcLogicException {
        RequestContext rc = mock(RequestContext.class);
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        HttpStatus code = HttpStatus.NOT_FOUND_404;
        String message = "PALOS\n";
        underTest.doFailure(rc, code, message, new Throwable());
    }

    @Test
    public void testValidateVM() throws RequestFailedException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        RequestContext rc = mock(RequestContext.class);
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        assertTrue(underTest.validateVM(rc, "TEST", "TEST", null));
    }

    @Test
    public void testGetContextNullVM() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        assertNull(underTest.getContext(rc, "%£$%^$", "%£$%^$"));
        verify(underTest).doFailure(Mockito.any(RequestContext.class), Mockito.any(HttpStatus.class),
                Mockito.anyString());
    }

    @Test
    public void testGetContextNullCache() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        underTest.setProviderCache(mg.getProviderCacheMap());
        assertNull(underTest.getContext(rc, "http://10.1.1.2:5000/v2/abc12345-1234-5678-890a-abcdefb12345/servers/"
                + "abc12345-1234-5678-890a-abcdefb12345", "%£$%^$"));
        verify(underTest).doFailure(Mockito.any(RequestContext.class), Mockito.any(HttpStatus.class),
                Mockito.anyString());
    }

    @Test
    public void testGetContextNullTenantCache() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        Map<String, ProviderCache> providerCacheMap = mg.getProviderCacheMap();
        providerCacheMap.put("TEST", mock(ProviderCache.class));
        underTest.setProviderCache(mg.getProviderCacheMap());
        assertNull(underTest.getContext(rc, "http://10.1.1.2:5000/v2/abc12345-1234-5678-890a-abcdefb12345/servers/"
                + "abc12345-1234-5678-890a-abcdefb12345", "TEST"));
        verify(underTest).doFailure(Mockito.any(RequestContext.class), Mockito.any(HttpStatus.class),
                Mockito.anyString());
    }

    @Test
    public void testGetContextPoolNullRegion() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        Map<String, ProviderCache> providerCacheMap = mg.getProviderCacheMap();
        TenantCache tenantCache = mock(TenantCache.class);
        when(tenantCache.getTenantName()).thenReturn("TEST");
        when(tenantCache.getTenantId()).thenReturn("TEST");
        when(tenantCache.determineRegion(Mockito.anyObject())).thenReturn(null);
        ProviderCache providerCache = mock(ProviderCache.class);
        when(providerCache.getTenant(Mockito.anyString())).thenReturn(tenantCache);
        providerCacheMap.put("TEST", providerCache);
        underTest.setProviderCache(mg.getProviderCacheMap());
        assertNull(underTest.getContext(rc, "http://10.1.1.2:5000/v2/abc12345-1234-5678-890a-abcdefb12345/servers/"
                + "abc12345-1234-5678-890a-abcdefb12345", "TEST"));
        verify(underTest).doFailure(Mockito.any(RequestContext.class), Mockito.any(HttpStatus.class),
                Mockito.anyString());
    }

    @Test
    public void testGetContextAttemptFailed() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        underTest.setProviderCache(mg.getProviderCacheMap());
        assertNull(underTest.getContext(rc,
                "http://10.1.1.2:5000/v2/abc12345-1234-5678-890a-abcdefb12345/servers/"
                        + "abc12345-1234-5678-890a-abcdefb12345",
                "http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3"));
    }

    @Test
    public void testGetContextRelogin() throws PoolExtensionException, PoolDrainedException {
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext svcLogicContext = mock(SvcLogicContext.class);
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        ProviderCache providerCache = mock(ProviderCache.class);
        Map<String, ProviderCache> providerCacheMap = new HashMap<String, ProviderCache>();
        providerCacheMap.put("http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3", providerCache);
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        OpenStackContext context = mock(OpenStackContext.class);
        when(context.isStale()).thenReturn(true);
        TenantCache tenantCache = mock(TenantCache.class);
        doReturn("cloudowner_region").when(tenantCache).determineRegion(any(VMURL.class));
        doReturn("abc12345-1234-5678-890a-abcdefb12345").when(tenantCache).getTenantId();
        doReturn("abc12345-1234-5678-890a-abcdefb12345").when(tenantCache).getTenantName();
        Pool pool = mock(Pool.class);
        Map<String, Pool> tenantCachePools = new HashMap<String, Pool>();
        tenantCachePools.put("cloudowner_region", pool);
        doReturn(tenantCachePools).when(tenantCache).getPools();
        when(providerCache.getTenant(Mockito.anyString())).thenReturn(tenantCache);
        doReturn(tenantCache).when(providerCache).getTenant(Mockito.anyString());
        doReturn(context).when(pool).reserve();
        underTest.setProviderCache(providerCacheMap);
        assertTrue(underTest.getContext(rc,
                "http://10.1.1.2:5000/v2/abc12345-1234-5678-890a-abcdefb12345/servers/"
                        + "abc12345-1234-5678-890a-abcdefb12345",
                "http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3") instanceof OpenStackContext);
    }

    @Test
    public void testGetContextPoolException() throws PoolExtensionException, PoolDrainedException {
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext svcLogicContext = mock(SvcLogicContext.class);
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        ProviderCache providerCache = mock(ProviderCache.class);
        Map<String, ProviderCache> providerCacheMap = new HashMap<String, ProviderCache>();
        providerCacheMap.put("http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3", providerCache);

        when(rc.attempt()).thenReturn(true).thenReturn(false);
        OpenStackContext context = mock(OpenStackContext.class);
        when(context.isStale()).thenReturn(true);
        TenantCache tenantCache = mock(TenantCache.class);
        doReturn("cloudowner_region").when(tenantCache).determineRegion(any(VMURL.class));
        doReturn("abc12345-1234-5678-890a-abcdefb12345").when(tenantCache).getTenantId();
        doReturn("abc12345-1234-5678-890a-abcdefb12345").when(tenantCache).getTenantName();
        Pool pool = mock(Pool.class);
        Map<String, Pool> tenantCachePools = new HashMap<String, Pool>();
        tenantCachePools.put("cloudowner_region", pool);
        doReturn(tenantCachePools).when(tenantCache).getPools();
        when(providerCache.getTenant(Mockito.anyString())).thenReturn(tenantCache);
        doReturn(tenantCache).when(providerCache).getTenant(Mockito.anyString());
        doThrow(new PoolExtensionException("TEST")).when(pool).reserve();
        underTest.setProviderCache(providerCacheMap);
        underTest.getContext(rc,
                "http://10.1.1.2:5000/v2/abc12345-1234-5678-890a-abcdefb12345/servers/"
                        + "abc12345-1234-5678-890a-abcdefb12345",
                "http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3");
        verify(rc).delay();
    }

    @Test
    public void testGetContextException() {
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext svcLogicContext = mock(SvcLogicContext.class);
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        ProviderCache providerCache = mock(ProviderCache.class);
        Map<String, ProviderCache> providerCacheMap = new HashMap<String, ProviderCache>();
        providerCacheMap.put("http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3", providerCache);

        when(rc.attempt()).thenReturn(true).thenReturn(false);
        OpenStackContext context = mock(OpenStackContext.class);
        when(context.isStale()).thenReturn(true);
        TenantCache tenantCache = mock(TenantCache.class);
        doReturn("cloudowner_region").when(tenantCache).determineRegion(any(VMURL.class));
        doReturn("abc12345-1234-5678-890a-abcdefb12345").when(tenantCache).getTenantId();
        doReturn("abc12345-1234-5678-890a-abcdefb12345").when(tenantCache).getTenantName();
        Pool pool = mock(Pool.class);
        Map<String, Pool> tenantCachePools = new HashMap<String, Pool>();
        tenantCachePools.put("cloudowner_region", pool);
        doReturn(tenantCachePools).when(tenantCache).getPools();
        when(providerCache.getTenant(Mockito.anyString())).thenReturn(tenantCache);
        doReturn(tenantCache).when(providerCache).getTenant(Mockito.anyString());
        doThrow(new RuntimeException("TEST")).when(rc).delay();
        underTest.setProviderCache(providerCacheMap);
        assertNull(underTest.getContext(rc,
                "http://10.1.1.2:5000/v2/abc12345-1234-5678-890a-abcdefb12345/servers/"
                        + "abc12345-1234-5678-890a-abcdefb12345",
                "http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3"));
    }

    @Test
    public void testValidateVMURLRequestFailedExceptionWellFormed() {
        VMURL vm = mock(VMURL.class);
        try {
            underTest.validateVMURL(vm);
            fail("Exception not thrown");
        } catch (RequestFailedException rfe) {
            assert (rfe.getMessage().startsWith("The value vm-id is not well formed"));
        }
    }

    @Test
    public void testValidateVMURLRequestFailedExceptionTenantId() throws RequestFailedException {
        VMURL vm = mock(VMURL.class);
        when(vm.toString()).thenReturn("192.168.0.1");
        when(vm.getTenantId()).thenReturn("%£$%^$");
        try {
            underTest.validateVMURL(vm);
            fail("Exception not thrown");
        } catch (RequestFailedException rfe) {
            assert (rfe.getMessage().startsWith("The value vm-id has an invalid tenantId"));
        }
    }

    @Test
    public void testValidateVMURLRequestFailedExceptionServerId() throws RequestFailedException {
        VMURL vm = mock(VMURL.class);
        when(vm.toString()).thenReturn("192.168.0.1");
        when(vm.getTenantId()).thenReturn("0000000000000000000000000000000a");
        when(vm.getServerId()).thenReturn("%£$%^$");
        try {
            underTest.validateVMURL(vm);
            fail("Exception not thrown");
        } catch (RequestFailedException rfe) {
            assert (rfe.getMessage().startsWith("The value vm-id has an invalid serverId"));
        }
    }

    @Test
    public void testResolveContext() throws RequestFailedException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        RequestContext rc = mock(RequestContext.class);
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        assertNull(underTest.resolveContext(rc, new HashMap<String, String>(), "TEST", "TEST"));
    }

    @Test(expected = RequestFailedException.class)
    public void testValidateParameters() throws RequestFailedException {
        Properties properties = new Properties();
        properties.putAll(ConfigurationFactory.getConfiguration().getProperties());
        properties.put("TEST", "");
        Map<String, String> propertyMap = new HashMap<String, String>();
        for (String keys: properties.stringPropertyNames()) { propertyMap.put(keys, properties.getProperty(keys) );}
        underTest.validateParametersExist(propertyMap, Constants.PROPERTY_APPLICATION_NAME, "TEST");
    }

}
