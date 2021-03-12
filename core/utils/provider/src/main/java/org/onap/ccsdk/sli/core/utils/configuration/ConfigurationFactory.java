/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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

package org.onap.ccsdk.sli.core.utils.configuration;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.utils.logging.LoggingUtils;
import org.onap.ccsdk.sli.core.utils.logging.Msg;

/**
 * The configuration factory is used to obtain access to an already created and initialized
 * singleton configuration object as well as to create and initialize the singleton if not already
 * set up.
 * <p>
 * This class is responsible for the creation of the configuration object used to manage the
 * configuration of the application. The configuration object implementation must implement the
 * <code>Configuration</code> interface. This allows for the factory to create different
 * specializations in the future if needed and not break any application code.
 * </p>
 * <p>
 * The configuration object is basically a wrapper around a properties object. The configuration is
 * therefore specified as a set of properties that are loaded and processed from different sources
 * with different precedences. It is important that the configuration object always be able to
 * supply default values for any configuration properties that must be supplied, and not rely on the
 * user always supplying these values. This also relieves the application itself from having to
 * interpret missing or invalid properties and applying defaults. By having all of the defaults in
 * one place, the application code can be simpler (not having to worry about defaults or invalid
 * properties), and the defaults can be changed much easier (they are all in one place and not
 * distributed throughout the codebase).
 * </p>
 * <p>
 * Since the configuration is managed as a property object, we can use a characteristic of the
 * <code>Properties</code> class to our advantage. Namely, if we put a property into a
 * <code>Properties</code> object that already exists, the <code>Properties</code> object replaces
 * it with the new value. This does not affect any other properties that may already be defined in
 * the properties object. This gives us the ability to initialize the properties with default values
 * for all of the application settings, then override just those that we need to override, possibly
 * from multiple sources and in increasing order of precedence.
 * </p>
 * <p>
 * This means that properties are in effect "merged" together from multiple sources in a prescribed
 * precedence order. In fact, the precedence order that this factory implements is defined as:
 * </p>
 * <ol>
 * <li>Default values from a system resource file.</li>
 * <li>User-supplied properties file, if any.</li>
 * <li>Application-supplied properties, if any.</li>
 * <li>Command-line properties (if any)</li>
 * </ol>
 * <p>
 * The name and location of the properties file that is loaded can also be set, either in the
 * defaults, overridden by the system command line via -D, or as a system environment variable.
 * There are two properties that can be specified to define the name and path. These are:
 * </p>
 * <dl>
 * <dt>org.onap.appc.bootstrap.file</dt>
 * <dd>This property defines the name of the file that will be loaded. If not specified, the default
 * value is "appc.properties". This can be specified in either (or both) the default properties or
 * the command line. The command line specification will always override.</dd>
 * <dt>org.onap.appc.bootstrap.path</dt>
 * <dd>This is a comma-delimited (,) path of directories to be searched to locate the specified
 * file. The first occurrence of the file is the one loaded, and no additional searching is
 * performed. The path can be specified in either, or both, the default values and the command line
 * specification. If specified on the command line, the value overrides the default values. If
 * omitted, the default path is <code>$/opt/onap/appc/data/properties,${user.home},.</code></dd>
 * </dl>
 *
 * @since Mar 18, 2014
 * @version $Id$
 */
