/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.ccsdk.sli.plugins.yangserializers.dfserializer;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.restapicall.HttpResponse;
import org.onap.ccsdk.sli.plugins.restapicall.RestapiCallNode;
import org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiCallNode;
import org.opendaylight.restconf.common.context.InstanceIdentifierContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.DECODE_FROM_JSON_RPC_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.DECODE_FROM_XML_RPC_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.ENCODE_TO_JSON_RPC_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.ENCODE_TO_JSON_WITH_AUG_PATH;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.ENCODE_TO_JSON_YANG_AUG_POST_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.ENCODE_TO_JSON_YANG_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.ENCODE_TO_JSON_YANG_PUT_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.ENCODE_TO_XML_RPC_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.ENCODE_TO_XML_YANG_AUG_POST_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.ENCODE_TO_XML_YANG_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.IdentifierValidationUtilsTest.ENCODE_TO_XML_YANG_PUT_ID;

/**
 * Unit test cases for identifier validation test.
 */
public class IdentifierValidationTest {

    private Map<String, String> p;

    private RestconfApiCallNode restconf;

    private RestapiCallNode restApi;

    private DfCaptor dfCaptor;

    /**
     * Sets up the pre-requisite for each test case.
     *
     * @throws SvcLogicException when test case fails
     */
    @Before
    public void setUp() throws SvcLogicException {
        p = new HashMap<>();
        p.put("restapiUser", "user1");
        p.put("restapiPassword", "abc123");
        p.put("responsePrefix", "response");
        p.put("skipSending", "true");
        restApi = new RestapiCallNode();
        restconf = mock(RestconfApiCallNode.class);
        dfCaptor = new DfCaptor();
        createMethodMocks();
    }

    /**
     * Creates method mocks using mockito for RestconfApiCallNode class.
     *
     * @throws SvcLogicException when test case fails
     */
    private void createMethodMocks() throws SvcLogicException {
        doReturn(restApi).when(restconf).getRestapiCallNode();
        doCallRealMethod().when(restconf).sendRequest(
                any(Map.class), any(SvcLogicContext.class));
        doCallRealMethod().when(restconf).sendRequest(
                any(Map.class), any(SvcLogicContext.class), any(Integer.class));
        doAnswer(dfCaptor).when(restconf).serializeRequest(
                any(Map.class), any(YangParameters.class), any(String.class),
                any(InstanceIdentifierContext.class));
        doAnswer(dfCaptor).when(restconf).updateReq(
                any(String.class), any(YangParameters.class),
                any(InstanceIdentifierContext.class));
    }

    /**
     * Creates mock using mockito with input data for decoding.
     *
     * @param decodeData input data
     * @throws SvcLogicException when test case fails
     */
    private void createMockForDecode(String decodeData)
            throws SvcLogicException {
        doReturn(decodeData).when(restconf).getResponse(
                any(SvcLogicContext.class), any(YangParameters.class),
                any(String.class), any(HttpResponse.class));
        doCallRealMethod().when(restconf).serializeResponse(
                any(YangParameters.class), any(String.class), any(String.class),
                any(InstanceIdentifierContext.class));
    }

