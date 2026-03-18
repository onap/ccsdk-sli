/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2025 AT&T Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.sli.plugins.restconfapicall;

import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * Local replacement for ODL's InstanceIdentifierContext which was removed
 * in OpenDaylight Scandium. Holds the schema node and schema context
 * resolved from a RESTCONF URI.
 */
public class InstanceIdentifierContext {

    private final SchemaNode schemaNode;
    private final EffectiveModelContext schemaContext;

    /**
     * Creates an instance identifier context.
     *
     * @param schemaNode    the resolved schema node
     * @param schemaContext the schema context
     */
    public InstanceIdentifierContext(SchemaNode schemaNode,
                                    EffectiveModelContext schemaContext) {
        this.schemaNode = schemaNode;
        this.schemaContext = schemaContext;
    }

    /**
     * Returns the schema node resolved from the URI.
     *
     * @return schema node
     */
    public SchemaNode getSchemaNode() {
        return schemaNode;
    }

    /**
     * Returns the schema context.
     *
     * @return schema context (EffectiveModelContext)
     */
    public EffectiveModelContext getSchemaContext() {
        return schemaContext;
    }
}
