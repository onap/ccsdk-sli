package org.onap.ccsdk.sli.northbound;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.status.Status;

public class LcmRpcInvocationException extends SvcLogicException {

	private Status status;
	private CommonHeader commonHeader;

	public LcmRpcInvocationException(Status status, CommonHeader commonHeader) {
		this.status = status;
		this.commonHeader = commonHeader;
	}

	public Status getStatus() {
		return status;
	}

	public CommonHeader getCommonHeader() {
		return commonHeader;
	}

}
