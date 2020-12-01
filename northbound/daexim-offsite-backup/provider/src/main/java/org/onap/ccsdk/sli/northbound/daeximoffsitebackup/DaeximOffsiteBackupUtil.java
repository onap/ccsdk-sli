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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.daeximoffsitebackup.rev180926.BackupDataOutputBuilder;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.daeximoffsitebackup.rev180926.RetrieveDataInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.daeximoffsitebackup.rev180926.RetrieveDataOutputBuilder;

import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaeximOffsiteBackupUtil extends MdsalHelper {
    private static final Logger LOG = LoggerFactory.getLogger(DaeximOffsiteBackupUtil.class);
    private static String PROPERTIES_FILE;

    public static void loadProperties() {
        File file = new File(PROPERTIES_FILE);
        Properties properties = new Properties();
        InputStream input = null;
        if(file.isFile() && file.canRead()) {
            try {
                input = new FileInputStream(file);
                properties.load(input);
                LOG.info("Loaded properties from " + PROPERTIES_FILE);
                setProperties(properties);
            } catch (Exception e) {
                LOG.error("Failed to load properties " + PROPERTIES_FILE + "\n", e);
            } finally {
                if(input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        LOG.error("Failed to close properties file " + PROPERTIES_FILE + "\n", e);
                    }
                }
            }
        }
    }

    static {
        // Trick class loader into loading builders. Some of
        // these will be needed later by Reflection classes, but need
        // to explicitly "new" them here to get class loader to load them.

        BackupDataOutputBuilder b1 = new BackupDataOutputBuilder();

        RetrieveDataOutputBuilder b2 = new RetrieveDataOutputBuilder();
        RetrieveDataInputBuilder b3 = new RetrieveDataInputBuilder();
    }
}
