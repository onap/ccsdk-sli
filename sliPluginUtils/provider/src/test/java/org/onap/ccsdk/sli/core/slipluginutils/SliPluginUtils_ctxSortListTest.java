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

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class SliPluginUtils_ctxSortListTest {
    private static final Logger LOG = LoggerFactory.getLogger(SliPluginUtils_ctxSortListTest.class);
    SliPluginUtils utils = new SliPluginUtils();
    SvcLogicContext ctx;
    HashMap<String, String> parameters;
    Random rand = new Random();

    @Before
    public void setUp() throws Exception {
        this.ctx = new SvcLogicContext();
        this.parameters = new HashMap<String, String>();
    }

    @Test
    public final void list_of_containers() throws SvcLogicException {
        this.parameters.put("list", "input.list");
        this.parameters.put("sort-fields", "sort-key");
        this.parameters.put("delimiter", ",");

        ctx.setAttribute("input.list_length", "10");
        for (int i = 0; i < 10; i++) {
            this.ctx.setAttribute("input.list[" + i + "].sort-key", Integer.toString(rand.nextInt(10)));
            this.ctx.setAttribute("input.list[" + i + "].value", Integer.toString(rand.nextInt(10)));
        }

        LOG.trace("BEFORE SORT:");
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        utils.ctxSortList(this.parameters, this.ctx);

        LOG.trace("AFTER SORT:");
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        for (int i = 0; i < 9; i++) {
            assertTrue(this.ctx.getAttribute("input.list[" + i + "].sort-key").compareTo(this.ctx.getAttribute("input.list[" + (i + 1) + "].sort-key")) < 1);
        }
    }

    @Test
    public final void list_of_elements() throws SvcLogicException {
        this.parameters.put("list", "input.list");
        this.parameters.put("delimiter", ",");

        this.ctx.setAttribute("input.list_length", "10");
        for (int i = 0; i < 10; i++) {
            this.ctx.setAttribute("input.list[" + i + ']', Integer.toString(rand.nextInt(10)));
        }

        LOG.trace("BEFORE SORT:");
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        utils.ctxSortList(this.parameters, this.ctx);

        LOG.trace("AFTER SORT:");
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        for (int i = 0; i < 9; i++) {
            assertTrue(this.ctx.getAttribute("input.list[" + i + ']').compareTo(this.ctx.getAttribute("input.list[" + (i + 1) + ']')) < 1);
        }
    }
}
