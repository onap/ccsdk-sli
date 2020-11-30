package org.onap.ccsdk.sli.core.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class JREFileResolverTest {

    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final PropertiesFileResolver resolver = new JREFileResolver("success", JREFileResolverTest.class);
        assertEquals("success", resolver.getSuccessfulResolutionMessage());
    }

}