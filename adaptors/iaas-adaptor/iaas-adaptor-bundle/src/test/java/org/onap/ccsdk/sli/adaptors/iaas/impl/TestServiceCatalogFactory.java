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
import org.junit.Assert;
import org.junit.Test;

/**
 * This class tests the service catalog factory against a known provider.
 */
public class TestServiceCatalogFactory {

    @Test
    public void testGetServiceCatalogV2() {
        String tenantIdentifier = null;
        String principal = null;
        String credential = null;
        String domain = null;
        Properties properties = null;

        String url = "http://192.168.1.1:5000/v2.0/";
        ServiceCatalog catalog = ServiceCatalogFactory.getServiceCatalog(url, tenantIdentifier, principal, credential,
                domain, properties);
        Assert.assertNotNull(catalog);
        Assert.assertEquals(catalog.getClass(), ServiceCatalogV2.class);

        url = "http://192.168.1.1:5000/v2/";
        catalog = ServiceCatalogFactory.getServiceCatalog(url, tenantIdentifier, principal, credential, domain,
                properties);
        Assert.assertNotNull(catalog);
        Assert.assertEquals(catalog.getClass(), ServiceCatalogV2.class);

        url = "http://192.168.1.1:5000/v2.1/";
        catalog = ServiceCatalogFactory.getServiceCatalog(url, tenantIdentifier, principal, credential, domain,
                properties);
        Assert.assertNotNull(catalog);
        Assert.assertEquals(catalog.getClass(), ServiceCatalogV2.class);

    }

    @Test
    public void testGetServiceCatalogV3() {
        String url = "http://192.168.1.1:5000/v3.0/";
        String tenantIdentifier = null;
        String principal = null;
        String credential = null;
        String domain = null;
        Properties properties = null;
        ServiceCatalog catalog = ServiceCatalogFactory.getServiceCatalog(url, tenantIdentifier, principal, credential,
                domain, properties);

        Assert.assertNotNull(catalog);
        Assert.assertEquals(catalog.getClass(), ServiceCatalogV3.class);
    }

    @Test
    public void testGetServiceCatalogOther() {
        String url = "http://192.168.1.1:5000/v4.0/";
        String tenantIdentifier = null;
        String principal = null;
        String credential = null;
        String domain = null;
        Properties properties = null;
        ServiceCatalog catalog = ServiceCatalogFactory.getServiceCatalog(url, tenantIdentifier, principal, credential,
                domain, properties);

        Assert.assertNull(catalog);
    }

    @Test
    public void testGetServiceCatalogEmptyURL() {
        String url = null;
        String tenantIdentifier = null;
        String principal = null;
        String credential = null;
        String domain = null;
        Properties properties = null;
        ServiceCatalog catalog = ServiceCatalogFactory.getServiceCatalog(url, tenantIdentifier, principal, credential,
                domain, properties);

        Assert.assertNull(catalog);
    }

    @Test
    public void testGetServiceCatalogWithoutVersion() {
        String url = "http://192.168.1.1:5000/";
        String tenantIdentifier = null;
        String principal = null;
        String credential = null;
        String domain = null;
        Properties properties = null;
        ServiceCatalog catalog = ServiceCatalogFactory.getServiceCatalog(url, tenantIdentifier, principal, credential,
                domain, properties);

        Assert.assertNull(catalog);
    }

}
