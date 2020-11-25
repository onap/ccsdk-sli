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

package org.onap.ccsdk.sli.core.sli.provider;

import java.util.Properties;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceBase;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;

public interface SvcLogicService extends SvcLogicServiceBase {

    String NAME = "org.onap.ccsdk.sli.core.sli.provider.SvcLogicService";

    @Deprecated
    Properties execute(String module, String rpc, String version, String mode, Properties parms) throws SvcLogicException;

    /**
     * Execute a directed graph
     *
     * @param module - module name
     * @param rpc - rpc name
     * @param version - version.  If null, use active version
     * @param mode - mode (sync/async)
     * @param parms - parameters, used to set SvcLogicContext attributes
     * @param domDataBroker - DOMDataBroker object
     * @return final values of attributes from SvcLogicContext, as Properties
     * @throws SvcLogicException
     */
    Properties execute(String module, String rpc, String version, String mode, Properties parms, DOMDataBroker domDataBroker) throws SvcLogicException;


}
