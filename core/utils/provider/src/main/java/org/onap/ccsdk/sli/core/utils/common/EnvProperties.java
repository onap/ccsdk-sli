package org.onap.ccsdk.sli.core.utils.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
