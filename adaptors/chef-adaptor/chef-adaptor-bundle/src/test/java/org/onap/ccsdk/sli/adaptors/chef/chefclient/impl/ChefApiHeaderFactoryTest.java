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

package org.onap.ccsdk.sli.adaptors.chef.chefclient.impl;

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class ChefApiHeaderFactoryTest {

    private static final String ORGANIZATIONS_PATH = "onap";
    private static final String USER_ID = "testUser";
    private static final String REQUEST_PATH = "/test/path";
    private static final String EXPECTED_TIMESTAMP = "1970-01-15T06:56:07Z";
    private static final String EMPTY_BODY = "";

    @Mock
    private org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.FormattedTimestamp formattedTimestamp;

    @InjectMocks
    private org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.ChefApiHeaderFactory chefApiHeaderFactory;

    @Test
    public void create_shouldCreateProperChefHeaders_withHashedAuthorizationString() {
        // GIVEN
        given(formattedTimestamp.format(any(Date.class))).willReturn(EXPECTED_TIMESTAMP);
        String pemFilePath = getClass().getResource("/testclient.pem").getPath();

        // WHEN
        ImmutableMap<String, String> headers = chefApiHeaderFactory
                .create("GET", REQUEST_PATH, "", USER_ID, ORGANIZATIONS_PATH, pemFilePath);

        // THEN
        assertEquals(headers, createExpectedHeaders());
    }

    private ImmutableMap<String, String> createExpectedHeaders() {
        String hashedBody = org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.Utils.sha1AndBase64(EMPTY_BODY);
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder
                .put("Content-type", "application/json")
                .put("Accept", "application/json")
                .put("X-Ops-Timestamp", EXPECTED_TIMESTAMP)
                .put("X-Ops-UserId", USER_ID)
                .put("X-Chef-Version", "12.4.1")
                .put("X-Ops-Content-Hash", hashedBody)
                .put("X-Ops-Sign", "version=1.0")
                .put("X-Ops-Authorization-1", "i+HGCso703727yd2ZQWMZIIpGKgTzm41fA31LIExNxEf9mOUMcpesIHjH/Wr")
                .put("X-Ops-Authorization-2", "QEvsX/Gy1ay9KsUtqhy9GA6PB8UfDeMNoVUisqR4HQW+S6IOfvqBjW+2afzE")
                .put("X-Ops-Authorization-3", "RdRReB/TJIF3s6ZC8vNpbEdY9kHmwiDglhxmS8X2FS+ArSh/DK/i7MqBbjux")
                .put("X-Ops-Authorization-4", "49iiOlRVG7aTr/FA115hlBYP9CYCIQWKIBUOK3JyV9fXNdVqc9R0r1XdjxUl")
                .put("X-Ops-Authorization-5", "EDGw6tuE8YW8mH5wkgHCjKpXG3WjmWt2X6kUrdIu44qCBK2N3sZziSub2fJA")
                .put("X-Ops-Authorization-6", "hPBuOhjiYDZuFUqC99lCryM0Hf5RMw1uTlkYsBEZmA==");

        return builder.build();
    }

}