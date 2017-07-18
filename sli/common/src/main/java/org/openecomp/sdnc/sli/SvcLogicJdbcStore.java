/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.openecomp.sdnc.sli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SvcLogicJdbcStore implements SvcLogicStore {
	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicJdbcStore.class);

	private String dbUrl = null;
	private String dbName = null;
	private String dbUser = null;
	private String dbPasswd = null;
	private String dbDriver = null;
        
	private Connection dbConn;
	private PreparedStatement hasActiveGraphStmt = null;
	private PreparedStatement hasVersionGraphStmt = null;
	private PreparedStatement fetchActiveGraphStmt = null;
	private PreparedStatement fetchVersionGraphStmt = null;
	private PreparedStatement storeGraphStmt = null;
	private PreparedStatement deleteGraphStmt = null;
	
	private PreparedStatement deactivateStmt = null;
	private PreparedStatement activateStmt = null;
	
	private PreparedStatement registerNodeStmt = null;
	private PreparedStatement unregisterNodeStmt = null;
	private PreparedStatement validateNodeStmt = null;
        	
	private void getConnection() throws ConfigurationException
	{

		Properties jdbcProps = new Properties();
		
		jdbcProps.setProperty("user", dbUser);
		jdbcProps.setProperty("password", dbPasswd);
		
		try {
			Driver dvr = new com.mysql.jdbc.Driver();
			if (dvr.acceptsURL(dbUrl))
			{
				LOG.debug("Driver com.mysql.jdbc.Driver accepts "+dbUrl);
			}
			else
			{
				LOG.warn("Driver com.mysql.jdbc.Driver does not accept "+dbUrl);
			}
		} catch (SQLException e1) {
			LOG.error("Caught exception trying to load com.mysql.jdbc.Driver", e1);


		}
		
		
		try
		{
			this.dbConn = DriverManager.getConnection(dbUrl, jdbcProps);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("failed to get database connection ["+dbUrl+"]", e);
		}	
		
	}
	
	private void createTable() throws ConfigurationException
	{


		DatabaseMetaData dbm = null;
		
		
		try {
			dbm = dbConn.getMetaData();
		} catch (SQLException e) {

			throw new ConfigurationException("could not get databse metadata", e);
		}

		// See if table SVC_LOGIC exists.  If not, create it.
		try
		{


			ResultSet tables = dbm.getTables(null, null, "SVC_LOGIC", null);
			if (tables.next()) {
				// Table exists
			}
			else {

				String crTableCmd = "CREATE TABLE "+dbName+".SVC_LOGIC ("
						+ "module varchar(80) NOT NULL,"
						+ "rpc varchar(80) NOT NULL,"
						+ "version varchar(40) NOT NULL,"
						+ "mode varchar(5) NOT NULL,"
						+ "active varchar(1) NOT NULL,"
						+ "graph BLOB,"
						+ "CONSTRAINT P_SVC_LOGIC PRIMARY KEY(module, rpc, version, mode))";

				Statement stmt = null;
				ConfigurationException myExc = null;
				try
				{
					stmt = dbConn.createStatement();
					stmt.executeUpdate(crTableCmd);
				}
				catch (SQLException e1)
				{
					myExc = new ConfigurationException("cannot create SVC_LOGIC table", e1);
				}
				finally
				{
					if (stmt != null)
					{
						stmt.close();
					}
				}

				if (myExc != null)
				{
					throw myExc;
				}
			}
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not create SVC_LOGIC table", e);
		}
		
		// See if NODE_TYPES table exists and, if not, create it
		
		try
		{


			ResultSet tables = dbm.getTables(null, null, "NODE_TYPES", null);
			if (tables.next()) {
				// Table exists
			}
			else {

				String crTableCmd = "CREATE TABLE "+dbName+".NODE_TYPES ("
						+ "nodetype varchar(80) NOT NULL,"
						+ "CONSTRAINT P_NODE_TYPES PRIMARY KEY(nodetype))";

				Statement stmt = null;
				ConfigurationException myExc = null;
				try
				{
					stmt = dbConn.createStatement();
					stmt.executeUpdate(crTableCmd);
				}
				catch (SQLException e1)
				{
					myExc = new ConfigurationException("cannot create SVC_LOGIC table", e1);
				}
				finally
				{
					if (stmt != null)
					{
						stmt.close();
					}
				}

				if (myExc != null)
				{
					throw myExc;
				}
			}
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not create SVC_LOGIC table", e);
		}
	}
	
	private void prepStatements() throws ConfigurationException
	{

		// Prepare statements
		String hasVersionGraphSql = "SELECT count(*) FROM "+dbName+".SVC_LOGIC"
				+ " WHERE module = ? AND rpc = ? AND mode = ? AND version = ?";
		
		try
		{
			hasVersionGraphStmt = dbConn.prepareStatement(hasVersionGraphSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+hasVersionGraphSql, e);
			
		}
		
		String hasActiveGraphSql = "SELECT count(*) FROM "+dbName+".SVC_LOGIC"
				+ " WHERE module = ? AND rpc = ? AND mode = ? AND active = 'Y'";
		
		try
		{
			hasActiveGraphStmt = dbConn.prepareStatement(hasActiveGraphSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+hasVersionGraphSql, e);
			
		}
		
		String fetchVersionGraphSql = "SELECT graph FROM "+dbName+".SVC_LOGIC"
				+ " WHERE module = ? AND rpc = ? AND mode = ? AND version = ?";
		
		try
		{
			fetchVersionGraphStmt = dbConn.prepareStatement(fetchVersionGraphSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+fetchVersionGraphSql, e);
			
		}
		
		String fetchActiveGraphSql = "SELECT graph FROM "+dbName+".SVC_LOGIC"
				+ " WHERE module = ? AND rpc = ? AND mode = ? AND active = 'Y'";
		
		try
		{
			fetchActiveGraphStmt = dbConn.prepareStatement(fetchActiveGraphSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+fetchVersionGraphSql, e);
			
		}
		
		String storeGraphSql = "INSERT INTO "+dbName+".SVC_LOGIC (module, rpc, version, mode, active, graph)"
				+ " VALUES(?, ?, ?, ?, ?, ?)";
		
		try
		{
			storeGraphStmt = dbConn.prepareStatement(storeGraphSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+storeGraphSql, e);
		}
		
		String deleteGraphSql = "DELETE FROM "+dbName+".SVC_LOGIC WHERE module = ? AND rpc = ? AND version = ? AND mode = ?";
		
		try
		{
			deleteGraphStmt = dbConn.prepareStatement(deleteGraphSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+deleteGraphSql, e);
		}
		
		String deactivateSql = "UPDATE "+dbName+".SVC_LOGIC SET active = 'N' WHERE module = ? AND rpc = ? AND mode = ?";
		
		try
		{
			deactivateStmt = dbConn.prepareStatement(deactivateSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+deactivateSql, e);
		}
		
		String activateSql = "UPDATE "+dbName+".SVC_LOGIC SET active = 'Y' WHERE module = ? AND rpc = ? AND version = ? AND mode = ?";
		
		try
		{
			activateStmt = dbConn.prepareStatement(activateSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+activateSql, e);
		}
		
		String registerNodeSql = "INSERT INTO "+dbName+".NODE_TYPES (nodetype) VALUES(?)";
		try
		{
			registerNodeStmt = dbConn.prepareStatement(registerNodeSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+registerNodeSql, e);
		}
		
		String unregisterNodeSql = "DELETE FROM "+dbName+".NODE_TYPES WHERE nodetype = ?";
		try
		{
			unregisterNodeStmt = dbConn.prepareStatement(unregisterNodeSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+unregisterNodeSql, e);
		}
		
		String validateNodeSql = "SELECT count(*) FROM "+dbName+".NODE_TYPES WHERE nodetype = ?";
		try
		{
			validateNodeStmt = dbConn.prepareStatement(validateNodeSql);
		}
		catch (Exception e)
		{
			throw new ConfigurationException("could not prepare statement "+validateNodeSql, e);
		}
	}
	
	private void initDbResources() throws ConfigurationException
	{
		if ((dbDriver != null) && (dbDriver.length() > 0))
		{
		
		    try
		    {
			    Class.forName(dbDriver);
		    }
		    catch (Exception e)
		    {
		    	throw new ConfigurationException("could not load driver class "+dbDriver, e);
		    }
		}
		getConnection();
		createTable();
		prepStatements();
	}
	

	public void init(Properties props) throws ConfigurationException {
		
		
		dbUrl = props.getProperty("org.openecomp.sdnc.sli.jdbc.url");
		if ((dbUrl == null) || (dbUrl.length() == 0))
		{
			throw new ConfigurationException("property org.openecomp.sdnc.sli.jdbc.url unset");
		}
		
		dbName = props.getProperty("org.openecomp.sdnc.sli.jdbc.database");
		if ((dbName == null) || (dbName.length() == 0))
		{
			throw new ConfigurationException("property org.openecomp.sdnc.sli.jdbc.database unset");
		}
		
		dbUser = props.getProperty("org.openecomp.sdnc.sli.jdbc.user");
		if ((dbUser == null) || (dbUser.length() == 0))
		{
			throw new ConfigurationException("property org.openecomp.sdnc.sli.jdbc.user unset");
		}

		
		dbPasswd = props.getProperty("org.openecomp.sdnc.sli.jdbc.password");
		if ((dbPasswd == null) || (dbPasswd.length() == 0))
		{
			throw new ConfigurationException("property org.openecomp.sdnc.sli.jdbc.password unset");
		}
		
		dbDriver = props.getProperty("org.openecomp.sdnc.sli.jdbc.driver");

			
		initDbResources();
		
	}
	
	private boolean isDbConnValid()
	{

		boolean isValid = false;
		
		try
		{
			if (dbConn != null)
			{
				isValid = dbConn.isValid(1);
			}
		}
		catch (SQLException e)
		{}
		
		return(isValid);
	}
