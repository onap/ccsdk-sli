package jtest.org.onap.ccsdk.sli.plugins.prop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import static org.junit.Assert.assertEquals;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.prop.PropertiesNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPropertiesNode {

    private static final Logger log = LoggerFactory.getLogger(TestPropertiesNode.class);
    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();
    
    @Test
    public void testJSONFileParsing() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.json");
        p.put("contextPrefix", "test-json");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("test-json.message"),"The provisioned access " +
                "bandwidth is at or exceeds 50% of the total server capacity.");
    }

    @Test
    public void testJSONFileArrayParsing() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.json");
        p.put("contextPrefix", "test-json");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("test-json.equipment-data[0].max-server-speed"),"1600000");
        assertEquals(ctx.getAttribute("test-json.resource-state.used"),"1605000");
        assertEquals(ctx.getAttribute("test-json.resource-rule.service-model"),"DUMMY");
        assertEquals(ctx.getAttribute("test-json.resource-rule.endpoint-position"),"VCE-Cust");
    }

    @Test
    public void testJSONFileParsingPrefixCheck() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.json");
        p.put("contextPrefix", "");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("equipment-data[0].max-server-speed"),"1600000");
        assertEquals(ctx.getAttribute("resource-state.used"),"1605000");
        assertEquals(ctx.getAttribute("resource-rule.service-model"),"DUMMY");
        assertEquals(ctx.getAttribute("resource-rule.endpoint-position"),"VCE-Cust");
        assertEquals(ctx.getAttribute("resource-rule.hard-limit-expression"),"max-server-" +
                "speed * number-primary-servers");
    }

    @Test
    public void testJSONFileParsingNoPrefix() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.json");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("equipment-data[0].max-server-speed"),"1600000");
        assertEquals(ctx.getAttribute("resource-state.used"),"1605000");
        assertEquals(ctx.getAttribute("resource-rule.service-model"),"DUMMY");
        assertEquals(ctx.getAttribute("resource-rule.endpoint-position"),"VCE-Cust");
        assertEquals(ctx.getAttribute("resource-rule.hard-limit-expression"),"max-server-" +
                "speed * number-primary-servers");
    }

    @Test
    public void testJSONFileParsingCtxCheck() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.json");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("equipment-data[0].max-server-speed"),"1600000");
        assertEquals(ctx.getAttribute("resource-state.used"),"1605000");
        assertEquals(ctx.getAttribute("resource-rule.service-model"),"DUMMY");
        assertEquals(ctx.getAttribute("resource-rule.endpoint-position"),"VCE-Cust");
        assertEquals(ctx.getAttribute("resource-rule.hard-limit-expression"),"max-server-" +
                "speed * number-primary-servers");
        assertEquals(ctx.getAttribute("tmp.sdn-circuit-req-row_length"),"1");
    }

    @Test(expected = SvcLogicException.class)
    public void testToPropertiesInvalidJson() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test-invalid.json");
        p.put("contextPrefix", "invalid");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("tmp.sdn-circuit-req-row_length"),"1");
    }

    @Test
    public void testTXTFileParsing() throws SvcLogicException {

        environmentVariables.set("deployer_pass", "sdncp-123");
        assertEquals("sdncp-123", System.getenv("deployer_pass"));

        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.txt");
        p.put("contextPrefix", "test-txt");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("test-txt.service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("test-txt.service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("test-txt.operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("test-txt.service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("test-txt.service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
        assertEquals(ctx.getAttribute("test-txt.obfuscated-var"), "sdncp-123");
    }

    @Test
    public void testTXTFileParsingPrefixCheck() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.txt");
        p.put("contextPrefix", "");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
    }

    @Test
    public void testTXTFileParsingNoPrefix() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.txt");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
    }

    @Test
    public void testTXTFileParsingCtxCheck() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.txt");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
        assertEquals(ctx.getAttribute("tmp.sdn-circuit-req-row_length"),"1");
    }

    @Test(expected = SvcLogicException.class)
    public void testToPropertiesInvalidParam() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");

        Map<String, String> p = new HashMap<String, String>();
        p.put("responsePrefix", "response");
        p.put("skipSending", "true");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("tmp.sdn-circuit-req-row_length"),"1");
    }

    @Test(expected = SvcLogicException.class)
    public void testToPropertiesNoParam() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("tmp.sdn-circuit-req-row_length"),"1");
    }

    @Test(expected = SvcLogicException.class)
    public void testToPropertiesFilePathError() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/tests/resources/test.txt");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("tmp.sdn-circuit-req-row_length"),"1");
    }

    @Test
    public void testXMLFileParsing() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.xml");
        p.put("contextPrefix", "test-xml");
        p.put("listName", "project.build");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("test-xml.project.modelVersion"),"4.0.0");
    }

    @Test
    public void testXMLFileInnerParsing() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.xml");
        p.put("contextPrefix", "test-xml");
        p.put("listName", "project.modelVersion");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("test-xml.project.properties.project.build.sourceEncoding"),"UTF-8");
        assertEquals(ctx.getAttribute("test-xml.project.dependencies.dependency.scope"),"provided");
        assertEquals(ctx.getAttribute("test-xml.project.build.pluginManagement.plugins.plugin.configuration" +
                                              ".lifecycleMappingMetadata.pluginExecutions.pluginExecution." +
                                              "pluginExecutionFilter.versionRange"),"[1.2.0.100,)");
        assertEquals(ctx.getAttribute("test-xml.project.build.plugins.plugin.configuration." +
                                              "instructions.Import-Package"),"*");
    }

    @Test
    public void testXMLFileParsingPrefixCheck() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.xml");
        p.put("contextPrefix", "");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("project.properties.project.build.sourceEncoding"),"UTF-8");
        assertEquals(ctx.getAttribute("project.dependencies.dependency.scope"),"provided");
        assertEquals(ctx.getAttribute("project.build.pluginManagement.plugins.plugin.configuration" +
                                              ".lifecycleMappingMetadata.pluginExecutions.pluginExecution." +
                                              "pluginExecutionFilter.versionRange"),"[1.2.0.100,)");
        assertEquals(ctx.getAttribute("project.build.plugins.plugin.configuration." +
                                              "instructions.Import-Package"),"*");
    }

    @Test
    public void testXMLFileParsingNoPrefix() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.xml");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("project.properties.project.build.sourceEncoding"),"UTF-8");
        assertEquals(ctx.getAttribute("project.dependencies.dependency.scope"),"provided");
        assertEquals(ctx.getAttribute("project.build.pluginManagement.plugins.plugin.configuration" +
                                              ".lifecycleMappingMetadata.pluginExecutions.pluginExecution." +
                                              "pluginExecutionFilter.versionRange"),"[1.2.0.100,)");
        assertEquals(ctx.getAttribute("project.build.plugins.plugin.configuration." +
                                              "instructions.Import-Package"),"*");
    }

    @Test
    public void testXMLFileParsingCtxCheck() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.xml");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("project.properties.project.build.sourceEncoding"),"UTF-8");
        assertEquals(ctx.getAttribute("project.dependencies.dependency.scope"),"provided");
        assertEquals(ctx.getAttribute("project.build.pluginManagement.plugins.plugin.configuration" +
                                              ".lifecycleMappingMetadata.pluginExecutions.pluginExecution." +
                                              "pluginExecutionFilter.versionRange"),"[1.2.0.100,)");
        assertEquals(ctx.getAttribute("project.build.plugins.plugin.configuration." +
                                              "instructions.Import-Package"),"*");
        assertEquals(ctx.getAttribute("tmp.sdn-circuit-req-row_length"),"1");
    }

    @Test(expected = SvcLogicException.class)
    public void testToPropertiesInvalidXML() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test-invalid.xml");
        p.put("contextPrefix", "invalid");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("tmp.sdn-circuit-req-row_length"),"1");
    }

    @Test
    public void testXMLFileParsingListName() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.xml");
        p.put("contextPrefix", "test-xml-listName");
        p.put("fileBasedParsing","true");
        p.put("listName", "project.build.pluginManagement");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("test-xml-listName.project.build." +
                                              "pluginManagement.plugins.plugin.version"),null);
        assertEquals(ctx.getAttribute("test-xml-listName.project.build." +
                                              "plugins.plugin.groupId"),"org.apache.felix");
    }

    @Test
    public void testXMLFileParsingListNameAnother() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.xml");
        p.put("contextPrefix", "test-xml-listName");
        p.put("fileBasedParsing","true");
        p.put("listName", "project.modelVersion");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("test-xml-listName.project.modelVersion"),null);
        assertEquals(ctx.getAttribute("test-xml-listName.project.build." +
                                              "plugins.plugin.groupId"),"org.apache.felix");
    }

    @Test
    public void testTXTFileParsingNotFileBased() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.txt");
        p.put("contextPrefix", "test-txt");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("test-txt.service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("test-txt.service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("test-txt.operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("test-txt.service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("test-txt.service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
    }

    @Test
    public void testTXTFileParsingPrefixCheckNotFileBased() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.txt");
        p.put("contextPrefix", "");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
    }

    @Test
    public void testTXTFileParsingNoPrefixNotFileBased() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.txt");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
    }

    @Test
    public void testTXTFileParsingCtxCheckNotFileBased() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.txt");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
        assertEquals(ctx.getAttribute("tmp.sdn-circuit-req-row_length"),"1");
    }

    @Test
    public void testJSONFileArrayParsingNotFileBased() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.json");
        p.put("contextPrefix", "NotFileBased");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("NotFileBased.\"limit-value\""),"\"1920000\"");
        assertEquals(ctx.getAttribute("NotFileBased.\"hard-limit-expression\""),"\"max-server-speed * number-primary-servers\",");
        assertEquals(ctx.getAttribute("NotFileBased.\"test-inner-node\""),"\"Test-Value\"");
    }

    @Test
    public void testXMLFileInnerParsingNotFileBased() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test.xml");
        p.put("contextPrefix", "NotFileBased");
        p.put("listName", "project.modelVersion");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("NotFileBased.<name>RESTAPI"),"Call Node - Provider</name>");
        assertEquals(ctx.getAttribute("NotFileBased.<project"),
                     "xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                             " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">");
        assertEquals(ctx.getAttribute("NotFileBased.openECOMP"),"SDN-C");
        assertEquals(ctx.getAttribute("NotFileBased.<ignore"),"/>");
    }

    @Test
    public void testNoFileTypeNoPrefixNotFileBased() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
    }

    @Test(expected = SvcLogicException.class)
    public void testNoFileTypeParseReqError() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("file Name", "src/test/resources/test");
        p.put("fileBasedParsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
    }

    @Test
    public void testNoFileTypeParseError() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<String, String>();
        p.put("fileName", "src/test/resources/test");
        p.put("file Based % Parsing","true");

        PropertiesNode rcn = new PropertiesNode();
        rcn.readProperties(p, ctx);

        assertEquals(ctx.getAttribute("service-data.service-information.service-type"),"AVPN");
        assertEquals(ctx.getAttribute("service-configuration-notification-input.response-code"),"0");
        assertEquals(ctx.getAttribute("operational-data.avpn-ip-port-information.port-" +
                                              "level-cos.queueing.pe-per-class-queueing-behaviors.cos3-queueing"),"WRED");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.l1-customer-handoff"),"_1000BASELX");
        assertEquals(ctx.getAttribute("service-data.avpn-ip-port-information.avpn-" +
                                              "access-information.vlan-tag-control"),"_1Q");
    }
}
