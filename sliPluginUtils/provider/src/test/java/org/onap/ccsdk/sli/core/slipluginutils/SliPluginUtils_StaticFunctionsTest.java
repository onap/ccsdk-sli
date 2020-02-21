/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

public class SliPluginUtils_StaticFunctionsTest {
    private static final Logger LOG = LoggerFactory.getLogger(SliPluginUtils_StaticFunctionsTest.class);
    SliPluginUtils utils = new SliPluginUtils();
    private SvcLogicContext ctx;
    private HashMap<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        this.ctx = new SvcLogicContext();
        parameters = new HashMap<String, String>();
    }

    // TODO: javadoc
    @Test
    public final void testCtxGetBeginsWith() {
        ctx.setAttribute("service-data.oper-status.order-status", "InProgress");
        ctx.setAttribute("service-data.service-information.service-instance-id", "my-instance");
        ctx.setAttribute("service-data.service-information.service-type", "my-service");

        Map<String, String> entries = SliPluginUtils.ctxGetBeginsWith(ctx, "service-data.service-information");

        assertEquals("my-instance", entries.get("service-data.service-information.service-instance-id"));
        assertEquals("my-service", entries.get("service-data.service-information.service-type"));
        assertFalse(entries.containsKey("service-data.oper-status.order-status"));
    }

    // TODO: javadoc
    @Test
    public final void testCtxListRemove_index() throws SvcLogicException {
        LOG.trace("=== testCtxListRemove_index ===");
        ctx.setAttribute("service-data.vnf-l3[0].vnf-host-name", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[0].device-host-name", "device-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[1].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2].vnf-host-name", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3[2].device-host-name", "device-host-name_2");
        ctx.setAttribute("service-data.vnf-l3_length", "3");

        parameters.put("index", "1");
        parameters.put("list_pfx", "service-data.vnf-l3");

        utils.ctxListRemove(parameters, ctx);
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        assertEquals("2", ctx.getAttribute("service-data.vnf-l3_length"));
        assertEquals("vnf-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].vnf-host-name"));
        assertEquals("device-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].device-host-name"));
        assertEquals("vnf-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].vnf-host-name"));
        assertEquals("device-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].device-host-name"));
    }

    // TODO: javadoc
    @Test
    public final void textCtxListRemove_keyValue() throws SvcLogicException {
        LOG.trace("=== textCtxListRemove_keyValue ===");
        ctx.setAttribute("service-data.vnf-l3[0].vnf-host-name", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[0].device-host-name", "device-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[1].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2].vnf-host-name", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3[2].device-host-name", "device-host-name_2");
        // 2nd entry
        ctx.setAttribute("service-data.vnf-l3[3].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[3].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3_length", "4");

        parameters.put("list_pfx", "service-data.vnf-l3");
        parameters.put("key", "vnf-host-name");
        parameters.put("value", "vnf-host-name_1");

        utils.ctxListRemove(parameters, ctx);
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        assertEquals("2", ctx.getAttribute("service-data.vnf-l3_length"));
        assertEquals("vnf-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].vnf-host-name"));
        assertEquals("device-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].device-host-name"));
        assertEquals("vnf-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].vnf-host-name"));
        assertEquals("device-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].device-host-name"));
    }

    // TODO: javadoc
    @Test
    public final void textCtxListRemove_keyValue_nullkey() throws SvcLogicException {
        LOG.trace("=== textCtxListRemove_keyValue_nullkey ===");
        ctx.setAttribute("service-data.vnf-l3[0]", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1]", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2]", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3_length", "3");

        parameters.put("list_pfx", "service-data.vnf-l3");
        parameters.put("value", "vnf-host-name_1");

        utils.ctxListRemove(parameters, ctx);
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        assertEquals("2", ctx.getAttribute("service-data.vnf-l3_length"));
        assertEquals("vnf-host-name_0", ctx.getAttribute("service-data.vnf-l3[0]"));
        assertEquals("vnf-host-name_2", ctx.getAttribute("service-data.vnf-l3[1]"));
    }

    // TODO: javadoc
    @Test
    public final void textCtxListRemove_keyValueList() throws SvcLogicException {
        LOG.trace("=== textCtxListRemove_keyValueList ===");
        ctx.setAttribute("service-data.vnf-l3[0].vnf-host-name", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[0].device-host-name", "device-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[1].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2].vnf-host-name", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3[2].device-host-name", "device-host-name_2");
        // 2nd entry
        ctx.setAttribute("service-data.vnf-l3[3].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[3].device-host-name", "device-host-name_1");
        // entries with only 1 of 2 key-value pairs matching
        ctx.setAttribute("service-data.vnf-l3[4].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[4].device-host-name", "device-host-name_4");
        ctx.setAttribute("service-data.vnf-l3[5].vnf-host-name", "vnf-host-name_5");
        ctx.setAttribute("service-data.vnf-l3[5].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3_length", "6");

        parameters.put("list_pfx", "service-data.vnf-l3");
        parameters.put("keys_length", "2");
        parameters.put("keys[0].key", "vnf-host-name");
        parameters.put("keys[0].value", "vnf-host-name_1");
        parameters.put("keys[1].key", "device-host-name");
        parameters.put("keys[1].value", "device-host-name_1");

        utils.ctxListRemove(parameters, ctx);
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        assertEquals("4", ctx.getAttribute("service-data.vnf-l3_length"));
        assertEquals("vnf-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].vnf-host-name"));
        assertEquals("device-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].device-host-name"));
        assertEquals("vnf-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].vnf-host-name"));
        assertEquals("device-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].device-host-name"));
        assertEquals("vnf-host-name_1", ctx.getAttribute("service-data.vnf-l3[2].vnf-host-name"));
        assertEquals("device-host-name_4", ctx.getAttribute("service-data.vnf-l3[2].device-host-name"));
        assertEquals("vnf-host-name_5", ctx.getAttribute("service-data.vnf-l3[3].vnf-host-name"));
        assertEquals("device-host-name_1", ctx.getAttribute("service-data.vnf-l3[3].device-host-name"));
    }

    // TODO: javadoc
    @Test(expected = SvcLogicException.class)
    public final void testCtxListRemove_nullListLength() throws SvcLogicException {
        LOG.trace("=== testCtxListRemove_nullListLength ===");
        ctx.setAttribute("service-data.vnf-l3[0].vnf-host-name", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[0].device-host-name", "device-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[1].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2].vnf-host-name", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3[2].device-host-name", "device-host-name_2");

        parameters.put("index", "1");
        parameters.put("list_pfx", "service-data.vnf-l3");

        utils.ctxListRemove(parameters, ctx);
    }

    // TODO: javadoc
    @Test
    public final void testCtxPutAll() {
        HashMap<String, Object> entries = new HashMap<String, Object>();
        entries.put("service-data.oper-status.order-status", "InProgress");
        entries.put("service-data.service-information.service-instance-id", "my-instance");
        entries.put("service-data.request-information.order-number", 1234);
        entries.put("service-data.request-information.request-id", null);

        SliPluginUtils.ctxPutAll(ctx, entries);

        assertEquals("InProgress", ctx.getAttribute("service-data.oper-status.order-status"));
        assertEquals("my-instance", ctx.getAttribute("service-data.service-information.service-instance-id"));
        assertEquals("1234", ctx.getAttribute("service-data.request-information.order-number"));
        assertFalse(ctx.getAttributeKeySet().contains("service-data.request-information.request-id"));
    }

    // TODO: javadoc
    @Test
    public final void testCtxSetAttribute_LOG() {
        LOG.debug("=== testCtxSetAttribute_LOG ===");
        Integer i = new Integer(3);
        SliPluginUtils.ctxSetAttribute(ctx, "test", i, LOG, SliPluginUtils.LogLevel.TRACE);
    }

    @Test
    public void setTime() throws SvcLogicException {
        String outputPath = "output";
        parameters.put("outputPath", outputPath);
        SliPluginUtils.setTime(parameters, ctx);
        assertNotNull(ctx.getAttribute(outputPath));
    }

    @Test
    public void containsKey() throws Exception {
        ctx = new SvcLogicContext();
        parameters.put(SliStringUtils.INPUT_PARAM_KEY, "key_does_not_exist");
        String result = SliPluginUtils.containsKey(parameters, ctx);
        assertEquals(SliStringUtils.FALSE_CONSTANT, result);

        ctx.setAttribute("a", null);
        parameters.put(SliStringUtils.INPUT_PARAM_KEY, "a");
        result = SliPluginUtils.containsKey(parameters, ctx);
        assertEquals(SliStringUtils.FALSE_CONSTANT, result);

        ctx.setAttribute("a", "hellworld");
        parameters.put(SliStringUtils.INPUT_PARAM_KEY, "a");
        result = SliPluginUtils.containsKey(parameters, ctx);
        assertEquals(SliStringUtils.TRUE_CONSTANT, result);
    }

    @Test
    public void testGetAttributeValue() throws Exception {
        parameters.put("outputPath", "testPath");
        parameters.put("source", "testSource");
        SliPluginUtils.getAttributeValue(parameters, ctx);
        assertNull(ctx.getAttribute(parameters.get("outputPath")));
    }

    @Test
    public void testCtxListContains() throws Exception {
        parameters.put("list", "10_length");
        parameters.put("keyName", "testName");
        parameters.put("keyValue", "testValue");
        ctx.setAttribute("10_length", "10");
        assertEquals("false", SliPluginUtils.ctxListContains(parameters, ctx));

    }

    @Test(expected = SvcLogicException.class)
    public void testPrintContextForEmptyParameters() throws SvcLogicException {
        SliPluginUtils.printContext(parameters, ctx);
    }

    @Test(expected = SvcLogicException.class)
    public void testPrintContextForNullParameters() throws SvcLogicException {
        SliPluginUtils.printContext(null, ctx);
    }

    @Test
    public void testPrintContext() throws SvcLogicException {
        parameters.put("filename", "testFileName");
        SliPluginUtils.printContext(parameters, ctx);
    }

    @Test
    public void testWriteJsonObject() throws SvcLogicException {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", "testName");
        obj.addProperty("age", 27);
        obj.addProperty("salary", 600000);
        SvcLogicContext ctx = new SvcLogicContext();
        SliPluginUtils.writeJsonObject(obj, ctx, "root");
        assertEquals("testName", ctx.getAttribute("root.name"));
        assertEquals("27", ctx.getAttribute("root.age"));
        assertEquals("600000", ctx.getAttribute("root.salary"));
    }

    @Test
    public void testCtxKeyEmpty() {
        ctx.setAttribute("key", "");
        assertTrue(SliPluginUtils.ctxKeyEmpty(ctx, "key"));
    }

    @Test
    public void testGetArrayLength() {
        ctx.setAttribute("key_length", "test");
        Logger log = LoggerFactory.getLogger(getClass());
        SliPluginUtils.getArrayLength(ctx, "key", log, LogLevel.INFO, "invalid input");
    }

    @Test
    public void testSetPropertiesForRoot() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("root", "RootVal");
        parameters.put("valueRoot", "ValueRootVal");
        assertEquals(SvcLogicConstants.SUCCESS, SliPluginUtils.setPropertiesForRoot(parameters, ctx));
    }

    @Test
    public void testJsonStringToCtxToplevelArray() throws Exception {
        String path = "src/test/resources/ArrayMenu.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("input", content);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("outputPath", "testPath");
        parameters.put("isEscaped", "false");
        parameters.put("source", "input");

        SliPluginUtils.jsonStringToCtx(parameters, ctx);

        assertEquals("1000", ctx.getAttribute("testPath.[0].calories"));
        assertEquals("1", ctx.getAttribute("testPath.[0].id"));
        assertEquals("plain", ctx.getAttribute("testPath.[0].name"));
        assertEquals("pizza", ctx.getAttribute("testPath.[0].type"));
        assertEquals("true", ctx.getAttribute("testPath.[0].vegetarian"));
        assertEquals("2000", ctx.getAttribute("testPath.[1].calories"));
        assertEquals("2", ctx.getAttribute("testPath.[1].id"));
        assertEquals("Tuesday Special", ctx.getAttribute("testPath.[1].name"));
        assertEquals("1", ctx.getAttribute("testPath.[1].topping[0].id"));
        assertEquals("onion", ctx.getAttribute("testPath.[1].topping[0].name"));
        assertEquals("2", ctx.getAttribute("testPath.[1].topping[1].id"));
        assertEquals("pepperoni", ctx.getAttribute("testPath.[1].topping[1].name"));
        assertEquals("2", ctx.getAttribute("testPath.[1].topping_length"));
        assertEquals("pizza", ctx.getAttribute("testPath.[1].type"));
        assertEquals("false", ctx.getAttribute("testPath.[1].vegetarian"));
        assertEquals("1500", ctx.getAttribute("testPath.[2].calories"));
        assertEquals("3", ctx.getAttribute("testPath.[2].id"));
        assertEquals("House Special", ctx.getAttribute("testPath.[2].name"));
        assertEquals("3", ctx.getAttribute("testPath.[2].topping[0].id"));
        assertEquals("basil", ctx.getAttribute("testPath.[2].topping[0].name"));
        assertEquals("4", ctx.getAttribute("testPath.[2].topping[1].id"));
        assertEquals("fresh mozzarella", ctx.getAttribute("testPath.[2].topping[1].name"));
        assertEquals("5", ctx.getAttribute("testPath.[2].topping[2].id"));
        assertEquals("tomato", ctx.getAttribute("testPath.[2].topping[2].name"));
        assertEquals("3", ctx.getAttribute("testPath.[2].topping_length"));
        assertEquals("pizza", ctx.getAttribute("testPath.[2].type"));
        assertEquals("true", ctx.getAttribute("testPath.[2].vegetarian"));
        assertEquals("3", ctx.getAttribute("testPath._length"));
    }

    @Test
    public void testJsonStringToCtx() throws Exception {
        String path = "src/test/resources/ObjectMenu.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("input", content);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("outputPath", "testPath");
        parameters.put("isEscaped", "false");
        parameters.put("source", "input");

        SliPluginUtils.jsonStringToCtx(parameters, ctx);


        assertEquals("1000", ctx.getAttribute("testPath.menu[0].calories"));
        assertEquals("1", ctx.getAttribute("testPath.menu[0].id"));
        assertEquals("plain", ctx.getAttribute("testPath.menu[0].name"));
        assertEquals("pizza", ctx.getAttribute("testPath.menu[0].type"));
        assertEquals("true", ctx.getAttribute("testPath.menu[0].vegetarian"));
        assertEquals("2000", ctx.getAttribute("testPath.menu[1].calories"));
        assertEquals("2", ctx.getAttribute("testPath.menu[1].id"));
        assertEquals("Tuesday Special", ctx.getAttribute("testPath.menu[1].name"));
        assertEquals("1", ctx.getAttribute("testPath.menu[1].topping[0].id"));
        assertEquals("onion", ctx.getAttribute("testPath.menu[1].topping[0].name"));
        assertEquals("2", ctx.getAttribute("testPath.menu[1].topping[1].id"));
        assertEquals("pepperoni", ctx.getAttribute("testPath.menu[1].topping[1].name"));
        assertEquals("2", ctx.getAttribute("testPath.menu[1].topping_length"));
        assertEquals("pizza", ctx.getAttribute("testPath.menu[1].type"));
        assertEquals("false", ctx.getAttribute("testPath.menu[1].vegetarian"));
        assertEquals("1500", ctx.getAttribute("testPath.menu[2].calories"));
        assertEquals("3", ctx.getAttribute("testPath.menu[2].id"));
        assertEquals("House Special", ctx.getAttribute("testPath.menu[2].name"));
        assertEquals("3", ctx.getAttribute("testPath.menu[2].topping[0].id"));
        assertEquals("basil", ctx.getAttribute("testPath.menu[2].topping[0].name"));
        assertEquals("4", ctx.getAttribute("testPath.menu[2].topping[1].id"));
        assertEquals("fresh mozzarella", ctx.getAttribute("testPath.menu[2].topping[1].name"));
        assertEquals("5", ctx.getAttribute("testPath.menu[2].topping[2].id"));
        assertEquals("tomato", ctx.getAttribute("testPath.menu[2].topping[2].name"));
        assertEquals("3", ctx.getAttribute("testPath.menu[2].topping_length"));
        assertEquals("pizza", ctx.getAttribute("testPath.menu[2].type"));
        assertEquals("true", ctx.getAttribute("testPath.menu[2].vegetarian"));
        assertEquals("3", ctx.getAttribute("testPath.menu_length"));
    }

    @Test
    public void test2dJsonStringToCtx() throws Exception {
        String path = "src/test/resources/2dArray.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("input", content);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("outputPath", "testPath");
        parameters.put("isEscaped", "false");
        parameters.put("source", "input");

        SliPluginUtils.jsonStringToCtx(parameters, ctx);
        assertEquals("apple", ctx.getAttribute("testPath.[0][0]"));
        assertEquals("orange", ctx.getAttribute("testPath.[0][1]"));
        assertEquals("banana", ctx.getAttribute("testPath.[0][2]"));
        assertEquals("3", ctx.getAttribute("testPath.[0]_length"));
        assertEquals("squash", ctx.getAttribute("testPath.[1][0]"));
        assertEquals("broccoli", ctx.getAttribute("testPath.[1][1]"));
        assertEquals("cauliflower", ctx.getAttribute("testPath.[1][2]"));
        assertEquals("3", ctx.getAttribute("testPath.[1]_length"));
        assertEquals("2", ctx.getAttribute("testPath._length"));
    }

    @Test
    public void test3dJsonStringToCtx() throws Exception {
        String path = "src/test/resources/3dArray.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("input", content);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("outputPath", "testPath");
        parameters.put("isEscaped", "false");
        parameters.put("source", "input");

        SliPluginUtils.jsonStringToCtx(parameters, ctx);
        assertEquals("a", ctx.getAttribute("testPath.[0][0][0]"));
        assertEquals("b", ctx.getAttribute("testPath.[0][0][1]"));
        assertEquals("c", ctx.getAttribute("testPath.[0][0][2]"));
        assertEquals("3", ctx.getAttribute("testPath.[0][0]_length"));
        assertEquals("d", ctx.getAttribute("testPath.[0][1][0]"));
        assertEquals("e", ctx.getAttribute("testPath.[0][1][1]"));
        assertEquals("f", ctx.getAttribute("testPath.[0][1][2]"));
        assertEquals("3", ctx.getAttribute("testPath.[0][1]_length"));
        assertEquals("2", ctx.getAttribute("testPath.[0]_length"));
        assertEquals("x", ctx.getAttribute("testPath.[1][0][0]"));
        assertEquals("y", ctx.getAttribute("testPath.[1][0][1]"));
        assertEquals("z", ctx.getAttribute("testPath.[1][0][2]"));
        assertEquals("3", ctx.getAttribute("testPath.[1][0]_length"));
        assertEquals("1", ctx.getAttribute("testPath.[1]_length"));
        assertEquals("2", ctx.getAttribute("testPath._length"));
    }

    @Test
    public void testJsonWidgetStringToCtx() throws Exception {
        String path = "src/test/resources/Widget.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("input", content);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("outputPath", "testPath");
        parameters.put("isEscaped", "false");
        parameters.put("source", "input");

        SliPluginUtils.jsonStringToCtx(parameters, ctx);
        assertEquals("false", ctx.getAttribute("testPath.widget.debug"));
        assertEquals("center", ctx.getAttribute("testPath.widget.image.alignment"));
        assertEquals("150", ctx.getAttribute("testPath.widget.image.hOffset"));
        assertEquals("moon", ctx.getAttribute("testPath.widget.image.name"));
        assertEquals("images/moon.png", ctx.getAttribute("testPath.widget.image.src"));
        assertEquals("150", ctx.getAttribute("testPath.widget.image.vOffset"));
        assertEquals("center", ctx.getAttribute("testPath.widget.text.alignment"));
        assertEquals("Click Me", ctx.getAttribute("testPath.widget.text.data"));
        assertEquals("350", ctx.getAttribute("testPath.widget.text.hOffset"));
        assertEquals("text1", ctx.getAttribute("testPath.widget.text.name"));
        assertEquals("21", ctx.getAttribute("testPath.widget.text.size"));
        assertEquals("bold", ctx.getAttribute("testPath.widget.text.style"));
        assertEquals("200", ctx.getAttribute("testPath.widget.text.vOffset"));
        assertEquals("300", ctx.getAttribute("testPath.widget.window.height"));
        assertEquals("main_window", ctx.getAttribute("testPath.widget.window.name"));
        assertEquals("ONAP Widget", ctx.getAttribute("testPath.widget.window.title"));
        assertEquals("200", ctx.getAttribute("testPath.widget.window.width"));
    }

    @Test
    public void testUpdateJsonObjectString() throws Exception {
        String path = "src/test/resources/JsonObject.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("input", content);

        Map<String, String> parametersUpdateJson = new HashMap<String, String>();
        parametersUpdateJson.put("source", "input");
        parametersUpdateJson.put("outputPath", "newJsonString");

        // add key "ccc" and its value
        parametersUpdateJson.put("add.ccc", "abcxyz");

        // update keys and their values of "aaa" and "c.d"
        parametersUpdateJson.put("add.aaa", "4567");
        parametersUpdateJson.put("add.c.d", "defg");

        // delete key "bbb"
        parametersUpdateJson.put("delete.bbb", "");

        SliPluginUtils.updateJsonObjectString(parametersUpdateJson, ctx);

        Map<String, String> parametersJsonToCtx = new HashMap<String, String>();
        parametersJsonToCtx.put("source", "newJsonString");
        parametersJsonToCtx.put("outputPath", "testPath");
        parametersJsonToCtx.put("isEscaped", "false");

        SliPluginUtils.jsonStringToCtx(parametersJsonToCtx, ctx);

        assertEquals("abcxyz", ctx.getAttribute("testPath.ccc"));
        assertEquals("4567", ctx.getAttribute("testPath.aaa"));
        assertEquals("defg", ctx.getAttribute("testPath.c.d"));
        assertEquals(null, ctx.getAttribute("testPath.bbb"));
    }

    @Test
    public void testEmbeddedEscapedJsonJsonStringToCtx() throws Exception {
        String path = "src/test/resources/EmbeddedEscapedJson.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("input", content);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("outputPath", "testPath");
        parameters.put("isEscaped", "false");
        parameters.put("source", "input");

        SliPluginUtils.jsonStringToCtx(parameters, ctx);

        assertEquals("escapedJsonObject", ctx.getAttribute("testPath.input.parameters[0].name"));
        assertEquals("[{\"id\":\"0.2.0.0/16\"},{\"id\":\"ge04::/64\"}]",
                ctx.getAttribute("testPath.input.parameters[0].value"));
        assertEquals("Hello/World", ctx.getAttribute("testPath.input.parameters[1].value"));
        assertEquals("resourceName", ctx.getAttribute("testPath.input.parameters[2].name"));
        assertEquals("The\t\"Best\"\tName", ctx.getAttribute("testPath.input.parameters[2].value"));
        assertEquals("3", ctx.getAttribute("testPath.input.parameters_length"));


        // Break the embedded json object into properties
        parameters.put("outputPath", "testPath.input.parameters[0].value");
        parameters.put("source", "testPath.input.parameters[0].value");
        SliPluginUtils.jsonStringToCtx(parameters, ctx);

        assertEquals("0.2.0.0/16", ctx.getAttribute("testPath.input.parameters[0].value.[0].id"));
        assertEquals("ge04::/64", ctx.getAttribute("testPath.input.parameters[0].value.[1].id"));
        assertEquals("2", ctx.getAttribute("testPath.input.parameters[0].value._length"));
    }

    @Test
    public void testEscapedJsonStringToCtx() throws Exception {
        String path = "src/test/resources/EscapedJson.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("input", content);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("outputPath", "testPath");
        parameters.put("isEscaped", "true"); // set to true because the entire json content has been escaped
        parameters.put("source", "input");


        SliPluginUtils.jsonStringToCtx(parameters, ctx);
        assertEquals("false", ctx.getAttribute("testPath.widget.debug"));
        assertEquals("center", ctx.getAttribute("testPath.widget.image.alignment"));
        assertEquals("150", ctx.getAttribute("testPath.widget.image.hOffset"));
        assertEquals("moon", ctx.getAttribute("testPath.widget.image.name"));
        assertEquals("images/moon.png", ctx.getAttribute("testPath.widget.image.src"));
        assertEquals("150", ctx.getAttribute("testPath.widget.image.vOffset"));
        assertEquals("center", ctx.getAttribute("testPath.widget.text.alignment"));
        assertEquals("Click Me", ctx.getAttribute("testPath.widget.text.data"));
        assertEquals("350", ctx.getAttribute("testPath.widget.text.hOffset"));
        assertEquals("text1", ctx.getAttribute("testPath.widget.text.name"));
        assertEquals("21", ctx.getAttribute("testPath.widget.text.size"));
        assertEquals("bold", ctx.getAttribute("testPath.widget.text.style"));
        assertEquals("200", ctx.getAttribute("testPath.widget.text.vOffset"));
        assertEquals("300", ctx.getAttribute("testPath.widget.window.height"));
        assertEquals("main_window", ctx.getAttribute("testPath.widget.window.name"));
        assertEquals("ONAP Widget", ctx.getAttribute("testPath.widget.window.title"));
        assertEquals("200", ctx.getAttribute("testPath.widget.window.width"));
    }

}
