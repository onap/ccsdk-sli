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

import org.dom4j.Element;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

/**
 * Abstraction of an entity which provides interface for XML walk. This
 * interface serves as a common tool for anyone who needs to parse the XML
 * node with depth-first algorithm.
 */
public interface XmlWalker {

    /**
     * Walks the XML data tree. Protocols implement XML listener service and
     * walks the XML tree with input as implemented object. XML walker
     * provides call back to the implemented methods.
     *
     * @param listener   XML listener implemented by the protocol
     * @param xmlElement root element of the XML data tree
     * @throws SvcLogicException when walking the XML node fails
     */
    void walk(XmlListener listener, Element xmlElement) throws
            SvcLogicException;
}
