package org.onap.ccsdk.sli.northbound.uebclient; 
 
import static org.junit.Assert.*; 
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.junit.Before; 
import org.junit.Test;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper; 
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
 
public class SdncVFCModelTest { 
 
	SdncVFCModel testSdncVFCModel; 
	IEntityDetails mockEntityDetails = null;
 
	@Before 
	public void setup() { 
		ISdcCsarHelper mockCsarHelper = mock(ISdcCsarHelper.class); 
		mockEntityDetails = mock(IEntityDetails.class); 
 		Metadata mockMetadata = mock(Metadata.class);
		DBResourceManager mockDBResourceManager = mock(DBResourceManager.class); 
		SdncUebConfiguration mockSdncUebConfiguration = mock(SdncUebConfiguration.class);
		
		when(mockEntityDetails.getMetadata()).thenReturn(mockMetadata);
		when(mockMetadata.getValue("customizationUUID")).thenReturn("aaaa-bbbb-cccc-dddd");
		
		Map<String,Map<String,Object>> cpPropertiesMap = new HashMap<String,Map<String,Object>>();
		Map<String,Object> propertiesMap = new HashMap<String,Object>();
		propertiesMap.put("network-role", "test-network-role");
		propertiesMap.put("network-role-tag", "test-network-role-tag");
		
		ArrayList<Map<String, Object>> ipRequirementsList = new ArrayList<Map<String, Object>>();
		Map<String,Object> ip4Prop = new HashMap<String,Object>();
		ip4Prop.put("ip_version", "4");
		ipRequirementsList.add(ip4Prop);
		Map<String,Object> ip6Prop = new HashMap<String,Object>();
		ip4Prop.put("ip_version", "6");
		ipRequirementsList.add(ip4Prop);

		propertiesMap.put("ip_requirements", ipRequirementsList);
		cpPropertiesMap.put("cp-node-1", propertiesMap);
		
		try {
			testSdncVFCModel = new SdncVFCModel(mockCsarHelper, mockEntityDetails, mockDBResourceManager, mockSdncUebConfiguration);
			testSdncVFCModel.setVmType("Test-type"); 
			testSdncVFCModel.setVmCount("5"); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
 
	} 
 
	@Test 
	public void testSdncVFCModelSetGetVmType() { 
		String newVMtype = "new-vm-type";
		testSdncVFCModel.setVmType(newVMtype);
		assertEquals(testSdncVFCModel.getVmType(), "new-vm-type"); 
	} 
 
	@Test 
	public void testSdncVFCModelSetGetVmCount() { 
		String newVMcount = "4";
		testSdncVFCModel.setVmCount(newVMcount);
		assertEquals(testSdncVFCModel.getVmCount(), "4"); 
	} 
	
	@Test 
	public void testInsertVFCModelData() { 
		try {
			testSdncVFCModel.insertVFCModelData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	} 

	@Test 
	public void testInsertVFCtoNetworkRoleMappingData() { 
		try {
			testSdncVFCModel.insertVFCtoNetworkRoleMappingData(mockEntityDetails);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	} 

	@Test 
	public void testIpPropParamsMapContainsSubnetRole() { 
		ArrayList<Map<String, String>>  testList = new ArrayList<Map<String, String>>(); 
		testSdncVFCModel.ipPropParamsMapContainsSubnetRole(testList, "test-subnet-role");
	} 

}
