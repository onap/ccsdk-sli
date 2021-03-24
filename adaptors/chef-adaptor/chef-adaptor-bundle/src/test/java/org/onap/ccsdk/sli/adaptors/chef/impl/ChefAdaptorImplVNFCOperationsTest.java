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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.ChefApiClientFactory;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefApiClient;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static com.google.common.collect.Maps.immutableEntry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ChefAdaptorImplVNFCOperationsTest {

    private static final String CHEF_END_POINT = "https://localhost/organizations/onap";
    private static final String USERNAME = "testclient";
    private static final String ORGANIZATIONS = "onap";
    private static final String SERVER_ADDRESS = "localhost";
    private static final String CLIENT_PRIVATE_KEY_PATH = "/opt/onap/appc/chef/localhost/onap/testclient.pem";
    private static final String RESULT_CODE_ATTR_KEY = "chefServerResult.code";
    private static final String RESULT_MESSAGE_ATTR_KEY = "chefServerResult.message";
    private static final String FAILURE_STATUS = "failure";
    private static final String SUCCESS_STATUS = "success";
    private static final String CHEF_ADAPTOR_ERROR_PREFIX = "Chef Adaptor error:";
    private static final String ENV_PARAM_KEY = "Environment";
    private static final String ENV_JSON_VALUE = "{name:envName}";

    @Mock
    private org.onap.ccsdk.sli.adaptors.chef.impl.PrivateKeyChecker privateKeyChecker;

    @Mock
    private ChefApiClientFactory chefApiClientFactory;

    @Mock
    private ChefApiClient chefApiClient;

    @InjectMocks
    private org.onap.ccsdk.sli.adaptors.chef.impl.ChefAdaptorFactory chefAdaptorFactory;
    private SvcLogicContext svcLogicContext;

    @Before
    public void setUp() {
        svcLogicContext = new SvcLogicContext();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcEnvironment_shouldSkipEnvironmentCreation_whenEnvParamIsEmpty() throws SvcLogicException {
        // GIVEN
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, ""));

        // WHEN
        chefAdaptorFactory.create().vnfcEnvironment(params, svcLogicContext);

        // THEN
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_OK));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo("Skip Environment block ");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcEnvironment_shouldCreateNewEnvironment_forEnvParam_whenRequestedEnvDoesNotExist()
            throws SvcLogicException {
        // GIVEN
        String expectedErrorMessage = "New Environment Created";
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, ENV_JSON_VALUE));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);
        given(chefApiClientFactory.create(CHEF_END_POINT, ORGANIZATIONS, USERNAME,
                CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.put("/environments/" + "envName", ENV_JSON_VALUE))
                .willReturn(ChefResponse.create(HttpStatus.SC_NOT_FOUND, ""));
        given(chefApiClient.post("/environments", ENV_JSON_VALUE))
                .willReturn(ChefResponse.create(HttpStatus.SC_CREATED, expectedErrorMessage));

        // WHEN
        chefAdaptorFactory.create().vnfcEnvironment(params, svcLogicContext);

        // THEN
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_CREATED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedErrorMessage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcEnvironment_shouldNotAttemptEnvCreation_andThrowException_whenPrivateKeyCheckFails() {
        // GIVEN
        String expectedErrorMsg = "Cannot find the private key in the APPC file system, please load the private key to ";
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, ENV_JSON_VALUE));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(false);

        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().vnfcEnvironment(params, svcLogicContext))
                .withMessage(CHEF_ADAPTOR_ERROR_PREFIX + expectedErrorMsg + CLIENT_PRIVATE_KEY_PATH);

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedErrorMsg + CLIENT_PRIVATE_KEY_PATH);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcEnvironment_shouldNotAttemptEnvCreation_andHandleJSONException_whenJSONParamsAreMalformed() {
        // GIVEN
        String expectedErrorMessage = "Error posting request due to invalid JSON block: ";
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, "MALFORMED_JSON"));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);

        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().vnfcEnvironment(params, svcLogicContext))
                .withMessageStartingWith(CHEF_ADAPTOR_ERROR_PREFIX + expectedErrorMessage);

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).startsWith(expectedErrorMessage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcEnvironment_shouldNotAttemptEnvCreation_andHandleException_whenExceptionOccursDuringExecution() {
        // GIVEN
        String expectedErrorMessage = "Error posting request: ";
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, ENV_JSON_VALUE));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);
        given(chefApiClientFactory.create(CHEF_END_POINT, ORGANIZATIONS, USERNAME,
                CLIENT_PRIVATE_KEY_PATH)).willThrow(new NullPointerException("Null value encountered"));

        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().vnfcEnvironment(params, svcLogicContext))
                .withMessage(CHEF_ADAPTOR_ERROR_PREFIX + expectedErrorMessage + "vnfcEnvironmentNull value encountered");

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).startsWith(expectedErrorMessage);
    }

    @Test
    public void vnfcNodeObjects_shouldUpdateNodeObjects_andSetCodeAndMessageFromLastSuccessfulResponseInSvcLogicContext()
            throws SvcLogicException {
        // GIVEN
        ChefResponse firstNodeResponse = ChefResponse.create(HttpStatus.SC_OK, "firstMessage");
        ChefResponse secondNodeResponse = ChefResponse.create(HttpStatus.SC_OK, "secondMessage");
        int expectedHttpStatus = HttpStatus.SC_OK;
        String expectedMessage = "secondMessage";

        assertNodeObjectsAreUpdatedFor(firstNodeResponse, secondNodeResponse, expectedHttpStatus, expectedMessage);
    }

    @Test
    public void vnfcNodeObjects_shouldStopProcessingNodeObjectUpdates_whenFirstReturnedResponseIsOtherThan_200()
            throws SvcLogicException {
        ChefResponse firstNodeResponse = ChefResponse.create(HttpStatus.SC_ACCEPTED, "firstMessage");
        ChefResponse secondNodeResponse = ChefResponse.create(HttpStatus.SC_OK, "secondMessage");
        int expectedHttpStatus = HttpStatus.SC_ACCEPTED;
        String expectedMessage = "firstMessage";

        assertNodeObjectsAreUpdatedFor(firstNodeResponse, secondNodeResponse, expectedHttpStatus, expectedMessage);
    }

    @SuppressWarnings("unchecked")
    public void assertNodeObjectsAreUpdatedFor(ChefResponse firstNodeResponse, ChefResponse secondNodeResponse,
                                               int expectedHttpStatus, String expectedMessage) throws SvcLogicException {
        // GIVEN
        Map<String, String> params = givenInputParams(
                immutableEntry("NodeList", "[\"test1.vnf_b.onap.com\", \"test2.vnf_b.onap.com\"]"),
                immutableEntry("Node", "{name:nodeName}"));

        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);
        given(chefApiClientFactory.create(CHEF_END_POINT, ORGANIZATIONS, USERNAME,
                CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.put("/nodes/" + "test1.vnf_b.onap.com", "{\"name\":\"test1.vnf_b.onap.com\"}"))
                .willReturn(firstNodeResponse);
        given(chefApiClient.put("/nodes/" + "test2.vnf_b.onap.com", "{\"name\":\"test2.vnf_b.onap.com\"}"))
                .willReturn(secondNodeResponse);

        // WHEN
        chefAdaptorFactory.create().vnfcNodeobjects(params, svcLogicContext);

        // THEN
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(expectedHttpStatus));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedMessage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcNodeObjects_shouldThrowSvcLogicException_whenNodeListParamIsEmpty() {
        Map<String, String> params = givenInputParams(
                immutableEntry("NodeList", ""),
                immutableEntry("Node", "{name:nodeName}"));
        checkMissingParamsAreValidated(params);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcNodeObjects_shouldThrowSvcLogicException_whenNodeParamIsEmpty() {
        Map<String, String> params = givenInputParams(
                immutableEntry("NodeList", "[\"test1.vnf_b.onap.com\", \"test2.vnf_b.onap.com\"]"),
                immutableEntry("Node", ""));
        checkMissingParamsAreValidated(params);
    }

    public void checkMissingParamsAreValidated(Map<String, String> params) {
        // GIVEN
        String expectedErrorMsg = "vnfcNodeobjectsMissing Mandatory param(s) Node , NodeList ";

        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().vnfcNodeobjects(params, svcLogicContext))
                .withMessage(CHEF_ADAPTOR_ERROR_PREFIX + "Error posting request: " + expectedErrorMsg);

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo("Error posting request: " + expectedErrorMsg);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcNodeObjects_shouldNotUpdateNodes_andHandleJSONException_whenJSONParamsAreMalformed() {
        // GIVEN
        Map<String, String> params = givenInputParams(
                immutableEntry("NodeList", "[\"test1.vnf_b.onap.com\", \"test2.vnf_b.onap.com\"]"),
                immutableEntry("Node", "MALFORMED_JSON"));
        String expectedErrorMessage = "Error posting request due to invalid JSON block: ";
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);

        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().vnfcNodeobjects(params, svcLogicContext))
                .withMessageStartingWith(CHEF_ADAPTOR_ERROR_PREFIX + expectedErrorMessage);

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).startsWith(expectedErrorMessage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcPushJob_shouldUpdateSvcContextWithJobId_whenPushJobWasSuccessfullyCreatedWithCallbackUrl()
            throws SvcLogicException {
        Map<String, String> params = givenInputParams(
                immutableEntry("NodeList", "[\"test1.vnf_b.onap.com\", \"test2.vnf_b.onap.com\"]"),
                immutableEntry("CallbackCapable", "true"),
                immutableEntry("RequestId", "666"),
                immutableEntry("CallbackUrl", "someURLForCallback"));
        int expectedResponseStatus = HttpStatus.SC_CREATED;
        String expectedResponseMessage = "jobs:666-9";

        assertVnfcPushJobExecutionFor(params, buildJsonRequestWithCallback(), expectedResponseStatus, expectedResponseMessage);
        assertThat(svcLogicContext.getAttribute("jobID")).isEqualTo("666");
    }

    private String buildJsonRequestWithCallback() {
        return "{" + "\"command\": \"chef-client\"," + "\"run_timeout\": 300," + "\"nodes\":"
               + "[\"test1.vnf_b.onap.com\", \"test2.vnf_b.onap.com\"]" + "," + "\"env\": {\"RequestId\": \"" + "666"
               + "\", \"CallbackUrl\": \""
               + "someURLForCallback" + "\"}," + "\"capture_output\": true" + "}";
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcPushJob_shouldUpdateSvcContextWithJobId_whenPushJobWasSuccessfullyCreatedWithoutCallbackUrl()
            throws SvcLogicException {
        Map<String, String> params = givenInputParams(
                immutableEntry("NodeList", "[\"test1.vnf_b.onap.com\", \"test2.vnf_b.onap.com\"]"),
                immutableEntry("RequestId", "666"));
        int expectedResponseStatus = HttpStatus.SC_OK;
        String expectedResponseMessage = "jobs:666-9";

        assertVnfcPushJobExecutionFor(params, buildJsonRequestWithoutCallback(), expectedResponseStatus, expectedResponseMessage);
        assertThat(svcLogicContext.getAttribute("jobID")).isBlank();
    }

    private String buildJsonRequestWithoutCallback() {
        return "{" + "\"command\": \"chef-client\"," + "\"run_timeout\": 300," + "\"nodes\":"
               + "[\"test1.vnf_b.onap.com\", \"test2.vnf_b.onap.com\"]" + "," + "\"env\": {}," + "\"capture_output\": true"
               + "}";
    }

    public void assertVnfcPushJobExecutionFor(Map<String, String> params, String pushRequestWithCallback,
                                              int expectedResponseStatus, String expectedResponseMessage) throws SvcLogicException {
        // GIVEN
        given(chefApiClientFactory.create(CHEF_END_POINT, ORGANIZATIONS, USERNAME,
                CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.post("/pushy/jobs", pushRequestWithCallback))
                .willReturn(ChefResponse.create(expectedResponseStatus, expectedResponseMessage));

        // WHEN
        chefAdaptorFactory.create().vnfcPushJob(params, svcLogicContext);

        // THEN
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(expectedResponseStatus));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedResponseMessage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void vnfcPushJob_shouldNotPushJob_andThrowException_whenNodeListParamIsEmpty() {
        // GIVEN
        String expectedErrorMessage = "Error posting request: vnfcPushJobMissing Mandatory param(s)  NodeList ";
        Map<String, String> params = givenInputParams();
        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().vnfcPushJob(params, svcLogicContext))
                .withMessageStartingWith(CHEF_ADAPTOR_ERROR_PREFIX + expectedErrorMessage);

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedErrorMessage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchResults_shouldNotFetchResults_andThrowException_whenNodeListParamIsEmpty() {
        // GIVEN
        String expectedErrorMessage = "Error posting request: fetchResultsMissing Mandatory param(s)  NodeList ";
        Map<String, String> params = givenInputParams();
        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().fetchResults(params, svcLogicContext))
                .withMessageStartingWith(CHEF_ADAPTOR_ERROR_PREFIX + expectedErrorMessage);

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedErrorMessage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchResults_shouldNotFetchResults_andThrowException_whenPrivateKeyCheckFails() {
        // GIVEN
        Map<String, String> params = givenInputParams(
                immutableEntry("NodeList", "[\"test1.vnf_b.onap.com\", \"test2.vnf_b.onap.com\"]"));
        String expectedErrorMessage =
                "Error posting request: fetchResults"
                + CHEF_ADAPTOR_ERROR_PREFIX
                + "Cannot find the private key in the APPC file system, please load the private key to "
                + CLIENT_PRIVATE_KEY_PATH;
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(false);

        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().fetchResults(params, svcLogicContext))
                .withMessage(CHEF_ADAPTOR_ERROR_PREFIX + expectedErrorMessage);

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedErrorMessage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchResults_shouldUpdateSvcLogicContextWithJsonResponse_fromSuccessfulChefServerCall()
            throws SvcLogicException {
        // GIVEN
        String json = "{normal:{PushJobOutput : \"ssh start/running, process 1090\"}}";
        Map<String, String> params = givenInputParams(
                immutableEntry("NodeList", "[\"test1.vnf_b.onap.com\"]"));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);
        given(chefApiClientFactory.create(CHEF_END_POINT, ORGANIZATIONS, USERNAME,
                CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.get("/nodes/" + "test1.vnf_b.onap.com"))
                .willReturn(ChefResponse.create(HttpStatus.SC_OK, json));

        // WHEN
        chefAdaptorFactory.create().fetchResults(params, svcLogicContext);

        // THEN
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_OK));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY))
                .isEqualTo("{\"test1.vnf_b.onap.com\":{\"PushJobOutput\":\"ssh start/running, process 1090\"}}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fetchResults_shouldUpdateSvcLogicContextWithFailedMessage_whenReturnedJSONMessageIsMissingAttribute()
            throws SvcLogicException {
        // GIVEN
        String json = "{normal:{invalidKey : \"ssh start/running, process 1090\"}}";
        Map<String, String> params = givenInputParams(
                immutableEntry("NodeList", "[\"test1.vnf_b.onap.com\"]"));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);
        given(chefApiClientFactory.create(CHEF_END_POINT, ORGANIZATIONS, USERNAME,
                CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.get("/nodes/" + "test1.vnf_b.onap.com"))
                .willReturn(ChefResponse.create(HttpStatus.SC_OK, json));

        // WHEN
        chefAdaptorFactory.create().fetchResults(params, svcLogicContext);

        // THEN
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo("Cannot find PushJobOutput");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> givenInputParams(Entry<String, String>... entries) {
        Builder<String, String> paramsBuilder = ImmutableMap.builder();
        paramsBuilder.put("username", USERNAME)
            .put("serverAddress", SERVER_ADDRESS)
            .put("organizations", ORGANIZATIONS);

        for (Entry<String, String> entry : entries) {
            paramsBuilder.put(entry);
        }
        return paramsBuilder.build();
    }

}
