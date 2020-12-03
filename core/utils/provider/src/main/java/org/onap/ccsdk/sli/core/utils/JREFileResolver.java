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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Resolves project properties files relative to the directory identified by the JRE property
 * <code>dblib.properties</code>.
 */
public class JREFileResolver implements PropertiesFileResolver {

    /**
     * Key for JRE argument representing the configuration directory
     */

    private final String successMessage;
    private final Class<?> clazz;

    public JREFileResolver(final String successMessage, final Class<?> clazz) {
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

        final Bundle bundle;

        try {
            bundle = FrameworkUtil.getBundle(this.clazz);
        } catch (NoClassDefFoundError e) {
            return Optional.empty();
        }

        final File dataFile;


        try {
            if (bundle == null) {
                return Optional.empty();
            }

            URL jreArgumentEntry = bundle.getEntry(filename);
            if (jreArgumentEntry == null) {
                return Optional.empty();
            }


            dataFile = bundle.getDataFile(filename);
            if(dataFile.exists()) {
                dataFile.delete();
            }

            try (InputStream input = jreArgumentEntry.openStream()){
                Files.copy(input, dataFile.toPath());
            } catch(Exception exc) {
                return Optional.empty();
            }

            return Optional.of(dataFile);
        } catch (NoClassDefFoundError e) {
            return Optional.empty();
        }
        catch(final Exception e) {
            return Optional.empty();
        }

    }

    @Override
    public String getSuccessfulResolutionMessage() {
        return this.successMessage;
    }
}
