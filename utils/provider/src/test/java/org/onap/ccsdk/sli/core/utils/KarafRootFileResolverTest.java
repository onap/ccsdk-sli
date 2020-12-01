package org.onap.ccsdk.sli.core.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class KarafRootFileResolverTest {
    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final PropertiesFileResolver resolver = new KarafRootFileResolver("success", null);
        assertEquals("success", resolver.getSuccessfulResolutionMessage());
    }

}