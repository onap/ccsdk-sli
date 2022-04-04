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
package org.onap.ccsdk.sli.adaptors.ssh;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestSshAdaptorMock {
    private SshAdaptorMock sshAdaptorMock;

    @Before
    public void setUp() {
        sshAdaptorMock = new SshAdaptorMock();
    }

    @Test
    public void testGetReturnStatus() {
        sshAdaptorMock.setReturnStatus(200);
        assertEquals(sshAdaptorMock.getReturnStatus(), 200);
    }

    @Test
    public void testGetReturnStdout() {
        sshAdaptorMock.setReturnStdout("success");
        assertNotNull(sshAdaptorMock.getReturnStdout());
        assertEquals(sshAdaptorMock.getReturnStdout(), "success");
    }

    @Test
    public void testGetReturnStderr() {
        sshAdaptorMock.setReturnStderr("error");
        assertNotNull(sshAdaptorMock.getReturnStderr());
        assertEquals(sshAdaptorMock.getReturnStderr(), "error");
    }

    @Test
    public void testGetConnectionMocks() {
        sshAdaptorMock.setReturnStatus(200);
        sshAdaptorMock.setReturnStdout("success");
        sshAdaptorMock.setReturnStderr("error");
        sshAdaptorMock.getConnection("localhost", 8080, "myUser", "myPassword");
        assertFalse(sshAdaptorMock.getConnectionMocks().isEmpty());
        assertNotNull(sshAdaptorMock.getConnectionMocks());
    }

    @Test
    public void testGetConnection() {
        SshAdaptorMock sshAdaptorMock = new SshAdaptorMock();
        sshAdaptorMock.setReturnStatus(200);
        sshAdaptorMock.setReturnStdout("success");
        sshAdaptorMock.setReturnStderr("error");
        sshAdaptorMock.getConnection("localhost", 8080, "keyFile");
        assertFalse(sshAdaptorMock.getConnectionMocks().isEmpty());
    }

    @Test
    public void testGetConnectionFailureCaseBasicAuth() {
        sshAdaptorMock.setReturnStatus(400);
        sshAdaptorMock.setReturnStdout("Bad Request");
        sshAdaptorMock.setReturnStderr("error");
        sshAdaptorMock.getConnection("localhost", 8080, "myUser", "myPassword");
        assertFalse(sshAdaptorMock.getConnectionMocks().isEmpty());
        assertNotNull(sshAdaptorMock.getConnectionMocks());
    }

    @Test
    public void testGetConnectionFailureCaseKey() {
        SshAdaptorMock sshAdaptorMock = new SshAdaptorMock();
        sshAdaptorMock.setReturnStatus(400);
        sshAdaptorMock.setReturnStdout("Bad Request");
        sshAdaptorMock.setReturnStderr("error");
        sshAdaptorMock.getConnection("localhost", 8080, "keyFile");
        assertFalse(sshAdaptorMock.getConnectionMocks().isEmpty());
    }
}
