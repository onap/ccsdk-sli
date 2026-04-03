package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class TestDBResourceManager {

    DbLibService dblibSvc;
    DBResourceManager dbm;

    @Before
    public void setUp() throws Exception {
        URL propUrl = getClass().getResource("/dblib.properties");

        InputStream propStr = getClass().getResourceAsStream("/dblib.properties");

        Properties props = new Properties();

        props.load(propStr);

        // Override jdbc URL/driver and database name for embedded Derby.
        String dbName = "test_" + UUID.randomUUID().toString().replace("-", "");
        props.setProperty("org.onap.ccsdk.sli.jdbc.database", dbName);
        props.setProperty("org.onap.ccsdk.sli.jdbc.driver", "org.apache.derby.iapi.jdbc.AutoloadedDriver");
        props.setProperty("org.onap.ccsdk.sli.jdbc.url", "jdbc:derby:memory:" + dbName + ";create=true");

        dblibSvc = new DBResourceManager(props);
        dbm = new DBResourceManager(props);
        dblibSvc.writeData("CREATE TABLE DBLIB_TEST (name varchar(20))", null, null);
        dblibSvc.getData("SELECT * FROM DBLIB_TEST", null, null);

    }

    @Test
    public void testForceRecovery() {
        dbm.testForceRecovery();
    }

    @Test
    public void testGetConnection() throws SQLException {
        assertNotNull(dbm.getConnection());
        assertNotNull(dbm.getConnection("testUser", "testPaswd"));
    }

    @Test
    public void testCleanup() {
        dbm.cleanUp();

    }

    @Test
    public void testGetLogWriter() throws SQLException {
        assertNull(dbm.getLogWriter());
    }

}
