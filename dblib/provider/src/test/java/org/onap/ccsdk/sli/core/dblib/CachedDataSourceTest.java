package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.sli.core.dblib.config.BaseDBConfiguration;
import org.onap.ccsdk.sli.core.dblib.config.JDBCConfiguration;
import org.onap.ccsdk.sli.core.dblib.jdbc.JdbcDBCachedDataSource;
import org.slf4j.LoggerFactory;

public class CachedDataSourceTest {

	private static final Properties props = new Properties();
	private static BaseDBConfiguration config;
	private static CachedDataSource ds;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		props.setProperty("org.onap.ccsdk.sli.dbtype", "jdbc");
		props.setProperty("org.onap.ccsdk.sli.jdbc.hosts", "localhost");
		props.setProperty("org.onap.ccsdk.sli.jdbc.url", "jdbc:mysql://dbhost:3306/test");
		props.setProperty("org.onap.ccsdk.sli.jdbc.driver", "org.mariadb.jdbc.Driver");
		props.setProperty("org.onap.ccsdk.sli.jdbc.database", "test");
		props.setProperty("org.onap.ccsdk.sli.jdbc.user", "dbuser");
		props.setProperty("org.onap.ccsdk.sli.jdbc.password", "passw0rd");
		props.setProperty("org.onap.ccsdk.sli.jdbc.connection.name", "testdb01");
		props.setProperty("org.onap.ccsdk.sli.jdbc.connection.timeout", "50");
		props.setProperty("org.onap.ccsdk.sli.jdbc.request.timeout", "100");
		props.setProperty("org.onap.ccsdk.sli.jdbc.limit.init", "10");
		props.setProperty("org.onap.ccsdk.sli.jdbc.limit.min", "10");
		props.setProperty("org.onap.ccsdk.sli.jdbc.limit.max", "20");
		props.setProperty("org.onap.dblib.connection.recovery", "false");

		config = new JDBCConfiguration(props);
		ds = new JdbcDBCachedDataSource(config);
	}

	@Test
	public void testCachedDataSource() {
		assertNotNull(ds);
	}

	@Test
	public void testConfigure() {

		assertNotNull(ds.configure(config));
	}

	@Test
	public void testSetInitialDelay() {
		ds.setInitialDelay(1000L);
		assertTrue(ds.getInitialDelay() == 1000L);
	}

	@Test
	public void testSetInterval() {
		ds.setInterval(1000L);
		assertTrue(ds.getInterval() == 1000L);
	}

	@Test
	public void testSetExpectedCompletionTime() {
		ds.setExpectedCompletionTime(100L);
		assertTrue(ds.getExpectedCompletionTime() == 100L);
	}

	@Test
	public void testSetUnprocessedFailoverThreshold() {
		ds.setUnprocessedFailoverThreshold(100L);
		assertTrue(ds.getUnprocessedFailoverThreshold() == 100L);
	}

	@Test
	public void testGetParentLogger() {
		try {
			assertNull(ds.getParentLogger());
		} catch (SQLFeatureNotSupportedException e) {
			LoggerFactory.getLogger(CachedDataSourceTest.class).warn("Test Failure", e);
		}
	}

	@Test
	public void testGettersForJdbcDBCachedDataSource() {

		assertEquals("jdbc:mysql://dbhost:3306/test", ((JdbcDBCachedDataSource) ds).getDbUrl());
		assertEquals("dbuser", ((JdbcDBCachedDataSource) ds).getDbUserId());
		assertEquals("passw0rd", ((JdbcDBCachedDataSource) ds).getDbPasswd());
		assertEquals("testdb01", ((JdbcDBCachedDataSource) ds).toString());
	}
}