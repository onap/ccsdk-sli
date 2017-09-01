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

package org.onap.ccsdk.sli.core.slipluginutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class Dme2Test {

    @Test
    public void createInstarUrl() {
        String instarUrl = "http://localhost:25055/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=TEST/routeOffer=DEFAULT/subContext=/enterpriseConnection/getEnterpriseConnectionDetails/v1?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        DME2 dme = new DME2("user@sample.com", "fake", "TEST", "DEFAULT", new String[] { "http://localhost:25055" }, "common");
        String constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(instarUrl, constructedUrl);
    }

    @Test
    public void createInstarUrlNoSubContext() {
        String instarUrl = "http://localhost:25055/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=TEST/routeOffer=DEFAULT?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        DME2 dme = new DME2("user@sample.com", "fake", "TEST", "DEFAULT", new String[] { "http://localhost:25055" }, "common");
        Map<String, String> parameters = new HashMap<String, String>();
        String constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", parameters.get(null));
        assertEquals(instarUrl, constructedUrl);
    }

    @Test
    public void testRoundRobin() {
        String[] proxyHostNames = new String[] { "http://one:25055", "http://two:25055", "http://three:25055" };
        String urlSuffix = "/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=TEST/routeOffer=DEFAULT/subContext=/enterpriseConnection/getEnterpriseConnectionDetails/v1?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        DME2 dme = new DME2("user@sample.com", "fake", "TEST", "DEFAULT", proxyHostNames, "common");
        String constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[0] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[1] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[2] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[0] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[1] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[2] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[0] + urlSuffix, constructedUrl);
    }

    @Test
    public void createDme2EndtoEnd() {
        SliPluginUtilsActivator activator = new SliPluginUtilsActivator();
        DME2 dme2 = activator.initDme2("src/test/resources/dme2.e2e.properties");
        assertEquals("user@sample.com", dme2.aafUserName);
        assertEquals("fake", dme2.aafPassword);
        assertEquals("UAT", dme2.envContext);
        assertEquals("UAT", dme2.routeOffer);
        Assert.assertArrayEquals("http://sample.com:25055,http://sample.com:25055".split(","), dme2.proxyUrls);
        assertEquals("1702.0", dme2.commonServiceVersion);
        assertEquals(null, dme2.partner);

        String constructedUrl = dme2.constructUrl("sample.com/restservices/instar/v1/assetSearch", null, "/mySubContext");
        assertNotNull(constructedUrl);
        System.out.println(constructedUrl);
    }

    @Test
    public void createDme2Prod() {
        SliPluginUtilsActivator activator = new SliPluginUtilsActivator();
        DME2 dme2 = activator.initDme2("src/test/resources/dme2.prod.properties");
        assertEquals("user@sample.com", dme2.aafUserName);
        assertEquals("fake", dme2.aafPassword);
        assertEquals("PROD", dme2.envContext);
        assertEquals("", dme2.routeOffer);
        Assert.assertArrayEquals("http://sample.com:25055,http://sample.com:25055".split(","), dme2.proxyUrls);
        assertEquals("1.0", dme2.commonServiceVersion);
        assertEquals("LPP_PROD", dme2.partner);

        String constructedUrl = dme2.constructUrl("sample.com/restservices/instar/v1/assetSearch", null, "/mySubContext");
        assertNotNull(constructedUrl);
        System.out.println(constructedUrl);
    }

}
