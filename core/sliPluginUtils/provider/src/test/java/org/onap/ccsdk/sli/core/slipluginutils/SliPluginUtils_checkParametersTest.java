/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.ccsdk.sli.core.slipluginutils;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliPluginUtils_checkParametersTest {

    @Test
    public void nullRequiredParameters() throws Exception {
        Map<String, String> parametersMap = new HashMap<String, String>();
        String[] requiredParams = null;
        Logger Log = LoggerFactory.getLogger(SliPluginUtils.class);
        SliPluginUtils.checkParameters(parametersMap, requiredParams, Log);
    }

    @Test(expected = SvcLogicException.class)
    public void emptyParametersMap() throws Exception {
        Map<String, String> parametersMap = new HashMap<String, String>();
        String[] requiredParams = new String[] { "param1", "param2", "param3" };
        Logger Log = LoggerFactory.getLogger(SliPluginUtils.class);
        SliPluginUtils.checkParameters(parametersMap, requiredParams, Log);
    }

    @Test(expected = SvcLogicException.class)
    public void paramNotFound() throws Exception {
        Map<String, String> parametersMap = new HashMap<String, String>();
        parametersMap.put("tst", "me");
        String[] requiredParams = new String[] { "param1", "parm2", "param3" };
        Logger Log = LoggerFactory.getLogger(SliPluginUtils.class);
        SliPluginUtils.checkParameters(parametersMap, requiredParams, Log);
    }

    @Test
    public void testSunnyRequiredParameters() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("param1", "hello");
        ctx.setAttribute("param2", "world");
        ctx.setAttribute("param3", "!");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param1", "dog");
        parameters.put("param2", "cat");
        parameters.put("param3", "fish");

        SliPluginUtils.requiredParameters(parameters, ctx);
    }

    @Test
    public void testSunnyRequiredParametersWithPrefix() throws Exception {
        String prefixValue = "my.unique.path.";
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(prefixValue + "param1", "hello");
        ctx.setAttribute(prefixValue + "param2", "world");
        ctx.setAttribute(prefixValue + "param3", "!");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("prefix", prefixValue);
        parameters.put("param1", "dog");
        parameters.put("param2", "cat");
        parameters.put("param3", "fish");

        SliPluginUtils.requiredParameters(parameters, ctx);
    }

    @Test(expected = SvcLogicException.class)
    public void testRainyMissingRequiredParameters() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("param1", "hello");
        ctx.setAttribute("param3", "!");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param1", null);
        parameters.put("param2", null);
        parameters.put("param3", null);

        SliPluginUtils.requiredParameters(parameters, ctx);
    }

    @Test(expected = SvcLogicException.class)
    public void testEmptyRequiredParameters() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("param1", "hello");
        ctx.setAttribute("param3", "!");

        Map<String, String> parameters = new HashMap<String, String>();

        SliPluginUtils.requiredParameters(parameters, ctx);
    }
}
