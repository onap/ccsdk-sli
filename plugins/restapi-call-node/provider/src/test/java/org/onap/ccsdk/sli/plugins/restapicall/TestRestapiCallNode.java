/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.plugins.restapicall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRestapiCallNode {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(TestRestapiCallNode.class);

    @Before
    public void init() {
        System.setProperty("SDNC_CONFIG_DIR", "src/test/resources");
    }

    @Test
    public void testDelete() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<>();
        p.put("restapiUrl", "https://echo.getpostman.com/delete");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "pwd1");
        p.put("httpMethod", "delete");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testDeleteWithPayload() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        ctx.setAttribute("prop.name", "site1");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/sdwan-site.json");
        p.put("restapiUrl", "https://echo.getpostman.com/delete");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "pwd1");
        p.put("httpMethod", "delete");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testSendFile() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<>();
        p.put("fileName", "src/test/resources/test_file.txt");
        p.put("url", "https://testurl.test");
        p.put("user", "user");
        p.put("password", "*******");
        p.put("skipSending", "true"); // Set real url, user, password, when testing actual sending

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendFile(p, ctx);
    }

    @Test
    public void testJsonTemplate() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "3");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].source-uid", "APIDOC-123");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].action", "delete");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-timestamp", "2016-09-09 16:30:35.0");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].processing-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].service-clfi", "testClfi1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].clci", "clci");
        ctx.setAttribute("tmp.sdn-circuit-req-row[1].source-uid", "APIDOC-123");
        ctx.setAttribute("tmp.sdn-circuit-req-row[1].action", "delete");
        ctx.setAttribute("tmp.sdn-circuit-req-row[1].request-timestamp", "2016-09-09 16:30:35.0");
        ctx.setAttribute("tmp.sdn-circuit-req-row[1].request-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[1].processing-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[1].service-clfi", "testClfi1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[1].clci", "clci");
        ctx.setAttribute("tmp.sdn-circuit-req-row[2].source-uid", "APIDOC-123");
        ctx.setAttribute("tmp.sdn-circuit-req-row[2].action", "delete");
        ctx.setAttribute("tmp.sdn-circuit-req-row[2].request-timestamp", "2016-09-09 16:30:35.0");
        ctx.setAttribute("tmp.sdn-circuit-req-row[2].request-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[2].processing-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[2].service-clfi", "testClfi1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[2].clci", "clci");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/test-template.json");
        p.put("restapiUrl", "http://echo.getpostman.com");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "abc123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "response");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testInvalidRepeatTimes() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "a");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].source-uid", "APIDOC-123");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].action", "delete");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-timestamp", "2016-09-09 16:30:35.0");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].processing-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].service-clfi", "testClfi1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].clci", "clci");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/test-template.json");
        p.put("restapiUrl", "http://echo.getpostman.com");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "abc123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "response");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test(expected = SvcLogicException.class)
    public void testInvalidTemplatePath() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].source-uid", "APIDOC-123");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].action", "delete");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-timestamp", "2016-09-09 16:30:35.0");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].processing-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].service-clfi", "testClfi1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].clci", "clci");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resourcess/test-template.json");
        p.put("restapiUrl", "http://echo.getpostman.com");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "abc123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "response");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test(expected = SvcLogicException.class)
    public void testWithoutSkipSending() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].source-uid", "APIDOC-123");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].action", "delete");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-timestamp", "2016-09-09 16:30:35.0");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].processing-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].service-clfi", "testClfi1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].clci", "clci");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/test-template.json");
        p.put("restapiUrl", "http://echo.getpostman.com");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "abc123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "response");
        p.put("skipSending", "false");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }


    @Test(expected = SvcLogicException.class)
    public void testWithInvalidURI() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.sdn-circuit-req-row_length", "1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].source-uid", "APIDOC-123");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].action", "delete");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-timestamp", "2016-09-09 16:30:35.0");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].request-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].processing-status", "New");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].service-clfi", "testClfi1");
        ctx.setAttribute("tmp.sdn-circuit-req-row[0].clci", "clci");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/test-template.json");
        p.put("restapiUrl", "http://echo.  getpostman.com");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "abc123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "response");
        p.put("skipSending", "false");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testVpnJsonTemplate() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("prop.l3vpn.name", "10000000-0000-0000-0000-000000000001");
        ctx.setAttribute("prop.l3vpn.topology", "point_to_point");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/l3smvpntemplate.json");
        p.put("restapiUrl", "http://ipwan:18002/restconf/data/huawei-ac-net-l3vpn-svc:l3vpn-svc-cfg/vpn-services");
        p.put("restapiUser", "admin");
        p.put("restapiPassword", "admin123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "restapi-result");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testSiteJsonTemplate() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("prop.l3vpn.name", "10000000-0000-0000-0000-000000000001");
        ctx.setAttribute("prop.l3vpn.topology", "point_to_point");

        ctx.setAttribute("prop.l3vpn.site1_name", "10000000-0000-0000-0000-000000000002");
        ctx.setAttribute("prop.l3vpn.vpn-policy1-id", "10000000-0000-0000-0000-000000000003");
        ctx.setAttribute("prop.l3vpn.entry1-id", "1");
        ctx.setAttribute("prop.l3vpn.sna1_name", "10000000-0000-0000-0000-000000000004");
        ctx.setAttribute("prop.l3vpn.pe1_id", "a8098c1a-f86e-11da-bd1a-00112444be1e");
        ctx.setAttribute("prop.l3vpn.ac1_id", "a8098c1a-f86e-11da-bd1a-00112444be1b");
        ctx.setAttribute("prop.l3vpn.ac1-peer-ip", "192.168.1.1");
        ctx.setAttribute("prop.l3vpn.ac1-ip", "192.168.1.2");
        ctx.setAttribute("prop.l3vpn.sna1_svlan", "100");
        ctx.setAttribute("prop.l3vpn.ac1_protocol", "static");
        ctx.setAttribute("prop.l3vpn.sna1-route.ip-prefix", "192.168.1.1/24");
        ctx.setAttribute("prop.l3vpn.sna1-route.next-hop", "192.168.1.4");

        ctx.setAttribute("prop.l3vpn.site2_name", "10000000-0000-0000-0000-000000000005");
        ctx.setAttribute("prop.l3vpn.vpn-policy2-id", "10000000-0000-0000-0000-000000000006");
        ctx.setAttribute("prop.l3vpn.entry2-id", "1");
        ctx.setAttribute("prop.l3vpn.sna2_name", "10000000-0000-0000-0000-000000000007");
        ctx.setAttribute("prop.l3vpn.pe2_id", "a8098c1a-f86e-11da-bd1a-00112444be1a");
        ctx.setAttribute("prop.l3vpn.ac2_id", "a8098c1a-f86e-11da-bd1a-00112444be1c");
        ctx.setAttribute("prop.l3vpn.ac2-peer-ip", "192.168.1.6");
        ctx.setAttribute("prop.l3vpn.ac2-ip", "192.168.1.5");
        ctx.setAttribute("prop.l3vpn.sna2_svlan", "200");
        ctx.setAttribute("prop.l3vpn.ac2_protocol", "bgp");
        ctx.setAttribute("prop.l3vpn.peer2-ip", "192.168.1.7");
        ctx.setAttribute("prop.l3vpn.ac2_protocol_bgp_as", "200");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/l3smsitetemplate.json");
        p.put("restapiUrl", "http://ipwan:18002/restconf/data/huawei-ac-net-l3vpn-svc:l3vpn-svc-cfg/sites");
        p.put("restapiUser", "admin");
        p.put("restapiPassword", "admin123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "restapi-result");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testVrfJsonTemplate() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("prop.l3vpn.vrf1-id", "10000000-0000-0000-0000-000000000007");
        ctx.setAttribute("prop.l3vpn.vpn-policy1-id", "10000000-0000-0000-0000-000000000003");
        ctx.setAttribute("prop.l3vpn.pe1_id", "a8098c1a-f86e-11da-bd1a-00112444be1e");
        ctx.setAttribute("prop.l3vpn.vrf2-id", "10000000-0000-0000-0000-000000000009");
        ctx.setAttribute("prop.l3vpn.vpn-policy2-id", "10000000-0000-0000-0000-000000000006");
        ctx.setAttribute("prop.l3vpn.pe2_id", "a8098c1a-f86e-11da-bd1a-00112444be1a");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/l3smvrftemplate.json");
        p.put("restapiUrl", "http://ipwan:18002/restconf/data/huawei-ac-net-l3vpn-svc:l3vpn-svc-cfg/vrf-attributes");
        p.put("restapiUser", "admin");
        p.put("restapiPassword", "admin123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "restapi-result");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testDeleteVpnJsonTemplate() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("prop.l3vpn.name", "10000000-0000-0000-0000-000000000001");
        ctx.setAttribute("prop.l3vpn.topology", "point_to_point");

        Map<String, String> p = new HashMap<>();
        //p.put("templateFileName", "src/test/resources/l3smvpntemplate.json");
        p.put("restapiUrl", "http://ipwan:18002/restconf/data/huawei-ac-net-l3vpn-svc:l3vpn-svc-cfg/vpn-services"
            + "/vpnservice=10000000-0000-0000-0000-000000000001");
        p.put("restapiUser", "admin");
        p.put("restapiPassword", "admin123");
        p.put("format", "json");
        p.put("httpMethod", "delete");
        p.put("responsePrefix", "restapi-result");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testL2DciTemplate() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("prop.dci-connects.id", "Id1");
        ctx.setAttribute("prop.dci-connects.name", "Name1");
        ctx.setAttribute("prop.dci-connects.local_networks[0]", "NetId1");
        ctx.setAttribute("prop.dci-connects.local_networks[1]", "NetId2");
        ctx.setAttribute("prop.dci-connects.evpn_irts[0]", "100:1");
        ctx.setAttribute("prop.dci-connects.evpn_erts[0]", "100:2");
        ctx.setAttribute("prop.dci-connects.evpn_irts[1]", "200:1");
        ctx.setAttribute("prop.dci-connects.evpn_erts[1]", "200:2");
        ctx.setAttribute("prop.dci-connects.vni", "1");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/l2-dci-connects-template.json");
        p.put("restapiUrl", "http://echo.getpostman.com");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "abc123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "response");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testL3DciTemplate() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("prop.dci-connects.id", "Id1");
        ctx.setAttribute("prop.dci-connects.name", "Name1");
        ctx.setAttribute("prop.dci-connects.local_networks_length", "2");
        ctx.setAttribute("prop.dci-connects.local_networks[0]", "NetId1");
        ctx.setAttribute("prop.dci-connects.local_networks[1]", "NetId2");
        ctx.setAttribute("prop.dci-connects.evpn_irts[0]", "100:1");
        ctx.setAttribute("prop.dci-connects.evpn_erts[0]", "100:2");
        ctx.setAttribute("prop.dci-connects.evpn_irts[1]", "200:1");
        ctx.setAttribute("prop.dci-connects.evpn_erts[1]", "200:2");
        ctx.setAttribute("prop.dci-connects.vni", "1");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/l3-dci-connects-template.json");
        p.put("restapiUrl", "http://echo.getpostman.com");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "abc123");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "response");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);

    }

    @Test
    public void testControllerTokenTemplate() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("prop.sdncRestApi.thirdpartySdnc.user", "admin");
        ctx.setAttribute("prop.sdncRestApi.thirdpartySdnc.password", "admin123");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/actokentemplate.json");
        p.put("restapiUrl", "https://ipwan:18002/controller/v2/tokens");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "restapi-result");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }


    @Test
    public void testDeleteNoneAsContentType() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();

        Map<String, String> p = new HashMap<>();
        p.put("restapiUrl", "https://echo.getpostman.com/delete");
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "pwd1");
        p.put("httpMethod", "delete");
        p.put("format", "none");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }

    @Test
    public void testPostNoneAsContentType() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("prop.l3vpn.name", "10000000-0000-0000-0000-000000000001");
        ctx.setAttribute("prop.l3vpn.topology", "point_to_point");

        Map<String, String> p = new HashMap<>();
        p.put("templateFileName", "src/test/resources/l3smvpntemplate.json");
        p.put("restapiUrl", "http://ipwan:18002/restconf/data/huawei-ac-net-l3vpn-svc:l3vpn-svc-cfg/vpn-services");
        p.put("restapiUser", "admin");
        p.put("restapiPassword", "admin123");
        p.put("format", "none");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "restapi-result");
        p.put("skipSending", "true");

        RestapiCallNode rcn = new RestapiCallNode();
        rcn.sendRequest(p, ctx);
    }
    /*
     * {
  "partnerOne": {
    "url": "http://localhost:7001"                                                                                                                                                             4 http://uebsb93kcdc.it.att.com:3904",
    "test": "/metrics"
  },
  "partnerTwo": {
    "url": "http://localhost:7002",
    "user": "controller_user",
    "password": "P@ssword",
    "test": "/metrics"
  },
  "partnerThree": {
    "url": "http://localhost:7003",
    "user": "controller_admin"
  }
}
     */
    @Test
    public void testPartners() throws Exception{
	String partnerTwoKey = "partnerTwo";
	String partnerTwoUsername = "controller_user";
	String partnerTwoPassword = "P@ssword";

	System.setProperty("SDNC_CONFIG_DIR", "src/test/resources");
        RestapiCallNode rcn = new RestapiCallNode();
        assertNull(rcn.partnerStore.get("partnerOne"));
        PartnerDetails details = rcn.partnerStore.get(partnerTwoKey);
        assertEquals(partnerTwoUsername,details.username);
        assertEquals(partnerTwoPassword,details.password);
        assertNull(rcn.partnerStore.get("partnerThree"));

        //In this scenario the caller expects username, password and url to be picked up from the partners json
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("partner", partnerTwoKey);
	rcn.handlePartner(paramMap );
        assertEquals(partnerTwoUsername,paramMap.get(RestapiCallNode.restapiUserKey));
        assertEquals(partnerTwoPassword,paramMap.get(RestapiCallNode.restapiPasswordKey));
        assertEquals("http://localhost:7002",paramMap.get(RestapiCallNode.restapiUrlString));

        //In this scenario the caller expects username, password and url to be picked up from the partners json
        //the provided suffix will be appended to the default url from the partners json
        paramMap = new HashMap<>();
        paramMap.put("partner", partnerTwoKey);
        paramMap.put("restapiUrlSuffix", "/networking/v1/instance/3");
	rcn.handlePartner(paramMap);
	Parameters p = new Parameters();
	RestapiCallNode.getParameters(paramMap, p);
        assertEquals(partnerTwoUsername,p.restapiUser);
        assertEquals(partnerTwoPassword,p.restapiPassword);
        assertEquals("http://localhost:7002/networking/v1/instance/3",p.restapiUrl);
    }

    @Test
    public void retryPolicyBean() throws Exception {
	Integer retries = 3;
	String first = "http://localhost:7001";
	String second = "http://localhost:7001";

	RetryPolicy p = new RetryPolicy(new String[] {first,second}, retries);
	assertEquals(retries,p.getMaximumRetries());
	assertNotNull(p.getRetryMessage());
	String next = p.getNextHostName();
	assertEquals(second,next);
	assertEquals(1,p.getRetryCount());
	next = p.getNextHostName();
	assertEquals(first,next);
	assertEquals(2,p.getRetryCount());
    }

    @Test
    public void testEmbeddedJsonTemplate() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
	String complexObj = "{\"image_name\":\"Ubuntu 14.04\",\"service-instance-id\":\"1\",\"vnf-model-customization-uuid\":\"2f\",\"vnf-id\":\"3b\"}";
	ctx.setAttribute("reqId", "1235");
        ctx.setAttribute("subReqId", "054243");
        ctx.setAttribute("actionName", "CREATE");
        ctx.setAttribute("myPrefix", "2016-09-09 16:30:35.0");
        ctx.setAttribute("complexObj", complexObj);
        RestapiCallNode rcn = new RestapiCallNode();
        String request = rcn.buildXmlJsonRequest(ctx, rcn.readFile("src/test/resources/testEmbeddedTemplate.json"), Format.JSON);
        //This will throw a JSONException and fail the test case if rest api call node doesn't form valid JSON
        assertNotNull(new JSONObject(request));
    }

    @Test
    public void testMultiLineEmbeddedJsonTemplate() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        String complexObj = "{\n"
                            + "  \"image_name\": \"Ubuntu 14.04\",\n"
                            + "  \"service-instance-id\": \"1\",\n"
                            + "  \"vnf-model-customization-uuid\": \"2f\",\n"
                            + "  \"vnf-id\": \"3b\"\n"
                            + "}";
        ctx.setAttribute("reqId", "1235");
        ctx.setAttribute("subReqId", "054243");
        ctx.setAttribute("actionName", "CREATE");
        ctx.setAttribute("myPrefix", "2016-09-09 16:30:35.0");
        ctx.setAttribute("complexObj", complexObj);
        RestapiCallNode rcn = new RestapiCallNode();
        String request = rcn.buildXmlJsonRequest(ctx, rcn.readFile("src/test/resources/testMultiLineEmbeddedTemplate.json"), Format.JSON);
        //This will throw a JSONException and fail the test case if rest api call node doesn't form valid JSON
        assertNotNull(new JSONObject(request));
    }
    
    @Test
    public void testGetMultipleUrls() throws Exception{
       String[] urls =  RestapiCallNode.getMultipleUrls("http://localhost:8008/rest/restconf/data/abc:def/abc:action=Create,deviceType=Banana,https://localhost:8008/rest/restconf/data/abc:def/abc:action=Create,deviceType=Potato");
       assertEquals("http://localhost:8008/rest/restconf/data/abc:def/abc:action=Create,deviceType=Banana",urls[0]);
       assertEquals("https://localhost:8008/rest/restconf/data/abc:def/abc:action=Create,deviceType=Potato",urls[1]);

       urls =  RestapiCallNode.getMultipleUrls("https://wiki.onap.org/,http://localhost:7001/,http://wiki.onap.org/");
       assertEquals("https://wiki.onap.org/",urls[0]);
       assertEquals("http://localhost:7001/",urls[1]);
       assertEquals("http://wiki.onap.org/",urls[2]);
       
       urls =  RestapiCallNode.getMultipleUrls("https://wiki.onap.org/test=4,5,6,http://localhost:7001/test=1,2,3,http://wiki.onap.org/test=7,8,9,10");
       assertEquals("https://wiki.onap.org/test=4,5,6",urls[0]);
       assertEquals("http://localhost:7001/test=1,2,3",urls[1]);
       assertEquals("http://wiki.onap.org/test=7,8,9,10",urls[2]);

       urls =  RestapiCallNode.getMultipleUrls("https://wiki.onap.org/,https://readthedocs.org/projects/onap/");
       assertEquals("https://wiki.onap.org/",urls[0]);
       assertEquals("https://readthedocs.org/projects/onap/",urls[1]);
    }
    
    @Test
    public void testContainsMultipleUrls() throws Exception{
        assertFalse(RestapiCallNode.containsMultipleUrls("https://wiki.onap.org/"));
        assertFalse(RestapiCallNode.containsMultipleUrls("http://wiki.onap.org/"));
        assertFalse(RestapiCallNode.containsMultipleUrls("http://localhost:8008/rest/restconf/data/abc:def/abc:action=Create,deviceType=Banana"));
        assertFalse(RestapiCallNode.containsMultipleUrls("https://localhost:8008/params=1,2,3,4,5,6"));

        assertTrue(RestapiCallNode.containsMultipleUrls("https://wiki.onap.org/,https://readthedocs.org/projects/onap/"));
        assertTrue(RestapiCallNode.containsMultipleUrls("http://localhost:7001/,http://localhost:7002"));
        assertTrue(RestapiCallNode.containsMultipleUrls("http://localhost:8008/rest/restconf/data/abc:def/abc:action=Create,deviceType=Banana,https://localhost:8008/rest/restconf/data/abc:def/abc:action=Create,deviceType=Potato"));
        assertTrue(RestapiCallNode.containsMultipleUrls("https://wiki.onap.org/,http://localhost:7001/,http://wiki.onap.org/"));
        assertTrue(RestapiCallNode.containsMultipleUrls("https://wiki.onap.org/test=4,5,6,http://localhost:7001/test=1,2,3,http://wiki.onap.org/test=7,8,9,10"));
    }

}
