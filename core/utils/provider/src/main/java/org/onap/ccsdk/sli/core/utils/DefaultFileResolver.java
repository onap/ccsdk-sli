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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Resolves dblib properties files relative to the default file path.  In Unix, this is represented by:
 * <code>/opt/sdnc/data/properties</code>
 */
public abstract class DefaultFileResolver implements PropertiesFileResolver {

    private final String successMessage;

    private final Path propertyPath;

    public DefaultFileResolver(final String successMessage, final Path propertyPath) {
        this.successMessage = successMessage;
        this.propertyPath = propertyPath;
    }

    /**
     * Parse a properties file location based on the default properties location
     *
     * @return an Optional File containing the location if it exists, or an empty Optional
     */
    @Override
    public Optional<File> resolveFile(final String filename) {
        final File fileFromDefaultDblibDir = propertyPath.resolve(filename).toFile();
        if (fileFromDefaultDblibDir.exists()) {
            return Optional.of(fileFromDefaultDblibDir);
        }
        return Optional.empty();
    }

    @Override
    public String getSuccessfulResolutionMessage() {
        return this.successMessage;
    }
}
