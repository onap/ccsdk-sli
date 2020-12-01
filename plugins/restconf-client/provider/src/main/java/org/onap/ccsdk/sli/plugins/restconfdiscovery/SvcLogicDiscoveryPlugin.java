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

import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

/**
 * Abstraction of a plugin to enable discovery from DG.
 */
public interface SvcLogicDiscoveryPlugin extends SvcLogicJavaPlugin {

    /**
     * Allows directed graphs to establish a discovery subscription for a given subscriber.
     * @param paramMap HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th><th>example values</th></thead>
     *  <tbody>
     *      <tr><td>templateDirName</td><td>Optional</td><td>full path to YANG directory that can be used to build a request</td><td>/sdncopt/bvc/resconfapi/test</td></tr>
     *      <tr><td>establishSubscriptionURL</td><td>Mandatory</td><td>url to establish connection with server</td><td>https://127.0.0.1:8181/restconf/operations/ietf-subscribed-notifications:establish-subscription</td></tr>
     *      <tr><td>sseConnectURL</td><td>Mandatory</td><td>url to setup SSE connection with server</td><td>https://127.0.0.1:8181/restconf/streams/yang-push-json</td></tr>
     *      <tr><td>callbackDG</td><td>Mandatory</td><td>callback DG to process the received notification</td><td>Resource-Discovery:handleSOTNTopology</td></tr>
     *      <tr><td>filterURL</td><td>Optional</td><td>url which needs to be subscribed, if null subscribe to all</td><td>http://example.com/sample-data/1.0</td></tr>
     *      <tr><td>subscriptionType</td><td>Optional</td><td>type of subscription, periodic or onDataChange</td><td>onDataChange</td></tr>
     *      <tr><td>updateFrequency</td><td>Optional</td><td>update frequency in milli seconds when subscription type is periodic</td><td>1000</td></tr>
     *      <tr><td>restapiUser</td><td>Optional</td><td>user name to use for http basic authentication</td><td>sdnc_ws</td></tr>
     *      <tr><td>restapiPassword</td><td>Optional</td><td>unencrypted password to use for http basic authentication</td><td>plain_password</td></tr>
     *      <tr><td>contentType</td><td>Optional</td><td>http content type to set in the http header</td><td>usually application/json or application/xml</td></tr>
     *      <tr><td>format</td><td>Optional</td><td>should match request body format</td><td>json or xml</td></tr>
     *      <tr><td>responsePrefix</td><td>Optional</td><td>location the notification response will be written to in context memory</td><td>tmp.restconfdiscovery.result</td></tr>
     *      <tr><td>skipSending</td><td>Optional</td><td></td><td>true or false</td></tr>
     *      <tr><td>convertResponse </td><td>Optional</td><td>whether the response should be converted</td><td>true or false</td></tr>
     *      <tr><td>customHttpHeaders</td><td>Optional</td><td>a list additional http headers to be passed in, follow the format in the example</td><td>X-CSI-MessageId=messageId,headerFieldName=headerFieldValue</td></tr>
     *      <tr><td>dumpHeaders</td><td>Optional</td><td>when true writes http header content to context memory</td><td>true or false</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     * @see String#split(String, int)
     */
    void establishSubscription(Map<String, String> paramMap, SvcLogicContext ctx) throws SvcLogicException;

    /**
     * Allows directed graphs to modify a discovery subscription for a given subscriber.
     * @param paramMap HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th><th>example values</th></thead>
     *  <tbody>
     *      <tr><td>subscriberId</td><td>Mandatory</td><td>subscription subscriber's identifier</td><td>topologyId/1111</td></tr>
     *      <tr><td>templateDirName</td><td>Optional</td><td>full path to YANG directory that can be used to build a request</td><td>/sdncopt/bvc/resconfapi/test</td></tr>
     *      <tr><td>establishSubscriptionURL</td><td>Mandatory</td><td>url to establish connection with server</td><td>https://127.0.0.1:8181/restconf/operations/ietf-subscribed-notifications:establish-subscription</td></tr>
     *      <tr><td>sseConnectURL</td><td>Mandatory</td><td>url to setup SSE connection with server</td><td>https://127.0.0.1:8181/restconf/streams/yang-push-json</td></tr>
     *      <tr><td>callbackDG</td><td>Mandatory</td><td>callback DG to process the received notification</td><td>Resource-Discovery:handleSOTNTopology</td></tr>
     *      <tr><td>filterURL</td><td>Optional</td><td>url filter list which needs to be subscribed, if null subscribe to all</td><td>http://example.com/sample-data/1.0</td></tr>
     *      <tr><td>subscriptionType</td><td>Optional</td><td>type of subscription, periodic or onDataChange</td><td>onDataChange</td></tr>
     *      <tr><td>updateFrequency</td><td>Optional</td><td>update frequency in milli seconds when subscription type is periodic</td><td>1000</td></tr>
     *      <tr><td>restapiUser</td><td>Optional</td><td>user name to use for http basic authentication</td><td>sdnc_ws</td></tr>
     *      <tr><td>restapiPassword</td><td>Optional</td><td>unencrypted password to use for http basic authentication</td><td>plain_password</td></tr>
     *      <tr><td>contentType</td><td>Optional</td><td>http content type to set in the http header</td><td>usually application/json or application/xml</td></tr>
     *      <tr><td>format</td><td>Optional</td><td>should match request body format</td><td>json or xml</td></tr>
     *      <tr><td>responsePrefix</td><td>Optional</td><td>location the notification response will be written to in context memory</td><td>tmp.restconfdiscovery.result</td></tr>
     *      <tr><td>skipSending</td><td>Optional</td><td></td><td>true or false</td></tr>
     *      <tr><td>convertResponse </td><td>Optional</td><td>whether the response should be converted</td><td>true or false</td></tr>
     *      <tr><td>customHttpHeaders</td><td>Optional</td><td>a list additional http headers to be passed in, follow the format in the example</td><td>X-CSI-MessageId=messageId,headerFieldName=headerFieldValue</td></tr>
     *      <tr><td>dumpHeaders</td><td>Optional</td><td>when true writes http header content to context memory</td><td>true or false</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     * @since 11.0.2
     * @see String#split(String, int)
     */
    void modifySubscription(Map<String, String> paramMap, SvcLogicContext ctx);

    /**
     * Allows directed graphs to delete the discovery subscription for a given subscriber.
     * @param paramMap HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     *  <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th><th>example values</th></thead>
     *  <tbody>
     *      <tr><td>subscriberId</td><td>Mandatory</td><td>subscription subscriber's identifier</td><td>topologyId/1111</td></tr>
     *  </tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     */
    void deleteSubscription(Map<String, String> paramMap, SvcLogicContext ctx);

}
