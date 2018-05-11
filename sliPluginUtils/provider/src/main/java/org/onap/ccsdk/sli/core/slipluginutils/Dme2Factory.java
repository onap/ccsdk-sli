/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.onap.ccsdk.sli.core.slipluginutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;
import org.onap.ccsdk.sli.core.utils.JREFileResolver;
import org.onap.ccsdk.sli.core.utils.KarafRootFileResolver;
import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;
import org.onap.ccsdk.sli.core.utils.common.CoreDefaultFileResolver;
import org.onap.ccsdk.sli.core.utils.common.SdncConfigEnvVarFileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable properties container for dme2 properties. Since the initial design decision was made to
 * utilize <code>Properties</code> instead of an OSGi <code>ManagedService</code>, it was decided to
 * make these properties immutable.
 */
public final class Dme2Factory {

    private static final Logger LOG = LoggerFactory.getLogger(Dme2Factory.class);
    private static final String DME2_PROPERTIES_FILE_NAME = "dme2.properties";

    static Properties properties;
    private Vector<PropertiesFileResolver> dme2PropertiesFileResolvers = new Vector<>();

    public Dme2Factory() {
        dme2PropertiesFileResolvers
                .add(new SdncConfigEnvVarFileResolver("Using property file (1) from environment variable"));
        dme2PropertiesFileResolvers.add(new CoreDefaultFileResolver("Using property file (2) from default directory"));
        dme2PropertiesFileResolvers
                .add(new JREFileResolver("Using property file (3) from JRE argument", Dme2Factory.class));
        dme2PropertiesFileResolvers.add(new KarafRootFileResolver("Using property file (4) from karaf root", this));
        File dme2File = getDme2File(DME2_PROPERTIES_FILE_NAME);
        init(dme2File);
    }

    private void init(final File dme2propertiesFile) {
        try {
            properties = getProperties(dme2propertiesFile);
        } catch (final FileNotFoundException e) {
            LOG.error("dme2.properties file could not be found at path: {}", dme2propertiesFile, e);
        } catch (final IOException e) {
            LOG.error("fatal error reading dme2.properties at path: {}", dme2propertiesFile, e);
        }
    }

    /**
     * Reports the method chosen for properties resolution to the <code>Logger</code>.
     *
     * @param message Some user friendly message
     * @param fileOptional The file location of the chosen properties file
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

    private File getDme2File(final String dme2Filename) {
        for (final PropertiesFileResolver dblibPropertiesFileResolver : dme2PropertiesFileResolvers) {
            final Optional<File> fileOptional = dblibPropertiesFileResolver.resolveFile(dme2Filename);
            if (fileOptional.isPresent()) {
                return reportSuccess(dblibPropertiesFileResolver.getSuccessfulResolutionMessage(), fileOptional);
            }
        }
        return (new File(dme2Filename));
    }

    private static Properties getProperties(final File dme2File) throws IOException {
        final Properties properties = new Properties();
        properties.load(new FileReader(dme2File));
        return properties;
    }
       
    public DME2 createDme2() {
        return new DME2(properties);
    }

}
