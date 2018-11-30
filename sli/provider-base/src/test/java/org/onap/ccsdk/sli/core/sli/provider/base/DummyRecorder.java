package org.onap.ccsdk.sli.core.sli.provider.base;

import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;

public class DummyRecorder implements SvcLogicRecorder {

    @Override
    public void record(Map<String, String> parmMap) throws SvcLogicException {
        return;
    }

}
