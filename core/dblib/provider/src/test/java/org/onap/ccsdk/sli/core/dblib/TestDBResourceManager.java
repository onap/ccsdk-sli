package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

public class TestDBResourceManager {

    private static final AtomicInteger DB_SEQ = new AtomicInteger(0);

    DbLibService dblibSvc;
    DBResourceManager dbm;

    @Before
    public void setUp() throws Exception {
        InputStream propStr = getClass().getResourceAsStream("/dblib.properties");

        Properties props = new Properties();

        props.load(propStr);

        // Override jdbc URL, driver, and database name for Derby embedded.
        // Use a unique DB name per setUp() call so each test gets a fresh in-memory database.
        String dbName = "testdbrmgr" + DB_SEQ.incrementAndGet();
        props.setProperty("org.onap.ccsdk.sli.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        props.setProperty("org.onap.ccsdk.sli.jdbc.database", dbName);
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
