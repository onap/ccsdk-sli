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
package org.onap.ccsdk.sli.adaptors.iaas.impl;

import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Stack;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

/**
 * This class is used to test methods and functions of the Request Failed Exception class
 */
public class RequestFailedExceptionTest {

    @Test
    public void testRequestFailedException() {
        RequestFailedException requestFailedException = new RequestFailedException();
        Assert.assertNull(requestFailedException.getCause());
        Assert.assertNull(requestFailedException.getLocalizedMessage());
        Assert.assertNull(requestFailedException.getMessage());
    }

    @Test
    public void testRequestFailedExceptionString() {
        String message = "my test message";
        RequestFailedException requestFailedException = new RequestFailedException(message);
        Assert.assertNull(requestFailedException.getCause());
        Assert.assertEquals(message, requestFailedException.getLocalizedMessage());
        Assert.assertEquals(message, requestFailedException.getMessage());
    }

    @Test
    public void testRequestFailedExceptionStringStringHttpStatusServer() {
        Server server=new Server();
        server.setId("svrId");
        HttpStatus status=HttpStatus.OK_200;
        String reason="Success";
        String operation="POST";
        RequestFailedException requestFailedException = new RequestFailedException(operation, reason, status, server);
        requestFailedException.setOperation(operation);
        requestFailedException.setReason(reason);
        requestFailedException.setServerId("svrId");
        requestFailedException.setStatus(status);
        requestFailedException.setServer(server);
        Assert.assertEquals("POST",requestFailedException.getOperation());
        Assert.assertEquals("Success",requestFailedException.getReason());
        Assert.assertEquals("svrId",requestFailedException.getServerId());
        Assert.assertEquals( HttpStatus.OK_200,requestFailedException.getStatus());
        Assert.assertEquals(server, requestFailedException.getServer());
    }

    @Test
    public void testRequestFailedExceptionStringStringHttpStatusStack() {
        String operation="POST";
        String reason="Success";
        HttpStatus status=HttpStatus.OK_200;
        Stack stack = new Stack();
        RequestFailedException requestFailedException = new RequestFailedException(operation, reason, status, stack);
        requestFailedException.setOperation(operation);
        requestFailedException.setReason(reason);
        requestFailedException.setStatus(status);
        Assert.assertEquals("POST",requestFailedException.getOperation());
        Assert.assertEquals("Success",requestFailedException.getReason());
        Assert.assertEquals( HttpStatus.OK_200,requestFailedException.getStatus());
    }

    @Test
    public void testRequestFailedExceptionThrowableStringStringHttpStatusServer() {
        String tMessage = "throwable message";
        Server server=new Server();
        HttpStatus status=HttpStatus.ACCEPTED_202;
        String reason="Success";
        String operation="POST";
        Throwable throwable = new Throwable(tMessage);
        RequestFailedException requestFailedException = new RequestFailedException(throwable,operation,reason, status, server);
        Assert.assertEquals(throwable, requestFailedException.getCause());
    }

    @Test
    public void testRequestFailedExceptionStringThrowable() {
        String message = "my test message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        RequestFailedException requestFailedException = new RequestFailedException(message, throwable);
        Assert.assertEquals(throwable, requestFailedException.getCause());
        Assert.assertTrue(requestFailedException.getLocalizedMessage().contains(message));
        Assert.assertTrue(requestFailedException.getMessage().contains(message));
    }

    @Test
    public void testRequestFailedExceptionStringThrowableBooleanBoolean() {
        String message = "my test message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        RequestFailedException requestFailedException = new RequestFailedException(message, throwable, true, true);
        Assert.assertEquals(throwable, requestFailedException.getCause());
        Assert.assertTrue(requestFailedException.getLocalizedMessage().contains(message));
        Assert.assertTrue(requestFailedException.getMessage().contains(message));
    }

    @Test
    public void testRequestFailedExceptionThrowable() {
        String message = "my test message";
        Throwable throwable = new Throwable(message);
        RequestFailedException requestFailedException = new RequestFailedException(throwable);
        Assert.assertEquals(throwable, requestFailedException.getCause());
        Assert.assertTrue(requestFailedException.getLocalizedMessage().contains(message));
        Assert.assertTrue(requestFailedException.getMessage().contains(message));
    }

    /**
     * This test case is used to test the request failed exception class without server
     *
     */
    @Test
    public void testRequestFailedExceptionThrowableStringWithoutServer() {
        String tMessage = "throwable message";
        Server server = null;
        HttpStatus status = HttpStatus.ACCEPTED_202;
        String reason = "Success";
        String operation = "POST";
        Throwable throwable = new Throwable(tMessage);
        RequestFailedException requestFailedException = new RequestFailedException(throwable, operation, reason, status, server);
        Assert.assertEquals(throwable, requestFailedException.getCause());
        Assert.assertNull(requestFailedException.getServer());
    }
}
