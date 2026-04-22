/**
 *
 */
package org.onap.ccsdk.sli.northbound.dmaapclient;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dt5972
 *
 */
public class TestSdncDhcpEventConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(TestSdncDhcpEventConsumer.class);


	private static final String DHCP_MAP_TABLE = "CREATE TABLE DHCP_MAP (" +
			" mac_addr varchar(80) NOT NULL," +
			" ip_addr varchar(80)," +
			" PRIMARY KEY(mac_addr)" +
			")";



	private static final String VALID_DHCP_EVENT =  "{\"msg_name\":\"DHCPACK\"," +
												     "\"macaddr\":\"fa:16:3e:8f:ea:68\"," +
												     "\"yiaddr\":\"10.3.0.2\"}";
	private static final String SECOND_DHCP_EVENT =  "{\"msg_name\":\"DHCPACK\"," +
		     "\"macaddr\":\"fa:16:3e:8f:ea:68\"," +
		     "\"yiaddr\":\"10.3.0.3\"}";
	private static final String MISSING_MSG_NAME_DHCP_EVENT =  "{\"macaddr\":\"fa:16:3e:8f:ea:68\"," +
												              "\"yiaddr\":\"10.3.0.2\"}";
	private static final String MISSING_MAC_ADDR_DHCP_EVENT =   "{\"msg_name\":\"DHCPACK\"," +
		     													"\"yiaddr\":\"10.3.0.2\"}";
	private static final String MISSING_IP_ADDR_DHCP_EVENT =  "{\"msg_name\":\"DHCPACK\"," +
														"\"macaddr\":\"fa:16:3e:8f:ea:68\"}";

	private static final String GET_DHCP_MAPPING = "SELECT * FROM DHCP_MAP WHERE mac_addr = 'fa:16:3e:8f:ea:68'";


	private static DBResourceManager dblibSvc;

	private static SdncDhcpEventConsumer consumer;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		LOG.debug("Setting up DHCP event testing");

		InputStream propStr = TestSdncDhcpEventConsumer.class.getResourceAsStream("/dblib.properties");

		Properties props = new Properties();

		props.load(propStr);


		// Override jdbc URL, driver, user, and database name for Derby embedded
		String dbName = "dmaaptest";
		props.setProperty("org.onap.ccsdk.sli.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
		props.setProperty("org.onap.ccsdk.sli.jdbc.database", dbName);
		props.setProperty("org.onap.ccsdk.sli.jdbc.url", "jdbc:derby:memory:" + dbName + ";create=true");
		props.setProperty("org.onap.ccsdk.sli.jdbc.user", "sa");
		props.setProperty("org.onap.ccsdk.sli.jdbc.password", "");


		// Create dblib connection

		LOG.debug("Getting DBResourceManager instance");
		dblibSvc = new DBResourceManager(props);

		// Create DHCP_MAP table
		dblibSvc.writeData(DHCP_MAP_TABLE, null, null);

		consumer = new SdncDhcpEventConsumer();
		consumer.setJdbcDataSource(dblibSvc);
		LOG.debug("Setup complete");

	}


	@Test
	public void testValid() throws InvalidMessageException, SQLException {
		consumer.processMsg(VALID_DHCP_EVENT);
		consumer.processMsg(SECOND_DHCP_EVENT);

		CachedRowSet results = dblibSvc.getData(GET_DHCP_MAPPING, null, null);

		if (!results.next()) {
			fail("Test query ["+GET_DHCP_MAPPING+"] returned no data");
		} else {
			String ipAddr = results.getString("ip_addr");
			if (!"10.3.0.3".equals(ipAddr)) {
				fail("Expecting ipAddr to be 10.3.0.3, but was "+ipAddr);
			}
		}

	}

	@Test (expected = InvalidMessageException.class)
	public void testMissingMsgName() throws InvalidMessageException {
		consumer.processMsg(MISSING_MSG_NAME_DHCP_EVENT);
	}

	@Test (expected = InvalidMessageException.class)
	public void testMissingMacAddress() throws InvalidMessageException {
		consumer.processMsg(MISSING_MAC_ADDR_DHCP_EVENT);
	}

	@Test (expected = InvalidMessageException.class)
	public void testMissingIpAddress() throws InvalidMessageException {
		consumer.processMsg(MISSING_IP_ADDR_DHCP_EVENT);
	}
}
