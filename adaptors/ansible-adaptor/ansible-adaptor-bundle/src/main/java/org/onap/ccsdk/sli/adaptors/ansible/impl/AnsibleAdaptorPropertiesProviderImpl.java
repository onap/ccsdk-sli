/*-
 * ============LICENSE_START=======================================================
 * ONAP : SLI
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ansible.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdaptorPropertiesProvider;
import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.utils.JREFileResolver;
import org.onap.ccsdk.sli.core.utils.KarafRootFileResolver;
import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;
import org.onap.ccsdk.sli.core.utils.common.CoreDefaultFileResolver;
import org.onap.ccsdk.sli.core.utils.common.EnvProperties;
import org.onap.ccsdk.sli.core.utils.common.SdncConfigEnvVarFileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for determining the properties file to use and instantiating the
 * <code>SqlResource</code> Service. The priority for properties file
 * resolution is as follows:
 *
 * <ol>
 * <li>A directory identified by the system environment variable
 * <code>SDNC_CONFIG_DIR</code></li>
 * <li>The default directory <code>DEFAULT_DBLIB_PROP_DIR</code></li>
 * <li>A directory identified by the JRE argument
 * <code>sql-resource.properties</code></li>
 * <li>A <code>sql-resource.properties</code> file located in the karaf root
 * directory</li>
 * </ol>
 */
public class AnsibleAdaptorPropertiesProviderImpl implements AnsibleAdaptorPropertiesProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AnsibleAdaptorPropertiesProviderImpl.class);

    /**
     * The name of the properties file for database configuration
     */
    private static final String ANSIBLE_ADAPTOR_PROPERTIES = "ansible-adaptor.properties";

    /**
     * A prioritized list of strategies for resolving sql-resource properties files.
     */
    private final List<PropertiesFileResolver> ansibleAdaptorPropertiesFileResolvers = new ArrayList<>();

    /**
     * The configuration properties for the db connection.
     */
    private Properties properties;

    /**
     * Set up the prioritized list of strategies for resolving dblib properties
     * files.
     */
    public AnsibleAdaptorPropertiesProviderImpl() {
        ansibleAdaptorPropertiesFileResolvers
                .add(new SdncConfigEnvVarFileResolver("Using property file (1) from environment variable"));
        ansibleAdaptorPropertiesFileResolvers
                .add(new CoreDefaultFileResolver("Using property file (2) from default directory"));
        ansibleAdaptorPropertiesFileResolvers
                .add(new JREFileResolver("Using property file (3) from JRE argument", AnsibleAdaptorPropertiesProviderImpl.class));
        ansibleAdaptorPropertiesFileResolvers
                .add(new KarafRootFileResolver("Using property file (4) from karaf root", this));

        // determines properties file as according to the priority described in the
        // class header comment
        final File propertiesFile = determinePropertiesFile();
        if (propertiesFile != null) {
            try (FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
                properties = new EnvProperties();
                properties.load(fileInputStream);
            } catch (final IOException e) {
                LOG.error("Failed to load properties for file: {}", propertiesFile,
                        new ConfigurationException("Failed to load properties for file: " + propertiesFile, e));
            }
        } else {
            // Try to read properties as resource
            InputStream propStr = getClass().getResourceAsStream("/" + ANSIBLE_ADAPTOR_PROPERTIES);
            if (propStr != null) {
                properties = new EnvProperties();
                try {
                    properties.load(propStr);
                    propStr.close();
                } catch (IOException e) {
                    properties = null;
                }
            }
        }
        if (properties == null) {
            reportFailure(new ConfigurationException(
                    "Missing configuration properties resource(3): " + ANSIBLE_ADAPTOR_PROPERTIES));
            LOG.info("Defaulting org.onap.appc.adaptor.ansible.clientType to TRUST_ALL");
            properties = new Properties();
            properties.setProperty("org.onap.appc.adaptor.ansible.clientType", "TRUST_ALL");
        }

    }

    /**
     * Instantiates a new Ansible adaptor properties provider.
     *
     * @param configFilePath the config file path
     */
    public AnsibleAdaptorPropertiesProviderImpl(String configFilePath) {
        properties = new EnvProperties();
        try {
            properties.load(new FileInputStream(configFilePath));
        } catch (IOException e) {
            properties = null;
        }
        if (properties == null) {
            reportFailure(new ConfigurationException(
                    "Missing configuration properties resource(3): " + ANSIBLE_ADAPTOR_PROPERTIES));
            LOG.info("Defaulting org.onap.appc.adaptor.ansible.clientType to TRUST_ALL");
            properties = new Properties();
            properties.setProperty("org.onap.appc.adaptor.ansible.clientType", "TRUST_ALL");
        }

    }

    /**
     * Reports the method chosen for properties resolution to the
     * <code>Logger</code>.
     *
     * @param message      Some user friendly message
     * @param fileOptional The file location of the chosen properties file
     *
     * @return the file location of the chosen properties file
     */
    private static File reportSuccess(final String message, final Optional<File> fileOptional) {
        if (fileOptional.isPresent()) {
            final File file = fileOptional.get();
            LOG.info("{} {}", message, file.getPath());
            return file;
        }
        return null;
    }

    /**
     * Reports fatal errors. This is the case in which no properties file could be
     * found.
     *
     * @param configurationException An exception describing what went wrong during resolution
     */
    private static void reportFailure(final ConfigurationException configurationException) {
        LOG.error("{}", "Missing configuration properties resource(3)", configurationException);
    }

    /**
     * Extract svclogic config properties.
     *
     * @return the svclogic config properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Determines the sql-resource properties file to use based on the following priority:
     * <ol>
     * <li>A directory identified by the system environment variable
     * <code>SDNC_CONFIG_DIR</code></li>
     * <li>The default directory <code>DEFAULT_DBLIB_PROP_DIR</code></li>
     * <li>A directory identified by the JRE argument
     * <code>sql-resource.properties</code></li>
     * <li>A <code>sql-resource.properties</code> file located in the karaf root
     * directory</li>
     * </ol>
     */
    File determinePropertiesFile() {
        for (final PropertiesFileResolver propertiesFileResolver : ansibleAdaptorPropertiesFileResolvers) {
            final Optional<File> fileOptional = propertiesFileResolver.resolveFile(ANSIBLE_ADAPTOR_PROPERTIES);
            if (fileOptional.isPresent()) {
                return reportSuccess(propertiesFileResolver.getSuccessfulResolutionMessage(), fileOptional);
            }
        }

        return null;
    }

}
