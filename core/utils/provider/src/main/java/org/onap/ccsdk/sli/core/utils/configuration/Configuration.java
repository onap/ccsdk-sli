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

import java.util.Properties;

/**
 * This interface defines the common configuration support that is available to the application.
 * <p>
 * Where properties are common to all CDP components (server, coordinator, and EPM), the property symbolic values are
 * defined as part of this interface. Where they are unique to each component, they must be defined within that
 * component.
 * </p>
 */
public interface Configuration {

    String PROPERTY_BOOTSTRAP_FILE_NAME = "org_onap_appc_bootstrap_file"; //
    String DEFAULT_BOOTSTRAP_FILE_NAME = "appc.properties";
    String PROPERTY_BOOTSTRAP_FILE_PATH = "org_onap_appc_bootstrap_path"; //
    String DEFAULT_BOOTSTRAP_FILE_PATH = "/opt/onap/appc/data/properties,${user.home},etc,../etc";
    String PROPERTY_RESOURCE_BUNDLES = "org.onap.appc.resources";
    String DEFAULT_RESOURCE_BUNDLES = "org/onap/appc/i18n/MessageResources";

   /**
     * This method is called to obtain a property expressed as a boolean value (true or false). The standard rules for
     * {@link Boolean#valueOf(String)} are used.
     *
     * @param key
     *            The property key
     * @return The value of the property expressed as a boolean, or false if it does not exist.
     */
    boolean getBooleanProperty(String key);

    /**
     * This method is called to obtain a property expressed as a boolean value (true or false). The standard rules for
     * {@link Boolean#valueOf(String)} are used.
     *
     * @param key
     *            The property key
     * @param defaultValue
     *            The default value to be returned if the property does not exist
     * @return The value of the property expressed as a boolean, or false if it does not exist.
     */
    boolean getBooleanProperty(String key, boolean defaultValue);

    /**
     * Returns the indicated property value expressed as a floating point double-precision value (double). The standard
     * rules for {@link Double#valueOf(String)} are used.
     *
     * @param key
     *            The property to retrieve
     * @return The value of the property, or 0.0 if not found or invalid
     */
    double getDoubleProperty(String key);

    /**
     * Returns the indicated property value expressed as a floating point double-precision value (double). The standard
     * rules for {@link Double#valueOf(String)} are used.
     *
     * @param key
     *            The property to retrieve
     * @param defaultValue
     *            The default value to be returned if the property does not exist
     * @return The value of the property, or 0.0 if not found or invalid
     */
    double getDoubleProperty(String key, double defaultValue);

    /**
     * Returns the property indicated expressed as an integer. The standard rules for
     * {@link Integer#parseInt(String, int)} using a radix of 10 are used.
     *
     * @param key
     *            The property name to retrieve.
     * @return The value of the property, or 0 if it does not exist or is invalid.
     */
    int getIntegerProperty(String key);

    /**
     * Returns the property indicated expressed as an integer. The standard rules for
     * {@link Integer#parseInt(String, int)} using a radix of 10 are used.
     *
     * @param key
     *            The property name to retrieve.
     * @param defaultValue
     *            The default value to be returned if the property does not exist
     * @return The value of the property, or 0 if it does not exist or is invalid.
     */
    int getIntegerProperty(String key, int defaultValue);

    /**
     * Returns the specified property as a long integer value, if it exists, or zero if it does not.
     *
     * @param key
     *            The key of the property desired.
     * @return The value of the property expressed as an integer long value, or zero if the property does not exist or
     *         is not a valid integer long.
     */
    long getLongProperty(String key);

    /**
     * Returns the specified property as a long integer value, if it exists, or the default value if it does not exist
     * or is invalid.
     *
     * @param key
     *            The key of the property desired.
     * @param defaultValue
     *            the value to be returned if the property is not valid or does not exist.
     * @return The value of the property expressed as an integer long value, or the default value if the property does
     *         not exist or is not a valid integer long.
     */
    long getLongProperty(String key, long defaultValue);

    /**
     * This method can be called to retrieve a properties object that is immutable. Any attempt to modify the properties
     * object returned will result in an exception. This allows a caller to view the current configuration as a set of
     * properties.
     *
     * @return An unmodifiable properties object.
     */
    Properties getProperties();

    /**
     * This method is called to obtain a property as a string value
     *
     * @param key
     *            The key of the property
     * @return The string value, or null if it does not exist.
     */
    String getProperty(String key);

    /**
     * This method is called to obtain a property as a string value
     *
     * @param key
     *            The key of the property
     * @param defaultValue
     *            The default value to be returned if the property does not exist
     * @return The string value, or null if it does not exist.
     */
    String getProperty(String key, String defaultValue);

    /**
     * Returns true if the named property is defined, false otherwise.
     *
     * @param key
     *            The key of the property we are interested in
     * @return True if the property exists.
     */
    boolean isPropertyDefined(String key);

    /**
     * Returns an indication of the validity of the boolean property. A boolean property is considered to be valid only
     * if it has the value "true" or "false" (ignoring case).
     *
     * @param key
     *            The property to be checked
     * @return True if the value is a boolean constant, or false if it does not exist or is not a correct string
     */
    boolean isValidBoolean(String key);

    /**
     * Returns an indication if the indicated property represents a valid double-precision floating point number.
     *
     * @param key
     *            The property to be examined
     * @return True if the property is a valid representation of a double, or false if it does not exist or contains
     *         illegal characters.
     */
    boolean isValidDouble(String key);

    /**
     * Returns an indication if the property is a valid integer value or not.
     *
     * @param key
     *            The key of the property to check
     * @return True if the value is a valid integer string, or false if it does not exist or contains illegal
     *         characters.
     */
    boolean isValidInteger(String key);

    /**
     * Determines is the specified property exists and is a valid representation of an integer long value.
     *
     * @param key
     *            The property to be checked
     * @return True if the property is a valid representation of an integer long value, and false if it either does not
     *         exist or is not valid.
     */
    boolean isValidLong(String key);

    /**
     * This method allows the caller to set all properties from a provided properties object into the configuration
     * property set.
     * <p>
     * The primary difference between this method and the factory method
     * {@link org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory#getConfiguration(Properties)} is that this method does not clear and reload the
     * configuration. Rather, this method merges the provided properties object contents into the existing properties,
     * replacing any same-named keys with the values from this object.
     * </p>
     *
     * @param properties
     *            The properties object to copy all properties from
     */
    void setProperties(Properties properties);

    /**
     * This method allows a caller to insert a new property definition into the configuration object. This allows the
     * application to adjust or add to the current configuration. If the property already exists, it is replaced with
     * the new value.
     *
     * @param key
     *            The key of the property to be defined
     * @param value
     *            The value of the property to be defined
     */
    void setProperty(String key, String value);
}
