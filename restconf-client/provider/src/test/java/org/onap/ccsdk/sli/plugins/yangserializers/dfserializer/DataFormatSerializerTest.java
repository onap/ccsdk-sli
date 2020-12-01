/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
import static org.onap.ccsdk.sli.plugins.restapicall.HttpMethod.GET;
import static org.onap.ccsdk.sli.plugins.restapicall.HttpMethod.PATCH;
import static org.onap.ccsdk.sli.plugins.restapicall.HttpMethod.POST;
import static org.onap.ccsdk.sli.plugins.restapicall.HttpMethod.PUT;
import static org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiUtils.parseUrl;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.DECODE_ANYXML_RESPONSE;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.DECODE_FROM_JSON_RPC;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.DECODE_FROM_XML_RPC;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_ANYXML;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_JSON_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_JSON_ID_PUT;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_JSON_RPC;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_JSON_YANG;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_JSON_YANG_AUG_POST;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_JSON_YANG_PUT;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_XML_ID;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_XML_ID_PUT;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_XML_RPC;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_XML_YANG;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_XML_YANG_AUG_POST;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormatUtilsTest.ENCODE_TO_XML_YANG_PUT;


/**
 * Unit test cases for data format serialization and restconf api call node.
 */
public class DataFormatSerializerTest {

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
     * Verifies encoding of parameters to JSON data format with identity-ref
     * and inter-file linking.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonId() throws SvcLogicException {
        String pre = "identity-test_test.";
        SvcLogicContext ctx = createAttList(pre);
        ctx.setAttribute(pre + "l", "abc");
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/identity-test:test");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_ID));
    }

