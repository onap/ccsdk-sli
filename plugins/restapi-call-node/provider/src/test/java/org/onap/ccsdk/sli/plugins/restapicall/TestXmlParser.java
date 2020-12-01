/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.plugins.restapicall;

import static org.junit.Assert.assertEquals;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TestXmlParser {

    @Test
    public void test() throws Exception {
        String path = "src/test/resources/test3.xml";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Set<String> listNameList = new HashSet<String>();
        listNameList.add("project.dependencies.dependency");
        listNameList.add("project.build.plugins.plugin");
        listNameList.add("project.build.plugins.plugin.executions.execution");
        listNameList.add("project.build.pluginManagement.plugins.plugin");
        listNameList.add("project.build.pluginManagement." +
                        "plugins.plugin.configuration.lifecycleMappingMetadata.pluginExecutions.pluginExecution");

        Map<String, String> mm = XmlParser.convertToProperties(content, listNameList);
        assertEquals("811182", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VpnId"));
        assertEquals("v6", mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.Family"));
        assertEquals("SET6_BVOIP_IN", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfImport"));
        assertEquals("AG_MAX_MCASTROUTES",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.ApplyGroup.ApplyGroup"));
        assertEquals("ICOREPVC-81114561", mm.get("ApplyGroupResponse.ApplyGroupResponseData.ServiceInstanceId"));
        assertEquals("SET_RESET_LP", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport"));
        assertEquals("21302:811182", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfName"));
        assertEquals("BGP4_PROTOCOL",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.RoutingProtocol"));
        assertEquals("AG6_MAX_PREFIX",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.ApplyGroupPeer.ApplyGroup"));
        assertEquals("VPNL811182", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.End2EndVpnKey"));
        assertEquals("AG6_BFD_BGP_3000",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.ApplyGroupNeighbour.ApplyGroup"));
        assertEquals("200", mm.get("ApplyGroupResponse.response-code"));
        assertEquals("gp6_21302:811182",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.PeerGroupName"));
        assertEquals("Y", mm.get("ApplyGroupResponse.ack-final-indicator"));
        assertEquals("Success", mm.get("ApplyGroupResponse.response-message"));
    }

    @Test
    public void testValidLength() throws Exception {
        String path = "src/test/resources/test3.xml";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Set<String> listNameList = new HashSet<String>();
        listNameList.add("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfImport");
        listNameList.add("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport");

        Map<String, String> mm = XmlParser.convertToProperties(content, listNameList);
        assertEquals("AG6_BFD_BGP_3000",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.ApplyGroupNeighbour.ApplyGroup"));
        assertEquals("AG6_MAX_PREFIX",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.ApplyGroupPeer.ApplyGroup"));
        assertEquals("v6", mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.Family"));
        assertEquals("gp6_21302:811182",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.PeerGroupName"));
        assertEquals("BGP4_PROTOCOL",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.RoutingApplyGroups.RoutingProtocol"));
        assertEquals("ICOREPVC-81114561", mm.get("ApplyGroupResponse.ApplyGroupResponseData.ServiceInstanceId"));
        assertEquals("AG_MAX_MCASTROUTES",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.ApplyGroup.ApplyGroup"));
        assertEquals("VPNL811182", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.End2EndVpnKey"));
        assertEquals("811182", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VpnId"));
        assertEquals("SET6_DSU", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport[0]"));
        assertEquals("SET_DSU", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport[1]"));
        assertEquals("SET6_MANAGED", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport[2]"));
        assertEquals("SET_MANAGED", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport[3]"));
        assertEquals("SET_LOVRF_COMMUNITY",
                mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport[4]"));
        assertEquals("SET_RESET_LP", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport[5]"));
        assertEquals("6", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport_length"));
        assertEquals("SET_BVOIP_IN", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfImport[0]"));
        assertEquals("SET6_BVOIP_IN", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfImport[1]"));
        assertEquals("2", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfImport_length"));
        assertEquals("21302:811182", mm.get("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfName"));
        assertEquals("Y", mm.get("ApplyGroupResponse.ack-final-indicator"));
        assertEquals("200", mm.get("ApplyGroupResponse.response-code"));
        assertEquals("Success", mm.get("ApplyGroupResponse.response-message"));
    }

    @Test(expected = SvcLogicException.class)
    public void testInvalidLength() throws Exception {
        String path = "src/test/resources/invalidlength.xml";
        String content = new String(Files.readAllBytes(Paths.get(path)));
        Set<String> listNameList = new HashSet<String>();
        listNameList.add("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfImport");
        listNameList.add("ApplyGroupResponse.ApplyGroupResponseData.VrfDetails.VrfExport");
        XmlParser.convertToProperties(content, listNameList); // throws an exception because the length in the xml is
                                                              // not a valid number
    }

}