    /**
     * Verifies encoding of parameters to JSON data format with containers,
     * grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonYang() throws SvcLogicException {
        String pre = "test_name_of_the_module_name_of_the_cont1.name_" +
                "of_the_cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman.com/restconf/operati" +
                "ons/test_name_of_the_module:name_of_the_cont1");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_YANG_ID));
    }

    /**
     * Verifies encoding of parameters with augment in the URL.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonYangWithAugUrl() throws SvcLogicException {
        String pre = "test_name_of_the_module_name_of_the_cont1.name_" +
                "of_the_cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman.com/restconf/operati" +
                "ons/test_name_of_the_module:name_of_the_cont1/name_of_t" +
                "he_cont2/name_of_the_cont4/test_augment_1_for_module:na" +
                "me_of_the_cont5");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_WITH_AUG_PATH));
    }

    /**
     * Verifies encoding of parameters to JSON data format with containers,
     * grouping and augment for put operation-type.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonYangWithPut() throws SvcLogicException {
        String pre = "test_name_of_the_module_name_of_the_cont1.name_of_the_cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "put");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test_name_of_the_module:name_of" +
                "_the_cont1/name_of_the_cont2/name_of_the_cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_YANG_PUT_ID));
    }

    /**
     * Verifies encoding of parameters to JSON data format with containers,
     * grouping and augment for patch operation-type.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonYangWithPatch() throws SvcLogicException {
        String pre = "test_name_of_the_module_name_of_the_cont1.name_o" +
                "f_the_cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "patch");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test_name_of_the_module:name_of_" +
                "the_cont1/name_of_the_cont2/name_of_the_cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_YANG_PUT_ID));
    }

    /**
     * Verifies encoding of parameters to JSON data format with augment as
     * root child.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonWithAugAsRootChild() throws SvcLogicException {
        String pre = "test_name_of_the_module_name_of_the_cont1.name_of_" +
                "the_cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test_name_of_the_module:name_of_" +
                "the_cont1/name_of_the_cont2/name_of_the_cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_YANG_AUG_POST_ID));
    }

    /**
     * Verifies decoding of parameters from JSON data format with containers,
     * grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void decodeToJsonYang() throws SvcLogicException {
        createMockForDecode(ENCODE_TO_JSON_YANG_ID);
        SvcLogicContext ctx = new SvcLogicContext();
        String pre = "test_name_of_the_module_name_of_the_cont1.name_" +
                "of_the_cont2.";
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "get");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test_name_of_the_module:name" +
                "_of_the_cont1");
        restconf.sendRequest(p, ctx);
        verifyAttListYang(ctx, pre);
    }

    /**
     * Verifies encoding of parameters to XML data format with containers,
     * grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlYang() throws SvcLogicException {
        String pre = "test_name_of_the_module_name_of_the_cont1.name_of" +
                "_the_cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman.com/restconf/operations/" +
                "test_name_of_the_module:name_of_the_cont1");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_YANG_ID));
    }

    /**
     * Verifies encoding of parameters to XML data format with containers,
     * grouping and augment for put operation-type
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlYangWithPut() throws SvcLogicException {
        String pre = "test_name_of_the_module_name_of_the_cont1.name_" +
                "of_the_cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "put");
        p.put("restapiUrl", "http://echo.getpostman.com/restconf/operations/" +
                "test_name_of_the_module:name_of_the_cont1/name_of_the_cont2" +
                "/name_of_the_cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_YANG_PUT_ID));
    }

    /**
     * Verifies encoding of parameters to XML data format with containers,
     * grouping and augment for patch operation-type
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlYangWithPatch() throws SvcLogicException {
        String pre = "test_name_of_the_module_name_of_the_cont1.name_of" +
                "_the_cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "put");
        p.put("restapiUrl", "http://echo.getpostman.com/restconf/operation" +
                "s/test_name_of_the_module:name_of_the_cont1/name_of_the_c" +
                "ont2/name_of_the_cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_YANG_PUT_ID));
    }

    /**
     * Verifies encoding of parameters to XML data format with augment as
     * root child.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlWithAugAsRootChild() throws SvcLogicException {
        String pre = "test_name_of_the_module_name_of_the_cont1.name_of_the" +
                "_cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman.com/restconf/operations/" +
                "test_name_of_the_module:name_of_the_cont1/name_of_the_cont2" +
                "/name_of_the_cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_YANG_AUG_POST_ID));
    }

    /**
     * Verifies decoding of parameters from XML data format with containers,
     * grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void decodeToXmlYang() throws SvcLogicException {
        createMockForDecode(ENCODE_TO_XML_YANG_ID);
        SvcLogicContext ctx = new SvcLogicContext();
        String pre = "test_name_of_the_module_name_of_the_cont1.name_of_" +
                "the_cont2.";
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "get");
        p.put("restapiUrl", "http://echo.getpostman.com/restconf/operation" +
                "s/test_name_of_the_module:name_of_the_cont1");
        restconf.sendRequest(p, ctx);
        verifyAttListYang(ctx, pre);
    }

    /**
     * Verifies encoding of and decoding from, JSON respectively for data
     * format with containers, grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void codecToJsonRpc() throws SvcLogicException {
        createMockForDecode(DECODE_FROM_JSON_RPC_ID);
        String inPre = "test_name_of_the_module_name_of_the_create-sfc.input.";
        String outPre = "test_name_of_the_module_name_of_the_create-sfc" +
                ".output.";
        SvcLogicContext ctx = createAttListRpc(inPre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman.com/restconf/operations" +
                "/test_name_of_the_module:name_of_the_create-sfc");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_RPC_ID));
        verifyAttListRpc(ctx, outPre);
    }

    /**
     * Verifies encoding of and decoding from, XML respectively for data
     * format with containers, grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void codecToXmlRpc() throws SvcLogicException {
        createMockForDecode(DECODE_FROM_XML_RPC_ID);
        String inPre = "test_name_of_the_module_name_of_the_create-sfc.input.";
        String outPre = "test_name_of_the_module_name_of_the_create-sfc.output.";
        SvcLogicContext ctx = createAttListRpc(inPre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test_name_of_the_module" +
                ":name_of_the_create-sfc");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_RPC_ID));
        verifyAttListRpc(ctx, outPre);
    }

    /**
     * Creates attribute list for encoding JSON or XML with container,
     * grouping and augmented YANG file.
     *
     * @param pre prefix
     * @return service logic context
     */
    private SvcLogicContext createAttListYang(String pre) {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(pre + "name_of_the_cont3.name_of_the_leaf" +
                                 "10", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_leaf1" +
                                 "", "true");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_leaf2" +
                                 "", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_leaf3" +
                                 "", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_" +
                                 "ll1[0]", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_" +
                                 "ll1[1]", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_" +
                                 "ll2[0]", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_" +
                                 "ll2[1]", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_" +
                                 "cont4.name_of_the_leaf11", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_" +
                                 "list4[0].name_of_the_leaf8", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_" +
                                 "list4[1].name_of_the_leaf8", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_" +
                                 "list5[0].name_of_the_leaf9", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[0].name_of_the_" +
                                 "list5[1].name_of_the_leaf9", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "leaf1", "true");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "leaf2", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "leaf3", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "ll1[0]", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "ll1[1]", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "ll2[0]", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "ll2[1]", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "cont4.name_of_the_leaf11", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "list4[0].name_of_the_leaf8", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "list4[1].name_of_the_leaf8", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "list5[0].name_of_the_leaf9", "abc");
        ctx.setAttribute(pre + "name_of_the_list1[1].name_of_the_" +
                                 "list5[1].name_of_the_leaf9", "abc");
        ctx.setAttribute(pre + "name_of_the_list2[0].name_of_the_" +
                                 "leaf4", "abc");
        ctx.setAttribute(pre + "name_of_the_list2[1].name_of_the_" +
                                 "leaf4", "abc");
        ctx.setAttribute(pre + "name_of_the_leaf5", "abc");
        ctx.setAttribute(pre + "name_of_the_leaf6", "abc");
        ctx.setAttribute(pre + "name_of_the_ll3[0]", "abc");
        ctx.setAttribute(pre + "name_of_the_ll3[1]", "abc");
        ctx.setAttribute(pre + "name_of_the_ll4[0]", "abc");
        ctx.setAttribute(pre + "name_of_the_ll4[1]", "abc");
        ctx.setAttribute(pre + "name_of_the_cont4.name_of_the_leaf10",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_list6[0].name_of_the_leaf11",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_list6[1].name_of_the_leaf11",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_leaf12", "abc");
        ctx.setAttribute(pre + "name_of_the_ll5[0]", "abc");
        ctx.setAttribute(pre + "name_of_the_ll5[1]", "abc");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for_" +
                                 "module_name_of_the_cont5.name_of_the_leaf13",
                         "true");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for_" +
                                 "module_name_of_the_list7[0].name_of_the" +
                                 "_leaf14", "test");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for_" +
                                 "module_name_of_the_list7[1].name_of_the" +
                                 "_leaf14", "create");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for_" +
                                 "module_name_of_the_leaf15", "abc");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for_" +
                                 "module_name_of_the_ll6[0]",
                         "unbounded");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for" +
                                 "_module_name_of_the_ll6[1]", "8");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for" +
                                 "_module_name_of_the_cont13.name_of_the_" +
                                 "cont12.name_of_the_leaf26",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for_" +
                                 "module_name_of_the_cont13.name_of_the_" +
                                 "list9[0].name_of_the_leaf27",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for" +
                                 "_module_name_of_the_cont13.name_of_the_" +
                                 "list9[1].name_of_the_leaf27",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for" +
                                 "_module_name_of_the_cont13.name_of_the_" +
                                 "leaf28", "abc");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for" +
                                 "_module_name_of_the_cont13.name_of_the_" +
                                 "ll9[0]", "abc");
        ctx.setAttribute(pre + "name_of_the_cont4.test_augment_1_for" +
                                 "_module_name_of_the_cont13.name_of_the_" +
                                 "ll9[1]", "abc");
        return ctx;
    }

    /**
     * Creates attribute list for encoding JSON or XML with RPC YANG file.
     *
     * @param pre prefix
     * @return service logic context
     */
    private SvcLogicContext createAttListRpc(String pre) {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(pre + "name_of_the_cont14.name_of_the_leaf28",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_list10[0].name_of_the_leaf29",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_list10[1].name_of_the_leaf29",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_leaf30", "abc");
        ctx.setAttribute(pre + "name_of_the_ll10[0]", "abc");
        ctx.setAttribute(pre + "name_of_the_ll10[1]", "abc");
        ctx.setAttribute(pre + "name_of_the_cont15.name_of_the_leaf31",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_cont13.name_of_the_list9[0]" +
                                 ".name_of_the_leaf27", "abc");
        ctx.setAttribute(pre + "name_of_the_cont13.name_of_the_list9[1]" +
                                 ".name_of_the_leaf27", "abc");
        ctx.setAttribute(pre + "name_of_the_cont13.name_of_the_leaf28",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_cont13.name_of_the_ll9[0]",
                         "abc");
        ctx.setAttribute(pre + "name_of_the_cont13.name_of_the_ll9[1]",
                         "abc");
        return ctx;
    }

    /**
     * Verifies the attribute list for decoding from JSON or XML with
     * container, grouping and augmented file.
     *
     * @param ctx service logic context
     * @param pre prefix
     */
    private void verifyAttListYang(SvcLogicContext ctx, String pre) {
        assertThat(ctx.getAttribute(pre + "name_of_the_cont3.name_of" +
                                            "_the_leaf10"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name" +
                                            "_of_the_leaf1"), is("true"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name" +
                                            "_of_the_leaf2"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name" +
                                            "_of_the_leaf3"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name" +
                                            "_of_the_ll1[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name" +
                                            "_of_the_ll1[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name" +
                                            "_of_the_ll2[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name" +
                                            "_of_the_ll2[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name" +
                                            "_of_the_cont4.name_of_the_leaf11"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name_of" +
                                            "_the_list4[0].name_of_the_leaf8"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name_of" +
                                            "_the_list4[1].name_of_the_leaf8"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name_of" +
                                            "_the_list5[0].name_of_the_leaf9"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[0].name_of" +
                                            "_the_list5[1].name_of_the_leaf9"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_leaf1"), is("true"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_leaf2"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_leaf3"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_ll1[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_ll1[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_ll2[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_ll2[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_cont4.name_of_the_leaf11"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_list4[0].name_of_the_leaf8"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_list4[1].name_of_the_leaf8"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_list5[0].name_of_the_leaf9"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list1[1].name_of" +
                                            "_the_list5[1].name_of_the_leaf9"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list2[0].name_of" +
                                            "_the_leaf4"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list2[1].name_of" +
                                            "_the_leaf4"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_leaf5"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_leaf6"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_ll3[0]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_ll3[1]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_ll4[0]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_ll4[1]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.name_of" +
                                            "_the_leaf10"), is( "abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list6[0].name_of" +
                                            "_the_leaf11"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list6[1].name_of" +
                                            "_the_leaf11"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_leaf12"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_ll5[0]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_ll5[1]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_cont5.name_of_the_leaf13"),
                   is("true"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_list7[0].name_of_the_leaf14"),
                   is("test"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_list7[1].name_of_the_leaf14"),
                   is("create"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_leaf15"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_ll6[0]"),
                   is("unbounded"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_ll6[1]"),
                   is("8"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_cont13" +
                                            ".name_of_the_cont12.name_of_" +
                                            "the_leaf26"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_cont13.name_of_the_list9[0]" +
                                            ".name_of_the_leaf27"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_cont13.name_of_the_list9[1]" +
                                            ".name_of_the_leaf27"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_cont13.name_of_the_leaf28"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_cont13.name_of_the_ll9[0]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont4.test_" +
                                            "augment_1_for_module_name_of_" +
                                            "the_cont13.name_of_the_ll9[1]"),
                   is("abc"));
    }

    /**
     * Verifies the attribute list for decoding from JSON or XML with
     * RPC YANG file.
     *
     * @param ctx service logic context
     * @param pre prefix
     */
    private void verifyAttListRpc(SvcLogicContext ctx, String pre) {
        assertThat(ctx.getAttribute(pre + "name_of_the_cont16.name_of_" +
                                            "the_leaf32"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list11[0].name" +
                                            "_of_the_leaf33"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_list11[1].name" +
                                            "_of_the_leaf33"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_leaf34"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_ll11[0]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_ll11[1]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont17.name_of_" +
                                            "the_leaf35"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont13.name_of_" +
                                            "the_cont12.name_of_the_leaf26"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont13.name_of_" +
                                            "the_list9[0].name_of_the_leaf27"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont13.name_of_" +
                                            "the_list9[1].name_of_the_leaf27"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont13.name_of_" +
                                            "the_ll9[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont13.name_of_" +
                                            "the_ll9[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "name_of_the_cont13.name_of_" +
                                            "the_leaf28"), is("abc"));
    }

    /**
     * Captures the data format messages by mocking it, which can be used in
     * testing the value.
     *
     * @param <String> capturing data format
     */
    public class DfCaptor<String> implements Answer {

        private String result;

        /**
         * Returns the captured data format message.
         *
         * @return data format message.
         */
        public String getResult() {
            return result;
        }

        @Override
        public String answer(InvocationOnMock invocationOnMock)
                throws Throwable {
            result = (String) invocationOnMock.callRealMethod();
            return result;
        }
    }

}
