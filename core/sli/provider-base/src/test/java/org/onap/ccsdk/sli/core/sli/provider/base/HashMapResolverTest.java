package org.onap.ccsdk.sli.core.sli.provider.base;

import static org.junit.Assert.assertNotNull;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicRecorder;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;

public class HashMapResolverTest {
    @Mock
    SvcLogicResource myResource;

    @Mock
    SvcLogicRecorder myRecorder;

    @Mock
    SvcLogicJavaPlugin myJavaPlugin;

    @Mock
    SvcLogicAdaptor myAdaptor;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void simpleTest() throws Exception {

        HashMapResolver resolver = new HashMapResolver();
        String resourceKey = "simple.resource";
        String recorderKey = "simple.record";
        String pluginKey = "simple.plugin";
        String adaptorKey = "simple.adaptor";

        resolver.addSvcLogicAdaptor(adaptorKey, myAdaptor);
        resolver.addSvcLogicRecorder(recorderKey, myRecorder);
        resolver.addSvcLogicResource(resourceKey, myResource);
        resolver.addSvcLogicSvcLogicJavaPlugin(pluginKey, myJavaPlugin);

        assertNotNull(resolver.getSvcLogicAdaptor(adaptorKey));
        assertNotNull(resolver.getSvcLogicJavaPlugin(pluginKey));
        assertNotNull(resolver.getSvcLogicRecorder(recorderKey));
        assertNotNull(resolver.getSvcLogicResource(resourceKey));


    }
}