    /**
     * Verifies encoding of parameters to JSON data format any xml in it.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeForAnyXml() throws SvcLogicException {
        String pre = "execution-service_process.";
        SvcLogicContext ctx = createAnyXmlAttList(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/api/v1/execution-service/process");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_ANYXML));
    }

    /**
     * Verifies encoding of parameters to JSON data format with identity-ref
     * and inter-file linking for put operation-type.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonIdWithPut() throws SvcLogicException {
        String pre = "identity-test_test.";
        SvcLogicContext ctx = createAttList(pre);
        ctx.setAttribute(pre + "l", "abc");
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "put");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/identity-test:test");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_ID_PUT));
    }

    /**
     * Verifies encoding of parameters to JSON data format with identity-ref
     * and inter-file linking for patch operation-type.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonIdWithPatch() throws SvcLogicException {
        String pre = "identity-test_test.";
        SvcLogicContext ctx = createAttList(pre);
        ctx.setAttribute(pre + "l", "abc");
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "patch");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/identity-test:test");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_ID_PUT));
    }

    /**
     * Verifies encoding of parameters to XML data format with identity-ref
     * and inter-file linking.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlId() throws SvcLogicException {
        String pre = "identity-test_test.";
        SvcLogicContext ctx = createAttList(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/identity-test:test");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_ID));
    }

    /**
     * Verifies encoding of parameters to XML data format with identity-ref
     * and inter-file linking for put operation-type.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlIdWithPut() throws SvcLogicException {
        String pre = "identity-test_test.";
        SvcLogicContext ctx = createAttList(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "put");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/identity-test:test");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_ID_PUT));
    }

    /**
     * Verifies encoding of parameters to XML data format with identity-ref
     * and inter-file linking for patch operation-type.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlIdWithPatch() throws SvcLogicException {
        String pre = "identity-test_test.";
        SvcLogicContext ctx = createAttList(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "patch");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/identity-test:test");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_ID_PUT));
    }

    /**
     * Verifies decoding of parameters from JSON data format with identity-ref
     * and inter-file linking.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void decodeToJsonId() throws SvcLogicException {
        createMockForDecode(ENCODE_TO_JSON_ID);
        SvcLogicContext ctx = new SvcLogicContext();
        String pre = "identity-test_test.";
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "get");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/identity-test:test");
        restconf.sendRequest(p, ctx);
        assertThat(ctx.getAttribute(pre + "l"), is("abc"));
        verifyAttList(ctx, pre);
    }

    /**
     * Verifies decoding of parameters from XML data format with identity-ref
     * and inter-file linking.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void decodeToXmlId() throws SvcLogicException {
        createMockForDecode(ENCODE_TO_XML_ID);
        SvcLogicContext ctx = new SvcLogicContext();
        String pre = "identity-test_test.";
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "get");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/identity-test:test");
        restconf.sendRequest(p, ctx);
        verifyAttList(ctx, pre);
    }

    /**
     * Verifies encoding of parameters to JSON data format with containers,
     * grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonYang() throws SvcLogicException {
        String pre = "test-yang_cont1.cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_YANG));
    }

    /**
     * Verifies encoding of parameters to JSON data format with containers,
     * grouping and augment for put operation-type.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonYangWithPut() throws SvcLogicException {
        String pre = "test-yang_cont1.cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "put");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1/cont2/cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_YANG_PUT));
    }

    /**
     * Verifies encoding of parameters to JSON data format with containers,
     * grouping and augment for patch operation-type.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonYangWithPatch() throws SvcLogicException {
        String pre = "test-yang_cont1.cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "patch");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1/cont2/cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_YANG_PUT));
    }

    /**
     * Verifies encoding of parameters to JSON data format with augment as
     * root child.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToJsonWithAugAsRootChild() throws SvcLogicException {
        String pre = "test-yang_cont1.cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1/cont2/cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_YANG_AUG_POST));
    }

    /**
     * Verifies decoding of parameters from JSON data format with containers,
     * grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void decodeToJsonYang() throws SvcLogicException {
        createMockForDecode(ENCODE_TO_JSON_YANG);
        SvcLogicContext ctx = new SvcLogicContext();
        String pre = "test-yang_cont1.cont2.";
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "get");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1");
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
        String pre = "test-yang_cont1.cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_YANG));
    }

    /**
     * Verifies encoding of parameters to XML data format with containers,
     * grouping and augment for put operation-type
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlYangWithPut() throws SvcLogicException {
        String pre = "test-yang_cont1.cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "put");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1/cont2/cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_YANG_PUT));
    }

    /**
     * Verifies encoding of parameters to XML data format with containers,
     * grouping and augment for patch operation-type
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlYangWithPatch() throws SvcLogicException {
        String pre = "test-yang_cont1.cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "put");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1/cont2/cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_YANG_PUT));
    }

    /**
     * Verifies encoding of parameters to XML data format with augment as
     * root child.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void encodeToXmlWithAugAsRootChild() throws SvcLogicException {
        String pre = "test-yang_cont1.cont2.";
        SvcLogicContext ctx = createAttListYang(pre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1/cont2/cont4");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_YANG_AUG_POST));
    }

    /**
     * Verifies decoding of parameters from XML data format with containers,
     * grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void decodeToXmlYang() throws SvcLogicException {
        createMockForDecode(ENCODE_TO_XML_YANG);
        SvcLogicContext ctx = new SvcLogicContext();
        String pre = "test-yang_cont1.cont2.";
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "get");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:cont1");
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
        createMockForDecode(DECODE_FROM_JSON_RPC);
        String inPre = "test-yang_create-sfc.input.";
        String outPre = "test-yang_create-sfc.output.";
        SvcLogicContext ctx = createAttListRpc(inPre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:create-sfc");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_JSON_RPC));
        verifyAttListRpc(ctx, outPre);
    }

    /**
     * Verifies encoding of and decoding from, JSON for ANYXML.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void codecForNormalAnyXml() throws SvcLogicException {
        createMockForDecode(DECODE_ANYXML_RESPONSE);
        String inPre = "execution-service_process.";
        SvcLogicContext ctx = createAnyXmlAttList(inPre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "json");
        p.put("httpMethod", "post");
        p.put("responsePrefix", "pp");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/api/v1/execution-service/process");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_ANYXML));
        verifyOutputOfAnyXml(ctx);
    }

    /**
     * Verifies encoding of and decoding from, XML respectively for data
     * format with containers, grouping and augment.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void codecToXmlRpc() throws SvcLogicException {
        createMockForDecode(DECODE_FROM_XML_RPC);
        String inPre = "test-yang_create-sfc.input.";
        String outPre = "test-yang_create-sfc.output.";
        SvcLogicContext ctx = createAttListRpc(inPre);
        p.put("dirPath", "src/test/resources");
        p.put("format", "xml");
        p.put("httpMethod", "post");
        p.put("restapiUrl", "http://echo.getpostman" +
                ".com/restconf/operations/test-yang:create-sfc");
        restconf.sendRequest(p, ctx);
        assertThat(dfCaptor.getResult(), is(ENCODE_TO_XML_RPC));
        verifyAttListRpc(ctx, outPre);
    }

    /**
     * Verifies URL parser returning path with only schema information for all
     * kind of URL.
     *
     * @throws SvcLogicException when test case fails
     */
    @Test
    public void validateUrlParser() throws SvcLogicException {
        String actVal = "identity-test:test";
        String putId = "/for-put";
        String url1 = "http://echo.getpostman.com/restconf/operations/" +
                actVal;
        String url2 = "http://echo.getpostman.com/restconf/data/" + actVal;
        String url3 = "https://echo.getpostman.com/restconf/operations/" +
                actVal;
        String url4 = "https://echo.getpostman.com/restconf/data/" + actVal +
                putId;
        String url5 = "http://localhost:8282/restconf/operations/" + actVal;
        String url6 = "https://localhost:8282/restconf/operations/" + actVal;
        String url7 = "http://localhost:8282/restconf/data/" + actVal +
                putId;
        String url8 = "https://localhost:8282/restconf/data/" + actVal;
        String url9 = "http://182.2.61.24:2250/restconf/data/" + actVal;
        String url10 = "https://182.2.61.24:2250/restconf/operations/" + actVal;
        String url11 = "https://182.2.61.24:2250/api/v1/execution-service" +
                "/process";
        String url12 = "https://182.2.61.24:2250/api/v1/execution-service" +
                "/process/payload";
        String url13 = "https://182.2.61.24:2250/api/v1/execution-service" +
                "/process/payload/";
        String val1 = parseUrl(url1, POST);
        String val2 = parseUrl(url2, GET);
        String val3 = parseUrl(url3, PATCH);
        String val4 = parseUrl(url4, PUT);
        String val5 = parseUrl(url5, GET);
        String val6 = parseUrl(url6, POST);
        String val7 = parseUrl(url7, PUT);
        String val8 = parseUrl(url8, POST);
        String val9 = parseUrl(url9, GET);
        String val10 = parseUrl(url10, POST);
        String val11 = parseUrl(url11, POST);
        String val12 = parseUrl(url12, POST);
        String val13 = parseUrl(url13, POST);
        assertThat(val1, is(actVal));
        assertThat(val2, is(actVal));
        assertThat(val3, is(actVal));
        assertThat(val4, is(actVal + putId));
        assertThat(val5, is(actVal));
        assertThat(val6, is(actVal));
        assertThat(val7, is(actVal + putId));
        assertThat(val8, is(actVal));
        assertThat(val9, is(actVal));
        assertThat(val10, is(actVal));
        assertThat(val11, is("execution-service:process"));
        assertThat(val12, is("execution-service:process/payload"));
        assertThat(val13, is("execution-service:process/payload/"));
    }

