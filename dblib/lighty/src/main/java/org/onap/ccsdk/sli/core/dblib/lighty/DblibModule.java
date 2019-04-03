/*
 * ============LICENSE_START==========================================
 * Copyright (c) 2019 PANTHEON.tech s.r.o.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 */

package org.onap.ccsdk.sli.core.dblib.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.dblib.DBLIBResourceProviderLighty;
import org.onap.ccsdk.sli.core.dblib.DBResourceManagerLighty;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the dblib artifact.
 */
public class DblibModule extends AbstractLightyModule {

    private final AAAEncryptionService aaaEncryptionService;

    private DBLIBResourceProviderLighty dbLibResourceProvider;
    private DBResourceManagerLighty dbResourceManager;

    public DblibModule(AAAEncryptionService aaaEncryptionService) {
        this.aaaEncryptionService = aaaEncryptionService;
    }

    @Override
    protected boolean initProcedure() {
        this.dbLibResourceProvider = new DBLIBResourceProviderLighty(aaaEncryptionService);
        this.dbResourceManager = new DBResourceManagerLighty(this.dbLibResourceProvider);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public DbLibService getDbLibService() {
        return dbResourceManager;
    }
}
