package org.onap.ccsdk.sli.core.sli;

import static org.junit.Assert.*;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;

public class TestMetricLogger {

    MetricLogger logger;

    @Before
    public void setUp() throws Exception {

        logger = new MetricLogger();
        logger.resetContext();
        logger.logRequest("svcInstance1", "svcName", "svcPartner", "targetEntity", "targetServiceName", "targetVirtualEntity", "hello-world");
        logger.logResponse("200", "200", "SUCCESS");

    }

    @Test
    public final void testGetRequestID() {
        logger.getRequestID();
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
    }

}
