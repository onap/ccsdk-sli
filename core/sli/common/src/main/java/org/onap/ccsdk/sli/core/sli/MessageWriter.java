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

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.sql.rowset.CachedRowSet;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class MessageWriter {

	private static final Logger LOG = LoggerFactory.getLogger(MessageWriter.class);

	private static final String DBLIB_SERVICE = "org.onap.ccsdk.sli.core.dblib.DBResourceManager";
	private static final String SVCLOGIC_PROP_VAR = "SDNC_SLI_PROPERTIES";
	private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";

	private static final String INCOMING_PROPERTY_NAME = "org.onap.ccsdk.sli.MessageWriter.writeIncomingRequests";
	private static final String OUTGOING_PROPERTY_NAME = "org.onap.ccsdk.sli.MessageWriter.writeOutgoingRequests";

	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static DbLibService dbLibService = null;

	private static boolean incomingEnabled = false;
	private static boolean outgoingEnabled = false;

	private static boolean initialized = false;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void init() {
		if (initialized)
			return;

		initialized = true;

		// Read properties
		Properties props = new Properties();
		String propPath = System.getenv(SVCLOGIC_PROP_VAR);

		if (propPath == null) {
			String propDir = System.getenv(SDNC_CONFIG_DIR);
			if (propDir == null) {
				propDir = "/opt/sdnc/data/properties";
			}
			propPath = propDir + "/svclogic.properties";
			LOG.warn("Environment variable " + SVCLOGIC_PROP_VAR + " unset - defaulting to " + propPath);
		}

		File propFile = new File(propPath);

		if (!propFile.exists()) {
			LOG.warn("Property file does not exist: " + propPath);
		}

		try {
			props.load(new FileInputStream(propFile));
		} catch (Exception e) {
			LOG.warn("Error loading property file: " + propPath, e);
		}

		incomingEnabled = Boolean.valueOf(props.getProperty(INCOMING_PROPERTY_NAME, "false"));
		outgoingEnabled = Boolean.valueOf(props.getProperty(OUTGOING_PROPERTY_NAME, "false"));

		LOG.info(INCOMING_PROPERTY_NAME + ": " + incomingEnabled);
		LOG.info(OUTGOING_PROPERTY_NAME + ": " + outgoingEnabled);
	}

	public static void saveOutgoingRequest(
	        String requestId,
	        String serviceInstanceId,
	        String targetUrl,
	        String request) {
		try {
			init();

			if (!outgoingEnabled)
				return;

			if (serviceInstanceId == null || serviceInstanceId.trim().length() == 0)
				serviceInstanceId = "NA";

			int seqnum = getLastSequenceNumber("OUTGOING_MESSAGE", requestId) + 1;
			String now = df.format(new Date());

			String sql = "INSERT INTO OUTGOING_MESSAGE (\n" +
			        "	request_id, sequence_number, service_instance_id, target_url, request, start_time)\n" +
			        "VALUES (?, ?, ?, ?, ?, ?)";

			ArrayList<String> data = new ArrayList<>();
			data.add(requestId);
			data.add(String.valueOf(seqnum));
			data.add(serviceInstanceId);
			data.add(targetUrl);
			data.add(request);
			data.add(now);

			dbLibService.writeData(sql, data, null);

		} catch (Exception e) {
			LOG.warn("Failed to save outgoing request for request-id: " + requestId, e);
		}
	}

	public static void saveOutgoingResponse(String requestId, int httpResponseCode, String response) {
		try {
			init();

			if (!outgoingEnabled)
				return;

			int seqnum = getLastSequenceNumber("OUTGOING_MESSAGE", requestId);
			if (seqnum == 0) {
				LOG.warn("Failed to save outgoing response for request-id: " + requestId +
				        ": Request record not found in OUTGOING_MESSAGE");
				return;
			}

			String now = df.format(new Date());

			String sql = "UPDATE OUTGOING_MESSAGE SET http_response_code = ?, response = ?,\n" +
			        "	duration = timestampdiff(MICROSECOND, start_time, ?) / 1000\n" +
			        "WHERE request_id = ? AND sequence_number = ?";

			ArrayList<String> data = new ArrayList<>();
			data.add(String.valueOf(httpResponseCode));
			data.add(response);
			data.add(now);
			data.add(requestId);
			data.add(String.valueOf(seqnum));

			dbLibService.writeData(sql, data, null);

		} catch (Exception e) {
			LOG.warn("Failed to save outgoing response for request-id: " + requestId, e);
		}
	}

	public static void saveIncomingRequest(
	        String requestId,
	        String serviceInstanceId,
	        String requestHost,
	        String request) {
		try {
			init();

			if (!incomingEnabled)
				return;

			if (serviceInstanceId == null || serviceInstanceId.trim().length() == 0)
				serviceInstanceId = "NA";

			int seqnum = getLastSequenceNumber("INCOMING_MESSAGE", requestId) + 1;
			String now = df.format(new Date());

			String sql = "INSERT INTO INCOMING_MESSAGE (\n" +
			        "	request_id, sequence_number, service_instance_id, request_host, request, start_time)\n" +
			        "VALUES (?, ?, ?, ?, ?, ?)";

			ArrayList<String> data = new ArrayList<>();
			data.add(requestId);
			data.add(String.valueOf(seqnum));
			data.add(serviceInstanceId);
			data.add(requestHost);
			data.add(request);
			data.add(now);

			dbLibService.writeData(sql, data, null);

		} catch (Exception e) {
			LOG.warn("Failed to save incoming request for request-id: " + requestId, e);
		}
	}

	public static void saveIncomingResponse(String requestId, int httpResponseCode, String response) {
		try {
			init();

			if (!incomingEnabled)
				return;

			int seqnum = getLastSequenceNumber("INCOMING_MESSAGE", requestId);
			if (seqnum == 0) {
				LOG.warn("Failed to save response for request-id: " + requestId +
				        ": Request record not found in INCOMING_MESSAGE");
				return;
			}

			String now = df.format(new Date());

			String sql = "UPDATE INCOMING_MESSAGE SET http_response_code = ?, response = ?,\n" +
			        "	duration = timestampdiff(MICROSECOND, start_time, ?) / 1000\n" +
			        "WHERE request_id = ? AND sequence_number = ?";

			ArrayList<String> data = new ArrayList<>();
			data.add(String.valueOf(httpResponseCode));
			data.add(response);
			data.add(now);
			data.add(requestId);
			data.add(String.valueOf(seqnum));

			dbLibService.writeData(sql, data, null);

		} catch (Exception e) {
			LOG.warn("Failed to save response for request-id: " + requestId, e);
		}
	}

	public static String getServiceInstanceId(String requestId) throws SQLException {
		init();

		String sql = "SELECT service_instance_id FROM OUTGOING_MESSAGE WHERE request_id = '" + requestId +
		        "' ORDER BY sequence_number DESC";

		CachedRowSet rs = null;
		try {
			rs = dbLibService.getData(sql, null, null);
			if (rs.next()) {
				return rs.getString("service_instance_id");
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					LOG.warn("Failed to close CachedRowSet", e);
				}
			}
		}
		return null;
	}

	private static int getLastSequenceNumber(String tableName, String requestId) throws SQLException {
		String sql = "SELECT sequence_number FROM " + tableName + " WHERE request_id = '" + requestId +
		        "' ORDER BY sequence_number DESC";

		CachedRowSet rs = null;
		try {
			rs = dbLibService.getData(sql, null, null);
			if (rs.next()) {
				return rs.getInt("sequence_number");
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					LOG.warn("Failed to close CachedRowSet", e);
				}
			}
		}
		return 0;
	}
}
