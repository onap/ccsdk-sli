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
import org.onap.ccsdk.sli.core.dblib.config.DbConfigPool;
import org.onap.ccsdk.sli.core.dblib.factory.DBConfigFactory;
import org.onap.ccsdk.sli.core.dblib.pm.PollingWorker;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.onap.ccsdk.sli.core.dblib.config.JDBCConfiguration;

/**
 * @version $Revision: 1.15 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */
public class DBResourceManager implements DataSource, DataAccessor, DBResourceObserver, DbLibService {
	private static Logger LOGGER = LoggerFactory.getLogger(DBResourceManager.class);

	transient boolean terminating = false;
	transient protected long retryInterval = 10000L;
	transient boolean recoveryMode = true;

	protected final AtomicBoolean dsSelector = new  AtomicBoolean();

//	Queue<CachedDataSource> dsQueue = new ConcurrentLinkedQueue<CachedDataSource>();
	Queue<CachedDataSource> dsQueue = new PriorityQueue<>(4, new Comparator<CachedDataSource>() {
	@Override
	public int compare(CachedDataSource left, CachedDataSource right) {
		try {
			if (left == null) {
				return 1;
			}
			if (right == null) {
				return -1;
			}

			if (!left.isSlave()) {
				return -1;
			}
			if (!right.isSlave()) {
				return 1;
			}
		} catch (Throwable e) {
			LOGGER.warn("", e);
		}
		return 0;
	}
});
	protected final Set<CachedDataSource> broken = Collections.synchronizedSet(new HashSet<CachedDataSource>());
	protected final Object monitor = new Object();
	protected final Properties configProps;
	protected final Thread worker;

	protected final long terminationTimeOut;
	protected final boolean monitorDbResponse;
	protected final long monitoringInterval;
	protected final long monitoringInitialDelay;
	protected final long expectedCompletionTime;
	protected final long unprocessedFailoverThreshold;

	public DBResourceManager(final DBLIBResourceProvider configuration) {
		this(configuration.getProperties());
	}

	public DBResourceManager(final Properties properties) {
		this.configProps = properties;

		// get retry interval value
		retryInterval = getLongFromProperties(properties, "org.onap.dblib.connection.retry", 10000L);

		// get recovery mode flag
		recoveryMode = getBooleanFromProperties(properties, "org.onap.dblib.connection.recovery", true);
		if(!recoveryMode)
		{
			recoveryMode = false;
			LOGGER.info("Recovery Mode disabled");
		}
		// get time out value for thread cleanup
		terminationTimeOut = getLongFromProperties(properties, "org.onap.dblib.termination.timeout", 300000L);
		// get properties for monitoring
		monitorDbResponse = getBooleanFromProperties(properties, "org.onap.dblib.connection.monitor", false);
		monitoringInterval = getLongFromProperties(properties, "org.onap.dblib.connection.monitor.interval", 1000L);
		monitoringInitialDelay = getLongFromProperties(properties, "org.onap.dblib.connection.monitor.startdelay", 5000L);
		expectedCompletionTime = getLongFromProperties(properties, "org.onap.dblib.connection.monitor.expectedcompletiontime", 5000L);
		unprocessedFailoverThreshold = getLongFromProperties(properties, "org.onap.dblib.connection.monitor.unprocessedfailoverthreshold", 3L);

		// initialize performance monitor
		PollingWorker.createInistance(properties);

		// initialize recovery thread
		worker = new RecoveryMgr();
		worker.setName("DBResourcemanagerWatchThread");
		worker.setDaemon(true);
		worker.start();

		try {
			this.config(properties);
		} catch (final Exception e) {
			// TODO: config throws <code>Exception</code> which is poor practice.  Eliminate this in a separate patch.
			LOGGER.error("Fatal Exception encountered while configuring DBResourceManager", e);
		}
	}

