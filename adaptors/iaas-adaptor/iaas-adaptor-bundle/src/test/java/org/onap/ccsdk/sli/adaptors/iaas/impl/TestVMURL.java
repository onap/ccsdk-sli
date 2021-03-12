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
import org.junit.Ignore;
import org.junit.Test;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
/**
 * This class is used to test methods and functions of the VMURL
 */
@Ignore
public class TestVMURL {

    private static String IP;
    private static String PORT;
    private static String TENANTID;
    private static String VMID;
    private static String URL;
    private static String VERSION;

    @BeforeClass
    public static void before() {
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        IP = props.getProperty("test.ip");
        PORT = props.getProperty("test.port");
        TENANTID = props.getProperty("test.tenantid");
        VMID = props.getProperty("test.vmid");
        VERSION = props.getProperty("test.version");
    }

    /**
     * Test that we can parse and interpret valid URLs
     */
    @Test
    public void testValidURL1() {
        URL = String.format("http://%s:%s/v2/%s/servers/%s", IP, PORT, TENANTID, VMID);
        VMURL url = VMURL.parseURL(URL);

        assertEquals("http", url.getScheme());
        assertEquals(IP, url.getHost());
        assertEquals(PORT, url.getPort());
        assertEquals(TENANTID, url.getTenantId());
        assertEquals(VMID, url.getServerId());
        assertEquals(VERSION, url.getVersion());
        assertEquals(url.toString(), URL);
    }

    @Test
    public void testValidURL2() {
        URL = String.format("http://%s/v2/%s/servers/%s", IP, TENANTID, VMID);
        VMURL url = VMURL.parseURL(URL);
        assertEquals("http", url.getScheme());
        assertEquals(IP, url.getHost());
        assertNull(url.getPort());
        assertNull(url.getPath());
        assertEquals(TENANTID, url.getTenantId());
        assertEquals(VMID, url.getServerId());
        assertEquals(VERSION, url.getVersion());
        assertEquals(url.toString(), URL);
    }

    @Test
    public void testValidURL3() {
        URL = "http://msb.onap.org:80/api/multicloud/v0/cloudowner_region/v2/abcde12345fghijk6789lmnopq123rst/servers/abc12345-1234-5678-890a-abcdefg12345";
        VMURL url = VMURL.parseURL(URL);
        assertNotNull(url);
        assertEquals("http", url.getScheme());
        assertEquals("msb.onap.org", url.getHost());
        assertEquals("80", url.getPort());
        assertEquals("/api/multicloud/v0/cloudowner_region", url.getPath());
        assertEquals(TENANTID, url.getTenantId());
        assertEquals(VMID, url.getServerId());
        assertEquals(url.toString(), URL);
    }

    /**
     * Test that we ignore and return null for invalid URLs
     */
    @Test
    public void testInvalidURLs() {
        VMURL url = VMURL.parseURL(null);
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/v2/%s/servers/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("http:/%s:%s/v2/%s/servers/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("http:///%s:%s/v2/%s/servers/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("http://v2/%s/servers/%s", TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/%s/servers/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/v2/servers/%s", IP, PORT, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/v2/%s/%s", IP, PORT, TENANTID, VMID));
        assertNull(url);

        url = VMURL.parseURL(String.format("%s:%s/v2/%s/servers", IP, PORT, TENANTID));
        assertNull(url);
    }
}