public final class ConfigurationFactory {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ConfigurationFactory.class);

    /**
     * This is a string constant for the comma character. It's intended to be used a common string
     * delimiter.
     */
    private static final String COMMA = ",";

    /**
     * The default Configuration object that implements the <code>Configuration</code> interface and
     * represents our system configuration settings.
     */
    private static DefaultConfiguration config = null;

    /**
     * The default properties resource to be loaded
     */
    private static final String DEFAULT_PROPERTIES = "default.properties";

    /**
     * This collection allows for special configurations to be created and maintained, organized by
     * some identification (such as an object reference to the StackBuilder to which they apply),
     * and then obtained from the configuration factory when needed.
     */
    private static HashMap<Object, Configuration> localConfigs = new HashMap<>();

    /**
     * The reentrant shared lock used to serialize access to the properties.
     */
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * This is a constant array of special property names that will be copied from the configuration
     * back to the System properties object if they are defined in the configuration AND they do not
     * already exist in the System properties object. These are intended as a convenience for
     * setting the AFT properties for the Discovery client where it may be difficult or impossible
     * to set VM arguments for the container.
     */
    private static final String[] specialProperties =
            {"AFT_LATITUDE", "AFT_LONGITUDE", "AFT_ENVIRONMENT", "SCLD_PLATFORM"};

    private ConfigurationFactory() {}

    /**
     * This method is used to obtain the common configuration object (as well as set it up if not
     * already).
     *
     * @return The configuration object implementation
     */
    public static Configuration getConfiguration() {

        /*
         * First, attempt to access the properties as a read lock holder
         */
        ReadLock readLock = lock.readLock();
        readLock.lock();
        try {

            /*
             * If the properties don't exist, release the read lock and acquire the write lock. Once
             * we get the write lock, we need to re-check to see that the configuration needs to be
             * set up (because another thread may have beat us to it). After we get a configuration
             * set up, release the write lock and re-obtain the read lock to access the properties.
             */
            if (config == null) {
                readLock.unlock();
                WriteLock writeLock = lock.writeLock();
                writeLock.lock();
                try {
                    if (config == null) {
                        config = new DefaultConfiguration();
                        initialize(null);
                    }
                } catch (Exception e){
                    logger.error("getConfiguration: " + LoggingUtils.formatException(e));
                } finally {
                    writeLock.unlock();
                }
                readLock.lock();
            }
        } finally {
            readLock.unlock();
        }
        return config;
    }

    /**
     * This method will obtain the local configuration for the specified object if it exists, or
     * will create it from the current global configuration. This allows the configuration to be
     * tailored for a specific process or operation, and uniquely identified by some value (such as
     * the object that represents the special use of the configuration).
     *
     * @param owner The owner or identification of the owner of the special configuration
     * @return The special configuration object, or a clone of the global configuration so that it
     *         can be altered if needed.
     */
    public static Configuration getConfiguration(final Object owner) {
        DefaultConfiguration local;
        ReadLock readLock = lock.readLock();
        readLock.lock();
        try {
            local = (DefaultConfiguration) localConfigs.get(owner);
            if (local == null) {
                readLock.unlock();
                WriteLock writeLock = lock.writeLock();
                writeLock.lock();
                local = (DefaultConfiguration) localConfigs.get(owner);
                if (local == null) {
                    local = getClonedDefaultConfiguration(owner, local);
                }
                writeLock.unlock();
            }
            readLock.lock();
        } finally {
            readLock.unlock();
        }
        return local;
    }

    /**
     * This method allows the caller to alter the configuration, supplying the specified
     * configuration properties which override the application default values.
     * <p>
     * The configuration is re-constructed (if already constructed) or created new (if not already
     * created) and the default properties are loaded into the configuration.
     * </p>
     * <p>
     * The primary purpose of this method is to allow the application configuration properties to be
     * reset or refreshed after the application has already been initialized. This method will lock
     * the configuration for the duration while it is being re-built, and should not be called on a
     * regular basis.
     * </p>
     *
     * @param props The properties used to configure the application.
     * @return Access to the configuration implementation
     */
    public static Configuration getConfiguration(final Properties props) {
        WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            config = new DefaultConfiguration();
            initialize(props);
            return config;
        } finally {
            writeLock.unlock();
        }
    }

    private static DefaultConfiguration getClonedDefaultConfiguration(Object owner, DefaultConfiguration local) {
        Optional<DefaultConfiguration> global =
                Optional.ofNullable((DefaultConfiguration) getConfiguration());
        try {
            if (global.isPresent()) {
                local = (DefaultConfiguration) global.get().clone();
            }
        } catch (CloneNotSupportedException e) {
            logger.error("getClonedDefaultConfiguration: " + LoggingUtils.formatException(e));
        }
        localConfigs.put(owner, local);
        return local;
    }

    /**
     * This method will clear the current configuration and then re-initialize it with the default
     * values, application-specific configuration file, user-supplied properties (if any), and then
     * command-line settings.
     * <p>
     * This method <strong><em>MUST</em></strong> be called holding the configuration lock!
     * </p>
     * <p>
     * This method is a little special in that logging messages generated during the method must be
     * cached and delayed until after the logging framework has been initialized. After that, the
     * delayed logging buffer can be dumped to the log file and cleared.
     * </p>
     *
     * @param props Application-supplied configuration values, if any
     */
    private static void initialize(final Properties props) {
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        Date now = new Date();
        logger.info(
                "------------------------------------------------------------------------------");

        logger.info(Msg.CONFIGURATION_STARTED, format.format(now));

        /*
         * Clear any existing properties
         */
        config.clear();
        logger.info(Msg.CONFIGURATION_CLEARED);

        /*
         * Load the defaults (if any are present)
         */
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_PROPERTIES);
        if (in != null) {
            logger.info(Msg.LOADING_DEFAULTS, DEFAULT_PROPERTIES);
            try {
                config.setProperties(in);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("Cannot close inputStream: " + LoggingUtils.formatException(e));
                }
            }
            for (String key : config.getProperties().stringPropertyNames()) {

                if ((!StringUtils.containsIgnoreCase(key, "pass"))&&(!StringUtils.containsIgnoreCase(key, "secret")))
                    logger.info(Msg.PROPERTY_VALUE, key, config.getProperty(key));
                else
                    logger.info(Msg.PROPERTY_VALUE, key, "XXXX");
            }
        } else {
            logger.info(Msg.NO_DEFAULTS_FOUND, DEFAULT_PROPERTIES);
        }

        /*
         * Look for application configuration property file. By default, we will look for the file
         * "cdp.properties" on the user home path, then on "./etc" (relative to current path), then
         * on "../etc" (relative to current path). If we do not find any property file, then we
         * continue. Otherwise, we load the first property file we find and then continue. In order
         * to allow default values for the filename and paths to be searched, we first attempt to
         * obtain these from our configuration object (which should be primed with default values
         * and/or overridden with application-specified values). We then use the values obtained
         * from that to get any user supplied values on the command line.
         */
        String filename = config.getProperty(Configuration.PROPERTY_BOOTSTRAP_FILE_NAME,
                Configuration.DEFAULT_BOOTSTRAP_FILE_NAME);
        filename = System.getProperty(Configuration.PROPERTY_BOOTSTRAP_FILE_NAME, filename);
        String env = System.getenv(Configuration.PROPERTY_BOOTSTRAP_FILE_NAME);
        if (env != null && env.trim().length() > 0) {
            filename = env;
        }

        String path = config.getProperty(Configuration.PROPERTY_BOOTSTRAP_FILE_PATH,
                Configuration.DEFAULT_BOOTSTRAP_FILE_PATH);
        path = System.getProperty(Configuration.PROPERTY_BOOTSTRAP_FILE_PATH, path);
        env = System.getenv(Configuration.PROPERTY_BOOTSTRAP_FILE_PATH);
        if (env != null && env.trim().length() > 0) {
            path = env;
        }

        logger.info(Msg.SEARCHING_CONFIGURATION_OVERRIDES, path, filename);

        String[] pathElements = path.split(COMMA);
        boolean found = false;
        for (String pathElement : pathElements) {
            File file = new File(pathElement, filename);
            if (file.exists() && file.canRead() && !file.isDirectory()) {

                logger.info(Msg.LOADING_CONFIGURATION_OVERRIDES, file.getAbsolutePath());
                Properties fileProperties = new Properties();
                BufferedInputStream stream = null;
                try {
                    stream = new BufferedInputStream(new FileInputStream(file));
                    fileProperties.load(stream);
                    for (String key : fileProperties.stringPropertyNames()) {
                        if ((!StringUtils.containsIgnoreCase(key, "pass"))&&(!StringUtils.containsIgnoreCase(key, "secret")))
                            logger.debug(Msg.PROPERTY_VALUE, key, fileProperties.getProperty(key));
                        else
                            logger.info(Msg.PROPERTY_VALUE, key, "XXXX");
                        config.setProperty(key, fileProperties.getProperty(key));
                    }
                    found = true;
                    break;
                } catch (IOException e) {
                    logger.error(LoggingUtils.formatException(e));
                } finally {
                    try {
                        if (stream != null) {
                            stream.close();
                        }
                    } catch (IOException e) {
                        logger.error("Unable to close stream: " + LoggingUtils.formatException(e));
                    }
                }
            }
        }

        if (!found) {
            logger.warn(Msg.NO_OVERRIDE_PROPERTY_FILE_LOADED, filename, path);
        }

        /*
         * Apply any application-specified properties
         */
        if (props != null) {
            logger.info(Msg.LOADING_APPLICATION_OVERRIDES);
            for (String key : props.stringPropertyNames()) {
                if ((!StringUtils.containsIgnoreCase(key, "pass"))&&(!StringUtils.containsIgnoreCase(key, "secret")))
                    logger.debug(Msg.PROPERTY_VALUE, key, props.getProperty(key));
                else
                    logger.info(Msg.PROPERTY_VALUE, key, "XXXX");
                config.setProperty(key, props.getProperty(key));
            }
        } else {
            logger.info(Msg.NO_APPLICATION_OVERRIDES);
        }

        /*
         * Merge in the System.properties to pick-up any command line arguments (-Dkeyword=value)
         */
        logger.info(Msg.MERGING_SYSTEM_PROPERTIES);
        config.setProperties(System.getProperties());

        /*
         * As a convenience, copy the "specialProperties" that are not defined in System.properties
         * from the configuration back to the system properties object.
         */
        for (String key : config.getProperties().stringPropertyNames()) {
            for (String specialProperty : specialProperties) {
                if (key.equals(specialProperty) && !System.getProperties().containsKey(key)) {
                    System.setProperty(key, config.getProperty(key));
                    logger.info(Msg.SETTING_SPECIAL_PROPERTY, key, config.getProperty(key));
                }
            }
        }

        /*
         * Initialize the resource manager by loading the requested bundles, if any are defined.
         * Resource bundles may be specified as a comma-delimited list of names. These resource
         * names are base names of resource bundles, do not include the language or country code, or
         * the ".properties" extension. The actual loading of the resource bundles is done lazily
         * when requested the first time. If the bundle does not exist, or cannot be loaded, it is
         * ignored.
         */
        String resourcesList = config.getProperty(Configuration.PROPERTY_RESOURCE_BUNDLES,
                Configuration.DEFAULT_RESOURCE_BUNDLES);
        String[] resources = resourcesList.split(",");
        for (String resource : resources) {
            logger.info(Msg.LOADING_RESOURCE_BUNDLE, resource.trim());
            EELFResourceManager.loadMessageBundle(resource.trim());
        }
    }
}
