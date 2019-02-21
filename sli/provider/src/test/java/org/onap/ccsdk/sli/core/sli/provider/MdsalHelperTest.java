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

package org.onap.ccsdk.sli.core.sli.provider;

import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput.Mode;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder;
import org.opendaylight.yang.gen.v1.test.TestObjectBuilder;
import org.opendaylight.yang.gen.v1.test.WrapperObj;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class MdsalHelperTest extends TestCase {
	private static final Logger LOG = LoggerFactory.getLogger(MdsalHelperTest.class);
	public static final String pathToSdnPropertiesFile = "src/test/resources/l3sdn.properties";

	public void testSdnProperties() {

		MdsalHelperTesterUtil.loadProperties(pathToSdnPropertiesFile);
		assertEquals("synccomplete", MdsalHelperTesterUtil.mapEnumeratedValue("request-status", "synccomplete"));
		assertEquals("asynccomplete", MdsalHelperTesterUtil.mapEnumeratedValue("request-status", "asynccomplete"));
		assertEquals("notifycomplete", MdsalHelperTesterUtil.mapEnumeratedValue("request-status", "notifycomplete"));
		assertEquals("service-configuration-operation",
				MdsalHelperTesterUtil.mapEnumeratedValue("rpc-name", "service-configuration-operation"));
	}

	public void testNegativeSdnProperties() {
		assertNotSame("synccomplete", MdsalHelperTesterUtil.mapEnumeratedValue("request-status", "Synccomplete"));
	}

	public void testToProperties() {

		ExecuteGraphInputBuilder execBuilder = new ExecuteGraphInputBuilder();
		SliParameterBuilder parmBuilder = new SliParameterBuilder();
		List<SliParameter> params = new LinkedList<SliParameter>();

		parmBuilder.setParameterName("boolean-parm");
		parmBuilder.setBooleanValue(Boolean.TRUE);
		params.add(parmBuilder.build());

		parmBuilder.setParameterName("int-parm");
		parmBuilder.setBooleanValue(null);
		parmBuilder.setIntValue(1);
		params.add(parmBuilder.build());

		parmBuilder.setParameterName("str-parm");
		parmBuilder.setIntValue(null);
		parmBuilder.setStringValue("hello");
		params.add(parmBuilder.build());

		parmBuilder.setParameterName("ipaddress4-parm");
		parmBuilder.setStringValue(null);
		parmBuilder.setIpaddressValue(IpAddressBuilder.getDefaultInstance("127.0.0.1"));
		params.add(parmBuilder.build());

		parmBuilder.setParameterName("ipaddress6-parm");
		parmBuilder.setIpaddressValue(IpAddressBuilder.getDefaultInstance("ef::1"));
		params.add(parmBuilder.build());
		
		parmBuilder.setParameterName("ipprefix-parm");
		parmBuilder.setIpaddressValue(null);
		parmBuilder.setIpprefixValue(IpPrefixBuilder.getDefaultInstance("10.0.0.0/24"));
		params.add(parmBuilder.build());
		
		parmBuilder.setParameterName("portnumber-parm");
		parmBuilder.setIpprefixValue(null);
		parmBuilder.setPortNumber(PortNumber.getDefaultInstance("8080"));
		params.add(parmBuilder.build());
		
		parmBuilder.setParameterName("dcsp-parm");
		parmBuilder.setPortNumber(null);
		parmBuilder.setDscp(Dscp.getDefaultInstance("57"));
		params.add(parmBuilder.build());

		execBuilder.setMode(Mode.Sync);
		execBuilder.setModuleName("my-module");
		execBuilder.setRpcName("do-it-now");
		execBuilder.setSliParameter(params);

		Properties props = new Properties();

		MdsalHelperTesterUtil.toProperties(props, execBuilder);

		LOG.info("Converted to properties");
		for (Map.Entry<Object, Object> e : props.entrySet()) {
			LOG.info(e.getKey().toString() + " = " + e.getValue().toString());

		}

	}

	public void testToBuilder() {

		Properties props = new Properties();

		props.setProperty("execute-graph-input.mode", "Sync");
		props.setProperty("execute-graph-input.module", "my-module");
		props.setProperty("execute-graph-input.rpc", "do-it-now");
		props.setProperty("execute-graph-input.sli-parameter[0].parameter-name", "bool-parm");
		props.setProperty("execute-graph-input.sli-parameter[0].boolean-value", "true");
		props.setProperty("execute-graph-input,sli-parameter[1].parameter-name", "int-param");
		props.setProperty("execute-graph-input.sli-parameter[1].int-value", "1");
		props.setProperty("execute-graph-input.sli-parameter[2].parameter-name", "str-param");
		props.setProperty("execute-graph-input.sli-parameter[2].str-value", "hello");
		props.setProperty("execute-graph-input.sli-parameter[3].parameter-name", "ipv4address-param");
		props.setProperty("execute-graph-input.sli-parameter[3].ipaddress-value", "127.0.0.1");
		props.setProperty("execute-graph-input.sli-parameter[4].parameter-name", "ipv6address-param");
		props.setProperty("execute-graph-input.sli-parameter[4].ipaddress-value", "ef::1");
		ExecuteGraphInputBuilder execBuilder = new ExecuteGraphInputBuilder();

		MdsalHelperTesterUtil.toBuilder(props, execBuilder);

	}

	public void testToJavaEnum() throws Exception {
		assertEquals("_2018HelloWorld", MdsalHelper.toJavaEnum("2018Hello World"));
		assertEquals("SomethingElse", MdsalHelper.toJavaEnum("Something.Else"));
		assertEquals("MyTestString", MdsalHelper.toJavaEnum("my-test-string"));
	}

	// During the default enumeration mapping no properties file is needed, the yang
	// value is returned
	// by the java object
	public void testDefaultEnumerationMapping() throws Exception {
		Properties props = new Properties();
		MdsalHelper.toProperties(props, new WrapperObj());
		assertEquals("4COS", props.getProperty("wrapper-obj.cos-model-type"));
	}

	// When no properties file exists the default java value will be returned if
	// legacy enumeration
	// mapping is enabled
	public void testLegacyEnumerationMappingNoProperties() throws Exception {
		Properties props = new Properties();
		MdsalHelper.toProperties(props, new WrapperObj(), true);
		assertEquals("_4COS", props.getProperty("wrapper-obj.cos-model-type"));
	}

	// When a properties file exists & legacy enumeration mapping is enabled the
	// value from the
	// properties file should be returned
	public void testLegacyEnumerationMappingWithProperties() throws Exception {
		MdsalHelper.loadProperties("src/test/resources/EnumerationMapping.properties");
		Properties props = new Properties();
		MdsalHelper.toProperties(props, new WrapperObj(), true);
		assertEquals("HelloWorld", props.getProperty("wrapper-obj.cos-model-type"));
	}

	public void testSingleIpAddressToProperties() throws Exception {
		Properties props = new Properties();
		String ipAddress = "11.11.11.11";
		MdsalHelper.toProperties(props, IpAddressBuilder.getDefaultInstance(ipAddress));
		assertEquals(ipAddress, props.getProperty(""));
		ipAddress = "cafe::8888";
		MdsalHelper.toProperties(props, IpAddressBuilder.getDefaultInstance(ipAddress));
		assertEquals(ipAddress, props.getProperty(""));
	}
	

	public void testSingleIpAddressToBuilder() throws Exception {
		Properties props = new Properties();
		String ipAddress = "11.11.11.11";
		props.setProperty("test-object.single-ip", ipAddress);
		TestObjectBuilder b = new TestObjectBuilder();
		MdsalHelper.toBuilder(props, b);

		assertEquals(ipAddress,String.valueOf(b.getSingleIp().stringValue()));
		
		ipAddress = "cafe::8888";
		props.setProperty("test-object.single-ip", ipAddress);
		b = new TestObjectBuilder();
		MdsalHelper.toBuilder(props, b);
		assertEquals(ipAddress,String.valueOf(b.getSingleIp().stringValue()));
	}
	
	public void testIpAddressListToProperties() throws Exception {
		Properties props = new Properties();
		String ipAddress = "11.11.11.11";
		TestObjectBuilder b = new TestObjectBuilder();
		List<IpAddress> ipAddressList = new ArrayList<IpAddress>();
		ipAddressList.add(IpAddressBuilder.getDefaultInstance(ipAddress));
		b.setFloatingIp(ipAddressList );
		MdsalHelper.toProperties(props, b.build());
		assertEquals(ipAddress, props.getProperty("test-object.floating-ip[0]"));
		assertEquals("1", props.getProperty("test-object.floating-ip_length"));
	}
	
	public void testIpAddressListToBuilder() throws Exception {
		Properties props = new Properties();
		String ipaddress = "11.11.11.12";
		props.setProperty("test-object.floating-ip_length", "1");
		props.setProperty("test-object.floating-ip[0]", ipaddress);
		TestObjectBuilder b = new TestObjectBuilder();
		MdsalHelper.toBuilder(props, b);
		assertEquals(ipaddress,String.valueOf(b.getFloatingIp().get(0).stringValue()));
		
		props = new Properties();
		ipaddress = "cafe::8888";
		props.setProperty("test-object.floating-ip_length", "1");
		props.setProperty("test-object.floating-ip[0]", ipaddress);
		b = new TestObjectBuilder();
		MdsalHelper.toBuilder(props, b);
		assertEquals(ipaddress,String.valueOf(b.getFloatingIp().get(0).stringValue()));
	}

	
	public void testSingleIpv4AddressToProperties() throws Exception {
		Properties props = new Properties();
		String v4address = "11.11.11.11";
		MdsalHelper.toProperties(props, IpAddressBuilder.getDefaultInstance(v4address).getIpv4Address());
		assertEquals(v4address, props.getProperty(""));
	}
	
	public void testSingleIpv4AddressToBuilder() throws Exception {
		Properties props = new Properties();
		String v4address = "11.11.11.11";
		props.setProperty("test-object.single-ip-v4", v4address);
		TestObjectBuilder b = new TestObjectBuilder();
		MdsalHelper.toBuilder(props, b);
		assertEquals(v4address,b.getSingleIpV4().getValue());
	}
	
	public void testIpv4AddressListToProperties() throws Exception {
		Properties props = new Properties();
		String v4address = "11.11.11.11";

		TestObjectBuilder b = new TestObjectBuilder();
		List<Ipv4Address> v4list = new ArrayList<Ipv4Address>();
		v4list.add(IpAddressBuilder.getDefaultInstance(v4address).getIpv4Address());
		b.setFloatingIpV4(v4list );
		MdsalHelper.toProperties(props, b.build());
		assertEquals(v4address, props.getProperty("test-object.floating-ip-v4[0]"));
		assertEquals("1", props.getProperty("test-object.floating-ip-v4_length"));
	}
	
	public void testIpv4AddressListToBuilder() throws Exception {
		Properties props = new Properties();
		String v4address = "11.11.11.12";
		props.setProperty("test-object.floating-ip-v4_length", "1");
		props.setProperty("test-object.floating-ip-v4[0]", v4address);
		TestObjectBuilder b = new TestObjectBuilder();
		MdsalHelper.toBuilder(props, b);
		assertEquals(v4address,b.getFloatingIpV4().get(0).getValue());
	}

	public void testSingleIpv6AddressToProperties() throws Exception {
		Properties props = new Properties();
		String v6address = "cafe::8888";
		MdsalHelper.toProperties(props, IpAddressBuilder.getDefaultInstance(v6address).getIpv6Address());
		MdsalHelper.toBuilder(props, IpAddressBuilder.getDefaultInstance("cafe::8887"));
		assertEquals(v6address, props.getProperty(""));
	}

	public void testSingleIpv6AddressToBuilder() throws Exception {
		Properties props = new Properties();
		String v6address = "cafe::8888";
		props.setProperty("test-object.single-ip-v6", v6address);
		TestObjectBuilder b = new TestObjectBuilder();
		MdsalHelper.toBuilder(props, b);
		assertEquals(v6address,b.getSingleIpV6().getValue());
	}
	
	public void testIpv6AddressListToProperties() throws Exception {
		Properties props = new Properties();
		String v6address = "cafe::8888";

		TestObjectBuilder b = new TestObjectBuilder();
		List<Ipv6Address> v6list = new ArrayList<Ipv6Address>();
		v6list.add(IpAddressBuilder.getDefaultInstance(v6address).getIpv6Address());
		b.setFloatingIpV6(v6list);
		MdsalHelper.toProperties(props, b.build());
		assertEquals(v6address, props.getProperty("test-object.floating-ip-v6[0]"));
		assertEquals("1", props.getProperty("test-object.floating-ip-v6_length"));
	}
	
	public void testIpv6AddressListToBuilder() throws Exception {
		Properties props = new Properties();
		String v6address = "cafe::8888";
		props.setProperty("test-object.floating-ip-v6_length", "1");
		props.setProperty("test-object.floating-ip-v6[0]", v6address);
		TestObjectBuilder b = new TestObjectBuilder();
		MdsalHelper.toBuilder(props, b);
		assertEquals(v6address,b.getFloatingIpV6().get(0).getValue());
	}
	
	public void testIpPrefix() throws Exception {
		String ipPrefix = "10.0.0.0/24";
		Properties props = new Properties();
		MdsalHelper.toProperties(props, IpPrefixBuilder.getDefaultInstance(ipPrefix));
		assertEquals(ipPrefix, props.getProperty(""));
	}

	public void testPortNumber() throws Exception {
		Properties props = new Properties();
		String portNumber = "5";
		MdsalHelper.toProperties(props, PortNumber.getDefaultInstance(portNumber));
		assertEquals(portNumber, props.getProperty(""));
	}

	public void testDscp() throws Exception {
		Properties props = new Properties();
		String dscp = "1";
		MdsalHelper.toProperties(props, Dscp.getDefaultInstance(dscp));
		assertEquals(dscp, props.getProperty(""));
	}

	public void testIetfInet() throws Exception {
		Properties props = new Properties();
		Inet6Address address = IetfInetUtil.INSTANCE
				.inet6AddressFor(IpAddressBuilder.getDefaultInstance("cafe::8888").getIpv6Address());
		MdsalHelper.toProperties(props, address);
		assertEquals("/cafe:0:0:0:0:0:0:8888", props.getProperty(""));
	}
}