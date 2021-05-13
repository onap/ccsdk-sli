/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas;

import com.woorea.openstack.base.client.OpenStackClient;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.openstack.heat.StackResource;
import org.powermock.reflect.internal.WhiteboxImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestStackResource {

    private StackResource stackResource;
    private OpenStackClient client;

    @Test
    public void testShow() {
        stackResource = new StackResource(client);
        assertNotNull(stackResource.show("stackName", "123"));
    }

    @Test
    public void testShowStack() throws Exception {
        stackResource = new StackResource(client);
        StackResource.ShowStack showStack = stackResource.new ShowStack("stackName", "111");
        StringBuilder path = WhiteboxImpl.getInternalState(showStack, "path");
        assertEquals("/stacks/stackName/111", path.toString());
        assertNotNull(stackResource.new ShowStack("stackName", "111"));
    }

}
