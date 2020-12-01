package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.*;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import org.junit.Test;
import org.onap.ccsdk.sli.core.dblib.config.BaseDBConfiguration;
import org.onap.ccsdk.sli.core.dblib.config.JDBCConfiguration;
import org.slf4j.LoggerFactory;

public class TerminatingCachedDataSourceTest {

    @Test
    public void testTerminatingCachedDataSource() {
        Properties props = new Properties();
        BaseDBConfiguration config = new JDBCConfiguration(props);
        CachedDataSource ds = new TerminatingCachedDataSource(config);
        assertNotNull(ds);
    }

    @Test
    public void testConfigure() {
        Properties props = new Properties();
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
        BaseDBConfiguration config = new JDBCConfiguration(props);

        CachedDataSource ds = new TerminatingCachedDataSource(config);
        assertNull(ds.configure(config));
    }

    @Test
    public void testSetInitialDelay() {
        Properties props = new Properties();
        BaseDBConfiguration config = new JDBCConfiguration(props);
        CachedDataSource ds = new TerminatingCachedDataSource(config);
        ds.setInitialDelay(1000L);
        assertTrue(ds.getInitialDelay() == 1000L);
    }

    @Test
    public void testSetInterval() {
        Properties props = new Properties();
        BaseDBConfiguration config = new JDBCConfiguration(props);
        CachedDataSource ds = new TerminatingCachedDataSource(config);
        ds.setInterval(1000L);
        assertTrue(ds.getInterval() == 1000L);
    }

    @Test
    public void testSetExpectedCompletionTime() {
        Properties props = new Properties();
        BaseDBConfiguration config = new JDBCConfiguration(props);
        CachedDataSource ds = new TerminatingCachedDataSource(config);
        ds.setExpectedCompletionTime(100L);
        assertTrue(ds.getExpectedCompletionTime() == 100L);
    }

    @Test
    public void testSetUnprocessedFailoverThreshold() {
        Properties props = new Properties();
        BaseDBConfiguration config = new JDBCConfiguration(props);
        CachedDataSource ds = new TerminatingCachedDataSource(config);
        ds.setUnprocessedFailoverThreshold(100L);
        assertTrue(ds.getUnprocessedFailoverThreshold() == 100L);
    }

    @Test
    public void testGetParentLogger() {
        Properties props = new Properties();
        BaseDBConfiguration config = new JDBCConfiguration(props);
        CachedDataSource ds = new TerminatingCachedDataSource(config);
        ds.setInterval(100L);
        try {
            assertNull(ds.getParentLogger());
        } catch (SQLFeatureNotSupportedException e) {
            LoggerFactory.getLogger(TerminatingCachedDataSourceTest.class).warn("Test Failure", e);
        }
    }
}