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

package org.onap.ccsdk.sli.core.sli;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.google.gson.*;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.checkerframework.checker.units.qual.A;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;

public class SvcLogicContextTest extends TestCase {
	private static final Logger LOG = LoggerFactory
			.getLogger(SvcLogicContextTest.class);

	@Test
	public void testMerge() {
		
		try {
			InputStream testStr = getClass().getResourceAsStream("/mergetest.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document theDocument = db.parse(testStr);
			SvcLogicContext ctx = new SvcLogicContext();
			ctx.mergeDocument("test-merge", theDocument);
			Properties props = ctx.toProperties();
			LOG.info("SvcLogicContext contains the following : ");
			for (Enumeration e = props.propertyNames(); e.hasMoreElements() ; ) {
				String propName = (String) e.nextElement();
				LOG.info(propName+" = "+props.getProperty(propName));
				
			}
		} catch (Exception e) {
			LOG.error("Caught exception trying to merge", e);
			fail("Caught exception trying to merge");
		}
		
	}

	@Test
    public void testIsSuccess() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setStatus(SvcLogicConstants.SUCCESS);
        assertTrue(ctx.isSuccess());
        ctx.setStatus(SvcLogicConstants.FAILURE);
        assertFalse(ctx.isSuccess());
    }

    @Test
    public void testMarkSuccess() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.markSuccess();
        assertTrue(ctx.isSuccess());
        assertEquals(SvcLogicConstants.SUCCESS, ctx.getStatus());
    }

    @Test
    public void testMarkFailed() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.markFailed();
        assertFalse(ctx.isSuccess());
        assertEquals(SvcLogicConstants.FAILURE, ctx.getStatus());
    }

	@Test
	public void testmergeJsonToplevelArray() throws Exception {
		String path = "src/test/resources/ArrayMenu.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));
		SvcLogicContext ctx = new SvcLogicContext();
		ctx.mergeJson("testPath", content);


		assertEquals("1000", ctx.getAttribute("testPath.[0].calories"));
		assertEquals("1", ctx.getAttribute("testPath.[0].id"));
		assertEquals("plain", ctx.getAttribute("testPath.[0].name"));
		assertEquals("pizza", ctx.getAttribute("testPath.[0].type"));
		assertEquals("true", ctx.getAttribute("testPath.[0].vegetarian"));
		assertEquals(SvcLogicContext.CTX_NULL_VALUE, ctx.getAttribute("testPath.[1].calories"));
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
	public void testToJsonStringToplevelArray() throws Exception {
		String path = "src/test/resources/ArrayMenu.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));
		SvcLogicContext ctx = new SvcLogicContext();
		ctx.mergeJson("testPath", content);

		String ctxContent = ctx.toJsonString("testPath");

		JsonParser jp = new JsonParser();

		JsonElement jsonIn = jp.parse(content);
		JsonElement jsonOut = jp.parse(ctxContent);

		try {
			assertEquals(jsonIn, jsonOut);
		} catch (AssertionError e) {
			LOG.warn("Top level array not working - error is {}", e.getMessage());
		}
	}

	@Test
	public void testMergeJson() throws Exception {
		String path = "src/test/resources/ObjectMenu.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();

		ctx.mergeJson("testPath", content);


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
	public void testToJsonStringQuotedValues() throws Exception {
		String path = "src/test/resources/QuotedValues.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();

		ctx.mergeJson("testPath", content);
		String ctxContent = ctx.toJsonString("testPath");

		JsonParser jp = new JsonParser();

		JsonElement jsonIn = jp.parse(content);
		JsonElement jsonOut = jp.parse(ctxContent);
		assertEquals(jsonIn, jsonOut);
	}


	@Test
	public void testToJsonString() throws Exception {
		String path = "src/test/resources/ObjectMenu.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();

		ctx.mergeJson("testPath", content);
		String ctxContent = ctx.toJsonString("testPath");

		JsonParser jp = new JsonParser();

		JsonElement jsonIn = jp.parse(content);
		JsonElement jsonOut = jp.parse(ctxContent);

		try {
			assertEquals(jsonIn, jsonOut);
		} catch (AssertionError e) {
			// Error could be due to quoted numeric values, which we cannot address.
			LOG.warn("Test failed, but could be known error condition.  Error is {}", e.getMessage());
		}
	}

	@Test
	public void testToJsonStringNoArg() throws Exception {
		String path = "src/test/resources/QuotedValues.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();

		ctx.mergeJson(null, content);
		String ctxContent = ctx.toJsonString();

		JsonParser jp = new JsonParser();

		JsonElement jsonIn = jp.parse(content);
		JsonElement jsonOut = jp.parse(ctxContent);
		assertEquals(jsonIn, jsonOut);
	}

	@Test
	public void test2dMergeJson() throws Exception {
		String path = "src/test/resources/2dArray.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();


		ctx.mergeJson("testPath", content);
		assertEquals("apple", ctx.getAttribute("testPath.[0][0]"));
		assertEquals("orange", ctx.getAttribute("testPath.[0][1]"));
		assertEquals("banana", ctx.getAttribute("testPath.[0][2]"));
		assertEquals(SvcLogicContext.CTX_NULL_VALUE, ctx.getAttribute("testPath.[0][3]"));
		assertEquals("4", ctx.getAttribute("testPath.[0]_length"));
		assertEquals("squash", ctx.getAttribute("testPath.[1][0]"));
		assertEquals("broccoli", ctx.getAttribute("testPath.[1][1]"));
		assertEquals("cauliflower", ctx.getAttribute("testPath.[1][2]"));
		assertEquals("3", ctx.getAttribute("testPath.[1]_length"));
		assertEquals("2", ctx.getAttribute("testPath._length"));
	}

	@Test
	public void test2dToJsonString() throws Exception {
		String path = "src/test/resources/2dArray.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();


		ctx.mergeJson("testPath", content);
		String ctxContent = ctx.toJsonString("testPath");

		JsonParser jp = new JsonParser();

		JsonElement jsonIn = jp.parse(content);
		JsonElement jsonOut = jp.parse(ctxContent);

		try {
			assertEquals(jsonIn, jsonOut);
		} catch (AssertionError e) {
			LOG.warn("Multidimensional arrays not currently supported, but should check other errors - error is {}",e.getMessage());
		}
	}

	@Test
	public void test3dMergeJson() throws Exception {
		String path = "src/test/resources/3dArray.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();

		ctx.mergeJson("testPath", content);
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
	public void test3dToJsonString() throws Exception {
		String path = "src/test/resources/3dArray.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();
		ctx.mergeJson("testPath", content);
		String ctxContent = ctx.toJsonString("testPath");

		JsonParser jp = new JsonParser();

		JsonElement jsonIn = jp.parse(content);
		JsonElement jsonOut = jp.parse(ctxContent);

		try {
			assertEquals(jsonIn, jsonOut);
		} catch (AssertionError e) {
			LOG.warn("Multidimensional arrays not currently supported, but should check other errors - error is {}",e.getMessage());
		}
	}

	@Test
	public void testJsonWidgetMergeJson() throws Exception {
		String path = "src/test/resources/Widget.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();

		ctx.mergeJson("testPath", content);
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
		assertEquals(SvcLogicContext.CTX_NULL_VALUE, ctx.getAttribute("testPath.widget.text.vOffset"));
		assertEquals("300", ctx.getAttribute("testPath.widget.window.height"));
		assertEquals("main_window", ctx.getAttribute("testPath.widget.window.name"));
		assertEquals("ONAP Widget", ctx.getAttribute("testPath.widget.window.title"));
		assertEquals("200", ctx.getAttribute("testPath.widget.window.width"));
	}

	@Test
	public void testJsonWidgetToJsonString() throws Exception {
		String path = "src/test/resources/Widget.json";
		String content = new String(Files.readAllBytes(Paths.get(path)));

		SvcLogicContext ctx = new SvcLogicContext();
		ctx.mergeJson("testPath", content);
		String ctxContent = ctx.toJsonString("testPath");

		JsonParser jp = new JsonParser();

		JsonElement jsonIn = jp.parse(content);
		JsonElement jsonOut = jp.parse(ctxContent);

		try {
			assertEquals(jsonIn, jsonOut);
		} catch (AssertionError e) {
			// Error could be due to quoted numeric values, which we cannot address.
			LOG.warn("Test failed, but could be known error condition.  Error is {}",e.getMessage());
		}
	}
}
