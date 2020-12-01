/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.plugins.restapicall;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TestJsonParser {

    @Test
    public void test() throws SvcLogicException, IOException {
        String path = "src/test/resources/test.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Map<String, String> mm = JsonParser.convertToProperties(content);
        assertEquals("Server1", mm.get("equipment-data[0].equipment-id"));
        assertEquals("1600000", mm.get("equipment-data[0].max-server-speed"));
        assertEquals("2", mm.get("equipment-data[0].number-primary-servers"));
        assertEquals("4", mm.get("equipment-data[0].server-count"));
        assertEquals("Server1", mm.get("equipment-data[0].server-id"));
        assertEquals("Unknown", mm.get("equipment-data[0].server-model"));
        assertEquals("Test-Value", mm.get("equipment-data[0].test-node.test-inner-node"));
        assertEquals("1", mm.get("equipment-data_length"));
        assertEquals("The provisioned access bandwidth is at or exceeds 50% of the total server capacity.",
                mm.get("message"));
        assertEquals("VCE-Cust", mm.get("resource-rule.endpoint-position"));
        assertEquals("Server", mm.get("resource-rule.equipment-level"));
        assertEquals("max-server-speed * number-primary-servers", mm.get("resource-rule.hard-limit-expression"));
        assertEquals("Bandwidth", mm.get("resource-rule.resource-name"));
        assertEquals("DUMMY", mm.get("resource-rule.service-model"));
        assertEquals("0.6 * max-server-speed * number-primary-servers", mm.get("resource-rule.soft-limit-expression"));
        assertEquals("1605000", mm.get("resource-state.last-added"));
        assertEquals("1920000", mm.get("resource-state.limit-value"));
        assertEquals("1600000", mm.get("resource-state.threshold-value"));
        assertEquals("1605000", mm.get("resource-state.used"));
    }

    @Test(expected = NullPointerException.class)
    public void testNullString() throws SvcLogicException {
        JsonParser.convertToProperties(null);
    }

    @Test
    public void testJsonStringToCtxToplevelArray() throws Exception {
        String path = "src/test/resources/ArrayMenu.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Map<String, String> mm = JsonParser.convertToProperties(content);

        assertEquals("1000", mm.get("[0].calories"));
        assertEquals("1", mm.get("[0].id"));
        assertEquals("plain", mm.get("[0].name"));
        assertEquals("pizza", mm.get("[0].type"));
        assertEquals("true", mm.get("[0].vegetarian"));
        assertEquals("2000", mm.get("[1].calories"));
        assertEquals("2", mm.get("[1].id"));
        assertEquals("Tuesday Special", mm.get("[1].name"));
        assertEquals("1", mm.get("[1].topping[0].id"));
        assertEquals("onion", mm.get("[1].topping[0].name"));
        assertEquals("2", mm.get("[1].topping[1].id"));
        assertEquals("pepperoni", mm.get("[1].topping[1].name"));
        assertEquals("2", mm.get("[1].topping_length"));
        assertEquals("pizza", mm.get("[1].type"));
        assertEquals("false", mm.get("[1].vegetarian"));
        assertEquals("1500", mm.get("[2].calories"));
        assertEquals("3", mm.get("[2].id"));
        assertEquals("House Special", mm.get("[2].name"));
        assertEquals("3", mm.get("[2].topping[0].id"));
        assertEquals("basil", mm.get("[2].topping[0].name"));
        assertEquals("4", mm.get("[2].topping[1].id"));
        assertEquals("fresh mozzarella", mm.get("[2].topping[1].name"));
        assertEquals("5", mm.get("[2].topping[2].id"));
        assertEquals("tomato", mm.get("[2].topping[2].name"));
        assertEquals("3", mm.get("[2].topping_length"));
        assertEquals("pizza", mm.get("[2].type"));
        assertEquals("true", mm.get("[2].vegetarian"));
        assertEquals("3", mm.get("_length"));
    }

    @Test
    public void testJsonStringToCtx() throws Exception {
        String path = "src/test/resources/ObjectMenu.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Map<String, String> mm = JsonParser.convertToProperties(content);
        assertEquals("1000", mm.get("menu[0].calories"));
        assertEquals("1", mm.get("menu[0].id"));
        assertEquals("plain", mm.get("menu[0].name"));
        assertEquals("pizza", mm.get("menu[0].type"));
        assertEquals("true", mm.get("menu[0].vegetarian"));
        assertEquals("2000", mm.get("menu[1].calories"));
        assertEquals("2", mm.get("menu[1].id"));
        assertEquals("Tuesday Special", mm.get("menu[1].name"));
        assertEquals("1", mm.get("menu[1].topping[0].id"));
        assertEquals("onion", mm.get("menu[1].topping[0].name"));
        assertEquals("2", mm.get("menu[1].topping[1].id"));
        assertEquals("pepperoni", mm.get("menu[1].topping[1].name"));
        assertEquals("2", mm.get("menu[1].topping_length"));
        assertEquals("pizza", mm.get("menu[1].type"));
        assertEquals("false", mm.get("menu[1].vegetarian"));
        assertEquals("1500", mm.get("menu[2].calories"));
        assertEquals("3", mm.get("menu[2].id"));
        assertEquals("House Special", mm.get("menu[2].name"));
        assertEquals("3", mm.get("menu[2].topping[0].id"));
        assertEquals("basil", mm.get("menu[2].topping[0].name"));
        assertEquals("4", mm.get("menu[2].topping[1].id"));
        assertEquals("fresh mozzarella", mm.get("menu[2].topping[1].name"));
        assertEquals("5", mm.get("menu[2].topping[2].id"));
        assertEquals("tomato", mm.get("menu[2].topping[2].name"));
        assertEquals("3", mm.get("menu[2].topping_length"));
        assertEquals("pizza", mm.get("menu[2].type"));
        assertEquals("true", mm.get("menu[2].vegetarian"));
        assertEquals("3", mm.get("menu_length"));
    }

    @Test
    public void test1dJsonStringToCtx() throws Exception {
        String path = "src/test/resources/1dArray.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Map<String, String> mm = JsonParser.convertToProperties(content);

        System.out.println(mm);
        assertEquals("6", mm.get("_length"));
        assertEquals("apple", mm.get("[0]"));
        assertEquals("orange", mm.get("[1]"));
        assertEquals("banana", mm.get("[2]"));
        assertEquals("squash", mm.get("[3]"));
        assertEquals("broccoli", mm.get("[4]"));
        assertEquals("cauliflower", mm.get("[5]"));
    }

    @Test
    public void test2dJsonStringToCtx() throws Exception {
        String path = "src/test/resources/2dArray.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Map<String, String> mm = JsonParser.convertToProperties(content);

        // code will crash before these tests
        assertEquals("apple", mm.get("[0][0]"));
        assertEquals("orange", mm.get("[0][1]"));
        assertEquals("banana", mm.get("[0][2]"));
        assertEquals("3", mm.get("[0]_length"));

        assertEquals("squash", mm.get("[1][0]"));
        assertEquals("broccoli", mm.get("[1][1]"));
        assertEquals("cauliflower", mm.get("[1][2]"));
        assertEquals("3", mm.get("[1]_length"));
        assertEquals("2", mm.get("_length"));
    }

    @Test
    public void test3dJsonStringToCtx() throws Exception {
        String path = "src/test/resources/3dArray.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Map<String, String> mm = JsonParser.convertToProperties(content);

        assertEquals("3", mm.get("_length"));
        assertEquals("1", mm.get("[0]_length"));
        assertEquals("3", mm.get("[0][0]_length"));

        assertEquals("2", mm.get("[1]_length"));
        assertEquals("3", mm.get("[1][0]_length"));
        assertEquals("3", mm.get("[1][1]_length"));

        assertEquals("3", mm.get("[2]_length"));
        assertEquals("3", mm.get("[2][0]_length"));
        assertEquals("3", mm.get("[2][1]_length"));
        assertEquals("3", mm.get("[2][2]_length"));

        assertEquals("x", mm.get("[0][0][0]"));
        assertEquals("y", mm.get("[0][0][1]"));
        assertEquals("z", mm.get("[0][0][2]"));

        assertEquals("abc", mm.get("[1][0][0]"));
        assertEquals("def", mm.get("[1][0][1]"));
        assertEquals("xyz", mm.get("[1][0][2]"));

        assertEquals("123", mm.get("[1][1][0]"));
        assertEquals("456", mm.get("[1][1][1]"));
        assertEquals("789", mm.get("[1][1][2]"));

        assertEquals("a", mm.get("[2][0][0]"));
        assertEquals("b", mm.get("[2][0][1]"));
        assertEquals("c", mm.get("[2][0][2]"));

        assertEquals("d", mm.get("[2][1][0]"));
        assertEquals("e", mm.get("[2][1][1]"));
        assertEquals("f", mm.get("[2][1][2]"));

        assertEquals("1", mm.get("[2][2][0]"));
        assertEquals("2", mm.get("[2][2][1]"));
        assertEquals("3", mm.get("[2][2][2]"));
    }

    @Test
    public void testJsonWidgetStringToCtx() throws Exception {
        String path = "src/test/resources/Widget.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Map<String, String> mm = JsonParser.convertToProperties(content);
        assertEquals("false", mm.get("widget.debug"));
        assertEquals("center", mm.get("widget.image.alignment"));
        assertEquals("150", mm.get("widget.image.hOffset"));
        assertEquals("moon", mm.get("widget.image.name"));
        assertEquals("images/moon.png", mm.get("widget.image.src"));
        assertEquals("150", mm.get("widget.image.vOffset"));
        assertEquals("center", mm.get("widget.text.alignment"));
        assertEquals("Click Me", mm.get("widget.text.data"));
        assertEquals("350", mm.get("widget.text.hOffset"));
        assertEquals("text1", mm.get("widget.text.name"));
        assertEquals("21", mm.get("widget.text.size"));
        assertEquals("bold", mm.get("widget.text.style"));
        assertEquals("200", mm.get("widget.text.vOffset"));
        assertEquals("300", mm.get("widget.window.height"));
        assertEquals("main_window", mm.get("widget.window.name"));
        assertEquals("ONAP Widget", mm.get("widget.window.title"));
        assertEquals("200", mm.get("widget.window.width"));
    }

    @Test
    public void testEmbeddedEscapedJsonJsonStringToCtx() throws Exception {
        String path = "src/test/resources/EmbeddedEscapedJson.json";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Map<String, String> mm = JsonParser.convertToProperties(content);
        assertEquals("escapedJsonObject", mm.get("input.parameters[0].name"));
        assertEquals("[{\"id\":\"0.2.0.0/16\"},{\"id\":\"ge04::/64\"}]", mm.get("input.parameters[0].value"));
        assertEquals("Hello/World", mm.get("input.parameters[1].value"));
        assertEquals("resourceName", mm.get("input.parameters[2].name"));
        assertEquals("The\t\"Best\"\tName", mm.get("input.parameters[2].value"));
        assertEquals("3", mm.get("input.parameters_length"));

        // Break the embedded json object into properties
        mm = JsonParser.convertToProperties(mm.get("input.parameters[0].value"));
        assertEquals("0.2.0.0/16", mm.get("[0].id"));
        assertEquals("ge04::/64", mm.get("[1].id"));
        assertEquals("2", mm.get("_length"));
    }

}
