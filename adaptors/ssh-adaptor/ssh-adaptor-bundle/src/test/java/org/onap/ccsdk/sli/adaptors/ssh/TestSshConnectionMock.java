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

public class TestSshConnectionMock {

    private SshConnectionMock sshConnectionMock;
   @Before
    public void setUp() {
        sshConnectionMock=new SshConnectionMock("localhost", 8080, "myUser", "myPassword", "sampleKeyFile");
    }

    @Test
    public void testGetHost() {
        assertEquals("localhost", sshConnectionMock.getHost());
    }

    @Test
    public void testGetPort() {
        assertEquals(8080, sshConnectionMock.getPort());
    }

    @Test
    public void testGetUsername() {
        assertEquals("myUser", sshConnectionMock.getUsername());
    }

    @Test
    public void testGetPassword() {
        assertEquals("myPassword", sshConnectionMock.getPassword());
    }

    @Test
    public void testKeyFile() {
        assertEquals("sampleKeyFile", sshConnectionMock.getKeyFile());
    }

    @Test
    public void testGetReturnStderr() {
        sshConnectionMock.setReturnStderr("returnStderr");
        assertEquals("returnStderr", sshConnectionMock.getReturnStderr());
    }
    @Test
    public void testGetReturnStdout() {
        sshConnectionMock.setReturnStdout("returnStdout");
        assertEquals("returnStdout", sshConnectionMock.getReturnStdout());
    }
    @Test
    public void testGetReturnStatus() {
        sshConnectionMock.setReturnStatus(200);
        assertEquals(200, sshConnectionMock.getReturnStatus());
    }
    @Test
    public void testGetExecutedCommands() {
        sshConnectionMock.getExecutedCommands().add("cls");
        sshConnectionMock.getExecutedCommands().add("pwd");
        assertNotNull(sshConnectionMock.getExecutedCommands());
        assertFalse(sshConnectionMock.getExecutedCommands().isEmpty());
    }

    @Test
    public void testExecTimeout()
    {
        sshConnectionMock.setExecTimeout(20);
        assertEquals(20,sshConnectionMock.getExecTimeout());
    }

    @Test
    public void testConnectCallCount()
    {
        assertEquals(0, sshConnectionMock.getConnectCallCount());
    }

    @Test
    public void testDisconnectCallCount()
    {
        assertEquals(0, sshConnectionMock.getDisconnectCallCount());
    }

}
