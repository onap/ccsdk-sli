package org.onap.ccsdk.sli.northbound.dmaapclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SdncDhcpEventConsumer extends SdncDmaapConsumerImpl {
	private static final Logger LOG = LoggerFactory.getLogger(SdncDhcpEventConsumer.class);

	private static final String MAC_ADDR_TAG = "macaddr";
	private static final String MSG_NAME_TAG = "msg_name";
	private static final String IP_ADDR_TAG = "yiaddr";

	private static DBResourceManager jdbcDataSource = null;
	private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";

	private class MissingDhcpAttributeException extends InvalidMessageException {

		public MissingDhcpAttributeException(String fieldName) {
			super("Invalid DHCP event - missing " + fieldName + " attribute");
		}
	}

	private static void setJdbcDataSource() throws IOException {

		String propPath;
		String propDir = System.getenv(SDNC_CONFIG_DIR);
		if (propDir == null) {
			propDir = "/opt/onap/sdnc/data/properties";
		}
		propPath = propDir + "/dblib.properties";
		File propFile = new File(propPath);

		if (!propFile.exists()) {

			throw new FileNotFoundException("Missing configuration properties file : " + propFile);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(propFile));

		setJdbcDataSource(new DBResourceManager(props));

	}

	static void setJdbcDataSource(DBResourceManager dbMgr) {

		jdbcDataSource = dbMgr;

		if (jdbcDataSource.isActive()) {
			LOG.warn("DBLIB: JDBC DataSource has been initialized.");
		} else {
			LOG.warn("DBLIB: JDBC DataSource did not initialize successfully.");
		}
	}

	@Override
	public void processMsg(String msg) throws InvalidMessageException {
		if (msg == null) {
			throw new InvalidMessageException("Null message");
		}

		ObjectMapper oMapper = new ObjectMapper();

		JsonNode dhcpRootNode;
		String msgName;
		String macAddr;
		String ipAddr;

		try {
			dhcpRootNode = oMapper.readTree(msg);

		} catch (IOException e) {
			throw new InvalidMessageException("Cannot parse json object", e);
		}

		JsonNode msgNameNode = dhcpRootNode.get(MSG_NAME_TAG);
		if (msgNameNode != null) {
			msgName = msgNameNode.textValue();

		} else {
			throw new MissingDhcpAttributeException(MSG_NAME_TAG);
		}

		JsonNode macAddrNode = dhcpRootNode.get(MAC_ADDR_TAG);
		if (macAddrNode != null) {
			macAddr = macAddrNode.textValue();

		} else {
			throw new MissingDhcpAttributeException(MAC_ADDR_TAG);
		}

		JsonNode ipAddrNode = dhcpRootNode.get(IP_ADDR_TAG);
		if (ipAddrNode != null) {
			ipAddr = ipAddrNode.textValue();

		} else {
			throw new MissingDhcpAttributeException(IP_ADDR_TAG);
		}

		LOG.debug("Got DHCP event : msg name {}; mac addr {}; ip addr {}", msgName, macAddr, ipAddr);

		if (jdbcDataSource == null) {
			try {
				setJdbcDataSource();
			} catch (IOException e) {
				LOG.error("Could not create JDBC connection", e);
				return;
			}
		}

		try {

			jdbcDataSource.writeData("INSERT INTO DHCP_MAP(mac_addr, ip_addr) VALUES('" + macAddr + "','" + ipAddr + "') ON DUPLICATE KEY UPDATE ip_addr = '"+ipAddr+"'", null, null);

		} catch (SQLException e) {
			LOG.error("Could not insert DHCP event data into the database ", e);
		}

	}

}
