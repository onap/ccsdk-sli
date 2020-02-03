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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput.Mode;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.testmodel.rev190723.Builtin.SampleBits;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.testmodel.rev190723.Builtin.SampleEnumeration;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.testmodel.rev190723.Builtin.SampleUnion;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.testmodel.rev190723.Percentage;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.testmodel.rev190723.SampleContainer;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.testmodel.rev190723.SampleContainerBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.testmodel.rev190723.sample.container.LoginBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.testmodel.rev190723.sample.container.login.CustomerAddresses;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.testmodel.rev190723.sample.container.login.CustomerAddressesBuilder;
import org.opendaylight.yang.gen.v1.test.TestObjectBuilder;
import org.opendaylight.yang.gen.v1.test.WrapperObj;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.DomainName;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.HostBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressNoZoneBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
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
    assertEquals("VENDOR6500MODEL", MdsalHelper.toJavaEnum("VENDOR_6500_MODEL"));
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

    assertEquals(ipAddress, String.valueOf(b.getSingleIp().stringValue()));

    ipAddress = "cafe::8888";
    props.setProperty("test-object.single-ip", ipAddress);
    b = new TestObjectBuilder();
    MdsalHelper.toBuilder(props, b);
    assertEquals(ipAddress, String.valueOf(b.getSingleIp().stringValue()));
  }

  public void testIpAddressListToProperties() throws Exception {
    Properties props = new Properties();
    String ipAddress = "11.11.11.11";
    TestObjectBuilder b = new TestObjectBuilder();
    List<IpAddress> ipAddressList = new ArrayList<IpAddress>();
    ipAddressList.add(IpAddressBuilder.getDefaultInstance(ipAddress));
    b.setFloatingIp(ipAddressList);
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
    assertEquals(ipaddress, String.valueOf(b.getFloatingIp().get(0).stringValue()));

    props = new Properties();
    ipaddress = "cafe::8888";
    props.setProperty("test-object.floating-ip_length", "1");
    props.setProperty("test-object.floating-ip[0]", ipaddress);
    b = new TestObjectBuilder();
    MdsalHelper.toBuilder(props, b);
    assertEquals(ipaddress, String.valueOf(b.getFloatingIp().get(0).stringValue()));
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
    assertEquals(v4address, b.getSingleIpV4().getValue());
  }

  public void testIpv4AddressListToProperties() throws Exception {
    Properties props = new Properties();
    String v4address = "11.11.11.11";

    TestObjectBuilder b = new TestObjectBuilder();
    List<Ipv4Address> v4list = new ArrayList<Ipv4Address>();
    v4list.add(IpAddressBuilder.getDefaultInstance(v4address).getIpv4Address());
    b.setFloatingIpV4(v4list);
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
    assertEquals(v4address, b.getFloatingIpV4().get(0).getValue());
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
    assertEquals(v6address, b.getSingleIpV6().getValue());
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
    assertEquals(v6address, b.getFloatingIpV6().get(0).getValue());
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
    Inet6Address address =
        IetfInetUtil.INSTANCE.inet6AddressFor(IpAddressBuilder.getDefaultInstance("cafe::8888").getIpv6Address());
    MdsalHelper.toProperties(props, address);
    assertEquals("/cafe:0:0:0:0:0:0:8888", props.getProperty(""));
  }

  public void testAsNumber() throws Exception {
    String value = "1";
    Properties props = new Properties();
    AsNumber num = AsNumber.getDefaultInstance(value);
    MdsalHelper.toProperties(props, num);
    assertEquals(value, props.getProperty("as-number"));
    assertEquals(value, props.getProperty("as-number.value"));

  }

  public void testGetFullPropertiesPath() {
    String propertiesName = "l3ucpe.properties";
    String path = MdsalHelper.getFullPropertiesPath(propertiesName);
    // verify the default works
    assertEquals("/opt/lsc/controller/configuration/l3ucpe.properties", path);
    System.setProperty("karaf.home", "/opt/opendaylight/current");
    path = MdsalHelper.getFullPropertiesPath(propertiesName);
    // verify the system property is read
    assertEquals("/opt/opendaylight/current/configuration/l3ucpe.properties", path);
  }

  public void testToPropertiesWithBinary() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Properties props = new Properties();
    byte arr[] = new byte[] {1, 6, 3};
    sampleBuilder.setSampleBinary(arr);
    MdsalHelper.toProperties(props, sampleBuilder);
    assertNotNull(props.get("sample-container.sample-binary"));
  }

  public void testToPropertiesWithBits() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Boolean fanRunning = true;
    Boolean hdLed = false;
    Boolean powerLed = false;
    SampleBits sampleBits = new SampleBits(fanRunning, hdLed, powerLed);
    sampleBuilder.setSampleBits(sampleBits);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(fanRunning.toString(), props.get("sample-container.sample-bits.fan-running"));
    assertEquals(hdLed.toString(), props.get("sample-container.sample-bits.hd-led"));
    assertEquals(powerLed.toString(), props.get("sample-container.sample-bits.power-led"));
  }

  public void testToPropertiesWithBoolean() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Boolean myBoolean = true;
    sampleBuilder.setSampleBoolean(myBoolean);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myBoolean.toString(), props.get("sample-container.sample-boolean"));
  }

  public void testToPropertiesWithDecimal64() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    BigDecimal myBigDecimal = new BigDecimal(".0000000000000000000000000000001");
    sampleBuilder.setSampleDecimal64(myBigDecimal);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    // note toString() value is 1E-31
    assertEquals(myBigDecimal.toString(), props.get("sample-container.sample-decimal64"));
  }

  public void testToPropertiesWithEmpty() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Boolean isEmpty = true;
    sampleBuilder.setSampleEmpty(isEmpty);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(isEmpty.toString(), props.get("sample-container.sample-empty"));
  }

  public void testToPropertiesWithEnumeration() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Properties props = new Properties();
    SampleEnumeration currentEnum = SampleEnumeration.ShelfSlotPort;
    sampleBuilder.setSampleEnumeration(currentEnum);
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals("shelf.slot.port", props.get("sample-container.sample-enumeration"));

    currentEnum = SampleEnumeration.NotAvailable;
    sampleBuilder.setSampleEnumeration(currentEnum);
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals("not available", props.get("sample-container.sample-enumeration"));

    currentEnum = SampleEnumeration.CURRENTLYAVAILABLE;
    sampleBuilder.setSampleEnumeration(currentEnum);
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals("CURRENTLY_AVAILABLE", props.get("sample-container.sample-enumeration"));

    currentEnum = SampleEnumeration._200OK;
    sampleBuilder.setSampleEnumeration(currentEnum);
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals("200OK", props.get("sample-container.sample-enumeration"));

    currentEnum = SampleEnumeration.HyphenSeparatedValue;
    sampleBuilder.setSampleEnumeration(currentEnum);
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals("hyphen-separated-value", props.get("sample-container.sample-enumeration"));
  }

  // TODO test sampleBuilder.setSampleIdentityref(value);

  public void testToPropertiesWithInt8() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Byte myByte = new Byte("-128");
    sampleBuilder.setSampleInt8(myByte);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myByte.toString(), props.get("sample-container.sample-int8"));
  }

  public void testToPropertiesWithInt16() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Short myShort = new Short("-32768");
    sampleBuilder.setSampleInt16(myShort);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myShort.toString(), props.get("sample-container.sample-int16"));
  }

  public void testToPropertiesWithInt32() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Integer myInt = new Integer("-32768");
    sampleBuilder.setSampleInt32(myInt);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myInt.toString(), props.get("sample-container.sample-int32"));
  }

  public void testToPropertiesWithInt64() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Long myLong = new Long("-9223372036854775808");
    sampleBuilder.setSampleInt64(myLong);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myLong.toString(), props.get("sample-container.sample-int64"));
  }

  public void testToPropertiesWithLeafRef() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Boolean myBool = false;
    sampleBuilder.setSampleLeafref(myBool);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myBool.toString(), props.get("sample-container.sample-leafref"));
  }

  public void testToPropertiesWithString() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    String myString = "Hello World!";
    sampleBuilder.setSampleString(myString);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myString.toString(), props.get("sample-container.sample-string"));
  }

  public void testToPropertiesWithuInt8() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Short myShort = new Short("255");
    sampleBuilder.setSampleUint8(myShort);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myShort.toString(), props.get("sample-container.sample-uint8"));
  }

  public void testToPropertiesWithuInt16() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Integer myInt = new Integer("65535");
    sampleBuilder.setSampleUint16(myInt);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myInt.toString(), props.get("sample-container.sample-uint16"));
  }

  public void testToPropertiesWithuInt32() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Long myLong = new Long("4294967295");
    sampleBuilder.setSampleUint32(myLong);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myLong.toString(), props.get("sample-container.sample-uint32"));
  }

  public void testToPropertiesWithuInt64() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    BigInteger myBigInt = new BigInteger("2432902008176640000");
    sampleBuilder.setSampleUint64(myBigInt);
    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myBigInt.toString(), props.get("sample-container.sample-uint64"));
  }

  public void testToPropertiesFromBuilderUnion() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Properties props = new Properties();

    Integer myInt = new Integer("1");
    SampleUnion test = new SampleUnion(myInt);
    sampleBuilder.setSampleUnion(test);
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(test.getInt32().toString(), props.get("sample-container.sample-union.int32"));

    test = new SampleUnion(SampleUnion.Enumeration.Unbounded);
    sampleBuilder.setSampleUnion(test);
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals("unbounded", props.get("sample-container.sample-union.enumeration"));
  }

  public void testToPropertiesWithCustomType() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    Properties props = new Properties();

    Short myShort = new Short("99");
    Percentage myPercent = new Percentage(myShort);
    sampleBuilder.setPercentCompleted(myPercent);
    MdsalHelper.toProperties(props, sampleBuilder);
    assertEquals(myShort.toString(), props.get("sample-container.percent-completed"));
    assertEquals(myShort.toString(), props.get("sample-container.percent-completed.value"));
  }

  public void testToPropertiesWithLeaftList() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    List<String> nickNames = new ArrayList<String>();
    sampleBuilder.setCustomerNicknames(nickNames);
    String nameOne = "coffee";
    String nameTwo = "java";
    String nameThree = "mud";

    nickNames.add(nameOne);
    nickNames.add(nameTwo);
    nickNames.add(nameThree);

    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);

    assertEquals(nameOne, props.get("sample-container.customer-nicknames[0]"));
    assertEquals(nameTwo, props.get("sample-container.customer-nicknames[1]"));
    assertEquals(nameThree, props.get("sample-container.customer-nicknames[2]"));
    assertEquals(String.valueOf(nickNames.size()), props.get("sample-container.customer-nicknames_length"));
  }

  public void testToPropertiesWithComplexContainer() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    LoginBuilder lb = new LoginBuilder();
    lb.setMessage("WELCOME!");
    List<CustomerAddresses> addresses = new ArrayList<CustomerAddresses>();
    CustomerAddressesBuilder cab = new CustomerAddressesBuilder();
    cab.setAddressName("home");
    cab.setState("NJ");
    cab.setStreetAddress("yellowbrick road");

    CustomerAddresses addressOne = cab.build();
    addresses.add(addressOne);

    cab.setAddressName("vacation house");
    cab.setState("FL");
    cab.setStreetAddress("ocean ave");

    CustomerAddresses addressTwo = cab.build();
    addresses.add(addressTwo);

    lb.setCustomerAddresses(addresses);
    sampleBuilder.setLogin(lb.build());

    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);

    assertEquals("WELCOME!", props.get("sample-container.login.message"));
    assertEquals("NJ", props.get("sample-container.login.customer-addresses[0].state"));
    assertEquals("home", props.get("sample-container.login.customer-addresses[0].address-name"));
    assertEquals("yellowbrick road", props.get("sample-container.login.customer-addresses[0].street-address"));
    assertEquals("FL", props.get("sample-container.login.customer-addresses[1].state"));
    assertEquals("vacation house", props.get("sample-container.login.customer-addresses[1].address-name"));
    assertEquals("ocean ave", props.get("sample-container.login.customer-addresses[1].street-address"));
    assertEquals("2", props.get("sample-container.login.customer-addresses_length"));
  }

  public void testToPropertiesIetf() throws Exception {
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();
    sampleBuilder.setIpVersion(IpVersion.Ipv4);
    sampleBuilder.setDscp(Dscp.getDefaultInstance("1"));
    sampleBuilder.setPortNumber(PortNumber.getDefaultInstance("2"));
    sampleBuilder.setIpv6FlowLabel(Ipv6FlowLabel.getDefaultInstance("3"));
    sampleBuilder.setAsNumber(AsNumber.getDefaultInstance("4"));
    sampleBuilder.setIpv6Address(Ipv6Address.getDefaultInstance("fdda:5cc1:23:4::1f"));
    sampleBuilder.setIpAddressNoZone(IpAddressNoZoneBuilder.getDefaultInstance("fdda:5cc1:23:4::1f"));
    sampleBuilder.setIpv4Address(Ipv4Address.getDefaultInstance("192.168.1.2"));
    sampleBuilder.setIpv4AddressNoZone(Ipv4AddressNoZone.getDefaultInstance("192.168.1.3"));
    sampleBuilder.setIpv6AddressNoZone(Ipv6AddressNoZone.getDefaultInstance("fdda:5cc1:23:4::1f"));
    sampleBuilder.setIpv4Prefix(Ipv4Prefix.getDefaultInstance("198.51.100.0/24"));
    sampleBuilder.setIpv6Prefix(Ipv6Prefix.getDefaultInstance("2001:db8:aaaa:1111::100/64"));
    sampleBuilder.setDomainName(DomainName.getDefaultInstance("onap.org"));
    sampleBuilder.setHost(HostBuilder.getDefaultInstance("machine.onap.org"));
    sampleBuilder.setUri(Uri.getDefaultInstance("http://wiki.onap.org:8080"));

    Properties props = new Properties();
    MdsalHelper.toProperties(props, sampleBuilder);

    assertEquals("4", props.get("sample-container.as-number"));
    assertEquals("4", props.get("sample-container.as-number.value"));
    assertEquals("onap.org", props.get("sample-container.domain-name"));
    assertEquals("onap.org", props.get("sample-container.domain-name.value"));
    assertEquals("1", props.get("sample-container.dscp"));
    assertEquals("machine.onap.org", props.get("sample-container.host.domain-name"));
    assertEquals("machine.onap.org", props.get("sample-container.host.domain-name.value"));
    assertEquals("fdda:5cc1:23:4::1f", props.get("sample-container.ip-address-no-zone.ipv6-address-no-zone"));
    assertEquals("fdda:5cc1:23:4::1f", props.get("sample-container.ip-address-no-zone.ipv6-address-no-zone.value"));
    assertEquals("ipv4", props.get("sample-container.ip-version"));
    assertEquals("192.168.1.2", props.get("sample-container.ipv4-address"));
    assertEquals("192.168.1.3", props.get("sample-container.ipv4-address-no-zone"));
    assertEquals("192.168.1.3", props.get("sample-container.ipv4-address-no-zone.value"));
    assertEquals("198.51.100.0/24", props.get("sample-container.ipv4-prefix"));
    assertEquals("198.51.100.0/24", props.get("sample-container.ipv4-prefix.value"));
    assertEquals("fdda:5cc1:23:4::1f", props.get("sample-container.ipv6-address"));
    assertEquals("fdda:5cc1:23:4::1f", props.get("sample-container.ipv6-address-no-zone"));
    assertEquals("fdda:5cc1:23:4::1f", props.get("sample-container.ipv6-address-no-zone.value"));
    assertEquals("3", props.get("sample-container.ipv6-flow-label"));
    assertEquals("3", props.get("sample-container.ipv6-flow-label.value"));
    assertEquals("2001:db8:aaaa:1111::100/64", props.get("sample-container.ipv6-prefix"));
    assertEquals("2001:db8:aaaa:1111::100/64", props.get("sample-container.ipv6-prefix.value"));
    assertEquals("2", props.get("sample-container.port-number"));
    assertEquals("http://wiki.onap.org:8080", props.get("sample-container.uri"));
    assertEquals("http://wiki.onap.org:8080", props.get("sample-container.uri.value"));
  }

  public void testIetfToBuilder() throws Exception {
    Properties props = new Properties();
    props.put("sample-container.as-number", "4");
    props.put("sample-container.as-number.value", "4");
    props.put("sample-container.domain-name", "onap.org");
    props.put("sample-container.domain-name.value", "onap.org");
    props.put("sample-container.dscp", "1");
    props.put("sample-container.host.domain-name", "machine.onap.org");
    props.put("sample-container.host.domain-name.value", "machine.onap.org");
    props.put("sample-container.ip-address-no-zone.ipv6-address-no-zone", "fdda:5cc1:23:4::1f");
    props.put("sample-container.ip-address-no-zone.ipv6-address-no-zone.value", "fdda:5cc1:23:4::1f");
    props.put("sample-container.ip-version", "ipv4");
    props.put("sample-container.ipv4-address", "192.168.1.2");
    props.put("sample-container.ipv4-address-no-zone", "192.168.1.3");
    props.put("sample-container.ipv4-address-no-zone.value", "192.168.1.3");
    props.put("sample-container.ipv4-prefix", "198.51.100.0/24");
    props.put("sample-container.ipv4-prefix.value", "198.51.100.0/24");
    props.put("sample-container.ipv6-address", "fdda:5cc1:23:4::1f");
    props.put("sample-container.ipv6-address-no-zone", "fdda:5cc1:23:4::1f");
    props.put("sample-container.ipv6-address-no-zone.value", "fdda:5cc1:23:4::1f");
    props.put("sample-container.ipv6-flow-label", "3");
    props.put("sample-container.ipv6-flow-label.value", "3");
    props.put("sample-container.ipv6-prefix", "2001:db8:aaaa:1111::100/64");
    props.put("sample-container.ipv6-prefix.value", "2001:db8:aaaa:1111::100/64");
    props.put("sample-container.port-number", "2");
    props.put("sample-container.uri", "http://wiki.onap.org:8080");
    props.put("sample-container.uri.value", "http://wiki.onap.org:8080");
    SampleContainerBuilder sampleBuilder = new SampleContainerBuilder();

    MdsalHelper.toBuilder(props, sampleBuilder);
    SampleContainer result = sampleBuilder.build();
    assertEquals(AsNumber.getDefaultInstance("4"), result.getAsNumber());
    assertEquals(DomainName.getDefaultInstance("onap.org"), result.getDomainName());
    assertEquals(Dscp.getDefaultInstance("1"), result.getDscp());
    // assertEquals(HostBuilder.getDefaultInstance("machine.onap.org").getDomainName(),
    // result.getHost().getDomainName());
    assertEquals(Ipv6AddressNoZone.getDefaultInstance("fdda:5cc1:23:4::1f").getValue(),
        result.getIpv6AddressNoZone().getValue());
    assertEquals(IpVersion.Ipv4, result.getIpVersion());
    assertEquals(Ipv4Address.getDefaultInstance("192.168.1.2"), result.getIpv4Address());
    assertEquals(Ipv4AddressNoZone.getDefaultInstance("192.168.1.3"), result.getIpv4AddressNoZone());
    assertEquals(Ipv4Prefix.getDefaultInstance("198.51.100.0/24"), result.getIpv4Prefix());
    assertEquals(Ipv6Address.getDefaultInstance("fdda:5cc1:23:4::1f"), result.getIpv6Address());
    assertEquals(IpAddressNoZoneBuilder.getDefaultInstance("fdda:5cc1:23:4::1f").getIpv6AddressNoZone().getValue(),
        result.getIpv6AddressNoZone().getValue());
    assertEquals(Ipv6FlowLabel.getDefaultInstance("3"), result.getIpv6FlowLabel());
    assertEquals(Ipv6Prefix.getDefaultInstance("2001:db8:aaaa:1111::100/64"), result.getIpv6Prefix());
    assertEquals(PortNumber.getDefaultInstance("2"), result.getPortNumber());
    assertEquals(Uri.getDefaultInstance("http://wiki.onap.org:8080"), result.getUri());
  }

}
