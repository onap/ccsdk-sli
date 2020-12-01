/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
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

import org.opendaylight.restconf.common.context.InstanceIdentifierContext;

/**
 * Representation of a holder for a proper path and its corresponding schema.
 */
public class SchemaPathHolder {

    /**
     * Schema context for the path.
     */
    private InstanceIdentifierContext insId;

    /**
     * Formatted path.
     */
    private String uri;

    /**
     * Constructs schema path holder with path and its schema.
     *
     * @param insId instance identifier context
     * @param uri   path
     */
    public SchemaPathHolder(InstanceIdentifierContext insId, String uri) {
        this.insId = insId;
        this.uri = uri;
    }

    /**
     * Returns the instance identifier context of the path.
     *
     * @return schema of the path
     */
    public InstanceIdentifierContext getInsId() {
        return insId;
    }

    /**
     * Returns the formatted path.
     *
     * @return formatted path
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the formatted path.
     *
     * @param uri formatted path
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
