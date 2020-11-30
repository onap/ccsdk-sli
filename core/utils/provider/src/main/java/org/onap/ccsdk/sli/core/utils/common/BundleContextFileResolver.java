/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
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

package org.onap.ccsdk.sli.core.utils.common;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Resolves properties files from runtime property value <code>SDNC_CONFIG_DIR</code> defined in the osgi properties.
 */
public class BundleContextFileResolver implements PropertiesFileResolver {

    /**
     * Key for osgi variable representing the configuration directory
     */
    private static final String SDNC_CONFIG_DIR_PROP_KEY = "SDNC_CONFIG_DIR";

    private final String successMessage;
    private final Class<?> clazz;

    public BundleContextFileResolver(final String successMessage, final Class<?> clazz) {
        this.successMessage = successMessage;
        this.clazz = clazz;
    }

    /**
     * Parse a properties file location based on JRE argument
     *
     * @return an Optional File containing the location if it exists, or an empty Optional
     */
    @Override
    public Optional<File> resolveFile(final String filename) {
        try {
            if (FrameworkUtil.getBundle(clazz) == null) {
                return Optional.empty();
            } else {
                     final String pathProperty = FrameworkUtil.getBundle(this.clazz).getBundleContext()
                            .getProperty(SDNC_CONFIG_DIR_PROP_KEY);
                    if (Strings.isNullOrEmpty(pathProperty)) {
                        return Optional.empty();
                    }
                    final Path dblibPath = Paths.get(pathProperty);
                    return Optional.of(dblibPath.resolve(filename).toFile());

            }
        } catch (Exception|NoClassDefFoundError e) {
            LoggerFactory.getLogger(this.getClass()).error("", e);
            return Optional.empty();
        }
    }

    @Override
    public String getSuccessfulResolutionMessage() {
        return this.successMessage;
    }
}
