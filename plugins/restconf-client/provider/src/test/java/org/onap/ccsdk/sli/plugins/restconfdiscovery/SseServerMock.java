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

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;

@Path("events")
public class SseServerMock {

    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput getServerSentEvents() throws IOException {
        String data = "{" +
                "\"ietf-restconf:notification\" : {" +
                "  \"eventTime\" : \"2017-10-25T08:22:33.44Z\"," +
                "    \"ietf-yang-push:push-change-update\": {" +
                "\"subscription-id\":\"89\"," +
                "\"datastore-changes\": {" +
                "\"ietf-yang-patch:yang-patch\":{" +
                "\"patch-id\":\"1\"," +
                "\"edit\":[{" +
                "\"edit-id\":\"edit1\"," +
                "\"operation\":\"merge\"," +
                "\"target\":\"/ietf-interfaces:interfaces-state\"," +
                "\"value\": {" +
                "\"ietf-interfaces:interfaces-state\":{"+
                "\"interface\": {" +
                "\"name\":\"eth0\"," +
                "\"oper-status\":\"down\"," +
                "}" +
                "}" +
                "}" +
                "}]"+
                "}" +
                "}" +
                "}" +
                "}" +
                "}";
        final EventOutput result = new EventOutput();
        result.write(new OutboundEvent.Builder().data(String.class, data).build());
        result.close();
        return result;
    }
}
