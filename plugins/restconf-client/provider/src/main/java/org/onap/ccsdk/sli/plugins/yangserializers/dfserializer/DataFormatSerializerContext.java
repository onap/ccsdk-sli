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

import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNodeSerializer;

import java.util.Map;

/**
 * Abstraction of data format serializer context.
 */
public class DataFormatSerializerContext {

    /**
     * Data format listener.
     */
    private Listener listener;

    /**
     * URI corresponding to the instance identifier.
     */
    private String uri;

    /**
     * Protocol annotation.
     */
    private Map<String, String> protocolAnnotation;

    /**
     * Properties node serializer.
     */
    private PropertiesNodeSerializer propNodeSerializer;

    /**
     * Creates an instance of data format serializer context.
     *
     * @param l data format listener
     * @param u URI corresponding to instance identifier
     * @param p protocol annotations
     * @param s properties node serializer
     */
    public DataFormatSerializerContext(Listener l, String u,
                                       Map<String, String> p,
                                       PropertiesNodeSerializer s) {
        listener = l;
        uri = u;
        protocolAnnotation = p;
        propNodeSerializer = s;
    }

    /**
     * Returns the data format listener.
     *
     * @return data format listener
     */
    public Listener listener() {
        return listener;
    }

    /**
     * Returns the URI.
     *
     * @return URI
     */
    public String uri() {
        return uri;
    }

    /**
     * Returns the protocol annotations.
     *
     * @return protocol annotations
     */
    public Map<String, String> getProtocolAnnotation() {
        return protocolAnnotation;
    }

    /**
     * Returns the properties node serializer.
     *
     * @return properties node serializer
     */
    public PropertiesNodeSerializer getPropNodeSerializer() {
        return propNodeSerializer;
    }
}