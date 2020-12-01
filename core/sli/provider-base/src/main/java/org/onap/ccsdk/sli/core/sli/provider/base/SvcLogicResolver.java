package org.onap.ccsdk.sli.core.sli.provider.base;

import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;

public interface SvcLogicResolver {
    
    abstract SvcLogicResource getSvcLogicResource(String resourceName);

    abstract SvcLogicRecorder getSvcLogicRecorder(String recorderName);

    abstract SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName);
    
    abstract SvcLogicAdaptor getSvcLogicAdaptor(String adaptorName);
}
