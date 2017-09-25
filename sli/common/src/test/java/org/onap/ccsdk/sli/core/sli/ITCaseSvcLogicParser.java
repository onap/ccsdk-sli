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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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


		// Start MariaDB4j database
		DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
		config.setPort(0); // 0 => autom. detect free port
		DB db = DB.newEmbeddedDB(config.build());
		db.start();


		// Override jdbc URL and database name
		props.setProperty("org.onap.ccsdk.sli.jdbc.database", "test");
		props.setProperty("org.onap.ccsdk.sli.jdbc.url", config.getURL("test"));


		store = SvcLogicStoreFactory.getSvcLogicStore(props);

		assertNotNull(store);

		store.registerNodeType("switch");
		store.registerNodeType("block");
		store.registerNodeType("get-resource");
		store.registerNodeType("reserve");
		store.registerNodeType("is-available");
		store.registerNodeType("exists");
		store.registerNodeType("configure");
		store.registerNodeType("return");
		store.registerNodeType("record");
		store.registerNodeType("allocate");
		store.registerNodeType("release");
		store.registerNodeType("for");
		store.registerNodeType("set");
		store.registerNodeType("call");
		store.registerNodeType("delete");
		store.registerNodeType("execute");
		store.registerNodeType("notify");
		store.registerNodeType("save");
		store.registerNodeType("update");
		store.registerNodeType("break");
	}

	@Before
	public void setUp() throws Exception {
		LOG.info("before");
	}

	@After
	public void tearDown() throws Exception {
		LOG.info("after");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		LOG.info("after class");
	}

	/**
	 * Test method for {@link org.onap.ccsdk.sli.core.sli.SvcLogicParser#parse(java.lang.String)}.
	 */
	@Test
	public void testParseValidXml() {

		try
		{
			InputStream testStr = getClass().getResourceAsStream("/parser-good.tests");
			BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
			String testCaseFile = null;
			while ((testCaseFile = testsReader.readLine()) != null) {

				testCaseFile = testCaseFile.trim();

				if (testCaseFile.length() > 0)
				{
					if (!testCaseFile.startsWith("/"))
					{
						testCaseFile = "/"+testCaseFile;
					}
					URL testCaseUrl = getClass().getResource(testCaseFile);
					if (testCaseUrl == null)
					{
						fail("Could not resolve test case file "+testCaseFile);
					}

					try {
						SvcLogicParser.validate(testCaseUrl.getPath(), store);
					} catch (Exception e) {
						fail("Validation failure ["+e.getMessage()+"]");
					}
				}
			}
		}
		catch (SvcLogicParserException e)
		{
			fail("Parser error : "+e.getMessage());
		}
		catch (Exception e)
		{
			LOG.error("", e);
			fail("Caught exception processing test cases");
		}
	}

	@Test
	public void testParseInvalidXml() {

		try
		{
			InputStream testStr = getClass().getResourceAsStream("/parser-bad.tests");
			BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
			String testCaseFile = null;
			while ((testCaseFile = testsReader.readLine()) != null) {

				testCaseFile = testCaseFile.trim();

				if (testCaseFile.length() > 0)
				{
					if (!testCaseFile.startsWith("/"))
					{
						testCaseFile = "/"+testCaseFile;
					}
					URL testCaseUrl = getClass().getResource(testCaseFile);
					if (testCaseUrl == null)
					{
						fail("Could not resolve test case file "+testCaseFile);
					}

					boolean valid = true;
					try {
						SvcLogicParser.load(testCaseUrl.getPath(), store);
					} catch (Exception e) {
						System.out.println(e.getMessage());
						valid = false;
					}

					if (valid) {
						fail("Expected compiler error on "+testCaseFile+", but got success");
					}
				}
			}
		}
		catch (SvcLogicParserException e)
		{
			fail("Parser error : "+e.getMessage());
		}
		catch (Exception e)
		{
			LOG.error("", e);
			fail("Caught exception processing test cases");
		}

	}

}
