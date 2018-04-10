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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.sli.MetricLogger;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicDblibStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class SvcLogicServiceImpl implements SvcLogicService {

    private static final Map<String, SvcLogicNodeExecutor> BUILTIN_NODES = new HashMap<String, SvcLogicNodeExecutor>() {
        {
            put("block", new BlockNodeExecutor());
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

    private static final Logger LOG = LoggerFactory.getLogger(SvcLogicServiceImpl.class);
    protected HashMap<String, SvcLogicNodeExecutor> nodeExecutors = null;
    protected BundleContext bctx = null;
    protected Properties properties;
    protected SvcLogicStore store;

    public SvcLogicServiceImpl(SvcLogicPropertiesProvider resourceProvider) throws SvcLogicException {
        properties = resourceProvider.getProperties();
        getStore();
    }

    public SvcLogicServiceImpl(SvcLogicPropertiesProvider resourceProvider, DbLibService dbSvc)
            throws SvcLogicException {
        properties = resourceProvider.getProperties();
        store = new SvcLogicDblibStore(dbSvc);
    }


    protected void registerExecutors() {

        LOG.info("Entered register executors");
        for (String nodeType : BUILTIN_NODES.keySet()) {
            LOG.info("SLI - registering node executor for node type " + nodeType);
            registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));
        }
        LOG.info("Done registerExecutors");
    }

    public void registerExecutor(ServiceReference sr) {
        String nodeName = (String) sr.getProperty("nodeType");
        if (nodeName != null) {

            SvcLogicNodeExecutor executor;

            try {
                executor = (SvcLogicNodeExecutor) bctx.getService(sr);
            } catch (Exception e) {
                LOG.error("Cannot get service executor for {}", nodeName, e);
                return;
            }
            registerExecutor(nodeName, executor);
        }
    }

    public void registerExecutor(String nodeName, SvcLogicNodeExecutor executor) {
        if (nodeExecutors == null) {
            nodeExecutors = new HashMap<>();
        }
        LOG.info("SLI - registering executor for node type {}", nodeName);
        nodeExecutors.put(nodeName, executor);
    }

    public void unregisterExecutor(ServiceReference sr) {
        String nodeName = (String) sr.getProperty("nodeType");

        if (nodeName != null) {
            unregisterExecutor(nodeName);
        }
    }

    public void unregisterExecutor(String nodeName) {
        LOG.info("SLI - unregistering executor for node type {}", nodeName);
        nodeExecutors.remove(nodeName);
    }

    public SvcLogicContext execute(SvcLogicGraph graph, SvcLogicContext ctx) throws SvcLogicException {
        if (nodeExecutors == null) {
            registerExecutors();
        }

        // Set service name in MDC to reference current working directed graph
        MDC.put(MetricLogger.SERVICE_NAME, graph.getModule() + ":" + graph.getRpc() + "/v" + graph.getVersion());

        MDC.put("currentGraph", graph.toString());

        SvcLogicNode curNode = graph.getRootNode();
        LOG.info("About to execute graph {}", graph.toString());

        while (curNode != null) {
            MDC.put("nodeId", curNode.getNodeId() + " (" + curNode.getNodeType() + ")");
            LOG.info("About to execute node # {} ({})", curNode.getNodeId(), curNode.getNodeType());

            SvcLogicNode nextNode = executeNode(curNode, ctx);
            curNode = nextNode;
        }
        MDC.remove("nodeId");
        MDC.remove("currentGraph");

        return (ctx);
    }

    public SvcLogicNode executeNode(SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {
        if (node == null) {
            return (null);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing node {}", node.getNodeId());
        }

        SvcLogicNodeExecutor executor = nodeExecutors.get(node.getNodeType());

        if (executor != null) {
            LOG.debug("Executing node executor for node type {} - {}", node.getNodeType(),
                    executor.getClass().getName());
            return (executor.execute(this, node, ctx));
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} node not implemented", node.getNodeType());
            }
            SvcLogicNode nextNode = node.getOutcomeValue("failure");
            if (nextNode != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("about to execute failure branch");
                }
                return (nextNode);
            }

            nextNode = node.getOutcomeValue("Other");
            if (nextNode != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("about to execute Other branch");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("no failure or Other branch found");
                }
            }
            return (nextNode);
        }
    }

    @Override
    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {
        return (store.hasGraph(module, rpc, version, mode));
    }

    @Override
    public Properties execute(String module, String rpc, String version, String mode, Properties props)
            throws SvcLogicException {
        return (execute(module, rpc, version, mode, props, null));
    }

    @Override
    public Properties execute(String module, String rpc, String version, String mode, Properties props,
            DOMDataBroker domDataBroker) throws SvcLogicException {
        LOG.info("Fetching service logic from data store");
        SvcLogicGraph graph = store.fetch(module, rpc, version, mode);

        if (graph == null) {
            Properties retProps = new Properties();
            retProps.setProperty("error-code", "401");
            retProps.setProperty("error-message",
                    "No service logic found for [" + module + "," + rpc + "," + version + "," + mode + "]");
            return (retProps);
        }

        SvcLogicContext ctx = new SvcLogicContext(props);
        ctx.setAttribute("currentGraph", graph.toString());
        ctx.setAttribute("X-ECOMP-RequestID", MDC.get("X-ECOMP-RequestID"));
        ctx.setDomDataBroker(domDataBroker);

        execute(graph, ctx);

        return (ctx.toProperties());
    }

    public SvcLogicStore getStore() throws SvcLogicException {
        // Create and initialize SvcLogicStore object - used to access
        // saved service logic.
        if (store != null) {
            return store;
        }

        try {
            store = SvcLogicStoreFactory.getSvcLogicStore(properties);
        } catch (Exception e) {
            throw new ConfigurationException("Could not get service logic store", e);

        }

        try {
            store.init(properties);
        } catch (SvcLogicException e) {
            throw new ConfigurationException("Could not get service logic store", e);
        }

        return store;
    }
}
