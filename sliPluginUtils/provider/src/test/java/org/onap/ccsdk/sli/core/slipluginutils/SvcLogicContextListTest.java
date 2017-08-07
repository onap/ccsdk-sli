/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 ONAP
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

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.slipluginutils.SvcLogicContextList;

public class SvcLogicContextListTest {
	//private static final Logger LOG = LoggerFactory.getLogger(SvcLogicContextTest.class);
	private SvcLogicContext ctx;

	@Before
	public void setUp() throws Exception {
		this.ctx = new SvcLogicContext();
	}

	// TODO: javadoc
	@Test
	public final void testSvcLogicContextList_SingleValueList() {
		ctx.setAttribute("list[0]", "0");
		ctx.setAttribute("list[1]", "1");
		ctx.setAttribute("list[2]", "2");
		ctx.setAttribute("list[3]", "3");
		ctx.setAttribute("list[4]", "4");
		ctx.setAttribute("list_length", "5");

		SvcLogicContextList list = new SvcLogicContextList( ctx, "list" );

		// Check that size of list is 5
		assertEquals(5, list.size());

		// Check that each HashMap has it's list value in the empty string key
		// and has no other values
		assertEquals(1, list.get(0).size());
		assertEquals("0", list.get(0).get(""));
		assertEquals(1, list.get(1).size());
		assertEquals("1", list.get(1).get(""));
		assertEquals(1, list.get(2).size());
		assertEquals("2", list.get(2).get(""));
		assertEquals(1, list.get(3).size());
		assertEquals("3", list.get(3).get(""));
		assertEquals(1, list.get(4).size());
		assertEquals("4", list.get(4).get(""));
	}

	// TODO: javadoc
	@Test
	public final void testSvcLogicContextList_ObjectList() {
		ctx.setAttribute("list[0].ipv4", "1.1.1.0");
		ctx.setAttribute("list[0].ipv6", "2001::0");
		ctx.setAttribute("list[1].ipv4", "1.1.1.1");
		ctx.setAttribute("list[1].ipv6", "2001::1");
		ctx.setAttribute("list[2].ipv4", "1.1.1.2");
		ctx.setAttribute("list[2].ipv6", "2001::2");
		ctx.setAttribute("list[3].ipv4", "1.1.1.3");
		ctx.setAttribute("list[3].ipv6", "2001::3");
		ctx.setAttribute("list[4].ipv4", "1.1.1.4");
		ctx.setAttribute("list[4].ipv6", "2001::4");
		ctx.setAttribute("list_length", "5");

		SvcLogicContextList list = new SvcLogicContextList( ctx, "list" );

		// Check that size of list is 5
		assertEquals(5, list.size());

		assertEquals(2, list.get(0).size());
		assertEquals("1.1.1.0", list.get(0).get("ipv4"));
		assertEquals("2001::0", list.get(0).get("ipv6"));
		assertEquals(2, list.get(1).size());
		assertEquals("1.1.1.1", list.get(1).get("ipv4"));
		assertEquals("2001::1", list.get(1).get("ipv6"));
		assertEquals(2, list.get(2).size());
		assertEquals("1.1.1.2", list.get(2).get("ipv4"));
		assertEquals("2001::2", list.get(2).get("ipv6"));
		assertEquals(2, list.get(3).size());
		assertEquals("1.1.1.3", list.get(3).get("ipv4"));
		assertEquals("2001::3", list.get(3).get("ipv6"));
		assertEquals(2, list.get(4).size());
		assertEquals("1.1.1.4", list.get(4).get("ipv4"));
		assertEquals("2001::4", list.get(4).get("ipv6"));
	}

