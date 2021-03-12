/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Samsung
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.netconf.jsch;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestJSchLogger {


    @Test
    public void testIsEnabled() throws IOException {
        JSchLogger jSchLogger = new JSchLogger();

        boolean response = jSchLogger.isEnabled(2);

        Assert.assertEquals(true, response);
    }

    @Test
    public void testLog() throws IOException {
        JSchLogger jSchLogger = new JSchLogger();

        jSchLogger.log(0, "test-debug");
        jSchLogger.log(1, "test-info");
        jSchLogger.log(2, "test-warn");
        jSchLogger.log(3, "test-error");
        jSchLogger.log(4, "test-fatal");
        jSchLogger.log(5, "test-other");
        assertNotNull(jSchLogger);

    }
}
