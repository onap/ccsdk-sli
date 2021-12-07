/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
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
package org.onap.ccsdk.sli.core.slipluginutils;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.JsonParserHelper;
import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph.BhandariGraphSearch;
import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph.DijkstraGraphSearch;
import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph.Graph;
import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.graph.Path;
import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology.*;
import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.topology.LogicalLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class SliTopologyUtils implements SvcLogicJavaPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(SliTopologyUtils.class);
    public static final String SUCCESS_CONSTANT = "success";
    public static final String FAILURE_CONSTANT = "failure";
    public static final String NOT_FOUND_CONSTANT = "not-found";


    public SliTopologyUtils(){};
    /**
     * Provides simple path computation functionality to Directed Graphs.
     * <p>
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     * 	<thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     * 	<tbody>
     * 		<tr><td>pnfs-pfx</td><td>Mandatory</td><td>Prefix in context memory to get the pnf attributes from.</td></tr>
     * 		<tr><td>links-pfx</td><td>Mandatory</td><td>Prefix in context memory to get the link attributes from.</td></tr>
     * 		<tr><td>src-node</td><td>Mandatory</td><td>Source pnf name.</td></tr>
     * 		<tr><td>dst-node</td><td>Mandatory</td><td>Destination pnf name.</td></tr>
     * 		<tr><td>response-pfx</td><td>Mandatory</td><td>Prefix in context memory to populate the resulting attributes in.</td></tr>
     * 		<tr><td>output-end-to-end-path</td><td>Optional</td><td>true or false to output end to end full path. If not included, only output cross domain path</td></tr>
     * 	</tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     */
    public static String computePath(Map<String, String> parameters, SvcLogicContext ctx ) throws SvcLogicException {

        LOG.debug( "ENTERING Execute Node \"computePath\"" );
        boolean outputFullPath = false;
        Graph<Pnf, LogicalLink> graph;
        Pnf src;
        Pnf dst;

        try{
            // Validate, Log, & read parameters
            checkParameters(parameters, new String[]{ "pnfs-pfx", "links-pfx",
                    "src-node", "dst-node", "response-pfx"}, LOG);
            String outputEndToEnd = parameters.get("output-end-to-end-path");

            if (outputEndToEnd != null && outputEndToEnd.equals("true")){
                outputFullPath = true;
                LOG.debug( "OutputEndToEndPath enabled");
            }

            String pnfsStr = ctx.toJsonString( parameters.get("pnfs-pfx"));
            String lkStr = ctx.toJsonString( parameters.get("links-pfx"));

            if (pnfsStr.isEmpty()){
                LOG.warn("Pnf Array attributes are empty");
                throw new Exception( "Pnf Array attributes are empty");
            }

            if (lkStr.isEmpty()){
                LOG.warn("Logical-links Array attributes are empty");
                throw new Exception( "Logical-links Array attributes are empty");
            }

            LOG.debug("Pnf Json String is: {}", pnfsStr);

            String srcNodeStr = parameters.get("src-node");
            String dstNodeStr = parameters.get("dst-node");

            if( srcNodeStr.isEmpty() || dstNodeStr.isEmpty()){
                LOG.warn("Src or Dst node is empty");
                throw new Exception("Src or Dst node is empty");
            }

            JsonParser jp = new JsonParser();

            JsonArray pnfArr = ((JsonObject) jp.parse(pnfsStr)).getAsJsonArray("pnf");
            JsonArray lkArr = ((JsonObject) jp.parse(lkStr)).getAsJsonArray("logical-link");
            LOG.debug("Creating graph with {} pnf(s) and {} link(s)", pnfArr.size(), lkArr.size());
            graph = buildGraph(pnfArr, lkArr);

            src = new Pnf(srcNodeStr);
            dst = new Pnf(dstNodeStr);

            if (!graph.getVertexes().contains(src) || !graph.getVertexes().contains(dst)){
                LOG.warn("Src or Dst node doesn't exist");
                throw new Exception("Src or Dst node doesn't exist");
            }

        } catch( Exception e ) {
            throw new SvcLogicException( "An error occurred in the computePath Execute node; failed to process the" +
                    "given params for path computation", e );
        }

        try {
            DijkstraGraphSearch.Result result =
                    new DijkstraGraphSearch<Pnf, LogicalLink>().search(graph, src, dst, null, -1);
            LOG.debug("Path Computing results: {}", result.paths().toString());

            if (result.paths().size() > 0) {
                JsonObject root = new JsonObject();
                JsonArray solnList = new JsonArray();

                Path<Pnf, LogicalLink> path = (Path<Pnf, LogicalLink>) result.paths().iterator().next();
                for (LogicalLink logicalLink : path.edges()) {
                    if (!outputFullPath && ((OtnLink) logicalLink.underlayLink()).isInnerDomain()) {
                        //Ignore inner domain links
                    } else {
                        JsonObject curLink = new JsonObject();
                        String srcNode = logicalLink.src().toString();
                        String dstNode = logicalLink.dst().toString();
                        String srcPInterface = ((OtnLink) logicalLink.underlayLink()).src().pInterfaceName().getName();
                        String dstPInterface = ((OtnLink) logicalLink.underlayLink()).dst().pInterfaceName().getName();
                        String linkName = ((OtnLink) logicalLink.underlayLink()).linkName();
                        curLink.addProperty("src_node", srcNode);
                        curLink.addProperty("dst_node", dstNode);
                        curLink.addProperty("src_pinterface", srcPInterface);
                        curLink.addProperty("dst_pinterface", dstPInterface);
                        curLink.addProperty("original_link", linkName);

                        solnList.add(curLink);
                    }
                }
                root.add("solutions", solnList);
                //Write result back to context memory;
                String pp = parameters.get("response-pfx").isEmpty() ? "" : parameters.get("response-pfx") + ".";
                Map<String, String> mm = null;
                mm = JsonParserHelper.convertToProperties(root.toString());
                if (mm != null) {
                    for (Map.Entry<String, String> entry : mm.entrySet()) {
                        ctx.setAttribute(pp + entry.getKey(), entry.getValue());
                    }
                }
                LOG.debug("SliTopologyUtils: path computation succeeds in finding the shortest path;" +
                        " result has been written back into context memory.");
                return SUCCESS_CONSTANT;
            } else {
                LOG.debug("SliTopologyUtils: no valid path found.");
                return NOT_FOUND_CONSTANT;
            }
        } catch (Exception e){
            throw new SvcLogicException( "An error occurred in the computePath Execute node; failed to execute the graph " +
                    "computation", e );
        } finally {
            LOG.debug( "Exiting Execute Node \"computePath\"" );
        }
    }

    /**
     * Provides disjoint paths computation functionality to Directed Graphs.
     * <p>
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     * 	<thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     * 	<tbody>
     * 		<tr><td>pnfs-pfx</td><td>Mandatory</td><td>Prefix in context memory to get the pnf attributes from.</td></tr>
     * 		<tr><td>links-pfx</td><td>Mandatory</td><td>Prefix in context memory to get the link attributes from.</td></tr>
     * 		<tr><td>src-node</td><td>Mandatory</td><td>Source pnf name.</td></tr>
     * 		<tr><td>dst-node</td><td>Mandatory</td><td>Destination pnf name.</td></tr>
     * 		<tr><td>dst-node-backup</td><td>Optional</td><td>Destination backup pnf name.</td></tr>
     * 		<tr><td>require-backuppath</td><td>Mandatory</td><td>If true, output primary and secondary paths.</td></tr>
     * 		<tr><td>response-pfx</td><td>Mandatory</td><td>Prefix in context memory to populate the resulting attributes in.</td></tr>
     * 		<tr><td>output-end-to-end-path</td><td>Optional</td><td>true or false to output end to end full path. If not included, only output cross domain path</td></tr>
     * 	</tbody>
     * </table>
     * @param ctx Reference to context memory
     * @throws SvcLogicException
     */
    public static String computePaths(Map<String, String> parameters, SvcLogicContext ctx ) throws SvcLogicException {

        LOG.debug( "ENTERING Execute Node \"computePaths (v2)\"" );
        boolean outputFullPath = false;
        boolean requireSecondaryPath = false;
        boolean isDifferentDst = false;
        Graph<Pnf, LogicalLink> graph;
        Pnf src;
        Pnf dst;
        Pnf dst2 = null;

        try{
            // Validate, Log, & read parameters
            checkParameters(parameters, new String[]{ "pnfs-pfx", "links-pfx",
                    "src-node", "dst-node", "response-pfx"}, LOG);
            String outputEndToEnd = parameters.get("output-end-to-end-path");
            String pnfsStr = ctx.toJsonString( parameters.get("pnfs-pfx"));
            String lkStr = ctx.toJsonString( parameters.get("links-pfx"));
            String srcNodeStr = parameters.get("src-node");
            String dstNodeStr = parameters.get("dst-node");
            String requireBackupPath = parameters.get("require-backuppath");
            String dstNodeBackupStr = parameters.get("dst-node-backup");

            if (outputEndToEnd != null && outputEndToEnd.equals("true")){
                outputFullPath = true;
                LOG.debug( "OutputEndToEndPath enabled");
            }

            if (requireBackupPath != null && requireBackupPath.equals("true")){
                requireSecondaryPath = true;
                LOG.debug( "RequiredBackupPath enabled");
            }

            if (pnfsStr.isEmpty()){
                LOG.warn("Pnf Array attributes are empty");
                throw new Exception( "Pnf Array attributes are empty");
            }

            if (lkStr.isEmpty()){
                LOG.warn("Logical-links Array attributes are empty");
                throw new Exception( "Logical-links Array attributes are empty");
            }

            LOG.debug("Pnf Json String is: {}", pnfsStr);

            if( srcNodeStr.isEmpty() || dstNodeStr.isEmpty()){
                LOG.warn("Src or Dst node is empty");
                throw new Exception("Src or Dst node is empty");
            }

            JsonParser jp = new JsonParser();

            JsonArray pnfArr = ((JsonObject) jp.parse(pnfsStr)).getAsJsonArray("pnf");
            JsonArray lkArr = ((JsonObject) jp.parse(lkStr)).getAsJsonArray("logical-link");
            LOG.debug("Creating graph with {} pnf(s) and {} link(s)", pnfArr.size(), lkArr.size());
            graph = buildGraph(pnfArr, lkArr);


            src = new Pnf(srcNodeStr);
            dst = new Pnf(dstNodeStr);

            if (!graph.getVertexes().contains(src) || !graph.getVertexes().contains(dst)){
                LOG.warn("Src or Dst node doesn't exist");
                throw new Exception("Src or Dst node doesn't exist");
            }

            if( dstNodeBackupStr != null && !dstNodeBackupStr.isEmpty()){
                dst2 = new Pnf(dstNodeBackupStr);
                if (!graph.getVertexes().contains(dst2)){
                    LOG.warn("Backup Dst node doesn't exist");
                    throw new Exception("Backup Dst node doesn't exist");
                }
                isDifferentDst = true;
                appendDummyNode(graph, dst, dst2);
            }
        } catch( Exception e ) {
            throw new SvcLogicException( "An error occurred in the computePath Execute node; failed to process the" +
                    "given params for path computation", e );
        }

        try {
            if (requireSecondaryPath){
                //Disjoint paths compuation using Bhandari Algorithm
                BhandariGraphSearch.DisjointPathResult result;
                if (!isDifferentDst){
                    result =
                            new BhandariGraphSearch<Pnf, LogicalLink>()
                                    .searchForDisjointPath(graph, src, dst, null, -1);
                } else {
                    result =
                            new BhandariGraphSearch<Pnf, LogicalLink>()
                                    .searchForDisjointPath(graph, src, new Pnf("FAKEPNF"), null, -1);
                }


                if (result != null && result.paths().size() == 2){
                    JsonObject root = new JsonObject();
                    JsonArray oneSolnList = new JsonArray();
                    JsonArray otherSolnList = new JsonArray();
                    boolean hasPrime = false;
                    Iterator<Path<Pnf, LogicalLink>> pathIter = result.paths().iterator();
                    Path<Pnf, LogicalLink> one = pathIter.next();
                    Path<Pnf, LogicalLink> other = pathIter.next();
                    for (LogicalLink logicalLink : one.edges()){
                        if (logicalLink.underlayLink().type() == Link.Type.DUMMY){
                            continue;
                        }
                        if (logicalLink.dst().equals(dst)){
                            hasPrime = true;
                        }
                        if (!outputFullPath && ((OtnLink) logicalLink.underlayLink()).isInnerDomain()) {
                            //Ignore inner domain links
                        } else {
                            JsonObject curLink = new JsonObject();

                            String srcNode = logicalLink.src().toString();
                            String dstNode = logicalLink.dst().toString();
                            String srcPInterface = ((OtnLink) logicalLink.underlayLink()).src().pInterfaceName().getName();
                            String dstPInterface = ((OtnLink) logicalLink.underlayLink()).dst().pInterfaceName().getName();
                            String linkName = ((OtnLink) logicalLink.underlayLink()).linkName();
                            curLink.addProperty("src_node", srcNode);
                            curLink.addProperty("dst_node", dstNode);
                            curLink.addProperty("src_pinterface", srcPInterface);
                            curLink.addProperty("dst_pinterface", dstPInterface);
                            curLink.addProperty("original_link", linkName);

                            oneSolnList.add(curLink);
                        }
                    }
                    if (hasPrime){
                        root.add("solutions", oneSolnList);
                    } else {
                        root.add("secondarySolutions", oneSolnList);
                    }

                    for (LogicalLink logicalLink : other.edges()){
                        if (logicalLink.underlayLink().type() == Link.Type.DUMMY){
                            continue;
                        }
                        if (!outputFullPath && ((OtnLink) logicalLink.underlayLink()).isInnerDomain()) {
                            //Ignore inner domain links
                        } else {
                            JsonObject curLink = new JsonObject();
                            String srcNode = logicalLink.src().toString();
                            String dstNode = logicalLink.dst().toString();
                            String srcPInterface = ((OtnLink) logicalLink.underlayLink()).src().pInterfaceName().getName();
                            String dstPInterface = ((OtnLink) logicalLink.underlayLink()).dst().pInterfaceName().getName();
                            String linkName = ((OtnLink) logicalLink.underlayLink()).linkName();
                            curLink.addProperty("src_node", srcNode);
                            curLink.addProperty("dst_node", dstNode);
                            curLink.addProperty("src_pinterface", srcPInterface);
                            curLink.addProperty("dst_pinterface", dstPInterface);
                            curLink.addProperty("original_link", linkName);

                            otherSolnList.add(curLink);
                        }
                    }
                    if (hasPrime){
                        root.add("secondarySolutions", otherSolnList);
                    } else {
                        root.add("solutions", otherSolnList);
                    }
                    LOG.debug("Path computing results: {}", root.toString());
                    //Write result back to context memory;
                    String pp = parameters.get("response-pfx").isEmpty() ? "" : parameters.get("response-pfx") + ".";
                    Map<String, String> mm = null;
                    mm = JsonParserHelper.convertToProperties(root.toString());
                    if (mm != null) {
                        for (Map.Entry<String, String> entry : mm.entrySet()) {
                            ctx.setAttribute(pp + entry.getKey(), entry.getValue());
                        }
                    }
                    return SUCCESS_CONSTANT;
                } else {
                    LOG.debug("SliTopologyUtils: no valid path found.");
                    return NOT_FOUND_CONSTANT;
                }

            } else {
                //Single path compuation using Dijkstra
                DijkstraGraphSearch.Result result =
                        new DijkstraGraphSearch<Pnf, LogicalLink>().search(graph, src, dst, null, -1);
                LOG.debug("Path computing results: {}", result.paths().toString());

                if (result.paths().size() > 0) {
                    JsonObject root = new JsonObject();
                    JsonArray solnList = new JsonArray();

                    Path<Pnf, LogicalLink> path = (Path<Pnf, LogicalLink>) result.paths().iterator().next();
                    for (LogicalLink logicalLink : path.edges()) {
                        if (!outputFullPath && ((OtnLink) logicalLink.underlayLink()).isInnerDomain()) {
                            //Ignore inner domain links
                        } else {
                            JsonObject curLink = new JsonObject();
                            String srcNode = logicalLink.src().toString();
                            String dstNode = logicalLink.dst().toString();
                            String srcPInterface = ((OtnLink) logicalLink.underlayLink()).src().pInterfaceName().getName();
                            String dstPInterface = ((OtnLink) logicalLink.underlayLink()).dst().pInterfaceName().getName();
                            String linkName = ((OtnLink) logicalLink.underlayLink()).linkName();
                            curLink.addProperty("src_node", srcNode);
                            curLink.addProperty("dst_node", dstNode);
                            curLink.addProperty("src_pinterface", srcPInterface);
                            curLink.addProperty("dst_pinterface", dstPInterface);
                            curLink.addProperty("original_link", linkName);

                            solnList.add(curLink);
                        }
                    }
                    root.add("solutions", solnList);
                    //Write result back to context memory;
                    String pp = parameters.get("response-pfx").isEmpty() ? "" : parameters.get("response-pfx") + ".";
                    Map<String, String> mm = null;
                    mm = JsonParserHelper.convertToProperties(root.toString());
                    if (mm != null) {
                        for (Map.Entry<String, String> entry : mm.entrySet()) {
                            ctx.setAttribute(pp + entry.getKey(), entry.getValue());
                        }
                    }
                    LOG.debug("SliTopologyUtils: path computation succeeds in finding the shortest path;" +
                            " result has been written back into context memory.");
                    return SUCCESS_CONSTANT;
                } else {
                    LOG.debug("SliTopologyUtils: no valid path found.");
                    return NOT_FOUND_CONSTANT;
                }
            }

        } catch (Exception e){
            throw new SvcLogicException( "An error occurred in the computePath Execute node; failed to execute the graph " +
                    "computation", e );
        } finally {
            LOG.debug( "Exiting Execute Node \"computePath\"" );
        }
    }
    private static Graph<Pnf, LogicalLink> buildGraph(JsonArray pnfs, JsonArray llks) {
        ImmutableSet.Builder pnfSetBlder = ImmutableSet.builder();
        ImmutableSet.Builder lkSetBlder = ImmutableSet.builder();

        //Create Immutable set of Pnf;
        for (int i = 0,e = pnfs.size(); i < e; i++){
            JsonElement pnfName = ((JsonObject) pnfs.get(i)).get("pnf-name");

            if (pnfName != null){
                String pnfNameStr = pnfName.getAsString();

                if (pnfNameStr != null && !pnfNameStr.isEmpty()){
                    pnfSetBlder.add(new Pnf(pnfNameStr));
                }

            } else {
                LOG.debug("SliTopologyUtils: invalid pnf: {}", ((JsonObject) pnfs.get(i)).toString());
            }
        }

        //Create Immutable set of Logical-Link
        for (int i = 0,e = llks.size(); i < e; i++){
            JsonObject lkRoot = ((JsonObject) llks.get(i));
            JsonElement relationList = lkRoot.get("relationship-list");

            if (relationList != null) {
                JsonElement relationListArray = ((JsonObject) relationList).get("relationship");

                if (relationListArray != null){
                    List<String> pnfNameStrList = new ArrayList<>();
                    List<String> pInterfaceStrList= new ArrayList<>();

                    for (int j = 0,k = ((JsonArray) relationListArray).size(); j < k; j++){
                        JsonObject relation = ((JsonArray) relationListArray).get(j).getAsJsonObject();
                        JsonElement relatedTo = relation.getAsJsonPrimitive("related-to");

                        if (relatedTo != null && relatedTo.getAsString().equals("p-interface")){
                            JsonArray data = relation.getAsJsonArray("relationship-data");
                            for (int m = 0, n = data.size(); m < n; m++){
                                JsonObject dataKeyValue = data.get(m).getAsJsonObject();

                                if (dataKeyValue.get("relationship-key").getAsString().equals("pnf.pnf-name")){
                                    pnfNameStrList.add(dataKeyValue.get("relationship-value").getAsString());
                                } else if (dataKeyValue.get("relationship-key").getAsString()
                                        .equals("p-interface.interface-name")){
                                    pInterfaceStrList.add(dataKeyValue.get("relationship-value").getAsString());
                                }
                            }
                        }
                    }

                    if (pnfNameStrList.size() == 2 && pInterfaceStrList.size() == 2){
                        String pnf1NameStr = pnfNameStrList.get(0);
                        String pnf2NameStr = pnfNameStrList.get(1);
                        String pI1NameStr = pInterfaceStrList.get(0);
                        String pI2NameStr = pInterfaceStrList.get(1);
                        Pnf pnf1 = new Pnf(pnf1NameStr);
                        Pnf pnf2 = new Pnf(pnf2NameStr);
                        PInterfaceName pI1Name = PInterfaceName.of(pI1NameStr);
                        PInterfaceName pI2Name = PInterfaceName.of(pI2NameStr);
                        PInterface pI1 = new PInterface(pnf1NameStr, pI1Name);
                        PInterface pI2 = new PInterface(pnf1NameStr, pI2Name);
                        String linkName_f = pI1Name.getNetworkId() + "-linkId-"
                                                + pI1Name.getPnfId() + "-"
                                                + pI1Name.getLtpId();
                        String linkName_b = pI2Name.getNetworkId()
                                + "-linkId-" + pI2Name.getPnfId()
                                + "-" + pI2Name.getLtpId();
                        OtnLink link_f = new OtnLink(linkName_f, pI1, pI2);
                        OtnLink link_b = new OtnLink(linkName_b, pI2, pI1);
                        lkSetBlder.add(new LogicalLink(pnf1, pnf2, link_f));
                        lkSetBlder.add(new LogicalLink(pnf2, pnf1, link_b));
                    }
                }
            }
        }
        return new Graph<Pnf, LogicalLink>(pnfSetBlder.build(), lkSetBlder.build());
    }
    private static void appendDummyNode(Graph<Pnf, LogicalLink> gt, Pnf...pnfs) {
        if (pnfs != null){
            Pnf fakePnf = new Pnf("FAKEPNF");
            for (Pnf pnf: pnfs){
                gt.addVertex(fakePnf);
                gt.addEdge(new LogicalLink(pnf, fakePnf, new DummyLink()));
                gt.addEdge(new LogicalLink(fakePnf, pnf, new DummyLink()));
            }
        }
    }
    /**
     * Throws an exception and writes an error to the log file if a required
     * parameters is not found in the parametersMap.
     * <p>
     * Use at the beginning of functions that can be called by Directed Graphs
     * and can take parameters to verify that all parameters have been provided
     * by the Directed Graph.
     * @param parametersMap parameters Map passed to this node
     * @param requiredParams Array of parameters required by the calling function
     * @param log Reference to Logger to log to
     * @throws SvcLogicException if a String in the requiredParams array is
     * not a key in parametersMap.
     * @since 1.0
     */
    public static final void checkParameters(Map<String, String> parametersMap, String[] requiredParams, Logger log) throws SvcLogicException {
        if( requiredParams == null || requiredParams.length < 1){
            log.debug("required parameters was empty, exiting early.");
            return;
        }
        if (parametersMap == null || parametersMap.keySet().isEmpty()){
            String errorMessage = "This method requires the parameters [" +   StringUtils.join(requiredParams,",") + "], but no parameters were passed in.";
            log.error(errorMessage);
            throw new SvcLogicException(errorMessage);
        }

        for (String param : requiredParams) {
            if (!parametersMap.containsKey(param)) {
                String errorMessage = "Required parameter \"" + param + "\" was not found in parameter list.";
                log.error(errorMessage);
                log.error("Total list of required parameters is [" + StringUtils.join(requiredParams, ",") + "].");
                throw new SvcLogicException(errorMessage);
            }
        }
    }
}