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

import org.apache.tomcat.jdbc.pool.PoolExhaustedException;
import org.onap.ccsdk.sli.core.dblib.config.BaseDBConfiguration;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitor;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitor.TestObject;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitorObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
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
import java.util.Observer;


/**
 * @version $Revision: 1.13 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */

public abstract class CachedDataSource implements DataSource, SQLExecutionMonitorObserver
{
	private static Logger LOGGER = LoggerFactory.getLogger(CachedDataSource.class);

	protected static final String AS_CONF_ERROR = "AS_CONF_ERROR: ";

	protected long CONN_REQ_TIMEOUT = 30L;
	protected long DATA_REQ_TIMEOUT = 100L;

	private final SQLExecutionMonitor monitor;
	protected DataSource ds = null;
	protected String connectionName = null;
	protected boolean initialized = false;

	private long interval = 1000;
	private long initialDelay = 5000;
	private long expectedCompletionTime = 50L;
	private boolean canTakeOffLine = true;
	private long unprocessedFailoverThreshold = 3L;

	private long nextErrorReportTime = 0L;

	private String globalHostName = null;


	public CachedDataSource(BaseDBConfiguration jdbcElem) throws DBConfigException
	{
		configure(jdbcElem);
		monitor = new SQLExecutionMonitor(this);
	}

	protected abstract void configure(BaseDBConfiguration jdbcElem) throws DBConfigException;
	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException
	{
		return ds.getConnection();
	}

