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
package org.onap.ccsdk.sli.core.utils.pool;

import org.junit.Assert;
import org.junit.Test;

public class PoolExceptionTest {

    @Test
    public void testPoolException() {
        PoolException poolException = new PoolException();
        Assert.assertTrue(poolException.getCause() == null);
        Assert.assertTrue(poolException.getMessage() == null);
    }

    @Test
    public void testPoolExceptionString() {
        String message = "test message";
        PoolException poolException = new PoolException(message);
        Assert.assertEquals(message, poolException.getMessage());
        Assert.assertEquals(message, poolException.getLocalizedMessage());
    }

    @Test
    public void testPoolExceptionThrowable() {
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        PoolException poolException = new PoolException(throwable);
        Assert.assertEquals(throwable, poolException.getCause());
    }

    @Test
    public void testPoolExceptionStringThrowable() {
        String message = "my test message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        PoolException poolException = new PoolException(message, throwable);
        Assert.assertEquals(throwable, poolException.getCause());
        Assert.assertTrue(poolException.getMessage().contains(message));
        Assert.assertEquals(message, poolException.getLocalizedMessage());
    }

    @Test
    public void testPoolExceptionStringThrowableBooleanBoolean() {
        String message = "my test message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        PoolException poolException = new PoolException(message, throwable, true, true);
        Assert.assertEquals(throwable, poolException.getCause());
        Assert.assertTrue(poolException.getMessage().contains(message));
        Assert.assertEquals(message, poolException.getLocalizedMessage());
    }

}
