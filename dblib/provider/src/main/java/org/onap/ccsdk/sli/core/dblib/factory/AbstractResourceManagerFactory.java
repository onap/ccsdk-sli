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

package org.onap.ccsdk.sli.core.dblib.factory;


import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.Callable;

import org.onap.ccsdk.sli.core.dblib.CachedDataSource;
import org.onap.ccsdk.sli.core.dblib.CachedDataSourceFactory;
import org.onap.ccsdk.sli.core.dblib.DBConfigException;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.ccsdk.sli.core.dblib.config.BaseDBConfiguration;
import org.onap.ccsdk.sli.core.dblib.config.DbConfigPool;
import org.onap.ccsdk.sli.core.dblib.config.JDBCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision: 1.6 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */
public abstract class AbstractResourceManagerFactory {
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractResourceManagerFactory.class);

	public abstract CachedDataSource[] initDBResourceManager(DbConfigPool dbConfig, DBResourceManager manager) throws Exception;
	public abstract CachedDataSource[] initDBResourceManager(DbConfigPool dbConfig,	DBResourceManager dbResourceManager, String sourceName) throws SQLException ;


	public static AbstractResourceManagerFactory createIntstance() throws FactoryNotDefinedException {
		throw new FactoryNotDefinedException("Factory method 'createIntstance' needs to be overriden in DBResourceManagerFactory");
	}

	public class DBInitTask implements Callable<CachedDataSource>
	{
		private BaseDBConfiguration config = null;
		private Set<DBInitTask> activeTasks;

		public DBInitTask(JDBCConfiguration jdbcconfig, Set<DBInitTask> tasks) {
			this.config = jdbcconfig;
			this.activeTasks = tasks;
		}

		public CachedDataSource call() throws Exception {
			CachedDataSource ds = null;
			try {
				ds = CachedDataSourceFactory.createDataSource(config);
				return ds;
			} finally {
				synchronized(activeTasks) {
					activeTasks.remove(this);
					if (activeTasks.isEmpty()) {
						final Runnable closure = new Runnable() {

							public void run() {
								try {
									Thread.sleep(300);
								} catch (Exception e) {
								}
								synchronized(activeTasks) {
									activeTasks.notifyAll();
								}
							}
						};
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Completed CachedDataSource.Call and notifyAll from " + ds.getDbConnectionName());
						}
						Thread worker = new Thread(closure);
						worker.setDaemon(true);
						worker.start();
					} else {
						if (LOGGER.isDebugEnabled()) {
							if (ds != null) {
								LOGGER.debug("Completed CachedDataSource.Call from " + ds.getDbConnectionName());
							}
						}
					}
				}
			}
		}
	}
}
