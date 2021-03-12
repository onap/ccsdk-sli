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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.onap.ccsdk.sli.core.utils.encryption.EncryptionTool;
import org.onap.ccsdk.sli.core.utils.logging.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the implementation of the <code>Configuration</code> interface. It is created
 * by the ConfigurationFactory and initialized with the configuration values for the process.
 *
 * @since Mar 18, 2014
 */
public final class DefaultConfiguration implements Configuration, Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfiguration.class);

    /**
     * The framework configuration properties.
     */
    private Properties properties = new Properties();

    /**
     * Construct the configuration object.
     */
    DefaultConfiguration() {}

    /**
     * Clears all properties
     */
    public void clear() {
        properties.clear();
    }

    /**
     * @see Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        DefaultConfiguration clone = (DefaultConfiguration) super.clone();

        clone.properties = new Properties(this.properties);
        clone.properties.putAll(this.properties);

        return clone;
    }

    /**
     * Decrypts an encrypted value, if it is encrypted, and returns the clear text. Performs no
     * operation on the string if it is not encrypted.
     *
     * @param value The value to (optionally) be decrypted
     * @return The clear text
     */
    @SuppressWarnings("nls")
    private static String decrypt(String value) {
        if (value != null && value.startsWith(EncryptionTool.ENCRYPTED_VALUE_PREFIX)) {
            try {
                return EncryptionTool.getInstance().decrypt(value);
            } catch (Exception e) {
                StringBuilder out = new StringBuilder();
                for (Provider p : Security.getProviders()) {
                    for (Service s : p.getServices()) {
                        String algo = s.getAlgorithm();
                        out.append(String.format(
                                "\n==Found Algorithm [ %s ] in provider [ %s ] and service [ %s ]",
                                algo, p.getName(), s.getClassName()));
                    }
                }
                logger.debug(out.toString());
                logger.warn(String.format("Could not decrypt the configuration value [%s]", value),
                        e);
            }
        }
        return value;
    }

    /**
     * Decrypts all elements in the properties object
     */
    private void decryptAllProperties() {
        if (properties != null) {
            for (Entry<Object, Object> e : properties.entrySet()) {
                if (e.getValue() != null) {
                    e.setValue(decrypt(e.getValue().toString()));
                }
            }
        }
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        DefaultConfiguration other = (DefaultConfiguration) obj;

        return (this.properties.size() == other.properties.size())
                && (this.properties.entrySet().containsAll(other.properties.entrySet()))
                && (other.properties.entrySet().containsAll(this.properties.entrySet()));

    }

    /**
     * This method will use the properties object to expand any variables that may be present in the
     * template provided. Variables are represented by the string "${name}", where "name" is the
     * name of a property defined in either the current configuration object, or system properties
     * if undefined. If the value cannot be found, the variable is removed and an empty string is
     * used to replace the variable.
     *
     * @param template The template to be expanded
     * @return The expanded template where each variable is replaced with its value
     */
    @SuppressWarnings("nls")
    private String expandVariables(String template) {
        if (template == null) {
            return null;
        }

        // Decrypt the template if needed
        // template = decrypt(template); DH: Do not assign values to parameters, bad form! Also,
        // Sonar complains
        // bitterly

        StringBuilder builder = new StringBuilder(decrypt(template));
        Pattern pattern = Pattern.compile("\\$\\{([^\\}]+)\\}");
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            String variable = matcher.group(1);
            String value = properties.getProperty(variable);
            if (value == null) {
                value = System.getProperty(variable);
            }
            if (value == null) {
                value = "";
            }
            builder.replace(matcher.start(), matcher.end(), value);

            matcher.reset();
        }
        return builder.toString().trim();
    }

    /**
     * This method is called to obtain a property expressed as a boolean value (true or false). The
     * standard rules for Boolean.parseBoolean() are used.
     *
     * @param key The property key
     * @return The value of the property expressed as a boolean, or false if it does not exist.
     */
    @SuppressWarnings("nls")
    @Override
    public boolean getBooleanProperty(String key) {
        return Boolean.valueOf(getProperty(key, "false"));
    }

    /**
     * This method is called to obtain a property expressed as a boolean value (true or false). The
     * standard rules for Boolean.valueOf(String) are used.
     *
     * @param key The property key
     * @param defaultValue The default value to be returned if the property does not exist
     * @return The value of the property expressed as a boolean, or false if it does not exist.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#getBooleanProperty(String, boolean)
     */
    @Override
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        if (isPropertyDefined(key)) {
            return getBooleanProperty(key);
        }
        return defaultValue;
    }

    /**
     * Returns the indicated property value expressed as a floating point double-precision value
     * (double).
     *
     * @param key The property to retrieve
     * @return The value of the property, or 0.0 if not found
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#getDoubleProperty(String)
     */
    @SuppressWarnings("nls")
    @Override
    public double getDoubleProperty(String key) {
        try {
            return Double.valueOf(getProperty(key, "0.0"));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * This method is called to obtain a property as a string value
     *
     * @param key The key of the property
     * @param defaultValue The default value to be returned if the property does not exist
     * @return The string value, or null if it does not exist.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#getDoubleProperty(String, double)
     */
    @Override
    public double getDoubleProperty(String key, double defaultValue) {
        if (isPropertyDefined(key)) {
            return getDoubleProperty(key);
        }
        return defaultValue;
    }

    /**
     * Returns the property indicated expressed as an integer. The standard rules for
     * {@link Integer#parseInt(String, int)} using a radix of 10 are used.
     *
     * @param key The property name to retrieve.
     * @return The value of the property, or 0 if it does not exist or is invalid.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#getIntegerProperty(String)
     */
    @SuppressWarnings("nls")
    @Override
    public int getIntegerProperty(String key) {
        try {
            return Integer.parseInt(getProperty(key, "0"), 10);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Returns the property indicated expressed as an integer. The standard rules for
     * Integer.parseInt(String, int) using a radix of 10 are used.
     *
     * @param key The property name to retrieve.
     * @param defaultValue The default value to be returned if the property does not exist
     * @return The value of the property, or 0 if it does not exist or is invalid.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#getIntegerProperty(String, int)
     */
    @Override
    public int getIntegerProperty(String key, int defaultValue) {
        if (isPropertyDefined(key)) {
            return getIntegerProperty(key);
        }
        return defaultValue;
    }

    /**
     * Returns the specified property as a long integer value, if it exists, or zero if it does not.
     *
     * @param key The key of the property desired.
     * @return The value of the property expressed as an integer long value, or zero if the property
     *         does not exist or is not a valid integer long.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#getLongProperty(String)
     */
    @SuppressWarnings("nls")
    @Override
    public long getLongProperty(String key) {
        try {
            return Long.parseLong(getProperty(key, "0"), 10);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Returns the specified property as a long integer value, if it exists, or the default value if
     * it does not exist or is invalid.
     *
     * @param key The key of the property desired.
     * @param defaultValue the value to be returned if the property is not valid or does not exist.
     * @return The value of the property expressed as an integer long value, or the default value if
     *         the property does not exist or is not a valid integer long.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#getLongProperty(String, long)
     */
    @Override
    public long getLongProperty(String key, long defaultValue) {
        if (isPropertyDefined(key)) {
            return getLongProperty(key);
        }
        return defaultValue;
    }

    /**
     * This method can be called to retrieve a properties object that is immutable. Any attempt to
     * modify the properties object returned will result in an exception. This allows a caller to
     * view the current configuration as a set of properties.
     *
     * @return An unmodifiable properties object.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#getProperties()
     */
    @Override
    public Properties getProperties() {
        return new UnmodifiableProperties(properties);
    }

    /**
     * This method is called to obtain a property as a string value
     *
     * @param key The key of the property
     * @return The string value, or null if it does not exist.
     */
    @Override
    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return null;
        }
        return expandVariables(value.trim());
    }

    /**
     * This method is called to obtain a property as a string value
     *
     * @param key The key of the property
     * @param defaultValue The default value to be returned if the property does not exist
     * @return The string value, or null if it does not exist.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#getProperty(String,
     *      String)
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        if (isPropertyDefined(key)) {
            return getProperty(key);
        }

        if (defaultValue == null) {
            return null;
        }

        return expandVariables(defaultValue.trim());
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return properties == null ? 0 : properties.hashCode();
    }

    /**
     * Returns true if the named property is defined, false otherwise.
     *
     * @param key The key of the property we are interested in
     * @return True if the property exists.
     */
    @Override
    public boolean isPropertyDefined(String key) {
        return properties.containsKey(key);
    }

    /**
     * Returns an indication of the validity of the boolean property. A boolean property is
     * considered to be valid only if it has the value "true" or "false" (ignoring case).
     *
     * @param key The property to be checked
     * @return True if the value is a boolean constant, or false if it does not exist or is not a
     *         correct string
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#isValidBoolean(String)
     */
    @SuppressWarnings("nls")
    @Override
    public boolean isValidBoolean(String key) {
        String value = getProperty(key);
        if (value != null) {
            value = value.toLowerCase();
            return value.matches("true|false");
        }
        return false;
    }

    /**
     * Returns an indication if the indicated property represents a valid double-precision floating
     * point number.
     *
     * @param key The property to be examined
     * @return True if the property is a valid representation of a double, or false if it does not
     *         exist or contains illegal characters.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#isValidDouble(String)
     */
    @Override
    public boolean isValidDouble(String key) {
        String value = getProperty(key);
        if (value != null) {
            try {
                Double.valueOf(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Returns an indication if the property is a valid integer value or not.
     *
     * @param key The key of the property to check
     * @return True if the value is a valid integer string, or false if it does not exist or
     *         contains illegal characters.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#isValidInteger(String)
     */
    @Override
    public boolean isValidInteger(String key) {
        String value = getProperty(key);
        if (value != null) {
            try {
                Integer.parseInt(value.trim(), 10);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Determines is the specified property exists and is a valid representation of an integer long
     * value.
     *
     * @param key The property to be checked
     * @return True if the property is a valid representation of an integer long value, and false if
     *         it either does not exist or is not valid.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#isValidLong(String)
     */
    @Override
    public boolean isValidLong(String key) {
        String value = getProperty(key);
        if (value != null) {
            try {
                Long.parseLong(value.trim(), 10);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * This method allows an implementation to load configuration properties that may override
     * default values.
     *
     * @param is An input stream that contains the properties to be loaded
     */
    public void setProperties(InputStream is) {
        try {
            properties.load(is);
        } catch (IOException e) {
            logger.warn("setProperties with inputStream got exception", e);
        }
    }

    /**
     * This method allows an implementation to load configuration properties that may override
     * default values.
     *
     * @param props An optional Properties object to be merged into the configuration, replacing any
     *        same-named properties.
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#setProperties(Properties)
     */
    @Override
    public void setProperties(Properties props) {
        properties.putAll(props);
        decryptAllProperties();
    }

    /**
     * This method allows a caller to insert a new property definition into the configuration
     * object. This allows the application to adjust or add to the current configuration. If the
     * property already exists, it is replaced with the new value.
     *
     * @param key The key of the property to be defined
     * @param value The value of the property to be defined
     * @see org.onap.ccsdk.sli.core.utils.configuration.Configuration#setProperty(String,
     *      String)
     */
    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, decrypt(value));
    }

    /**
     * @see Object#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return String.format("Configuration: %d properties, keys:[%s]", properties.size(),
                properties.keySet().toString());
    }

    /**
     * This is a helper method to read the manifest of the jar file that this class was loaded from.
     * Note that this will only work if the code is packaged in a jar file. If it is an open
     * deployment, such as under eclipse, this will not work and there is code added to detect that
     * case.
     *
     * @return The manifest object from the jar file, or null if the code is not packaged in a jar
     *         file.
     */
    @SuppressWarnings({"unused", "nls"})
    private Manifest getManifest() {
        ProtectionDomain domain = getClass().getProtectionDomain();
        CodeSource source = domain.getCodeSource();
        URL location = source.getLocation();
        String path = location.getPath();
        int index = path.indexOf('!');
        if (index != -1) {
            path = path.substring(0, index);
        }
        if (path.endsWith(".jar")) {
            try (JarFile jar = new JarFile(location.getFile())) {
                return jar.getManifest();
            } catch (IOException e) {
                logger.error("getManifest: " + LoggingUtils.formatException(e));
            }
        }

        return null;
    }
}
