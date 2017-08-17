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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.onap.ccsdk.sli.core.dblib.DBLIBResourceProvider;
import org.osgi.framework.FrameworkUtil;

/**
 * Resolves dblib properties files relative to the directory identified by the JRE property
 * <code>dblib.properties</code>.
 */
public class DblibJREFileResolver implements DblibPropertiesFileResolver {

    /**
     * Key for JRE argument representing the configuration directory
     */
    private static final String DBLIB_JRE_PROPERTY_KEY = "dblib.properties";

    private final String successMessage;

    public DblibJREFileResolver(final String successMessage) {
        this.successMessage = successMessage;
    }

    /**
     * Parse a properties file location based on JRE argument
     *
     * @return an Optional File containing the location if it exists, or an empty Optional
     */
    @Override
    public Optional<File> resolveFile(final String dblibFileName) {
        final URL jreArgumentUrl = FrameworkUtil.getBundle(DBLIBResourceProvider.class)
                .getResource(DBLIB_JRE_PROPERTY_KEY);
        try {
            if (jreArgumentUrl == null) {
                return Optional.empty();
            }
            final Path dblibPath = Paths.get(jreArgumentUrl.toURI());
            return Optional.of(dblibPath.resolve(dblibFileName).toFile());
        } catch(final URISyntaxException e) {
            return Optional.empty();
        }
    }

    @Override
    public String getSuccessfulResolutionMessage() {
        return this.successMessage;
    }
}
