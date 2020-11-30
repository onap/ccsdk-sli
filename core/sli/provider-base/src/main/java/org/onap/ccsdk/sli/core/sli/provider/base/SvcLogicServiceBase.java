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

package org.onap.ccsdk.sli.core.sli.provider.base;

import java.util.Properties;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;

public interface SvcLogicServiceBase {


    /**
     * Check for existence of a directed graph
     * @param module - module name
     * @param rpc - rpc name
     * @param version - version.  If null, looks for active version
     * @param mode - mode (sync/async)
     * @return true if directed graph found, false otherwise
     * @throws SvcLogicException
     */
    boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException;

    /**
     *  Execute a directed graph
     *
     * @param module - module name
     * @param rpc - rpc name
     * @param version - version.  If null, use active version
     * @param mode - mode (sync/async)
     * @param parms - parameters, used to set SvcLogicContext attributes
     * @return final values of attributes from SvcLogicContext, as Properties
     * @throws SvcLogicException
     *
     *
     */
    Properties execute(String module, String rpc, String version, String mode, Properties parms) throws SvcLogicException;
    /**
     *  Execute a directed graph
     *
     * @param module - module name
     * @param rpc - rpc name
     * @param version - version.  If null, use active version
     * @param mode - mode (sync/async)
     * @param ctx - parameters, as a SvcLogicContext object
     * @return final values of attributes from SvcLogicContext, as Properties
     * @throws SvcLogicException
     *
     *
     */
    SvcLogicContext execute(String module, String rpc, String version, String mode, SvcLogicContext ctx) throws SvcLogicException;

    SvcLogicStore getStore() throws SvcLogicException;

    SvcLogicContext execute(SvcLogicGraph calledGraph, SvcLogicContext ctx) throws SvcLogicException;

    SvcLogicNode executeNode(SvcLogicNode nextNode, SvcLogicContext ctx) throws SvcLogicException;

}
