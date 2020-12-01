/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Modifications Copyright © 2018 IBM
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

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;

import java.util.Map;

import static org.onap.ccsdk.sli.plugins.restapicall.JsonParser.convertToProperties;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Processes the events from event queue and executes callback DG.
 */
class EventProcessor implements Runnable {

    private static final Logger log = getLogger(EventProcessor.class);
    private RestconfDiscoveryNode node;

    private static final String EVENT_SUBSCRIPTION_ID = "notification." +
            "push-change-update.subscription-id";

    public EventProcessor(RestconfDiscoveryNode node) {
        this.node = node;
    }

    @Override
    public void run() {
        while(true) {
            try {
                String payload = node.eventQueue().take();
                Map<String, String> param = convertToProperties(payload);
                String id = param.get(EVENT_SUBSCRIPTION_ID);
                SubscriptionInfo info = node.subscriptionInfoMap().get(id);
                if (info != null) {
                    SvcLogicContext ctx = setContext(param);
                    SvcLogicGraphInfo callbackDG = info.callBackDG();
                    callbackDG.executeGraph(ctx);
                }
            } catch (InterruptedException | SvcLogicException e) {
                log.error("Interrupted!", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private SvcLogicContext setContext(Map<String, String> param) {
        SvcLogicContext ctx = new SvcLogicContext();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            ctx.setAttribute(entry.getKey(), entry.getValue());
        }
        return ctx;
    }
}
