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



public interface NetconfClientRestconf {

    /*
    mount device to controller
     @param deviceMountPointName - the name of the mounting point in controller
     @param payload - json data describing device info
     */
    void connect(String deviceMountPointName, String payload) throws SvcLogicException;

    /*
    check connection to device
    @param deviceMountPointName - the name of the mounting point in controller
     */
    boolean checkConnection(String deviceMountPointName) throws SvcLogicException;

    /*
	send configuration to Netconf server
	 @param configuration - xml configuration payload
	 @param deviceMountPointName - the name of the mounting point in controller
	 @param moduleName - name of the yang model
	 @param nodeName - name of the node created in server
	*/
    void configure(String configuration, String deviceMountPointName, String moduleName, String nodeName) throws SvcLogicException;

    /*
	returns configuration of Netconf server
	 @param deviceMountPointName - the name of the mounting point in controller
	 @param moduleName - name of the yang model
	 @param nodeName - name of the node created in server
	*/
    String getConfiguration(String deviceName, String moduleName, String nodeName) throws SvcLogicException;

    /*
    unmount device
     @param deviceMountPointName - the name of the mounting point in controller
     */
    void disconnect(String deviceMountPointName) throws SvcLogicException;
}
