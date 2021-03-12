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

import java.io.OutputStream;

/**
 * Provides utility method(s) to call commands on remote host via SSH.
 */
public interface SshConnection {

	/**
	 * Connect to SSH server.
	 */
	void connect();

	/**
	 * Connect to SSH Server using a retry mechanism
	 */
	void connectWithRetry();

	/**
	 * Disconnect from SSH server.
	 */
	void disconnect();

	/**
	 * Exec remote command over SSH. Return command execution status.
	 * Command output is written to out or err stream.
	 *
	 * @param cmd command to execute
	 * @param out content of sysout will go to this stream
	 * @param err content of syserr will go to this stream
	 * @return command execution status
	 */
	int execCommand(String cmd, OutputStream out, OutputStream err);

	/**
	 * Exec remote command over SSH with pseudo-tty. Return command execution status.
	 * Command output is written to out stream only as pseudo-tty writes to one stream only.
	 *
	 * @param cmd command to execute
	 * @param out content of sysout will go to this stream
	 * @return command execution status
	 */
	int execCommandWithPty(String cmd, OutputStream out);

	/**
	 * Set the command execution timeout
	 * @param timeout time in milliseconds
     */
	void setExecTimeout(long timeout);
}
