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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.DefaultPropertiesNodeWalker;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNode;
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.PropertiesNodeWalker;

import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DataFormat.JSON;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.JSON_LIS_ERR;
import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.JSON_TREE_ERR;

/**
 * Representation of JSON serializer which encodes properties to JSON and
 * decodes properties from JSON with the data format serializer.
 */
public class JsonSerializer extends DataFormatSerializer {

    /**
     * Creates an instance of data format serializer.
     *
     * @param serializerContext data format serializer context
     */
    protected JsonSerializer(DataFormatSerializerContext serializerContext) {
        super(JSON, serializerContext);
    }

    @Override
    public String encode(Map<String, String> param,
                         Map<String, List<Annotation>> annotations)
            throws SvcLogicException {
        PropertiesNode propNode = serializerContext().getPropNodeSerializer()
                .encode(param);
        PropertiesNodeWalker nodeWalker = new DefaultPropertiesNodeWalker<>();
        PropertiesNodeJsonListener jsonLis = new PropertiesNodeJsonListener();
        nodeWalker.walk(jsonLis, propNode);
        Writer writer = jsonLis.getWriter();
        return writer.toString();
    }

    @Override
    public Map<String, String> decode(String dataFormatBody)
            throws SvcLogicException {
        if (!(serializerContext().listener() instanceof JsonListener)) {
            throw new SvcLogicException(JSON_LIS_ERR);
        }

        JsonListener listener = (JsonListener) serializerContext().listener();
        JsonWalker walker = new DefaultJsonWalker();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;

        try {
            jsonNode = mapper.readTree(dataFormatBody);
        } catch (IOException e) {
            throw new SvcLogicException(JSON_TREE_ERR, e);
        }

        walker.walk(listener, jsonNode);

        return serializerContext().getPropNodeSerializer().decode(
                listener.serializerHelper().getPropertiesNode());
    }
}
