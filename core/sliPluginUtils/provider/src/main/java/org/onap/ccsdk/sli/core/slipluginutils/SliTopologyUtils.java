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
import com.google.gson.JsonNull;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.slipluginutils.slitopologyutils.JsonParserHelper;
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

            String pnfsStr = toJsonString(ctx, parameters.get("pnfs-pfx"));
            String lkStr = toJsonString(ctx, parameters.get("links-pfx"));

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

    private static String toJsonString(SvcLogicContext ctx, String pfx){

        JsonObject root = new JsonObject();
        JsonElement lastJsonObject = root;
        JsonElement currJsonLeaf = root;
        HashMap<String, String> localAttributes = new HashMap<>();
        int pfxlen = pfx.length();
        for (String key: ctx.getAttributeKeySet()){
            if (key.startsWith(pfx)){
                String truncKey = key.substring(pfxlen+1);
                localAttributes.put(truncKey, ctx.getAttribute(key));
            }
        }

        if (localAttributes.size() < 1){
            return "";
        }

        String attrName = null;
        String attrVal = null;
        // Sort properties so that arrays will be reconstructed in proper order
        TreeMap<String, String> sortedAttributes = new TreeMap<>(new Comparator<String>(
        ) {
            @Override
            public int compare(String a, String b){
                int aLength = a.length();
                int bLength = b.length();
                int minSize = Math.min(aLength, bLength);
                char aChar, bChar;
                boolean aNumber, bNumber;
                boolean asNumeric = false;
                int lastNumericCompare = 0;
                for (int i = 0; i < minSize; i++) {
                    aChar = a.charAt(i);
                    bChar = b.charAt(i);
                    aNumber = aChar >= '0' && aChar <= '9';
                    bNumber = bChar >= '0' && bChar <= '9';
                    if (asNumeric)
                        if (aNumber && bNumber) {
                            if (lastNumericCompare == 0)
                                lastNumericCompare = aChar - bChar;
                        } else if (aNumber)
                            return 1;
                        else if (bNumber)
                            return -1;
                        else if (lastNumericCompare == 0) {
                            if (aChar != bChar)
                                return aChar - bChar;
                            asNumeric = false;
                        } else
                            return lastNumericCompare;
                    else if (aNumber && bNumber) {
                        asNumeric = true;
                        if (lastNumericCompare == 0)
                            lastNumericCompare = aChar - bChar;
                    } else if (aChar != bChar)
                        return aChar - bChar;
                }
                if (asNumeric)
                    if (aLength > bLength && a.charAt(bLength) >= '0' && a.charAt(bLength) <= '9') // as number
                        return 1;  // a has bigger size, thus b is smaller
                    else if (bLength > aLength && b.charAt(aLength) >= '0' && b.charAt(aLength) <= '9') // as number
                        return -1;  // b has bigger size, thus a is smaller
                    else if (lastNumericCompare == 0)
                        return aLength - bLength;
                    else
                        return lastNumericCompare;
                else
                    return aLength - bLength;
            }
        });
        sortedAttributes.putAll(localAttributes);

        // Loop through properties, sorted by key
        for (Map.Entry<String, String> entry : sortedAttributes.entrySet()) {
            attrName = entry.getKey();
            attrVal = entry.getValue();

            currJsonLeaf = root;
            String curFieldName = null;
            JsonArray curArray = null;
            lastJsonObject = null;
            boolean addNeeded = false;

            // Split property names by period and iterate through parts
            for (String attrNamePart : attrName.split("\\.")) {

                // Add last object found to JSON tree.  Need to handle
                // this way because last element found (leaf) needs to be
                // assigned the property value.
                if (lastJsonObject != null) {
                    if (addNeeded) {
                        if (currJsonLeaf.isJsonArray()) {
                            ((JsonArray) currJsonLeaf).add(lastJsonObject);
                        } else {
                            ((JsonObject) currJsonLeaf).add(curFieldName, lastJsonObject);
                        }
                    }
                    currJsonLeaf = (JsonObject) lastJsonObject;
                }
                addNeeded = false;
                // See if current level should be a JsonArray or JsonObject based on
                // whether name part contains square brackets.
                if (!attrNamePart.contains("[")) {
                    // This level should be inserted as a JsonObject
                    curFieldName = attrNamePart;
                    lastJsonObject = ((JsonObject) currJsonLeaf).get(curFieldName);
                    if (lastJsonObject == null) {
                        lastJsonObject = new JsonObject();
                        addNeeded = true;
                    } else if (!lastJsonObject.isJsonObject()) {
                        LOG.error("Unexpected condition - expecting to find JsonObject, but found " + lastJsonObject.getClass().getName());
                        lastJsonObject = new JsonObject();
                        addNeeded = true;
                    }
                } else {
                    // This level should be inserted as a JsonArray.

                    String[] curFieldNameParts = attrNamePart.split("[\\[\\]]");
                    curFieldName = curFieldNameParts[0];
                    int curIndex = Integer.parseInt(curFieldNameParts[1]);


                    curArray = ((JsonObject) currJsonLeaf).getAsJsonArray(curFieldName);

                    if (curArray == null) {
                        // This is the first time we see this array.
                        // Create a new JsonArray and add it to current
                        // leaf
                        curArray = new JsonArray();
                        ((JsonObject) currJsonLeaf).add(curFieldName, curArray);
                    }

                    // Current leaf should point to the JsonArray for this level.
                    // lastJsonObject should point to the array item entry to append
                    // the next level to - which is a new one if the index value
                    // isn't the end of the current array.
                    currJsonLeaf = curArray;
                    if (curArray.size() == curIndex + 1) {
                        lastJsonObject = curArray.get(curArray.size() - 1);
                    } else {
                        lastJsonObject = new JsonObject();
                        addNeeded = true;
                    }
                }
            }

            // Done parsing property name.  Add the value of this
            // property to the current json leaf, either as a property
            // or as a string (if the current leaf is a JsonArray)

            if (!curFieldName.endsWith("_length")) {
                if (currJsonLeaf.isJsonArray()) {
                    if ("true".equals(attrVal) || "false".equals(attrVal)) {
                        ((JsonArray) currJsonLeaf).add(Boolean.valueOf(attrVal));
                    } else if ("null".equals(attrVal)) {
                        ((JsonArray) currJsonLeaf).add(new JsonNull());
                    } else {
                        ((JsonArray) currJsonLeaf).add(attrVal);
                    }
                } else {
                    if (("true".equals(attrVal) || "false".equals(attrVal))) {
                        ((JsonObject) currJsonLeaf).addProperty(curFieldName, Boolean.valueOf(attrVal));
                    } else if ("null".equals(attrVal)){

                        ((JsonObject) currJsonLeaf).add(curFieldName, new JsonNull());
                    } else {
                        ((JsonObject) currJsonLeaf).addProperty(curFieldName, attrVal);
                    }
                }
            }
        }

        return (root.toString());
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