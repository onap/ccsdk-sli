/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2022 Samsung Electronics
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

package org.onap.ccsdk.sli.adaptors.resource.sql;

import static org.junit.Assert.assertNotNull;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

public class SqlResourceProviderTest {

    private static SqlResourcePropertiesProvider provider;
    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";

    @Test
    public void testSqlResourceProvider() {
        try{
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(SDNC_CONFIG_DIR, "./src/test/resources");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }

        provider = new SqlResourcePropertiesProviderImpl();
        assertNotNull(provider);
    }

    @Test
    public void testGetProperties() {
        Properties properties = provider.getProperties();
        assertNotNull(properties);
    }

    @Test
    public void testReportSuccess() {
        try{
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(SDNC_CONFIG_DIR, "./src/test/resources");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }

        provider = new SqlResourcePropertiesProviderImpl();
        Properties properties = provider.getProperties();
        assertNotNull(properties);
    }
}
