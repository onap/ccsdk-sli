/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2022 Samsung Electronics
 * =============================================================================
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

package org.onap.ccsdk.sli.adaptors.netconf;

import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class MockOperationalStateValidatorImplTest {
    private MockOperationalStateValidatorImpl mockImpl = new MockOperationalStateValidatorImpl();

    @Test
    public void testGetVnfType() throws Exception {
        Assert.assertEquals(VnfType.MOCK.upperCaseName, mockImpl.getVnfType().getUpperCaseName());
    }

    @Test
    public void testFailValidateResponse() throws Exception {
        Exception exception = Assert.assertThrows(, () -> mockImpl.validateResponse("INVALID"));
        Assert.assertEquals("INVALID", exception.getMessage());
    }

    @Test
    public void testValidateResponse() throws Exception {
        mockImpl.validateResponse("VALID");
        Assert.assertEquals("VALID", "VALID");
    }

}