    /**
     * Creates attribute list for encoding JSON or XML with ANYXML YANG
     * file.
     *
     * @param pre prefix
     * @return service logic context
     */
    private SvcLogicContext createAnyXmlAttList(String pre) {
        SvcLogicContext ctx = new SvcLogicContext();
        String pre1 = pre + "commonHeader.";
        String pre2 = pre + "actionIdentifiers.";
        ctx.setAttribute(pre + "isNonAppend", "true");
        ctx.setAttribute(pre1 + "originatorId", "SDNC_DG");
        ctx.setAttribute(pre1 + "requestId", "123456-1000");
        ctx.setAttribute(pre1 + "subRequestId", "sub-123456-1000");
        ctx.setAttribute(pre2 + "blueprintName",
                         "baseconfiguration");
        ctx.setAttribute(pre2 + "blueprintVersion", "1.0.0");
        ctx.setAttribute(pre2 + "actionName", "assign-activate");
        ctx.setAttribute(pre2 + "mode", "sync");
        ctx.setAttribute(pre + "payload." +
                                 "template-prefix", "vDNS-test");
        ctx.setAttribute(pre + "payload.resource-assignment-request" +
                                 ".resource-assignment-properties",
                         "{\n" +
                                 "                \"service-instance-id\": " +
                                 "\"1234\",\n" +
                                 "                \"vnf-id\": \"3526\",\n" +
                                 "                \"customer-name\": \"htipl\",\n" +
                                 "                \"subscriber-name\": \"huawei\"\n" +
                                 "            }");
        return ctx;
    }

