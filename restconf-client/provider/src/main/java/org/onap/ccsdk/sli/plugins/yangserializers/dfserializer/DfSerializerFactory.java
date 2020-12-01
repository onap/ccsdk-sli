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

import static java.lang.String.format;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.FORMAT_ERR;

/**
 * Represents the data format serializer factory which will return JSON or XML
 * serializer according to the serializer context.
 */
public final class DfSerializerFactory {

    /**
     * Creates a new DfSerializerFactory.
     */
    private DfSerializerFactory() {
    }

    /**
     * Returns the instance of the data format serializer factory.
     *
     * @return instance of the data format serializer factory
     */
    public static DfSerializerFactory instance() {
        return DfSerializerFactory.LazyHolder.INSTANCE;
    }

    /**
     * Bill pugh singleton pattern. Instance will not be instantiated until
     * the lazy holder class is loaded via a call to the instance of method
     * below.
     */
    private static class LazyHolder {
        private static final DfSerializerFactory INSTANCE =
                new DfSerializerFactory();
    }

    /**
     * Returns the data format serializer by deciding it based on the format of
     * the parameter.
     *
     * @param serCtx serializer context
     * @param params parameters
     * @return data format serializer
     * @throws SvcLogicException when the data format type is wrong
     */
    public DataFormatSerializer getSerializer(DataFormatSerializerContext serCtx,
                                              YangParameters params)
            throws SvcLogicException {
        DataFormatSerializer serializer;
        switch (params.format) {
            case JSON:
                serializer = new JsonSerializer(serCtx);
                break;

            case XML:
                serializer = new XmlSerializer(serCtx);
                break;

            default:
                throw new SvcLogicException(format(FORMAT_ERR,
                                                   params.format));
        }
        return serializer;
    }
}
