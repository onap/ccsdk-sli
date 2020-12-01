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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);

    private JsonParser() {
        // Preventing instantiation of the same.
    }


    private static void handleJsonArray(String key, Map<String, Object> jArrayMap, JSONArray jsonArr) throws JSONException {
        JSONObject jsonObj;
        JSONArray subJsonArr;
        boolean stripKey = false;

        for (int i = 0, length = jsonArr.length(); i < length; i++) {
            if (stripKey)
                key = key.substring(0, key.length()-3);

            subJsonArr = jsonArr.optJSONArray(i);
            if (subJsonArr != null) {
                key = StringUtils.trimToEmpty(key) + "[" + i + "]";
                jArrayMap.putIfAbsent(key + "_length", String.valueOf(subJsonArr.length()));
                handleJsonArray(key, jArrayMap, subJsonArr);
                stripKey = true;
                continue;
            }

            jsonObj = jsonArr.optJSONObject(i);
            if (jsonObj != null) {
                Iterator<String> ii = jsonObj.keys();
                while (ii.hasNext()) {
                    String nodeKey = ii.next();
                    String key1 = "[" + i + "]." + nodeKey;
                    String[] subKey = key1.split(":");
                    if (subKey.length == 2) {
                        jArrayMap.putIfAbsent(subKey[1], jsonObj.get(nodeKey));
                    } else {
                        jArrayMap.putIfAbsent(key1, jsonObj.get(nodeKey));
                    }
                }
            }
            else {
                jArrayMap.putIfAbsent(StringUtils.trimToEmpty(key), jsonArr);
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> convertToProperties(String s)
            throws SvcLogicException {

        checkNotNull(s, "Input should not be null.");

        try {
            Map<String, Object> wm = new HashMap<>();
            JSONObject json;
            JSONArray jsonArr;
            //support top level list in json response
            if (s.startsWith("[")) {
                jsonArr = new JSONArray(s);
                wm.put("_length", String.valueOf(jsonArr.length()));
                handleJsonArray(null, wm, jsonArr);
            } else {
                json = new JSONObject(s);
                Iterator<String> ii = json.keys();
                while (ii.hasNext()) {
                    String key1 = ii.next();
                    String[] subKey = key1.split(":");
                    if (subKey.length == 2) {
                        wm.put(subKey[1], json.get(key1));
                    } else {
                        wm.put(key1, json.get(key1));
                    }
                }
            }

            Map<String, String> mm = new HashMap<>();
            while (!wm.isEmpty()) {
                for (String key : new ArrayList<>(wm.keySet())) {
                    Object o = wm.get(key);
                    wm.remove(key);

                    if (o instanceof Boolean || o instanceof Number || o instanceof String) {
                        mm.put(key, o.toString());
                        log.info("Added property: {} : {}", key, o.toString());
                    } else if (o instanceof JSONObject) {
                        JSONObject jo = (JSONObject) o;
                        Iterator<String> i = jo.keys();
                        while (i.hasNext()) {
                            String key1 = i.next();
                            String[] subKey = key1.split(":");
                            if (subKey.length == 2) {
                                wm.put(key + "." + subKey[1], jo.get(key1));
                            } else {
                                wm.put(key + "." + key1, jo.get(key1));
                            }
                        }
                    } else if (o instanceof JSONArray) {
                        JSONArray ja = (JSONArray) o;
                        mm.put(key + "_length", String.valueOf(ja.length()));
                        log.info("Added property: {}_length: {}", key, ja.length());

                        for (int i = 0; i < ja.length(); i++) {
                            wm.put(key + '[' + i + ']', ja.get(i));
                        }
                    }
                }
            }
            return mm;
        } catch (JSONException e) {
            throw new SvcLogicException("Unable to convert JSON to properties" + e.getLocalizedMessage(), e);
        }
    }

}
