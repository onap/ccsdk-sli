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

package org.onap.ccsdk.sli.adaptors.ssh.sshd;

import org.onap.ccsdk.sli.adaptors.ssh.SshAdaptor;
import org.onap.ccsdk.sli.adaptors.ssh.SshConnection;

public class SshAdaptorSshd implements SshAdaptor {

    //@Override
    public SshConnection getConnection(String host, int port, String username, String password) {
        return new SshConnectionSshd(host, port, username, password);
    }

    // @Override
    public SshConnection getConnection(String host, int port, String keyFile) {
        return new SshConnectionSshd(host, port, keyFile);
    }

}
