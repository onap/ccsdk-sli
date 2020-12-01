package org.onap.ccsdk.sli.northbound.asdcapi; 
 
import static org.junit.Assert.*; 
import static org.mockito.Mockito.*; 
 
import java.util.Properties; 
 
import org.junit.Before; 
import org.junit.Test; 
import org.onap.ccsdk.sli.core.sli.SvcLogicException; 
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService; 
 
public class AsdcApiSliClientTest { 
	Properties mockProp; 
	Properties propReturn; 
	AsdcApiSliClient testAsdcApiSliClient; 
 
	@Before 
	public void setup() { 
		SvcLogicService mockSvcLogic = mock(SvcLogicService.class); 
		mockProp = new Properties(); 
		mockProp.setProperty("test-value1", "value1"); 
		propReturn = new Properties(); 
		propReturn.setProperty("SvcLogic.status", "Success"); 
		propReturn.setProperty("Object1", "value1"); 
		try { 
			when(mockSvcLogic.hasGraph("TestModule", "TestRPC", "TestVersion", "TestMode")).thenReturn(true); 
			when(mockSvcLogic.hasGraph("NotExist", "TestRPC", "TestVersion", "TestMode")).thenReturn(false); 
			when(mockSvcLogic.execute("TestModule", "TestRPC", "TestVersion", "TestMode", mockProp)).thenReturn(propReturn); 
		} catch (Exception e) { 
			System.out.println(e); 
		} 
 
		testAsdcApiSliClient = new AsdcApiSliClient(mockSvcLogic); 
	} 
 
	@Test 
	public void testhasGraphGraphExsists() throws SvcLogicException { 
		assertTrue(testAsdcApiSliClient.hasGraph("TestModule", "TestRPC", "TestVersion", "TestMode")); 
	} 
 
	@Test 
	public void testhasGraphnoGraph() throws SvcLogicException { 
		assertFalse(testAsdcApiSliClient.hasGraph("NotExist", "TestRPC", "TestVersion", "TestMode")); 
	} 
 
	@Test 
	public void testExecutewithSvcLogicSuccess() throws SvcLogicException { 
		Properties result = testAsdcApiSliClient.execute("TestModule", "TestRPC", "TestVersion", "TestMode", mockProp); 
		assertEquals(result.getProperty("error-code"), "200"); 
	} 
 
	@Test 
	public void testExecutewithSvcLogicFailure500() throws SvcLogicException { 
		propReturn.setProperty("SvcLogic.status", "failure"); 
		Properties result = testAsdcApiSliClient.execute("TestModule", "TestRPC", "TestVersion", "TestMode", mockProp); 
		assertEquals(result.getProperty("error-code"), "500"); 
	} 
}