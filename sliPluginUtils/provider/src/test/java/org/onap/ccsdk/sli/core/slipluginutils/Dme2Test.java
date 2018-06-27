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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

public class Dme2Test {
    public Properties makesProperties(String aafUserName, String aafPassword, String envContext, String routeOffer,
            String proxyUrls, String commonServiceVersion) {
        Properties props = new Properties();
        props.put(DME2.AAF_USERNAME_KEY, aafUserName);
        props.put(DME2.AAF_PASSWORD_KEY, aafPassword);
        props.put(DME2.ENV_CONTEXT_KEY, envContext);
        props.put(DME2.ROUTE_OFFER_KEY, routeOffer);
        props.put(DME2.PROXY_URL_KEY, proxyUrls);
        props.put(DME2.COMMON_SERVICE_VERSION_KEY, commonServiceVersion);
        return props;
    }

    @Test
    public void createUrl() {
        String localUrl =
                "http://localhost:25055/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=TEST/routeOffer=DEFAULT/subContext=/enterpriseConnection/getEnterpriseConnectionDetails/v1?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        Properties props =
                makesProperties("user@sample.com", "fake", "TEST", "DEFAULT", "http://localhost:25055", "common");
        DME2 dme = new DME2(props);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(DME2.SERVICE_KEY, "sample.com/services/eim/v1/rest");
        parameters.put(DME2.VERSION_KEY, "1702.0");
        parameters.put(DME2.SUBCONTEXT_KEY, "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        parameters.put(DME2.OUTPUT_PATH_KEY, "tmp.test");

        String constructedUrl = dme.constructUrl(parameters);
        assertEquals(localUrl, constructedUrl);
    }

    @Test
    public void createUrlNoSubContext() {
        String localUrl =
                "http://localhost:25055/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=TEST/routeOffer=DEFAULT?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        Properties props =
                makesProperties("user@sample.com", "fake", "TEST", "DEFAULT", "http://localhost:25055", "common");
        DME2 dme = new DME2(props);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(DME2.SERVICE_KEY, "sample.com/services/eim/v1/rest");
        parameters.put(DME2.VERSION_KEY, "1702.0");
        parameters.put(DME2.SUBCONTEXT_KEY, null);
        String constructedUrl = dme.constructUrl(parameters);
        assertEquals(localUrl, constructedUrl);
    }

    @Test
    public void testRoundRobin() {
        String[] proxyHostNames = new String[] {"http://one:25055", "http://two:25055", "http://three:25055"};
        String proxyHostNameString = proxyHostNames[0] + "," + proxyHostNames[1] + "," + proxyHostNames[2];

        String urlSuffix =
                "/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=TEST/routeOffer=DEFAULT/subContext=/enterpriseConnection/getEnterpriseConnectionDetails/v1?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        Properties props = makesProperties("user@sample.com", "fake", "TEST", "DEFAULT", proxyHostNameString, "common");
        DME2 dme = new DME2(props);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(DME2.SERVICE_KEY, "sample.com/services/eim/v1/rest");
        parameters.put(DME2.VERSION_KEY, "1702.0");
        parameters.put(DME2.SUBCONTEXT_KEY, "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        String constructedUrl = dme.constructUrl(parameters);
        assertEquals(proxyHostNames[0] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl(parameters);
        assertEquals(proxyHostNames[1] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl(parameters);
        assertEquals(proxyHostNames[2] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl(parameters);
        assertEquals(proxyHostNames[0] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl(parameters);
        assertEquals(proxyHostNames[1] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl(parameters);
        assertEquals(proxyHostNames[2] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl(parameters);
        assertEquals(proxyHostNames[0] + urlSuffix, constructedUrl);
    }

    @Test
    public void createDme2EndtoEnd() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream("src/test/resources/dme2.e2e.properties"));
        DME2 dme2 = new DME2(props);
        assertEquals("user@sample.com", dme2.aafUserName);
        assertEquals("fake", dme2.aafPassword);
        assertEquals("UAT", dme2.envContext);
        assertEquals("UAT", dme2.routeOffer);
        Assert.assertArrayEquals(
                "http://sample.com:25055,http://sample.com:25055".split(DME2.PROXY_URLS_VALUE_SEPARATOR),
                dme2.proxyUrls);
        assertEquals("1702.0", dme2.commonServiceVersion);
        assertEquals(null, dme2.partner);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(DME2.PARTNER_KEY, "I SHOULD BE FILTERED NOW!");
        parameters.put(DME2.SERVICE_KEY, "sample.com/restservices/sys/v1/assetSearch");
        parameters.put(DME2.VERSION_KEY, null);
        parameters.put(DME2.SUBCONTEXT_KEY, "/mySubContext");
        String constructedUrl = dme2.constructUrl(parameters);
        assertNotNull(constructedUrl);
        String expected =
                "http://sample.com:25055/service=sample.com/restservices/sys/v1/assetSearch/version=1702.0/envContext=UAT/routeOffer=UAT/subContext=/mySubContext?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        assertEquals(expected, constructedUrl);
    }

    @Test
    public void createDme2Prod() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream("src/test/resources/dme2.prod.properties"));
        DME2 dme2 = new DME2(props);
        assertEquals("user@sample.com", dme2.aafUserName);
        assertEquals("fake", dme2.aafPassword);
        assertEquals("PROD", dme2.envContext);
        assertEquals(null, dme2.routeOffer);
        Assert.assertArrayEquals(
                "http://sample.com:25055,http://sample.com:25055".split(DME2.PROXY_URLS_VALUE_SEPARATOR),
                dme2.proxyUrls);
        assertEquals("1.0", dme2.commonServiceVersion);
        assertEquals("LPP_PROD", dme2.partner);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(DME2.SERVICE_KEY, "sample.com/services/eim/v1/rest");
        parameters.put(DME2.VERSION_KEY, "1702.0");
        parameters.put(DME2.SUBCONTEXT_KEY, "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        String constructedUrl = dme2.constructUrl(parameters);
        assertNotNull(constructedUrl);
        String expected =
                "http://sample.com:25055/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=PROD/subContext=/enterpriseConnection/getEnterpriseConnectionDetails/v1?dme2.password=fake&dme2.username=user@sample.com&partner=LPP_PROD&dme2.allowhttpcode=true";
        assertEquals(expected, constructedUrl);
    }

    @Test
    public void blankProperties() throws Exception {
        Properties props = new Properties();
        DME2 dme2 = new DME2(props);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(DME2.SERVICE_KEY, "easyService");
        parameters.put(DME2.VERSION_KEY, "3");
        parameters.put(DME2.SUBCONTEXT_KEY, "/sub");
        assertEquals(
                "http://localhost:5000/service=easyService/version=3/envContext=null/subContext=/sub?dme2.password=null&dme2.username=null&dme2.allowhttpcode=true",
                dme2.constructUrl(parameters));
    }

    @Test
    public void optionalParameters() {
        String localUrl =
                "http://localhost:25055/service=serv/version=4/envContext=TEST/routeOffer=DEFAULT/subContext=/sub?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true&test=123";
        Properties props =
                makesProperties("user@sample.com", "fake", "TEST", "DEFAULT", "http://localhost:25055", "common");
        DME2 dme = new DME2(props);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(DME2.SERVICE_KEY, "serv");
        parameters.put(DME2.VERSION_KEY, "4");
        parameters.put(DME2.SUBCONTEXT_KEY, "/sub");
        parameters.put("test", "123");

        String constructedUrl = dme.constructUrl(parameters);
        assertEquals(localUrl, constructedUrl);
    }

    @Test
    public void createLocalUrlLegacy() {
        String localUrl =
                "http://localhost:25055/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=TEST/routeOffer=DEFAULT/subContext=/enterpriseConnection/getEnterpriseConnectionDetails/v1?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        Properties props =
                makesProperties("user@sample.com", "fake", "TEST", "DEFAULT", "http://localhost:25055", "common");
        DME2 dme = new DME2(props);
        String constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0",
                "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(localUrl, constructedUrl);
    }

    @Test
    public void createLocalUrlNoSubContextLegacy() {
        String localUrl =
                "http://localhost:25055/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=TEST/routeOffer=DEFAULT?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        Properties props =
                makesProperties("user@sample.com", "fake", "TEST", "DEFAULT", "http://localhost:25055", "common");
        DME2 dme = new DME2(props);
        Map<String, String> parameters = new HashMap<String, String>();
        String constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0", parameters.get(null));
        assertEquals(localUrl, constructedUrl);
    }

    @Test
    public void testRoundRobinLegacy() {
        String[] proxyHostNames = new String[] {"http://one:25055", "http://two:25055", "http://three:25055"};
        String urlSuffix =
                "/service=sample.com/services/eim/v1/rest/version=1702.0/envContext=TEST/routeOffer=DEFAULT/subContext=/enterpriseConnection/getEnterpriseConnectionDetails/v1?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        Properties props = makesProperties("user@sample.com", "fake", "TEST", "DEFAULT",
                "http://one:25055,http://two:25055,http://three:25055", "common");
        DME2 dme = new DME2(props);
        String constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0",
                "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[0] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0",
                "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[1] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0",
                "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[2] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0",
                "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[0] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0",
                "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[1] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0",
                "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[2] + urlSuffix, constructedUrl);
        constructedUrl = dme.constructUrl("sample.com/services/eim/v1/rest", "1702.0",
                "/enterpriseConnection/getEnterpriseConnectionDetails/v1");
        assertEquals(proxyHostNames[0] + urlSuffix, constructedUrl);
    }

    @Test
    public void createDme2EndtoEndLegacy() throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("src/test/resources/dme2.e2e.properties"));
        DME2 dme2 = new DME2(props);
        assertEquals("user@sample.com", dme2.aafUserName);
        assertEquals("fake", dme2.aafPassword);
        assertEquals("UAT", dme2.envContext);
        assertEquals("UAT", dme2.routeOffer);
        Assert.assertArrayEquals("http://sample.com:25055,http://sample.com:25055".split(","), dme2.proxyUrls);
        assertEquals("1702.0", dme2.commonServiceVersion);
        assertEquals(null, dme2.partner);
        String constructedUrl = dme2.constructUrl("sample.com/restservices/sys/v1/assetSearch", null, "/mySubContext");
        assertNotNull(constructedUrl);
        String expected =
                "http://sample.com:25055/service=sample.com/restservices/sys/v1/assetSearch/version=1702.0/envContext=UAT/routeOffer=UAT/subContext=/mySubContext?dme2.password=fake&dme2.username=user@sample.com&dme2.allowhttpcode=true";
        assertEquals(expected, constructedUrl);
    }

    @Test
    public void createDme2ProdLegacy() throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("src/test/resources/dme2.prod.properties"));
        DME2 dme2 = new DME2(props);
        assertEquals("user@sample.com", dme2.aafUserName);
        assertEquals("fake", dme2.aafPassword);
        assertEquals("PROD", dme2.envContext);
        assertEquals(null, dme2.routeOffer);
        Assert.assertArrayEquals("http://sample.com:25055,http://sample.com:25055".split(","), dme2.proxyUrls);
        assertEquals("1.0", dme2.commonServiceVersion);
        assertEquals("LPP_PROD", dme2.partner);
        String constructedUrl = dme2.constructUrl("sample.com/restservices/sys/v1/assetSearch", null, "/mySubContext");
        assertNotNull(constructedUrl);
        String expected =
                "http://sample.com:25055/service=sample.com/restservices/sys/v1/assetSearch/version=1.0/envContext=PROD/subContext=/mySubContext?dme2.password=fake&dme2.username=user@sample.com&partner=LPP_PROD&dme2.allowhttpcode=true";
        assertEquals(expected, constructedUrl);
    }

}
