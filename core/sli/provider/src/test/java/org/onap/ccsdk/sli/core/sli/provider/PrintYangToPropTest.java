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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput.Mode;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResultsBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterKey;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResult;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResultBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResultKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ieee754.rev130819.Ieee754Data;
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

		Map<SliParameterKey, SliParameter> pMap = new HashMap<>();

		SliParameterBuilder pBuilder = new SliParameterBuilder();
        pBuilder.setParameterName("string-param");
        pBuilder.setStringValue("hi");
        pMap.put(new SliParameterKey(pBuilder.getParameterName()), pBuilder.build());
        pBuilder.setParameterName("int-param");
        pBuilder.setIntValue(1);
        pBuilder.setStringValue(null);
        pMap.put(new SliParameterKey(pBuilder.getParameterName()), pBuilder.build());
        pBuilder.setParameterName("bool-param");
        pBuilder.setIntValue(null);
        pBuilder.setBooleanValue(true);
        pMap.put(new SliParameterKey(pBuilder.getParameterName()), pBuilder.build());
        pBuilder.setParameterName("ipaddress-value1");
        pBuilder.setBooleanValue(null);
        pBuilder.setIpaddressValue(IetfInetUtil.ipAddressFor("127.0.0.1"));
        pMap.put(new SliParameterKey(pBuilder.getParameterName()), pBuilder.build());
        pBuilder.setParameterName("ipaddress-value2");
        pBuilder.setIpaddressValue(IetfInetUtil.ipAddressFor("::1"));
        pMap.put(new SliParameterKey(pBuilder.getParameterName()), pBuilder.build());
        pBuilder.setParameterName("ipprefix-value1");
        pBuilder.setIpaddressValue(null);
        pBuilder.setIpprefixValue(IetfInetUtil.ipPrefixFor("192.168.0.0/16"));
        pMap.put(new SliParameterKey(pBuilder.getParameterName()), pBuilder.build());
        pBuilder.setParameterName("ipprefix-value2");
        pBuilder.setIpprefixValue(IetfInetUtil.ipPrefixFor("2001:db8:3c4d::/48"));
        pMap.put(new SliParameterKey(pBuilder.getParameterName()), pBuilder.build());



		egBuilder.setSliParameter(pMap);


		// Generate properties
		props = MdsalHelper.toProperties(props, egBuilder);

		Enumeration propNames = props.propertyNames();

		while (propNames.hasMoreElements()) {
			String propName = (String) propNames.nextElement();
			LOG.info("Property {} = {}", propName, props.getProperty(propName));
		}

		// Generate builder from properties just generated
		MdsalHelper.toBuilder(props, pBuilder);


	}

    @Test
    public void testWithList() {
        TestResultsBuilder resultsBuilder = new TestResultsBuilder();
        TestResultBuilder resultBuilder = new TestResultBuilder();

        // Set builder with values
        Map<TestResultKey, TestResult> resultList = new HashMap<>();
        resultBuilder.setTestIdentifier("test1");
        Set<String> results = new LinkedHashSet <String>();
        results.add("pass");
        resultBuilder.setResults(results);
        resultList.put(new TestResultKey(resultBuilder.getTestIdentifier()), resultBuilder.build());
        resultsBuilder.setTestResult(resultList);

        // Generate properties
        Properties props = new Properties();
        props = MdsalHelper.toProperties(props, resultsBuilder);

        Enumeration propNames = props.propertyNames();

        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            LOG.info("Property {} = {}", propName, props.getProperty(propName));
        }

        // Generate builder from properties just generated
        MdsalHelper.toBuilder(props, resultsBuilder);

    }

}