	// TODO: javadoc
	@Test
	public final void testExtract() {
		ctx.setAttribute("list[0]", "0");
		ctx.setAttribute("list[1]", "1");
		ctx.setAttribute("list[2]", "2");
		ctx.setAttribute("list[3]", "3");
		ctx.setAttribute("list[4]", "4");
		ctx.setAttribute("list_length", "5");
		ctx.setAttribute("Other", "other");

		SvcLogicContextList list = SvcLogicContextList.extract(ctx, "list");

		// Check that size of list is 5
		assertEquals(5, list.size());

		// Check that all list values exist in list object
		assertEquals(1, list.get(0).size());
		assertEquals("0", list.get(0).get(""));
		assertEquals(1, list.get(1).size());
		assertEquals("1", list.get(1).get(""));
		assertEquals(1, list.get(2).size());
		assertEquals("2", list.get(2).get(""));
		assertEquals(1, list.get(3).size());
		assertEquals("3", list.get(3).get(""));
		assertEquals(1, list.get(4).size());
		assertEquals("4", list.get(4).get(""));

		// Check that all list values no longer exist in ctx
		assertNull(ctx.getAttribute("list[0]"));
		assertNull(ctx.getAttribute("list[1]"));
		assertNull(ctx.getAttribute("list[2]"));
		assertNull(ctx.getAttribute("list[3]"));
		assertNull(ctx.getAttribute("list[4]"));
		assertNull(ctx.getAttribute("list_length"));

		// Check that non-list values still exist in ctx
		assertEquals("other", ctx.getAttribute("Other"));
	}

	// TODO: javadoc
	@Test
	public final void testRemove_int() {
		ctx.setAttribute("list[0]", "0");
		ctx.setAttribute("list[1]", "1");
		ctx.setAttribute("list[2]", "2");
		ctx.setAttribute("list[3]", "3");
		ctx.setAttribute("list[4]", "4");
		ctx.setAttribute("list_length", "5");

		SvcLogicContextList list = new SvcLogicContextList( ctx, "list" );
		list.remove(2);

		// Check that size of list is 4 (1 less than original)
		assertEquals(4, list.size());

		// Check that value was remove from list
		assertEquals(1, list.get(0).size());
		assertEquals("0", list.get(0).get(""));
		assertEquals(1, list.get(1).size());
		assertEquals("1", list.get(1).get(""));
		assertEquals(1, list.get(2).size());
		assertEquals("3", list.get(2).get(""));
		assertEquals(1, list.get(3).size());
		assertEquals("4", list.get(3).get(""));
	}

	// TODO: javadoc
	@Test
	public final void testRemove_StringString() {
		ctx.setAttribute("list[0].ipv4", "1.1.1.0");
		ctx.setAttribute("list[0].ipv6", "2001::0");
		ctx.setAttribute("list[1].ipv4", "1.1.1.1");
		ctx.setAttribute("list[1].ipv6", "2001::1");
		ctx.setAttribute("list[2].ipv4", "1.1.1.2");
		ctx.setAttribute("list[2].ipv6", "2001::2");
		ctx.setAttribute("list[3].ipv4", "1.1.1.3");
		ctx.setAttribute("list[3].ipv6", "2001::3");
		ctx.setAttribute("list[4].ipv4", "1.1.1.4");
		ctx.setAttribute("list[4].ipv6", "2001::4");
		ctx.setAttribute("list[5].ipv4", "1.1.1.2");
		ctx.setAttribute("list[5].ipv6", "2001::2");
		ctx.setAttribute("list_length", "6");

		SvcLogicContextList list = new SvcLogicContextList( ctx, "list" );
		list.remove("ipv4", "1.1.1.2");

		// Check that size of list is 4 (2 less than original)
		assertEquals(4, list.size());

		// Check that all elements with values ending in 2 were removed
		assertEquals("1.1.1.0", list.get(0).get("ipv4"));
		assertEquals("2001::0", list.get(0).get("ipv6"));
		assertEquals("1.1.1.1", list.get(1).get("ipv4"));
		assertEquals("2001::1", list.get(1).get("ipv6"));
		assertEquals("1.1.1.3", list.get(2).get("ipv4"));
		assertEquals("2001::3", list.get(2).get("ipv6"));
		assertEquals("1.1.1.4", list.get(3).get("ipv4"));
		assertEquals("2001::4", list.get(3).get("ipv6"));
	}

