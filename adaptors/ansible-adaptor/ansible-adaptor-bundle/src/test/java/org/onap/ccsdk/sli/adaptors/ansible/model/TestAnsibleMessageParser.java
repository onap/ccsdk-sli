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

package org.onap.ccsdk.sli.adaptors.ansible.model;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleMessageParser;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

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
        params.put("Password", "TestPass");

        assertEquals("TestAgentUrl", msgParser.reqMessage(params).get("AgentUrl"));
    }

    @Test
    public void testReqUriResult() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgentUrl");
        params.put("Id", "TestId");
        params.put("User", "TestUser");
        params.put("Password", "TestPass");

        assertTrue(msgParser.reqUriResult(params).contains("TestId"));
    }

    @Test
    public void testReqUriLog() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgent-Url");
        params.put("Id", "TestId");
        params.put("User", "TestUser");
        params.put("Password", "TestPass");

        assertTrue(msgParser.reqUriLog(params).contains("TestAgent-Url"));
    }

    @Test
    public void TestParsePostResponse() throws Exception {
        String input = "{\"StatusCode\":\"100\",\"StatusMessage\":\"TestMessage\"}";
        assertEquals("TestMessage", msgParser.parsePostResponse(input).getStatusMessage());

    }

    @Test(expected = SvcLogicException.class)
    public void TestParsePostResponseException() throws Exception {
        String input = "{\"StatusCode\":\"600\",\"StatusMessage\":\"TestMessage\"}";
        assertTrue(msgParser.parsePostResponse(input).getStatusMessage().contains("Error parsing response"));
    }

    @Test(expected = SvcLogicException.class)
    public void TestParsePostResponseException2() throws Exception {
        String input = "{\"StatusCode\":\"600\"}";
        assertTrue(msgParser.parsePostResponse(input).getStatusMessage().contains("Error parsing response"));
    }

    @Test(expected = SvcLogicException.class)
    public void TestParseGetResponseException() throws Exception {
        String input = "{\"StatusCode\":\"100\",\"StatusMessage\":\"TestMessage\"}";
        assertTrue(msgParser.parseGetResponse(input).getStatusMessage().contains("Invalid FinalResponse code"));
    }

    @Test
    public void TestParseGetResponseExec() throws Exception {
        String input = "{\"StatusCode\":\"200\",\"StatusMessage\":\"TestMessage\"}";
        assertTrue(msgParser.parseGetResponse(input).getStatusMessage().contains("Results not found in GET for response"));
    }

    @Test
    public void TestParseGetResponse() throws Exception {
        String input = "{"
                       + "  \"StatusCode\": \"200\","
                       + "  \"StatusMessage\": \"TestMessage\","
                       + "  \"Results\": {"
                       + "    \"host\": {"
                       + "      \"StatusCode\": \"200\","
                       + "      \"StatusMessage\": \"SUCCESS\""
                       + "    }"
                       + "  },"
                       + "  \"Output\": {"
                       + "    \"results-output\": {"
                       + "      \"OutputResult\": \"TestOutPutResult\""
                       + "    }"
                       + "  }"
                       + "}";
        assertTrue(msgParser.parseGetResponse(input).getOutput().contains("TestOutPutResult"));
    }

    @Test
    public void TestParseGetResponseEx() throws Exception {
        String input = "{\"StatusCode\":\"200\",\"StatusMessage\":\"TestMessage\",\"Results\":{\"host\":\"TestHost\"}}";
        assertTrue(msgParser.parseGetResponse(input).getStatusMessage().contains("Error processing response message"));
    }

    @Test
    public void TestParseGetResponseJsonEx() throws Exception {
        String input = "{\"StatusCode\":\"200\",\"StatusMessage\":\"TestMessage\",\"Results\":\"host\":\"TestHost\"}";
        assertTrue(msgParser.parseGetResponse(input).getStatusMessage().contains("Error parsing response"));
    }

    @Test
    public void TestParseGetResponseResultEx() throws Exception {
        String input = "{"
                       + "  \"StatusCode\": \"200\","
                       + "  \"StatusMessage\": \"TestMessage\","
                       + "  \"Results\": {"
                       + "    \"host\": {"
                       + "      \"StatusCode\": \"100\","
                       + "      \"StatusMessage\": \"Failure\""
                       + "    }"
                       + "  },"
                       + "  \"Output\": {"
                       + "    \"results-output\": {"
                       + "      \"OutputResult\": \"TestOutPutResult\""
                       + "    }"
                       + "  }"
                       + "}";
        assertTrue(msgParser.parseGetResponse(input).getOutput().contains("TestOutPutResult"));
    }

    @Test
    public void testParseOptionalParam() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgentUrl");
        params.put("PlaybookName", "TestPlaybookName");
        params.put("User", "TestUser");
        params.put("Password", "TestPass");
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
        params.put("Password", "TestPass");
        params.put("EnvParameters", "{name:value}");

        JSONObject result = msgParser.reqMessage(params);
        assertEquals("TestAgentUrl", result.get("AgentUrl"));
        assertEquals("TestPlaybookName", result.get("PlaybookName"));
        assertEquals("TestUser", result.get("User"));
        assertEquals("TestPass", result.get("Password"));
    }

    @Test
    public void TestParseGetConfigResponseResult() throws Exception {
        String input = "{"
                       + "  \"StatusCode\": \"200\","
                       + "  \"StatusMessage\": \"TestMessage\","
                       + "  \"Results\": {"
                       + "    \"host\": {"
                       + "      \"StatusCode\": \"200\","
                       + "      \"StatusMessage\": \"SUCCESS\","
                       + "      \"Output\": {"
                       + "        \"info\": {"
                       + "          \"configData\": {"
                       + "            \"abc\": \"TestOutPutResult\","
                       + "            \"rtr\": \"vfc\""
                       + "          }"
                       + "        }"
                       + "      }"
                       + "    }"
                       + "  }"
                       + "}";
        assertTrue(msgParser.parseGetResponse(input).getConfigData().contains("abc"));
    }

    @Test
    public void testParseOptionalParamTest2() throws Exception {

        Map<String, String> params = new HashMap<>();
        params.put("AgentUrl", "TestAgentUrl");
        params.put("PlaybookName", "TestPlaybookName");
        params.put("User", "TestUser");
        params.put("Password", "TestPass");
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
        params.put("Password", "TestPass");
        String serverIp = "10.0.2.3";
        String actual = msgParser.reqUriResultWithIP(params, serverIp);
        String expected = "http://10.0.2.3:yy:zz?Id=TestId&Type=GetResult";
        assertEquals(expected, actual);
    }

}
