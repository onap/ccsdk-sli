/*-
 * ============LICENSE_START=======================================================
 * openecomp
 * ================================================================================
 * Copyright (C) 2016 - 2017 AT&T
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

package org.openecomp.sdnc.sli.resource.dblib;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBLIBResourceActivator implements BundleActivator {

	private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";

	private static final String DBLIB_PROP_PATH = "/dblib.properties";

	private ServiceRegistration registration = null;

	private static final Logger LOG = LoggerFactory.getLogger(DBLIBResourceActivator.class);

	@Override
	public void start(BundleContext ctx) throws Exception {
		LOG.info("entering DBLIBResourceActivator.start");
		
		DbLibService jdbcDataSource = null;
		// Read properties
		Properties props = new Properties();
		
		File file =  null;
		URL propURL = null;
		String propDir = System.getenv(SDNC_CONFIG_DIR);
		if ((propDir == null) || (propDir.length() == 0)) {
			propDir = "/opt/sdnc/data/properties";
		}
		file = new File(propDir + DBLIB_PROP_PATH);
		if(file.exists()) {
			propURL = file.toURI().toURL();
			LOG.info("Using property file (1): " + file.toString());
		} else {
			propURL = ctx.getBundle().getResource("dblib.properties");
			URL tmp = null;
			if (propURL == null) {
				file = new File(DBLIB_PROP_PATH);
				tmp = this.getClass().getResource(DBLIB_PROP_PATH);
//				if(!file.exists()) {
				if(tmp == null) {
					throw new DblibConfigurationException("Missing configuration properties resource(3) : " + DBLIB_PROP_PATH);
				} else {
					propURL = tmp; //file.toURI().toURL();
					LOG.info("Using property file (4): " + file.toString());
				}
			} else {
				LOG.info("Using property file (2): " + propURL.toString());
			}
		}

		
		try {
			props.load(propURL.openStream());
		} catch (Exception e) {
			throw new DblibConfigurationException("Could not load properties at URL " + propURL.toString(), e);

		}



		try {
			jdbcDataSource = DBResourceManager.create(props);
		} catch (Exception exc) {
			throw new DblibConfigurationException("Could not get initialize database", exc);
		}

		String regName = jdbcDataSource.getClass().getName();

		LOG.info("Registering DBResourceManager service "+regName);
		registration = ctx.registerService(new String[] { regName, DbLibService.class.getName(), "javax.sql.DataSource" }, jdbcDataSource, null);
	}

	@Override
	public void stop(BundleContext ctx) throws Exception {
		LOG.info("entering DBLIBResourceActivator.stop");
		if (registration != null)
		{
			try {
				ServiceReference sref = ctx.getServiceReference(DbLibService.class.getName());

				if (sref == null) {
					LOG.warn("Could not find service reference for DBLIB service ("	+ DbLibService.class.getName() + ")");
				} else {
					DBResourceManager dblibSvc = (DBResourceManager) ctx.getService(sref);
					if (dblibSvc == null) {
						LOG.warn("Could not find service reference for DBLIB service ("	+ DbLibService.class.getName() + ")");
					} else {
						dblibSvc.cleanUp();
					}
				}
			} catch(Throwable exc) {
				LOG.warn("Cleanup", exc);
			}

			registration.unregister();
			registration = null;
			LOG.debug("Deregistering DBResourceManager service");
		}
	}

}
