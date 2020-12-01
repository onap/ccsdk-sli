/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * reserved.
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

package org.onap.ccsdk.sli.plugins.prop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesNode implements SvcLogicJavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(PropertiesNode.class);

    public void readProperties(Map<String, String> paramMap, SvcLogicContext ctx) throws SvcLogicException {
        Parameters param = getParameters(paramMap);
        Properties prop = new Properties();
        try {
            File file = new File(param.fileName);
            try (InputStream in = new FileInputStream(file)) {
                Map<String, String> mm = null;
                String pfx = param.contextPrefix != null ? param.contextPrefix + '.' : "";
                if (param.fileBasedParsing) {
                    byte[] data = new byte[(int) file.length()];
                    if ("json".equalsIgnoreCase(getFileExtension(param.fileName))) {
                        in.read(data);
                        String str = new String(data, "UTF-8");
                        mm = JsonParser.convertToProperties(str);
                    } else if ("xml".equalsIgnoreCase(getFileExtension(param.fileName))) {
                        in.read(data);
                        String str = new String(data, "UTF-8");
                        mm = XmlParser.convertToProperties(str, param.listNameList);
                    } else {
                        prop.load(in);
                        for (Object key : prop.keySet()) {
                            String name = (String) key;
                            String value = prop.getProperty(name);
                            if (value != null && value.trim().length() > 0) {
                                ctx.setAttribute(pfx + name, getObfuscatedVal(value.trim()));
                                log.info("+++ " + pfx + name + ": [" + maskPassword(pfx + name, value) + "]");
                            }
                        }
                    }
                    if (mm != null) {
                        for (Map.Entry<String, String> entry : mm.entrySet()) {
                            ctx.setAttribute(pfx + entry.getKey(), getObfuscatedVal(entry.getValue()));
                            log.info("+++ " + pfx + entry.getKey() + ": ["
                                    + maskPassword(pfx + entry.getKey(), entry.getValue()) + "]");
                        }
                    }
                } else {
                    prop.load(in);
                    for (Object key : prop.keySet()) {
                        String name = (String) key;
                        String value = prop.getProperty(name);
                        if (value != null && value.trim().length() > 0) {
                            ctx.setAttribute(pfx + name, getObfuscatedVal(value.trim()));
                            log.info("+++ " + pfx + name + ": [" + maskPassword(pfx + name, value) + "]");
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new SvcLogicException("Cannot read property file: " + param.fileName + ": " + e.getMessage(), e);
        }
    }

    /* Unobfuscate param value */ 
    private static String getObfuscatedVal(String paramValue) {
        String resValue = paramValue;
        if (paramValue != null && paramValue.startsWith("${") && paramValue.endsWith("}"))
        {
                String paramStr = paramValue.substring(2, paramValue.length()-1);
                if (paramStr  != null && paramStr.length() > 0)
                {
                        String val = System.getenv(paramStr);
                        if (val != null && val.length() > 0)
                        {
                             resValue=val;
                             log.info("Obfuscated value RESET for param value:" + paramValue);
                        }
                 }
        }
        return resValue;
    }

    /*
     * Getting extension has to do the following "" --> "" "name" --> "" "name.txt" --> "txt"
     * ".htpasswd" --> "" "name.with.many.dots.myext" --> "myext"
     */
    private static String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    protected Parameters getParameters(Map<String, String> paramMap) throws SvcLogicException {
        Parameters p = new Parameters();
        p.fileName = parseParam(paramMap, "fileName", true, null);
        p.contextPrefix = parseParam(paramMap, "contextPrefix", false, null);
        p.listNameList = getListNameList(paramMap);
        String fileBasedParsingStr = paramMap.get("fileBasedParsing");
        p.fileBasedParsing = "true".equalsIgnoreCase(fileBasedParsingStr);
        return p;
    }

    protected Set<String> getListNameList(Map<String, String> paramMap) {
        Set<String> ll = new HashSet<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (entry.getKey().startsWith("listName")) {
                ll.add(entry.getValue());
            }
        }
        return ll;
    }

    private String parseParam(Map<String, String> paramMap, String name, boolean required, String def)
            throws SvcLogicException {
        String s = paramMap.get(name);

        if (s == null || s.trim().length() == 0) {
            if (!required) {
                return def;
            }
            throw new SvcLogicException("Parameter " + name + " is required in PropertiesNode");
        }

        s = s.trim();
        String value = "";
        int i = 0;
        int i1 = s.indexOf('%');
        while (i1 >= 0) {
            int i2 = s.indexOf('%', i1 + 1);
            if (i2 < 0) {
                throw new SvcLogicException("Cannot parse parameter " + name + ": " + s + ": no matching %");
            }

            String varName = s.substring(i1 + 1, i2);
            String varValue = System.getenv(varName);
            if (varValue == null) {
                varValue = "";
            }

            value += s.substring(i, i1);
            value += varValue;

            i = i2 + 1;
            i1 = s.indexOf('%', i);
        }
        value += s.substring(i);

        log.info("Parameter " + name + ": " + maskPassword(name, value));
        return value;
    }

    private static Object maskPassword(String name, Object value) {
        String[] pwdNames = {"pwd", "passwd", "password", "Pwd", "Passwd", "Password"};
        for (String pwdName : pwdNames) {
            if (name.contains(pwdName)) {
                return "**********";
            }
        }
        return value;
    }
}
