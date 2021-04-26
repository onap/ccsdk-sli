/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.netconf.jsch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSch logger implementation delegating to logback.
 */
public class JSchLogger implements com.jcraft.jsch.Logger {

    private static final Logger LOG = LoggerFactory.getLogger(JSchLogger.class);

    @Override
    public boolean isEnabled(int level) {
        return true;
    }

    @Override
    public void log(int level, String message) {
        switch(level) {
            case com.jcraft.jsch.Logger.DEBUG:
                LOG.debug(message);
                break;

            case com.jcraft.jsch.Logger.INFO:
                LOG.info(message);
                break;

            case com.jcraft.jsch.Logger.WARN:
                LOG.warn(message);
                break;

            case com.jcraft.jsch.Logger.ERROR:
            case com.jcraft.jsch.Logger.FATAL:
                LOG.error(message);
                break;
            default:
                break;
        }
    }
}