	private void config(Properties configProps) throws Exception {
		final ConcurrentLinkedQueue<CachedDataSource> semaphore = new ConcurrentLinkedQueue<>();
		final DbConfigPool dbConfig = DBConfigFactory.createConfig(configProps);

		long startTime = System.currentTimeMillis();

		try {
			JDBCConfiguration[] config = dbConfig.getJDBCbSourceArray();
			CachedDataSource[] cachedDS = new CachedDataSource[config.length];
			if (cachedDS == null || cachedDS.length == 0) {
				LOGGER.error("Initialization of CachedDataSources failed. No instance was created.");
				throw new Exception("Failed to initialize DB Library. No data source was created.");
			}

			for(int i = 0; i < config.length; i++) {
				cachedDS[i] = CachedDataSourceFactory.createDataSource(config[i]);
				if(cachedDS[i] == null)
					continue;
				semaphore.add(cachedDS[i]);
					cachedDS[i].setInterval(monitoringInterval);
					cachedDS[i].setInitialDelay(monitoringInitialDelay);
					cachedDS[i].setExpectedCompletionTime(expectedCompletionTime);
					cachedDS[i].setUnprocessedFailoverThreshold(unprocessedFailoverThreshold);
				cachedDS[i].addObserver(DBResourceManager.this);
			}

//					CachedDataSource[] cachedDS = factory.initDBResourceManager(dbConfig, DBResourceManager.this, semaphore);
			DataSourceTester[] tester = new DataSourceTester[config.length];

			for(int i=0; i<tester.length; i++){
				tester[i] = new DataSourceTester(cachedDS[i], DBResourceManager.this, semaphore);
				tester[i].start();
				}

			// the timeout param is set is seconds.
			long timeout = ((dbConfig.getTimeout() <= 0) ? 60L : dbConfig.getTimeout());
			LOGGER.debug("Timeout set to " +timeout+" seconds");
			timeout *= 1000;


			synchronized (semaphore) {
				semaphore.wait(timeout);
			}
		} catch(Exception exc){
			LOGGER.warn("DBResourceManager.initWorker", exc);
		} finally {
			startTime = System.currentTimeMillis() - startTime;
			LOGGER.info("Completed wait with "+ dsQueue.size() + " active datasource(s) in " + startTime + " ms");
		}
	}


	class DataSourceTester extends Thread {

		private final CachedDataSource ds;
		private final DBResourceManager manager;
		private final ConcurrentLinkedQueue<CachedDataSource> semaphoreQ;

		public DataSourceTester(CachedDataSource ds, DBResourceManager manager, ConcurrentLinkedQueue<CachedDataSource> semaphore) {
			this.ds = ds;
			this.manager = manager;
			this.semaphoreQ = semaphore;
		}

		@Override
		public void run() {
			manager.setDataSource(ds);
			boolean slave = true;
			if(ds != null) {
				try {
					slave = ds.isSlave();
				} catch (Exception exc) {
					LOGGER.warn("", exc);
				}
			}
			if(!slave) {
				LOGGER.info(String.format("Adding MASTER (%s) to active queue", ds.getDbConnectionName()));
				try {
					synchronized (semaphoreQ) {
						semaphoreQ.notifyAll();
					}
				} catch(Exception exc) {
					LOGGER.warn("", exc);
				}
		}
			try {
				synchronized (semaphoreQ) {
					semaphoreQ.remove(ds);
				}
				if(semaphoreQ.isEmpty()) {
					synchronized (semaphoreQ) {
						semaphoreQ.notifyAll();
					}
				}
			} catch(Exception exc) {
				LOGGER.warn("", exc);
			}
			LOGGER.info(String.format("Thread DataSourceTester terminated %s for %s", this.getName(), ds.getDbConnectionName()));
		}

	}


	private long getLongFromProperties(Properties props, String property, long defaultValue)
	{
		String value = null;
		long tmpLongValue = defaultValue;
		try {
			value = props.getProperty(property);
			if(value != null)
				tmpLongValue = Long.parseLong(value);

		} catch(NumberFormatException exc) {
			if(LOGGER.isWarnEnabled()){
				LOGGER.warn("'"+property+"'=" + value+" is invalid. It should be a numeric value");
			}
		} catch(Exception exc) {
		}
		return tmpLongValue;

	}

