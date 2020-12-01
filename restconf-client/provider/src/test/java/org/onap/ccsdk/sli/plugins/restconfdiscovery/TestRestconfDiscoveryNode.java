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

package org.onap.ccsdk.sli.plugins.restconfdiscovery;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.restapicall.RestapiCallNode;
import org.onap.ccsdk.sli.plugins.restconfapicall.RestconfApiCallNode;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class TestRestconfDiscoveryNode {

    private static final URI CONTEXT = URI.create("http://localhost:8080/");

    @Test
    public void sendRequest() throws SvcLogicException, InterruptedException, IOException {
        final ResourceConfig resourceConfig = new ResourceConfig(
                SseServerMock.class, SseFeature.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(CONTEXT,
                                                           resourceConfig);
        server.start();
        RestconfApiCallNode restconf = mock(RestconfApiCallNode.class);
        doNothing().when(restconf)
                .sendRequest(any(Map.class), any(SvcLogicContext.class));
        RestapiCallNode restApi = new RestapiCallNode();
        doReturn(restApi).when(restconf).getRestapiCallNode();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("prop.encoding-json", "encoding-json");
        ctx.setAttribute("restapi-result.response-code", "200");
        ctx.setAttribute("restapi-result.ietf-subscribed-notifications" +
                                 ":establish-subscription.output.identifier",
                         "89");

        Map<String, String> p = new HashMap<>();
        p.put("sseConnectURL", "http://localhost:8080/events");
        p.put("subscriberId", "networkId");
        p.put("responsePrefix", "restapi-result");
        p.put("restapiUser", "access");
        p.put("restapiPassword", "abc@123");
        p.put("customHttpHeaders", "X-ACCESS-TOKEN=x-ik2ps4ikvzupbx0486ft" +
                "1ebzs7rt85futh9ho6eofy3wjsap7wqktemlqm4bbsmnar3vrtbyrzuk" +
                "bv5itd6m1cftldpjarnyle3sdcqq9hftc4lebz464b5ffxmlbvg9");
        p.put("restapiUrl", "https://localhost:8080/restconf/operations/" +
                "ietf-subscribed-notifications:establish-subscription");
        p.put("module", "testmodule");
        p.put("rpc", "testrpc");
        p.put("version", "1.0");
        p.put("mode", "sync");
        RestconfDiscoveryNode rdn = new RestconfDiscoveryNode(restconf);
        rdn.establishSubscription(p, ctx);
        Thread.sleep(1000);
        rdn.deleteSubscription(p, ctx);
        server.shutdown();
    }

    @Test(expected = SvcLogicException.class)
    public void testSubGraphExecution() throws SvcLogicException{
        SvcLogicGraphInfo subDg = new SvcLogicGraphInfo();
        subDg.mode("sync");
        subDg.module("l3VpnService");
        subDg.rpc("createVpn");
        subDg.version("1.0");
        SvcLogicContext ctx = new SvcLogicContext();
        subDg.executeGraph(ctx);
    }

    @Test(expected = SvcLogicException.class)
    public void testEstablishSubscriptionWithoutSubscriberId()
            throws SvcLogicException{
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> p = new HashMap<>();
        RestconfDiscoveryNode rdn = new RestconfDiscoveryNode(
                new RestconfApiCallNode(new RestapiCallNode()));
        rdn.establishSubscription(p, ctx);
    }

    @Test
    public void testResponseCode() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("restapi-result.response-code", "200");
        ctx.setAttribute("response-code", "404");
        RestconfDiscoveryNode rdn = new RestconfDiscoveryNode(
                new RestconfApiCallNode(new RestapiCallNode()));
        assertThat(rdn.getResponseCode("restapi-result", ctx),
                   is("200"));
        assertThat(rdn.getResponseCode(null, ctx),
                   is("404"));
    }

    @Test
    public void testOutputIdentifier() {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("restapi-result.ietf-subscribed-notifications:" +
                                 "establish-subscription.output.identifier",
                         "89");
        ctx.setAttribute("ietf-subscribed-notifications:establish-subscripti" +
                                 "on.output.identifier", "89");
        RestconfDiscoveryNode rdn = new RestconfDiscoveryNode(
                new RestconfApiCallNode(new RestapiCallNode()));
        assertThat(rdn.getOutputIdentifier("restapi-result", ctx),
                   is("89"));
    }

    @Test
    public void testGetTokenId() {
        String customHttpHeaders = "X-ACCESS-TOKEN=x-ik2ps4ikvzupbx0486ft1ebzs7rt85" +
                "futh9ho6eofy3wjsap7wqktemlqm4bbsmnar3vrtbyrzukbv5itd6m1cftldpjarny" +
                "le3sdcqq9hftc4lebz464b5ffxmlbvg9";
        RestconfDiscoveryNode rdn = new RestconfDiscoveryNode(
                new RestconfApiCallNode(new RestapiCallNode()));

        assertThat(rdn.getTokenId(customHttpHeaders),
                   is("x-ik2ps4ikvzupbx0486ft1ebzs7rt85futh9ho6eofy3wjsap7wqkt" +
                              "emlqm4bbsmnar3vrtbyrzukbv5itd6m1cftldpjarnyle3sdcqq9h" +
                              "ftc4lebz464b5ffxmlbvg9"));
    }

    @Test
    public void testSubscriptionInfo() throws SvcLogicException {
        SubscriptionInfo info = new SubscriptionInfo();
        info.subscriberId("network-id");
        info.subscriptionId("8");
        info.filterUrl("/ietf-interfaces:interfaces");
        info.yangFilePath("/opt/yang");
        SvcLogicGraphInfo svcLogicGraphInfo = new SvcLogicGraphInfo();
        svcLogicGraphInfo.mode("sync");
        svcLogicGraphInfo.module("testModule");
        svcLogicGraphInfo.rpc("testRpc");
        svcLogicGraphInfo.version("1.0");
        info.callBackDG(svcLogicGraphInfo);
        assertThat(info.subscriberId(), is("network-id"));
        assertThat(info.subscriptionId(), is("8"));
        assertThat(info.filterUrl(), is("/ietf-interfaces:interfaces"));
        assertThat(info.yangFilePath(), is("/opt/yang"));
        assertThat(info.callBackDG().module(), is("testModule"));
        assertThat(info.callBackDG().mode(), is("sync"));
        assertThat(info.callBackDG().rpc(), is("testRpc"));
        assertThat(info.callBackDG().version(), is("1.0"));
    }
}
