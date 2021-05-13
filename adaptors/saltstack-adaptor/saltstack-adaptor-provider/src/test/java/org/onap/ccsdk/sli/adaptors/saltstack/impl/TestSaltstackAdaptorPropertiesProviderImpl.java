/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics. All rights reserved.
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

package org.onap.ccsdk.sli.adaptors.saltstack.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.saltstack.SaltstackAdaptorPropertiesProvider;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SaltstackAdaptorImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestSaltstackAdaptorPropertiesProviderImpl {

    private SaltstackAdaptorImpl adaptor;
    private Properties params;


    @Before
    public void setup() throws IllegalArgumentException {
        params = new Properties();
    }

    @After
    public void tearDown() {
        adaptor = null;
        params = null;
    }

    @Test
    public void reqExecCommand_setPropertiesBasicPortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "BASIC");
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_setPropertiesBasicPortString() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "BASIC");
        params.put("org.onap.appc.adaptor.saltstack.host", "test");
        params.put("org.onap.appc.adaptor.saltstack.port", "test");
        params.put("org.onap.appc.adaptor.saltstack.userName", "test");
        params.put("org.onap.appc.adaptor.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adaptor.saltstack.sshKey", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesBasicSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "BASIC");
        params.put("org.onap.appc.adaptor.saltstack.host", "test");
        params.put("org.onap.appc.adaptor.saltstack.port", "10");
        params.put("org.onap.appc.adaptor.saltstack.userName", "test");
        params.put("org.onap.appc.adaptor.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adaptor.saltstack.sshKey", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(adaptor);
    }

    @Test
    public void reqExecCommand_setPropertiesSSH_CERTPortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "SSH_CERT");
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_setPropertiesSSH_CERTPortString() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "SSH_CERT");
        params.put("org.onap.appc.adaptor.saltstack.host", "test");
        params.put("org.onap.appc.adaptor.saltstack.port", "test");
        params.put("org.onap.appc.adaptor.saltstack.userName", "test");
        params.put("org.onap.appc.adaptor.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adaptor.saltstack.sshKey", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesSSH_CERTSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "SSH_CERT");
        params.put("org.onap.appc.adaptor.saltstack.host", "test");
        params.put("org.onap.appc.adaptor.saltstack.port", "10");
        params.put("org.onap.appc.adaptor.saltstack.userName", "test");
        params.put("org.onap.appc.adaptor.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adaptor.saltstack.sshKey", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesBOTHPortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "BOTH");
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesBOTHSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "BOTH");
        params.put("org.onap.appc.adaptor.saltstack.host", "test");
        params.put("org.onap.appc.adaptor.saltstack.port", "10");
        params.put("org.onap.appc.adaptor.saltstack.userName", "test");
        params.put("org.onap.appc.adaptor.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adaptor.saltstack.sshKey", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesNonePortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "NONE");
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesNonePortString() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "NONE");
        params.put("org.onap.appc.adaptor.saltstack.host", "test");
        params.put("org.onap.appc.adaptor.saltstack.port", "test");
        params.put("org.onap.appc.adaptor.saltstack.userName", "test");
        params.put("org.onap.appc.adaptor.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adaptor.saltstack.sshKey", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesNoneSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.clientType", "NONE");
        params.put("org.onap.appc.adaptor.saltstack.host", "test");
        params.put("org.onap.appc.adaptor.saltstack.port", "10");
        params.put("org.onap.appc.adaptor.saltstack.userName", "test");
        params.put("org.onap.appc.adaptor.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adaptor.saltstack.sshKey", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }


    @Test
    public void reqExecCommand_setPropertiesElsePortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesElsePortString() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.host", "test");
        params.put("org.onap.appc.adaptor.saltstack.port", "test");
        params.put("org.onap.appc.adaptor.saltstack.userName", "test");
        params.put("org.onap.appc.adaptor.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adaptor.saltstack.sshKey", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        assertNotNull(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesElseSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adaptor.saltstack.host", "test");
        params.put("org.onap.appc.adaptor.saltstack.port", "10");
        params.put("org.onap.appc.adaptor.saltstack.userName", "test");
        params.put("org.onap.appc.adaptor.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adaptor.saltstack.sshKey", "test");
        SaltstackAdaptorPropertiesProvider propProvider = new SaltstackAdaptorPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adaptor = new SaltstackAdaptorImpl(propProvider);
        String adaptorName = adaptor.getAdaptorName();
        assertEquals("Saltstack Adaptor", adaptorName);
    }

    @Test
    public void reqExecCommand_setPropertiesDefault() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        adaptor = new SaltstackAdaptorImpl();
        assertNotNull(adaptor);
    }
}