	private boolean getBooleanFromProperties(Properties props, String property, boolean defaultValue)
	{
		boolean tmpValue = defaultValue;
		String value = null;

		try {
			value = props.getProperty(property);
			if(value != null)
				tmpValue = Boolean.parseBoolean(value);

		} catch(NumberFormatException exc) {
			if(LOGGER.isWarnEnabled()){
				LOGGER.warn("'"+property+"'=" + value+" is invalid. It should be a boolean value");
			}
		} catch(Exception exc) {
		}
		return tmpValue;

	}


	@Override
	public void update(Observable observable, Object data) {
		// if observable is active and there is a standby available, switch
		if(observable instanceof SQLExecutionMonitor)
		{
			SQLExecutionMonitor monitor = (SQLExecutionMonitor)observable;
			if(monitor.getParent() instanceof CachedDataSource)
			{
				CachedDataSource dataSource = (CachedDataSource)monitor.getParent();
				if(dataSource == dsQueue.peek())
				{
					if(recoveryMode && dsQueue.size() > 1){
						handleGetConnectionException(dataSource, new Exception(data.toString()));
					}
				}
			}
		}
	}

	public void testForceRecovery()
	{
		CachedDataSource active = this.dsQueue.peek();
		handleGetConnectionException(active, new Exception("test"));
	}

	class RecoveryMgr extends Thread {

		@Override
		public void run() {
			while(!terminating)
			{
				try {
					Thread.sleep(retryInterval);
				} catch (InterruptedException e1) {	}
				CachedDataSource brokenSource = null;
				try {
					if (!broken.isEmpty()) {
						CachedDataSource[] sourceArray = broken.toArray(new CachedDataSource[0]);
						for (int i = 0; i < sourceArray.length; i++)
						{
							brokenSource = sourceArray[i];
							if (brokenSource instanceof TerminatingCachedDataSource)
								break;
							if (resetConnectionPool(brokenSource)) {
								broken.remove(brokenSource);
								brokenSource.blockImmediateOffLine();
								dsQueue.add(brokenSource);
								LOGGER.info("DataSource <"
										+ brokenSource.getDbConnectionName()
										+ "> recovered.");
							}
							brokenSource = null;
						}
					}
				} catch (Exception exc) {
					LOGGER.warn(exc.getMessage());
					if(brokenSource != null){
						try {
							if(!broken.contains(brokenSource))
								broken.add(brokenSource);
							brokenSource = null;
						} catch (Exception e1) {	}
					}
				}
			}
			LOGGER.info("DBResourceManager.RecoveryMgr <"+this.toString() +"> terminated." );
		}

