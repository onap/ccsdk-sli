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

/**
 * Holder to store information of subscription.
 */
public class SubscriptionInfo {
    private String subscriptionId;
    private String subscriberId;
    private SvcLogicGraphInfo callbackDG;
    private String yangFilePath;
    private String filterUrl;

    /**
     * Returns callback DG.
     *
     * @return callback DG
     */
    public SvcLogicGraphInfo callBackDG() {
        return callbackDG;
    }

    /**
     * Sets callback DG.
     *
     * @param callbackDg callback DG
     */
    public void callBackDG(SvcLogicGraphInfo callbackDg) {
        this.callbackDG = callbackDg;
    }

    /**
     * Returns YANG file path.
     *
     * @return YANG file path
     */
    public String yangFilePath() {
        return yangFilePath;
    }

    /**
     * Sets YANG file path.
     *
     * @param yangFilePath yang file path
     */
    public void yangFilePath(String yangFilePath) {
        this.yangFilePath = yangFilePath;
    }

    /**
     * Returns filter URL.
     *
     * @return filter URL
     */
    public String filterUrl() {
        return filterUrl;
    }

    /**
     * Sets filter URL.
     *
     * @param filterUrl filter URL
     */
    public void filterUrl(String filterUrl) {
        this.filterUrl = filterUrl;
    }

    /**
     * Returns subscription Id.
     *
     * @return subscription Id
     */
    public String subscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets subscription id.
     *
     * @param subscriptionId subscription id
     */
    public void subscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Returns subscription Id.
     *
     * @return subscription Id
     */
    public String subscriberId() {
        return subscriberId;
    }

    /**
     * Sets subscriber id.
     *
     * @param subscriberId subscriber id
     */
    public void subscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }
}
