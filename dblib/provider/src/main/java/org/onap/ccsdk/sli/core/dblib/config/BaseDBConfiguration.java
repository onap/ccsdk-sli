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

package org.onap.ccsdk.sli.core.dblib.config;

import java.util.Properties;

/**
 * Base class responsible for parsing business logic for database configuration from given <code>Properties</code>.
 */
public abstract class BaseDBConfiguration {

    /**
     * Property key within a properties configuration File for db type
     */
    public static final String DATABASE_TYPE	= "org.onap.ccsdk.sli.dbtype";

    /**
     * Property key with a properties configuration File for db url
     */
    public static final String DATABASE_URL		= "org.onap.ccsdk.sli.jdbc.url";

    /**
     * Property key with a properties configuration File for database name
     */
    public static final String DATABASE_NAME	= "org.onap.ccsdk.sli.jdbc.database";

    /**
     * Property key with a properties configuration File for db database connection name
     */
    public static final String CONNECTION_NAME	= "org.onap.ccsdk.sli.jdbc.connection.name";

    /**
     * Property key with a properties configuration File for database user
     */
    public static final String DATABASE_USER 	= "org.onap.ccsdk.sli.jdbc.user";

    /**
     * Property key with a properties configuration File for database password for associated with
     * <code>org.onap.ccsdk.sli.jdbc.user</code>.
     */
    public static final String DATABASE_PSSWD	= "org.onap.ccsdk.sli.jdbc.password";

    /**
     * Property key with a properties configuration File for database connection timeout
     */
    public static final String CONNECTION_TIMEOUT="org.onap.ccsdk.sli.jdbc.connection.timeout";

    /**
     * Property key with a properties configuration File for database request timeout
     */
    public static final String REQUEST_TIMEOUT	= "org.onap.ccsdk.sli.jdbc.request.timeout";

    /**
     * Property key with a properties configuration File for database minimum limit
     */
    public static final String MIN_LIMIT		= "org.onap.ccsdk.sli.jdbc.limit.min";

    /**
     * Property key with a properties configuration File for database maximum limit
     */
    public static final String MAX_LIMIT		= "org.onap.ccsdk.sli.jdbc.limit.max";

    /**
     * Property key with a properties configuration File for database initial limit
     */
    public static final String INIT_LIMIT		= "org.onap.ccsdk.sli.jdbc.limit.init";

    /**
     * Property key with a properties configuration File for database hosts
     */
    public static final String DATABASE_HOSTS   = "org.onap.ccsdk.sli.jdbc.hosts";

    /**
     * default value when the connection timeout is not present or cannot be parsed
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = -1;

    /**
     * default value when the request timeout is not present or cannot be parsed
     */
    private static final int DEFAULT_REQUEST_TIMEOUT = -1;

    /**
     * A set of properties with database configuration information.
     */
    protected final Properties properties;

    /**
     * Builds a configuration based on given properties
     *
     * @param properties properties represented by the public constant keys defined by this class
     */
    public BaseDBConfiguration(final Properties properties) {
        this.properties = properties;
    }

    /**
     * Extracts the connection timeout.
     *
     * @return the connection timeout, or <code>DEFAULT_CONNECTION_TIMEOUT</code> if not present
     */
    public int getConnTimeout() {
        return extractProperty(properties, CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * Extracts the request timeout.
     *
     * @return the request timeout, or <code>DEFAULT_REQUEST_TIMEOUT</code> if not present
     */
    public int getRequestTimeout() {
        return extractProperty(properties, REQUEST_TIMEOUT, DEFAULT_REQUEST_TIMEOUT);
    }

    /**
     * A utility method to extract int property from Properties.
     *
     * @param properties a set of <code>Properties</code>
     * @param propertyKey the key in question
     * @param defaultValue the value to return if the key does not exist
     * @return Either the property value for <code>propertyKey</code> or <code>defaultValue</code> if not present
     */
    private static int extractProperty(final Properties properties, final String propertyKey, final int defaultValue) {
        try {
            final String valueString = properties.getProperty(propertyKey, Integer.toString(defaultValue));
            return Integer.parseInt(valueString);
        } catch(final NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Extracts the db connection name.
     *
     * @return the db connection name, or <code>null</code> if not present
     */
    public String getDbConnectionName() {
        return properties.getProperty(CONNECTION_NAME);
    }

    /**
     * Extracts the db name.
     *
     * @return the db name, or <code>null</code> if not present
     */
    public String getDatabaseName() {
        return properties.getProperty(DATABASE_NAME);
    }

    /**
     * Extracts the db user id.
     *
     * @return the db user id, or <code>null</code> if not present
     */
    public String getDbUserId() {
        return properties.getProperty(DATABASE_USER);
    }

    /**
     * Extracts the db password.
     *
     * @return the db password, or <code>null</code> if not present
     */
    public String getDbPasswd() {
        return properties.getProperty(DATABASE_PSSWD);
    }

    /**
     * Extracts the db min limit.
     *
     * @return the db min limit
     * @throws NumberFormatException if the property is not specified, or cannot be parsed as an <code>Integer</code>.
     */
    public int getDbMinLimit() throws NumberFormatException {
        String value = properties.getProperty(MIN_LIMIT);
        return Integer.parseInt(value);
    }

    /**
     * Extracts the db max limit.
     *
     * @return the db max limit
     * @throws NumberFormatException if the property is not specified, or cannot be parsed as an <code>Integer</code>.
     */
    public int getDbMaxLimit() throws NumberFormatException {
        String value = properties.getProperty(MAX_LIMIT);
        return Integer.parseInt(value);
    }

    /**
     * Extracts the db initial limit.
     *
     * @return the db initial limit
     * @throws NumberFormatException if the property is not specified, or cannot be parsed as an <code>Integer</code>.
     */
    public int getDbInitialLimit() throws NumberFormatException {
        String value = properties.getProperty(INIT_LIMIT);
        return Integer.parseInt(value);
    }

    /**
     * Extracts the db url.
     *
     * @return the db url, or <code>null</code> if not present
     */
    public String getDbUrl() {
        return properties.getProperty(DATABASE_URL);
    }

    /**
     * Extracts the db server group.
     *
     * @return <code>null</code>
     */
    @Deprecated
    public String getServerGroup() {
        return null;
    }
}
