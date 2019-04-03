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
package org.onap.ccsdk.sli.core.sli.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicClassResolverLighty;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicServiceImplLighty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the sli-provider artifact.
 */
public class SliModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SliModule.class);

    private final DbLibService dbLibService;
    private final SvcLogicResource svcLogicResource;
    private final SvcLogicRecorder svcLogicRecorder;
    private final SvcLogicJavaPlugin svcLogicJavaPlugin;
    private final SvcLogicAdaptor svcLogicAdaptor;

    private SvcLogicPropertiesProviderImpl svcLogicPropertiesImpl;
    private SvcLogicServiceImplLighty svcLogicImpl;
    private SvcLogicClassResolverLighty svcLogicClassResolver;

    public SliModule(DbLibService dbLibService, SvcLogicResource svcLogicResource, SvcLogicRecorder svcLogicRecorder,
            SvcLogicJavaPlugin svcLogicJavaPlugin, SvcLogicAdaptor svcLogicAdaptor) {
        this.dbLibService = dbLibService;
        this.svcLogicResource = svcLogicResource;
        this.svcLogicRecorder = svcLogicRecorder;
        this.svcLogicJavaPlugin = svcLogicJavaPlugin;
        this.svcLogicAdaptor = svcLogicAdaptor;
    }

    @Override
    protected boolean initProcedure() {
        this.svcLogicPropertiesImpl = new SvcLogicPropertiesProviderImpl();
        this.svcLogicClassResolver = new SvcLogicClassResolverLighty(svcLogicResource, svcLogicRecorder,
                svcLogicJavaPlugin, svcLogicAdaptor);
        this.svcLogicImpl = new SvcLogicServiceImplLighty(this.svcLogicPropertiesImpl, dbLibService,
                svcLogicClassResolver);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public SvcLogicService getSvcLogicServiceImpl() {
        return this.svcLogicImpl;
    }
}
