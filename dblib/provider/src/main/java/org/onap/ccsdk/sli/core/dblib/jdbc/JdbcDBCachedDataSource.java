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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLFeatureNotSupportedException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.onap.ccsdk.sli.core.dblib.CachedDataSource;
import org.onap.ccsdk.sli.core.dblib.DBConfigException;
import org.onap.ccsdk.sli.core.dblib.config.BaseDBConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.Driver;


/**
 * @version $Revision: 1.7 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */

public class JdbcDBCachedDataSource extends CachedDataSource
{
	private String dbUserId;
	private String dbPasswd;
	private String dbUrl;

	private int minLimit;
	private int maxLimit;
	private int initialLimit;

	private static final String AS_CONF_ERROR = "AS_CONF_ERROR: ";

	private static Logger LOGGER = LoggerFactory.getLogger(JdbcDBCachedDataSource.class);

	/**
	 * @param jdbcElem
	 */
	public JdbcDBCachedDataSource(BaseDBConfiguration jdbcElem)
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

        minLimit = jdbcConfig.getDbMinLimit();
//        if (minLimit == null)
//        {
//        	String errorMsg =  "Invalid XML contents: JDBC Connection missing minLimit attribute";
//        	LOGGER.error(AS_CONF_ERROR + errorMsg);
//        	throw new DBConfigException(errorMsg);
//        }
        maxLimit =  jdbcConfig.getDbMaxLimit();
//        if (maxLimit == null)
//        {
//        	String errorMsg =  "Invalid XML contents: JDBC Connection missing maxLimit attribute";
//        	LOGGER.error(AS_CONF_ERROR + errorMsg);
//        	throw new DBConfigException(errorMsg);
//        }
        initialLimit =  jdbcConfig.getDbInitialLimit();
//        if (initialLimit == null)
//        {
//        	String errorMsg =  "Invalid XML contents: JDBC Connection missing initialLimit attribute";
//        	LOGGER.error(AS_CONF_ERROR + errorMsg);
//        	throw new DBConfigException(errorMsg);
//        }

        dbUrl = jdbcConfig.getDbUrl();
        if(dbUrl == null){
        	String errorMsg =  "Invalid XML contents: JDBCConnection missing dbUrl attribute";
        	LOGGER.error(AS_CONF_ERROR + errorMsg);
            throw new DBConfigException(errorMsg);
        }

		try {
			Driver dr = new com.mysql.jdbc.Driver();
			Class clazz = Class.forName("com.mysql.jdbc.Driver") ;

			PoolProperties p = new PoolProperties();
			p.setDriverClassName("com.mysql.jdbc.Driver");
			p.setUrl(dbUrl);
			p.setUsername(dbUserId);
			p.setPassword(dbPasswd);
			p.setJmxEnabled(true);
			p.setTestWhileIdle(false);
			p.setTestOnBorrow(true);
			p.setValidationQuery("SELECT 1");
			p.setTestOnReturn(false);
			p.setValidationInterval(30000);
			p.setTimeBetweenEvictionRunsMillis(30000);
			p.setInitialSize(initialLimit);
			p.setMaxActive(maxLimit);
			p.setMaxIdle(maxLimit);
			p.setMaxWait(10000);
			p.setRemoveAbandonedTimeout(60);
			p.setMinEvictableIdleTimeMillis(30000);
			p.setMinIdle(minLimit);
			p.setLogAbandoned(true);
			p.setRemoveAbandoned(true);
			p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
					+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

			DataSource dataSource = new DataSource(p);

			synchronized(this)
			{
				this.ds = dataSource;
				Connection con = null;
				PreparedStatement st = null;
				ResultSet rs = null;

				try {
					con = dataSource.getConnection();
					st = con.prepareStatement("Select 1 FROM DUAL");
					rs = st.executeQuery();
				} catch(Exception exc) {
					LOGGER.error(exc.getMessage());
				} finally {
					if(rs != null) rs.close();
					if(st != null) st.close();
					if(con != null) con.close();
				}

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

	public static JdbcDBCachedDataSource createInstance(BaseDBConfiguration config) /*throws Exception*/ {
		return new JdbcDBCachedDataSource(config);
	}

	public String toString(){
		return getDbConnectionName();
	}

	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public void cleanUp(){
		DataSource dataSource = (DataSource)ds;
		dataSource.getPool().purge();
		int active = dataSource.getActive();
		int size = dataSource.getSize();
		dataSource.close(true);
		super.cleanUp();
	}

}
