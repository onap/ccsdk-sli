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

package org.onap.ccsdk.sli.core.sli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicJdbcStore implements SvcLogicStore {
    private static final Logger LOG = LoggerFactory.getLogger(SvcLogicJdbcStore.class);

    private String dbUrl = null;
    private String dbName = null;
    private String dbUser = null;
    private String dbPasswd = null;
    private String dbDriver = null;

    private Connection dbConn;
    private PreparedStatement hasActiveGraphStmt = null;
    private PreparedStatement hasVersionGraphStmt = null;
    private PreparedStatement fetchActiveGraphStmt = null;
    private PreparedStatement fetchVersionGraphStmt = null;
    private PreparedStatement storeGraphStmt = null;
    private PreparedStatement deleteGraphStmt = null;

    private PreparedStatement deactivateStmt = null;
    private PreparedStatement activateStmt = null;

    private void getConnection() throws ConfigurationException {

        Properties jdbcProps = new Properties();

        jdbcProps.setProperty("user", dbUser);
        jdbcProps.setProperty("password", dbPasswd);

        try {
            Driver dvr = new org.mariadb.jdbc.Driver();
            if (dvr.acceptsURL(dbUrl)) {
                LOG.debug("Driver com.mysql.jdbc.Driver accepts {}", dbUrl);
            } else {
                LOG.warn("Driver com.mysql.jdbc.Driver does not accept {}", dbUrl);
            }
        } catch (SQLException e1) {
            LOG.error("Caught exception trying to load com.mysql.jdbc.Driver", e1);
        }

        try {
            this.dbConn = DriverManager.getConnection(dbUrl, jdbcProps);
        } catch (Exception e) {
            throw new ConfigurationException("failed to get database connection [" + dbUrl + "]", e);
        }

    }

    private void createTable() throws ConfigurationException {

        DatabaseMetaData dbm;

        try {
            dbm = dbConn.getMetaData();
        } catch (SQLException e) {

            throw new ConfigurationException("could not get databse metadata", e);
        }

        // See if table SVC_LOGIC exists. If not, create it.
        Statement stmt = null;
        try {

            ResultSet tables = dbm.getTables(null, null, "SVC_LOGIC", null);
            if (tables.next()) {
                LOG.debug("SVC_LOGIC table already exists");
            } else {
                String crTableCmd = "CREATE TABLE " + dbName + ".SVC_LOGIC (" + "module varchar(80) NOT NULL,"
                        + "rpc varchar(80) NOT NULL," + "version varchar(40) NOT NULL," + "mode varchar(5) NOT NULL,"
                        + "active varchar(1) NOT NULL,graph BLOB,"
                        + "modified_timestamp timestamp ,"
                        + "md5sum varchar(128) DEFAULT NULL,"
                        + "CONSTRAINT P_SVC_LOGIC PRIMARY KEY(module, rpc, version, mode))";

                stmt = dbConn.createStatement();
                stmt.executeUpdate(crTableCmd);
            }
        } catch (Exception e) {
            throw new ConfigurationException("could not create SVC_LOGIC table", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.error("Statement close error ", e);
                }
            }
        }

        // See if NODE_TYPES table exists and, if not, create it
        stmt = null;
        try {

            ResultSet tables = dbm.getTables(null, null, "NODE_TYPES", null);
            if (tables.next()) {
                LOG.debug("NODE_TYPES table already exists");
            } else {
                String crTableCmd = "CREATE TABLE " + dbName + ".NODE_TYPES (" + "nodetype varchar(80) NOT NULL,"
                        + "CONSTRAINT P_NODE_TYPES PRIMARY KEY(nodetype))";

                stmt = dbConn.createStatement();

                stmt.executeUpdate(crTableCmd);
            }
        } catch (Exception e) {
            throw new ConfigurationException("could not create SVC_LOGIC table", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.error("Statement close error ", e);
                }
            }
        }
    }

    private void prepStatements() throws ConfigurationException {

        // Prepare statements
        String hasVersionGraphSql = CommonConstants.JDBC_SELECT_COUNT + dbName + CommonConstants.SVCLOGIC_TABLE
                + CommonConstants.JDBC_GRAPH_QUERY;

        try {
            hasVersionGraphStmt = dbConn.prepareStatement(hasVersionGraphSql);
        } catch (Exception e) {
            throw new ConfigurationException(CommonConstants.JDBC_STATEMENT_ERR + hasVersionGraphSql, e);

        }

        String hasActiveGraphSql = CommonConstants.JDBC_SELECT_COUNT + dbName + CommonConstants.SVCLOGIC_TABLE
                + CommonConstants.JDBC_ACTIVE_GRAPH_QUERY;

        try {
            hasActiveGraphStmt = dbConn.prepareStatement(hasActiveGraphSql);
        } catch (Exception e) {
            throw new ConfigurationException(CommonConstants.JDBC_STATEMENT_ERR + hasVersionGraphSql, e);

        }

        String fetchVersionGraphSql = CommonConstants.JDBC_SELECT_GRAPGH + dbName + CommonConstants.SVCLOGIC_TABLE
                + CommonConstants.JDBC_GRAPH_QUERY;

        try {
            fetchVersionGraphStmt = dbConn.prepareStatement(fetchVersionGraphSql);
        } catch (Exception e) {
            throw new ConfigurationException(CommonConstants.JDBC_STATEMENT_ERR + fetchVersionGraphSql, e);

        }

        String fetchActiveGraphSql = CommonConstants.JDBC_SELECT_GRAPGH + dbName + CommonConstants.SVCLOGIC_TABLE
                + CommonConstants.JDBC_ACTIVE_GRAPH_QUERY;

        try {
            fetchActiveGraphStmt = dbConn.prepareStatement(fetchActiveGraphSql);
        } catch (Exception e) {
            throw new ConfigurationException(CommonConstants.JDBC_STATEMENT_ERR + fetchVersionGraphSql, e);

        }

        String storeGraphSql = CommonConstants.JDBC_INSERT + dbName
                + ".SVC_LOGIC (module, rpc, version, mode, active, graph, md5sum) VALUES(?, ?, ?, ?, ?, ?, ?)";

        try {
            storeGraphStmt = dbConn.prepareStatement(storeGraphSql);
        } catch (Exception e) {
            throw new ConfigurationException(CommonConstants.JDBC_STATEMENT_ERR + storeGraphSql, e);
        }

        String deleteGraphSql = CommonConstants.JDBC_DELETE + dbName
                + ".SVC_LOGIC WHERE module = ? AND rpc = ? AND version = ? AND mode = ?";

        try {
            deleteGraphStmt = dbConn.prepareStatement(deleteGraphSql);
        } catch (Exception e) {
            throw new ConfigurationException(CommonConstants.JDBC_STATEMENT_ERR + deleteGraphSql, e);
        }

        String deactivateSql = CommonConstants.JDBC_UPDATE + dbName
                + ".SVC_LOGIC SET active = 'N' WHERE module = ? AND rpc = ? AND mode = ?";

        try {
            deactivateStmt = dbConn.prepareStatement(deactivateSql);
        } catch (Exception e) {
            throw new ConfigurationException(CommonConstants.JDBC_STATEMENT_ERR + deactivateSql, e);
        }

        String activateSql = CommonConstants.JDBC_UPDATE + dbName
                + ".SVC_LOGIC SET active = 'Y' WHERE module = ? AND rpc = ? AND version = ? AND mode = ?";

        try {
            activateStmt = dbConn.prepareStatement(activateSql);
        } catch (Exception e) {
            throw new ConfigurationException(CommonConstants.JDBC_STATEMENT_ERR + activateSql, e);
        }
    }

    private void initDbResources() throws ConfigurationException {
        if ((dbDriver != null) && (dbDriver.length() > 0)) {

            try {
                Class.forName(dbDriver);
            } catch (Exception e) {
                throw new ConfigurationException("could not load driver class " + dbDriver, e);
            }
        }
        getConnection();
        createTable();
        prepStatements();
    }


    @Override
    public void init(Properties props) throws ConfigurationException {


        dbUrl = props.getProperty("org.onap.ccsdk.sli.jdbc.url");
        if ((dbUrl == null) || (dbUrl.length() == 0)) {
            throw new ConfigurationException("property org.onap.ccsdk.sli.jdbc.url unset");
        }

        dbName = props.getProperty("org.onap.ccsdk.sli.jdbc.database");
        if ((dbName == null) || (dbName.length() == 0)) {
            throw new ConfigurationException("property org.onap.ccsdk.sli.jdbc.database unset");
        }

        dbUser = props.getProperty("org.onap.ccsdk.sli.jdbc.user");
        if ((dbUser == null) || (dbUser.length() == 0)) {
            throw new ConfigurationException("property org.onap.ccsdk.sli.jdbc.user unset");
        }


        dbPasswd = props.getProperty("org.onap.ccsdk.sli.jdbc.password");
        if ((dbPasswd == null) || (dbPasswd.length() == 0)) {
            throw new ConfigurationException("property org.onap.ccsdk.sli.jdbc.password unset");
        }

        dbDriver = props.getProperty("org.onap.ccsdk.sli.jdbc.driver");


        initDbResources();

    }

    private boolean isDbConnValid() {

        boolean isValid = false;

        try {
            if (dbConn != null) {
                isValid = dbConn.isValid(1);
            }
        } catch (SQLException e) {
            LOG.error("Not a valid db connection: ", e);
        }

        return isValid;
    }

    @Override
    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {

        if (!isDbConnValid()) {

            // Try reinitializing
            initDbResources();

            if (!isDbConnValid()) {
                throw new ConfigurationException(CommonConstants.JDBC_CONN_ERR);
            }
        }

        boolean retval = false;
        ResultSet results = null;

        PreparedStatement hasGraphStmt;
        if (version == null) {
            hasGraphStmt = hasActiveGraphStmt;
        } else {
            hasGraphStmt = hasVersionGraphStmt;
        }

        try {
            hasGraphStmt.setString(1, module);
            hasGraphStmt.setString(2, rpc);
            hasGraphStmt.setString(3, mode);


            if (version != null) {
                hasGraphStmt.setString(4, version);
            }
            boolean oldAutoCommit = dbConn.getAutoCommit();
            dbConn.setAutoCommit(false);
            results = hasGraphStmt.executeQuery();
            dbConn.commit();
            dbConn.setAutoCommit(oldAutoCommit);

            if (results.next()) {
                int cnt = results.getInt(1);

                if (cnt > 0) {
                    retval = true;
                }

            }
        } catch (Exception e) {
            throw new ConfigurationException("SQL query failed", e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException x) {
                    LOG.error(CommonConstants.RESULTSET_CLOSE_ERR, x);
                }
            }

        }

        return retval;

    }

    @Override
    public SvcLogicGraph fetch(String module, String rpc, String version, String mode) throws SvcLogicException {


        if (!isDbConnValid()) {

            // Try reinitializing
            initDbResources();

            if (!isDbConnValid()) {
                throw new ConfigurationException(CommonConstants.JDBC_CONN_ERR);
            }
        }

        SvcLogicGraph retval = null;
        ResultSet results = null;

        PreparedStatement fetchGraphStmt;
        if (version == null) {
            fetchGraphStmt = fetchActiveGraphStmt;
        } else {
            fetchGraphStmt = fetchVersionGraphStmt;
        }
        try {
            fetchGraphStmt.setString(1, module);
            fetchGraphStmt.setString(2, rpc);
            fetchGraphStmt.setString(3, mode);


            if (version != null) {
                fetchGraphStmt.setString(4, version);
            }
            boolean oldAutoCommit = dbConn.getAutoCommit();
            dbConn.setAutoCommit(false);
            results = fetchGraphStmt.executeQuery();
            dbConn.commit();
            dbConn.setAutoCommit(oldAutoCommit);

            if (results.next()) {
                Blob graphBlob = results.getBlob("graph");

                ObjectInputStream gStream = new ObjectInputStream(graphBlob.getBinaryStream());

                Object graphObj = gStream.readObject();
                gStream.close();

                if (graphObj instanceof SvcLogicGraph) {
                    retval = (SvcLogicGraph) graphObj;
                } else {
                    throw new ConfigurationException("invalid type for graph (" + graphObj.getClass().getName());

                }
            }

        } catch (Exception e) {
            throw new ConfigurationException("SQL query failed", e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException x) {
                    LOG.error(CommonConstants.RESULTSET_CLOSE_ERR, x);
                }
            }

        }

        return retval;
    }

    public void store(SvcLogicGraph graph) throws SvcLogicException {


        if (!isDbConnValid()) {

            // Try reinitializing
            initDbResources();

            if (!isDbConnValid()) {
                throw new ConfigurationException(CommonConstants.JDBC_CONN_ERR);
            }
        }

        if (graph == null) {
            throw new SvcLogicException("graph cannot be null");
        }

        byte[] graphBytes;

        try (ByteArrayOutputStream byteStr = new ByteArrayOutputStream();
                ObjectOutputStream goutStr = new ObjectOutputStream(byteStr)) {

            goutStr.writeObject(graph);

            graphBytes = byteStr.toByteArray();

        } catch (Exception e) {
            throw new SvcLogicException("could not serialize graph", e);
        }

        // If object already stored in database, delete it
        if (hasGraph(graph.getModule(), graph.getRpc(), graph.getVersion(), graph.getMode())) {
            delete(graph.getModule(), graph.getRpc(), graph.getVersion(), graph.getMode());
        }

        try {
            boolean oldAutoCommit = dbConn.getAutoCommit();
            dbConn.setAutoCommit(false);
            storeGraphStmt.setString(1,  graph.getModule());
            storeGraphStmt.setString(2,  graph.getRpc());
            storeGraphStmt.setString(3, graph.getVersion());
            storeGraphStmt.setString(4, graph.getMode());
            storeGraphStmt.setString(5, "N");
            storeGraphStmt.setBlob(6,  new ByteArrayInputStream(graphBytes));
             storeGraphStmt.setString(7, graph.getMd5sum());

            storeGraphStmt.executeUpdate();
            dbConn.commit();
            
            dbConn.setAutoCommit(oldAutoCommit);
        } catch (Exception e) {
            throw new SvcLogicException("Could not write object to database", e);
        }
    }

    @Override
    public void delete(String module, String rpc, String version, String mode) throws SvcLogicException {
        if (!isDbConnValid()) {

            // Try reinitializing
            initDbResources();

            if (!isDbConnValid()) {
                throw new ConfigurationException(CommonConstants.JDBC_CONN_ERR);
            }
        }

        try {
            boolean oldAutoCommit = dbConn.getAutoCommit();
            dbConn.setAutoCommit(false);
            deleteGraphStmt.setString(1, module);
            deleteGraphStmt.setString(2, rpc);
            deleteGraphStmt.setString(3, version);
            deleteGraphStmt.setString(4, mode);


            deleteGraphStmt.executeUpdate();
            dbConn.commit();
            dbConn.setAutoCommit(oldAutoCommit);
        } catch (Exception e) {
            throw new SvcLogicException("Could not delete object from database", e);
        }
    }

    @Override
    public void activate(SvcLogicGraph graph) throws SvcLogicException {
        try {
            boolean oldAutoCommit = dbConn.getAutoCommit();

            dbConn.setAutoCommit(false);

            // Deactivate any current active version
            deactivateStmt.setString(1, graph.getModule());
            deactivateStmt.setString(2, graph.getRpc());
            deactivateStmt.setString(3, graph.getMode());
            deactivateStmt.executeUpdate();

            // Activate this version
            activateStmt.setString(1, graph.getModule());
            activateStmt.setString(2, graph.getRpc());
            activateStmt.setString(3, graph.getVersion());
            activateStmt.setString(4, graph.getMode());
            activateStmt.executeUpdate();

            dbConn.commit();

            dbConn.setAutoCommit(oldAutoCommit);

        } catch (Exception e) {
            throw new SvcLogicException("Could not activate graph", e);
        }
    }

    @Override
    public void activate(String module, String rpc, String version, String mode) throws SvcLogicException {
        try {
            boolean oldAutoCommit = dbConn.getAutoCommit();

            dbConn.setAutoCommit(false);

            // Deactivate any current active version
            deactivateStmt.setString(1, module);
            deactivateStmt.setString(2, rpc);
            deactivateStmt.setString(3, mode);
            deactivateStmt.executeUpdate();

            // Activate this version
            activateStmt.setString(1, module);
            activateStmt.setString(2, rpc);
            activateStmt.setString(3, version);
            activateStmt.setString(4, mode);
            activateStmt.executeUpdate();

            dbConn.commit();

            dbConn.setAutoCommit(oldAutoCommit);

        } catch (Exception e) {
            throw new SvcLogicException("Could not activate graph", e);
        }
    }


}
