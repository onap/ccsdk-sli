package org.onap.ccsdk.sli.core.sli.provider.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.LinkedList;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicParser;
import org.onap.ccsdk.sli.core.sli.provider.base.SetNodeExecutor;

public class SetNodeExecutorTest {
    @Test
    public void clearProperties() throws Exception {
        SetNodeExecutor sne = new SetNodeExecutor();
        SvcLogicContext ctx = new SvcLogicContext();

        SvcLogicParser slp = new SvcLogicParser();
        LinkedList<SvcLogicGraph> graph = slp.parse("src/test/resources/clearValues.xml");
        SvcLogicNode root = graph.getFirst().getRootNode();
        SvcLogicNode nodeOne = root.getOutcomeValue("1");
        SvcLogicNode nodeTwo = root.getOutcomeValue("2");

        sne.execute(nodeOne, ctx);
        sne.execute(nodeTwo, ctx);

        assertNull(ctx.getAttribute("si.field1"));
        assertNull(ctx.getAttribute("si.field2"));
        assertNull(ctx.getAttribute("si.field3"));
        assertNull(ctx.getAttribute("si.subarray[0]"));
        assertNull(ctx.getAttribute("si.subarray[1]"));
        assertNull(ctx.getAttribute("si.subarray[2]"));
        assertNull(ctx.getAttribute("si.subarray_length"));
        assertEquals("6", ctx.getAttribute("search1"));
        assertEquals("KeepMe!", ctx.getAttribute("simonSays"));
    }

    @Test
    public void clearMultipleArrayProperties() throws Exception {
        SetNodeExecutor sne = new SetNodeExecutor();
        SvcLogicContext ctx = new SvcLogicContext();

        SvcLogicParser slp = new SvcLogicParser();
        LinkedList<SvcLogicGraph> graph = slp.parse("src/test/resources/clearMultipleArrayValues.xml");
        SvcLogicNode root = graph.getFirst().getRootNode();
        SvcLogicNode nodeOne = root.getOutcomeValue("1");
        SvcLogicNode nodeTwo = root.getOutcomeValue("2");

        sne.execute(nodeOne, ctx);
        sne.execute(nodeTwo, ctx);

        assertNull(ctx.getAttribute("si[0].field1"));
        assertNull(ctx.getAttribute("si[1].field2"));
        assertNull(ctx.getAttribute("si[2].field3"));
        assertNull(ctx.getAttribute("si_length"));
        assertNull(ctx.getAttribute("si[0].subarray[0]"));
        assertNull(ctx.getAttribute("si[0].subarray[1]"));
        assertNull(ctx.getAttribute("si[0].subarray[2]"));
        assertNull(ctx.getAttribute("si[0].subarray_length"));
        assertEquals("6", ctx.getAttribute("search1"));
        assertEquals("KeepMe!", ctx.getAttribute("simonSays"));
    }

    @Test
    public void clearSingleArrayProperties() throws Exception {
        SetNodeExecutor sne = new SetNodeExecutor();
        SvcLogicContext ctx = new SvcLogicContext();

        SvcLogicParser slp = new SvcLogicParser();
        LinkedList<SvcLogicGraph> graph = slp.parse("src/test/resources/clearSingleArrayValues.xml");
        SvcLogicNode root = graph.getFirst().getRootNode();
        SvcLogicNode nodeOne = root.getOutcomeValue("1");
        SvcLogicNode nodeTwo = root.getOutcomeValue("2");

        sne.execute(nodeOne, ctx);
        sne.execute(nodeTwo, ctx);

        assertNull(ctx.getAttribute("si[0].field1"));
        assertNull(ctx.getAttribute("si[0].subarray[0]"));
        assertNull(ctx.getAttribute("si[0].subarray[1]"));
        assertNull(ctx.getAttribute("si[0].subarray[2]"));
        assertNull(ctx.getAttribute("si[0].subarray_length"));
        
        // TODO: This is just setting up elements as null but note reducing the size of Array.
        assertEquals("3",ctx.getAttribute("si_length"));
        
        assertEquals("2",ctx.getAttribute("si[1].field2"));
        assertEquals("3", ctx.getAttribute("si[2].field3"));
        assertEquals("6", ctx.getAttribute("search1"));
        assertEquals("KeepMe!", ctx.getAttribute("simonSays"));
    }
    
    @Test
    public void clearSingleSubArrayProperties() throws Exception {
        SetNodeExecutor sne = new SetNodeExecutor();
        SvcLogicContext ctx = new SvcLogicContext();

        SvcLogicParser slp = new SvcLogicParser();
        LinkedList<SvcLogicGraph> graph = slp.parse("src/test/resources/clearSingleSubArrayValues.xml");
        SvcLogicNode root = graph.getFirst().getRootNode();
        SvcLogicNode nodeOne = root.getOutcomeValue("1");
        SvcLogicNode nodeTwo = root.getOutcomeValue("2");

        sne.execute(nodeOne, ctx);
        sne.execute(nodeTwo, ctx);

        assertEquals("1",ctx.getAttribute("tmp.si[0].field1"));
        assertEquals("2",ctx.getAttribute("tmp.si[1].field2"));
        assertEquals("3", ctx.getAttribute("tmp.si[2].field3"));
        assertEquals("3", ctx.getAttribute("tmp.si_length"));
        
        assertEquals("a",ctx.getAttribute("tmp.si[0].subarray[0]"));
        
        // TODO: This is setting up element as Empty instead null
        //assertNull(ctx.getAttribute("tmp.si[0].subarray[1]"));
        assertEquals("", ctx.getAttribute("tmp.si[0].subarray[1]"));
        
        assertEquals("c", ctx.getAttribute("tmp.si[0].subarray[2]"));
        assertEquals("3", ctx.getAttribute("tmp.si[0].subarray_length"));
        
        assertEquals("x",ctx.getAttribute("tmp.si[1].subarray[0]"));
        assertEquals("y",ctx.getAttribute("tmp.si[1].subarray[1]"));
        assertEquals("z", ctx.getAttribute("tmp.si[1].subarray[2]"));
        assertEquals("3", ctx.getAttribute("tmp.si[1].subarray_length"));
        
        assertEquals("6", ctx.getAttribute("search1"));
        assertEquals("KeepMe!", ctx.getAttribute("simonSays"));
    }
    
