package org.onap.ccsdk.sli.northbound.dmaapclient; 
 
import static org.junit.Assert.*; 
 
 
import java.io.File; 
 
import org.junit.Before; 
import org.junit.Test; 
 
public class SdncFlatJsonDmaapConsumerTest { 
 
	private static final String DMAAP_LISTENER_PROPERTIES = "dmaap-listener.properties"; 
    private static final String DMAAP_LISTENER_PROPERTIES_DIR = "src/test/resources"; 
 
    SdncFlatJsonDmaapConsumer consumer; 
 
	@Before 
	public void setUp() throws Exception { 
		consumer = new SdncFlatJsonDmaapConsumer(); 
	} 
 
	@Test(expected = InvalidMessageException.class) 
	public void testProcessMsgString_NullInvalidMessageException() throws InvalidMessageException { 
		// expected = InvalidMessageException: Null message 
		consumer.processMsg(null); 
	} 
 
	@Test(expected = InvalidMessageException.class) 
	public void testProcessMsgString_UnformatedMessageInvalidMessageException() throws InvalidMessageException { 
		// expected = InvalidMessageException: Cannot parse json object 
		consumer.processMsg("TESTING", null); 
	} 
 
	@Test(expected = InvalidMessageException.class) 
	public void testing()throws InvalidMessageException  { 
		// Expected = InvalidMessageException: Unable to process message - cannot load field mappings 
		String msg = "{\"test\":\"string\"}"; 
		consumer.processMsg(msg, null); 
	} 
}