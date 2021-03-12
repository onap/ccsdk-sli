/*
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

import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;

/**
 * This class is used to track and maintain recovery and time-to-live information for a request as it is being
 * processed.
 */
public class RequestContext {
    /**
     * The number of seconds of wait time between successive attempts to connect to the provider. This is used to
     * recover from provider outages or failures. It is not used to recover from logical errors, such as an invalid
     * request, server not found, etc.
     */
    private Integer retryDelay;

    /**
     * The number of times we will attempt to connect to the provider. This is used to recover from provider outages or
     * failures. It is not used to recover from logical errors, such as an invalid request, server not found, etc.
     */
    private Integer retryLimit;

    /**
     * The total time, in milliseconds, that the provider can have to process this request. If the accumulated time
     * exceeds the time to live, then the request is failed with a timeout exception, regardless of the state of the
     * provider. Note that the caller may supply this as a value in seconds, in which case it must be converted to
     * milliseconds for the request context.
     */
    private Long timeToLive;

    /**
     * The accumulated time, in milliseconds, that has been used so far to process the request. This is compared to the
     * time to live each time it is updated. If the accumulated time exceeds the time to live, then the request is
     * failed with a timeout exception, regardless of the state of the provider.
     */
    private long accumulatedTime;

    /**
     * The total number of retries attempted so far
     */
    private int attempt;

    /**
     * The time when the stopwatch was started
     */
    private long startTime = -1;

    /**
     * The service logic (DG) context from the SLI
     */
    private SvcLogicContext svcLogicContext;

    /**
     * The configuration
     */
    private Configuration configuration = ConfigurationFactory.getConfiguration();

    /**
     * Set to true whenever the retry limit has been exceeded, reset to false when reset() is called.
     */
    private boolean retryFailed;

    /**
     * Creates the request context
     *
     * @param context The service logic (SLI) context associated with the current DG
     */
    public RequestContext(SvcLogicContext context) {
        setSvcLogicContext(context);
    }

    /**
     * @return The retry delay, in seconds. If zero, then no retry is to be performed
     */
    public int getRetryDelay() {
        if (retryDelay == null) {
            int value = configuration.getIntegerProperty(Constants.PROPERTY_RETRY_DELAY);
            retryDelay = Integer.valueOf(value);
        }

        return retryDelay.intValue();
    }

    /**
     * This method is a helper that allows the caller to delay for the retry interval time and not have to handle the
     * thread interruption, timer handling, etc.
     */
    public void delay() {
        long time = getRetryDelay() * 1000L;
        long future = System.currentTimeMillis() + time;
        if (time != 0) {
            while (System.currentTimeMillis() < future && time > 0) {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    /*
                     * This is rare, but it can happen if another thread interrupts us while we are sleeping. In that
                     * case, the thread is resumed before the delay time has actually expired, so re-calculate the
                     * amount of delay time needed and reenter the sleep until we get to the future time.
                     */
                    time = future - System.currentTimeMillis();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * @return The number of retries that are allowed per connection
     */
    public int getRetryLimit() {
        if (retryLimit == null) {
            int value = configuration.getIntegerProperty(Constants.PROPERTY_RETRY_LIMIT);
            retryLimit = Integer.valueOf(value);
        }

        return retryLimit.intValue();
    }

    /**
     * Check and count the connection attempt.
     *
     * @return True if the connection should be attempted. False indicates that the number of retries has been exhausted
     *         and it should NOT be attempted.
     */
    public boolean attempt() {
        if (retryFailed || attempt >= getRetryLimit()) {
            retryFailed = true;
            return false;
        }
        attempt++;

        return true;
    }

    /**
     * @return The number of retry attempts so far
     */
    public int getAttempts() {
        return attempt;
    }

    /**
     * @return True if the retry limit has been exceeded, false otherwise
     */
    public boolean isFailed() {
        return retryFailed;
    }

    /**
     * This method both checks the time to live to see if it has been exceeded and accumulates the total time used so
     * far.
     * <p>
     * Each time this method is called it accumulates the total duration since the last time it was called to the total
     * time accumulator. It then checks the total time to the time to live and if greater, it returns false. As long as
     * the total time used is less than or equal to the time to live limit, the method returns true. It is important to
     * call this method at the very beginning of the process so that all parts of the process are tracked.
     * </p>
     *
     * @return True if the total time to live has not been exceeded. False indicates that the total time to live has
     *         been exceeded and no further processing should be performed.
     */
    public boolean isAlive() {
        long now = System.currentTimeMillis();
        if (startTime == -1) {
            startTime = now;
            return true;
        }
        accumulatedTime += (now - startTime);
        startTime = now;
        return accumulatedTime <= timeToLive;
    }

    /**
     * @return The total amount of time used, in milliseconds.
     */
    public long getTotalDuration() {
        return accumulatedTime;
    }

    /**
     * This method is called to reset the retry counters. It has no effect on the time to live accumulator.
     */
    public void reset() {
        attempt = 0;
    }

    /**
     * Sets the time to live to the value, expressed in seconds
     *
     * @param time The time to live, in seconds
     */
    public void setTimeToLiveSeconds(int time) {
        setTimeToLiveMS(time * 1000L);
    }

    /**
     * Sets the time to live to the value, expressed in milliseconds
     *
     * @param time The time to live, in milliseconds
     */
    public void setTimeToLiveMS(long time) {
        this.timeToLive = time;
    }

    /**
     * @return The service logic context associated with this request
     */
    public SvcLogicContext getSvcLogicContext() {
        return svcLogicContext;
    }

    /**
     * @param svcLogicContext The service logic context to be associated with this request
     */
    public void setSvcLogicContext(SvcLogicContext svcLogicContext) {
        this.svcLogicContext = svcLogicContext;
    }
}
