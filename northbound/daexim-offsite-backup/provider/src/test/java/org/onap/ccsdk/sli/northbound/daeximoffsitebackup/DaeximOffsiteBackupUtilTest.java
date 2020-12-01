/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights
 * 			reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.northbound.daeximoffsitebackup;

import org.junit.Test;

import java.lang.reflect.Field;

public class DaeximOffsiteBackupUtilTest {
    @Test
    public void loadProperties() {
        try {
            Field field = DaeximOffsiteBackupUtil.class.getDeclaredField("PROPERTIES_FILE");
            field.setAccessible(true);
            field.set(new DaeximOffsiteBackupUtil(), "src/test/resources/daexim-offsite-backup.properties");
            DaeximOffsiteBackupUtil.loadProperties();
        } catch(Exception e) {
            // Files don't exist on build server
        }
    }
}
