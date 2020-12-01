package org.onap.ccsdk.sli.northbound.asdcapi; 
 
import static org.junit.Assert.*; 
 
import org.junit.Test; 
 
public class AsdcApiUtilTest { 
 
	@Test 
	public void testAsdcApiUtilConstructor() { 
		AsdcApiUtil asdcApiUtilTest = new AsdcApiUtil(); 
		assertNotNull(asdcApiUtilTest); 
	} 
 
}