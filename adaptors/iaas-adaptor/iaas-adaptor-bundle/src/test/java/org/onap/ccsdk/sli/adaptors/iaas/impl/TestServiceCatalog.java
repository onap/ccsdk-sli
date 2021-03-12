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

import com.att.cdp.exceptions.ZoneException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * This class tests the service catalog against a known provider.
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class TestServiceCatalog {

    // Number
    private static int EXPECTED_REGIONS = 2;

    private ServiceCatalog catalog;


    @BeforeClass
    public static void before() {
        Properties props = ConfigurationFactory.getConfiguration().getProperties();

        EXPECTED_REGIONS = Integer.parseInt(props.getProperty("test.expected-regions", "2"));
    }

    /**
     * Setup the test environment by loading a new service catalog for each test
     *
     * @throws ZoneException
     */
    @Before
    public void setup() throws ZoneException {
        catalog = Mockito.mock(ServiceCatalog.class, CALLS_REAL_METHODS);
        Mockito.doCallRealMethod().when(catalog).trackRequest();
        catalog.rwLock = new ReentrantReadWriteLock();
        Set<String> testdata = new HashSet<>();
        testdata.add("RegionOne");
        catalog.regions = testdata;
    }

    /**
     * Ensure that we set up the Region property correctly
     */
    @Test
    public void testKnownRegions() {
        assertEquals(EXPECTED_REGIONS, catalog.getRegions().size());
    }

    /**
     * Ensure that we invoke the track request method
     */
    @Test
    public void testTrackRequest() {
    	catalog.trackRequest();
        verify(catalog,times(1)).trackRequest();
    }
}
