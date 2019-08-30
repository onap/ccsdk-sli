/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 *  Modifications Copyright (C) 2018 IBM.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.onap.ccsdk.sli.core.sli.ExitNodeException;
import org.onap.ccsdk.sli.core.sli.MetricLogger;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class SvcLogicServiceImplBase implements SvcLogicServiceBase {
	protected SvcLogicResolver resolver;
    protected static final Map<String, AbstractSvcLogicNodeExecutor> BUILTIN_NODES = new HashMap<String, AbstractSvcLogicNodeExecutor>() {
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
            put("break", new BreakNodeExecutor());
            put("while", new WhileNodeExecutor());
            put("exit", new ExitNodeExecutor());
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(SvcLogicServiceImplBase.class);
    protected HashMap<String, AbstractSvcLogicNodeExecutor> nodeExecutors = null;
    protected Properties properties;
    protected SvcLogicStore store;
    protected static final String CURRENT_GRAPH="currentGraph";

    public SvcLogicServiceImplBase(SvcLogicStore store) {
    	this.store = store;
    }

    protected void registerExecutors() {
        for (String nodeType : BUILTIN_NODES.keySet()) {
            registerExecutor(nodeType, BUILTIN_NODES.get(nodeType));
        }
    }

    public void registerExecutor(String nodeName, AbstractSvcLogicNodeExecutor executor) {
        if (nodeExecutors == null) {
            nodeExecutors = new HashMap<>();
        }
        executor.setResolver(resolver);
        nodeExecutors.put(nodeName, executor);
    }

    public void unregisterExecutor(String nodeName) {
        nodeExecutors.remove(nodeName);
    }

    public SvcLogicContext execute(SvcLogicGraph graph, SvcLogicContext ctx) throws SvcLogicException {
        if (nodeExecutors == null) {
            registerExecutors();
        }

        MDC.put(CURRENT_GRAPH, graph.toString());

        SvcLogicNode curNode = graph.getRootNode();
        LOG.info("About to execute graph {}", graph.toString());
		try {
			while (curNode != null) {
				SvcLogicNode nextNode = executeNode(curNode, ctx);
				curNode = nextNode;
			}
		} catch (ExitNodeException e) {
            LOG.debug("SvcLogicServiceImpl caught ExitNodeException");
		}
        MDC.remove("nodeId");
        MDC.remove(CURRENT_GRAPH);

        return (ctx);
    }
    
	public SvcLogicNode executeNode(SvcLogicNode node, SvcLogicContext ctx) throws SvcLogicException {
        if (node == null) {
            return (null);
        }

		LOG.info("About to execute node # {} ({})", node.getNodeId(), node.getNodeType());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing node " + node.getNodeId() + " of " + node.getGraph().getRpc());
        }

        AbstractSvcLogicNodeExecutor executor = nodeExecutors.get(node.getNodeType());

        if (executor != null) {
    		MDC.put("nodeId", node.getNodeId() + " (" + node.getNodeType() + ")");
            return (executor.execute(this, node, ctx));
        } else {
            throw new SvcLogicException("Attempted to execute a node of type " + node.getNodeType() + ", but no executor was registered for this type");
        }
    }

    @Override
    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {
        return (store.hasGraph(module, rpc, version, mode));
    }

    @Override
	public Properties execute(String module, String rpc, String version, String mode, Properties props)
			throws SvcLogicException {
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
		ctx.setAttribute(CURRENT_GRAPH, graph.toString());
		ctx.setAttribute("X-ECOMP-RequestID", MDC.get("X-ECOMP-RequestID"));
		execute(graph, ctx);
		return (ctx.toProperties());
	}

	@Override
	public SvcLogicStore getStore() throws SvcLogicException {
		return this.store;
	}

}
