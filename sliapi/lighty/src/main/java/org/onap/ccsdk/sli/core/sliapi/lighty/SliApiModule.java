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
package org.onap.ccsdk.sli.core.sliapi.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.onap.ccsdk.sli.core.sliapi.sliapiProviderLighty;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the sliapi-provider artifact.
 */
public class SliApiModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SliApiModule.class);

    private final DataBroker dataBroker;
    private final NotificationPublishService notificationPublishService;
    private final RpcProviderRegistry rpcRegistry;
    private final SvcLogicService svcLogic;

    private sliapiProviderLighty sliapiProvider;

    public SliApiModule (DataBroker dataBroker, NotificationPublishService notificationPublishService,
            RpcProviderRegistry rpcRegistry, SvcLogicService svcLogic) {
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.rpcRegistry = rpcRegistry;
        this.svcLogic = svcLogic;
    }

    @Override
    protected boolean initProcedure() {
        LOG.debug("Initializing Lighty module {}...", this.getClass());
        this.sliapiProvider = new sliapiProviderLighty(dataBroker, notificationPublishService, rpcRegistry, svcLogic);
        LOG.debug("Lighty module {} initialized", this.getClass());
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

}
