/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.sli.adaptors.iaas.provider.operation.api;

import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.*;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation.*;

public class ProviderOperationFactoryTest {

    @Test
    public void should_return_evacuate_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(EVACUATE_SERVICE);

        Assert.assertTrue(operation instanceof EvacuateServer);
    }

    @Test
    public void should_return_migrate_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(MIGRATE_SERVICE);

        Assert.assertTrue(operation instanceof MigrateServer);
    }

    @Test
    public void should_return_rebuild_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(REBUILD_SERVICE);

        Assert.assertTrue(operation instanceof RebuildServer);
    }

    @Test
    public void should_return_restart_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(RESTART_SERVICE);

        Assert.assertTrue(operation instanceof RestartServer);
    }

    @Test
    public void should_return_vm_status_check_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(VMSTATUSCHECK_SERVICE);

        Assert.assertTrue(operation instanceof VmStatuschecker);
    }

    @Test
    public void should_return_snapshot_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(SNAPSHOT_SERVICE);

        Assert.assertTrue(operation instanceof CreateSnapshot);
    }

    @Test
    public void should_return_terminate_stack_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(TERMINATE_STACK);

        Assert.assertTrue(operation instanceof TerminateStack);
    }

    @Test
    public void should_return_snapshot_stack_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(SNAPSHOT_STACK);

        Assert.assertTrue(operation instanceof SnapshotStack);
    }

    @Test
    public void should_return_restore_stack_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(RESTORE_STACK);

        Assert.assertTrue(operation instanceof RestoreStack);
    }

    @Test
    public void should_return_start_service_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(START_SERVICE);

        Assert.assertTrue(operation instanceof StartServer);
    }

    @Test
    public void should_return_stop_service_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(STOP_SERVICE);

        Assert.assertTrue(operation instanceof StopServer);
    }

    @Test
    public void should_return_terminate_service_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(TERMINATE_SERVICE);

        Assert.assertTrue(operation instanceof TerminateServer);
    }

    @Test
    public void should_return_lookup_service_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(LOOKUP_SERVICE);

        Assert.assertTrue(operation instanceof LookupServer);
    }

    @Test
    public void should_return_attach_volume_service_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(ATTACHVOLUME_SERVICE);

        Assert.assertTrue(operation instanceof AttachVolumeServer);
    }

    @Test
    public void should_return_detach_volume_service_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(DETACHVOLUME_SERVICE);

        Assert.assertTrue(operation instanceof DettachVolumeServer);
    }

    @Test
    public void should_return_reboot_service_operation() throws SvcLogicException {
        IProviderOperation operation = ProviderOperationFactory
            .getInstance()
            .getOperationObject(Operation.REBOOT_SERVICE);

        Assert.assertTrue(operation instanceof RebootServer);
    }

}