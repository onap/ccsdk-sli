/*
 * ============LICENSE_START==========================================
 * Copyright (c) 2019 PANTHEON.tech s.r.o.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 */

package org.onap.ccsdk.sli.core.lighty.common;

import io.lighty.core.controller.api.LightyModule;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils class containing methods to start/stop LightyModules easier.
 */
public class CcsdkLightyUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CcsdkLightyUtils.class);

    private CcsdkLightyUtils() {
        throw new IllegalStateException("This class should not be instantiated!");
    }

    /**
     * Starts provided LightyModule
     * @param lightyModule LightyModule to start
     * @return true if start was successful; false otherwise
     */
    public static boolean startLightyModule(LightyModule lightyModule) {
        LOG.debug("Starting Lighty module: {} ...", lightyModule.getClass());
        try {
            if (lightyModule.start().get()) {
                LOG.debug("Lighty module: {} was started successfully", lightyModule.getClass());
                return true;
            } else {
                LOG.error("Unable to start Lighty Module: {}!", lightyModule.getClass());
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception thrown while initializing Lighty Module: {}!", lightyModule.getClass(), e);
            return false;
        }
    }

    /**
     * Stops provided LightyModule
     * @param lightyModule LightyModule to stop
     * @return true if stop was successful; false otherwise
     */
    public static boolean stopLightyModule(LightyModule lightyModule) {
        LOG.debug("Stopping Lighty Module: {}...", lightyModule.getClass());
        try {
            if (lightyModule.shutdown().get()) {
                LOG.debug("Lighty Module: {} was stopped successfully", lightyModule.getClass());
                return true;
            } else {
                LOG.error("Unable to stop Lighty Module: {}!", lightyModule.getClass());
                return false;
            }
        } catch (Exception e) {
            LOG.error("Exception thrown while shutting down {} in CCSDK Core Lighty module!", lightyModule.getClass(),
                    e);
            return false;
        }
    }
}
