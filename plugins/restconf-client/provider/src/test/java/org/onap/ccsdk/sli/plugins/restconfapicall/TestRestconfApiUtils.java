package org.onap.ccsdk.sli.plugins.restconfapicall;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/*-
 * ============LICENSE_START=======================================================
 * ONAP: CCSDK
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights
 * 						reserved.
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

public class TestRestconfApiUtils {
    @Test 
    public void testGetSchemaCtxFromDir() throws SvcLogicException {

        // Test with valid subdirectories
        EffectiveModelContext modelCtx = RestconfApiUtils.getSchemaCtxFromDir("src/test/test-yang");
        assertNotNull(modelCtx);

        // Test with directory with no yang
        modelCtx = RestconfApiUtils.getSchemaCtxFromDir("src/test/java");
        assertNotNull(modelCtx);

        // Test with invalid directory
        modelCtx = RestconfApiUtils.getSchemaCtxFromDir("no/such/directory");
        assertNotNull(modelCtx);
    }
}
