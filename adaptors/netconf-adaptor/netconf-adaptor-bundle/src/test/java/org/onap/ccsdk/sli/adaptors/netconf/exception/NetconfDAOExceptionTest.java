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

public class NetconfDAOExceptionTest {

    @Test
    public void testConstructorNoArgument() throws Exception {
        NetconfDAOException netconfDAOException = new NetconfDAOException();
        Assert.assertTrue(netconfDAOException.getCause() == null);
        Assert.assertTrue(netconfDAOException.getLocalizedMessage() == null);
        Assert.assertTrue(netconfDAOException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessage() throws Exception {
        String message = "testing message";
        NetconfDAOException netconfDAOException = new NetconfDAOException(message);
        Assert.assertTrue(netconfDAOException.getCause() == null);
        Assert.assertEquals(message, netconfDAOException.getLocalizedMessage());
        Assert.assertEquals(message, netconfDAOException.getMessage());
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        NetconfDAOException netconfDAOException = new NetconfDAOException(throwable);
        Assert.assertEquals(throwable, netconfDAOException.getCause());
        Assert.assertTrue(netconfDAOException.getLocalizedMessage().contains(message));
        Assert.assertTrue(netconfDAOException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        NetconfDAOException netconfDAOException = new NetconfDAOException(message, throwable);
        Assert.assertEquals(throwable, netconfDAOException.getCause());
        Assert.assertTrue(netconfDAOException.getLocalizedMessage().contains(message));
        Assert.assertTrue(netconfDAOException.getMessage().contains(message));
    }
}
