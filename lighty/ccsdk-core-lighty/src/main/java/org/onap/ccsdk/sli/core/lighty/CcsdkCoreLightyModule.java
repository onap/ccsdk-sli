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

package org.onap.ccsdk.sli.core.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.dblib.lighty.DblibModule;
import org.onap.ccsdk.sli.core.lighty.common.CcsdkLightyUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.lighty.SliModule;
import org.onap.ccsdk.sli.core.sliapi.lighty.SliApiModule;
import org.onap.ccsdk.sli.core.slipluginutils.lighty.SliPluginUtilsModule;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that groups all other LightyModules
 * from the ccsdk-sli-core repository so they can be all treated as one component (for example started/stopped at once).
 * For more information about the lighty.io visit the website https://lighty.io.
 */
public class CcsdkCoreLightyModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(CcsdkCoreLightyModule.class);

    private final DataBroker dataBroker;
    private final NotificationPublishService notificationPublishService;
    private final RpcProviderRegistry rpcProviderRegistry;
    private final AAAEncryptionService aaaEncryptionService;
    private final SvcLogicResource svcLogicResource;
    private final SvcLogicRecorder svcLogicRecorder;
    private final SvcLogicJavaPlugin svcLogicJavaPlugin;
    private final SvcLogicAdaptor svcLogicAdaptor;

    private DblibModule dblibModule;
    private SliModule sliModule;
    private SliApiModule sliApiModule;
    private SliPluginUtilsModule sliPluginUtilsModule;

    // FIXME cyclic dependency - implementation of SvcLogicResource is located in adaptors and CcsdkAdaptorsLightyModule
    //  is depencent on DbLibService from core
    public CcsdkCoreLightyModule(DataBroker dataBroker, NotificationPublishService notificationPublishService,
            RpcProviderRegistry rpcProviderRegistry, AAAEncryptionService aaaEncryptionService,
            SvcLogicResource svcLogicResource, SvcLogicRecorder svcLogicRecorder, SvcLogicJavaPlugin svcLogicJavaPlugin,
            SvcLogicAdaptor svcLogicAdaptor) {
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.aaaEncryptionService = aaaEncryptionService;
        this.svcLogicResource = svcLogicResource;
        this.svcLogicRecorder = svcLogicRecorder;
        this.svcLogicJavaPlugin = svcLogicJavaPlugin;
        this.svcLogicAdaptor = svcLogicAdaptor;
    }

    protected boolean initProcedure() {
        LOG.debug("Initializing CCSDK Core Lighty module...");

        this.dblibModule = new DblibModule(aaaEncryptionService);
        if (!CcsdkLightyUtils.startLightyModule(dblibModule)) {
            return false;
        }

        this.sliModule = new SliModule(dblibModule.getDbLibService(), svcLogicResource, svcLogicRecorder,
                svcLogicJavaPlugin, svcLogicAdaptor);
        if (!CcsdkLightyUtils.startLightyModule(sliModule)) {
            return false;
        }

        this.sliApiModule = new SliApiModule(dataBroker, notificationPublishService, rpcProviderRegistry, sliModule.getSvcLogicServiceImpl());
        if (!CcsdkLightyUtils.startLightyModule(sliApiModule)) {
            return false;
        }

        this.sliPluginUtilsModule = new SliPluginUtilsModule();
        if (!CcsdkLightyUtils.startLightyModule(sliPluginUtilsModule)) {
            return false;
        }

        LOG.debug("CCSDK Core Lighty module was initialized successfully");
        return true;
    }

    protected boolean stopProcedure() {
        LOG.debug("Stopping CCSDK Core Lighty module...");

        boolean stopSuccessful = true;

        if (!CcsdkLightyUtils.stopLightyModule(sliPluginUtilsModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(sliApiModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(sliModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(dblibModule)) {
            stopSuccessful = false;
        }

        if (stopSuccessful) {
            LOG.debug("CCSDK Core Lighty module was stopped successfully");
        } else {
            LOG.error("CCSDK Core Lighty module was not stopped successfully!");
        }
        return stopSuccessful;
    }

    public DblibModule getDblibModule() {
        return dblibModule;
    }

    public SliModule getSliModule() {
        return sliModule;
    }

    public SliApiModule getSliApiModule() {
        return sliApiModule;
    }

    public SliPluginUtilsModule getSliPluginUtilsModule() {
        return sliPluginUtilsModule;
    }
}
