/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright 2018 TechMahindra
 *=================================================================================
 * Modifications Copyright 2019 IBM.
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

package org.onap.ccsdk.sli.adaptors.rest.impl;

import com.att.cdp.zones.model.Server;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

public class RequestFailedExceptionTest {

    @Test
    public void testConstructorNoArgument() throws Exception {
        RequestFailedException requestFailedException = new RequestFailedException();
        Assert.assertTrue(requestFailedException.getCause() == null);
        Assert.assertTrue(requestFailedException.getLocalizedMessage() == null);
        Assert.assertTrue(requestFailedException.getMessage() == null);
    }

    @Test
    public void testConstructorWithMessage() throws Exception {
        String message = "testing message";
        RequestFailedException requestFailedException = new RequestFailedException(message);
        Assert.assertTrue(requestFailedException.getCause() == null);
        Assert.assertEquals(message, requestFailedException.getLocalizedMessage());
        Assert.assertEquals(message, requestFailedException.getMessage());
    }

    @Test
    public void testConstructor_And_GetterSetters() throws Exception {
        Server server = new Server();
        HttpStatus status = HttpStatus.ACCEPTED_202;
        String reason = "Success";
        String operation = "POST";
        RequestFailedException requestFailedException = new RequestFailedException(operation, reason, status, server);
        requestFailedException.setOperation(operation);
        requestFailedException.setReason(reason);
        requestFailedException.setServerId("A");
        requestFailedException.setStatus(status);
        Assert.assertEquals("POST", requestFailedException.getOperation());
        Assert.assertEquals("Success", requestFailedException.getReason());
        Assert.assertEquals("A", requestFailedException.getServerId());
        Assert.assertEquals(HttpStatus.ACCEPTED_202, requestFailedException.getStatus());
        Assert.assertEquals("A", requestFailedException.getServerId());
    }

    @Test
    public void testConstructorWithFiveArguements() throws Exception {
        String tMessage = "throwable message";
        Server server = new Server();
        HttpStatus status = HttpStatus.ACCEPTED_202;
        String reason = "Success";
        String operation = "POST";
        Throwable throwable = new Throwable(tMessage);
        RequestFailedException requestFailedException = new RequestFailedException(throwable, operation, reason, status,
                server);
        Assert.assertEquals(throwable, requestFailedException.getCause());

    }

    @Test
    public void testConstructorWithFiveArguements_server_Null() throws Exception {
        String tMessage = "throwable message";
        Server server = null;
        HttpStatus status = HttpStatus.ACCEPTED_202;
        String reason = "Success";
        String operation = "POST";
        Throwable throwable = new Throwable(tMessage);
        RequestFailedException requestFailedException = new RequestFailedException(throwable, operation, reason, status,
                server);
        Assert.assertEquals(throwable, requestFailedException.getCause());
    }

    @Test
    public void testConstructorWith_Server_Null() throws Exception {
        Server server = new Server();
        server.setId("testId");
        HttpStatus status = HttpStatus.ACCEPTED_202;
        String reason = "Success";
        String operation = "POST";
        RequestFailedException requestFailedException = new RequestFailedException(operation, reason, status, server);
        requestFailedException.setServer(server);
        Assert.assertEquals(server, requestFailedException.getServer());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        RequestFailedException requestFailedException = new RequestFailedException(message, throwable);
        Assert.assertEquals(throwable, requestFailedException.getCause());
        Assert.assertTrue(requestFailedException.getLocalizedMessage().contains(message));
        Assert.assertTrue(requestFailedException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithFourArguements() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        RequestFailedException requestFailedException = new RequestFailedException(message, throwable, true, true);
        Assert.assertEquals(throwable, requestFailedException.getCause());
        Assert.assertTrue(requestFailedException.getLocalizedMessage().contains(message));
        Assert.assertTrue(requestFailedException.getMessage().contains(message));
    }

    @Test
    public void testConstructorWithThrowable() throws Exception {
        String message = "testing message";
        Throwable throwable = new Throwable(message);
        RequestFailedException requestFailedException = new RequestFailedException(throwable);
        Assert.assertEquals(throwable, requestFailedException.getCause());
        Assert.assertTrue(requestFailedException.getLocalizedMessage().contains(message));
        Assert.assertTrue(requestFailedException.getMessage().contains(message));
    }

}
