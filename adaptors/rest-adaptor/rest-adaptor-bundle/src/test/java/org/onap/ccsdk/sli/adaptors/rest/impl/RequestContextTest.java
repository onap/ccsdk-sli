/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */


package org.onap.ccsdk.sli.adaptors.rest.impl;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.rest.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the RequestContext object
 * <p>
 * The request context is used to track retries, recovery attempts, and time to live of the
 * processing of a request.
 * </p>
 */

public class RequestContextTest {

    private RequestContext rc;
    private Configuration config = ConfigurationFactory.getConfiguration();

    /**
     * Set up the test environment by forcing the retry delay and limit to small values for the test
     * and setting up the request context object.
     */
    @Before
    public void setup() {
        config.setProperty(Constants.PROPERTY_RETRY_DELAY, "1");
        config.setProperty(Constants.PROPERTY_RETRY_LIMIT, "3");
        rc = new RequestContext(null);
        rc.setTimeToLiveSeconds(2);
    }

    /**
     * Ensure that we set up the property correctly
     */
    @Test
    public void testRetryDelayProperty() {
        assertEquals(1, rc.getRetryDelay());
    }

    /**
     * Ensure that we set up the property correctly
     */
    @Test
    public void testRetryLimitProperty() {
        assertEquals(3, rc.getRetryLimit());
    }

    /**
     * This test ensures that the retry attempt counter is zero on a new context
     */
    @Test
    public void testRetryCountNoRetries() {
        assertEquals(0, rc.getAttempts());
    }

    /**
     * Test that the delay is accurate
     */
    @Test
    public void testDelay() {
        long future = System.currentTimeMillis() + (rc.getRetryDelay() * 1000L);

        rc.delay();

        assertTrue(System.currentTimeMillis() >= future);
    }

    /**
     * The RequestContext tracks the number of retry attempts against the limit.
     * This unannotated test verifies that the maximum number of retries can be attempted.
     * With argument testPastLimit set to true, it demonstrates going beyond the limit fails.
     */
    private void internalTestCanRetry(boolean testPastLimit) {
        assertEquals(0, rc.getAttempts());
        assertTrue(rc.attempt());
        assertFalse(rc.isFailed());
        assertEquals(1, rc.getAttempts());
        assertTrue(rc.attempt());
        assertFalse(rc.isFailed());
        assertEquals(2, rc.getAttempts());
        assertTrue(rc.attempt());
        assertFalse(rc.isFailed());
        assertEquals(3, rc.getAttempts());
        if (testPastLimit) {
            assertFalse(rc.attempt());
            assertTrue(rc.isFailed());
            assertEquals(3, rc.getAttempts());
            assertFalse(rc.attempt());
            assertTrue(rc.isFailed());
            assertEquals(3, rc.getAttempts());
        }
    }

    /**
     * The RequestContext tracks the number of retry attempts against the limit. This test verifies
     * that tracking logic works correctly.
     */
    @Test
    public void testCanRetry() {
        internalTestCanRetry(true);
    }

    /**
     * The same RequestContext is used throughout the processing, and retries need to be reset once
     * successfully connected so that any earlier (successful) recoveries are not considered when
     * performing any new future recoveries. This test ensures that a reset clears the retry counter
     * and that we can attempt retries again up to the limit.
     */
    @Test
    public void testResetAndCanRetry() {
        internalTestCanRetry(false);
        rc.reset();
        internalTestCanRetry(true);
    }

    /**
     * This test is used to test tracking of time to live for the request context. Because time is
     * inexact, the assertions can only be ranges of values, such as at least some value or greater.
     * The total duration tracking in the request context is only updated on each call to
     * {@link RequestContext#isAlive()}. Also, durations are NOT affected by calls to reset.
     */
    @Test
    public void testTimeToLive() {
        assertTrue(rc.getTotalDuration() == 0L);
        assertTrue(rc.isAlive());
        rc.reset();
        rc.delay();
        assertTrue(rc.isAlive());
        assertTrue(rc.getTotalDuration() >= 1000L);
        rc.reset();
        rc.delay();
        rc.isAlive();
        assertTrue(rc.getTotalDuration() >= 2000L);
        rc.reset();
        rc.delay();
        assertFalse(rc.isAlive());
        assertTrue(rc.getTotalDuration() >= 3000L);
    }

    /**
     * Demonstrate setSvcLogicContext/getSvcLogicContext work as expected
     */
    @Test
    public void testGetSvcLogicContext() {
        assertNull(rc.getSvcLogicContext());
        SvcLogicContext slc = new SvcLogicContext();
        rc.setSvcLogicContext(slc);
        assertEquals(slc, rc.getSvcLogicContext());
    }

}
