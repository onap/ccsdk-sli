package org.onap.ccsdk.sli.northbound.uebclient; 
 
import static org.junit.Assert.*; 
 
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before; 
import org.junit.Test;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
 
public class SdncNodeModelTest { 
 
	SdncNodeModel sdncNodeModel; 
 
	@Before 
	public void setUp() throws Exception {
		SdncUebConfiguration config = mock(SdncUebConfiguration.class);
		ISdcCsarHelper isdcCsarHelper = mock(ISdcCsarHelper.class); 
		IEntityDetails entityDetails = mock(IEntityDetails.class); 
		Metadata mockMetadata = mock(Metadata.class);
		DBResourceManager mockDBResourceManager = mock(DBResourceManager.class); 
		
		when(entityDetails.getMetadata()).thenReturn(mockMetadata);
		when(mockMetadata.getValue("customizationUUID")).thenReturn("aaaa-bbbb-cccc-dddd");
		
		sdncNodeModel = new SdncNodeModel(isdcCsarHelper, entityDetails, mockDBResourceManager, config); 
		sdncNodeModel.setServiceUUID("0e8d757f-1c80-40af-85de-31d64f1f5af8"); 
		sdncNodeModel.setEcompGeneratedNaming("hello-world"); 
	} 
 
	@Test 
	public void testGetServiceUUID() { 
		String result = sdncNodeModel.getServiceUUID(); 
		assertNotNull(result != null); 
	} 
 
	@Test 
	public void testGetEcompGeneratedNaming() { 
		String result = sdncNodeModel.getEcompGeneratedNaming(); 
		assertEquals("hello-world", result); 
	} 
 
	@Test 
	public void testGetSqlString() { 
		String result = sdncNodeModel.getSql("TEST-HELLO"); 
		String test = "INSERT into NETWORK_MODEL (service_uuid, customization_uuid, model_yaml, ecomp_generated_naming) values (0e8d757f-1c80-40af-85de-31d64f1f5af8, \"aaaa-bbbb-cccc-dddd\", \"TEST-HELLO\", \"hello-world\");"; 
		assertEquals(test, result); 
	} 
 
	@Test 
	public void testGetVpnBindingsSql() { 
		String result = sdncNodeModel.getVpnBindingsSql(); 
		assertNotNull(result); 
	} 
	
	@Test 
	public void testInsertNetworkModelData() { 
		try {
			sdncNodeModel.insertNetworkModelData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	} 

}