    @Test
    public void clearSubArrayProperties() throws Exception {
        SetNodeExecutor sne = new SetNodeExecutor();
        SvcLogicContext ctx = new SvcLogicContext();

        SvcLogicParser slp = new SvcLogicParser();
        LinkedList<SvcLogicGraph> graph = slp.parse("src/test/resources/clearSubArrayValues.xml");
        SvcLogicNode root = graph.getFirst().getRootNode();
        SvcLogicNode nodeOne = root.getOutcomeValue("1");
        SvcLogicNode nodeTwo = root.getOutcomeValue("2");

        sne.execute(nodeOne, ctx);
        sne.execute(nodeTwo, ctx);

        assertEquals("1", ctx.getAttribute("si[0].field1"));
        assertEquals("2",ctx.getAttribute("si[1].field2"));
        assertEquals("3", ctx.getAttribute("si[2].field3"));
        assertEquals("3", ctx.getAttribute("si_length"));
        assertNull(ctx.getAttribute("si[0].subarray[0]"));
        assertNull(ctx.getAttribute("si[0].subarray[1]"));
        assertNull(ctx.getAttribute("si[0].subarray[2]"));
        assertNull(ctx.getAttribute("si[0].subarray_length"));

        assertEquals("6", ctx.getAttribute("search1"));
        assertEquals("KeepMe!", ctx.getAttribute("simonSays"));
    }

    @Test
    public void arrayPattern() {
        SetNodeExecutor sne = new SetNodeExecutor();
        String source = "one.two[0].three[0].four";
        assertEquals("one.two.three.four", source.replaceAll(sne.arrayPattern, ""));
    }

    @Test
    public void subtreeCopy() throws Exception {
        SetNodeExecutor sne = new SetNodeExecutor();
        SvcLogicContext ctx = new SvcLogicContext();

        SvcLogicParser slp = new SvcLogicParser();
        LinkedList<SvcLogicGraph> graph = slp.parse("src/test/resources/copyValues.xml");
        SvcLogicNode root = graph.getFirst().getRootNode();
        SvcLogicNode nodeOne = root.getOutcomeValue("1");
        SvcLogicNode nodeTwo = root.getOutcomeValue("2");

        sne.execute(nodeOne, ctx);
        sne.execute(nodeTwo, ctx);

        assertEquals("1", ctx.getAttribute("si.field1"));
        assertEquals("2", ctx.getAttribute("si.field2"));
        assertEquals("3", ctx.getAttribute("si.field3"));
        assertEquals("1", ctx.getAttribute("rootTwo.field1"));
        assertEquals("2", ctx.getAttribute("rootTwo.field2"));
        assertEquals("3", ctx.getAttribute("rootTwo.field3"));
    }

    @Test
    public void clearNestedSubArrayProperties() throws Exception { 
        SetNodeExecutor sne = new SetNodeExecutor(); 
        SvcLogicContext ctx = new SvcLogicContext(); 
 
        SvcLogicParser slp = new SvcLogicParser(); 
        LinkedList<SvcLogicGraph> graph = slp.parse("src/test/resources/clearNestedSubArrayValues.xml"); 
        SvcLogicNode root = graph.getFirst().getRootNode(); 
        SvcLogicNode nodeOne = root.getOutcomeValue("1"); 
        SvcLogicNode nodeTwo = root.getOutcomeValue("2"); 
 
        sne.execute(nodeOne, ctx); 
        sne.execute(nodeTwo, ctx); 
 
        assertEquals("1", ctx.getAttribute("tmp.si[0].field1")); 
        assertEquals("2",ctx.getAttribute("tmp.si[1].field2")); 
        assertEquals("3", ctx.getAttribute("tmp.si[2].field3")); 
        assertEquals("3", ctx.getAttribute("tmp.si_length")); 
 
        assertNull(ctx.getAttribute("tmp.si[0].subarray[0]")); 
        assertNull(ctx.getAttribute("tmp.si[0].subarray[1]")); 
        assertNull(ctx.getAttribute("tmp.si[0].subarray[2]")); 
        assertNull(ctx.getAttribute("tmp.si[0].subarray_length")); 
        
        assertEquals("x", ctx.getAttribute("tmp.si[1].subarray[0]")); 
        assertEquals("y",ctx.getAttribute("tmp.si[1].subarray[1]")); 
        assertEquals("z", ctx.getAttribute("tmp.si[1].subarray[2]")); 
        assertEquals("3", ctx.getAttribute("tmp.si[1].subarray_length")); 
 
        assertEquals("6", ctx.getAttribute("search1")); 
        assertEquals("KeepMe!", ctx.getAttribute("simonSays")); 
    }

}
