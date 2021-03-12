/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.ccsdk.sli.adaptors.netconf.internal;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import org.onap.ccsdk.sli.adaptors.netconf.ConnectionDetails;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfAdaptorConstants;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfConnectionDetails;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfDataAccessService;
import org.onap.ccsdk.sli.adaptors.netconf.exception.DataAccessException;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

public class NetconfDataAccessServiceImpl implements NetconfDataAccessService {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(NetconfDataAccessServiceImpl.class);

    private String schema;

    private DbLibService dbLibService;

    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public void setDbLibService(DbLibService service) {dbLibService = service;}

    @Override
    public String retrieveConfigFileName(String xmlID) {
        String fileContent = "";

        String queryString = "select " + NetconfAdaptorConstants.FILE_CONTENT_TABLE_FIELD_NAME + " " +
                             "from " + NetconfAdaptorConstants.CONFIGFILES_TABLE_NAME + " " +
                             "where " + NetconfAdaptorConstants.FILE_NAME_TABLE_FIELD_NAME + " = ?";

        ArrayList<String> argList = new ArrayList<>();
        argList.add(xmlID);

        try {
            final CachedRowSet data = dbLibService.getData(queryString, argList, schema);
            if (data.first()) {
                fileContent = data.getString(NetconfAdaptorConstants.FILE_CONTENT_TABLE_FIELD_NAME);
            }
        } catch (Exception e) {
            logger.error("Error Accessing Database " + e);
            throw new DataAccessException(e);
        }

        return fileContent;
    }

    @Override
    public boolean retrieveConnectionDetails(String vnfType, ConnectionDetails connectionDetails) {
        boolean recordFound = false;

        String queryString = "select " + NetconfAdaptorConstants.USER_NAME_TABLE_FIELD_NAME + "," +
                             NetconfAdaptorConstants.PASSWORD_TABLE_FIELD_NAME + "," + NetconfAdaptorConstants.PORT_NUMBER_TABLE_FIELD_NAME + " " +
                             "from " + NetconfAdaptorConstants.DEVICE_AUTHENTICATION_TABLE_NAME + " " +
                             "where " + NetconfAdaptorConstants.VNF_TYPE_TABLE_FIELD_NAME + " = ?";

        ArrayList<String> argList = new ArrayList<>();
        argList.add(vnfType);

        try {
            final CachedRowSet data = dbLibService.getData(queryString, argList, schema);
            if (data.first()) {
                connectionDetails.setUsername(data.getString(NetconfAdaptorConstants.USER_NAME_TABLE_FIELD_NAME));
                connectionDetails.setPassword(data.getString(NetconfAdaptorConstants.PASSWORD_TABLE_FIELD_NAME));
                connectionDetails.setPort(data.getInt(NetconfAdaptorConstants.PORT_NUMBER_TABLE_FIELD_NAME));
                recordFound = true;
            }
        } catch (SQLException e) {
            logger.error("Error Accessing Database " + e);
            throw new DataAccessException(e);
        }

        return recordFound;
    }

    @Override
    public boolean retrieveNetconfConnectionDetails(String vnfType, NetconfConnectionDetails connectionDetails) {
        ConnectionDetails connDetails = new ConnectionDetails();
        if(this.retrieveConnectionDetails(vnfType, connDetails))
        {
            connectionDetails.setHost(connDetails.getHost());
            connectionDetails.setPort(connDetails.getPort());
            connectionDetails.setUsername(connDetails.getUsername());
            connectionDetails.setPassword(connDetails.getPassword());
        }
        return true;
    }

    @Override
    public boolean logDeviceInteraction(String instanceId, String requestId, String creationDate, String logText) {
        String queryString = "INSERT INTO " + NetconfAdaptorConstants.DEVICE_INTERFACE_LOG_TABLE_NAME + "(" +
                             NetconfAdaptorConstants.SERVICE_INSTANCE_ID_FIELD_NAME + "," +
                             NetconfAdaptorConstants.REQUEST_ID_FIELD_NAME + "," +
                             NetconfAdaptorConstants.CREATION_DATE_FIELD_NAME + "," +
                             NetconfAdaptorConstants.LOG_FIELD_NAME + ") ";
        queryString += "values(?,?,?,?)";

        ArrayList<String> argList = new ArrayList<>();
        argList.add(instanceId);
        argList.add(requestId);
        argList.add(creationDate);
        argList.add(logText);

        try {
            dbLibService.writeData(queryString, argList, schema);
        } catch (SQLException e) {
            logger.error("Logging Device interaction failed - " + queryString);
            throw new DataAccessException(e);
        }

        return true;
    }

}
