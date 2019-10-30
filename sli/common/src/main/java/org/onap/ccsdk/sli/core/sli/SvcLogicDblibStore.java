/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
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

package org.onap.ccsdk.sli.core.sli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import javax.sql.rowset.CachedRowSet;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvcLogicDblibStore implements SvcLogicStore {
	private static final Logger LOG = LoggerFactory.getLogger(SvcLogicDblibStore.class);
	private DbLibService dbSvc;

	public SvcLogicDblibStore(DbLibService dbsvc) {
		this.dbSvc = dbsvc;
	}

	public Connection getConnection() throws SQLException {
	    return(dbSvc.getConnection());
	}

	@Override
	public void init(Properties props) throws ConfigurationException {
		if(dbSvc == null) {
			LOG.error("SvcLogic cannot acquire DBLIB_SERVICE");
			return;
		}
		try {
			dbSvc.getData("select 1 from DUAL", new ArrayList<String>(), null);
			LOG.debug("SQL test was successful");
		} catch (SQLException e) {
			LOG.error("Failed SQL test", e);
		}
	}

	@Override
	public boolean hasGraph(String module, String rpc, String version,
			String mode) throws SvcLogicException {
		boolean retval = false;
		CachedRowSet results = null;
		String hasVersionGraphSql = "SELECT count(*) FROM SVC_LOGIC"
				+ " WHERE module = ? AND rpc = ? AND mode = ? AND version = ?";

		String hasActiveGraphSql = "SELECT count(*) FROM SVC_LOGIC"
				+ " WHERE module = ? AND rpc = ? AND mode = ? AND active = 'Y'";

		ArrayList<String> args = new ArrayList<>();
		args.add(module);
		args.add(rpc);
		args.add(mode);

		try {

			if (version == null) {
				results = dbSvc.getData(hasActiveGraphSql, args, null);
			} else {
				args.add(version);
				results = dbSvc.getData(hasVersionGraphSql, args, null);
			}

			if (results.next()) {
				int cnt = results.getInt(1);

				if (cnt > 0) {
					retval = true;
				}
			}
		} catch (Exception e) {
			throw new ConfigurationException("SQL query failed", e);
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (SQLException x) {
					LOG.error("Failed to close CachedRowSet", x);
				}
			}

		}

		return retval;
	}

	public SvcLogicGraph fetch(String module, String rpc, String version,
			String mode) throws SvcLogicException {

		PreparedStatement fetchGraphStmt = null;
		Connection dbConn = null;
		SvcLogicGraph retval = null;
		ResultSet results = null;

		String fetchVersionGraphSql = "SELECT graph FROM SVC_LOGIC"
				+ " WHERE module = ? AND rpc = ? AND mode = ? AND version = ?";

		String fetchActiveGraphSql = "SELECT graph FROM SVC_LOGIC"
				+ " WHERE module = ? AND rpc = ? AND mode = ? AND active = 'Y'";


		try {
			dbConn = dbSvc.getConnection();

			if (version == null) {
				fetchGraphStmt = dbConn.prepareStatement(fetchActiveGraphSql);
			} else {
				fetchGraphStmt = dbConn.prepareStatement(fetchVersionGraphSql);
			}

			fetchGraphStmt.setString(1, module);
			fetchGraphStmt.setString(2,  rpc);
			fetchGraphStmt.setString(3, mode);
			if (version != null) {
				fetchGraphStmt.setString(4,version);
			}

			results = fetchGraphStmt.executeQuery();

			if (results.next()) {
				Blob graphBlob = results.getBlob("graph");

				ObjectInputStream gStream = new ObjectInputStream(graphBlob.getBinaryStream());

				Object graphObj = gStream.readObject();
				gStream.close();

				if (graphObj instanceof SvcLogicGraph) {
					retval = (SvcLogicGraph) graphObj;
				} else {
					throw new ConfigurationException("invalid type for graph ("
							+ graphObj.getClass().getName());

				}

			} else {
				return null;
			}
		} catch (Exception e) {
		    LOG.error("Graph processing failed", e);
			throw new ConfigurationException("Graph processing failed: " + e.getMessage());
		} finally {
			try {
				if (fetchGraphStmt != null) {
					fetchGraphStmt.close();
				}
			} catch (SQLException e) {
				LOG.error("PreparedStatement close error", e);
			}
			if (results != null) {
				try {
					results.close();
				} catch (SQLException x) {
					LOG.error("ResultSet close error", x);
				}
			}
			try {
				if (dbConn != null && !dbConn.isClosed()) {
					dbConn.close();
				}
			} catch (Exception exc) {
				LOG.error("dbConn close error", exc);
			} finally {
				dbConn = null;
			}

		}

		return retval;
	}

	public void store(SvcLogicGraph graph) throws SvcLogicException {



		String storeGraphSql = "INSERT INTO SVC_LOGIC (module, rpc, version, mode, active, graph, md5sum)"
				+ " VALUES(?, ?, ?, ?, ?, ?, ?)";

		if (graph == null) {
			throw new SvcLogicException("graph cannot be null");
		}

		byte[] graphBytes = null;

		try (ByteArrayOutputStream byteStr = new ByteArrayOutputStream();
			ObjectOutputStream goutStr = new ObjectOutputStream(byteStr)) {

			goutStr.writeObject(graph);

			graphBytes = byteStr.toByteArray();

		} catch (Exception e) {
			throw new SvcLogicException("could not serialize graph", e);
		}

		// If object already stored in database, delete it
		if (hasGraph(graph.getModule(), graph.getRpc(), graph.getVersion(),
				graph.getMode())) {
			delete(graph.getModule(), graph.getRpc(), graph.getVersion(),
					graph.getMode());
		}

		Connection dbConn = null;
        PreparedStatement storeGraphStmt = null;
		try {
            dbConn = dbSvc.getConnection();
            boolean oldAutoCommit = dbConn.getAutoCommit();
			dbConn.setAutoCommit(false);
            storeGraphStmt = dbConn.prepareStatement(storeGraphSql);
			storeGraphStmt.setString(1, graph.getModule());
			storeGraphStmt.setString(2, graph.getRpc());
			storeGraphStmt.setString(3, graph.getVersion());
			storeGraphStmt.setString(4, graph.getMode());
			storeGraphStmt.setString(5, "N");
			storeGraphStmt.setBlob(6, new ByteArrayInputStream(graphBytes));
			storeGraphStmt.setString(7, graph.getMd5sum());
			storeGraphStmt.executeUpdate();
			dbConn.commit();
            dbConn.setAutoCommit(oldAutoCommit);
		} catch (Exception e) {
			throw new SvcLogicException("Could not write object to database", e);
		} finally {
			try {
				if (storeGraphStmt != null) {
					storeGraphStmt.close();
				}
			} catch (SQLException e) {
				LOG.error("PreparedStatement close error", e);
			}
			try {
				if (dbConn != null && !dbConn.isClosed()) {
					dbConn.close();
				}
			} catch (Exception exc) {
				LOG.error("dbConn close error", exc);
			} finally {
				dbConn = null;
			}

		}
	}

	public void delete(String module, String rpc, String version, String mode)
			throws SvcLogicException {
		String deleteGraphSql = "DELETE FROM SVC_LOGIC WHERE module = ? AND rpc = ? AND version = ? AND mode = ?";

		ArrayList<String> args = new ArrayList<>();

		args.add(module);
		args.add(rpc);
		args.add(version);
		args.add(mode);

		try {
			dbSvc.writeData(deleteGraphSql, args, null);
		} catch (Exception e) {
			throw new SvcLogicException("Could not delete object from database", e);
		}
	}

	public void activate(SvcLogicGraph graph) throws SvcLogicException {
		String deactivateSql = "UPDATE SVC_LOGIC SET active = 'N' WHERE module = ? AND rpc = ? AND mode = ?";
		String activateSql = "UPDATE SVC_LOGIC SET active = 'Y' WHERE module = ? AND rpc = ? AND mode = ? AND version = ?";

		ArrayList<String> args = new ArrayList<>();

		args.add(graph.getModule());
		args.add(graph.getRpc());
		args.add(graph.getMode());

		try {
			dbSvc.writeData(deactivateSql, args, null);
			args.add(graph.getVersion());
			dbSvc.writeData(activateSql, args, null);
		} catch (Exception e) {
			throw new SvcLogicException("Could not activate graph", e);
		}
	}

    @Override
    public void activate(String module, String rpc, String version, String mode) throws SvcLogicException {

        String deactivateSql = "UPDATE SVC_LOGIC SET active = 'N' WHERE module = ? AND rpc = ? AND mode = ?";

        String activateSql = "UPDATE SVC_LOGIC SET active = 'Y' WHERE module = ? AND rpc = ? AND mode = ? AND version = ?";

        ArrayList<String> args = new ArrayList<>();

        args.add(module);
        args.add(rpc);
        args.add(mode);

        try {

            dbSvc.writeData(deactivateSql, args, null);

            args.add(version);
            dbSvc.writeData(activateSql, args, null);

        } catch (Exception e) {
            throw new SvcLogicException("Could not activate graph", e);
        }        
    }

}
