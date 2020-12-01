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
import org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType;

/**
 * Abstraction of an entity which provides call back methods, which in turn
 * are called by JSON walker while walking the JSON tree. This interface
 * needs to be implemented by protocol, implementing listener based call
 * while doing JSON walk.
 */
public interface JsonListener extends Listener {

    /**
     * Call back invoked during JSON node entry. All other related
     * information can be obtained from the JSON node.
     *
     * @param nodeName JSON node name
     * @param node     JSON node
     * @param nodeType JSON node type
     * @throws SvcLogicException when node type is of wrong format
     */
    void enterJsonNode(String nodeName, JsonNode node, NodeType nodeType)
            throws SvcLogicException;

    /**
     * Call back invoked during JSON node exit. All the related information
     * can be obtained from the JSON node.
     *
     * @param node JSON node
     * @throws SvcLogicException when JSON node exit doesn't happen
     */
    void exitJsonNode(JsonNode node) throws SvcLogicException;
}
