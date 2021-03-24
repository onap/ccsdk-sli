/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.chef.ChefAdaptor;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.ChefApiClientFactory;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefApiClient;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ChefAdaptorImplHttpMethodTest {

    private static final String CLIENT_PRIVATE_KEY_PATH = "/opt/onap/appc/chef/localhost/onap/testclient.pem";
    private static final String RESULT_CODE_ATTR_KEY = "chefServerResult.code";
    private static final String RESULT_MESSAGE_ATTR_KEY = "chefServerResult.message";
    private static final String EXPECTED_RESPONSE_MSG = "chefResponseMessage";
    private static final String ACTION_PARAM = "action";
    private static final String REQUEST_BODY_DATA = "requestBodyData";
    private static final Map<String, String> PARAMS = ImmutableMap
            .of("username", "testclient",
                    "serverAddress", "localhost",
                    "organizations", "onap",
                    "chefAction", ACTION_PARAM,
                    "chefRequestBody", REQUEST_BODY_DATA);
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
    public void chefGet_shouldExecuteHttpClient_andSetChefResponseInContext_whenPrivateKeyFileExists() {
        assertSuccessfulChefHttpCallFor(() -> chefApiClient.get(ACTION_PARAM), this :: chefGet);
    }

    @Test
    public void chefGet_shouldNotExecuteHttpClient_andSetErrorResponseInContext_whenPrivateKeyFileDoesNotExist() {
        assertNoChefCallOccurFor(this :: chefGet);
    }

    @Test
    public void chefDelete_shouldExecuteHttpClient_andSetChefResponseInContext_whenPrivateKeyFileExists() {
        assertSuccessfulChefHttpCallFor(() -> chefApiClient.delete(ACTION_PARAM), this :: chefDelete);
    }

    @Test
    public void chefDelete_shouldNotExecuteHttpClient_andSetErrorResponseInContext_whenPrivateKeyFileDoesNotExist() {
        assertNoChefCallOccurFor(this :: chefDelete);
    }

    @Test
    public void chefPut_shouldExecuteHttpClient_andSetChefResponseInContext_whenPrivateKeyFileExists() {
        assertSuccessfulChefHttpCallFor(() -> chefApiClient.put(ACTION_PARAM, REQUEST_BODY_DATA), this :: chefPut);
    }

    @Test
    public void chefPut_shouldNotExecuteHttpClient_andSetErrorResponseInContext_whenPrivateKeyFileDoesNotExist() {
        assertNoChefCallOccurFor(this :: chefPut);
    }

    @Test
    public void chefPost_shouldExecuteHttpClient_andSetChefResponseInContext_whenPrivateKeyFileExists() {
        assertSuccessfulChefHttpCallFor(() -> chefApiClient.post(ACTION_PARAM, REQUEST_BODY_DATA), this :: chefPost);
    }

    @Test
    public void chefPost_shouldNotExecuteHttpClient_andSetErrorResponseInContext_whenPrivateKeyFileDoesNotExist() {
        assertNoChefCallOccurFor(this :: chefPost);
    }

    public void assertSuccessfulChefHttpCallFor(Supplier<ChefResponse> responseSupplier,
                                                Consumer<ChefAdaptor> chefAdaptorCall) {
        // GIVEN
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);
        given(chefApiClientFactory.create("https://localhost/organizations/onap",
                "onap",
                "testclient",
                CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(responseSupplier.get()).willReturn(ChefResponse.create(HttpStatus.SC_OK, EXPECTED_RESPONSE_MSG));

        // WHEN
        chefAdaptorCall.accept(chefAdaptorFactory.create());

        // THEN
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_OK));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(EXPECTED_RESPONSE_MSG);
    }

    public void assertNoChefCallOccurFor(Consumer<ChefAdaptor> chefAdaptorCall) {
        // GIVEN
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(false);

        // WHEN
        chefAdaptorCall.accept(chefAdaptorFactory.create());

        // THEN
        verifyZeroInteractions(chefApiClient);
        assertTrue(svcLogicContext.isSuccess());
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY))
                .isEqualTo(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(
                "Cannot find the private key in the APPC file system, please load the private key to "
                + CLIENT_PRIVATE_KEY_PATH);
    }

    public void chefGet(ChefAdaptor chefAdaptor) {
        try {
            chefAdaptor.chefGet(PARAMS, svcLogicContext);
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
    }

    public void chefDelete(ChefAdaptor chefAdaptor) {
        try {
            chefAdaptor.chefDelete(PARAMS, svcLogicContext);
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
    }

    public void chefPost(ChefAdaptor chefAdaptor) {
        try {
            chefAdaptor.chefPost(PARAMS, svcLogicContext);
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
    }

    public void chefPut(ChefAdaptor chefAdaptor) {
        try {
            chefAdaptor.chefPut(PARAMS, svcLogicContext);
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
    }

}