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
    public final void testGetBeginTimestamp() {
        logger.getBeginTimestamp();
    }

    @Test
    public final void testGetEndTimestamp() {
       logger.getEndTimestamp();
    }

    @Test
    public final void testGetRequestID() {
        logger.getRequestID();
    }

    @Test
    public final void testGetServiceInstanceID() {
        logger.getServiceInstanceID();
    }

    @Test
    public final void testGetServiceName() {
        logger.getServiceName();
    }

    @Test
    public final void testGetPartnerName() {
        logger.getPartnerName();
    }

    @Test
    public final void testGetTargetEntity() {
        logger.getTargetEntity();
    }

    @Test
    public final void testGetTargetServiceName() {
        logger.getTargetServiceName();
    }

    @Test
    public final void testGetStatusCode() {
        logger.getStatusCode();
    }

    @Test
    public final void testGetResponseCode() {
       logger.getResponseCode();
    }

    @Test
    public final void testGetResponseDescription() {
        logger.getResponseDescription();
    }

    @Test
    public final void testGetInstanceUUID() {
        logger.getInstanceUUID();
    }

    @Test
    public final void testGetCategoryLogLevel() {
        logger.getCategoryLogLevel();
    }

    @Test
    public final void testGetSeverity() {
        logger.getSeverity();
    }

    @Test
    public final void testGetServerIpAddress() {
        logger.getServerIpAddress();
    }

    @Test
    public final void testGetElapsedTime() {
        logger.getElapsedTime();
    }

    @Test
    public final void testGetServer() {
        logger.getServer();
    }

    @Test
    public final void testGetClientIp() {
        logger.getClientIp();
    }

    @Test
    public final void testGetClassName() {
        logger.getClassName();
    }

    @Test
    public final void testGetTargetVirtualEntity() {
        logger.getTargetVirtualEntity();
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
