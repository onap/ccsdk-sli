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

package org.openecomp.sdnc.sli.resource.dblib.factory;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import org.openecomp.sdnc.sli.resource.dblib.config.BaseDBConfiguration;
import org.openecomp.sdnc.sli.resource.dblib.config.DbConfigPool;
import org.openecomp.sdnc.sli.resource.dblib.config.JDBCConfiguration;

/**
 * @version $Revision: 1.1 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki  01/17/08 Initial version
 */
public class DBConfigFactory {

	public static DbConfigPool createConfig(Properties resource) {
		return getConfigparams(resource);
	}

	static DbConfigPool getConfigparams(Properties properties){
		DbConfigPool xmlConfig = new DbConfigPool(properties);
		ArrayList<Properties> propertySets = new ArrayList<Properties>();

		if("JDBC".equalsIgnoreCase(xmlConfig.getType())) {
			String hosts = properties.getProperty(BaseDBConfiguration.DATABASE_HOSTS);
			if(hosts == null || hosts.isEmpty()) {
				propertySets.add(properties);
			} else {
				String[] newhost = hosts.split(",");
				for(int i=0; i< newhost.length; i++) {
					Properties localset = new Properties();
					localset.putAll(properties);
					String url = localset.getProperty(BaseDBConfiguration.DATABASE_URL);
					if(url.contains("DBHOST"))
						url = url.replace("DBHOST", newhost[i]);
					if(url.contains("dbhost"))
						url = url.replace("dbhost", newhost[i]);
					localset.setProperty(BaseDBConfiguration.DATABASE_URL, url);
					localset.setProperty(BaseDBConfiguration.CONNECTION_NAME, newhost[i]);
					propertySets.add(localset);
				}
			}
		} else {
			propertySets.add(properties);
		}
		try {
			Iterator<Properties>  it = propertySets.iterator();
			while(it.hasNext()) {
				BaseDBConfiguration config = parse(it.next());
				xmlConfig.addConfiguration(config);
			}

		} catch (Exception e) {
			LoggerFactory.getLogger(DBConfigFactory.class).warn("",e);
		}

		return xmlConfig;
	}

	public static BaseDBConfiguration parse(Properties props) throws Exception {

		String type = props.getProperty(BaseDBConfiguration.DATABASE_TYPE);

		BaseDBConfiguration config = null;

		if("JDBC".equalsIgnoreCase(type)) {
			config = new JDBCConfiguration(props);
		}

		return config;

	}
}
