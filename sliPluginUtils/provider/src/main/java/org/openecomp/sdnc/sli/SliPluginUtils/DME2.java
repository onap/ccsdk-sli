/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 ONAP
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

package org.openecomp.sdnc.sli.SliPluginUtils;

import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A SvcLogicJavaPlugin that generates DME2 proxy urls using parameters from context memory.
 */
public class DME2 implements SvcLogicJavaPlugin {
    String aafUserName;
    String aafPassword;
    String envContext;
    String routeOffer;
    String[] proxyUrls;
    Integer index;
    String commonServiceVersion;
    String partner;

    private static final Logger LOG = LoggerFactory.getLogger(DME2.class);

    public void setPartner(String partner) {
        if (partner != null && partner.length() > 0) {
            this.partner = partner;
        }
    }

    public DME2(String aafUserName, String aafPassword, String envContext, String routeOffer, String[] proxyUrls, String commonServiceVersion) {
        this.aafUserName = aafUserName;
        this.aafPassword = aafPassword;
        this.envContext = envContext;
        this.routeOffer = routeOffer;
        this.proxyUrls = proxyUrls;
        this.index = 0;
        this.commonServiceVersion = commonServiceVersion;
    }

    // constructs a URL to contact the proxy which contacts a DME2 service
    public String constructUrl(String service, String version, String subContext) {
        StringBuilder sb = new StringBuilder();

        // The hostname is assigned in a round robin fashion
        sb.append(acquireHostName());
        sb.append("/service=" + service);

        //If the directedGraph passes an explicit version use that, if not use the commonServiceVersion found in the properties file
        if (version == null) {
            version = this.commonServiceVersion;
        }
        sb.append("/version=" + version);

        sb.append("/envContext=" + this.envContext);
        if (this.routeOffer != null && this.routeOffer.length() > 0) {
            sb.append("/routeOffer=" + this.routeOffer);
        }
        if (subContext != null && subContext.length() > 0) {
            sb.append("/subContext=" + subContext);
        }
        sb.append("?dme2.password=" + this.aafPassword);
        sb.append("&dme2.username=" + this.aafUserName);
        if (this.partner != null) {
            sb.append("&dme2.partner=" + this.partner);
        }
        sb.append("&dme2.allowhttpcode=true");
        return (sb.toString());
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
        SliPluginUtils.checkParameters(parameters, new String[] { "service", "outputPath" }, LOG);
        String completeProxyUrl = constructUrl(parameters.get("service"), parameters.get("version"), parameters.get("subContext"));
        ctx.setAttribute(parameters.get("outputPath"), completeProxyUrl);
    }

}
