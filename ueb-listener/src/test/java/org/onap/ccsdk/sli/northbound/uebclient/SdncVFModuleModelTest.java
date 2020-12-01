package org.onap.ccsdk.sli.northbound.uebclient; 
 
import static org.junit.Assert.*; 
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper; 
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.ccsdk.sli.northbound.uebclient.SdncVFModel;
 
public class SdncVFModuleModelTest { 
 
	@Test 
	public void testSdncVFModuleModelConstructor() { 
		IEntityDetails mockEntity = mock(IEntityDetails.class); 
		Metadata mockMetadata = mock(Metadata.class);
		ISdcCsarHelper mockCsarHelper = mock(ISdcCsarHelper.class); 
		SdncVFModel mockSdncVFModel = mock(SdncVFModel.class); 
		
		when(mockEntity.getMetadata()).thenReturn(mockMetadata);
		
		SdncVFModuleModel testSdncVFModel = new SdncVFModuleModel(mockCsarHelper, mockEntity, mockSdncVFModel); 
		assertNotNull(testSdncVFModel); 
	} 
 
}
