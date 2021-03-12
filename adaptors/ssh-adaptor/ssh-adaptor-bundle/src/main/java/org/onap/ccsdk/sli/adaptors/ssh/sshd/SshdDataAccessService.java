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

package org.onap.ccsdk.sli.adaptors.ssh.sshd;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import org.onap.ccsdk.sli.adaptors.ssh.Constants;
import org.onap.ccsdk.sli.adaptors.ssh.SshConnectionDetails;
import org.onap.ccsdk.sli.adaptors.ssh.SshDataAccessException;
import org.onap.ccsdk.sli.adaptors.ssh.SshDataAccessService;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

public class SshdDataAccessService implements SshDataAccessService {

    private String schema = Constants.NETCONF_SCHEMA;
    private DbLibService dbLibService;

    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }

     public String getSchema() {
        return this.schema;
    }

    @Override
    public void setDbLibService(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    public DbLibService getDbLibService() {
        return this.dbLibService;
    }


    @Override
    public boolean retrieveConnectionDetails(String vnfType, SshConnectionDetails connectionDetails) throws SshDataAccessException {

        boolean recordFound = false;

        String queryString = "select " + Constants.USER_NAME_TABLE_FIELD_NAME + "," + Constants.PASSWORD_TABLE_FIELD_NAME + "," + Constants.PORT_NUMBER_TABLE_FIELD_NAME + " " +
                "from " + Constants.DEVICE_AUTHENTICATION_TABLE_NAME + " " +
                "where " + Constants.VNF_TYPE_TABLE_FIELD_NAME + " = ?";

        ArrayList<String> argList = new ArrayList<>();
        argList.add(vnfType);

        try {

            final CachedRowSet data = dbLibService.getData(queryString, argList, schema);
            if (data.first()) {
                recordFound = true;
                connectionDetails.setUsername(data.getString(Constants.USER_NAME_TABLE_FIELD_NAME));
                connectionDetails.setPassword(data.getString(Constants.PASSWORD_TABLE_FIELD_NAME));
                connectionDetails.setPort(data.getInt(Constants.PORT_NUMBER_TABLE_FIELD_NAME));
            }

        } catch (SQLException e) {
            throw new SshDataAccessException(e);
        }

        return recordFound;
    }

    @Override
    public String retrieveConfigFileName(String xmlID) throws SshDataAccessException {
        String fileContent;

        String queryString = "select " + Constants.FILE_CONTENT_TABLE_FIELD_NAME + " " +
                "from " + Constants.CONFIGFILES_TABLE_NAME + " " +
                "where " + Constants.FILE_NAME_TABLE_FIELD_NAME + " = ?";

        ArrayList<String> argList = new ArrayList<>();
        argList.add(xmlID);

        try {

            final CachedRowSet data = dbLibService.getData(queryString, argList, schema);
            fileContent = data.getString(Constants.FILE_CONTENT_TABLE_FIELD_NAME);

        } catch (SQLException e) {
            throw new SshDataAccessException(e);
        }

        return fileContent;
    }


}
