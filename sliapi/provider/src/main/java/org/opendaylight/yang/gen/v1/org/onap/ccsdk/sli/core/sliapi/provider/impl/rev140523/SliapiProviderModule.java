/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.provider.impl.rev140523;

import org.onap.ccsdk.sli.core.sliapi.sliapiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliapiProviderModule extends org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.provider.impl.rev140523.AbstractSliapiProviderModule {
    private final Logger LOG = LoggerFactory.getLogger( SliapiProviderModule.class );

	public SliapiProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public SliapiProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.provider.impl.rev140523.SliapiProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

    	LOG.info("Calling SliapiProviderModule.createInstance");
        final sliapiProvider provider = new sliapiProvider();
        provider.setDataBroker( getDataBrokerDependency() );
        provider.setNotificationService( getNotificationServiceDependency() );
        provider.setRpcRegistry( getRpcRegistryDependency() );
        provider.initialize();
        return new AutoCloseable() {

           @Override
           public void close() throws Exception {
               //TODO: CLOSE ANY REGISTRATION OBJECTS CREATED USING ABOVE BROKER/NOTIFICATION
               //SERVIE/RPC REGISTRY
               provider.close();
           }
       };
    }

}
