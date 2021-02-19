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

import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
        try{
            LOG.debug( "ENTERING Execute Node \"computePath\"" );

            // Validate, Log, & read parameters
            checkParameters(parameters, new String[]{ "pnfs-pfx", "links-pfx",
                    "src-node", "dst-node", "response-pfx"}, LOG);

            return SUCCESS_CONSTANT;


        } catch( Exception e ) {
            throw new SvcLogicException( "An error occurred in the computePath Execute node", e );
        } finally {
            LOG.debug( "EXITING Execute Node \"computePath\"" );
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