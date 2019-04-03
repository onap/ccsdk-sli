/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import org.apache.tomcat.jdbc.pool.PoolExhaustedException;
import org.onap.ccsdk.sli.core.dblib.config.DbConfigPool;
import org.onap.ccsdk.sli.core.dblib.config.JDBCConfiguration;
import org.onap.ccsdk.sli.core.dblib.config.TerminatingConfiguration;
import org.onap.ccsdk.sli.core.dblib.factory.DBConfigFactory;
import org.onap.ccsdk.sli.core.dblib.pm.PollingWorker;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THIS CLASS IS A COPY OF {@link DBResourceManager} WITH REMOVED OSGi DEPENDENCIES
 */
public class DBResourceManagerLighty implements DataSource, DataAccessor, DBResourceObserver, DbLibService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBResourceManagerLighty.class);
    private static final String DATABASE_URL = "org.onap.ccsdk.sli.jdbc.url";

    transient boolean terminating = false;
    transient protected long retryInterval = 10000L;
    transient boolean recoveryMode = true;

    SortedSet<CachedDataSource> dsQueue = new ConcurrentSkipListSet<>(new DataSourceComparator());
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
    private static final String LOGGER_ALARM_MSG="Generated alarm: DBResourceManager.getData - No active DB connection pools are available.";
    private static final String EXCEPTION_MSG= "No active DB connection pools are available in RequestDataNoRecovery call.";

    public DBResourceManagerLighty(final DBLIBResourceProviderLighty configuration) {
        this(configuration.getProperties());
    }

    public DBResourceManagerLighty(final Properties properties) {
        this.configProps = processSystemVariables(properties);

        // TODO : hack to force classloader to cache mariadb driver.  This shouldnt be necessary,
        // but for some reason it is (without this, dblib throws ClassNotFound on mariadb driver
        // and fails to load).
        LOGGER.info("Creating dummy instance of org.mariadb.jdbc.Driver");
        Driver dvr = new org.mariadb.jdbc.Driver();
        dvr = null;

        // get retry interval value
        retryInterval = getLongFromProperties(configProps, "org.onap.dblib.connection.retry", 10000L);

        // get recovery mode flag
        recoveryMode = getBooleanFromProperties(configProps, "org.onap.dblib.connection.recovery", true);
        if(!recoveryMode)
        {
            recoveryMode = false;
            LOGGER.info("Recovery Mode disabled");
        }
        // get time out value for thread cleanup
        terminationTimeOut = getLongFromProperties(configProps, "org.onap.dblib.termination.timeout", 300000L);
        // get properties for monitoring
        monitorDbResponse = getBooleanFromProperties(configProps, "org.onap.dblib.connection.monitor", false);
        monitoringInterval = getLongFromProperties(configProps, "org.onap.dblib.connection.monitor.interval", 1000L);
        monitoringInitialDelay = getLongFromProperties(configProps, "org.onap.dblib.connection.monitor.startdelay", 5000L);
        expectedCompletionTime = getLongFromProperties(configProps, "org.onap.dblib.connection.monitor.expectedcompletiontime", 5000L);
        unprocessedFailoverThreshold = getLongFromProperties(configProps, "org.onap.dblib.connection.monitor.unprocessedfailoverthreshold", 3L);

        // initialize performance monitor
        PollingWorker.createInistance(configProps);

        // initialize recovery thread
        worker = new RecoveryMgr();
        worker.setName("DBResourcemanagerWatchThread");
        worker.setDaemon(true);
        worker.start();

        try {
            this.config(configProps);
        } catch (final Exception e) {
            // TODO: config throws <code>Exception</code> which is poor practice.  Eliminate this in a separate patch.
            LOGGER.error("Fatal Exception encountered while configuring DBResourceManager", e);
        }
    }

    public static Properties processSystemVariables(Properties properties) {
        Map<Object, Object> hmap = new Properties();
        hmap.putAll(properties);

        Map<Object, Object> result = hmap.entrySet().stream()
            .filter(map -> map.getValue().toString().startsWith("${"))
            .filter(map -> map.getValue().toString().endsWith("}"))
            .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

        result.forEach((name, propEntries) -> {
            hmap.put(name, replace(propEntries.toString()));
        });

        if(hmap.containsKey(DATABASE_URL) && hmap.get(DATABASE_URL).toString().contains("${")) {
            String url = hmap.get(DATABASE_URL).toString();
            String[] innerChunks = url.split("\\$\\{");
            for(String chunk : innerChunks) {
                if(chunk.contains("}")) {
                    String subChunk = chunk.substring(0, chunk.indexOf("}"));
                    String varValue = System.getenv(subChunk);
                    url = url.replace("${"+subChunk+"}", varValue);
                }
            }
            hmap.put(DATABASE_URL, url);
        }
        return Properties.class.cast(hmap);
      }


      private static String replace(String value) {
          String globalVariable = value.substring(2, value.length() -1);
          String varValue = System.getenv(globalVariable);
          return  (varValue != null) ? varValue : value;
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
                cachedDS[i].addObserver(DBResourceManagerLighty.this);
            }

            DataSourceTester[] tester = new DataSourceTester[config.length];

            for(int i=0; i<tester.length; i++){
                tester[i] = new DataSourceTester(cachedDS[i], DBResourceManagerLighty.this, semaphore);
                tester[i].start();
                }

            // the timeout param is set is seconds.
            long timeout = ((dbConfig.getTimeout() <= 0) ? 60L : dbConfig.getTimeout());
            LOGGER.debug("Timeout set to {} seconds", timeout);
            timeout *= 1000;


            synchronized (semaphore) {
                semaphore.wait(timeout);
            }
        } catch(Exception exc){
            LOGGER.warn("DBResourceManager.initWorker", exc);
        } finally {
            startTime = System.currentTimeMillis() - startTime;
            LOGGER.info("Completed wait with {} active datasource(s) in {} ms", dsQueue.size(), startTime);
        }
    }


    private final class DataSourceComparator implements Comparator<CachedDataSource> {
        @Override
        public int compare(CachedDataSource left, CachedDataSource right) {
            if(LOGGER.isTraceEnabled())
                LOGGER.trace("----------SORTING-------- () : ()", left.getDbConnectionName(), right.getDbConnectionName());
            try {
                if(left == right) {
                    return 0;
                }
                if(left == null){
                    return 1;
                }
                if(right == null){
                    return -1;
                }

                boolean leftMaster = !left.isSlave();
                if(leftMaster) {
                    if(left.getIndex() <= right.getIndex())
                        return -1;
                    else {
                        boolean rightMaster = !right.isSlave();
                        if(rightMaster) {
                            if(left.getIndex() <= right.getIndex())
                                return -1;
//                            if(left.getIndex() > right.getIndex())
                            else {
                                return 1;
                            }
                        } else {
                    return -1;
                        }
                    }
                }
                if(!right.isSlave())
                    return 1;

                if(left.getIndex() <= right.getIndex())
                    return -1;
                if(left.getIndex() > right.getIndex())
                    return 1;


            } catch (Throwable e) {
                LOGGER.warn("", e);
            }
            return -1;
        }
    }

    class DataSourceTester extends Thread {

        private final CachedDataSource ds;
        private final DBResourceManagerLighty manager;
        private final ConcurrentLinkedQueue<CachedDataSource> semaphoreQ;

        public DataSourceTester(CachedDataSource ds, DBResourceManagerLighty manager, ConcurrentLinkedQueue<CachedDataSource> semaphore) {
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
                LOGGER.info("Adding MASTER {} to active queue", ds.getDbConnectionName());
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
            if(ds != null)
                LOGGER.info("Thread DataSourceTester terminated {} for {}", this.getName(), ds.getDbConnectionName());
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
                if(dataSource == dsQueue.first())
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
        CachedDataSource active = this.dsQueue.first();
        handleGetConnectionException(active, new Exception("test"));
    }

    class RecoveryMgr extends Thread {

        @Override
        public void run() {
            while(!terminating)
            {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e1) {    }
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
                        } catch (Exception e1) {    }
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

        // test if there are any connection pools available
        if(this.dsQueue.isEmpty()){
            LOGGER.error(LOGGER_ALARM_MSG);
            throw new DBLibException("No active DB connection pools are available in RequestDataWithRecovery call.");
        }

        // loop through available data sources to retrieve data.
        for(int i=0; i< 2; i++)
        {
            CachedDataSource active = this.dsQueue.first();

            long time = System.currentTimeMillis();
            try {
                if(!active.isFabric()) {
                    if(this.dsQueue.size() > 1 && active.isSlave()) {
                    CachedDataSource master = findMaster();
                    if(master != null) {
                        active = master;
                    }
                }
                }

                return active.getData(statement, arguments);
            } catch(SQLDataException | SQLSyntaxErrorException | SQLIntegrityConstraintViolationException exc){
                throw exc;
            } catch(Throwable exc){
                if(exc instanceof SQLException) {
                    SQLException sqlExc = (SQLException)exc;
                    int code = sqlExc.getErrorCode();
                    String state = sqlExc.getSQLState();
                    LOGGER.debug("SQLException code: {} state: {}", code, state);
                    if("07001".equals(sqlExc.getSQLState())) {
                        throw sqlExc;
                    }
                }
                lastException = exc;
                LOGGER.error("Generated alarm: {}", active.getDbConnectionName(), exc);
                handleGetConnectionException(active, exc);
            } finally {
                if(LOGGER.isDebugEnabled()){
                    time = System.currentTimeMillis() - time;
                    LOGGER.debug("getData processing time : {} {} miliseconds.", active.getDbConnectionName(), time);
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
            LOGGER.error(LOGGER_ALARM_MSG);
            throw new DBLibException(EXCEPTION_MSG);
        }
        CachedDataSource active = this.dsQueue.first();
        long time = System.currentTimeMillis();
        try {
            if(!active.isFabric()) {
                if(this.dsQueue.size() > 1 && active.isSlave()) {
                CachedDataSource master = findMaster();
                    if(master != null) {
                    active = master;
            }
                }
            }
            return active.getData(statement, arguments);

        } catch(Throwable exc){
            String message = exc.getMessage();
            if(message == null)
                message = exc.getClass().getName();
            LOGGER.error("Generated alarm: {} - {}",active.getDbConnectionName(), message);
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
                LOGGER.debug(">> getData : {} {}  miliseconds.", active.getDbConnectionName(), time);
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

    synchronized CachedDataSource findMaster() throws SQLException {
        final CachedDataSource[] clone = this.dsQueue.toArray(new CachedDataSource[0]);

        for(final CachedDataSource  dss : clone) {
            if(!dss.isSlave()) {
                final CachedDataSource first = this.dsQueue.first();
                if(first != dss) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("----------REODRERING--------");
                    dsQueue.clear();
                    if(!dsQueue.addAll(Arrays.asList(clone))) {
                        LOGGER.error("Failed adding datasources");
                }
                }
                return  dss;
            }
        }
        LOGGER.warn("MASTER not found.");
        return null;
    }


    private boolean writeDataNoRecovery(String statement, ArrayList<Object> arguments, String preferredDS) throws SQLException {
        if(dsQueue.isEmpty()){
            LOGGER.error(LOGGER_ALARM_MSG);
            throw new DBLibException(EXCEPTION_MSG);
        }

        boolean initialRequest = true;
        boolean retryAllowed = true;
        CachedDataSource active = this.dsQueue.first();
        long time = System.currentTimeMillis();
        while(initialRequest) {
            initialRequest = false;
            try {
                if(!active.isFabric()) {
                    if(this.dsQueue.size() > 1 && active.isSlave()) {
                    CachedDataSource master = findMaster();
                    if(master != null) {
                        active = master;
                    }
                }
                }

                return active.writeData(statement, arguments);
            } catch(Throwable exc){
                String message = exc.getMessage();
                if(message == null)
                    message = exc.getClass().getName();
                LOGGER.error("Generated alarm: {} - {}", active.getDbConnectionName(), message);
                if(exc instanceof SQLException) {
                    SQLException sqlExc = SQLException.class.cast(exc);
                    // handle read-only exception
                    if(sqlExc.getErrorCode() == 1290 && "HY000".equals(sqlExc.getSQLState())) {
                        LOGGER.warn("retrying due to: {}", sqlExc.getMessage());
                        this.findMaster();
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
                    LOGGER.debug("writeData processing time : {} {} miliseconds.", active.getDbConnectionName(), time);
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
            active = dsQueue.first();

            if(!active.isFabric()) {
                if(this.dsQueue.size() > 1 && active.isSlave()) {
                LOGGER.debug("Forcing reorder on: {}", dsQueue.toString());
                    CachedDataSource master = findMaster();
                    if(master != null) {
                        active = master;
                    }
                }
            }
            return new DBLibConnection(active.getConnection(), active);
        } catch(javax.sql.rowset.spi.SyncFactoryException exc){
            LOGGER.debug("Free memory (bytes): " + Runtime.getRuntime().freeMemory());
            LOGGER.warn("CLASSPATH issue. Allowing retry", exc);
            lastException = exc;
        } catch(PoolExhaustedException exc) {
            throw new NoAvailableConnectionsException(exc);
        } catch(SQLNonTransientConnectionException exc){
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
            active = dsQueue.first();
            if(!active.isFabric()) {
                if(this.dsQueue.size() > 1 && active.isSlave()) {
                    CachedDataSource master = findMaster();
                    if(master != null) {
                        active = master;
                    }
                }
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

    private void handleGetConnectionException(final CachedDataSource source, Throwable exc) {
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
                    LOGGER.warn("DB Recovery: DataSource <" + source.getDbConnectionName()    + "> put in the recovery mode. Reason : " + exc.getMessage());
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
                    LOGGER.warn("DB DataSource <" + dsQueue.first().getDbConnectionName()    + "> became active");
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
                    broken.add( new TerminatingCachedDataSource(new TerminatingConfiguration()));
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
        return this.dsQueue.first().getLogWriter();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.dsQueue.first().getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.dsQueue.first().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.dsQueue.first().setLoginTimeout(seconds);
    }

    public void displayState(){
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("POOLS : Active = "+dsQueue.size() + ";\t Broken = "+broken.size());
            CachedDataSource current = dsQueue.first();
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
                    if (dsQueue.first() == list.get(i))
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
                    if (dsQueue.first() == list.get(i))
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
        CachedDataSource obj = dsQueue.first();
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

    @Override
    public java.util.logging.Logger getParentLogger()
            throws SQLFeatureNotSupportedException {
        return null;
    }

    class RemindTask extends TimerTask {
        @Override
        public void run() {
            CachedDataSource ds = dsQueue.first();
            if(ds != null)
                ds.getPoolInfo(false);
        }
    }

    public int poolSize() {
        return dsQueue.size();
    }
}
