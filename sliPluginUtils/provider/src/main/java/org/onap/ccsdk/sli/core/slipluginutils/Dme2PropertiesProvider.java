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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable properties container for dme2 properties.  Since the initial design decision was made to
 * utilize <code>Properties</code> instead of an OSGi <code>ManagedService</code>, it was decided
 * to make these properties immutable.
 */
public final class Dme2PropertiesProvider {

    private static final Logger LOG = LoggerFactory.getLogger(Dme2PropertiesProvider.class);

    /**
     * The name of the environment variable to specify the configuration directory.
     */
    private static final String SDNC_ROOT_DIR_ENV_VAR_KEY = "SDNC_CONFIG_DIR";

    /**
     * the dme2 properties file name.
     */
    private static final String DME2_PROPERTIES_FILE_NAME = "dme2.properties";

    /**
     * the key for <code>proxyUrl</code>, which represents a CSV list of urls
     */
    static final String PROXY_URL_KEY = "proxyUrl";

    /**
     * indicates that proxy urls are separated by commas
     */
    private static final String PROXY_URLS_VALUE_SEPARATOR = ",";

    /**
     * the key for <code>aafUserName</code>
     */
    static final String AAF_USERNAME_KEY = "aafUserName";

    /**
     * the key for <code>aafPassword</code>
     */
    static final String AAF_PASSWORD_KEY = "aafPassword";

    /**
     * the key for <code>envContext</code>
     */
    static final String ENV_CONTEXT_KEY = "envContext";

    /**
     * the key for <code>routeOffer</code>
     */
    static final String ROUTE_OFFER_KEY = "routeOffer";

    /**
     * the key for <code>commonServiceVersion</code>
     */
    static final String COMMON_SERVICE_VERSION_KEY = "commonServiceVersion";

    /**
     * the key for <code>partner</code>
     */
    static final String PARTNER_KEY = "partner";

    private Optional<String []> proxyUrls = Optional.empty();

    private Optional<String> aafUsername = Optional.empty();

    private Optional<String> aafPassword = Optional.empty();

    private Optional<String> envContext = Optional.empty();

    private Optional<String> routeOffer = Optional.empty();

    private Optional<String> commonServiceVersion = Optional.empty();

    private Optional<String> partner = Optional.empty();


    /**
     * Instantiates the properties provider, which involves loading the appropriate properties for dme2.
     */
    public Dme2PropertiesProvider() {
        this(getDme2Path(SDNC_ROOT_DIR_ENV_VAR_KEY, DME2_PROPERTIES_FILE_NAME).toString());
    }

    /**
     * Instantiates the properties provider, which involves loading the appropriate properties for dme2.
     *
     * @param dme2Path location of the dme2.properties file
     */
    @VisibleForTesting
    Dme2PropertiesProvider(final String dme2Path) {
        final Properties properties;
        try {
            properties = getProperties(dme2Path);
            this.proxyUrls = getProxyUrls(properties);
            this.aafUsername = getAafUsername(properties);
            this.aafPassword = getAafPassword(properties);
            this.envContext = getEnvContext(properties);
            this.routeOffer = getRouteOffer(properties);
            this.commonServiceVersion = getCommonServiceVersion(properties);
            this.partner = getPartner(properties);
        } catch (final FileNotFoundException e) {
            LOG.error("dme2.properties file could not be found at path: {}", dme2Path, e);
        } catch (final IOException e) {
            LOG.error("fatal error reading dme2.properties at path: {}", dme2Path, e);
        }
    }

    private static Path getDme2Path(final String sdncRootDirectory, final String dme2Filename) {
        return Paths.get(sdncRootDirectory, dme2Filename);
    }

    private static Properties getProperties(final String dme2Path) throws IOException {
        final File dme2File = new File(dme2Path);
        final Properties properties = new Properties();
        properties.load(new FileReader(dme2File));
        return properties;
    }

    private String getProxyUrl(final Properties properties) {
        return properties.getProperty(PROXY_URL_KEY);
    }

    private Optional<String []> getProxyUrls(final Properties properties) {
        final String proxyUrlsValue = getProxyUrl(properties);
        if (!Strings.isNullOrEmpty(proxyUrlsValue)) {
            return Optional.ofNullable(proxyUrlsValue.split(PROXY_URLS_VALUE_SEPARATOR));
        }
        return Optional.empty();
    }

    public Optional<String []> getProxyUrls() {
        return this.proxyUrls;
    }

    private Optional<String> getAafUsername(final Properties properties) {
        final String aafUsernameValue = properties.getProperty(AAF_USERNAME_KEY);
        return Optional.ofNullable(aafUsernameValue);
    }

    Optional<String> getAafUsername() {
        return this.aafUsername;
    }

    private Optional<String> getAafPassword(final Properties properties) {
        final String aafPassword = properties.getProperty(AAF_PASSWORD_KEY);
        return Optional.ofNullable(aafPassword);
    }

    Optional<String> getAafPassword() {
        return this.aafPassword;
    }

    private Optional<String> getEnvContext(final Properties properties) {
        final String envContext = properties.getProperty(ENV_CONTEXT_KEY);
        return Optional.ofNullable(envContext);
    }

    Optional<String> getEnvContext() {
        return this.envContext;
    }

    private Optional<String> getRouteOffer(final Properties properties) {
        final String routeOffer = properties.getProperty(ROUTE_OFFER_KEY);
        return Optional.ofNullable(routeOffer);
    }

    Optional<String> getRouteOffer() {
        return this.routeOffer;
    }

    private Optional<String> getCommonServiceVersion(final Properties properties) {
        final String commonServiceVersion = properties.getProperty(COMMON_SERVICE_VERSION_KEY);
        return Optional.ofNullable(commonServiceVersion);
    }

    Optional<String> getCommonServiceVersion() {
        return this.commonServiceVersion;
    }

    private Optional<String> getPartner(final Properties properties) {
        final String partner = properties.getProperty(PARTNER_KEY);
        return Optional.ofNullable(partner);
    }

    Optional<String> getPartner() {
        return this.partner;
    }
}
