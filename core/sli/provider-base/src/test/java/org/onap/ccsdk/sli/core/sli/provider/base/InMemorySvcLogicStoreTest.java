package org.onap.ccsdk.sli.core.sli.provider.base;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;

public class InMemorySvcLogicStoreTest {
    @Test
    public void simpleTest() throws Exception {
        InMemorySvcLogicStore store = new InMemorySvcLogicStore();
        store.init(new Properties());
        SvcLogicGraph graph = new SvcLogicGraph();
        String module = "TEST";
        String rpc = "NOTIFICATION";
        String mode = "sync";
        String version = "1";

        graph.setModule(module);
        graph.setRpc(rpc);
        graph.setMode(mode);
        graph.setVersion(version);

        store.store(graph);
        assertTrue(store.hasGraph(module, rpc, version, mode));
        assertNotNull(store.fetch(module, rpc, version, mode));
        store.activate(graph);
        store.activate(module, rpc, version, mode);

        store.delete(module, rpc, version, mode);
        assertNull(store.fetch(module, rpc, version, mode));
    }
}
