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

import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import java.util.List;
import java.util.Map;

/**
 * Abstraction of serializer to encode/decode context memory parameters
 * to/from specified data format.
 */
public abstract class DataFormatSerializer {

    /**
     * Data format type of the serializer.
     */
    private DataFormat dataFormat;

    /**
     * Data format serializer context.
     */
    private DataFormatSerializerContext serializerContext;

    /**
     * Creates an instance of data format serializer.
     *
     * @param d type of data format
     * @param s data format serializer context
     */
    protected DataFormatSerializer(DataFormat d,
                                   DataFormatSerializerContext s) {
        this.dataFormat = d;
        this.serializerContext = s;
    }

    /**
     * Encodes context memory parameters to data format.
     *
     * @param param context memory parameter
     * @param annotations annotations
     * @return data format body
     * @throws SvcLogicException when serialization fails
     */
    public abstract String encode(Map<String, String> param,
        Map<String, List<Annotation>> annotations) throws SvcLogicException;

    /**
     * Decodes data format body to context memory parameters.
     *
     * @param dataFormatBody abstract node
     * @return context memory parameters
     * @throws SvcLogicException when serialization fails
     */
    public abstract Map<String, String> decode(String dataFormatBody)
            throws SvcLogicException;

    /**
     * Returns data format serializer context.
     *
     * @return data format serializer context
     */
    public DataFormatSerializerContext serializerContext() {
        return serializerContext;
    }

    /**
     * Returns supported data format.
     *
     * @return supported data format
     */
    public DataFormat dataFormat() {
        return dataFormat;
    }
}