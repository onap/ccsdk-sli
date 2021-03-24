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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ChefAdaptorImplJobPusherTest {

    private static final String CLIENT_PRIVATE_KEY_PATH = "/opt/onap/appc/chef/localhost/onap/testclient.pem";
    private static final String RESULT_CODE_ATTR_KEY = "chefServerResult.code";
    private static final String RESULT_MESSAGE_ATTR_KEY = "chefServerResult.message";
    private static final String EXPECTED_RESPONSE_MSG = "jobs/{666}/";

    private static final String USERNAME = "testclient";
    private static final String SERVER_ADDRESS = "localhost";
    private static final String ORGANIZATIONS = "onap";
    private static final String ACTION_PARAM = "/pushy/jobs";
    private static final String REQUEST_BODY_DATA = "requestBodyData";
    private static final String JOB_ID = "jobID";

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

    @Test
    public void pushJob_shouldSuccessfullyMakePostCall_andUpdateSvcLogicContext_whenReturnedStatusIsDifferentThan_201()
            throws SvcLogicException {
        assertSuccessfulPostCallForStatus(HttpStatus.SC_OK);
        assertThat(svcLogicContext.getAttribute(JOB_ID)).isBlank();
    }

    @Test
    public void pushJob_shouldSuccessfullyMakePostCall_andUpdateSvcLogicContext_withReturnedStatusIs_201()
            throws SvcLogicException {
        assertSuccessfulPostCallForStatus(HttpStatus.SC_CREATED);
        assertThat(svcLogicContext.getAttribute(JOB_ID)).isEqualTo("666");
    }

    @SuppressWarnings("unchecked")
    public void assertSuccessfulPostCallForStatus(int expectedHttpStatus) throws SvcLogicException {
        // GIVEN
        Map<String, String> params = givenInputParams(
                immutableEntry("chefAction", ACTION_PARAM),
                immutableEntry("pushRequest", REQUEST_BODY_DATA));
        given(chefApiClientFactory.create("https://localhost/organizations/onap", ORGANIZATIONS, USERNAME,
                CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.post(ACTION_PARAM, REQUEST_BODY_DATA))
                .willReturn(ChefResponse.create(expectedHttpStatus, EXPECTED_RESPONSE_MSG));

        // WHEN
        chefAdaptorFactory.create().pushJob(params, svcLogicContext);

        // THEN
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(expectedHttpStatus));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(EXPECTED_RESPONSE_MSG);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void pushJob_shouldHandleAllOccurringExceptions_duringMethodExecution() {
        // GIVEN
        Map<String, String> params = givenInputParams();
        String expectedErrorMessage = "Something went wrong";
        given(chefApiClientFactory.create("https://localhost/organizations/onap", ORGANIZATIONS, USERNAME,
                CLIENT_PRIVATE_KEY_PATH)).willThrow(new NullPointerException(expectedErrorMessage));

        // WHEN // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().pushJob(params, svcLogicContext))
                .withMessage("Chef Adaptor error:" + expectedErrorMessage);

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedErrorMessage);
        assertThat(svcLogicContext.getAttribute(JOB_ID)).isBlank();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void checkPushJob_shouldSetFailStatusAndMsgInContext_andThrowException_whenRetryTimesParamIsMissing() {
        // GIVEN
        Map<String, String> params = givenInputParams(
                immutableEntry("retryInterval", "1"),
                immutableEntry("jobid", "666"));

        // WHEN // THEN
        assertIfInputParamsAreValidated(params);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void checkPushJob_shouldSetFailStatusAndMsgInContext_andThrowException_whenRetryIntervalParamIsMissing() {
        // GIVEN
        Map<String, String> params = givenInputParams(
                immutableEntry("retryTimes", "4"),
                immutableEntry("jobid", "666"));

        // WHEN // THEN
        assertIfInputParamsAreValidated(params);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void checkPushJob_shouldSetFailStatusAndMsgInContext_andThrowException_whenJobIdParamIsMissing() {
        // GIVEN
        Map<String, String> params = givenInputParams(
                immutableEntry("retryTimes", "4"),
                immutableEntry("retryInterval", "1"));
        assertIfInputParamsAreValidated(params);
    }

    public void assertIfInputParamsAreValidated(Map<String, String> params) {
        // WHEN // THEN
        assertThatExceptionOfType(SvcLogicException.class)
                .isThrownBy(() -> chefAdaptorFactory.create().checkPushJob(params, svcLogicContext))
                .withMessage("Chef Adaptor error:" + "Missing Mandatory param(s) retryTimes , retryInterval ");

        assertFalse(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo("Missing Mandatory param(s) retryTimes , retryInterval ");
    }

    @Test
    public void checkPushJob_shouldCheckJobStatusOnlyOnce_withoutAdditionalRetries_whenFirstReturnedJobStatusIs_Complete()
            throws SvcLogicException {
        String expectedHttpStatus = Integer.toString(HttpStatus.SC_OK);
        //String expectedMessage = "{status:complete}";
        String expectedMessage = "{\"nodes\":{\"succeeded\":[\"NODE1.atttest.com\"]},\"id\":\"26d\",\"command\":\"chef-client\",\"status\":\"complete\"} ";
        assertCheckJobStatusFor(
                expectedHttpStatus,
                expectedMessage,
                ChefResponse.create(HttpStatus.SC_OK, expectedMessage),
                ChefResponse.create(HttpStatus.SC_OK, "{status:running}"));
    }

    @Test
    public void checkPushJob_withFailedNode_whenFirstReturnedJobStatusIs_Complete()
            throws SvcLogicException {
        String expectedHttpStatus = "401";
        String message = "{\"nodes\":{\"failed\":[\"NODE1.atttest.com\"]},\"id\":\"26d\",\"command\":\"chef-client\",\"status\":\"complete\"} ";
        String expectedMessage = "PushJob Status Complete but check failed nodes in the message :" + message;

        assertCheckJobStatusFor(
                expectedHttpStatus,
                expectedMessage,
                ChefResponse.create(HttpStatus.SC_OK, message));
    }

    @Test
    public void checkPushJob_shouldCheckJobStatusExpectedNumberOf_ThreeRetryTimes_whenEachReturnedStatusIs_Running()
            throws SvcLogicException {
        String expectedHttpStatus = Integer.toString(HttpStatus.SC_ACCEPTED);
        String expectedMessage = "chef client runtime out";

        assertCheckJobStatusFor(
                expectedHttpStatus,
                expectedMessage,
                ChefResponse.create(HttpStatus.SC_OK, "{status:running}"),
                ChefResponse.create(HttpStatus.SC_OK, "{status:running}"),
                ChefResponse.create(HttpStatus.SC_OK, "{status:running}"));
    }

    @Test
    public void checkPushJob_shouldCheckJobStatusOnlyOnce_withoutAdditionalRetries_whenFirstReturnedJobStatusIsNot_Running()
            throws SvcLogicException {

        String expectedHttpStatus = Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        String expectedMessage = "{status:unexpectedStatus}";

        assertCheckJobStatusFor(
                expectedHttpStatus,
                expectedMessage,
                ChefResponse.create(HttpStatus.SC_OK, "{status:unexpectedStatus}"),
                ChefResponse.create(HttpStatus.SC_OK, "{status:running}"));
    }

    @SuppressWarnings("unchecked")
    public void assertCheckJobStatusFor(String expectedHttpStatus, String expectedMessage, ChefResponse firstResponse,
                                        ChefResponse... nextResponses) throws SvcLogicException {

        // GIVEN
        Map<String, String> params = givenInputParams(
                immutableEntry("jobid", "666"),
                immutableEntry("retryTimes", "3"),
                immutableEntry("retryInterval", "1"));
        given(chefApiClientFactory.create("https://localhost/organizations/onap", ORGANIZATIONS, USERNAME,
                CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.get(ACTION_PARAM + "/" + params.get("jobid")))
                .willReturn(firstResponse, nextResponses);

        // WHEN
        chefAdaptorFactory.create().checkPushJob(params, svcLogicContext);

        // THEN
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(expectedHttpStatus);
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedMessage);
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
