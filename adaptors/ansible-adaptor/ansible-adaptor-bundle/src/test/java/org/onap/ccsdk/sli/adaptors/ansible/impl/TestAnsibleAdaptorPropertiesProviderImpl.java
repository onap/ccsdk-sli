/*-
 * ============LICENSE_START=======================================================
 * ONAP : SLI
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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

package org.onap.ccsdk.sli.adaptors.ansible.impl;

import java.io.File;
import java.util.Properties;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdaptorPropertiesProviderImpl;

import static org.junit.Assert.assertEquals;
import static org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptorConstants.*;

public class TestAnsibleAdaptorPropertiesProviderImpl {

    @Test
    public void testGetProperties() throws IllegalStateException, IllegalArgumentException {
        Properties prop = new AnsibleAdaptorPropertiesProviderImpl().getProperties();

        assertEquals("TRUST_ALL", prop.getProperty(CLIENT_TYPE_PROPERTY_NAME));
        assertEquals("org.onap.appc.appc_ansible_adaptor", prop.getProperty("org.onap.appc.provider.adaptor.name"));
        assertEquals("changeit", prop.getProperty(TRUSTSTORE_PASS_PROPERTY_NAME));
        assertEquals("${user.home},/opt/opendaylight/current/properties,.", prop.getProperty("org.onap.appc.bootstrap.path"));
        assertEquals("APPC", prop.getProperty("appc.application.name"));
        assertEquals("appc.properties", prop.getProperty("org.onap.appc.bootstrap.file"));
        assertEquals("org/onap/appc/i18n/MessageResources", prop.getProperty("org.onap.appc.resources"));
        assertEquals("/opt/opendaylight/tls-client/mykeystore.js", prop.getProperty(TRUSTSTORE_PROPERTY_NAME));
    }

    @Test
    public void testGetTestProperties() throws IllegalStateException, IllegalArgumentException {
        final String configFilePath = "src/test/resources/properties/ansible-adaptor-test.properties".replace("/", File.separator);
        Properties prop = new AnsibleAdaptorPropertiesProviderImpl(configFilePath).getProperties();

        assertEquals("appc", prop.getProperty(CLIENT_TYPE_PROPERTY_NAME));
        assertEquals("org.onap.appc.appc_ansible_adaptor", prop.getProperty("org.onap.appc.provider.adaptor.name"));
        assertEquals("Aa123456", prop.getProperty(TRUSTSTORE_PASS_PROPERTY_NAME));
        assertEquals("${user.home},/opt/opendaylight/current/properties,.", prop.getProperty("org.onap.appc.bootstrap.path"));
        assertEquals("APPC", prop.getProperty("appc.application.name"));
        assertEquals("appc.properties", prop.getProperty("org.onap.appc.bootstrap.file"));
        assertEquals("org/onap/appc/i18n/MessageResources", prop.getProperty("org.onap.appc.resources"));
        assertEquals("src/test/resources/org/onap/appc/asdc-client.jks", prop.getProperty(TRUSTSTORE_PROPERTY_NAME));
    }

}
