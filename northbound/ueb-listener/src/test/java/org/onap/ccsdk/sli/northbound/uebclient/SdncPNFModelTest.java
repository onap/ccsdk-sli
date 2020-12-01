package org.onap.ccsdk.sli.northbound.uebclient;
 
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.Property;

import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
 
 public class SdncPNFModelTest {
 
	SdncPNFModel testSdncPNFModel = null;
	 
	@Before 
 	public void setUp() throws Exception {
 		ISdcCsarHelper mockCsarHelper = mock(ISdcCsarHelper.class);
  		IEntityDetails mockEntityDetails = mock(IEntityDetails.class);
 		Metadata mockMetadata = mock(Metadata.class);
 		Property mockProperty = mock(Property.class);
 		Map<String, Property> mockProperties = new HashMap<String, Property>();
		DBResourceManager mockDBResourceManager = mock(DBResourceManager.class);
		SdncUebConfiguration mockSdncUebConfiguration = mock(SdncUebConfiguration.class);
		
		when(mockEntityDetails.getMetadata()).thenReturn(mockMetadata);
		when(mockMetadata.getValue("customizationUUID")).thenReturn("aaaa-bbbb-cccc-dddd");
		mockProperty.setValue("test-nf-naming-code");
		when(mockProperties.get("nf_naming_code")).thenReturn(mockProperty);
		
		try {
			testSdncPNFModel = new SdncPNFModel(mockCsarHelper,mockEntityDetails,mockDBResourceManager,mockSdncUebConfiguration);
			testSdncPNFModel.setServiceUUID("bbbb-cccc-dddd-eeee"); 
			testSdncPNFModel.setServiceInvariantUUID("cccc-dddd-eeee-ffff"); 
			testSdncPNFModel.setVendor("Cisco"); 
			testSdncPNFModel.setVendorModelDescription("Cisco Equipment Model"); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

 		assertNotNull(testSdncPNFModel);
 	}
 
	@Test 
	public void testSetGetVendor() {
		String newVendorModel = "new-vendor-model";
		testSdncPNFModel.setVendor(newVendorModel); 
		String result = testSdncPNFModel.getVendor(); 
		assertEquals(result, newVendorModel); 
	} 
	
	@Test 
	public void testSetGetVendorModelDescription() { 
		String newVendorModelDescription = "new-vendor-model-description";
		testSdncPNFModel.setVendorModelDescription(newVendorModelDescription); 
		String result = testSdncPNFModel.getVendorModelDescription(); 
		assertEquals(result, newVendorModelDescription); 
	}
	
	@Test 
	public void testSetGetNfNamingCode() { 
		String newNfNamingCode = "new-nf-naming-code";
		testSdncPNFModel.setNfNamingCode(newNfNamingCode); 
		String result = testSdncPNFModel.getNfNamingCode(); 
		assertEquals(result, newNfNamingCode); 
	}
	
	@Test 
	public void testSetGetServiceUUID() { 
		String newServiceUuid = "cccc-dddd-eeee-ffff";
		testSdncPNFModel.setServiceUUID(newServiceUuid);
		String result = testSdncPNFModel.getServiceUUID(); 
		assertEquals(newServiceUuid, result); 
	} 
 
	@Test 
	public void testSetGetServiceInvariantUUID() { 
		String newServiceInvariantUuid = "dddd-eeee-ffff-eeee";
		testSdncPNFModel.setServiceInvariantUUID(newServiceInvariantUuid);
		String result = testSdncPNFModel.getServiceInvariantUUID(); 
		assertEquals(result, newServiceInvariantUuid); 
	} 

	@Test 
	public void testInsertData() { 
		try {
			testSdncPNFModel.insertData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
 }
