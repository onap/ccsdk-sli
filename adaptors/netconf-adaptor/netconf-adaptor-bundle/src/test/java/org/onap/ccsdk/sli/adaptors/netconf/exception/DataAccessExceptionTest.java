/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.ccsdk.sli.adaptors.netconf.exception;

import org.junit.Assert;
import org.junit.Test;

public class DataAccessExceptionTest {

    @Test
    public void testConstructorNoArgument() throws Exception {
        DataAccessException dataAccessException = new DataAccessException();
        Assert.assertTrue(dataAccessException.getCause() == null);
        Assert.assertTrue(dataAccessException.getLocalizedMessage() == null);
        Assert.assertTrue(dataAccessException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessage() throws Exception {
        String message = "testing message";
        DataAccessException dataAccessException = new DataAccessException(message);
        Assert.assertTrue(dataAccessException.getCause() == null);
        Assert.assertEquals(message, dataAccessException.getLocalizedMessage());
        Assert.assertEquals(message, dataAccessException.getMessage());
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        DataAccessException dataAccessException = new DataAccessException(throwable);
        Assert.assertEquals(throwable, dataAccessException.getCause());
        Assert.assertTrue(dataAccessException.getLocalizedMessage().contains(message));
        Assert.assertTrue(dataAccessException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        DataAccessException dataAccessException = new DataAccessException(message, throwable);
        Assert.assertEquals(throwable, dataAccessException.getCause());
        Assert.assertTrue(dataAccessException.getLocalizedMessage().contains(message));
        Assert.assertTrue(dataAccessException.getMessage().contains(message));
    }
}
