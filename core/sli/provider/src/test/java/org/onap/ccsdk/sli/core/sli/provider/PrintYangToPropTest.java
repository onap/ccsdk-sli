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
package org.onap.ccsdk.sli.core.sli.provider;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.provider.PrintYangToProp;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput.Mode;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResultsBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResult;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResultBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefixBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dt5972
 *
 */
public class PrintYangToPropTest {

	private static final Logger LOG = LoggerFactory.getLogger(PrintYangToPropTest.class);
	@Test
	public void test() {

		Properties props = new Properties();

		// Set up a builder with data
		ExecuteGraphInputBuilder egBuilder = new ExecuteGraphInputBuilder();
		egBuilder.setMode(Mode.Sync);
		egBuilder.setModuleName("my-module");
		egBuilder.setRpcName("my-rpc");

		List<SliParameter> pList = new LinkedList<>();

		SliParameterBuilder pBuilder = new SliParameterBuilder();
		pBuilder.setParameterName("string-param");
		pBuilder.setStringValue("hi");
		pList.add(pBuilder.build());
		pBuilder.setParameterName("int-param");
		pBuilder.setIntValue(1);
		pBuilder.setStringValue(null);
		pList.add(pBuilder.build());
		pBuilder.setParameterName("bool-param");
		pBuilder.setIntValue(null);
		pBuilder.setBooleanValue(true);
		pList.add(pBuilder.build());
		pBuilder.setParameterName("ipaddress-value1");
		pBuilder.setBooleanValue(null);
		pBuilder.setIpaddressValue(IpAddressBuilder.getDefaultInstance("127.0.0.1"));
		pList.add(pBuilder.build());
		pBuilder.setParameterName("ipaddress-value1");
		pBuilder.setIpaddressValue(IpAddressBuilder.getDefaultInstance("::1"));
		pList.add(pBuilder.build());
		pBuilder.setParameterName("ipprefix-value1");
		pBuilder.setIpaddressValue(null);
		pBuilder.setIpprefixValue(IpPrefixBuilder.getDefaultInstance("192.168.0.0/16"));
		pList.add(pBuilder.build());
		pBuilder.setParameterName("ipprefix-value2");
		pBuilder.setIpprefixValue(IpPrefixBuilder.getDefaultInstance("2001:db8:3c4d::/48"));
		pList.add(pBuilder.build());



		egBuilder.setSliParameter(pList);


		// Generate properties
		props = PrintYangToProp.toProperties(props, egBuilder);

		Enumeration propNames = props.propertyNames();

		while (propNames.hasMoreElements()) {
			String propName = (String) propNames.nextElement();
			LOG.info("Property {} = {}", propName, props.getProperty(propName));
		}

		// Generate builder from properties just generated
		PrintYangToProp.toBuilder(props, pBuilder);
		

	}
	
    @Test
    public void testWithList() {
        TestResultsBuilder resultsBuilder = new TestResultsBuilder();
        TestResultBuilder resultBuilder = new TestResultBuilder();

        // Set builder with values
        List<TestResult> resultList = new LinkedList<>();
        resultBuilder.setTestIdentifier("test1");
        List<String> results = new LinkedList<>();
        results.add("pass");
        resultBuilder.setResults(results);
        resultList.add(resultBuilder.build());
        resultsBuilder.setTestResult(resultList);

        // Generate properties
        Properties props = new Properties();
        props = PrintYangToProp.toProperties(props, resultsBuilder);

        Enumeration propNames = props.propertyNames();

        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            LOG.info("Property {} = {}", propName, props.getProperty(propName));
        }

        // Generate builder from properties just generated
        PrintYangToProp.toBuilder(props, resultsBuilder);

    }

}
