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

import java.io.Closeable;

/**
 * @param <T>
 *            The generic type we are caching
 */

public interface Destructor<T extends Closeable> {

    /**
     * Called to destroy the object when it is no longer being used by the pool
     *
     * @param obj
     *            The object to be destroyed
     * @param pool
     *            The pool that the object is being removed from
     */
    void destroy(T obj, Pool<T> pool);
}
