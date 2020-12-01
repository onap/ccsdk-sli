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
 * Abstraction of an entity which provides call back methods, which in turn
 * are called by XML walker while walking the XML tree. This interface needs
 * to be implemented by protocol implementing listener based call while doing
 * XML walk.
 */
public interface XmlListener extends Listener {

    /**
     * Callback invoked during a node entry. All the related information
     * about the node can be obtained from the element.
     *
     * @param element  current XML element
     * @param nodeType node type of the element
     * @throws SvcLogicException when node type is of wrong format
     */
    void enterXmlElement(Element element, XmlNodeType nodeType)
            throws SvcLogicException;

    /**
     * Callback invoked during a node exit. All the related information about
     * the node can be obtained from the element.
     *
     * @param element current xml element.
     * @throws SvcLogicException when XML node exit doesn't happen
     */
    void exitXmlElement(Element element) throws SvcLogicException;
}
