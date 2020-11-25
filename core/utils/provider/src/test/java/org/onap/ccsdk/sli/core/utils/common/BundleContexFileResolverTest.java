package org.onap.ccsdk.sli.core.utils.common;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;

public class BundleContexFileResolverTest {

    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final PropertiesFileResolver resolver =
                new BundleContextFileResolver("success", BundleContexFileResolverTest.class);
        assertEquals("success", resolver.getSuccessfulResolutionMessage());
    }

}