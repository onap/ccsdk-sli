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

/**
 *
 */
package org.onap.ccsdk.sli.core.slipluginutils;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * @author km991u
 *
 */
public class SliStringUtilsTest {
    private SvcLogicContext ctx;
    private HashMap<String, String> param;
    private SliStringUtils stringUtils = new SliStringUtils();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.ctx = new SvcLogicContext();
        param = new HashMap<String, String>();
    }

    /**
     * @throws SvcLogicException
     * @see SliStringUtils#split(Map, SvcLogicContext)
     */
    @Test
    public final void testSplit() throws SvcLogicException {
        param.put("original_string", "one ## two ## three");
        param.put("regex", " ## ");
        param.put("ctx_memory_result_key", "result");

        stringUtils.split(param, ctx);

        assertThat(ctx.getAttribute("result[0]"), equalTo("one"));
        assertThat(ctx.getAttribute("result[1]"), equalTo("two"));
        assertThat(ctx.getAttribute("result[2]"), equalTo("three"));
        assertThat(ctx.getAttribute("result_length"), equalTo("3"));
    }

    /**
     * @throws SvcLogicException
     * @see SliStringUtils#split(Map, SvcLogicContext)
     */
    @Test
    public final void testSplit_limit() throws SvcLogicException {
        param.put("original_string", "one ## two ## three");
        param.put("regex", " ## ");
        param.put("limit", "2");
        param.put("ctx_memory_result_key", "result");

        stringUtils.split(param, ctx);

        assertThat(ctx.getAttribute("result[0]"), equalTo("one"));
        assertThat(ctx.getAttribute("result[1]"), equalTo("two ## three"));
        assertThat(ctx.getAttribute("result_length"), equalTo("2"));
    }

    @Test
    public void equalsIgnoreCaseTrue() throws SvcLogicException {
        String sourceString = "HeLlOwORLD";
        String targetSTring = "HELLOWORLD";
        param.put("source", sourceString);
        param.put("target", targetSTring);
        assertEquals("true", SliStringUtils.equalsIgnoreCase(param, ctx));
    }

    @Test
    public void equalsIgnoreCaseFalse() throws SvcLogicException {
        String sourceString = "HeLlOwORLD";
        String targetSTring = "goodbyeWORLD";
        param.put("source", sourceString);
        param.put("target", targetSTring);
        assertEquals("false", SliStringUtils.equalsIgnoreCase(param, ctx));
    }

    @Test
    public void toUpper() throws SvcLogicException {
        String sourceString = "HeLlOwORLD";
        param.put("source", sourceString);
        String path = "my.unique.path.";
        param.put("outputPath", path);
        SliStringUtils.toUpper(param, ctx);
        assertEquals(sourceString.toUpperCase(), ctx.getAttribute(path));
    }

    @Test
    public void toLower() throws SvcLogicException {
        String sourceString = "HeLlOwORLD";
        param.put("source", sourceString);
        String path = "my.unique.path.";
        param.put("outputPath", path);
        SliStringUtils.toLower(param, ctx);
        assertEquals(sourceString.toLowerCase(), ctx.getAttribute(path));
    }

    @Test
    public void containsTrue() throws SvcLogicException {
        String sourceString = "Pizza";
        String targetSTring = "izza";
        param.put("source", sourceString);
        param.put("target", targetSTring);
        assertEquals("true", SliStringUtils.contains(param, ctx));
    }

    @Test
    public void containsFalse() throws SvcLogicException {
        String sourceString = "Pizza";
        String targetSTring = "muffin";
        param.put("source", sourceString);
        param.put("target", targetSTring);
        assertEquals("false", SliStringUtils.contains(param, ctx));
    }

    @Test
    public void endsWithTrue() throws SvcLogicException {
        String sourceString = "Pizza";
        String targetSTring = "za";
        param.put("source", sourceString);
        param.put("target", targetSTring);
        assertEquals("true", SliStringUtils.endsWith(param, ctx));
    }

    @Test
    public void endsWithFalse() throws SvcLogicException {
        String sourceString = "Pizza";
        String targetSTring = "muffin";
        param.put("source", sourceString);
        param.put("target", targetSTring);
        assertEquals("false", SliStringUtils.endsWith(param, ctx));
    }

    @Test
    public void trim() throws SvcLogicException {
        String sourceString = " H E L L O W O R L D";
        String outputPath = "muffin";
        param.put("source", sourceString);
        param.put("outputPath", outputPath);
        SliStringUtils.trim(param, ctx);
        assertEquals(sourceString.trim(), ctx.getAttribute(outputPath));
    }

    @Test
    public void getLength() throws SvcLogicException {
        String sourceString = "SomeRandomString";
        String outputPath = "muffin";
        param.put("source", sourceString);
        param.put("outputPath", outputPath);
        SliStringUtils.getLength(param, ctx);
        assertEquals(String.valueOf(sourceString.length()), ctx.getAttribute(outputPath));
    }

    @Test
    public void startsWithFalse() throws SvcLogicException {
        String sourceString = "Java";
        String targetSTring = "DG";
        param.put("source", sourceString);
        param.put("target", targetSTring);
        assertEquals("false", SliStringUtils.startsWith(param, ctx));
    }

    @Test
    public void startsWithTrue() throws SvcLogicException {
        String sourceString = "Java";
        String targetSTring = "Ja";
        param.put("source", sourceString);
        param.put("target", targetSTring);
        assertEquals("true", SliStringUtils.startsWith(param, ctx));
    }

    @Test
    public void replace() throws SvcLogicException {
        String sourceString = "cat Hello World cat";
        String old = "cat";
        String neww = "dog";
        String outputPath = "out";

        param.put("source", sourceString);
        param.put("target", old);
        param.put("replacement", neww);
        param.put("outputPath", outputPath);
        SliStringUtils.replace(param, ctx);
        assertEquals(sourceString.replace(old, neww), ctx.getAttribute(outputPath));
    }

    @Test
    public void concat() throws SvcLogicException {
        String sourceString = "cat";
        String targetString = "dog";
        String outputPath = "out";

        param.put("source", sourceString);
        param.put("target", targetString);
        param.put("outputPath", outputPath);
        SliStringUtils.concat(param, ctx);
        assertEquals(sourceString + targetString, ctx.getAttribute(outputPath));
    }

    @Test
    public void urlEncode() throws SvcLogicException {
        String sourceString = "102/GE100/SNJSCAMCJP8/SNJSCAMCJT4";
        String outputPath = "out";

        param.put("source", sourceString);
        param.put("outputPath", outputPath);
        SliStringUtils.urlEncode(param, ctx);
        assertEquals("102%2FGE100%2FSNJSCAMCJP8%2FSNJSCAMCJT4", ctx.getAttribute(outputPath));
    }

}
