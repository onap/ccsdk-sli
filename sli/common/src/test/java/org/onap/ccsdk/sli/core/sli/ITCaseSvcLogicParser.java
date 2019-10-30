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

/**
 *
 */
package org.onap.ccsdk.sli.core.sli;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dt5972
 *
 */
public class ITCaseSvcLogicParser {

	private static SvcLogicStore store;
	private static final Logger LOG = LoggerFactory.getLogger(SvcLogicJdbcStore.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		LOG.info("before class");

		URL propUrl = ITCaseSvcLogicParser.class.getResource("/svclogic.properties");

		InputStream propStr = ITCaseSvcLogicParser.class.getResourceAsStream("/svclogic.properties");

		Properties props = new Properties();

		props.load(propStr);

        store = SvcLogicStoreFactory.getSvcLogicStore(props);

        assertNotNull(store);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        LOG.info("after class");
    }

    @Before
    public void setUp() throws Exception {
        LOG.info("before");
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("after");
    }

    /**
     * Test method for {@link org.onap.ccsdk.sli.core.sli.SvcLogicParser#parse(java.lang.String)}.
     */
    @Test
    public void testParseValidXml() {

        try {
            InputStream testStr = getClass().getResourceAsStream("/parser-good.tests");
            BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
            String testCaseFile = null;
            while ((testCaseFile = testsReader.readLine()) != null) {

                testCaseFile = testCaseFile.trim();

                if (testCaseFile.length() > 0) {
                    if (!testCaseFile.startsWith("/")) {
                        testCaseFile = "/" + testCaseFile;
                    }
                    URL testCaseUrl = getClass().getResource(testCaseFile);
                    if (testCaseUrl == null) {
                        fail("Could not resolve test case file " + testCaseFile);
                    }
                    
                    // Test parsing and printing
                    try {
                        SvcLogicParser parser = new SvcLogicParser();
                        
                        for (SvcLogicGraph graph : parser.parse(testCaseUrl.getPath()))  {
                            System.out.println("XML for graph "+graph.getModule()+":"+graph.getRpc());
                            graph.printAsXml(System.out);
                            System.out.println("GV for graph "+graph.getModule()+":"+graph.getRpc());
                            graph.printAsGv(System.out); 
                        }
                    }  catch (Exception e) {

                        fail("Validation failure [" + e.getMessage() + "]");
                    }

                    try {
                        SvcLogicParser.load(testCaseUrl.getPath(), store);
                    } catch (Exception e) {

                        fail("Validation failure [" + e.getMessage() + "]");
                    }
                }
            }
        } catch (SvcLogicParserException e) {
            fail("Parser error : " + e.getMessage());
        } catch (Exception e) {
            LOG.error("", e);
            fail("Caught exception processing test cases");
        }
    }

    @Test
    public void testDblibLoadValidXml() throws IOException, SQLException, ConfigurationException {

        URL propUrl = ITCaseSvcLogicParser.class.getResource("/dblib.properties");

        InputStream propStr = ITCaseSvcLogicParser.class.getResourceAsStream("/dblib.properties");

        Properties props = new Properties();

        props.load(propStr);

        SvcLogicDblibStore dblibStore = new SvcLogicDblibStore(new DBResourceManager(props));

        Connection dbConn = dblibStore.getConnection();

        String dbName = props.getProperty("org.onap.ccsdk.sli.jdbc.database", "sdnctl");

        DatabaseMetaData dbm;

        try {
            dbm = dbConn.getMetaData();
        } catch (SQLException e) {

            throw new ConfigurationException("could not get databse metadata", e);
        }

        // See if table SVC_LOGIC exists. If not, create it.
        Statement stmt = null;
        try {

            ResultSet tables = dbm.getTables(null, null, "SVC_LOGIC", null);
            if (tables.next()) {
                LOG.debug("SVC_LOGIC table already exists");
            } else {
                String crTableCmd = "CREATE TABLE " + dbName + ".SVC_LOGIC (" + "module varchar(80) NOT NULL,"
                        + "rpc varchar(80) NOT NULL," + "version varchar(40) NOT NULL," + "mode varchar(5) NOT NULL,"
                        + "active varchar(1) NOT NULL,graph BLOB,"
                        + "modified_timestamp timestamp DEFAULT NULL,"
                        + "md5sum varchar(128) DEFAULT NULL,"
                        + "CONSTRAINT P_SVC_LOGIC PRIMARY KEY(module, rpc, version, mode))";

                stmt = dbConn.createStatement();
                stmt.executeUpdate(crTableCmd);
            }
        } catch (Exception e) {
            throw new ConfigurationException("could not create SVC_LOGIC table", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.error("Statement close error ", e);
                }
            }
        }

        // See if NODE_TYPES table exists and, if not, create it
        stmt = null;
        try {

            ResultSet tables = dbm.getTables(null, null, "NODE_TYPES", null);
            if (tables.next()) {
                LOG.debug("NODE_TYPES table already exists");
            } else {
                String crTableCmd = "CREATE TABLE " + dbName + ".NODE_TYPES (" + "nodetype varchar(80) NOT NULL,"
                        + "CONSTRAINT P_NODE_TYPES PRIMARY KEY(nodetype))";

                stmt = dbConn.createStatement();

                stmt.executeUpdate(crTableCmd);
            }
        } catch (Exception e) {
            throw new ConfigurationException("could not create SVC_LOGIC table", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.error("Statement close error ", e);
                }
            }
        }

        try {
            InputStream testStr = getClass().getResourceAsStream("/parser-good.tests");
            BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
            String testCaseFile = null;
            while ((testCaseFile = testsReader.readLine()) != null) {

                testCaseFile = testCaseFile.trim();

                if (testCaseFile.length() > 0) {
                    if (!testCaseFile.startsWith("/")) {
                        testCaseFile = "/" + testCaseFile;
                    }
                    URL testCaseUrl = getClass().getResource(testCaseFile);
                    if (testCaseUrl == null) {
                        fail("Could not resolve test case file " + testCaseFile);
                    }

                    try {
                        SvcLogicParser.load(testCaseUrl.getPath(), dblibStore);
                    } catch (Exception e) {

                        fail("Validation failure [" + e.getMessage() + "]");
                    }
                }
            }
        } catch (SvcLogicParserException e) {
            fail("Parser error : " + e.getMessage());
        } catch (Exception e) {
            LOG.error("", e);
            fail("Caught exception processing test cases");
        }
    }

    @Test(expected = SvcLogicException.class)
    public void testParseInvalidXml() throws SvcLogicException, IOException {

        InputStream testStr = getClass().getResourceAsStream("/parser-bad.tests");
        BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
        String testCaseFile;
        while ((testCaseFile = testsReader.readLine()) != null) {

            testCaseFile = testCaseFile.trim();

            if (testCaseFile.length() > 0) {
                if (!testCaseFile.startsWith("/")) {
                    testCaseFile = "/" + testCaseFile;
                }
                URL testCaseUrl = getClass().getResource(testCaseFile);
                if (testCaseUrl == null) {
                    fail("Could not resolve test case file " + testCaseFile);
                }
                SvcLogicParser.validate(testCaseUrl.getPath(), store);
            }
        }
    }
}
