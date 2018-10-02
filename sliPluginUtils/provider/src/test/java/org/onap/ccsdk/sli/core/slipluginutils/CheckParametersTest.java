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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.gson.JsonObject;

public class CheckParametersTest {

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

    @Test(expected = SvcLogicException.class)
    public void testJsonStringToCtx() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("outputPath", "testPath");
        parameters.put("isEscaped", "true");
        parameters.put("source", "//{/name1/:value1/}//");
        SliPluginUtils.jsonStringToCtx(parameters, ctx);
    }

    @Test
    public void testGetAttributeValue() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("outputPath", "testPath");
        parameters.put("source", "testSource");
        SliPluginUtils.getAttributeValue(parameters, ctx);
        assertNull(ctx.getAttribute(parameters.get("outputPath")));
    }

    @Test
    public void testCtxListContains() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("list", "10_length");
        parameters.put("keyName", "testName");
        parameters.put("keyValue", "testValue");
        ctx.setAttribute("10_length", "10");
        assertEquals("false", SliPluginUtils.ctxListContains(parameters, ctx));

    }
    
    @Test(expected= SvcLogicException.class)
    public void testPrintContextForNullParameters() throws SvcLogicException
    {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> parameters = new HashMap<String, String>();
        SliPluginUtils.printContext(parameters, ctx);
    }
    
    @Test
    public void testPrintContext() throws SvcLogicException
    {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("filename","testFileName");
        SliPluginUtils.printContext(parameters, ctx);
    }
    
    @Test
    public void testWriteJsonObject() throws SvcLogicException
    {
        JsonObject obj=new JsonObject();
        obj.addProperty("name","testName");
        obj.addProperty("age",27);
        obj.addProperty("salary",600000);
        SvcLogicContext ctx = new SvcLogicContext();
        SliPluginUtils.writeJsonObject(obj, ctx,"root");
        assertEquals("testName", ctx.getAttribute("root.name"));
        assertEquals("27", ctx.getAttribute("root.age"));
        assertEquals("600000", ctx.getAttribute("root.salary"));
    }
    
    @Test
    public void testCtxKeyEmpty()
    {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("key", "");
        assertTrue(SliPluginUtils.ctxKeyEmpty(ctx, "key"));
    }
    
    @Test
    public void testGetArrayLength()
    {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("key_length", "test");
        Logger log = LoggerFactory.getLogger(getClass());
        SliPluginUtils.getArrayLength(ctx, "key", log , LogLevel.INFO, "invalid input");
    }
    
    @Test
    public void testSetPropertiesForRoot()
    {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> parameters= new HashMap<>();
        parameters.put("root","RootVal");
        parameters.put("valueRoot", "ValueRootVal");
        assertEquals("success",SliPluginUtils.setPropertiesForRoot(parameters,ctx));
    }
}
