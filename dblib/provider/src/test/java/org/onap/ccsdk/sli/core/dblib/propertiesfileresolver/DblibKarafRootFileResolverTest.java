package org.onap.ccsdk.sli.core.dblib.propertiesfileresolver;

import static org.junit.Assert.*;

import org.junit.Test;

public class DblibKarafRootFileResolverTest {
    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final DblibPropertiesFileResolver resolver = new DblibKarafRootFileResolver("success", null);
        assertEquals("success", resolver.getSuccessfulResolutionMessage());
    }

}