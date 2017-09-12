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

package org.onap.ccsdk.sli.core.dblib.jdbc;


import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.onap.ccsdk.sli.core.dblib.CachedDataSource;
import org.onap.ccsdk.sli.core.dblib.CachedDataSourceFactory;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.ccsdk.sli.core.dblib.config.DbConfigPool;
import org.onap.ccsdk.sli.core.dblib.config.JDBCConfiguration;
import org.onap.ccsdk.sli.core.dblib.factory.AbstractResourceManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision: 1.6 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */
public class JdbcDbResourceManagerFactory extends AbstractResourceManagerFactory {
	private static Logger LOGGER = LoggerFactory.getLogger(JdbcDbResourceManagerFactory.class );
	private JdbcDbResourceManagerFactory(){

	}

	class MyFutureTask extends FutureTask<CachedDataSource>
	{

		public MyFutureTask(Callable<CachedDataSource> result) {
			super(result);
		}

	}

	public CachedDataSource[] initDBResourceManager(DbConfigPool dbConfig, DBResourceManager manager, String sourceName) throws SQLException
	{
		// here create the data sources objects
		JDBCConfiguration[] list = dbConfig.getJDBCbSourceArray();
		CachedDataSource[] cachedDS = new CachedDataSource[1];

		for(int i=0, max=list.length; i<max; i++){
			if(!sourceName.equals(list[i].getDbConnectionName()))
				continue;

			JDBCConfiguration config = list[i];
			CachedDataSource dataSource = CachedDataSourceFactory.createDataSource(config);
			cachedDS[0] = dataSource;
		}
		return cachedDS;
	}

	public CachedDataSource[] initDBResourceManager(DbConfigPool dbConfig, DBResourceManager manager) /* throws Exception */ {

		ExecutorService threadExecutor = Executors.newFixedThreadPool(2);
		// here create the data sources objects
		JDBCConfiguration[] list = dbConfig.getJDBCbSourceArray();

		MyFutureTask[] futures = new MyFutureTask[list.length];
		final Set<DBInitTask> tasks = new HashSet<DBInitTask>();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Creating " + list.length + " datasources.");
		}

		for(int i=0, max=list.length; i<max; i++){
			JDBCConfiguration config = list[i];

			DBInitTask task = new DBInitTask(config, tasks);
			tasks.add(task);
			futures[i] = new MyFutureTask(task);
		}

		try {
			synchronized(tasks){
				for(int i=0, max=list.length; i<max; i++){
					if(LOGGER.isDebugEnabled())
						LOGGER.debug("Starting executor tasks.");
					threadExecutor.execute(futures[i]);
				}
				// the timeout param is set is seconds.
				long timeout = ((dbConfig.getTimeout() <= 0) ? 60L : dbConfig.getTimeout());
				LOGGER.debug("Timeout set to " +timeout+" seconds");
				timeout *= 1000;
				// the timeout param is set is seconds, hence it needs to be multiplied by 1000.
				tasks.wait(timeout);
				if(LOGGER.isDebugEnabled())
					LOGGER.debug("initDBResourceManager wait completed.");
			}
		} catch(Exception exc) {
			LOGGER.error("Failed to initialize JndiCachedDataSource. Reason: ", exc);
		}

		if(threadExecutor != null){
			try {
				threadExecutor.shutdown();
			} catch(Exception exc){}
		}

		CachedDataSource[] cachedDS = new CachedDataSource[futures.length];

		boolean initialized = false;
		for(int i=0; i<futures.length; i++){
			Object obj = null;
			if(futures[i].isDone()){
				try {
					obj = futures[i].get();
					if(obj instanceof CachedDataSource){
						cachedDS[i] = (CachedDataSource)obj;
						initialized |= cachedDS[i].isInitialized();
						if(cachedDS[i].isInitialized())
							LOGGER.info("DataSource "+list[i].getDbConnectionName()+" initialized successfully");
						else
							LOGGER.error("DataSource "+list[i].getDbConnectionName()+" initialization failed");
					} else {
						if(obj == null) {
							LOGGER.warn("DataSource " + i + " initialization failed. Returned object is null");
						} else {
							LOGGER.warn("DataSource " + i + " initialization failed. Returned object is " + obj.getClass().getName());
						}
					}
				} catch (InterruptedException exc) {
					LOGGER.error("DataSource "+list[i].getDbConnectionName()+" initialization failed", exc);
				} catch (ExecutionException exc) {
					LOGGER.error("DataSource "+list[i].getDbConnectionName()+" initialization failed", exc);
				} catch (Exception exc) {
					LOGGER.error("DataSource "+list[i].getDbConnectionName()+" initialization failed", exc);
				}
			} else {
				try {
					obj = futures[i].get();
					if(obj instanceof CachedDataSource){
						LOGGER.warn("DataSource "+((CachedDataSource)obj).getDbConnectionName()+" failed");
					} else {
						if(obj == null) {
							LOGGER.warn("DataSource " + i + " initialization failed. Returned object is null");
						} else {
							LOGGER.warn("DataSource " + i + " initialization failed. Returned object is " + obj.getClass().getName());
						}
					}
				} catch (Exception exc) {
					LOGGER.error("DataSource "+list[i].getDbConnectionName()+" initialization failed", exc);
				}
			}
		}

		if(!initialized){
			new Error("Failed to initialize DB Library.");
		}
		return cachedDS;
	}

	public static AbstractResourceManagerFactory createIntstance() {
		return new JdbcDbResourceManagerFactory();
	}
}
