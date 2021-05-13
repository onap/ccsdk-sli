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

package org.onap.ccsdk.sli.adaptors.iaas.provider.operation.api;

import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.*;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * Singleton factory of provider operations objects with cache
 *
 * @since September 26, 2016
 */
public class ProviderOperationFactory {
    /**
     * holds instance of the class
     */
    private static ProviderOperationFactory instance;
    /**
     * holds concrete operations objects
     */
    private Map<Operation, IProviderOperation> operations;
    /**
     * private constructor
     */
    private ProviderOperationFactory() {
        this.operations = new HashMap<>();
    }
    /**
     * @return instance of the factory
     */
    public static ProviderOperationFactory getInstance() {
        if (instance == null) {
            instance = new ProviderOperationFactory();
        }
        return instance;
    }
    /**
     * @param op
     * @return concrete operation impl
     */
    public IProviderOperation getOperationObject(Operation op) throws SvcLogicException {
        IProviderOperation opObject = operations.get(op);
        if (opObject == null) {
            switch (op) {
                case EVACUATE_SERVICE:
                    opObject = new EvacuateServer();
                    break;
                case MIGRATE_SERVICE:
                    opObject = new MigrateServer();
                    break;
                case REBUILD_SERVICE:
                    opObject = new RebuildServer();
                    break;
                case RESTART_SERVICE:
                    opObject = new RestartServer();
                    break;
                case VMSTATUSCHECK_SERVICE:
                    opObject = new VmStatuschecker();
                    break;
                case SNAPSHOT_SERVICE:
                    opObject = new CreateSnapshot();
                    break;
                case TERMINATE_STACK:
                    opObject = new TerminateStack();
                    break;
                case SNAPSHOT_STACK:
                    opObject = new SnapshotStack();
                    break;
                case RESTORE_STACK:
                    opObject = new RestoreStack();
                    break;
                case START_SERVICE:
                    opObject = new StartServer();
                    break;
                case STOP_SERVICE:
                    opObject = new StopServer();
                    break;
                case TERMINATE_SERVICE:
                    opObject = new TerminateServer();
                    break;
                case LOOKUP_SERVICE:
                    opObject = new LookupServer();
                    break;
                case ATTACHVOLUME_SERVICE:
                    opObject = new AttachVolumeServer();
                    break;
                case DETACHVOLUME_SERVICE:
                    opObject = new DettachVolumeServer();
                    break;
                case REBOOT_SERVICE:
                    opObject = new RebootServer();
                    break;
                default:
                    throw new SvcLogicException("Unsupported provider operation.");
            }
            operations.put(op, opObject);
        }
        return opObject;
    }
}
