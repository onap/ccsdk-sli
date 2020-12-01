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

package org.onap.ccsdk.sli.plugins.yangserializers.pnserializer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.COLON;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.MdsalPropertiesNodeUtils.UNDERSCORE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.MULTI_INSTANCE_LEAF_NODE;
import static org.onap.ccsdk.sli.plugins.yangserializers.pnserializer.NodeType.SINGLE_INSTANCE_LEAF_NODE;

/**
 * Represents implementation of PropertiesNodeListener.
 */
public class DefaultPropertiesNodeListener implements PropertiesNodeListener {

    private Map<String, String> params = new HashMap<>();

    @Override
    public void start(PropertiesNode node) {
        // do nothing
    }

    @Override
    public void end(PropertiesNode node) throws SvcLogicException {
        exitPropertiesNode(node);
    }

    @Override
    public void enterPropertiesNode(PropertiesNode node) {
        /*
         * Only if it is leaf node or leaf-list node,
         * then create a property entry and add to map
         */
        if (node.nodeType() == SINGLE_INSTANCE_LEAF_NODE
                || node.nodeType() == MULTI_INSTANCE_LEAF_NODE) {
            String val = ((LeafNode) node).value();
            if (((LeafNode) node).valueNs() != null) {
                val = ((LeafNode) node).valueNs().moduleName() + COLON + val;
            }
            String uri = node.uri().replaceAll(COLON, UNDERSCORE);
            params.put(uri, val);
        }
    }

    @Override
    public void exitPropertiesNode(PropertiesNode node) throws
            SvcLogicException {
        if (!node.augmentations().isEmpty()) {
            for (Map.Entry<Object, Collection<PropertiesNode>> augmentationTochild
                    : node.augmentations().asMap().entrySet()) {
                Collection<PropertiesNode> childsFromAugmentations = augmentationTochild
                        .getValue();
                if (!childsFromAugmentations.isEmpty()) {
                    PropertiesNodeWalker walker = new DefaultPropertiesNodeWalker<>();
                    for (PropertiesNode pNode : childsFromAugmentations) {
                        enterPropertiesNode(pNode);
                        walker.walk(this, pNode);
                        exitPropertiesNode(pNode);
                    }
                }
            }
        }
    }

    /**
     * Returns properties.
     *
     * @return properties
     */
    public Map<String, String> params() {
        return params;
    }

    /**
     * Sets properties.
     *
     * @param params properties
     */
    public void params(Map<String, String> params) {
        this.params = params;
    }
}
