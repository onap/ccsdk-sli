package org.onap.ccsdk.sli.northbound;

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.status.Status;

public class TestLcmRpcInvocationException {

    @Test(expected = SvcLogicException.class)
    public void testLcmRpcInvocationException() throws SvcLogicException{
        Status status = null;
        CommonHeader commonHeader = null;
        LcmRpcInvocationException exception = new LcmRpcInvocationException(status, commonHeader);
        assert(exception.getStatus() == status);
        assert(exception.getCommonHeader() == commonHeader);
        throw exception;
    }
}
