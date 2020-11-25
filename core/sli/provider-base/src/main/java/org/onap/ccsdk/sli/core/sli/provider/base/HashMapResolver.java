package org.onap.ccsdk.sli.core.sli.provider.base;

import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;

public class HashMapResolver implements SvcLogicResolver {
    Map<String, SvcLogicResource> svcLogicResourceMap = new HashMap<String, SvcLogicResource>();
    Map<String, SvcLogicJavaPlugin> svcLogicJavaPluginMap = new HashMap<String, SvcLogicJavaPlugin>();
    Map<String, SvcLogicAdaptor> adaptorMap = new HashMap<String, SvcLogicAdaptor>();
    Map<String, SvcLogicRecorder> recorderMap = new HashMap<String, SvcLogicRecorder>();

    @Override
    public SvcLogicResource getSvcLogicResource(String resourceName) {
        return svcLogicResourceMap.get(resourceName);
    }

    @Override
    public SvcLogicRecorder getSvcLogicRecorder(String recorderName) {
        return recorderMap.get(recorderName);
    }

    @Override
    public SvcLogicJavaPlugin getSvcLogicJavaPlugin(String pluginName) {
        return svcLogicJavaPluginMap.get(pluginName);
    }

    @Override
    public SvcLogicAdaptor getSvcLogicAdaptor(String adaptorName) {
        return adaptorMap.get(adaptorName);
    }

    public void addSvcLogicAdaptor(String adaptorName, SvcLogicAdaptor adaptor) {
        adaptorMap.put(adaptorName, adaptor);
    }

    public void addSvcLogicRecorder(String recorderName, SvcLogicRecorder recorder) {
        recorderMap.put(recorderName, recorder);
    }

    public void addSvcLogicSvcLogicJavaPlugin(String pluginName, SvcLogicJavaPlugin plugin) {
        svcLogicJavaPluginMap.put(pluginName, plugin);
    }

    public void addSvcLogicResource(String resourceName, SvcLogicResource resource) {
        svcLogicResourceMap.put(resourceName, resource);
    }

}
