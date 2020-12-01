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
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * Abstraction of an entity which provides interface for JSON walk. This
 * interface serves as a common tool for anyone who needs to parse the JSON
 * node with depth-first algorithm.
 */
public interface JsonWalker {

    /**
     * Walks the JSON data tree. Protocols implement JSON listener service
     * and walks the JSON tree with input as implemented object. JSON walker
     * provides call back to the implemented methods.
     *
     * @param listener JSON listener implemented by the protocol
     * @param jsonNode root node of the JSON data tree
     * @throws SvcLogicException when walking the JSON node fails
     */
    void walk(JsonListener listener, JsonNode jsonNode)
            throws SvcLogicException;
}
