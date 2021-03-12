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

package org.onap.ccsdk.sli.adaptors.ansible.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdaptorImpl;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdaptorPropertiesProviderImpl;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleMessageParser;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptorConstants.*;
@RunWith(MockitoJUnitRunner.class)
public class TestAnsibleAdaptorImpl {

    private static final String PENDING = "100";
    private static final String AGENT_URL = "https://192.168.1.1";

    private static String KEYSTORE_PSWD;
    private static Properties properties;
    private boolean testMode = true;

    private AnsibleAdaptorImpl adaptor;
    private AnsibleResult result;
    private AnsibleAdaptorImpl spyAdaptor;
    private Map<String, String> params;
    private SvcLogicContext svcContext;
    private JSONObject jsonPayload;

    @Mock
    private AnsibleMessageParser messageProcessor;

    @BeforeClass
    public static void once() {
        properties = new AnsibleAdaptorPropertiesProviderImpl().getProperties();
        KEYSTORE_PSWD = properties.getProperty("org.onap.appc.adaptor.ansible.trustStore.trustPasswd");
    }

    /**
     * Use reflection to locate fields and methods so that they can be manipulated
     * during the test to change the internal state accordingly.
     */
    @Before
    public void setup() {
        testMode = true;
        svcContext = new SvcLogicContext();
        adaptor = new AnsibleAdaptorImpl(testMode);
        params = new HashMap<>();
        params.put("AgentUrl", AGENT_URL);
        jsonPayload = new JSONObject();
        jsonPayload.put("Id", "100");
        jsonPayload.put("User", "test");
        jsonPayload.put("Password", "test");
        jsonPayload.put("PlaybookName", "test_playbook.yaml");
        jsonPayload.put("Timeout", "60000");
        jsonPayload.put("AgentUrl", AGENT_URL);
        result = new AnsibleResult();
        result.setStatusMessage("Success");
        result.setResults("Success");
        result.setOutput("{}");
        Whitebox.setInternalState(adaptor, "messageProcessor", messageProcessor);
        spyAdaptor = Mockito.spy(adaptor);
    }

    @After
    public void tearDown() {
        testMode = false;
        adaptor = null;
        params = null;
        svcContext = null;
    }

    @Test
    public void reqExec_shouldSetPending() throws SvcLogicException {
        result.setStatusCode(Integer.parseInt(PENDING));
        when(messageProcessor.reqMessage(params)).thenReturn(jsonPayload);
        when(messageProcessor.parsePostResponse(anyString())).thenReturn(result);
        spyAdaptor.reqExec(params, svcContext);
        assertEquals(PENDING, svcContext.getAttribute(RESULT_CODE_ATTRIBUTE_NAME));
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecResult_shouldSetSuccess() throws SvcLogicException {
        params.put("Id", "100");
        result.setStatusMessage(SUCCESS);
        when(messageProcessor.reqUriResult(params)).thenReturn(AGENT_URL);
        when(messageProcessor.parseGetResponse(anyString())).thenReturn(result);
        spyAdaptor.reqExecResult(params, svcContext);
        assertEquals(SUCCESS, svcContext.getAttribute(SUCCESS));
    }
    @Test(expected = SvcLogicException.class)
    public void reqExecResult_Failure() throws SvcLogicException {
        params.put("Id", "100");
        result.setStatusCode(100);
        result.setStatusMessage("Failed");
        JSONObject cData = new JSONObject();
        cData.put("GatewayInfo", "Radius");
        result.setConfigData(cData.toString());
        result.setOutput(cData.toString());
        when(messageProcessor.reqUriResult(params)).thenReturn(AGENT_URL);
        when(messageProcessor.parseGetResponse(anyString())).thenReturn(result);
        adaptor.reqExecResult(params, svcContext);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecResult_SvcLogicException() throws SvcLogicException {
        when(messageProcessor.reqUriResult(params)).thenThrow(new SvcLogicException());
        adaptor.reqExecResult(params, svcContext);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecResult_numberFormatException()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        when(messageProcessor.reqUriResult(params)).thenThrow(new NumberFormatException());
        adaptor.reqExecResult(params, svcContext);
    }

    @Test
    public void reqExecLog_shouldSetMessage() throws SvcLogicException {
        params.put("Id", "101");
        when(messageProcessor.reqUriLog(params)).thenReturn(AGENT_URL);
        adaptor.reqExecLog(params, svcContext);
        String message = getResponseMessage();
        assertEquals(message, svcContext.getAttribute(LOG_ATTRIBUTE_NAME));
    }

    private String getResponseMessage() {
        JSONObject response = new JSONObject();
        response.put(STATUS_CODE, 200);
        response.put(STATUS_MESSAGE, "FINISHED");
        JSONObject results = new JSONObject();

        JSONObject vmResults = new JSONObject();
        vmResults.put(STATUS_CODE, 200);
        vmResults.put(STATUS_MESSAGE, "SUCCESS");
        vmResults.put("Id", "");
        results.put("192.168.1.10", vmResults);

        response.put("Results", results);
        return response.toString();
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecException()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        when(messageProcessor.reqUriLog(params)).thenThrow(new SvcLogicException("Appc Exception"));
        adaptor.reqExecLog(params, svcContext);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExec_SvcLogicException()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        when(messageProcessor.reqMessage(params)).thenThrow(new SvcLogicException());
        adaptor.reqExec(params, svcContext);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExec_JsonException()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        when(messageProcessor.reqMessage(params)).thenThrow(new JSONException("Json Exception"));
        adaptor.reqExec(params, svcContext);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExec_NumberFormatException()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException {
        when(messageProcessor.reqMessage(params)).thenThrow(new NumberFormatException("Numbre Format Exception"));
        adaptor.reqExec(params, svcContext);
    }

    @Test
    public void testInitializeWithDefault() {
        properties.setProperty("org.onap.appc.adaptor.ansible.clientType", "");
        adaptor = new AnsibleAdaptorImpl();
        assertNotNull(adaptor);
    }

    @Test
    public void testInitializeWithTrustAll() {
        properties.setProperty("org.onap.appc.adaptor.ansible.clientType", "TRUST_ALL");
        adaptor = new AnsibleAdaptorImpl();
        assertNotNull(adaptor);
    }

    @Test
    public void testInitializeWithTrustCert() {
        properties.setProperty("org.onap.appc.adaptor.ansible.clientType", "TRUST_CERT");
        properties.setProperty("org.onap.appc.adaptor.ansible.trustStore.trustPasswd", KEYSTORE_PSWD);
        adaptor = new AnsibleAdaptorImpl();
        assertNotNull(adaptor);
    }

    @Test
    public void testInitializeWithException() {
        properties.setProperty("org.onap.appc.adaptor.ansible.clientType", "TRUST_CERT");
        properties.setProperty("org.onap.appc.adaptor.ansible.trustStore.trustPasswd", "appc");
        adaptor = new AnsibleAdaptorImpl();
        assertNotNull(adaptor);
    }

}