		private boolean resetConnectionPool(CachedDataSource dataSource){
			try {
				return dataSource.testConnection();
			} catch (Exception exc) {
				LOGGER.info("DataSource <" + dataSource.getDbConnectionName() + "> resetCache failed with error: "+ exc.getMessage());
				return false;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.resource.dblib.DbLibService#getData(java.lang.String, java.util.ArrayList, java.lang.String)
	 */
	@Override
	public CachedRowSet getData(String statement, ArrayList<String> arguments, String preferredDS) throws SQLException {
		ArrayList<Object> newList= new ArrayList<>();
		if(arguments != null && !arguments.isEmpty()) {
			newList.addAll(arguments);
		}
		if(recoveryMode)
			return requestDataWithRecovery(statement, newList, preferredDS);
		else
			return requestDataNoRecovery(statement, newList, preferredDS);
	}

	private CachedRowSet requestDataWithRecovery(String statement, ArrayList<Object> arguments, String preferredDS) throws SQLException {
		Throwable lastException = null;
		CachedDataSource active = null;

		// test if there are any connection pools available
		LinkedList<CachedDataSource> sources = new LinkedList<>(this.dsQueue);
		if(sources.isEmpty()){
			LOGGER.error("Generated alarm: DBResourceManager.getData - No active DB connection pools are available.");
			throw new DBLibException("No active DB connection pools are available in RequestDataWithRecovery call.");
		}
		if(preferredDS != null && !sources.peek().getDbConnectionName().equals(preferredDS)) {
			Collections.reverse(sources);
		}


		// loop through available data sources to retrieve data.
		while(!sources.isEmpty())
		{
			active = sources.peek();

			long time = System.currentTimeMillis();
			try {
				if(!active.isFabric()) {
					CachedDataSource master = findMaster();
					if(master != null) {
						active = master;
						master = null;
					}
				}
				sources.remove(active);
				return active.getData(statement, arguments);
			} catch(SQLDataException | SQLSyntaxErrorException | SQLIntegrityConstraintViolationException exc){
				throw exc;
			} catch(Throwable exc){
				lastException = exc;
				String message = exc.getMessage();
				if(message == null) {
					if(exc.getCause() != null) {
						message = exc.getCause().getMessage();
					}
					if(message == null)
						message = exc.getClass().getName();
				}
				LOGGER.error("Generated alarm: "+active.getDbConnectionName()+" - "+message);
				handleGetConnectionException(active, exc);
			} finally {
				if(LOGGER.isDebugEnabled()){
					time = System.currentTimeMillis() - time;
					LOGGER.debug("getData processing time : "+ active.getDbConnectionName()+"  "+time+" miliseconds.");
				}
			}
		}
		if(lastException instanceof SQLException){
			throw (SQLException)lastException;
		}
		// repackage the exception
		// you are here because either you run out of available data sources
		// or the last exception was not of SQLException type.
		// repackage the exception
		if(lastException == null) {
			throw new DBLibException("The operation timed out while waiting to acquire a new connection." );
		} else {
			SQLException exception = new DBLibException(lastException.getMessage());
			exception.setStackTrace(lastException.getStackTrace());
			if(lastException.getCause() instanceof SQLException) {
				throw (SQLException)lastException.getCause();
			}
			throw exception;
		}
	}

	private CachedRowSet requestDataNoRecovery(String statement, ArrayList<Object> arguments, String preferredDS) throws SQLException {
		if(dsQueue.isEmpty()){
			LOGGER.error("Generated alarm: DBResourceManager.getData - No active DB connection pools are available.");
			throw new DBLibException("No active DB connection pools are available in RequestDataNoRecovery call.");
		}
		CachedDataSource active = this.dsQueue.peek();
		long time = System.currentTimeMillis();
		try {
			if(!active.isFabric()) {
				CachedDataSource master = findMaster();
				if(master != null)
					active = master;
			}
			return active.getData(statement, arguments);
//		} catch(SQLDataException exc){
//			throw exc;
		} catch(Throwable exc){
			String message = exc.getMessage();
			if(message == null)
				message = exc.getClass().getName();
			LOGGER.error("Generated alarm: "+active.getDbConnectionName()+" - "+message);
			if(exc instanceof SQLException)
				throw (SQLException)exc;
			else {
				DBLibException excptn = new DBLibException(exc.getMessage());
				excptn.setStackTrace(exc.getStackTrace());
				throw excptn;
			}
		} finally {
			if(LOGGER.isDebugEnabled()){
				time = System.currentTimeMillis() - time;
				LOGGER.debug(">> getData : "+ active.getDbConnectionName()+"  "+time+" miliseconds.");
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.resource.dblib.DbLibService#writeData(java.lang.String, java.util.ArrayList, java.lang.String)
	 */
	@Override
	public boolean writeData(String statement, ArrayList<String> arguments, String preferredDS) throws SQLException
        {
		ArrayList<Object> newList= new ArrayList<>();
		if(arguments != null && !arguments.isEmpty()) {
			newList.addAll(arguments);
		}

		return writeDataNoRecovery(statement, newList, preferredDS);
	}

	CachedDataSource findMaster() throws PoolExhaustedException {
	    CachedDataSource master = null;
		CachedDataSource[] dss = this.dsQueue.toArray(new CachedDataSource[0]);
		for(int i=0; i<dss.length; i++) {
			if(!dss[i].isSlave()) {
				master = dss[i];
				if(i != 0) {
					dsQueue.remove(master);
					dsQueue.add(master);
				}
				return master;
			}
		}
		LOGGER.warn("MASTER not found.");
		return null;
	}


	private boolean writeDataNoRecovery(String statement, ArrayList<Object> arguments, String preferredDS) throws SQLException {
		if(dsQueue.isEmpty()){
			LOGGER.error("Generated alarm: DBResourceManager.getData - No active DB connection pools are available.");
			throw new DBLibException("No active DB connection pools are available in RequestDataNoRecovery call.");
		}

		boolean initialRequest = true;
		boolean retryAllowed = true;
		CachedDataSource active = this.dsQueue.peek();
		long time = System.currentTimeMillis();
		while(initialRequest) {
			initialRequest = false;
			try {
				if(!active.isFabric()) {
					CachedDataSource master = findMaster();
					if(master != null) {
						active = master;
					}
				}

				return active.writeData(statement, arguments);
			} catch(Throwable exc){
				String message = exc.getMessage();
				if(message == null)
					message = exc.getClass().getName();
				LOGGER.error("Generated alarm: "+active.getDbConnectionName()+" - "+message);
				if(exc instanceof SQLException) {
					SQLException sqlExc = SQLException.class.cast(exc);
					// handle read-only exception
					if(sqlExc.getErrorCode() == 1290 && "HY000".equals(sqlExc.getSQLState())) {
						LOGGER.warn("retrying due to: " + sqlExc.getMessage());
						dsQueue.remove(active);
						dsQueue.add(active);
						if(retryAllowed){
							retryAllowed = false;
							initialRequest = true;
							continue;
						}
					}
					throw (SQLException)exc;
				} else {
					DBLibException excptn = new DBLibException(exc.getMessage());
					excptn.setStackTrace(exc.getStackTrace());
					throw excptn;
				}
			} finally {
				if(LOGGER.isDebugEnabled()){
					time = System.currentTimeMillis() - time;
					LOGGER.debug("writeData processing time : "+ active.getDbConnectionName()+"  "+time+" miliseconds.");
				}
			}
		}
		return true;
	}

	public void setDataSource(CachedDataSource dataSource) {
		if(this.dsQueue.contains(dataSource))
			return;
		if(this.broken.contains(dataSource))
			return;

		if(dataSource.testConnection(true)){
			this.dsQueue.add(dataSource);
		} else {
			this.broken.add(dataSource);
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		Throwable lastException = null;
		CachedDataSource active = null;

		if(dsQueue.isEmpty()){
			throw new DBLibException("No active DB connection pools are available in GetConnection call.");
		}

		try {
			active = dsQueue.peek();
			CachedDataSource tmpActive = findMaster();
			if(tmpActive != null) {
				active = tmpActive;
			}
			return new DBLibConnection(active.getConnection(), active);
		} catch(javax.sql.rowset.spi.SyncFactoryException exc){
			LOGGER.debug("Free memory (bytes): " + Runtime.getRuntime().freeMemory());
			LOGGER.warn("CLASSPATH issue. Allowing retry", exc);
			lastException = exc;
		} catch(PoolExhaustedException exc) {
			throw new NoAvailableConnectionsException(exc);
		} catch(Exception exc){
			lastException = exc;
			if(recoveryMode){
				handleGetConnectionException(active, exc);
			} else {
				if(exc instanceof SQLException) {
					throw (SQLException)exc;
				} else {
					DBLibException excptn = new DBLibException(exc.getMessage());
					excptn.setStackTrace(exc.getStackTrace());
					throw excptn;
				}
			}
		} catch (Throwable trwb) {
			DBLibException excptn = new DBLibException(trwb.getMessage());
			excptn.setStackTrace(trwb.getStackTrace());
			throw excptn;
		} finally {
			if(LOGGER.isDebugEnabled()){
				displayState();
			}
		}

		if(lastException instanceof SQLException){
			throw (SQLException)lastException;
		}
		// repackage the exception
		if(lastException == null) {
			throw new DBLibException("The operation timed out while waiting to acquire a new connection." );
		} else {
			SQLException exception = new DBLibException(lastException.getMessage());
			exception.setStackTrace(lastException.getStackTrace());
			if(lastException.getCause() instanceof SQLException) {
//				exception.setNextException((SQLException)lastException.getCause());
				throw (SQLException)lastException.getCause();
			}
			throw exception;
		}
	}

	@Override
	public Connection getConnection(String username, String password)
	throws SQLException {
		CachedDataSource active = null;

		if(dsQueue.isEmpty()){
			throw new DBLibException("No active DB connection pools are available in GetConnection call.");
		}


		try {
			active = dsQueue.peek();
			CachedDataSource tmpActive = findMaster();
			if(tmpActive != null) {
				active = tmpActive;
			}
			return active.getConnection(username, password);
		} catch(Throwable exc){
			if(recoveryMode){
				handleGetConnectionException(active, exc);
			} else {
				if(exc instanceof SQLException)
					throw (SQLException)exc;
				else {
					DBLibException excptn = new DBLibException(exc.getMessage());
					excptn.setStackTrace(exc.getStackTrace());
					throw excptn;
				}
			}

		}

		throw new DBLibException("No connections available in DBResourceManager in GetConnection call.");
	}

	private void handleGetConnectionException(CachedDataSource source, Throwable exc) {
		try {
			if(!source.canTakeOffLine())
			{
				LOGGER.error("Could not switch due to blocking");
				return;
			}

			boolean removed = dsQueue.remove(source);
			if(!broken.contains(source))
			{
				if(broken.add(source))
				{
					LOGGER.warn("DB Recovery: DataSource <" + source.getDbConnectionName()	+ "> put in the recovery mode. Reason : " + exc.getMessage());
				} else {
					LOGGER.warn("Error putting DataSource <" +source.getDbConnectionName()+  "> in recovery mode.");
				}
			} else {
				LOGGER.info("DB Recovery: DataSource <" + source.getDbConnectionName() + "> already in recovery queue");
			}
			if(removed)
			{
				if(!dsQueue.isEmpty())
				{
					LOGGER.warn("DB DataSource <" + dsQueue.peek().getDbConnectionName()	+ "> became active");
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	public void cleanUp() {
		for(Iterator<CachedDataSource> it=dsQueue.iterator();it.hasNext();){
			CachedDataSource cds = it.next();
			it.remove();
			cds.cleanUp();
		}

		try {
			this.terminating = true;
			if(broken != null)
			{
				try {
					broken.add( new TerminatingCachedDataSource(null));
				} catch(Exception exc){
					LOGGER.error("Waiting for Worker to stop", exc);
				}
			}
			worker.join(terminationTimeOut);
			LOGGER.info("DBResourceManager.RecoveryMgr <"+worker.toString() +"> termination was successful: " + worker.getState());
		} catch(Exception exc){
			LOGGER.error("Waiting for Worker thread to terminate ", exc);
		}
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return this.dsQueue.peek().getLogWriter();
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return this.dsQueue.peek().getLoginTimeout();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.dsQueue.peek().setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.dsQueue.peek().setLoginTimeout(seconds);
	}

	public void displayState(){
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("POOLS : Active = "+dsQueue.size() + ";\t Broken = "+broken.size());
			CachedDataSource current = dsQueue.peek();
			if(current != null) {
				LOGGER.debug("POOL : Active name = \'"+current.getDbConnectionName()+ "\'");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.resource.dblib.DbLibService#isActive()
	 */
	@Override
	public boolean isActive() {
		return this.dsQueue.size()>0;
	}

	public String getActiveStatus(){
		return "Connected: " + dsQueue.size()+"\tIn-recovery: "+broken.size();
	}

	public String getDBStatus(boolean htmlFormat) {
		StringBuilder buffer = new StringBuilder();

		ArrayList<CachedDataSource> list = new ArrayList<>();
		list.addAll(dsQueue);
		list.addAll(broken);
		if (htmlFormat)
		{
			buffer.append("<tr class=\"headerRow\"><th id=\"header1\">")
					.append("Name:").append("</th>");
			for (int i = 0; i < list.size(); i++) {
				buffer.append("<th id=\"header").append(2 + i).append("\">");
				buffer.append(list.get(i).getDbConnectionName()).append("</th>");
			}
			buffer.append("</tr>");

			buffer.append("<tr><td>State:</td>");
			for (int i = 0; i < list.size(); i++) {
				if (broken.contains(list.get(i))) {
					buffer.append("<td>in recovery</td>");
				}
				if (dsQueue.contains(list.get(i))) {
					if (dsQueue.peek() == list.get(i))
						buffer.append("<td>active</td>");
					else
						buffer.append("<td>standby</td>");
				}
			}
			buffer.append("</tr>");

		} else {
			for (int i = 0; i < list.size(); i++) {
				buffer.append("Name: ").append(list.get(i).getDbConnectionName());
				buffer.append("\tState: ");
				if (broken.contains(list.get(i))) {
					buffer.append("in recovery");
				} else
				if (dsQueue.contains(list.get(i))) {
					if (dsQueue.peek() == list.get(i))
						buffer.append("active");
					else
						buffer.append("standby");
				}

				buffer.append("\n");

			}
		}
		return buffer.toString();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	/**
	 * @return the monitorDbResponse
	 */
	@Override
	public final boolean isMonitorDbResponse() {
		return recoveryMode && monitorDbResponse;
	}

	public void test(){
		CachedDataSource obj = dsQueue.peek();
		Exception ption = new Exception();
		try {
			for(int i=0; i<5; i++)
			{
				handleGetConnectionException(obj, ption);
			}
		} catch(Throwable exc){
			LOGGER.warn("", exc);
		}
	}

	public String getPreferredDSName(){
		if(isActive()){
			return getPreferredDataSourceName(dsSelector);
		}
		return "";
	}

	public String getPreferredDataSourceName(AtomicBoolean flipper) {

		LinkedList<CachedDataSource> snapshot = new LinkedList<>(dsQueue);
		if(snapshot.size() > 1){
			CachedDataSource first = snapshot.getFirst();
			CachedDataSource last = snapshot.getLast();

			int delta = first.getMonitor().getProcessedConnectionsCount() - last.getMonitor().getProcessedConnectionsCount();
			if(delta < 0) {
				flipper.set(false);
			} else if(delta > 0) {
				flipper.set(true);
			} else {
				// check the last value and return !last
				flipper.getAndSet(!flipper.get());
			}

			if (flipper.get())
				Collections.reverse(snapshot);
		}
		return snapshot.peek().getDbConnectionName();
	}

	@Override
	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		return null;
	}

	public String getMasterName() {
		if(isActive()){
			return getMasterDataSourceName(dsSelector);
		}
		return "";
	}


	private String getMasterDataSourceName(AtomicBoolean flipper) {

		LinkedList<CachedDataSource> snapshot = new LinkedList<>(dsQueue);
		if(snapshot.size() > 1){
			CachedDataSource first = snapshot.getFirst();
			CachedDataSource last = snapshot.getLast();

			int delta = first.getMonitor().getProcessedConnectionsCount() - last.getMonitor().getProcessedConnectionsCount();
			if(delta < 0) {
				flipper.set(false);
			} else if(delta > 0) {
				flipper.set(true);
			} else {
				// check the last value and return !last
				flipper.getAndSet(!flipper.get());
			}

			if (flipper.get())
				Collections.reverse(snapshot);
		}
		return snapshot.peek().getDbConnectionName();
	}

    class RemindTask extends TimerTask {
        @Override
		public void run() {
			CachedDataSource ds = dsQueue.peek();
			if(ds != null)
				ds.getPoolInfo(false);
        }
    }

	public int poolSize() {
		return dsQueue.size();
	}
}
