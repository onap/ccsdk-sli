/*
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

package org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl;

import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.openstack.connectors.HeatConnector;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.StackService;
import com.att.cdp.zones.model.Server.Status;
import com.att.cdp.zones.model.Stack;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class TestRestoreStack {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void restoreStackTest() throws ZoneException, SvcLogicException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        HeatConnector heatConnector = Mockito.mock(HeatConnector.class);
        StackService stackService = mock(StackService.class);
        Stack stack1 = mock(Stack.class);
        doReturn("stack1").when(stack1).getId();
        doReturn("stack1").when(stack1).getName();
        doReturn(heatConnector).when(mg.getContext()).getHeatConnector();
        com.att.cdp.zones.model.Stack.Status stackStatus =
                com.att.cdp.zones.model.Stack.Status.DELETED;
        doReturn(stackStatus).when(stack1).getStatus();
        doReturn(mg.getContext()).when(stack1).getContext();
        List<Stack> stackList = new LinkedList<Stack>();
        stackList.add(stack1);
        doReturn(stackList).when(stackService).getStacks();
        doReturn(stack1).when(stackService).getStack("stack1", "stack1");
        doReturn(stackService).when(mg.getContext()).getStackService();
        mg.getParams().put(ProviderAdapter.PROPERTY_STACK_ID, "stack1");
        mg.getParams().put(ProviderAdapter.PROPERTY_INSTANCE_URL, "URL");
        mg.getParams().put(ProviderAdapter.PROPERTY_PROVIDER_NAME, "NAME");
        mg.getParams().put(ProviderAdapter.PROPERTY_INPUT_SNAPSHOT_ID, "SNAPSHOT_ID");
        SubclassRestoreStack rbs = Mockito.spy(new SubclassRestoreStack());
        rbs.setProviderCache(mg.getProviderCacheMap());
        Mockito.doReturn(stack1).when(rbs).lookupStack(Mockito.any(RequestContext.class), Mockito.any(Context.class),
                Mockito.anyString());
        Mockito.doReturn(mg.getContext()).when(rbs).resolveContext(Mockito.any(RequestContext.class), Mockito.anyMap(),
                Mockito.anyString(), Mockito.anyString());
        expectedEx.expect(SvcLogicException.class);
        rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
    }

    class SubclassRestoreStack extends RestoreStack {
        @Override
        protected Context resolveContext(RequestContext rc, Map<String, String> params, String appName, String vmUrl) {
            return Mockito.mock(Context.class);
        }
        @Override
        protected Stack lookupStack(RequestContext rc, Context context, String id) {
            Stack stack = Mockito.mock(Stack.class);
            return stack;
        }
    }
}
