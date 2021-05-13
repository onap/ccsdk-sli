/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.utils.logging;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class LoggingConstantsTest {
    @Test (expected = IllegalAccessError.class)
    public void testConstructor() throws Exception {
        Whitebox.invokeConstructor(LoggingConstants.class);
    }

    @Test (expected = IllegalAccessError.class)
    public void testMdcKeysConstructor() throws Exception {
        Whitebox.invokeConstructor(LoggingConstants.MDCKeys.class);
    }

    @Test (expected = IllegalAccessError.class)
    public void testStatusCodesConstructor() throws Exception {
        Whitebox.invokeConstructor(LoggingConstants.StatusCodes.class);
    }

    @Test (expected = IllegalAccessError.class)
    public void testTargetNamesConstructor() throws Exception {
        Whitebox.invokeConstructor(LoggingConstants.TargetNames.class);
    }

    @Test (expected = IllegalAccessError.class)
    public void testTargetServiceNamesConstructor() throws Exception {
        Whitebox.invokeConstructor(LoggingConstants.TargetServiceNames.class);
    }

    @Test (expected = IllegalAccessError.class)
    public void testAAIServiceNamesConstructor() throws Exception {
        Whitebox.invokeConstructor(LoggingConstants.TargetServiceNames.AAIServiceNames.class);
    }
}
