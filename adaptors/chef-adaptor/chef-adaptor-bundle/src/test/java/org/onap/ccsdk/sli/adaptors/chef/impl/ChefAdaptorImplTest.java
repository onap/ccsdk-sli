/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * =============================================================================
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

package org.onap.ccsdk.sli.adaptors.chef.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.ChefApiClientFactory;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ChefAdaptorImplTest {

    private static final String EXPECTED_NODE_OBJECT_ATTR_NAME = "chef.nodeObject";
    private static final String RESULT_CODE_ATTR_KEY = "chefClientResult.code";
    private static final String RESULT_MESSAGE_ATTR_KEY = "chefClientResult.message";
    private static final String EXPECTED_RESPONSE_MSG = "chefResponseMessage";
    private static final String IP_PARAM = "ip";
    private static final String ENDPOINT_IP = "http://127.0.0.1";
    private static final String CHEF_AGENT_CODE_KEY = "chefAgent.code";
    private static final String CHEF_AGENT_MESSAGE_KEY = "chefAgent.message";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ChefApiClientFactory chefApiClientFactory;
    @Mock
    private org.onap.ccsdk.sli.adaptors.chef.impl.PrivateKeyChecker privateKeyChecker;

    @InjectMocks
    private org.onap.ccsdk.sli.adaptors.chef.impl.ChefAdaptorFactory chefAdaptorFactory;

    @Test
    public void nodeObjectBuilder_shouldBuildJsonNodeObject_forPassedParams_andAddToSvcLogicContext() {
        // GIVEN
        Map<String, String> params = givenInputParams();
        SvcLogicContext svcLogicContext = new SvcLogicContext();

        // WHEN
        chefAdaptorFactory.create().nodeObejctBuilder(params, svcLogicContext);

        // THEN
        assertThat(resultJson(svcLogicContext)).isEqualTo(expectedJson());
    }

    private String resultJson(SvcLogicContext svcLogicContext) {
        String resultJsonString = svcLogicContext.getAttribute(EXPECTED_NODE_OBJECT_ATTR_NAME);
        return new JSONObject(resultJsonString).toString();
    }

    private Map<String, String> givenInputParams() {
        return ImmutableMap.<String, String>builder()
                .put("nodeobject.name", "testNodeName")
                .put("nodeobject.normal", "val:normal")
                .put("nodeobject.overrides", "val:override")
                .put("nodeobject.defaults", "val:default")
                .put("nodeobject.run_list", "val1,val2,val3")
                .put("nodeobject.chef_environment", "testChefEnvVal")
                .build();
    }

    private String expectedJson() {
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("json_class", "Chef::Node");
        expectedJson.put("chef_type", "node");
        expectedJson.put("automatic", Collections.emptyMap());
        expectedJson.put("name", "testNodeName");
        expectedJson.put("normal", ImmutableMap.of("val", "normal"));
        expectedJson.put("override", ImmutableMap.of("val", "override"));
        expectedJson.put("default", ImmutableMap.of("val", "default"));
        expectedJson.put("run_list", ImmutableList.of("val1", "val2", "val3"));
        expectedJson.put("chef_environment", "testChefEnvVal");
        return expectedJson.toString();
    }

    @Test
    public void combineStrings_shouldConcatenateTwoParamStrings_andSetThemInSvcContext() {
        // GIVEN
        Map<String, String> params = ImmutableMap
                .of("dgContext", "contextValue", "String1", "paramString1", "String2", "paramString2");
        SvcLogicContext svcLogicContext = new SvcLogicContext();

        // WHEN
        chefAdaptorFactory.create().combineStrings(params, svcLogicContext);

        // THEN
        assertThat(svcLogicContext.getAttribute("contextValue")).isEqualTo("paramString1paramString2");
    }

    @Test
    public void trigger_shouldTriggerTargetEndpoint_andUpdateSvclogicContext() {
        // GIVEN
        Map<String, String> params = ImmutableMap.of(IP_PARAM, ENDPOINT_IP);
        SvcLogicContext svcLogicContext = new SvcLogicContext();
        given(chefApiClientFactory.create(ENDPOINT_IP, "").get(""))
                .willReturn(ChefResponse.create(HttpStatus.SC_OK, EXPECTED_RESPONSE_MSG));

        // WHEN
        chefAdaptorFactory.create().trigger(params, svcLogicContext);

        // THEN
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(CHEF_AGENT_CODE_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_OK));
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_OK));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(EXPECTED_RESPONSE_MSG);
    }

    @Test
    public void trigger_shouldUpdateSvcLogicContext_withFailStatusAndMsg_whenExceptionOccurs() {
        // GIVEN
        Map<String, String> params = ImmutableMap.of(IP_PARAM, ENDPOINT_IP);
        SvcLogicContext svcLogicContext = new SvcLogicContext();
        given(chefApiClientFactory.create(ENDPOINT_IP, "")).willThrow(new RuntimeException());

        // WHEN
        chefAdaptorFactory.create().trigger(params, svcLogicContext);

        // THEN
        assertThat(svcLogicContext.getAttribute(CHEF_AGENT_CODE_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(svcLogicContext.getAttribute(CHEF_AGENT_MESSAGE_KEY)).isEqualTo(new RuntimeException().toString());
    }

    @Test
    public void chefInfo_shouldUpdateSvcLogicContext_withFailStatusAndMsg_andThrowException_whenUsernameParamIsMissing() {
        Map<String, String> params = ImmutableMap.of(
                "serverAddress", "http://chefAddress",
                "organizations", "onap");
        checkIfInputParamsAreValidated(params);
    }

    @Test
    public void chefInfo_shouldUpdateSvcLogicContext_withFailStatusAndMsg_andThrowException_whenServerAddressParamIsMissing() {
        Map<String, String> params = ImmutableMap.of(
                "username", "TestUsername",
                "organizations", "onap");
        checkIfInputParamsAreValidated(params);
    }

    @Test
    public void chefInfo_shouldUpdateSvcLogicContext_withFailStatusAndMsg_andThrowException_whenOrganizationsParamIsMissing() {
        Map<String, String> params = ImmutableMap.of(
                "username", "TestUsername",
                "serverAddress", "http://chefAddress");
        checkIfInputParamsAreValidated(params);
    }

    private void checkIfInputParamsAreValidated(Map<String, String> params) {
        // GIVEN
        String expectedErrorMsg = "Missing mandatory param(s) such as username, serverAddress, organizations";
        SvcLogicContext svcLogicContext = new SvcLogicContext();

        // WHEN// THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().chefGet(params, svcLogicContext))
                .withMessage("Chef Adaptor error:"
                             + expectedErrorMsg);

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute("chefServerResult.code")).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute("chefServerResult.message")).isEqualTo(expectedErrorMsg);
    }

}
