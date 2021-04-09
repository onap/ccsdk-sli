/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ssh;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SshConnectionMock implements SshConnection {

    private static final int DEF_SUCCESS_STATUS = 0;

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String keyFile;
    private long timeout;

    private int returnStatus = DEF_SUCCESS_STATUS;
    private String returnStdout;
    private String returnStderr;

    private int connectCallCount = 0;
    private int disconnectCallCount = 0;
    private final List<String> executedCommands = new ArrayList<>();

    public SshConnectionMock(String host, int port, String username, String password, String keyFile) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.keyFile = keyFile;
    }



    @Override
    public void connect() {
        connectCallCount++;
    }

    @Override
    public void connectWithRetry() {
        connectCallCount++;
    }

    @Override
    public void disconnect() {
        disconnectCallCount++;
    }

    @Override
    public int execCommand(String cmd, OutputStream out, OutputStream err) {
        return execCommand(cmd, out, err, false);
    }

    @Override
    public int execCommandWithPty(String cmd, OutputStream out) {
        return execCommand(cmd, out, out, true);
    }

    private int execCommand(String cmd, OutputStream out, OutputStream err, boolean usePty) {
        executedCommands.add(cmd);
        try {
            if((out != null) && (returnStdout != null)) {
                out.write(returnStdout.getBytes());
            }
        } catch(IOException e) {
            throw new RuntimeException("Error writing to stdout output stream", e);
        }
        try {
            if((err != null) && (returnStderr != null)) {
                err.write(returnStderr.getBytes());
            }
        } catch(IOException e) {
            throw new RuntimeException("Error writing to stderr output stream", e);
        }
        return returnStatus;
    }

    @Override
    public void setExecTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getExecTimeout() {
        return timeout;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

   public String getKeyFile() {
        return keyFile;
    }

    public int getConnectCallCount() {
        return connectCallCount;
    }

    public int getDisconnectCallCount() {
        return disconnectCallCount;
    }

    public List<String> getExecutedCommands() {
        return executedCommands;
    }

    public int getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(int returnStatus) {
        this.returnStatus = returnStatus;
    }

    public String getReturnStdout() {
        return returnStdout;
    }

    public void setReturnStdout(String returnStdout) {
        this.returnStdout = returnStdout;
    }

    public String getReturnStderr() {
        return returnStderr;
    }

    public void setReturnStderr(String returnStderr) {
        this.returnStderr = returnStderr;
    }
}
