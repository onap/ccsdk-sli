package org.onap.ccsdk.sli.plugins.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TemplateNodeTest {

    @Test
    public void sunnyDay() throws Exception {
        String requestId = "REQ001";
        String uniqueKey = "UNIQUE_TEST";
        String action = "uPdaTe";
        String serviceType = "VPN";

        TemplateNode t = new MockTemplateNode();

        Map<String, String> params = new HashMap<String, String>();
        params.put(TemplateNode.PREFIX_KEY, "output");
        params.put(TemplateNode.OUTPUT_PATH_KEY, "mycontainer");
        params.put(TemplateNode.TEMPLATE_PATH, "src/test/resources/basic.vtl");
        params.put("service-type", serviceType);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("input.svc-request-id", requestId);
        ctx.setAttribute("input.unique-key", uniqueKey);
        ctx.setAttribute("action", action);

        t.evaluateTemplate(params, ctx);
        String result = ctx.getAttribute("output.mycontainer");
        assertNotNull(result);
        assertTrue(result.contains(requestId));
        assertTrue(result.contains(uniqueKey));
        assertTrue(result.contains(action.toUpperCase()));
        assertTrue(result.contains(serviceType));
    }

    @Test(expected = SvcLogicException.class)
    public void parameterException() throws Exception {
        TemplateNode t = new MockTemplateNode();
        Map<String, String> params = new HashMap<String, String>();
        SvcLogicContext ctx = new SvcLogicContext();
        t.evaluateTemplate(params, ctx);
    }

    @Test(expected = SvcLogicException.class)
    public void missingTemplate() throws Exception {
        TemplateNode t = new MockTemplateNode();
        Map<String, String> params = new HashMap<String, String>();
        params.put(TemplateNode.PREFIX_KEY, "output");
        params.put(TemplateNode.OUTPUT_PATH_KEY, "mycontainer");
        params.put(TemplateNode.TEMPLATE_PATH, "src/test/resources/missing.vtl");
        SvcLogicContext ctx = new SvcLogicContext();
        t.evaluateTemplate(params, ctx);
    }

    @Test
    public void withProperties() throws Exception {
        System.setProperty(TemplateNode.PROPERTIES_DIR_KEY, "src/test/resources");
        TemplateNode t = new TemplateNode();
        Vector<String> loader = (Vector<String>) t.ve.getProperty(RuntimeConstants.RESOURCE_LOADER);
        assertTrue(loader.contains("class"));
        assertEquals("/home/my/example", t.ve.getProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH));
        assertEquals("true", t.ve.getProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE));
        assertEquals("customValue", t.ve.getProperty("custom.property"));
    }

    @Test
    public void withNoProperties() throws Exception {
        System.setProperty(TemplateNode.PROPERTIES_DIR_KEY, "i/do/not/exist");
        TemplateNode t = new TemplateNode();
        Vector<String> loader = (Vector<String>) t.ve.getProperty(RuntimeConstants.RESOURCE_LOADER);
        assertTrue(loader.contains("file"));
        assertEquals("/opt/onap/sdnc/restapi/templates", t.ve.getProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH));
        assertEquals("false", t.ve.getProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE));
        assertEquals(null, t.ve.getProperty("custom.property"));
    }

}