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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This utility class is used to wrap a properties object and to delegate all read operations to the property object,
 * while disallowing any write or modification to the property object.
 *
 */
public class UnmodifiableProperties extends Properties implements Cloneable {

    /**
     * Serial number
     */
    private static final long serialVersionUID = 1L;

    private static final String PROPERTY_CANNOT_BE_MODIFIED_MSG = "Property cannot be modified!";

    /**
     * The properties object which we are wrapping
     */
    private Properties properties;

    /**
     * Create the unmodifiable wrapper around the provided properties object
     *
     * @param properties
     *            The properties to be wrapped and protected from modification
     */
    public UnmodifiableProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * @see java.util.Hashtable#clear()
     */
    @Override
    public synchronized void clear() {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    /**
     * @see java.util.Hashtable#clone()
     */
    // @sonar:off
    @Override
    public synchronized Object clone() {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    // @sonar:on

    /**
     * @see java.util.Hashtable#contains(Object)
     */
    @Override
    public synchronized boolean contains(Object value) {
        return properties.contains(value);
    }

    /**
     * @see java.util.Hashtable#containsKey(Object)
     */
    @Override
    public synchronized boolean containsKey(Object key) {
        return properties.containsKey(key);
    }

    /**
     * @see java.util.Hashtable#containsValue(Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    /**
     * @see java.util.Hashtable#elements()
     */
    @Override
    public synchronized Enumeration<Object> elements() {
        return properties.elements();
    }

    /**
     * @see java.util.Hashtable#entrySet()
     */
    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return Collections.unmodifiableSet(properties.entrySet());
    }

    /**
     * @see java.util.Hashtable#equals(Object)
     */
    @Override
    public synchronized boolean equals(Object o) {
        return properties.equals(o);
    }

    /**
     * @see java.util.Hashtable#get(Object)
     */
    @Override
    public synchronized Object get(Object key) {
        return properties.get(key);
    }

    /**
     * @see Properties#getProperty(String)
     */
    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * @see Properties#getProperty(String, String)
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * @see java.util.Hashtable#hashCode()
     */
    @Override
    public synchronized int hashCode() {
        return properties.hashCode();
    }

    /**
     * @see java.util.Hashtable#isEmpty()
     */
    @Override
    public synchronized boolean isEmpty() {
        return properties.isEmpty();
    }

    /**
     * @see java.util.Hashtable#keys()
     */
    @Override
    public synchronized Enumeration<Object> keys() {
        return properties.keys();
    }

    /**
     * @see java.util.Hashtable#keySet()
     */
    @Override
    public Set<Object> keySet() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    /**
     * @see Properties#list(PrintStream)
     */
    @Override
    public void list(PrintStream out) {
        properties.list(out);
    }

    /**
     * @see Properties#list(PrintWriter)
     */
    @Override
    public void list(PrintWriter out) {
        properties.list(out);
    }

    /**
     * @see Properties#load(InputStream)
     */
    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    /**
     * @see Properties#load(Reader)
     */
    @Override
    public synchronized void load(Reader reader) throws IOException {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    /**
     * @see Properties#loadFromXML(InputStream)
     */
    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    /**
     * @see Properties#propertyNames()
     */
    @Override
    public Enumeration<?> propertyNames() {
        return properties.propertyNames();
    }

    /**
     * @see java.util.Hashtable#put(Object, Object)
     */
    @Override
    public synchronized Object put(Object key, Object value) {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    /**
     * @see java.util.Hashtable#putAll(Map)
     */
    @Override
    public synchronized void putAll(Map<? extends Object, ? extends Object> t) {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    /**
     * @see java.util.Hashtable#rehash()
     */
    @Override
    protected void rehash() {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    /**
     * @see java.util.Hashtable#remove(Object)
     */
    @Override
    public synchronized Object remove(Object key) {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    /**
     * @see Properties#save(OutputStream, String)
     */
    @Override
    @Deprecated
    public synchronized void save(OutputStream out, String comments) {
        properties.save(out, comments);
    }

    /**
     * @see Properties#setProperty(String, String)
     */
    @Override
    public synchronized Object setProperty(String key, String value) {
        throw new UnsupportedOperationException(PROPERTY_CANNOT_BE_MODIFIED_MSG);
    }

    /**
     * @see java.util.Hashtable#size()
     */
    @Override
    public synchronized int size() {
        return properties.size();
    }

    /**
     * @see Properties#store(OutputStream, String)
     */
    @Override
    public void store(OutputStream out, String comments) throws IOException {
        properties.store(out, comments);
    }

    /**
     * @see Properties#store(Writer, String)
     */
    @Override
    public void store(Writer writer, String comments) throws IOException {
        properties.store(writer, comments);
    }

    /**
     * @see Properties#storeToXML(OutputStream, String)
     */
    @Override
    public synchronized void storeToXML(OutputStream os, String comment) throws IOException {
        properties.storeToXML(os, comment);
    }

    /**
     * @see Properties#storeToXML(OutputStream, String, String)
     */
    @Override
    public synchronized void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        properties.storeToXML(os, comment, encoding);
    }

    /**
     * @see Properties#stringPropertyNames()
     */
    @Override
    public Set<String> stringPropertyNames() {
        return properties.stringPropertyNames();
    }

    /**
     * @see java.util.Hashtable#toString()
     */
    @Override
    public synchronized String toString() {
        return properties.toString();
    }

    /**
     * @see java.util.Hashtable#values()
     */
    @Override
    public Collection<Object> values() {
        return Collections.unmodifiableCollection(properties.values());
    }
}
