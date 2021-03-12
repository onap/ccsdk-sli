/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ssh;

import java.lang.reflect.Constructor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConstantsTest {

    @Test
    public void test() {
        String payLoad = Constants.PAYLOAD;
        assertEquals(payLoad, "payload");

        assertEquals(Constants.NETCONF_SCHEMA, "sdnctl");
        assertEquals(Constants.SDNCTL_SCHEMA, "sdnctl" );
        assertEquals(Constants.DEVICE_AUTHENTICATION_TABLE_NAME, "DEVICE_AUTHENTICATION" );
        assertEquals(Constants.CONFIGFILES_TABLE_NAME, "CONFIGFILES" );
        assertEquals(Constants.DEVICE_INTERFACE_LOG_TABLE_NAME, "DEVICE_INTERFACE_LOG" );
        assertEquals(Constants.FILE_CONTENT_TABLE_FIELD_NAME, "FILE_CONTENT" );
        assertEquals(Constants.FILE_NAME_TABLE_FIELD_NAME, "FILE_NAME" );

        assertEquals(Constants.VM_NAMES[0], "fe1");
        assertEquals(Constants.VM_NAMES[1], "fe2");
        assertEquals(Constants.VM_NAMES[2], "be1");
        assertEquals(Constants.VM_NAMES[3], "be2");
        assertEquals(Constants.VM_NAMES[4], "be3");
        assertEquals(Constants.VM_NAMES[5], "be4");
        assertEquals(Constants.VM_NAMES[6], "be5");
        assertEquals(Constants.VM_NAMES[7], "smp1");
        assertEquals(Constants.VM_NAMES[8], "smp2");

        assertEquals(Constants.DG_ERROR_FIELD_NAME, "org.openecom.appc.dg.error");

    }
    @Test
    public void testValidatesThatClassConstantsIsNotInstantiable() throws Exception {
        Constructor<Constants> c = Constants.class.getDeclaredConstructor();
        c.setAccessible(true);
        c.newInstance();
    }
}
