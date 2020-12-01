package org.onap.sdnc.northbound.dataChange; 
 
import static org.junit.Assert.*; 
import static org.mockito.Mockito.*; 
 
import java.util.Properties; 
 
import org.junit.Before; 
import org.junit.Test; 
import org.onap.ccsdk.sli.core.sli.SvcLogicException; 
import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper; 
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService; 
import org.onap.ccsdk.sli.northbound.DataChangeClient; 
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.datachange.rev150519.DataChangeNotificationOutputBuilder; 
 
public class DataChangeClientTest { 
 
	SvcLogicService mockSvcLogicService; 
	String module = "test-module"; 
	String rpc = "test-rpc"; 
	String version = "test-version"; 
	String mode = "test-mode"; 
	Properties localProp = new Properties(); 
 
	@Before 
	public void setUp() throws Exception { 
		mockSvcLogicService = mock(SvcLogicService.class); 
		when(mockSvcLogicService.hasGraph(module, rpc, version, mode)).thenReturn(true); 
	} 
 
	@Test 
	public void testDataChangeClientConstructor() { 
		DataChangeClient dataChangeClient = new DataChangeClient(mockSvcLogicService); 
		assertNotNull(dataChangeClient); 
	} 
 
	@Test 
	public void testHasGraph() throws SvcLogicException { 
		DataChangeClient dataChangeClient = new DataChangeClient(mockSvcLogicService); 
		boolean result = dataChangeClient.hasGraph(module, rpc, version, mode); 
		assertTrue(result); 
	} 
 
	@Test 
	public void testExecuteSvcLogicStatusFailure() throws SvcLogicException { 
		DataChangeNotificationOutputBuilder serviceData = mock(DataChangeNotificationOutputBuilder.class); 
		Properties parms = mock(Properties.class); 
		SvcLogicService svcLogicService = mock(SvcLogicService.class); 
		Properties properties = new Properties(); 
		properties.setProperty("SvcLogic.status", "failure"); 
		when(svcLogicService.execute(module, rpc, version, mode, properties)).thenReturn(properties); 
		DataChangeClient sliClient = new DataChangeClient(svcLogicService); 
		Properties prop = sliClient.execute(module, rpc, version, mode, serviceData, properties); 
		assertTrue(prop != null); 
	} 
}