/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ssh;

import org.onap.ccsdk.sli.core.dblib.DbLibService;


@SuppressWarnings("JavaDoc")
public interface SshDataAccessService {

    /**
     *
     * @param schema
     */
    void setSchema(String schema);

    /**
     *@param dbLibService
     */
    void setDbLibService(DbLibService dbLibService);

    /**
     *
     * @param xmlID
     * @return
     * @throws SshDataAccessException
     */
    String retrieveConfigFileName(String xmlID) throws SshDataAccessException;

    /**
     *
     * @param vnfType
     * @param connectionDetails
     * @return
     * @throws SshException
     */
    boolean retrieveConnectionDetails(String vnfType, SshConnectionDetails connectionDetails) throws SshDataAccessException;

}
