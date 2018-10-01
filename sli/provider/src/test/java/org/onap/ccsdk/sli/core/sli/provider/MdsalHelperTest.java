/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInput.Mode;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterBuilder;
import org.opendaylight.yang.gen.v1.test.WrapperObj;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
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

        execBuilder.setMode(Mode.Sync);
        execBuilder.setModuleName("my-module");
        execBuilder.setRpcName("do-it-now");
        execBuilder.setSliParameter(params);

        Properties props = new Properties();

        MdsalHelperTesterUtil.toProperties(props, execBuilder);
        MdsalHelperTesterUtil.toProperties(props, "", execBuilder);

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

    // During the default enumeration mapping no properties file is needed, the
    // yang value is returned
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

    public void testGetFullPropertiesPath() {
        String fullPath = MdsalHelper.getFullPropertiesPath("testFile");
        assertEquals("/opt/bvc/controller/configuration/testFile", fullPath);

    }

    public void testIsDscp() {

        assertTrue(MdsalHelper.isDscp(Dscp.class));

    }
    
    public void testIsPortNumber() {

        assertTrue(MdsalHelper.isPortNumber(PortNumber.class));

    }
}
