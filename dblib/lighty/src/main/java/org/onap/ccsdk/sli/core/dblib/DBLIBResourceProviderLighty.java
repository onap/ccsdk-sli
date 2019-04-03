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

package org.onap.ccsdk.sli.core.dblib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;
import org.onap.ccsdk.sli.core.utils.KarafRootFileResolver;
import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;
import org.onap.ccsdk.sli.core.utils.common.CoreDefaultFileResolver;
import org.onap.ccsdk.sli.core.utils.common.SdncConfigEnvVarFileResolver;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THIS CLASS IS A COPY OF {@link DBLIBResourceProvider} WITH REMOVED OSGi DEPENDENCIES
 */
public class DBLIBResourceProviderLighty {

    private static final Logger LOG = LoggerFactory.getLogger(DBLIBResourceProviderLighty.class);

    /**
     * The name of the properties file for database configuration
     */
    private static final String DBLIB_PROP_FILE_NAME = "dblib.properties";

    private static final String DBLIB_PROPERTY_NAME = "org.onap.ccsdk.sli.jdbc.password";
    private final AAAEncryptionService aaaEncryptionService;

    /**
     * A prioritized list of strategies for resolving dblib properties files.
     */
    private Vector<PropertiesFileResolver> dblibPropertiesFileResolvers = new Vector<>();

    /**
     * The configuration properties for the db connection.
     */
    private Properties properties;

    /**
     * Set up the prioritized list of strategies for resolving dblib properties files.
     */
    public DBLIBResourceProviderLighty(AAAEncryptionService aaaEncryptionService) {
        this.aaaEncryptionService = aaaEncryptionService;

        dblibPropertiesFileResolvers.add(new SdncConfigEnvVarFileResolver(
                "Using property file (1) from environment variable"
        ));
        dblibPropertiesFileResolvers.add(new CoreDefaultFileResolver(
                "Using property file (2) from default directory"
        ));
        dblibPropertiesFileResolvers.add(new KarafRootFileResolver(
                "Using property file (4) from karaf root", this));

        // determines properties file as according to the priority described in the class header comment
        final File propertiesFile = determinePropertiesFile(this);
        if (propertiesFile != null) {
            try(FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
                properties = new Properties();
                properties.load(fileInputStream);

                if(properties.containsKey(DBLIB_PROPERTY_NAME)) {
                    String sensitive = properties.getProperty(DBLIB_PROPERTY_NAME);
                    if(sensitive != null && sensitive.startsWith("ENC:")) {
                        try {
                            sensitive = sensitive.substring(4);
                            String postsense = decrypt(sensitive);
                            properties.setProperty(DBLIB_PROPERTY_NAME, postsense);
                        } catch(Exception exc) {
                            LOG.error("Failed to translate property", exc);
                        }
                    }
                }

            } catch (final IOException e) {
                LOG.error("Failed to load properties for file: {}", propertiesFile.toString(),
                        new DblibConfigurationException("Failed to load properties for file: "
                                + propertiesFile.toString(), e));
            }
        }
    }

    /**
     *
     * @param value
     * @return decrypted string if successful or the original value if unsuccessful
     */
    private String decrypt(String value) {
        return aaaEncryptionService.decrypt(value);
    }

    /**
     * Extract db config properties.
     *
     * @return the db config properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Reports the method chosen for properties resolution to the <code>Logger</code>.
     *
     * @param message Some user friendly message
     * @param fileOptional The file location of the chosen properties file
     * @return the file location of the chosen properties file
     */
    private static File reportSuccess(final String message, final Optional<File> fileOptional) {
        if(fileOptional.isPresent()) {
            final File file = fileOptional.get();
            LOG.info("{} {}", message, file.getPath());
            return file;
        }
        return null;
    }

    /**
     * Reports fatal errors.  This is the case in which no properties file could be found.
     *
     * @param message An appropriate fatal error message
     * @param dblibConfigurationException An exception describing what went wrong during resolution
     */
    private static void reportFailure(final String message,
                                      final DblibConfigurationException dblibConfigurationException) {

        LOG.error("{}", message, dblibConfigurationException);
    }

    /**
     * Determines the dblib properties file to use based on the following priority:
     * <ol>
     *     <li>A directory identified by the system environment variable <code>SDNC_CONFIG_DIR</code></li>
     *     <li>The default directory <code>DEFAULT_DBLIB_PROP_DIR</code></li>
     *     <li>A directory identified by the JRE argument <code>dblib.properties</code></li>
     *     <li>A <code>dblib.properties</code> file located in the karaf root directory</li>
     * </ol>
     */
    File determinePropertiesFile(final DBLIBResourceProviderLighty dblibResourceProvider) {

        for (final PropertiesFileResolver dblibPropertiesFileResolver : dblibPropertiesFileResolvers) {
            final Optional<File> fileOptional = dblibPropertiesFileResolver.resolveFile(DBLIB_PROP_FILE_NAME);
            if (fileOptional.isPresent()) {
                return reportSuccess(dblibPropertiesFileResolver.getSuccessfulResolutionMessage(), fileOptional);
            }
        }

        reportFailure("Missing configuration properties resource(3)",
                new DblibConfigurationException("Missing configuration properties resource(3): "
                        + DBLIB_PROP_FILE_NAME));
        return null;
    }
}
