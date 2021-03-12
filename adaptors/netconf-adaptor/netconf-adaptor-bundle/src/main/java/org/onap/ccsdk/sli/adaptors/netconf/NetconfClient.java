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

package org.onap.ccsdk.sli.adaptors.netconf;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;



public interface NetconfClient {

    /**
     * Open connection to netconf device.
     *
     * @param connectionDetails object providing details required for netconf connection
     */
    void connect(NetconfConnectionDetails connectionDetails) throws SvcLogicException;

    /**
     * Send Netconf message to device and receive response.
     *
     * @param message input netconf xml message
     * @return output netconf xml message
     */
    String exchangeMessage(String message) throws SvcLogicException;

    /**
     * send configuration to Netconf server
     *
     * @param configuration - xml configuration payload
     */
    void configure(String configuration) throws SvcLogicException;

    /**
     * returns running configuration of Netconf server
	*/
    String getConfiguration() throws SvcLogicException;

    /**
     * Disconnect from netconf device.
     */
    void disconnect() throws SvcLogicException;
}
