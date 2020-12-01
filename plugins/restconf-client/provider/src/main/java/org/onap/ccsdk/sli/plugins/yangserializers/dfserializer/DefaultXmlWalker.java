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

import java.util.Iterator;

import static org.onap.ccsdk.sli.plugins.yangserializers.dfserializer.DfSerializerUtil.getXmlNodeType;

/**
 * Implementation of XML walker to walk through the nodes and process it.
 */
public class DefaultXmlWalker implements XmlWalker {

    @Override
    public void walk(XmlListener listener, Element xmlElement) throws
            SvcLogicException {
        listener.enterXmlElement(xmlElement, getXmlNodeType(xmlElement));
        if (xmlElement.hasContent() && !xmlElement.isTextOnly()) {
            Iterator i = xmlElement.elementIterator();
            while (i.hasNext()) {
                Element childElement = (Element) i.next();
                walk(listener, childElement);
            }
        }
        listener.exitXmlElement(xmlElement);
    }
}
