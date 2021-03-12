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

package org.onap.ccsdk.sli.core.utils.pool;

/**
 * A pool exception is a specialization of checked exceptions that define various pool abnormal states or requests.
 *
 */
public class PoolException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * PoolException constructor
     */
    public PoolException() {
    }

    /**
     * PoolException constructor
     *
     * @param message
     *            The error message
     */
    public PoolException(String message) {
        super(message);
    }

    /**
     * PoolException constructor
     *
     * @param cause
     *            The cause of the exception
     */
    public PoolException(Throwable cause) {
        super(cause);
    }

    /**
     * PoolException constructor
     *
     * @param message
     *            The error message
     * @param cause
     *            The cause of the exception
     */
    public PoolException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * PoolException constructor
     *
     * @param message
     *            The error message
     * @param cause
     *            The cause of the exception
     * @param enableSuppression
     *            whether or not suppression is enabled or disabled
     * @param writableStackTrace
     *            whether or not the stack trace should be writable
     */
    public PoolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
