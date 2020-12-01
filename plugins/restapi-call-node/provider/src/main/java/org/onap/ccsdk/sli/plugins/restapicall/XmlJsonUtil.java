/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.plugins.restapicall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XmlJsonUtil {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(XmlJsonUtil.class);

    private XmlJsonUtil() {
        // Preventing instantiation of the same.
    }

    public static String getXml(Map<String, String> varmap, String var) {
        boolean escape = true;
        if (var.startsWith("'")) {
            var = var.substring(1);
            escape = false;
        }

        Object o = createStructure(varmap, var);
        return generateXml(o, 0, escape);
    }

    public static String getJson(Map<String, String> varmap, String var) {
        boolean escape = true;
        if (var.startsWith("'")) {
            var = var.substring(1);
            escape = false;
        }

        boolean quotes = true;
        if (var.startsWith("\"")) {
            var = var.substring(1);
            quotes = false;
        }

        Object o = createStructure(varmap, var);
        return generateJson(o, escape, quotes);
    }

    private static Object createStructure(Map<String, String> flatmap, String var) {
        if (flatmap.containsKey(var)) {
            return flatmap.get(var);
        }

        Map<String, Object> mm = new HashMap<>();
        List<Object> ll = new ArrayList<>();

        for (Map.Entry<String, String> e : flatmap.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();

            if (key.endsWith("_length") || key.endsWith("].key")) {
                continue;
            }

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            if (key.startsWith(var + "[")) {
                String newKey = key.substring(var.length());
                set(ll, newKey, value);
            } else if (var == null || var.isEmpty()) {
                set(mm, key, value);
            } else if (key.startsWith(var + ".")) {
                String newKey = key.substring(var.length() + 1);
                set(mm, newKey, value);
            }
        }

        if (!mm.isEmpty()) {
            return mm;
        }
        if (!ll.isEmpty()) {
            return ll;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void set(Object struct, String compositeKey, Object value) {
        if (struct == null) {
            throw new IllegalArgumentException("Null argument: struct");
        }

        if (compositeKey == null || compositeKey.length() == 0) {
            throw new IllegalArgumentException("Null or empty argument: compositeKey");
        }

        if (value == null) {
            return;
        }

        List<Object> keys = splitCompositeKey(compositeKey);
        Object currentValue = struct;
        String currentKey = "";

        for (int i = 0; i < keys.size() - 1; i++) {
            Object key = keys.get(i);

            if (key instanceof Integer) {
                if (!(currentValue instanceof List)) {
                    throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References list '" + currentKey + "', but '" + currentKey + "' is not a list");
                }

                Integer keyi = (Integer) key;
                List<Object> currentValueL = (List<Object>) currentValue;
                int size = currentValueL.size();

                if (keyi >= size) {
                    for (int k = 0; k < keyi - size + 1; k++) {
                        currentValueL.add(null);
                    }
                }

                Object newValue = currentValueL.get(keyi);
                if (newValue == null) {
                    Object nextKey = keys.get(i + 1);
                    if (nextKey instanceof Integer) {
                        newValue = new ArrayList<>();
                    } else {
                        newValue = new HashMap<>();
                    }
                    currentValueL.set(keyi, newValue);
                }

                currentValue = newValue;
                currentKey += "[" + key + "]";

            } else {
                if (!(currentValue instanceof Map)) {
                    throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References map '" + currentKey + "', but '" + currentKey + "' is not a map");
                }

                Object newValue = ((Map<String, Object>) currentValue).get(key);
                if (newValue == null) {
                    Object nextKey = keys.get(i + 1);
                    if (nextKey instanceof Integer) {
                        newValue = new ArrayList<>();
                    } else {
                        newValue = new HashMap<>();
                    }
                    ((Map<String, Object>) currentValue).put((String) key, newValue);
                }

                currentValue = newValue;
                currentKey += "." + key;
            }
        }

        Object key = keys.get(keys.size() - 1);
        if (key instanceof Integer) {
            if (!(currentValue instanceof List)) {
                throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References list '" + currentKey + "', but '" + currentKey + "' is not a list");
            }

            Integer keyi = (Integer) key;
            List<Object> currentValueL = (List<Object>) currentValue;
            int size = currentValueL.size();

            if (keyi >= size) {
                for (int k = 0; k < keyi - size + 1; k++) {
                    currentValueL.add(null);
                }
            }

            currentValueL.set(keyi, value);

        } else {
            if (!(currentValue instanceof Map)) {
                throw new IllegalArgumentException("Cannot resolve: " + compositeKey + ": References map '" + currentKey + "', but '" + currentKey + "' is not a map");
            }

            ((Map<String, Object>) currentValue).put((String) key, value);
        }
    }

    private static List<Object> splitCompositeKey(String compositeKey) {
        if (compositeKey == null) {
            return Collections.emptyList();
        }

        String[] ss = compositeKey.split("\\.");
        List<Object> ll = new ArrayList<>();
        for (String s : ss) {
            if (s.length() == 0) {
                continue;
            }

            int i1 = s.indexOf('[');
            if (i1 < 0) {
                ll.add(s);
            } else {
                if (!s.endsWith("]")) {
                    throw new IllegalArgumentException("Invalid composite key: " + compositeKey + ": No matching ] found");
                }

                String s1 = s.substring(0, i1);
                if (s1.length() > 0) {
                    ll.add(s1);
                }

                String s2 = s.substring(i1 + 1, s.length() - 1);
                try {
                    int n = Integer.parseInt(s2);
                    if (n < 0) {
                        throw new IllegalArgumentException("Invalid composite key: " + compositeKey + ": Index must be >= 0: " + n);
                    }

                    ll.add(n);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid composite key: " + compositeKey + ": Index not a number: " + s2);
                }
            }
        }

        return ll;
    }

    @SuppressWarnings("unchecked")
    private static String generateXml(Object o, int indent, boolean escape) {
        if (o == null) {
            return null;
        }

        if (o instanceof String) {
            return escape ? StringEscapeUtils.escapeXml10((String) o) : (String) o;
        };

        if (o instanceof Map) {
            StringBuilder ss = new StringBuilder();
            Map<String, Object> mm = (Map<String, Object>) o;
            for (Map.Entry<String, Object> entry : mm.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                if (v instanceof String) {
                    String s = escape ? StringEscapeUtils.escapeXml10((String) v) : (String) v;
                    ss.append(pad(indent)).append('<').append(key).append('>');
                    ss.append(s);
                    ss.append("</").append(key).append('>').append('\n');
                } else if (v instanceof Map) {
                    ss.append(pad(indent)).append('<').append(key).append('>').append('\n');
                    ss.append(generateXml(v, indent + 1, escape));
                    ss.append(pad(indent)).append("</").append(key).append('>').append('\n');
                } else if (v instanceof List) {
                    List<Object> ll = (List<Object>) v;
                    for (Object o1 : ll) {
                        ss.append(pad(indent)).append('<').append(key).append('>').append('\n');
                        ss.append(generateXml(o1, indent + 1, escape));
                        ss.append(pad(indent)).append("</").append(key).append('>').append('\n');
                    }
                }
            }
            return ss.toString();
        }

        return null;
    }
    private static String generateJson(Object o, boolean escape, boolean quotes) {
        if (o == null) {
            return null;
        }
        if (o instanceof String && ((String) o).length() == 0) {
            return null;
        }

        StringBuilder ss = new StringBuilder();
        generateJson(ss, o, 0, false, escape, quotes);
        return ss.toString();
    }

    @SuppressWarnings("unchecked")
    private static void generateJson(StringBuilder ss, Object o, int indent, boolean padFirst, boolean escape, boolean quotes) {
        if (o instanceof String) {
            String s = escape ? StringEscapeUtils.escapeJson((String) o) : (String) o;
            if (padFirst) {
                ss.append(pad(indent));
            }
            if (quotes) {
                ss.append('"').append(s).append('"');
            } else {
                ss.append(s);
            }
            return;
        }

        if (o instanceof Map) {
            Map<String, Object> mm = (Map<String, Object>) o;

            if (padFirst) {
                ss.append(pad(indent));
            }
            ss.append("{\n");

            boolean first = true;
            for (Map.Entry<String, Object> entry : mm.entrySet()) {
                if (!first) {
                    ss.append(",\n");
                }
                first = false;
                Object v = entry.getValue();
                String key = entry.getKey();
                ss.append(pad(indent + 1)).append('"').append(key).append("\": ");
                generateJson(ss, v, indent + 1, false, escape, true);
            }

            ss.append("\n");
            ss.append(pad(indent)).append('}');

            return;
        }

        if (o instanceof List) {
            List<Object> ll = (List<Object>) o;

            if (padFirst) {
                ss.append(pad(indent));
            }
            ss.append("[\n");

            boolean first = true;
            for (Object o1 : ll) {
                if (!first) {
                    ss.append(",\n");
                }
                first = false;

                generateJson(ss, o1, indent + 1, true, escape, quotes);
            }

            ss.append("\n");
            ss.append(pad(indent)).append(']');
        }
    }

    public static String removeLastCommaJson(String s) {
        StringBuilder sb = new StringBuilder();
        int k = 0;
        int start = 0;
        while (k < s.length()) {
            int i11 = s.indexOf('}', k);
            int i12 = s.indexOf(']', k);
            int i1 = -1;
            if (i11 < 0) {
                i1 = i12;
            } else if (i12 < 0) {
                i1 = i11;
            } else {
                i1 = i11 < i12 ? i11 : i12;
            }
            if (i1 < 0) {
                break;
            }

            int i2 = s.lastIndexOf(',', i1);
            if (i2 < 0) {
                k = i1 + 1;
                continue;
            }

            String between = s.substring(i2 + 1, i1);
            if (between.trim().length() > 0) {
                k = i1 + 1;
                continue;
            }

            sb.append(s.substring(start, i2));
            start = i2 + 1;
            k = i1 + 1;
        }

        sb.append(s.substring(start, s.length()));

        return sb.toString();
    }

    public static String removeEmptyStructJson(String template, String s) {
        int k = 0;
        while (k < s.length()) {
            boolean curly = true;
            int i11 = s.indexOf('{', k);
            int i12 = s.indexOf('[', k);
            int i1 = -1;
            if (i11 < 0) {
                i1 = i12;
                curly = false;
            } else if (i12 < 0) {
                i1 = i11;
            } else if (i11 < i12) {
                i1 = i11;
            } else {
                i1 = i12;
                curly = false;
            }

            if (i1 >= 0) {
                int i2 = curly ? s.indexOf('}', i1) : s.indexOf(']', i1);
                if (i2 > 0) {
                    String value = s.substring(i1 + 1, i2);
                    if (value.trim().length() == 0) {
                        int i4 = s.lastIndexOf('\n', i1);
                        if (i4 < 0) {
                            i4 = 0;
                        }
                        int i5 = s.indexOf('\n', i2);
                        if (i5 < 0) {
                            i5 = s.length();
                        }


                        /*If template mandates empty construct to be present, those should not be removed.*/
                        if (template != null && template.contains(s.substring(i4))) {
                            k = i1 + 1;
                        } else {
                            s = s.substring(0, i4) + s.substring(i5);
                            k = 0;
                        }
                    } else {
                        k = i1 + 1;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return s;
    }

    public static String removeEmptyStructXml(String s) {
        int k = 0;
        while (k < s.length()) {
            int i1 = s.indexOf('<', k);
            if (i1 < 0 || i1 == s.length() - 1) {
                break;
            }

            char c1 = s.charAt(i1 + 1);
            if (c1 == '?' || c1 == '!') {
                k = i1 + 2;
                continue;
            }

            int i2 = s.indexOf('>', i1);
            if (i2 < 0) {
                k = i1 + 1;
                continue;
            }

            String closingTag = "</" + s.substring(i1 + 1, i2 + 1);
            int i3 = s.indexOf(closingTag, i2 + 1);
            if (i3 < 0) {
                k = i2 + 1;
                continue;
            }

            String value = s.substring(i2 + 1, i3);
            if (value.trim().length() > 0) {
                k = i2 + 1;
                continue;
            }

            int i4 = s.lastIndexOf('\n', i1);
            if (i4 < 0) {
                i4 = 0;
            }
            int i5 = s.indexOf('\n', i3);
            if (i5 < 0) {
                i5 = s.length();
            }

            s = s.substring(0, i4) + s.substring(i5);
            k = 0;
        }

        return s;
    }

    private static String pad(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append(Character.toString('\t'));
        }
        return s.toString();
    }
}
