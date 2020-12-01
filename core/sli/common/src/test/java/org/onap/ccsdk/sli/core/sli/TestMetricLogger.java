package org.onap.ccsdk.sli.core.sli;

import static org.junit.Assert.*;
import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;

public class TestMetricLogger {

    MetricLogger logger;

    @Before
    public void setUp() throws Exception {
        logger = new MetricLogger();
        MetricLogger.resetContext();
    }

    @Test
    public final void testGetRequestID() {
        UUID uuid = UUID.randomUUID();
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, uuid.toString());
        assertEquals(uuid.toString(),logger.getRequestID());
    }
   
    @Test
    public final void testAsIso8601Date() {
        logger.asIso8601(new Date());
    }

    @Test
    public final void testAsIso8601Long() {
        logger.asIso8601(System.currentTimeMillis());
    }

    @Test
    public void formatString() {
        String output = logger.formatString("\n");
        assertEquals("",output);
        output = logger.formatString("|");
        assertEquals("%7C",output);
        output = logger.formatString(null);
        assertEquals(null,output);
        output = logger.formatString("\t");
        assertEquals(" ", output);
        output = logger.formatString("one,two,three,");
        assertEquals("one\\,two\\,three\\,", output);
    }
    
    @Test
    public void generateInvocationId() {
        logger.logRequest("svcInstance1", "svcName", "svcPartner", "targetEntity", "targetServiceName", "targetVirtualEntity", "hello-world");
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.CLIENT_INVOCATION_ID));
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID));
    }
    
    @Test
    public void generateRequestId() {
        logger.logRequest("svcInstance1", "svcName", "svcPartner", "targetEntity", "targetServiceName", "targetVirtualEntity", "hello-world");
        assertNotNull(MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
    }
    
    @Test
    public void overrideInvocationId() {
        String oldUUID = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.CLIENT_INVOCATION_ID, oldUUID);
        MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, oldUUID);

        logger.logRequest("svcInstance1", "svcName", "svcPartner", "targetEntity", "targetServiceName", "targetVirtualEntity", "hello-world");
        String newUUID = MDC.get(ONAPLogConstants.MDCs.CLIENT_INVOCATION_ID);
        assertFalse(oldUUID.equals(newUUID));
        newUUID = MDC.get(ONAPLogConstants.MDCs.INVOCATION_ID);
        assertFalse(oldUUID.equals(newUUID));
    }
    
    @Test
    public void persistRequestId() {
        String oldUUID = UUID.randomUUID().toString();
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, oldUUID);
        assertEquals(oldUUID, MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
    }
}
