/*-
   2  * ============LICENSE_START=======================================================
   3  * ONAP CCSDK
   4  * ================================================================================
   5  * Copyright (C) 2019 AT&T Intellectual Property. All rights
   6  *                             reserved.
   7  * ================================================================================
   8  * Licensed under the Apache License, Version 2.0 (the "License");
   9  * you may not use this file except in compliance with the License.
  10  * You may obtain a copy of the License at
  11  *
  12  * http://www.apache.org/licenses/LICENSE-2.0
  13  *
  14  * Unless required by applicable law or agreed to in writing, software
  15  * distributed under the License is distributed on an "AS IS" BASIS,
  16  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  17  * See the License for the specific language governing permissions and
  18  * limitations under the License.
  19  * ============LICENSE_END============================================
  20  * ===================================================================
  21  *
  22  */
package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

public class TestDBResourceManager2 {

    DbLibService dblibSvc;
    DBResourceManager dbm;

    @Before
    public void setUp() throws Exception {
        URL propUrl = getClass().getResource("/dblib.properties");

        InputStream propStr = getClass().getResourceAsStream("/dblib.properties");

        Properties props = new Properties();

        props.load(propStr);

        // Start MariaDB4j database
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0); // 0 => autom. detect free port
        DB db = DB.newEmbeddedDB(config.build());
        db.start();

        // Override jdbc URL, database name, and recovery
        props.setProperty("org.onap.ccsdk.sli.jdbc.database", "test");
        props.setProperty("org.onap.ccsdk.sli.jdbc.url", config.getURL("test"));
        props.setProperty("org.onap.dblib.connection.recovery", "true");
        

        dblibSvc = new DBResourceManager(props);
        dbm = new DBResourceManager(props);
        dblibSvc.writeData("CREATE TABLE DBLIB_TEST2 (name varchar(20));", null, null);
        dblibSvc.getData("SELECT * FROM DBLIB_TEST2", null, null);
        

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
