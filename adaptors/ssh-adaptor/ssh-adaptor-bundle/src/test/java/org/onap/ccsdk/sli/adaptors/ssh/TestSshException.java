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

import org.junit.Assert;
import org.junit.Test;

public class TestSshException {

    @Test
    public void testConstructorWithMessaqge() throws Exception {
        String message = "testing message";
        SshException sshException = new SshException(message);
        Assert.assertNull(sshException.getCause());
        Assert.assertEquals(message, sshException.getLocalizedMessage());
        Assert.assertEquals(message, sshException.getMessage());
    }

    @Test
    public void testConstructorWithMessageAndThrowable() throws Exception {
        String message = "testing message";
        String tMessage = "throwable message";
        Throwable throwable = new Throwable(tMessage);
        SshException sshException = new SshException(message, throwable);
        Assert.assertEquals(throwable, sshException.getCause());
        Assert.assertTrue(sshException.getLocalizedMessage().contains(message));
        Assert.assertTrue(sshException.getMessage().contains(message));
    }
}
