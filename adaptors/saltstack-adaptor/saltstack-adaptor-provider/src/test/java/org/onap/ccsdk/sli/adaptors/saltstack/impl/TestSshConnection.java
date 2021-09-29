/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2021 Samsung Electronics. All rights reserved.
 * ================================================================================
 *
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.adapter.impl;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackResult;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SshConnection;


import java.io.FileOutputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

public class TestSshConnection {

private SshConnection sshConnection;


    @Before
    public void setup() throws IllegalArgumentException {
        String HostName = "localhost";
        int Port = 22;
        String User = "test";
        String Password = "test";
        sshConnection = new SshConnection(HostName, Port, User, Password);
    }


    @Test(expected=NullPointerException.class)
    public void reqConnect_exitStatusFailed() {
            sshConnection.setExecTimeout(10);
            sshConnection.connect();
    }

    @Test
    public void reqexecCommandWithPty_exitStatusFailed() {
        sshConnection.setExecTimeout(10);
        int outcome = 999;
        try {
            OutputStream res = new FileOutputStream("test.out");
            outcome = sshConnection.execCommandWithPty("ls",res);
            assertEquals(1,outcome);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(expected=NullPointerException.class)
    public void reqDisconnect_exitStatusFailed() {
        sshConnection.setExecTimeout(10);
        sshConnection.disconnect();
    }

    @Test
    public void reqexecCommand_exitStatusFailed() {
        sshConnection.setExecTimeout(10);
        int outcome=999;
        try {
            OutputStream res = new FileOutputStream("test.out");
            OutputStream resErr = new FileOutputStream("test.out");
            outcome = sshConnection.execCommand("ls",res, resErr);
            assertEquals(1,outcome);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

