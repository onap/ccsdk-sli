/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                      reserved.
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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SvcLogicJavaPlugin that generates DME2 proxy urls (for calling the DME2 ingress proxy) using
 * parameters from context memory.
 */
public class DME2 implements SvcLogicJavaPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(DME2.class);
    // the key for <code>proxyUrl</code>, which represents a CSV list of urls
    static final String PROXY_URL_KEY = "proxyUrl";
    static final String PROXY_URLS_VALUE_SEPARATOR = ",";
    static final String AAF_USERNAME_KEY = "aafUserName";
    static final String AAF_PASSWORD_KEY = "aafPassword";
    static final String ENV_CONTEXT_KEY = "envContext";
    static final String ROUTE_OFFER_KEY = "routeOffer";
    static final String COMMON_SERVICE_VERSION_KEY = "commonServiceVersion";
    static final String PARTNER_KEY = "partner";
    static final String VERSION_KEY = "version";
    static final String SERVICE_KEY = "service";
    static final String SUBCONTEXT_KEY = "subContext";
    static final String ENDPOINT_READ_TIMEOUT_KEY = "endpointReadTimeout";
    static final String OUTPUT_PATH_KEY = "outputPath";

    final String aafUserName;
    final String aafPassword;
    final String envContext;
    final String routeOffer;
    final String[] proxyUrls;
    final String commonServiceVersion;
    final String partner;
    final String endpointReadTimeout;
    Integer index;

    public DME2(Properties properties) {
        Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Object, Object> entry = it.next();
            if (entry.getValue() == null || entry.getValue().toString().length() < 1) {
                it.remove();
            }
        }
        this.aafUserName = properties.getProperty(AAF_USERNAME_KEY, null);
        this.aafPassword = properties.getProperty(AAF_PASSWORD_KEY, null);
        this.envContext = properties.getProperty(ENV_CONTEXT_KEY, null);
        this.routeOffer = properties.getProperty(ROUTE_OFFER_KEY, null);
        this.commonServiceVersion = properties.getProperty(COMMON_SERVICE_VERSION_KEY, null);
        this.partner = properties.getProperty(PARTNER_KEY, null);
        this.endpointReadTimeout = properties.getProperty(ENDPOINT_READ_TIMEOUT_KEY, null);
        String proxyUrlString = properties.getProperty(PROXY_URL_KEY, null);
        if (proxyUrlString != null && proxyUrlString.length() > 0) {
            this.proxyUrls = proxyUrlString.split(PROXY_URLS_VALUE_SEPARATOR);
        } else {
            String[] local = {"http://localhost:5000"};
            this.proxyUrls = local;
        }
        this.index = 0;
    }

    // constructs a URL to contact the proxy which contacts a DME2 service
    public String constructUrl(Map<String, String> parameters) {
        StringBuilder sb = new StringBuilder();

        // The hostname is assigned in a round robin fashion
        sb.append(acquireHostName());
        sb.append("/service=" + parameters.get(SERVICE_KEY));

        // If the directedGraph passes an explicit version use that, if not use the commonServiceVersion
        // found in the properties file
        String version = parameters.getOrDefault(VERSION_KEY, this.commonServiceVersion);
        sb.append("/version=" + version);
        String envContext = parameters.getOrDefault(ENV_CONTEXT_KEY, this.envContext);
        sb.append("/envContext=" + envContext);
        String routeOffer = parameters.getOrDefault(ROUTE_OFFER_KEY, this.routeOffer);
        sb.append("/routeOffer=" + routeOffer);

        String subContext = parameters.get(SUBCONTEXT_KEY);
        if (subContext != null && subContext.length() > 0) {
            sb.append("/subContext=" + subContext);
        }
        sb.append("?dme2.password=" + this.aafPassword);
        sb.append("&dme2.username=" + this.aafUserName);
        if (this.partner != null) {
            sb.append("&partner=" + this.partner);
        }
        sb.append("&dme2.allowhttpcode=true");
        String endpointReadTimeout = parameters.getOrDefault(ENDPOINT_READ_TIMEOUT_KEY, this.endpointReadTimeout);
        if (endpointReadTimeout != null) {
            sb.append("&dme2.endpointReadTimeout=" + endpointReadTimeout);
        }
        String incompleteUrl = sb.toString();

        // Support optional parameters in a flexible way
        for (Entry<String, String> param : parameters.entrySet()) {
            if (!incompleteUrl.contains(param.getKey() + "=") && param.getValue() != null
                    && param.getValue().length() > 0 && !OUTPUT_PATH_KEY.equals(param.getKey())) {
                sb.append("&" + param.getKey() + "=" + param.getValue());
            }
        }
        return sb.toString();
    }

    public synchronized String acquireHostName() {
        String retVal = proxyUrls[index];
        index++;
        if (index == this.proxyUrls.length) {
            index = 0;
        }
        return retVal;
    }

    // Node entry point
    public void constructUrl(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        SliPluginUtils.checkParameters(parameters, new String[] {SERVICE_KEY, OUTPUT_PATH_KEY}, LOG);
        String completeProxyUrl = constructUrl(parameters);
        ctx.setAttribute(parameters.get(OUTPUT_PATH_KEY), completeProxyUrl);
    }

}