public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {


		
		
		if (!isDbConnValid())
		{
			
			// Try reinitializing
			initDbResources();
			
			if (!isDbConnValid())
			{
				throw new ConfigurationException("no jdbc connection");
			}
		}

		
		
		boolean retval = false;
		ResultSet results = null;
		
		PreparedStatement hasGraphStmt = null;
		if (version == null)
		{
			hasGraphStmt = hasActiveGraphStmt;
		}
		else
		{
			hasGraphStmt = hasVersionGraphStmt;
		}
		

		
		try
		{
			hasGraphStmt.setString(1, module);
			hasGraphStmt.setString(2,  rpc);
			hasGraphStmt.setString(3,  mode);

			
			if (version != null)
			{
				hasGraphStmt.setString(4, version);
			}
			boolean oldAutoCommit = dbConn.getAutoCommit();
			dbConn.setAutoCommit(false);
			results = hasGraphStmt.executeQuery();
			dbConn.commit();
			dbConn.setAutoCommit(oldAutoCommit);
			
			if (results.next())
			{
				int cnt = results.getInt(1);
				
				if (cnt > 0)
				{
					retval = true;
				}

			}
		}
		catch (Exception e)
		{
			throw new ConfigurationException("SQL query failed", e);
		}
		finally
		{
			if (results != null)
			{
				try
				{
					
					results.close();
				}
				catch (SQLException x)
				{}
			}
			
		}
		
		
		return(retval);
		
		
	}

	public SvcLogicGraph fetch(String module, String rpc, String version, String mode) throws SvcLogicException {


		
		
		if (!isDbConnValid())
		{
			
			// Try reinitializing
			initDbResources();
			
			if (!isDbConnValid())
			{
				throw new ConfigurationException("no jdbc connection");
			}
		}

		
		
		SvcLogicGraph retval = null;
		ResultSet results = null;
		
		PreparedStatement fetchGraphStmt = null;
		if (version == null)
		{
			fetchGraphStmt = fetchActiveGraphStmt;
		}
		else
		{
			fetchGraphStmt = fetchVersionGraphStmt;
		}
		try
		{
			fetchGraphStmt.setString(1, module);
			fetchGraphStmt.setString(2,  rpc);
			fetchGraphStmt.setString(3,  mode);

			
			if (version != null)
			{
				fetchGraphStmt.setString(4, version);
			}
			boolean oldAutoCommit = dbConn.getAutoCommit();
			dbConn.setAutoCommit(false);
			results = fetchGraphStmt.executeQuery();
			dbConn.commit();
			dbConn.setAutoCommit(oldAutoCommit);
			
			if (results.next())
			{
				Blob graphBlob = results.getBlob("graph");
				
				ObjectInputStream gStream = new ObjectInputStream(graphBlob.getBinaryStream());
				
				Object graphObj = gStream.readObject();
				gStream.close();
				
				if (graphObj instanceof SvcLogicGraph)
				{
					retval = (SvcLogicGraph) graphObj;
				}
				else
				{
					throw new ConfigurationException("invalid type for graph ("+graphObj.getClass().getName());
					
				}
				
			}
			else
			{
				return(null);
			}
		}
		catch (Exception e)
		{
			throw new ConfigurationException("SQL query failed", e);
		}
		finally
		{
			if (results != null)
			{
				try
				{
					results.close();
				}
				catch (SQLException x)
				{}
			}
			
		}
		
		
		return(retval);
		
		
	}

	public void store(SvcLogicGraph graph) throws SvcLogicException {
		
		
		if (!isDbConnValid())
		{
			
			// Try reinitializing
			initDbResources();
			
			if (!isDbConnValid())
			{
				throw new ConfigurationException("no jdbc connection");
			}
		}

		if (graph == null)
		{
			throw new SvcLogicException("graph cannot be null");
		}
		
		byte[] graphBytes = null;
		
		ByteArrayOutputStream byteStr = null;
		ObjectOutputStream goutStr = null;
		
		try
		{
			byteStr = new ByteArrayOutputStream();
			goutStr = new ObjectOutputStream(byteStr);
			goutStr.writeObject(graph);
			
			graphBytes = byteStr.toByteArray();
			
		}
		catch (Exception e)
		{
			throw new SvcLogicException("could not serialize graph", e);
		}
		finally
		{
			
			if (goutStr != null)
			{
				try {
					goutStr.close();
				} catch (IOException e) {
	
				}
			}
			
			if (byteStr != null)
			{
				try {
					byteStr.close();
				} catch (IOException e) {
	
				}
			}
		}
		
		
		// If object already stored in database, delete it
		if (hasGraph(graph.getModule(), graph.getRpc(), graph.getVersion(), graph.getMode()))
		{
			delete(graph.getModule(), graph.getRpc(), graph.getVersion(), graph.getMode());
		}
		
		try
		{
			boolean oldAutoCommit = dbConn.getAutoCommit();
			dbConn.setAutoCommit(false);
			storeGraphStmt.setString(1,  graph.getModule());
			storeGraphStmt.setString(2,  graph.getRpc());
			storeGraphStmt.setString(3, graph.getVersion());
			storeGraphStmt.setString(4, graph.getMode());
			storeGraphStmt.setString(5, "N");
			storeGraphStmt.setBlob(6,  new ByteArrayInputStream(graphBytes));
			
			storeGraphStmt.executeUpdate();
			dbConn.commit();
			
			dbConn.setAutoCommit(oldAutoCommit);
		}
		catch (Exception e)
		{
			throw new SvcLogicException("Could not write object to database", e);
		}	
	}
	
	public void delete(String module, String rpc, String version, String mode) throws SvcLogicException
	{		
		if (!isDbConnValid())
		{
			
			// Try reinitializing
			initDbResources();
			
			if (!isDbConnValid())
			{
				throw new ConfigurationException("no jdbc connection");
			}
		}

		try
		{
			boolean oldAutoCommit = dbConn.getAutoCommit();
			dbConn.setAutoCommit(false);
			deleteGraphStmt.setString(1,  module);
			deleteGraphStmt.setString(2,  rpc);
			deleteGraphStmt.setString(3, version);
			deleteGraphStmt.setString(4,  mode);

			
			deleteGraphStmt.executeUpdate();
			dbConn.commit();
			dbConn.setAutoCommit(oldAutoCommit);
		}
		catch (Exception e)
		{
			throw new SvcLogicException("Could not delete object from database", e);
		}	
	}

	public void activate(SvcLogicGraph graph) throws SvcLogicException
	{
		try
		{
			boolean oldAutoCommit = dbConn.getAutoCommit();
			
			dbConn.setAutoCommit(false);
			
			// Deactivate any current active version
			deactivateStmt.setString(1,  graph.getModule());
			deactivateStmt.setString(2, graph.getRpc());
			deactivateStmt.setString(3, graph.getMode());
			deactivateStmt.executeUpdate();
			
			// Activate this version
			activateStmt.setString(1,  graph.getModule());
			activateStmt.setString(2, graph.getRpc());
			activateStmt.setString(3, graph.getVersion());
			activateStmt.setString(4, graph.getMode());
			activateStmt.executeUpdate();
			
			dbConn.commit();
			
			dbConn.setAutoCommit(oldAutoCommit);
			
		}
		catch (Exception e)
		{
			throw new SvcLogicException("Could not activate graph", e);
		}
	}

	@Override
	public void registerNodeType(String nodeType) throws SvcLogicException {
		
		if (isValidNodeType(nodeType))
		{
			return;
		}
		
		if (!isDbConnValid())
		{
			
			// Try reinitializing
			initDbResources();
			
			if (!isDbConnValid())
			{
				throw new ConfigurationException("no jdbc connection");
			}
		}
		
		try
		{
			boolean oldAutoCommit = dbConn.getAutoCommit();
			dbConn.setAutoCommit(false);
			registerNodeStmt.setString(1,  nodeType);
			registerNodeStmt.executeUpdate();
			dbConn.commit();
			dbConn.setAutoCommit(oldAutoCommit);
		}
		catch (Exception e)
		{
			throw new SvcLogicException("Could not add node type to database", e);
		}
		
	}

	@Override
	public void unregisterNodeType(String nodeType) throws SvcLogicException {
		
		if (!isValidNodeType(nodeType))
		{
			return;
		}
		
		if (!isDbConnValid())
		{
			
			// Try reinitializing
			initDbResources();
			
			if (!isDbConnValid())
			{
				throw new ConfigurationException("no jdbc connection");
			}
		}
		
		try
		{
			boolean oldAutoCommit = dbConn.getAutoCommit();
			dbConn.setAutoCommit(false);
			unregisterNodeStmt.setString(1,  nodeType);
			unregisterNodeStmt.executeUpdate();
			dbConn.commit();
			dbConn.setAutoCommit(oldAutoCommit);
		}
		catch (Exception e)
		{
			throw new SvcLogicException("Could not delete node type from database", e);
		}
		
	}

	@Override
	public boolean isValidNodeType(String nodeType) throws SvcLogicException {
		
		boolean isValid = false;
		
		if (!isDbConnValid())
		{
			
			// Try reinitializing
			initDbResources();
			
			if (!isDbConnValid())
			{
				throw new ConfigurationException("no jdbc connection");
			}
		}
		
		ResultSet results = null;
		try
		{
			validateNodeStmt.setString(1, nodeType);
			
			boolean oldAutoCommit = dbConn.getAutoCommit();
			dbConn.setAutoCommit(false);
			results = validateNodeStmt.executeQuery();
			dbConn.commit();
			dbConn.setAutoCommit(oldAutoCommit);
			
			if (results != null)
			{
				if (results.next())
				{
					int cnt = results.getInt(1);
					
					if (cnt > 0)
					{
						isValid = true;
					}
				}
			}
			
		}
		catch (Exception e)
		{
			throw new SvcLogicException("Cannot select node type from database", e);
		}
		finally
		{
			if (results != null)
			{
				try
				{
					results.close();
				}
				catch (SQLException x)
				{}
			}
			
		}
		
		return(isValid);
	}
	
	
}
