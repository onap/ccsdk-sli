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
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves project properties files relative to the directory identified by the JRE property
 * <code>dblib.properties</code>.
 */
public class JREFileResolver implements PropertiesFileResolver {

    /**
     * Key for JRE argument representing the configuration directory
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(JREFileResolver.class);
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

        try {

            final Bundle bundle = FrameworkUtil.getBundle(this.clazz);
            if (bundle == null) {
                return Optional.empty();
            }

            URL jreArgumentEntry = bundle.getEntry(filename);
            if (jreArgumentEntry == null) {
                return Optional.empty();
            }


            final File dataFile = bundle.getDataFile(filename);
            if(dataFile.exists()) {
                dataFile.delete();
            }

            try (InputStream input = jreArgumentEntry.openStream()){
                Files.copy(input, dataFile.toPath());
            } catch(Exception exc) {
                return Optional.empty();
            }

            return Optional.of(dataFile);
        } catch (final NoClassDefFoundError e) {
            LOGGER.info("Getting /{} embedded with {}", filename, clazz.getCanonicalName());
            try (InputStream input = clazz.getResourceAsStream("/"+filename)) {
                File propFile = File.createTempFile("tmp", ".properties", null);
                LOGGER.info("Copying /{} to {}", filename, propFile.getAbsolutePath());
                Files.copy(input, propFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return Optional.of(propFile);
            } catch (Exception e1) {
                LOGGER.info("Caught exception getting {} embedded in jar", filename, e1);
                return Optional.empty();
            }
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
