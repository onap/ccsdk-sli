/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights
 * 			reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.northbound.daeximoffsitebackup;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.google.common.util.concurrent.FluentFuture;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.daeximoffsitebackup.rev180926.RetrieveDataInput;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.Rpc;

public class DaeximOffsiteBackupProviderTest {
    public DataBroker dataBroker;
    public ReadWriteTransaction writeTransaction;
    public FluentFuture checkedFuture;
    public RpcProviderService rpcRegistry;
    public DaeximOffsiteBackupProvider provider;
    public Properties resProps;

    @Before
    public void setup() {
        resProps = new Properties();
        resProps.put("error-code", "200");
        resProps.put("error-message", "Success");
        dataBroker = mock(DataBroker.class);
        writeTransaction = mock(ReadWriteTransaction.class);
        checkedFuture = mock(FluentFuture.class);
        rpcRegistry = mock(RpcProviderService.class);
        when(rpcRegistry.registerRpcImplementations((Rpc<?,?>)any())).thenReturn(null);
        try {
            when(checkedFuture.get()).thenReturn(null);
        }
        catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        when(writeTransaction.commit()).thenReturn(checkedFuture);
        when(dataBroker.newReadWriteTransaction()).thenReturn(writeTransaction);

        provider = new DaeximOffsiteBackupProvider(dataBroker, rpcRegistry);
    }

    @Test
    public void initializeTest() {
        provider.initialize();
    }

    @Test
    public void closeTest() {
        try {
            provider.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void backupDataTest() {
        try {
            assertNotNull(provider.invoke(null));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.invoke(null));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.invoke(null));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.invoke(null));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.invoke(null));
        }
        catch(Exception e) {
            fail();
        }
    }

    @Test
    public void retrieveDataTest() {
        RetrieveDataInput input = new RetrieveDataInput() {

            @Override
            public String getPodName() {
                return "Some Pod";
            }

            @Override
            public String getTimestamp() {
                return "Some Timestamp";
            }


            @Override
            public @NonNull Map<Class<? extends Augmentation<RetrieveDataInput>>, Augmentation<RetrieveDataInput>> augmentations() {
                return null;
            }

        };

        try {
            assertNotNull(provider.retrieveData(input));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.retrieveData(input));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.retrieveData(input));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.retrieveData(input));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.retrieveData(input));
        }
        catch(Exception e) {
            fail();
        }
    }

    @Test
    public void archiveOperationsTest() {
        List<String> files = Arrays.asList("src/test/resources/fileToZip1", "src/test/resources/fileToZip2");
        String archive = "src/test/resources/zippedArchive.zip";
        try {
            Method method = provider.getClass().getDeclaredMethod("createArchive", List.class, String.class);
            method.setAccessible(true);
            method.invoke(provider, files, archive);

        }
        catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            fail();
        }

        try {
            Field field = provider.getClass().getDeclaredField("DAEXIM_DIR");
            field.setAccessible(true);
            field.set(provider, "");
            Method method = provider.getClass().getDeclaredMethod("extractArchive", String.class);
            method.setAccessible(true);
            method.invoke(provider, archive);
        }
        catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            fail();
        }
        finally {
            File zip = new File(archive);
            zip.delete();
        }
    }
}
