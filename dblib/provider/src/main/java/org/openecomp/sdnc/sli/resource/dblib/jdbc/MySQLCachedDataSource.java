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

package org.openecomp.sdnc.sli.resource.dblib.jdbc;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import org.openecomp.sdnc.sli.resource.dblib.CachedDataSource;
import org.openecomp.sdnc.sli.resource.dblib.DBConfigException;
import org.openecomp.sdnc.sli.resource.dblib.config.BaseDBConfiguration;
import org.openecomp.sdnc.sli.resource.dblib.config.JDBCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;


 

/**
 * @version $Revision: 1.7 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */

public class MySQLCachedDataSource extends CachedDataSource
{
	private String dbUserId;
	private String dbPasswd;
	private String dbUrl;
	
	private String minLimit;
	private String maxLimit;
	private String initialLimit;
	
	private static final String AS_CONF_ERROR = "AS_CONF_ERROR: ";
	
	private static Logger LOGGER = LoggerFactory.getLogger(MySQLCachedDataSource.class);

	/**
	 * @param jdbcElem
	 */
	public MySQLCachedDataSource(BaseDBConfiguration jdbcElem)
	{
			super(jdbcElem);
	}

	@Override
	protected void configure(BaseDBConfiguration xmlElem) throws DBConfigException 
	{
		BaseDBConfiguration jdbcConfig = (BaseDBConfiguration)xmlElem;
		if(jdbcConfig.getConnTimeout() > 0){
			this.CONN_REQ_TIMEOUT = jdbcConfig.getConnTimeout();
		}
		if(jdbcConfig.getRequestTimeout() > 0){
				this.DATA_REQ_TIMEOUT = jdbcConfig.getRequestTimeout();
		}

    	// set connection pool name
		String dbConnectionName = jdbcConfig.getDbConnectionName();
    	super.setDbConnectionName(dbConnectionName);
    	// Configure the JDBC connection
    	dbUserId = jdbcConfig.getDbUserId();
        if (dbUserId == null)
        {
        	String errorMsg =  "Invalid XML contents: JDBCConnection missing dbUserId attribute";
        	LOGGER.error(AS_CONF_ERROR + errorMsg);
            throw new DBConfigException(errorMsg);
        }

        dbPasswd = jdbcConfig.getDbPasswd();
        if (dbPasswd == null)
        {
        	String errorMsg =  "Invalid XML contents: JDBCConnection missing dbPasswd attribute";
        	LOGGER.error(AS_CONF_ERROR + errorMsg);
            throw new DBConfigException(errorMsg);
        }
        /*
        dbDriver = jdbcConfig.getDbDriver();
        if (dbDriver == null)
        {
        	String errorMsg =  "Invalid XML contents: JDBCConnection missing dbDriver attribute";
        	LOGGER.error(AS_CONF_ERROR + errorMsg);
        	throw new ScpTblUpdateError(errorMsg);
        }
        */

        minLimit = Integer.toString(jdbcConfig.getDbMinLimit());
        if (minLimit == null)
        {
        	String errorMsg =  "Invalid XML contents: JDBC Connection missing minLimit attribute";
        	LOGGER.error(AS_CONF_ERROR + errorMsg);
        	throw new DBConfigException(errorMsg);
        }
        maxLimit =  Integer.toString(jdbcConfig.getDbMaxLimit());
        if (maxLimit == null)
        {
        	String errorMsg =  "Invalid XML contents: JDBC Connection missing maxLimit attribute";
        	LOGGER.error(AS_CONF_ERROR + errorMsg);
        	throw new DBConfigException(errorMsg);
        }
        initialLimit =  Integer.toString(jdbcConfig.getDbInitialLimit());
        if (initialLimit == null)
        {
        	String errorMsg =  "Invalid XML contents: JDBC Connection missing initialLimit attribute";
        	LOGGER.error(AS_CONF_ERROR + errorMsg);
        	throw new DBConfigException(errorMsg);
        }

        dbUrl = jdbcConfig.getDbUrl();
        if(dbUrl == null){
        	String errorMsg =  "Invalid XML contents: JDBCConnection missing dbUrl attribute";
        	LOGGER.error(AS_CONF_ERROR + errorMsg);
            throw new DBConfigException(errorMsg);
        }
        
		try {
			
			MysqlDataSource dataSource = new MysqlDataSource();
		    dataSource.setUser(dbUserId);
		    dataSource.setPassword(dbPasswd);
		    dataSource.setURL(dbUrl);
//		    dataSource.setInitialSize(5);
//		    dataSource.setMaxTotal(60);
//		    dataSource.setMaxActive(100);
//		    dataSource.setMaxWait(10000);
//		    dataSource.setMaxIdle(10);

			Properties connAttr = new Properties();

			connAttr.setProperty("MinLimit", minLimit);
			connAttr.setProperty("MaxLimit", maxLimit);
			connAttr.setProperty("InitialLimit", initialLimit);
			connAttr.setProperty("TRANSACTION_ISOLATION","SERIALIZABLE");
			connAttr.setProperty("CONNECTION_TAG", dbConnectionName.toUpperCase()+"_CONNECTION");
			connAttr.setProperty("InactivityTimeout", "900");
			connAttr.setProperty("AbandonedConnectionTimeout", "600");
			connAttr.setProperty("PropertyCheckInterval", "60");
			connAttr.setProperty("ValidateConnection", "true");
			

			synchronized(this)
			{
				this.ds = dataSource;

				initialized = true;
				LOGGER.info("MySQLDataSource <"+dbConnectionName+"> configured successfully. Using URL: "+dbUrl);
			}

//		} catch (SQLException exc) {
//			initialized = false;
//			StringBuffer sb = new StringBuffer();
//			sb.append("Failed to initialize MySQLDataSource<");
//			sb.append(dbConnectionName).append(">. Reason: ");
//			sb.append(exc.getMessage());
//			LOGGER.error("AS_CONF_ERROR: " + sb.toString());
////			throw new DBConfigException(e.getMessage());
		} catch (Exception exc) {
    		initialized = false;
			StringBuffer sb = new StringBuffer();
			sb.append("Failed to initialize MySQLCachedDataSource <");
			sb.append(dbConnectionName).append(">. Reason: ");
			sb.append(exc.getMessage());
			LOGGER.error("AS_CONF_ERROR: " + sb.toString());
//    		throw new DBConfigException(e.getMessage());
    	}
    }

	public final String getDbUrl()
	{
		return dbUrl;
	}

	public final String getDbUserId()
	{
		return dbUserId;
	}

	public final String getDbPasswd()
	{
		return dbPasswd;
	}

	public static MySQLCachedDataSource createInstance(BaseDBConfiguration config) /*throws Exception*/ {
		return new MySQLCachedDataSource(config);
	}
	
	public String toString(){
		return getDbConnectionName();
	}

	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}
}
