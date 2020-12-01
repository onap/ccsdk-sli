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
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
 
 public class SdncVFModelTest {
 
	SdncVFModel testSdncVFModel = null;
	 
	@Before 
 	public void setUp() throws Exception {
 		ISdcCsarHelper mockCsarHelper = mock(ISdcCsarHelper.class);
		IEntityDetails mockEntityDetails = mock(IEntityDetails.class); 
 		Metadata mockMetadata = mock(Metadata.class);
 		DBResourceManager mockDBResourceManager = mock(DBResourceManager.class);
		SdncUebConfiguration mockSdncUebConfiguration = mock(SdncUebConfiguration.class);
		
 		Property mockProperty = mock(Property.class);
 		Map<String, Property> mockProperties = new HashMap<String, Property>();
		
		when(mockEntityDetails.getMetadata()).thenReturn(mockMetadata);
		when(mockEntityDetails.getProperties()).thenReturn(mockProperties);
		when(mockMetadata.getValue("customizationUUID")).thenReturn("aaaa-bbbb-cccc-dddd");
		when(mockMetadata.getValue(SdcPropertyNames.PROPERTY_NAME_TYPE)).thenReturn("VF");
		mockProperty.setValue("test-nf-naming-code");
		when(mockProperties.get("nf_naming_code")).thenReturn(mockProperty);		
		try {
			testSdncVFModel = new SdncVFModel(mockCsarHelper,mockEntityDetails,mockDBResourceManager,mockSdncUebConfiguration);
			testSdncVFModel.setServiceUUID("bbbb-cccc-dddd-eeee"); 
			testSdncVFModel.setServiceInvariantUUID("cccc-dddd-eeee-ffff"); 
			testSdncVFModel.setVendor("Cisco"); 
			testSdncVFModel.setVendorModelDescription("Cisco Equipment Model"); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

 		assertNotNull(testSdncVFModel);
 	}
 
	@Test 
	public void testSetGetVendor() {
		String newVendorModel = "new-vendor-model";
		testSdncVFModel.setVendor(newVendorModel); 
		String result = testSdncVFModel.getVendor(); 
		assertEquals(result, newVendorModel); 
	} 
	
	@Test 
	public void testSetGetVendorModelDescription() { 
		String newVendorModelDescription = "new-vendor-model-description";
		testSdncVFModel.setVendorModelDescription(newVendorModelDescription); 
		String result = testSdncVFModel.getVendorModelDescription(); 
		assertEquals(result, newVendorModelDescription); 
	}
	
	@Test 
	public void testSetGetNfNamingCode() { 
		String newNfNamingCode = "new-nf-naming-code";
		testSdncVFModel.setNfNamingCode(newNfNamingCode); 
		String result = testSdncVFModel.getNfNamingCode(); 
		assertEquals(result, newNfNamingCode); 
	}
	
	@Test 
	public void testSetGetServiceUUID() { 
		String newServiceUuid = "cccc-dddd-eeee-ffff";
		testSdncVFModel.setServiceUUID(newServiceUuid);
		String result = testSdncVFModel.getServiceUUID(); 
		assertEquals(newServiceUuid, result); 
	} 
 
	@Test 
	public void testSetGetServiceInvariantUUID() { 
		String newServiceInvariantUuid = "dddd-eeee-ffff-eeee";
		testSdncVFModel.setServiceInvariantUUID(newServiceInvariantUuid);
		String result = testSdncVFModel.getServiceInvariantUUID(); 
		assertEquals(result, newServiceInvariantUuid); 
	} 

	@Test 
	public void testInsertData() { 
		try {
			testSdncVFModel.insertData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
 }
