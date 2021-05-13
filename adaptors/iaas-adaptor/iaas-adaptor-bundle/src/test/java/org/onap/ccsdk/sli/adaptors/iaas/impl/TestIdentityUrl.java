/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestIdentityUrl {

    private static String URL;

    @BeforeClass
    public static void before() {
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        URL = props.getProperty("");
    }

    /**
     * Test that we can parse and interpret valid URLs
     */
    @Test
    public void testValidURL1() {
        URL = "http://192.168.1.1:5000/v2.0/";
        IdentityURL idurl = IdentityURL.parseURL(URL);
        assertNotNull(idurl);
        assertTrue(idurl.getScheme().equals("http"));
        assertTrue(idurl.getHost().equals("192.168.1.1"));
        assertTrue(idurl.getPort().equals("5000"));
        assertNull(idurl.getPath());
        assertTrue(idurl.getVersion().equals("v2.0"));
        assertTrue(idurl.toString().equals("http://192.168.1.1:5000/v2.0"));
    }

    @Test
    public void testValidURL2() {
        URL = "https://192.168.1.1:5000/v3/";
        IdentityURL idurl = IdentityURL.parseURL(URL);
        assertNotNull(idurl);
        assertTrue(idurl.getScheme().equals("https"));
        assertTrue(idurl.getHost().equals("192.168.1.1"));
        assertTrue(idurl.getPort().equals("5000"));
        assertNull(idurl.getPath());
        assertTrue(idurl.getVersion().equals("v3"));
        assertTrue(idurl.toString().equals("https://192.168.1.1:5000/v3"));
    }

    @Test
    public void testValidURL3() {
        URL = "http://192.168.1.1/v2.0/";
        IdentityURL idurl = IdentityURL.parseURL(URL);
        assertNotNull(idurl);
        assertTrue(idurl.getScheme().equals("http"));
        assertTrue(idurl.getHost().equals("192.168.1.1"));
        assertNull(idurl.getPort());
        assertNull(idurl.getPath());
        assertTrue(idurl.getVersion().equals("v2.0"));
        System.out.println(idurl.toString());
        assertTrue(idurl.toString().equals("http://192.168.1.1/v2.0"));
    }

    @Test
    public void testValidURL4() {
        URL = "http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3";
        IdentityURL idurl = IdentityURL.parseURL(URL);
        assertNotNull(idurl);
        assertTrue(idurl.getScheme().equals("http"));
        assertTrue(idurl.getHost().equals("msb.onap.org"));
        assertTrue(idurl.getPort().equals("80"));
        assertTrue(idurl.getPath().equals("/api/multicloud/v0/cloudowner_region/identity"));
        assertTrue(idurl.getVersion().equals("v3"));
        assertTrue(idurl.toString().equals("http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/identity/v3"));
    }
}
