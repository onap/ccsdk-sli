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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

import org.onap.ccsdk.sli.core.sli.SvcLogicParser;
import org.onap.ccsdk.sli.core.sli.SvcLogicParserException;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;

import junit.framework.TestCase;

/**
 * @author dt5972
 *
 */
public class SvcLogicParserTest extends TestCase {

	/**
	 * Test method for {@link org.onap.ccsdk.sli.core.sli.SvcLogicParser#parse(java.lang.String)}.
	 */
	
	
	public void testParse() {

		
		try
		{

			URL propUrl = getClass().getResource("/svclogic.properties");
			
			InputStream propStr = getClass().getResourceAsStream("/svclogic.properties");
			
			SvcLogicStore store = SvcLogicStoreFactory.getSvcLogicStore(propStr);
			
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
			
			testStr = getClass().getResourceAsStream("/parser-bad.tests");
			testsReader = new BufferedReader(new InputStreamReader(testStr));
			testCaseFile = null;
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
			e.printStackTrace();
			fail("Caught exception processing test cases");
		}
		
		
	}
	
	

}
