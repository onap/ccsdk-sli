package org.onap.ccsdk.sli.core.sli.provider.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicParser;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphTests {

    private static final Logger LOG = LoggerFactory.getLogger(GraphTests.class);
    private static final SvcLogicStore store = new InMemorySvcLogicStore();
    private static final HashMapResolver resolver = new HashMapResolver();
    private static final SvcLogicServiceBase svc = new SvcLogicServiceImplBase(store, resolver);
    private static final SvcLogicParser p = new SvcLogicParser();
    // Write a very simple recorder so record nodes can be used during debugging
    private static final SvcLogicRecorder recorder = new SvcLogicRecorder() {
        @Override
        public void record(Map<String, String> map) throws SvcLogicException {
            map.remove("level");
            for (Entry<String, String> entry : map.entrySet()) {
                LOG.debug(entry.getKey() + " = " + entry.getValue());
            }
        }
    };

    @Test
    public void testBreakNode() throws Exception {
        // This graph as a for node that will loop with start 0 and end 999
        // in the loop idx is printed and variable "a" is incremented by 1
        // there is an if block in the loop that when a equals 2 a break node should execute and break out of the for
        // loop
        SvcLogicContext ctx = executeGraph("src/test/resources/breakGraph.xml");
        assertTrue(ctx.isSuccess());
        assertEquals("2", ctx.getAttribute("idx")); // the break should happen when idx equals 2
        assertEquals("3", ctx.getAttribute("a")); // incrementing a happens before the break so a should be idx + 1
    }

    public SvcLogicContext executeGraph(String pathToGraph) throws SvcLogicException {
        return executeGraph(pathToGraph, new SvcLogicContext());
    }

    public SvcLogicContext executeGraph(String pathToGraph, SvcLogicContext context) throws SvcLogicException {
        resolver.addSvcLogicRecorder("org.onap.ccsdk.sli.core.sli.recording.Slf4jRecorder", recorder);
        LinkedList<SvcLogicGraph> graphList = p.parse(pathToGraph);
        SvcLogicGraph graph = graphList.get(0);
        store.store(graph);
        store.activate(graph);
        return svc.execute(graph, context);
    }

}
