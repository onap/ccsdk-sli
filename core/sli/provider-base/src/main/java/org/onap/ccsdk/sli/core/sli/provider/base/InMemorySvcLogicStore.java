package org.onap.ccsdk.sli.core.sli.provider.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemorySvcLogicStore implements SvcLogicStore {
    private static final Logger logger = LoggerFactory.getLogger(InMemorySvcLogicStore.class);
    public Map<String, SvcLogicGraph> graphStore;

    public InMemorySvcLogicStore() {
        graphStore = new HashMap<String, SvcLogicGraph>();
    }

    @Override
    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {
        String storeId = new String(module + ":" + rpc);
        return graphStore.containsKey(storeId);
    }

    @Override
    public SvcLogicGraph fetch(String module, String rpc, String version, String mode) throws SvcLogicException {
        String storeId = new String(module + ":" + rpc);
        return graphStore.get(storeId);
    }

    @Override
    public void store(SvcLogicGraph graph) throws SvcLogicException {
        if (graph != null) {
            String storeId = new String(graph.getModule() + ":" + graph.getRpc());
            graphStore.put(storeId, graph);
            logger.info(graph.toString() + " stored in InMemorySvcLogicStore.");
        }
    }

    @Override
    public void init(Properties props) throws SvcLogicException {
        // noop
    }

    @Override
    public void delete(String module, String rpc, String version, String mode) throws SvcLogicException {
        String storeId = new String(module + ":" + rpc);
        if (graphStore.containsKey(storeId)) {
            graphStore.remove(storeId);
        }
    }

    @Override
    public void activate(SvcLogicGraph graph) throws SvcLogicException {
        // noop
    }

    @Override
    public void activate(String module, String rpc, String version, String mode) throws SvcLogicException {
        // noop
    }

}
