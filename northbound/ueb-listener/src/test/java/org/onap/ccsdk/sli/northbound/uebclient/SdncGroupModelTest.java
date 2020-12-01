package org.onap.ccsdk.sli.northbound.uebclient;
 
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
 
 public class SdncGroupModelTest {
 
 	@Test
 	public void testSdncGroupModelConstructor() {
 		ISdcCsarHelper mockCsarHelper = mock(ISdcCsarHelper.class);
 		IEntityDetails group = mock(IEntityDetails.class);
 		IEntityDetails entityDetails = mock(IEntityDetails.class);
 		Metadata mockMetadata = mock(Metadata.class);
		DBResourceManager mockDBResourceManager = mock(DBResourceManager.class);
		SdncUebConfiguration mockSdncUebConfiguration = mock(SdncUebConfiguration.class);
		
		when(entityDetails.getMetadata()).thenReturn(mockMetadata);
		when(group.getMetadata()).thenReturn(mockMetadata);
		
 		SdncGroupModel testSdncGroupModel = null;
		try {
			testSdncGroupModel = new SdncGroupModel(mockCsarHelper,group,entityDetails,mockSdncUebConfiguration,mockDBResourceManager);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		assertNotNull(testSdncGroupModel);
 	}
 
 }
 
