/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
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

package org.onap.ccsdk.sli.core.dblib;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.apache.tomcat.jdbc.pool.PoolExhaustedException;
import org.onap.ccsdk.sli.core.dblib.config.BaseDBConfiguration;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitor;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitor.TestObject;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitorObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision: 1.14 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */

public abstract class CachedDataSource implements DataSource, SQLExecutionMonitorObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedDataSource.class);

    private static final String SQL_FAILURE = "SQL FAILURE. time(ms): ";
    private static final String FAILED_TO_EXECUTE = "> Failed to execute: ";
    private static final String WITH_ARGUMENTS = " with arguments: ";
    private static final String WITH_NO_ARGUMENTS = " with no arguments. ";
    private static final String DATA_SOURCE_CONNECT_SUCCESS = "SQL DataSource < {} > connected to {}, read-only is {}, tested successfully";
    private static final String DATA_SOURCE_CONNECT_FAILURE = "SQL DataSource < {} > test failed. Cause : {}> test failed. Cause : {}";

    protected long connReqTimeout = 30L;
    protected long dataReqTimeout = 100L;

    private final SQLExecutionMonitor monitor;
    protected final DataSource ds;
    protected String connectionName = null;
    protected boolean initialized = false;

    private long interval = 1000;
    private long initialDelay = 5000;
    private long expectedCompletionTime = 50L;
    private boolean canTakeOffLine = true;
    private long unprocessedFailoverThreshold = 3L;

    private long nextErrorReportTime = 0L;

    private String globalHostName = null;
    private final int index;

    private boolean isDerby = false;

    public CachedDataSource(BaseDBConfiguration jdbcElem) throws DBConfigException {
        ds = configure(jdbcElem);
        index = initializeIndex(jdbcElem);
        if ("org.apache.derby.jdbc.EmbeddedDriver".equals(jdbcElem.getDriverName())) {
            isDerby = true;
        }
        monitor = new SQLExecutionMonitor(this);
    }

    protected abstract DataSource configure(BaseDBConfiguration jdbcElem) throws DBConfigException;
    protected abstract int getAvailableConnections();

    protected int initializeIndex(BaseDBConfiguration jdbcElem) {
        if(jdbcElem.containsKey(BaseDBConfiguration.DATABASE_HOSTS)) {
            String hosts = jdbcElem.getProperty(BaseDBConfiguration.DATABASE_HOSTS);
            String name = jdbcElem.getProperty(BaseDBConfiguration.CONNECTION_NAME);
            List<String> numbers = Arrays.asList(hosts.split(","));
            return numbers.indexOf(name);
        } else
            return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sql.DataSource#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException {
        LapsedTimer lt = new LapsedTimer();
        try {
        return ds.getConnection();
        } finally {
            if(LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("SQL Connection aquisition time : %s", lt.lapsedTime()));
            }
        }
    }

    public CachedRowSet getData(String statement, List<Object> arguments) throws SQLException {
        TestObject testObject = monitor.registerRequest();

        try (Connection connection = this.getConnection()) {
            if (connection == null) {
                throw new SQLException("Connection invalid");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Obtained connection <{}>: {}", connectionName, connection);
            }
            return executePreparedStatement(connection, statement, arguments, true);
        } finally {
            monitor.deregisterRequest(testObject);
        }
    }

    public boolean writeData(String statement, List<Object> arguments) throws SQLException {
        TestObject testObject = monitor.registerRequest();

        try (Connection connection = this.getConnection()) {
            if (connection == null) {
                throw new SQLException("Connection invalid");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Obtained connection <{}>: {}", connectionName, connection);
            }
            return executeUpdatePreparedStatement(connection, statement, arguments, true);
        } finally {
            monitor.deregisterRequest(testObject);
        }
    }

    CachedRowSet executePreparedStatement(Connection conn, String statement, List<Object> arguments, boolean close)
            throws SQLException {
        long time = System.currentTimeMillis();

        CachedRowSet data = null;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SQL Statement: {}", statement);
            if (arguments != null && !arguments.isEmpty()) {
                LOGGER.debug("Argunments: {}", arguments);
            }
        }

        ResultSet rs = null;
        try (PreparedStatement ps = conn.prepareStatement(statement)) {
            data = RowSetProvider.newFactory().createCachedRowSet();
            if (arguments != null) {
                for (int i = 0, max = arguments.size(); i < max; i++) {
                    ps.setObject(i + 1, arguments.get(i));
                }
            }
            rs = ps.executeQuery();
            data.populate(rs);
            // Point the rowset Cursor to the start
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SQL SUCCESS. rows returned: {}, time(ms): {}", data.size(),
                        (System.currentTimeMillis() - time));
            }
        } catch (SQLException exc) {
            handleSqlExceptionForExecuteStatement(conn, statement, arguments, exc, time);
        } finally {
            handleFinallyBlockForExecutePreparedStatement(rs, conn, close);
        }

        return data;
    }

    private void handleSqlExceptionForExecuteStatement(Connection conn, String statement, List<Object> arguments,
            SQLException exc, long time) throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SQL_FAILURE + (System.currentTimeMillis() - time));
        }
        try {
            conn.rollback();
        } catch (Exception thr) {
            LOGGER.error(thr.getLocalizedMessage(), thr);
        }
        if (arguments != null && !arguments.isEmpty()) {
            LOGGER.error(String.format("<%s%s%s%s%s", connectionName, FAILED_TO_EXECUTE, statement, WITH_ARGUMENTS,
                    arguments), exc);
        } else {
            LOGGER.error(String.format("<%s%s%s%s", connectionName, FAILED_TO_EXECUTE, statement, WITH_NO_ARGUMENTS),
                    exc);
        }
        throw exc;
    }

    private void handleFinallyBlockForExecutePreparedStatement(ResultSet rs, Connection conn, boolean close) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception exc) {
            LOGGER.error(exc.getLocalizedMessage(), exc);
        }
        try {
            if (conn != null && close) {
                conn.close();
            }
        } catch (Exception exc) {
            LOGGER.error(exc.getLocalizedMessage(), exc);
        }
    }

    boolean executeUpdatePreparedStatement(Connection conn, String statement, List<Object> arguments, boolean close)
            throws SQLException {
        long time = System.currentTimeMillis();

        try (PreparedStatement ps = conn.prepareStatement(statement);
            CachedRowSet data = RowSetProvider.newFactory().createCachedRowSet()) {
            if (arguments != null) {
                prepareStatementForExecuteUpdate(arguments, ps);
            }
            ps.executeUpdate();
            // Point the rowset Cursor to the start
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SQL SUCCESS. rows returned: {}, time(ms): {}", data.size(),
                        (System.currentTimeMillis() - time));
            }
        } catch (SQLException exc) {
            handleSqlExceptionForExecuteStatement(conn, statement, arguments, exc, time);
        } finally {
            try {
                if (close) {
                    conn.close();
                }
            } catch (Exception exc) {
                LOGGER.error(exc.getLocalizedMessage(), exc);
            }
        }

        return true;
    }

    private void prepareStatementForExecuteUpdate(List<Object> arguments, PreparedStatement ps) throws SQLException {
        for (int i = 0, max = arguments.size(); i < max; i++) {
            Object value = arguments.get(i);
            if (value instanceof Blob) {
                ps.setBlob(i + 1, (Blob) value);
            } else if (value instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) value);
            } else if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else if (value instanceof Long) {
                ps.setLong(i + 1, (Long) value);
            } else if (value instanceof Date) {
                ps.setDate(i + 1, (Date) value);
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return ds.getConnection(username, password);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sql.DataSource#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return ds.getLogWriter();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sql.DataSource#getLoginTimeout()
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return ds.getLoginTimeout();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        ds.setLogWriter(out);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sql.DataSource#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        ds.setLoginTimeout(seconds);
    }

    @Override
    public final String getDbConnectionName() {
        return connectionName;
    }

    protected final void setDbConnectionName(String name) {
        this.connectionName = name;
    }

    public void cleanUp() {
        if (ds != null && ds instanceof Closeable) {
            try {
                ((Closeable) ds).close();
            } catch (IOException e) {
                LOGGER.warn(e.getMessage());
            }
        }
        monitor.deleteObservers();
        monitor.cleanup();
    }

    public boolean isInitialized() {
        return initialized;
    }

    protected boolean testConnection() {
        return testConnection(false);
    }

    protected boolean testConnection(boolean errorLevel) {

        String testQuery = "SELECT @@global.read_only, @@global.hostname";
        if (isDerby) {
            testQuery = "SELECT 'false', 'localhost' FROM SYSIBM.SYSDUMMY1";
        }
        ResultSet rs = null;
        try (Connection conn = this.getConnection(); Statement stmt = conn.createStatement()) {
            Boolean readOnly;
            String hostname;
            rs = stmt.executeQuery(testQuery); // ("SELECT 1 FROM DUAL"); //"select
                                                                                    // BANNER from SYS.V_$VERSION"
            while (rs.next()) {
                readOnly = rs.getBoolean(1);
                hostname = rs.getString(2);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(DATA_SOURCE_CONNECT_SUCCESS,getDbConnectionName(),hostname,readOnly);
                }
            }
        } catch (Exception exc) {
            if (errorLevel) {
                LOGGER.error(DATA_SOURCE_CONNECT_FAILURE, this.getDbConnectionName(),exc.getMessage());
            } else {
                LOGGER.info(DATA_SOURCE_CONNECT_FAILURE, this.getDbConnectionName(),exc.getMessage());
            }
            return false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return true;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public void addObserver(Observer observer) {
        monitor.addObserver(observer);
    }

    public void deleteObserver(Observer observer) {
        monitor.deleteObserver(observer);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public long getInterval() {
        return interval;
    }

    @Override
    public long getInitialDelay() {
        return initialDelay;
    }

    @Override
    public void setInterval(long value) {
        interval = value;
    }

    @Override
    public void setInitialDelay(long value) {
        initialDelay = value;
    }

    @Override
    public long getExpectedCompletionTime() {
        return expectedCompletionTime;
    }

    @Override
    public void setExpectedCompletionTime(long value) {
        expectedCompletionTime = value;
    }

    @Override
    public long getUnprocessedFailoverThreshold() {
        return unprocessedFailoverThreshold;
    }

    @Override
    public void setUnprocessedFailoverThreshold(long value) {
        this.unprocessedFailoverThreshold = value;
    }

    public boolean canTakeOffLine() {
        return canTakeOffLine;
    }

    public void blockImmediateOffLine() {
        canTakeOffLine = false;
        final Thread offLineTimer = new Thread(() -> {
            try {
                Thread.sleep(30000L);
            } catch (Exception exc) {
                LOGGER.error(exc.getLocalizedMessage(), exc);
            } finally {
                canTakeOffLine = true;
            }
        });
        offLineTimer.setDaemon(true);
        offLineTimer.start();
    }

    /**
     * @return the monitor
     */
    final SQLExecutionMonitor getMonitor() {
        return monitor;
    }

    protected boolean isSlave() throws PoolExhaustedException {

        // If using Apache derby, just return false
        if (isDerby) {
            return false;
        }
        CachedRowSet rs;
        boolean isSlave;
        String hostname = "UNDETERMINED";
        try {
            boolean localSlave = true;
            rs = this.getData("SELECT @@global.read_only, @@global.hostname", new ArrayList<>());
            while (rs.next()) {
                localSlave = rs.getBoolean(1);
                hostname = rs.getString(2);
            }
            isSlave = localSlave;
        } catch (PoolExhaustedException peexc) {
            throw peexc;
        } catch (Exception e) {
            LOGGER.error("", e);
            isSlave = true;
        }
        if (isSlave) {
            LOGGER.debug("SQL SLAVE : {} on server {}, pool {}", connectionName, getDbConnectionName(), getAvailableConnections());
        } else {
            LOGGER.debug("SQL MASTER : {} on server {}, pool {}", connectionName, getDbConnectionName(), getAvailableConnections());
        }
        return isSlave;
    }

    public boolean isFabric() {
        return false;
    }

    protected boolean lockTable(Connection conn, String tableName) {
        boolean retValue = false;
        String query = "LOCK TABLES " + tableName + " WRITE";
        try (Statement preStmt = conn.createStatement();
             Statement lock = conn.prepareStatement(query)) {
            if (tableName != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Executing 'LOCK TABLES " + tableName + " WRITE' on connection " + conn.toString());
                    if ("SVC_LOGIC".equals(tableName)) {
                        Exception e = new Exception();
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        LOGGER.debug(sw.toString());
                    }
                }
                lock.execute(query);
                retValue = true;
            }
        } catch (Exception exc) {
            LOGGER.error("", exc);
            retValue = false;
        }
        return retValue;
    }

    protected boolean unlockTable(Connection conn) {
        boolean retValue;
        try (Statement lock = conn.createStatement()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Executing 'UNLOCK TABLES' on connection {}", conn);
            }
            retValue = lock.execute("UNLOCK TABLES");
        } catch (Exception exc) {
            LOGGER.error("", exc);
            retValue = false;
        }
        return retValue;
    }

    public void getPoolInfo(boolean allocation) {

    }

    public long getNextErrorReportTime() {
        return nextErrorReportTime;
    }

    public void setNextErrorReportTime(long nextTime) {
        this.nextErrorReportTime = nextTime;
    }

    public void setGlobalHostName(String hostname) {
        this.globalHostName = hostname;
    }

    public String getGlobalHostName() {
        return globalHostName;
    }

    static class LapsedTimer {
        private final long msTime = System.currentTimeMillis();
 
        public String lapsedTime() {
            double timediff = System.currentTimeMillis() - msTime;
            timediff = timediff/1000;
            return String.valueOf( timediff)+"s";
        }
    }
}