    /**
     * Creates attribute list for encoding JSON or XML with identity-ref YANG
     * file.
     *
     * @param pre prefix
     * @return service logic context
     */
    private SvcLogicContext createAttList(String pre) {
        SvcLogicContext ctx = new SvcLogicContext();
        String pre1 = pre + "con1.interfaces.";
        ctx.setAttribute(pre + "con1.interface", "identity-types:physical");
        ctx.setAttribute(pre1 + "int-list[0].iden", "optical");
        ctx.setAttribute(pre1 + "int-list[0].available.ll[0]", "Giga");
        ctx.setAttribute(pre1 + "int-list[0].available.ll[1]",
                         "identity-types:Loopback");
        ctx.setAttribute(pre1 + "int-list[0].available.ll[2]",
                         "identity-types-second:Ethernet");
        ctx.setAttribute(pre1 + "int-list[0].available.leaf1", "58");
        ctx.setAttribute(pre1 + "int-list[0].available.leaf2",
                         "identity-types-second:iden2");

        ctx.setAttribute(pre1 + "int-list[1].iden", "214748364");
        ctx.setAttribute(pre1 + "int-list[1].available.ll[0]", "Giga");
        ctx.setAttribute(pre1 + "int-list[1].available.ll[1]",
                         "identity-types:Loopback");
        ctx.setAttribute(pre1 + "int-list[1].available.ll[2]",
                         "identity-types-second:Ethernet");
        ctx.setAttribute(pre1 + "int-list[1].available.leaf1",
                         "8888");
        ctx.setAttribute(pre1 + "int-list[1].available.leaf2",
                         "identity-types-second:iden2");
        return ctx;
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
        ctx.setAttribute(pre + "cont3.leaf10", "abc");
        ctx.setAttribute(pre + "list1[0].leaf1", "true");
        ctx.setAttribute(pre + "list1[0].leaf2", "abc");
        ctx.setAttribute(pre + "list1[0].leaf3", "abc");
        ctx.setAttribute(pre + "list1[0].ll1[0]", "abc");
        ctx.setAttribute(pre + "list1[0].ll1[1]", "abc");
        ctx.setAttribute(pre + "list1[0].ll2[0]", "abc");
        ctx.setAttribute(pre + "list1[0].ll2[1]", "abc");
        ctx.setAttribute(pre + "list1[0].cont4.leaf11", "abc");
        ctx.setAttribute(pre + "list1[0].list4[0].leaf8", "abc");
        ctx.setAttribute(pre + "list1[0].list4[1].leaf8", "abc");
        ctx.setAttribute(pre + "list1[0].list5[0].leaf9", "abc");
        ctx.setAttribute(pre + "list1[0].list5[1].leaf9", "abc");
        ctx.setAttribute(pre + "list1[1].leaf1", "true");
        ctx.setAttribute(pre + "list1[1].leaf2", "abc");
        ctx.setAttribute(pre + "list1[1].leaf3", "abc");
        ctx.setAttribute(pre + "list1[1].ll1[0]", "abc");
        ctx.setAttribute(pre + "list1[1].ll1[1]", "abc");
        ctx.setAttribute(pre + "list1[1].ll2[0]", "abc");
        ctx.setAttribute(pre + "list1[1].ll2[1]", "abc");
        ctx.setAttribute(pre + "list1[1].cont4.leaf11", "abc");
        ctx.setAttribute(pre + "list1[1].list4[0].leaf8", "abc");
        ctx.setAttribute(pre + "list1[1].list4[1].leaf8", "abc");
        ctx.setAttribute(pre + "list1[1].list5[0].leaf9", "abc");
        ctx.setAttribute(pre + "list1[1].list5[1].leaf9", "abc");
        ctx.setAttribute(pre + "list2[0].leaf4", "abc");
        ctx.setAttribute(pre + "list2[1].leaf4", "abc");
        ctx.setAttribute(pre + "leaf5", "abc");
        ctx.setAttribute(pre + "leaf6", "abc");
        ctx.setAttribute(pre + "ll3[0]", "abc");
        ctx.setAttribute(pre + "ll3[1]", "abc");
        ctx.setAttribute(pre + "ll4[0]", "abc");
        ctx.setAttribute(pre + "ll4[1]", "abc");
        ctx.setAttribute(pre + "cont4.leaf10", "abc");
        ctx.setAttribute(pre + "list6[0].leaf11", "abc");
        ctx.setAttribute(pre + "list6[1].leaf11", "abc");
        ctx.setAttribute(pre + "leaf12", "abc");
        ctx.setAttribute(pre + "ll5[0]", "abc");
        ctx.setAttribute(pre + "ll5[1]", "abc");
        ctx.setAttribute(pre + "cont4.test-augment_cont5.leaf13", "true");
        ctx.setAttribute(pre + "cont4.test-augment_list7[0].leaf14", "test");
        ctx.setAttribute(pre + "cont4.test-augment_list7[1].leaf14", "create");
        ctx.setAttribute(pre + "cont4.test-augment_leaf15", "abc");
        ctx.setAttribute(pre + "cont4.test-augment_ll6[0]", "unbounded");
        ctx.setAttribute(pre + "cont4.test-augment_ll6[1]", "8");
        ctx.setAttribute(pre + "cont4.test-augment_cont13.cont12.leaf26",
                         "abc");
        ctx.setAttribute(pre + "cont4.test-augment_cont13.list9[0].leaf27",
                         "abc");
        ctx.setAttribute(pre + "cont4.test-augment_cont13.list9[1].leaf27",
                         "abc");
        ctx.setAttribute(pre + "cont4.test-augment_cont13.leaf28", "abc");
        ctx.setAttribute(pre + "cont4.test-augment_cont13.ll9[0]", "abc");
        ctx.setAttribute(pre + "cont4.test-augment_cont13.ll9[1]", "abc");
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
        ctx.setAttribute(pre + "cont14.leaf28", "abc");
        ctx.setAttribute(pre + "list10[0].leaf29", "abc");
        ctx.setAttribute(pre + "list10[1].leaf29", "abc");
        ctx.setAttribute(pre + "leaf30", "abc");
        ctx.setAttribute(pre + "ll10[0]", "abc");
        ctx.setAttribute(pre + "ll10[1]", "abc");
        ctx.setAttribute(pre + "cont15.leaf31", "abc");
        ctx.setAttribute(pre + "cont13.list9[0].leaf27", "abc");
        ctx.setAttribute(pre + "cont13.list9[1].leaf27", "abc");
        ctx.setAttribute(pre + "cont13.leaf28", "abc");
        ctx.setAttribute(pre + "cont13.ll9[0]", "abc");
        ctx.setAttribute(pre + "cont13.ll9[1]", "abc");
        return ctx;
    }

