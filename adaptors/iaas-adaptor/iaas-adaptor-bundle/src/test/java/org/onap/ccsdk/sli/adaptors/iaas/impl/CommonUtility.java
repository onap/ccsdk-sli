/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2016-2018 Ericsson. All rights reserved.
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
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * This class is used as a utility class to support the test cases.
 */
public class CommonUtility {

    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test
     * to change the internal state accordingly.
     *
     * @param privateFields
     * @param object
     *
     */
    public static void injectMockObjects(Map<String, Object> privateFields, Object object) {
        privateFields.forEach((fieldName, fieldInstance) -> {
            try {
                Field privateField = object.getClass().getDeclaredField(fieldName);
                privateField.setAccessible(true);
                privateField.set(object, fieldInstance);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // Exception occurred while accessing the private fields
            }
        });
    }

    /**
     * Use reflection to locate fields and methods of the base class so that they can be manipulated
     * during the test to change the internal state accordingly.
     *
     * @param privateFields
     * @param catalogObject
     *
     */
    public static void injectMockObjectsInBaseClass(Map<String, Object> privateFields, Object catalogObject) {
        // For base class
        privateFields.forEach((fieldName, fieldInstance) -> {
            try {
                Field privateField = catalogObject.getClass().getSuperclass().getDeclaredField(fieldName);
                privateField.setAccessible(true);
                privateField.set(catalogObject, fieldInstance);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // Exception occurred while accessing the private fields
            }
        });
    }

}
