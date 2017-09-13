package org.onap.ccsdk.sli.core.dblib.propertiesfileresolver;

import static org.junit.Assert.*;

import org.junit.Test;

public class DblibJREFileResolverTest {

    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final DblibPropertiesFileResolver resolver = new DblibJREFileResolver("success");
        assertEquals("success", resolver.getSuccessfulResolutionMessage());
    }

}