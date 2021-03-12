/*-
 * ============LICENSE_START=======================================================
 * ONAP : SLI
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.adapter.ansible.model;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleMessageParser;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestAnsibleMessageParser {
    private AnsibleMessageParser msgParser;

    @Before
    public void setup() {
        msgParser = new AnsibleMessageParser();
    }

    @Test
    public void testReqMessage() throws Exception {
        // String result = "{"\AgentUrl : TestAgentUrl}";
        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgentUrl");
        params.put("PlaybookName", "TestPlaybookName");
        params.put("User", "TestUser");
        params.put("Password", "TestPassword");

        JSONObject jObject = msgParser.reqMessage(params);
        assertEquals("TestAgentUrl", jObject.get("AgentUrl"));

    }

    @Test
    public void testReqUriResult() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgentUrl");
        params.put("Id", "TestId");
        params.put("User", "TestUser");
        params.put("Password", "TestPassword");

        String result = msgParser.reqUriResult(params);
        assertTrue(result.contains("TestId"));

    }

    @Test
    public void testReqUriLog() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgent-Url");
        params.put("Id", "TestId");
        params.put("User", "TestUser");
        params.put("Password", "TestPassword");

        String result = msgParser.reqUriLog(params);
        assertTrue(result.contains("TestAgent-Url"));

    }

    @Test
    public void TestParsePostResponse() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"100\",\"StatusMessage\":\"TestMessage\"}";
        ansibleResult = msgParser.parsePostResponse(input);
        assertEquals("TestMessage", ansibleResult.getStatusMessage());

    }

    @Test(expected = Exception.class)
    public void TestParsePostResponseException() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"600\",\"StatusMessage\":\"TestMessage\"}";
        String result = "Error parsing response";
        ansibleResult = msgParser.parsePostResponse(input);
        assertTrue(ansibleResult.getStatusMessage().contains(result));
    }

    @Test
    public void TestParsePostResponseException2() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"600\"}";
        String result = "Error parsing response";
        ansibleResult = msgParser.parsePostResponse(input);
        assertTrue(ansibleResult.getStatusMessage().contains(result));
    }

    @Test(expected = Exception.class)
    public void TestParseGetResponseException() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"100\",\"StatusMessage\":\"TestMessage\"}";
        String result = "Invalid FinalResponse code";
        ansibleResult = msgParser.parseGetResponse(input);
        assertTrue(ansibleResult.getStatusMessage().contains(result));
    }

    @Test
    public void TestParseGetResponseExec() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"200\",\"StatusMessage\":\"TestMessage\"}";
        String result = "Results not found in GET for response";
        ansibleResult = msgParser.parseGetResponse(input);
        assertTrue(ansibleResult.getStatusMessage().contains(result));
    }

    @Test
    public void TestParseGetResponse() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"200\",\"StatusMessage\":\"TestMessage\",\"Results\":{\"host\":{\"StatusCode\":\"200\",\"StatusMessage\":\"SUCCESS\"}},\"Output\":{\"results-output\":{\"OutputResult\":\"TestOutPutResult\"}}}";
        ansibleResult = msgParser.parseGetResponse(input);
        String result = "TestOutPutResult";
        assertTrue(ansibleResult.getOutput().contains(result));
    }

    @Test
    public void TestParseGetResponseEx() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"200\",\"StatusMessage\":\"TestMessage\",\"Results\":{\"host\":\"TestHost\"}}";
        ansibleResult = msgParser.parseGetResponse(input);
        String result = "Error processing response message";
        assertTrue(ansibleResult.getStatusMessage().contains(result));
    }

    @Test
    public void TestParseGetResponseJsonEx() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"200\",\"StatusMessage\":\"TestMessage\",\"Results\":\"host\":\"TestHost\"}";
        ansibleResult = msgParser.parseGetResponse(input);
        String result = "Error parsing response";
        assertTrue(ansibleResult.getStatusMessage().contains(result));
    }

    @Test
    public void TestParseGetResponseResultEx() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"200\",\"StatusMessage\":\"TestMessage\",\"Results\":{\"host\":{\"StatusCode\":\"100\",\"StatusMessage\":\"Failure\"}},\"Output\":{\"results-output\":{\"OutputResult\":\"TestOutPutResult\"}}}";
        ansibleResult = msgParser.parseGetResponse(input);
        String result = "TestOutPutResult";
        assertTrue(ansibleResult.getOutput().contains(result));
    }

    @Test
    public void testParseOptionalParam() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgentUrl");
        params.put("PlaybookName", "TestPlaybookName");
        params.put("User", "TestUser");
        params.put("Password", "TestPassword");
        params.put("Timeout", "3");
        params.put("Version", "1");
        params.put("InventoryNames", "VNFC");
        JSONObject jObject = msgParser.reqMessage(params);
        assertEquals("1", jObject.get("Version"));
        assertEquals("VNFC", jObject.get("InventoryNames"));
    }

    @Test
    public void testParseOptionalParamForEnvParameters() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgentUrl");
        params.put("PlaybookName", "TestPlaybookName");
        params.put("User", "TestUser");
        params.put("Password", "TestPassword");
        params.put("EnvParameters", "{name:value}");
        JSONObject result = msgParser.reqMessage(params);
        assertEquals("TestAgentUrl", result.get("AgentUrl"));
        assertEquals("TestPlaybookName", result.get("PlaybookName"));
        assertEquals("TestUser", result.get("User"));
        assertEquals("TestPassword", result.get("Password"));
    }

    @Test
    public void TestParseGetConfigResponseResult() throws Exception {
        AnsibleResult ansibleResult;
        String input = "{\"StatusCode\":\"200\",\"StatusMessage\":\"TestMessage\",\"Results\":{\"host\":{\"StatusCode\":\"200\",\"StatusMessage\":\"SUCCESS\",\"Output\":{\"info\":{\"configData\":{\"abc\":\"TestOutPutResult\",\"rtr\":\"vfc\"}}}}}}";
        ansibleResult = msgParser.parseGetResponse(input);
        String result = "abc";
        assertTrue(ansibleResult.getConfigData().contains(result));
    }

    @Test
    public void testParseOptionalParamTest2() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgentUrl");
        params.put("PlaybookName", "TestPlaybookName");
        params.put("User", "TestUser");
        params.put("Password", "TestPassword");
        //params.put("Timeout", "3");
        params.put("Version", "1");
        params.put("InventoryNames", "VNFC");
        params.put("Timeout", "4");
        params.put("EnvParameters", "{ \"userID\": \"$0002\", \"vnf-type\" : \"\", \"vnf\" : \"abc\" }");
        params.put("NodeList", "${Nodelist}");
        JSONObject jObject = msgParser.reqMessage(params);
        assertEquals("1", jObject.get("Version"));
        assertEquals("4", jObject.get("Timeout"));
    }

    @Test
    public void testReqUriResultWithIPs() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "http://xx:yy:zz");
        params.put("Id", "TestId");
        params.put("User", "TestUser");
        params.put("Password", "TestPassword");
        String serverIp = "10.0.2.3";
        String actual = msgParser.reqUriResultWithIP(params, serverIp);
        String expected = "http://10.0.2.3:yy:zz?Id=TestId&Type=GetResult";
        assertEquals(expected, actual);
    }

}
