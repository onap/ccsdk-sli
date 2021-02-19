package org.onap.ccsdk.sli.core.slipluginutils;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils_ctxSortList;
import org.onap.ccsdk.sli.core.slipluginutils.SliTopologyUtils;
import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.JsonParserHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class SliTopologyUtilsTest {
    private SvcLogicContext ctx;
    private static final Logger LOG = LoggerFactory.getLogger(SliPluginUtils_ctxSortList.class);
    private HashMap<String, String> param;
    private SliTopologyUtils topologyUtil = new SliTopologyUtils();
    @Before
    public void setUp() throws Exception {
        //Loading test logicallinks and pnfs
        this.ctx = new SvcLogicContext();
        param = new HashMap<String, String>();
        String fileName1 = "src/test/resources/Pnfs.json";
        String fileName2 = "src/test/resources/LogicalLinks.json";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName1));
            String fileString = new String(encoded, "UTF-8");
            String pp1 = "Pnfs.";
            Map<String, String> mm = null;
            mm = JsonParserHelper.convertToProperties(fileString);
            if (mm != null) {
                for (Map.Entry<String, String> entry : mm.entrySet()) {
                    ctx.setAttribute(pp1 + entry.getKey(), entry.getValue());
                }
            }

            encoded = Files.readAllBytes(Paths.get(fileName2));
            fileString = new String(encoded, "UTF-8");
            String pp2 = "LogicalLinks.";
            mm = null;
            mm = JsonParserHelper.convertToProperties(fileString);
            if (mm != null) {
                for (Map.Entry<String, String> entry : mm.entrySet()) {
                    ctx.setAttribute(pp2 + entry.getKey(), entry.getValue());
                }
            }

        } catch (Exception e ){
            LOG.trace("Failed to read topology json files" +  e.getMessage());
        }
    }

    @Test
    public void computePath()  throws SvcLogicException {

        param.put("pnfs-pfx", "Pnfs");
        param.put("links-pfx", "LogicalLinks");
        param.put("response-pfx", "prefix");
        param.put("output-end-to-end-path", "true");
        param.put("src-node","networkId-providerId-20-clientId-0-topologyId-1-nodeId-10.2.1.2" );
        param.put("dst-node", "networkId-providerId-10-clientId-0-topologyId-1-nodeId-10.1.1.4");

        SliTopologyUtils.computePath(param, ctx);
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);
        assertTrue(Integer.parseInt(this.ctx.getAttribute("prefix.solutions_length") ) > 0);
    }
}