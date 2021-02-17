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
		return super.setProperty(key, resolveValue(value));
	}

    private void resolveAllValues() {
       Enumeration<?> propNames = propertyNames();

       while (propNames.hasMoreElements()) {
           String propName = (String) propNames.nextElement();
           super.setProperty(propName, resolveValue(getProperty(propName)));
       }
       
    }

    private String resolveValue(String value) {
        if (value == null) {
            return null;
        }

        Pattern p = Pattern.compile("\\$\\{(\\w+)\\}");
        Matcher m = p.matcher(value);

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
            String envVarValue = System.getenv(envVarName);
            m.appendReplacement(sb,
                null == envVarValue ? "" : Matcher.quoteReplacement(envVarValue));
         }
        m.appendTail(sb);
        return sb.toString();
        
    }
}
