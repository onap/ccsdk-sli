/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2021 AT&T
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

package org.onap.ccsdk.sli.core.utils.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Drop-in replacement for java.util.Properties that allows env
 * variables to be used as settings for property files.  For example,
 * 
 * my.property = ${MY_PROPERTY}
 * 
 * A default value can also be provided using :- notation.  For example,
 * 
 * my.property = ${MY_PROPERTY:-defaultValue}
 */

public class EnvProperties extends Properties {

    @Override
    public synchronized void load(Reader reader) throws IOException {
        super.load(reader);
        resolveAllValues();
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        super.load(inStream);
        resolveAllValues();
    }

	@Override
	public synchronized Object setProperty(String key, String value) {
		return super.setProperty(key, EnvProperties.resolveValue(value));
	}

    private void resolveAllValues() {
       Enumeration<?> propNames = propertyNames();

       while (propNames.hasMoreElements()) {
           String propName = (String) propNames.nextElement();
           super.setProperty(propName, EnvProperties.resolveValue(getProperty(propName)));
       }

    }

    public static String resolveValue(String value) {
        if (value == null) {
            return null;
        }

        Pattern p = Pattern.compile("\\$\\{(\\w+)((?:\\:\\-)([^\\}]*))?\\}");
        Matcher m = p.matcher(value);

        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
            String envVarDefault = null == m.group(3) ? "" : m.group(3);
            String envVarValue = System.getenv(envVarName);

            m.appendReplacement(sb,
                null == envVarValue ? Matcher.quoteReplacement(envVarDefault) : Matcher.quoteReplacement(envVarValue));
         }
        m.appendTail(sb);
        return sb.toString();

    }
}