    /**
     * Verifies the attribute list for decoding from JSON or XML with
     * identity-ref YANG file.
     *
     * @param ctx service logic context
     * @param pre prefix
     */
    private void verifyAttList(SvcLogicContext ctx, String pre) {
        String pre1 = pre + "con1.interfaces.";
        assertThat(ctx.getAttribute(pre + "con1.interface"), is(
                "identity-types:physical"));
        assertThat(ctx.getAttribute(pre + "con1.interface"), is(
                "identity-types:physical"));
        assertThat(ctx.getAttribute(pre1 + "int-list[0].iden"), is("optical"));
        assertThat(ctx.getAttribute(pre1 + "int-list[0].available.ll[0]"), is(
                "Giga"));
        assertThat(ctx.getAttribute(pre1 + "int-list[0].available.ll[1]"), is(
                "identity-types:Loopback"));
        assertThat(ctx.getAttribute(pre1 + "int-list[0].available.ll[2]"), is(
                "identity-types-second:Ethernet"));
        assertThat(ctx.getAttribute(pre1 + "int-list[0].available.leaf1"), is(
                "58"));
        assertThat(ctx.getAttribute(pre1 + "int-list[0].available.leaf2"), is(
                "identity-types-second:iden2"));

        assertThat(ctx.getAttribute(pre1 + "int-list[1].iden"), is(
                "214748364"));
        assertThat(ctx.getAttribute(pre1 + "int-list[1].available.ll[0]"), is(
                "Giga"));
        assertThat(ctx.getAttribute(pre1 + "int-list[1].available.ll[1]"), is(
                "identity-types:Loopback"));
        assertThat(ctx.getAttribute(pre1 + "int-list[1].available.ll[2]"), is(
                "identity-types-second:Ethernet"));
        assertThat(ctx.getAttribute(pre1 + "int-list[1].available.leaf1"), is(
                "8888"));
        assertThat(ctx.getAttribute(pre1 + "int-list[1].available.leaf2"), is(
                "identity-types-second:iden2"));
    }