	// TODO: javadoc
	@Test
	public final void testRemove_StringString_ValueList() {
		ctx.setAttribute("list[0]", "5");
		ctx.setAttribute("list[1]", "6");
		ctx.setAttribute("list[2]", "7");
		ctx.setAttribute("list[3]", "8");
		ctx.setAttribute("list[4]", "9");
		ctx.setAttribute("list_length", "5");

		SvcLogicContextList list = new SvcLogicContextList( ctx, "list" );
		list.remove("", "6");

		// Check that size of list is 4 (1 less than original)
		assertEquals(4, list.size());

		// Check that value was remove from list
		assertEquals(1, list.get(0).size());
		assertEquals("5", list.get(0).get(""));
		assertEquals(1, list.get(1).size());
		assertEquals("7", list.get(1).get(""));
		assertEquals(1, list.get(2).size());
		assertEquals("8", list.get(2).get(""));
		assertEquals(1, list.get(3).size());
		assertEquals("9", list.get(3).get(""));
	}

	// TODO: javadoc
	@Test
	public final void testRemove_Map() {
		ctx.setAttribute("list[0].ipv4", "1.1.1.0");
		ctx.setAttribute("list[0].ipv6", "2001::0");
		ctx.setAttribute("list[1].ipv4", "1.1.1.1");
		ctx.setAttribute("list[1].ipv6", "2001::1");
		ctx.setAttribute("list[2].ipv4", "1.1.1.2");
		ctx.setAttribute("list[2].ipv6", "2001::2");
		ctx.setAttribute("list[3].ipv4", "1.1.1.3");
		ctx.setAttribute("list[3].ipv6", "2001::3");
		ctx.setAttribute("list[4].ipv4", "1.1.1.4");
		ctx.setAttribute("list[4].ipv6", "2001::4");
		ctx.setAttribute("list[5].ipv4", "1.1.1.2");
		ctx.setAttribute("list[5].ipv6", "2001::2");
		ctx.setAttribute("list_length", "6");

		HashMap<String,String> remove_key = new HashMap<String,String>();
		remove_key.put("ipv4", "1.1.1.2");
		remove_key.put("ipv6", "2001::2");

		SvcLogicContextList list = new SvcLogicContextList( ctx, "list" );
		list.remove(remove_key);

		// Check that size of list is 4 (2 less than original)
		assertEquals(4, list.size());

		// Check that all elements with values ending in 2 were removed
		assertEquals("1.1.1.0", list.get(0).get("ipv4"));
		assertEquals("2001::0", list.get(0).get("ipv6"));
		assertEquals("1.1.1.1", list.get(1).get("ipv4"));
		assertEquals("2001::1", list.get(1).get("ipv6"));
		assertEquals("1.1.1.3", list.get(2).get("ipv4"));
		assertEquals("2001::3", list.get(2).get("ipv6"));
		assertEquals("1.1.1.4", list.get(3).get("ipv4"));
		assertEquals("2001::4", list.get(3).get("ipv6"));
	}

	// TODO: javadoc
	@Test
	public final void testWriteToContext() {
		ctx.setAttribute("list[0]", "0");
		ctx.setAttribute("list[1]", "1");
		ctx.setAttribute("list[2]", "2");
		ctx.setAttribute("list[3]", "3");
		ctx.setAttribute("list[4]", "4");
		ctx.setAttribute("list_length", "5");
		ctx.setAttribute("Other", "other");

		SvcLogicContextList list = new SvcLogicContextList( ctx, "list" );

		// Erase context memory
		ctx = new SvcLogicContext();

		// Write list back into context memory
		list.writeToContext(ctx);

		// Check that size of list is 5
		assertEquals(5, list.size());

		// Check that all list values exist in list object
		assertEquals("0", ctx.getAttribute("list[0]"));
		assertEquals("1", ctx.getAttribute("list[1]"));
		assertEquals("2", ctx.getAttribute("list[2]"));
		assertEquals("3", ctx.getAttribute("list[3]"));
		assertEquals("4", ctx.getAttribute("list[4]"));
		assertEquals("5", ctx.getAttribute("list_length"));

		// Check that old list values aren't in new list
		assertNull(ctx.getAttribute("Other"));
	}
}
