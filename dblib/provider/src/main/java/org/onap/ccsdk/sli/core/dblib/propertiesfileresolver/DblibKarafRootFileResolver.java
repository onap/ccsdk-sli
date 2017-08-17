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

package org.onap.ccsdk.sli.core.dblib.propertiesfileresolver;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import org.onap.ccsdk.sli.core.dblib.DBLIBResourceProvider;

/**
 * Resolves dblib properties files relative to the karaf root directory.
 */
public class DblibKarafRootFileResolver implements DblibPropertiesFileResolver {

    final DBLIBResourceProvider dblibResourceProvider;

    private final String successMessage;

    public DblibKarafRootFileResolver(final String successMessage, final DBLIBResourceProvider dblibResourceProvider) {
        this.successMessage = successMessage;
        this.dblibResourceProvider = dblibResourceProvider;
    }

    /**
     * Parse a properties file location relative to the karaf root
     *
     * @return an Optional File containing the location if it exists, or an empty Optional
     */
    @Override
    public Optional<File> resolveFile(final String dblibFileName) {
        final URL fromKarafRoot = dblibResourceProvider.getClass().getResource(dblibFileName);
        if (fromKarafRoot != null) {
            final File propertiesFile = new File(fromKarafRoot.getFile());
            if (propertiesFile.exists()) {
                return Optional.of(propertiesFile);
            }
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public String getSuccessfulResolutionMessage() {
        return this.successMessage;
    }
}
