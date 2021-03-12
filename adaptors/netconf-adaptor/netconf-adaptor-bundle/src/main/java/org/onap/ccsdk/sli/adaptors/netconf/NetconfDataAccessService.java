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

import org.onap.ccsdk.sli.adaptors.netconf.exception.DataAccessException;
import org.onap.ccsdk.sli.core.dblib.DbLibService;


@SuppressWarnings("JavaDoc")
public interface NetconfDataAccessService {

    /**
     *
     * @param schema
     */
    void setSchema(String schema);

    /**
     *
     * @param dbLibService
     */
    void setDbLibService(DbLibService dbLibService);

    /**
     *
     * @param xmlID
     * @return
     * @throws DataAccessException
     */
    String retrieveConfigFileName(String xmlID);

    /**
     *
     * @param vnfType
     * @param connectionDetails
     * @return
     * @throws DataAccessException
     */
    boolean retrieveConnectionDetails(String vnfType, ConnectionDetails connectionDetails);

    /**
     *
     * @param vnfType
     * @param connectionDetails
     * @return
     * @throws DataAccessException
     */
    boolean retrieveNetconfConnectionDetails(String vnfType, NetconfConnectionDetails connectionDetails);

    /**
     *
     * @param instanceId
     * @param requestId
     * @param creationDate
     * @param logText
     * @return
     * @throws DataAccessException
     */
    boolean logDeviceInteraction(String instanceId, String requestId, String creationDate, String logText);

}
