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

package org.onap.ccsdk.sli.core.sli.provider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicParser;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.onap.ccsdk.sli.core.sli.provider.base.AbstractSvcLogicNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.BlockNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.BreakNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.CallNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.ConfigureNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.DeleteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.ExecuteNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.ExistsNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.ForNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.GetResourceNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.IsAvailableNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.NotifyNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.RecordNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.ReleaseNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.ReserveNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.ReturnNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.SaveNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.SetNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicPropertiesProvider;
import org.onap.ccsdk.sli.core.sli.provider.base.SwitchNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.UpdateNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.WhileNodeExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ITCaseSvcLogicGraphExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(SvcLogicGraph.class);
    private static final Map<String, AbstractSvcLogicNodeExecutor> BUILTIN_NODES = new HashMap<String, AbstractSvcLogicNodeExecutor>() {
        {
            put("block", new BlockNodeExecutor());
            put("break", new BreakNodeExecutor());
            put("call", new CallNodeExecutor());
            put("configure", new ConfigureNodeExecutor());
            put("delete", new DeleteNodeExecutor());
            put("execute", new ExecuteNodeExecutor());
            put("exists", new ExistsNodeExecutor());
            put("for", new ForNodeExecutor());
            put("get-resource", new GetResourceNodeExecutor());
            put("is-available", new IsAvailableNodeExecutor());
            put("notify", new NotifyNodeExecutor());
            put("record", new RecordNodeExecutor());
            put("release", new ReleaseNodeExecutor());
            put("reserve", new ReserveNodeExecutor());
            put("return", new ReturnNodeExecutor());
            put("save", new SaveNodeExecutor());
            put("set", new SetNodeExecutor());
            put("switch", new SwitchNodeExecutor());
            put("update", new UpdateNodeExecutor());
            put("while", new WhileNodeExecutor());

        }
    };

    private static SvcLogicClassResolver svcLogicClassResolver;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        LOG.info("before class");

        InputStream propStr = ITCaseSvcLogicGraphExecutor.class.getResourceAsStream("/svclogic.properties");

        Properties svcprops = new Properties();
        svcprops.load(propStr);

        SvcLogicStore store = SvcLogicStoreFactory.getSvcLogicStore(svcprops);

        assertNotNull(store);

        SvcLogicParser parser = new SvcLogicParser();

        SvcLogicPropertiesProvider resourceProvider = new SvcLogicPropertiesProviderImpl();
        svcLogicClassResolver = new SvcLogicClassResolver();
        SvcLogicServiceImpl svc = new SvcLogicServiceImpl(resourceProvider, svcLogicClassResolver);

        for (String nodeType : BUILTIN_NODES.keySet()) {
            svc.registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));
        }




    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        LOG.info("after class");
    }

    @Before
    public void setUp() throws Exception {
        LOG.info("before");
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("after");
    }

    @Test
    public void testExecute() {

        try {
            InputStream testStr = getClass().getResourceAsStream("/executor.tests");
            BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));

            InputStream propStr = getClass().getResourceAsStream("/svclogic.properties");

            Properties svcprops = new Properties();
            svcprops.load(propStr);




            SvcLogicParser parser = new SvcLogicParser();

            // Loop through executor tests
            SvcLogicPropertiesProvider resourceProvider = new SvcLogicPropertiesProvider() {
                @Override
                public Properties getProperties() {
                    return svcprops;
                }
            };
            SvcLogicServiceImpl svc = new SvcLogicServiceImpl(resourceProvider, svcLogicClassResolver);
            SvcLogicStore store = svc.getStore();
            assertNotNull(store);
            for (String nodeType : BUILTIN_NODES.keySet()) {


                svc.registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));

            }
            String testCaseLine = null;
            while ((testCaseLine = testsReader.readLine()) != null) {

                String[] testCaseFields = testCaseLine.split(":");
                String testCaseFile = testCaseFields[0];
                String testCaseMethod = testCaseFields[1];
                String testCaseParameters = null;

                if (testCaseFields.length > 2) {
                    testCaseParameters = testCaseFields[2];
                }

                SvcLogicContext ctx = new SvcLogicContext();
                if (testCaseParameters != null) {
                    String[] testCaseParameterSettings = testCaseParameters.split(",");

                    for (int i = 0; i < testCaseParameterSettings.length; i++) {
                        String[] nameValue = testCaseParameterSettings[i].split("=");
                        if (nameValue != null) {
                            String name = nameValue[0];
                            String value = "";
                            if (nameValue.length > 1) {
                                value = nameValue[1];
                            }

                            ctx.setAttribute(name, value);
                        }
                    }
                }

                testCaseFile = testCaseFile.trim();

                if (testCaseFile.length() > 0) {
                    if (!testCaseFile.startsWith("/")) {
                        testCaseFile = "/" + testCaseFile;
                    }
                    URL testCaseUrl = getClass().getResource(testCaseFile);
                    if (testCaseUrl == null) {
                        fail("Could not resolve test case file " + testCaseFile);
                    }


                    LinkedList<SvcLogicGraph> graphs = parser.parse(testCaseUrl.getPath());

                    assertNotNull(graphs);

                    // Load grqphs into db to support call node
                    SvcLogicParser.load(testCaseUrl.getPath(), store);
                    SvcLogicParser.activate("neutron", "canCreateNetwork", "1.0.0", "sync", store);
                    SvcLogicParser.activate("neutron", "switchTester", "1.0.0", "sync", store);
                    SvcLogicParser.activate("neutron", "forRecordTester", "1.0.0", "sync", store);
                    SvcLogicParser.activate("neutron", "whileNodeTester", "1.0.0", "sync", store);
                    SvcLogicParser.activate("neutron", "resourceTester", "1.0.0", "sync", store);
                    SvcLogicParser.activate("neutron", "configureTester", "1.0.0", "sync", store);
                    SvcLogicParser.activate("neutron", "javaPluginTester", "1.0.0", "sync", store);
                    SvcLogicParser.activate("neutron", "allNodesTester", "1.0.0", "sync", store);
                    SvcLogicParser.activate("neutron", "networkCreated", "1.0.0", "sync", store);

                    for (SvcLogicGraph graph : graphs) {
                        if (graph.getRpc().equals(testCaseMethod)) {
                            Properties props = ctx.toProperties();
                            LOG.info("SvcLogicContext before executing {}:", testCaseMethod);
                            for (Enumeration e1 = props.propertyNames(); e1.hasMoreElements(); ) {
                                String propName = (String) e1.nextElement();
                                LOG.info(propName + " = " + props.getProperty(propName));
                            }

                            svc.execute(graph, ctx);

                            props = ctx.toProperties();
                            LOG.info("SvcLogicContext after executing {}:", testCaseMethod);
                            for (Enumeration e2 = props.propertyNames(); e2.hasMoreElements(); ) {
                                String propName = (String) e2.nextElement();
                                LOG.info(propName + " = " + props.getProperty(propName));
                            }
                        }
                    }

                }
            }

        } catch (Exception e) {
            LOG.error("Caught exception executing directed graphs", e);
            fail("Exception executing graphs");
        }
    }
}