	public CachedRowSet getData(String statement, ArrayList<Object> arguments)
			throws SQLException, Throwable
	{
		TestObject testObject = null;
		testObject = monitor.registerRequest();

		Connection connection = null;
		try {
			connection = this.getConnection();
			if(connection ==  null ) {
				throw new SQLException("Connection invalid");
			}
			if(LOGGER.isDebugEnabled())
				LOGGER.debug("Obtained connection <" + connectionName + ">: "+connection.toString());
			return executePreparedStatement(connection, statement, arguments, true);
		} finally {
			try {
				if(connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch(Throwable exc) {
				// the exception not monitored
			} finally {
				connection = null;
			}

			monitor.deregisterRequest(testObject);
		}
	}

	public boolean writeData(String statement, ArrayList<Object> arguments)
			throws SQLException, Throwable
	{
		TestObject testObject = null;
		testObject = monitor.registerRequest();

		Connection connection = null;
		try {
			connection = this.getConnection();
			if(connection ==  null ) {
				throw new SQLException("Connection invalid");
			}
			if(LOGGER.isDebugEnabled())
				LOGGER.debug("Obtained connection <" + connectionName + ">: "+connection.toString());
			return executeUpdatePreparedStatement(connection, statement, arguments, true);
		} finally {
			try {
				if(connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch(Throwable exc) {
				// the exception not monitored
			} finally {
				connection = null;
			}

			monitor.deregisterRequest(testObject);
		}
	}

	CachedRowSet executePreparedStatement(Connection conn, String statement,
			ArrayList<Object> arguments, boolean close) throws SQLException, Throwable
	{
		long time = System.currentTimeMillis();

		CachedRowSet data = null;
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("SQL Statement: "+ statement);
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.debug("Argunments: "+ Arrays.toString(arguments.toArray()));
			}
		}

		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			data = RowSetProvider.newFactory().createCachedRowSet();
			ps = conn.prepareStatement(statement);
			if(arguments != null)
			{
				for(int i = 0, max = arguments.size(); i < max; i++){
					ps.setObject(i+1, arguments.get(i));
				}
			}
			rs = ps.executeQuery();
			data.populate(rs);
		    // Point the rowset Cursor to the start
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL SUCCESS. rows returned: " + data.size()+ ", time(ms): "+ (System.currentTimeMillis() - time));			}
		} catch(SQLException exc){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL FAILURE. time(ms): "+ (System.currentTimeMillis() - time));
			}
			try {	conn.rollback(); } catch(Throwable thr){}
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with arguments: "+arguments.toString(), exc);
			} else {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with no arguments. ", exc);
			}
			throw exc;
		} catch(Throwable exc){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL FAILURE. time(ms): "+ (System.currentTimeMillis() - time));
			}
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with arguments: "+arguments.toString(), exc);
			} else {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with no arguments. ", exc);
			}
			throw exc; // new SQLException(exc);
		} finally {

			try {
				if(rs != null){
					rs.close();
					rs = null;
				}
			} catch(Exception exc){

			}
			try {
				if(conn != null && close){
					conn.close();
					conn = null;
				}
			} catch(Exception exc){

			}
			try {
				if (ps != null){
					ps.close();
				}
			} catch (Exception exc){

			}
		}

		return data;
	}

	boolean executeUpdatePreparedStatement(Connection conn, String statement, ArrayList<Object> arguments, boolean close) throws SQLException, Throwable {
		long time = System.currentTimeMillis();

		CachedRowSet data = null;

		int rs = -1;
		try {
			data = RowSetProvider.newFactory().createCachedRowSet();
			PreparedStatement ps = conn.prepareStatement(statement);
			if(arguments != null)
			{
				for(int i = 0, max = arguments.size(); i < max; i++){
					if(arguments.get(i) instanceof Blob) {
						ps.setBlob(i+1, (Blob)arguments.get(i));
					} else	if(arguments.get(i) instanceof Timestamp) {
						ps.setTimestamp(i+1, (Timestamp)arguments.get(i));
					} else	if(arguments.get(i) instanceof Integer) {
						ps.setInt(i+1, (Integer)arguments.get(i));
					} else	if(arguments.get(i) instanceof Long) {
						ps.setLong(i+1, (Long)arguments.get(i));
					} else	if(arguments.get(i) instanceof Date) {
						ps.setDate(i+1, (Date)arguments.get(i));
					} else {
					ps.setObject(i+1, arguments.get(i));
				}
			}
			}
			rs = ps.executeUpdate();
		    // Point the rowset Cursor to the start
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL SUCCESS. rows returned: " + data.size()+ ", time(ms): "+ (System.currentTimeMillis() - time));
			}
		} catch(SQLException exc){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL FAILURE. time(ms): "+ (System.currentTimeMillis() - time));
			}
			try {	conn.rollback(); } catch(Throwable thr){}
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with arguments: "+arguments.toString(), exc);
			} else {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with no arguments. ", exc);
			}
			throw exc;
		} catch(Throwable exc){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL FAILURE. time(ms): "+ (System.currentTimeMillis() - time));
			}
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with arguments: "+arguments.toString(), exc);
			} else {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with no arguments. ", exc);
			}
			throw exc; // new SQLException(exc);
		} finally {
			try {
				if(conn != null && close){
					conn.close();
					conn = null;
				}
			} catch(Exception exc){

			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public Connection getConnection(String username, String password)
			throws SQLException
	{
		return ds.getConnection(username, password);
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getLogWriter()
	 */
	@Override
	public PrintWriter getLogWriter() throws SQLException
	{
		return ds.getLogWriter();
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getLoginTimeout()
	 */
	@Override
	public int getLoginTimeout() throws SQLException
	{
		return ds.getLoginTimeout();
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
	 */
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException
	{
		ds.setLogWriter(out);
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#setLoginTimeout(int)
	 */
	@Override
	public void setLoginTimeout(int seconds) throws SQLException
	{
		ds.setLoginTimeout(seconds);
	}


	@Override
	public final String getDbConnectionName(){
		return connectionName;
	}

	protected final void setDbConnectionName(String name) {
		this.connectionName = name;
	}

	public void cleanUp(){
		if(ds != null && ds instanceof Closeable) {
			try {
				((Closeable)ds).close();
			} catch (IOException e) {
				LOGGER.warn(e.getMessage());
			}
		}
		ds = null;
		monitor.deleteObservers();
		monitor.cleanup();
	}

	public boolean isInitialized() {
		return initialized;
	}

	protected boolean testConnection(){
		return testConnection(false);
	}

	protected boolean testConnection(boolean error_level){
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		try
		{
			Boolean readOnly = null;
			String hostname = null;
			conn = this.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT @@global.read_only, @@global.hostname");   //("SELECT 1 FROM DUAL"); //"select BANNER from SYS.V_$VERSION"
			while(rs.next())
			{
				readOnly = rs.getBoolean(1);
				hostname = rs.getString(2);

					if(LOGGER.isDebugEnabled()){
						LOGGER.debug("SQL DataSource <"+getDbConnectionName() + "> connected to " + hostname + ", read-only is " + readOnly + ", tested successfully ");
					}
			}

		} catch (Throwable exc) {
			if(error_level) {
				LOGGER.error("SQL DataSource <" + this.getDbConnectionName() +	"> test failed. Cause : " + exc.getMessage());
			} else {
				LOGGER.info("SQL DataSource <" + this.getDbConnectionName() +	"> test failed. Cause : " + exc.getMessage());
			}
			return false;
		} finally {
			if(rs != null) {
				try {
					rs.close();
					rs = null;
				} catch (SQLException e) {
				}
			}
			if(stmt != null) {
				try {
					stmt.close();
					stmt = null;
				} catch (SQLException e) {
				}
			}
			if(conn !=null){
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
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

	@SuppressWarnings("deprecation")
	public void setConnectionCachingEnabled(boolean state)
	{
//		if(ds != null && ds instanceof OracleDataSource)
//			try {
//				((OracleDataSource)ds).setConnectionCachingEnabled(true);
//			} catch (SQLException exc) {
//				LOGGER.warn("", exc);
//			}
	}

	public void addObserver(Observer observer) {
		monitor.addObserver(observer);
	}

	public void deleteObserver(Observer observer) {
		monitor.deleteObserver(observer);
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
		final Thread offLineTimer = new Thread()
		{
			@Override
			public void run(){
				try {
					Thread.sleep(30000L);
				}catch(Throwable exc){

				}finally{
					canTakeOffLine = true;
				}
			}
		};
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
		CachedRowSet rs = null;
		boolean isSlave = true;
		String hostname = "UNDETERMINED";
		try {
			boolean localSlave = true;
			rs = this.getData("SELECT @@global.read_only, @@global.hostname", new ArrayList<Object>());
			while(rs.next()) {
				localSlave = rs.getBoolean(1);
				hostname = rs.getString(2);
			}
			isSlave = localSlave;
		} catch(PoolExhaustedException peexc){
			throw peexc;
		} catch (SQLException e) {
			LOGGER.error("", e);
			isSlave = true;
		} catch (Throwable e) {
			LOGGER.error("", e);
			isSlave = true;
		}
		if(isSlave){
			LOGGER.debug("SQL SLAVE : "+connectionName + " on server " + hostname);
		} else {
			LOGGER.debug("SQL MASTER : "+connectionName + " on server " + hostname);
		}
		return isSlave;
	}

	public boolean isFabric() {
		return false;
	}

	protected boolean lockTable(Connection conn, String tableName) {
		boolean retValue = false;
		Statement lock = null;
		try {
			if(tableName != null) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("Executing 'LOCK TABLES " + tableName + " WRITE' on connection " + conn.toString());
					if("SVC_LOGIC".equals(tableName)) {
						Exception e = new Exception();
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						LOGGER.debug(sw.toString());
					}
				}
				lock = conn.createStatement();
				lock.execute("LOCK TABLES " + tableName + " WRITE");
				retValue = true;
			}
		} catch(Exception exc){
			LOGGER.error("", exc);
			retValue =  false;
		} finally {
			try {
                            if (lock != null) {
                                lock.close();
                            }
			} catch(Exception exc) {

			}
		}
		return retValue;
	}

	protected boolean unlockTable(Connection conn) {
		boolean retValue = false;
		try (Statement lock = conn.createStatement()){
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Executing 'UNLOCK TABLES' on connection " + conn.toString());
			}
			retValue = lock.execute("UNLOCK TABLES");
		} catch(Exception exc){
			LOGGER.error("", exc);
			retValue =  false;
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
		this.globalHostName  = hostname;
	}

	public String getGlobalHostName() {
		return globalHostName;
	}
}
