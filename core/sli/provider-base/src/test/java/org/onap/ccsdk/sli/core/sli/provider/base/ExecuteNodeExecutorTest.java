/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
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

package org.onap.ccsdk.sli.core.sli.provider.base;

import java.util.Properties;
import org.onap.ccsdk.sli.core.sli.DuplicateValueException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicExpression;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import junit.framework.TestCase;

public class ExecuteNodeExecutorTest extends TestCase {
    public class MockExecuteNodeExecutor extends ExecuteNodeExecutor {

        protected SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName) {
            return (SvcLogicJavaPlugin) new LunchSelectorPlugin();
        }

        protected String evaluate(SvcLogicExpression expr, SvcLogicNode node,
                SvcLogicContext ctx) throws SvcLogicException {
            return "selectLunch";
        }
    }

    public void testBadPlugin() throws DuplicateValueException, SvcLogicException {
        LunchSelectorPlugin p = new LunchSelectorPlugin();
        MockExecuteNodeExecutor execute = new MockExecuteNodeExecutor();
        SvcLogicNode node = new SvcLogicNode(0, "", "", new SvcLogicGraph());
        node.setAttribute("method", "selectLunch");
        SvcLogicPropertiesProvider resourceProvider = new SvcLogicPropertiesProvider() {

			public Properties getProperties() {
				return new Properties();
			};
        };
        
        
        execute.execute(new SvcLogicServiceImplBase(null, null), new SvcLogicNode(0, "", "", new SvcLogicGraph()),
                new SvcLogicContext());
    }

}
