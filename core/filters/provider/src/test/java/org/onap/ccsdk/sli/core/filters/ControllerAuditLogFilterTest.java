package org.onap.ccsdk.sli.core.filters;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ControllerAuditLogFilterTest {

    @Test
    public void getSimpleSiid() throws Exception {
        ControllerAuditLogFilter filter = new ControllerAuditLogFilter();
        String siid = filter.getServiceInstanceId("/rests/data/Layer3API:services/service-list=100?content=config");
        assertEquals("100", siid);
    }

    @Test
    public void getSimpleComplexSiid() throws Exception {
        ControllerAuditLogFilter filter = new ControllerAuditLogFilter();
        String siid = filter.getServiceInstanceId(
                "/rests/data/Layer3API:services/service-list=1337/service-data/oper-status?content=config");
        assertEquals("1337", siid);
    }

}
