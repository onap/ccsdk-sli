package org.onap.ccsdk.sli.core.slipluginutils;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


import static org.junit.Assert.assertTrue;

public class SliTopologyUtilsTest {
    private SvcLogicContext ctx;
    private static final Logger LOG = LoggerFactory.getLogger(SliTopologyUtils.class);
    private HashMap<String, String> param;
    private SliTopologyUtils topologyUtil = new SliTopologyUtils();
    @Before
    public void setUp() throws Exception {
        //Loading test logicallinks and pnfs
        this.ctx = new SvcLogicContext();
        param = new HashMap<String, String>();
        String fileName = "src/test/resources/context.txt";

        try (FileInputStream fstr = new FileInputStream(new File(fileName));
             InputStreamReader is = new InputStreamReader(fstr,StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(is))
        {
            String line;
            while ((line = br.readLine()) != null){
                if (! line.startsWith("#")){
                    String [] curpair = line.split("\\s=\\s");
                    if (curpair.length == 2){
                        ctx.setAttribute(curpair[0], curpair[1]);
                    } else if (curpair.length == 1){
                        //ctx.setAttribute(curpair[0], "");
                        //LOG.info("Ignore empty (value) context memory record format {}", line);
                    } else {
                        //LOG.info("Ignore incorrect context memory record format {}", line);
                    }
                }
            }
            //SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.INFO);
        } catch (Exception e) {
            throw new SvcLogicException("Cannot read context from file " + fileName, e);
        }
    }

    @Test
    public void testComputePath()  throws SvcLogicException {

        param.put("pnfs-pfx", "ccsdkTopopnfs");
        param.put("links-pfx", "ccsdkTopologicalLinks");
        param.put("response-pfx", "prefix");
        param.put("output-end-to-end-path", "false");

        param.put("src-node","networkId-providerId-10-clientId-0-topologyId-1-nodeId-10.1.1.1" );
        param.put("dst-node", "networkId-providerId-20-clientId-0-topologyId-1-nodeId-10.2.1.2");

        SliTopologyUtils.computePath(param, ctx);
        //SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.INFO);
        assertTrue(Integer.parseInt(this.ctx.getAttribute("prefix.solutions_length") ) > 0);
        LOG.info("Computation finished");

        for (String key: this.ctx.getAttributeKeySet()){
            if (key.startsWith("prefix")){
                LOG.info("Results: {} : {}" , key, this.ctx.getAttribute(key));
            }
        }
    }


    @Test
    public void testComputePaths()  throws SvcLogicException {

        param.put("pnfs-pfx", "ccsdkTopopnfs");
        param.put("links-pfx", "ccsdkTopologicalLinks");
        param.put("response-pfx", "prefix");
        param.put("output-end-to-end-path", "false");
        param.put("require-backuppath", "true");
        param.put("src-node","networkId-providerId-10-clientId-0-topologyId-1-nodeId-10.1.1.1" );
        param.put("dst-node", "networkId-providerId-20-clientId-0-topologyId-1-nodeId-10.2.1.2");
        param.put("dst-node-backup", "networkId-providerId-20-clientId-0-topologyId-1-nodeId-10.2.1.3");

        SliTopologyUtils.computePaths(param, ctx);
        //SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.INFO);
        assertTrue(Integer.parseInt(this.ctx.getAttribute("prefix.solutions_length") ) > 0);
        assertTrue(Integer.parseInt(this.ctx.getAttribute("prefix.secondarySolutions_length") ) > 0);
        LOG.info(this.ctx.getAttribute("prefix.secondarySolutions[0].original_link"));
        for (String key: this.ctx.getAttributeKeySet()){
            if (key.startsWith("prefix")){
                LOG.info("Results: {} : {}" , key, this.ctx.getAttribute(key));
            }
        }
        assertTrue(this.ctx.getAttribute("prefix.secondarySolutions[0].original_link").equals("networkId-providerId-10-clientId-0-topologyId-1-linkId-10.1.1.3-8"));

        LOG.info("Computation finished");


    }
}