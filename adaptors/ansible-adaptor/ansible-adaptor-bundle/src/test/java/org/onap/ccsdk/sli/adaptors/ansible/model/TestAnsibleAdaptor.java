/*-
 * ============LICENSE_START=======================================================
 * ONAP : SLI
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ansible.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleMessageParser;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleServerEmulator;

import static org.junit.Assert.assertNotNull;

public class TestAnsibleAdaptor {

    @Test
    public void callPrivateConstructorsMethodsForCodeCoverage()
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException {

        /* test constructors */
        Class<?>[] classesOne = {AnsibleMessageParser.class};
        for (Class<?> clazz : classesOne) {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertNotNull(constructor.newInstance());
        }
        Class<?>[] classesTwo = {AnsibleServerEmulator.class};
        for (Class<?> clazz : classesTwo) {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertNotNull(constructor.newInstance());
        }
        Class<?>[] classesThree = {AnsibleResult.class};
        for (Class<?> clazz : classesThree) {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertNotNull(constructor.newInstance());
        }

        /* test methods */
        AnsibleMessageParser ansibleMessageParser = new AnsibleMessageParser();
        Class<?>[] parameterTypes = new Class[1];
        parameterTypes[0] = java.lang.String.class;

        Method m = ansibleMessageParser.getClass().getDeclaredMethod("getFilePayload", parameterTypes);
        m.setAccessible(true);
        assertNotNull(m.invoke(ansibleMessageParser, "{\"test\": test}"));

        // test logging-suppression for an invalid host value (Fortify Log Forging fix)
        String input = "{"
                       + "  \"Results\": {"
                       + "    \"192.168.1.10\": {"
                       + "      \"Id\": \"101\","
                       + "      \"StatusCode\": 200,"
                       + "      \"StatusMessage\": \"SUCCESS\""
                       + "    },"
                       + "    \"192%168%1%10\": {"
                       + "      \"Id\": \"102\","
                       + "      \"StatusCode\": 200,"
                       + "      \"StatusMessage\": \"SUCCESS\""
                       + "    },"
                       + "    \"server-dev.att.com\": {"
                       + "      \"Id\": \"103\","
                       + "      \"StatusCode\": 200,"
                       + "      \"StatusMessage\": \"SUCCESS\""
                       + "    }"
                       + "  },"
                       + "  \"StatusCode\": 200,"
                       + "  \"StatusMessage\": \"FINISHED\""
                       + "}";
        Method m2 = ansibleMessageParser.getClass().getDeclaredMethod("parseGetResponseNested", AnsibleResult.class, JSONObject.class);
        m2.setAccessible(true);
        m2.invoke(ansibleMessageParser, new AnsibleResult(), new JSONObject(input));
    }

}

