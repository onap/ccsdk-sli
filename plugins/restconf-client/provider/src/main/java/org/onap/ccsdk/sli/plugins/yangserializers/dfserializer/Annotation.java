/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.ccsdk.sli.plugins.yangserializers.dfserializer;

/**
 * Representation of an entity that represents annotated attribute.
 */
public class Annotation {

    /**
     * Name of the annotation.
     */
    private String name;

    /**
     * Value of the annotation.
     */
    private String value;

    /**
     * Creates an instance of annotation.
     *
     * @param n annotation name
     * @param v annotation value
     */
    public Annotation(String n, String v) {
        name = n;
        value = v;
    }

    /**
     * Returns name of annotation.
     *
     * @return name of annotation
     */
    public String name() {
        return name;
    }

    /**
     * Returns value of annotation.
     *
     * @return value of annotation
     */
    public String value() {
        return value;
    }
}