    /**
     * Verifies the attribute list for decoding from JSON or XML with
     * container, grouping and augmented file.
     *
     * @param ctx service logic context
     * @param pre prefix
     */
    private void verifyAttListYang(SvcLogicContext ctx, String pre) {
        assertThat(ctx.getAttribute(pre + "cont3.leaf10"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].leaf1"), is("true"));
        assertThat(ctx.getAttribute(pre + "list1[0].leaf2"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].leaf3"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].ll1[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].ll1[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].ll2[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].ll2[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].cont4.leaf11"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].list4[0].leaf8"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].list4[1].leaf8"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].list5[0].leaf9"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[0].list5[1].leaf9"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].leaf1"), is("true"));
        assertThat(ctx.getAttribute(pre + "list1[1].leaf2"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].leaf3"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].ll1[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].ll1[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].ll2[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].ll2[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].cont4.leaf11"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].list4[0].leaf8"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].list4[1].leaf8"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].list5[0].leaf9"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "list1[1].list5[1].leaf9"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "list2[0].leaf4"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list2[1].leaf4"), is("abc"));
        assertThat(ctx.getAttribute(pre + "leaf5"), is("abc"));
        assertThat(ctx.getAttribute(pre + "leaf6"), is("abc"));
        assertThat(ctx.getAttribute(pre + "ll3[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "ll3[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "ll4[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "ll4[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont4.leaf10"), is( "abc"));
        assertThat(ctx.getAttribute(pre + "list6[0].leaf11"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list6[1].leaf11"), is("abc"));
        assertThat(ctx.getAttribute(pre + "leaf12"), is("abc"));
        assertThat(ctx.getAttribute(pre + "ll5[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "ll5[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_cont5.leaf13"),
                   is("true"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_list7[0].leaf14"),
                   is("test"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_list7[1].leaf14"),
                   is("create"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_leaf15"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_ll6[0]"),
                   is("unbounded"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_ll6[1]"),
                   is("8"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_cont13" +
                                            ".cont12.leaf26"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_cont13.list9[0]" +
                                            ".leaf27"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_cont13.list9[1]" +
                                            ".leaf27"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_cont13.leaf28"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_cont13.ll9[0]"),
                   is("abc"));
        assertThat(ctx.getAttribute(pre + "cont4.test-augment_cont13.ll9[1]"),
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
        assertThat(ctx.getAttribute(pre + "cont16.leaf32"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list11[0].leaf33"), is("abc"));
        assertThat(ctx.getAttribute(pre + "list11[1].leaf33"), is("abc"));
        assertThat(ctx.getAttribute(pre + "leaf34"), is("abc"));
        assertThat(ctx.getAttribute(pre + "ll11[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "ll11[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont17.leaf35"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont13.cont12.leaf26"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont13.list9[0].leaf27"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont13.list9[1].leaf27"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont13.ll9[0]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont13.ll9[1]"), is("abc"));
        assertThat(ctx.getAttribute(pre + "cont13.leaf28"), is("abc"));
    }

    /**
     * Verifies the attribute list for decoding from JSON or XML with
     * ANYXML YANG file.
     *
     * @param ctx service logic context
     */
    private void verifyOutputOfAnyXml(SvcLogicContext ctx) {
        System.out.println(ctx.getAttribute("pp.status.eventType"));
        assertThat(ctx.getAttribute("pp.status.eventType"), is(
                "EVENT_COMPONENT_EXECUTED"));
        assertThat(ctx.getAttribute("pp.actionIdentifiers.blueprintName"),
                   is("golden"));
        assertThat(ctx.getAttribute("pp.actionIdentifiers.mode"),
                   is("sync"));
        assertThat(ctx.getAttribute("pp.stepData.name"),
                   is("resource-assignment"));
        assertThat(ctx.getAttribute("pp.status.message"),
                   is("success"));
        assertThat(ctx.getAttribute("pp.commonHeader.originatorId"),
                   is("System"));
        assertThat(ctx.getAttribute("pp.status.code"),
                   is("200"));
        assertThat(ctx.getAttribute("pp.commonHeader.requestId"),
                   is("1234"));
        assertThat(ctx.getAttribute("pp.commonHeader.subRequestId"),
                   is("1234-12234"));
        assertThat(ctx.getAttribute("pp.commonHeader.timestamp"),
                   is("2019-05-18T23:42:41.658Z"));
        assertThat(ctx.getAttribute("pp.status.timestamp"),
                   is("2019-05-18T23:42:41.950Z"));
        assertThat(ctx.getAttribute("pp.actionIdentifiers.blueprintV" +
                                            "ersion"), is("1.0.0"));
        assertThat(ctx.getAttribute("pp.actionIdentifiers.actionName"),
                   is("resource-assignment"));
        assertThat(ctx.getAttribute("pp.payload.resource-assignment-resp" +
                                            "onse.meshed-template.vf-module-1"),
                   is("<interface>\n    <description>This i" +
                              "s the Virtual Firewall entity</description>\n" +
                              "    <vfw>10.0.101.20/24</vfw>\n" +
                              "</interface>"));
        assertThat(ctx.getAttribute("pp.actionIdentifiers.actionName"),
                   is("resource-assignment"));
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
