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
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class ChefAdaptorImplDataRetrieverTest {

    private static final String KEY_PARAM = "key";
    private static final String DG_CONTEXT_PARAM = "dgContext";
    private static final String ALL_CONFIG_PARAM = "allConfig";
    private static final String KEY_VALUE = "keyValue";
    private static final String DG_CONTEXT_VALUE = "contextValue";

    @Test
    public void retrieveData_shouldSetContextData_withExtractedJsonString() {
        // GIVEN
        Map<String, String> params = givenParamMapWithJson("{" + KEY_VALUE + ":testValue}");
        SvcLogicContext svcLogicContext = new SvcLogicContext();

        // WHEN
        new org.onap.ccsdk.sli.adaptors.chef.impl.ChefAdaptorFactory().create().retrieveData(params, svcLogicContext);

        // THEN
        Assertions.assertThat(svcLogicContext.getAttribute(DG_CONTEXT_VALUE)).isEqualTo("testValue");
    }

    @Test
    public void retrieveData_shouldSetContextData_withExtractedJsonObject() {
        // GIVEN
        Map<String, String> params = givenParamMapWithJson("{" + KEY_VALUE + ": {param : testValue} }");
        SvcLogicContext svcLogicContext = new SvcLogicContext();

        // WHEN
        new org.onap.ccsdk.sli.adaptors.chef.impl.ChefAdaptorFactory().create().retrieveData(params, svcLogicContext);

        // THEN
        Assertions.assertThat(svcLogicContext.getAttribute(DG_CONTEXT_VALUE)).isEqualTo("{\"param\":\"testValue\"}");
    }

    @Test
    public void retrieveData_shouldSetContextData_withExtractedJsonArray() {
        // GIVEN
        Map<String, String> params = givenParamMapWithJson("{" + KEY_VALUE + ": [val1, val2, val3] }");
        SvcLogicContext svcLogicContext = new SvcLogicContext();

        // WHEN
        new org.onap.ccsdk.sli.adaptors.chef.impl.ChefAdaptorFactory().create().retrieveData(params, svcLogicContext);

        // THEN
        Assertions.assertThat(svcLogicContext.getAttribute(DG_CONTEXT_VALUE)).isEqualTo("[\"val1\",\"val2\",\"val3\"]");
    }

    private Map<String, String> givenParamMapWithJson(String json) {
        return ImmutableMap
                .of(KEY_PARAM, KEY_VALUE,
                        DG_CONTEXT_PARAM, DG_CONTEXT_VALUE,
                        ALL_CONFIG_PARAM, json);
    }

}