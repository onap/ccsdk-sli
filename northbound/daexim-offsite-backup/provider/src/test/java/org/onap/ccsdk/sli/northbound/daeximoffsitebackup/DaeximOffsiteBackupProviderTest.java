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
import com.google.common.util.concurrent.CheckedFuture;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.daeximoffsitebackup.rev180926.DaeximOffsiteBackupService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.daeximoffsitebackup.rev180926.RetrieveDataInput;
import org.opendaylight.yangtools.yang.binding.Augmentation;

public class DaeximOffsiteBackupProviderTest {
    public DataBroker dataBroker;
    public ReadWriteTransaction writeTransaction;
    public CheckedFuture<Void, TransactionCommitFailedException> checkedFuture;
    public RpcProviderRegistry rpcRegistry;
    public DaeximOffsiteBackupProvider provider;
    public Properties resProps;

    @Before
    public void setup() {
        resProps = new Properties();
        resProps.put("error-code", "200");
        resProps.put("error-message", "Success");
        dataBroker = mock(DataBroker.class);
        writeTransaction = mock(ReadWriteTransaction.class);
        checkedFuture = mock(CheckedFuture.class);
        rpcRegistry = mock(RpcProviderRegistry.class);
        when(rpcRegistry.addRoutedRpcImplementation(any(), any(DaeximOffsiteBackupService.class))).thenReturn(null);
        try {
            when(checkedFuture.get()).thenReturn(null);
        }
        catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        when(writeTransaction.submit()).thenReturn(checkedFuture);
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
    public void onDataTreeChangedTest() {
        provider.onDataTreeChanged(null);
        // onDataTreeChanged is an empty stub
    }

    @Test
    public void backupDataTest() {
        try {
            assertNotNull(provider.backupData(null));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.backupData(null));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.backupData(null));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.backupData(null));
        }
        catch(Exception e) {
            fail();
        }
        try {
            assertNotNull(provider.backupData(null));
        }
        catch(Exception e) {
            fail();
        }
    }

    @Test
    public void retrieveDataTest() {
        RetrieveDataInput input = new RetrieveDataInput() {
            @Override
            public <E extends Augmentation<RetrieveDataInput>> @Nullable E augmentation(Class<E> augmentationType) {
                return null;
            }

            @Override
            public String getPodName() {
                return "Some Pod";
            }

            @Override
            public String getTimestamp() {
                return "Some Timestamp";
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
