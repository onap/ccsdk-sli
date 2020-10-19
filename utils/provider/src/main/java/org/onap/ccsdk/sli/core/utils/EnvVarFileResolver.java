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

package org.onap.ccsdk.sli.core.utils;

import com.google.common.base.Strings;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Resolves properties files relative to the directory identified by the <code>SDNC_CONFIG_DIR</code>
 * environment variable. If a system property with the same name is set it is given precedence.
 */
public abstract class EnvVarFileResolver implements PropertiesFileResolver {

    /**
     * Key for environment variable representing the configuration directory
     */
    private final String propertyKey;

    private final String successMessage;

    public EnvVarFileResolver(final String successMessage, final String propertyKey) {
        this.successMessage = successMessage;
        this.propertyKey = propertyKey;
    }

    /**
     * Parse a properties file location based on System environment variable
     *
     * @return an Optional File containing the location if it exists, or an empty Optional
     */
    @Override
    public Optional<File> resolveFile(final String filename) {
        // attempt to read the system property first
        String propDirectoryFromEnvVariable = System.getProperty(propertyKey);
 
        if(propDirectoryFromEnvVariable == null) {
            // attempt to resolve the property directory from the corresponding environment variable
            propDirectoryFromEnvVariable = System.getenv(propertyKey);
        }

        final File fileFromEnvVariable;
        if (!Strings.isNullOrEmpty(propDirectoryFromEnvVariable)) {
            fileFromEnvVariable = Paths.get(propDirectoryFromEnvVariable).resolve(filename).toFile();
            if(PathValidator.isValidFilePath(fileFromEnvVariable.getAbsolutePath()) && fileFromEnvVariable.exists()) {
                return Optional.of(fileFromEnvVariable);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getSuccessfulResolutionMessage() {
        return this.successMessage;
    }
}
