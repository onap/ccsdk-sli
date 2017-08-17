package org.onap.ccsdk.sli.core.dblib.propertiesfileresolver;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Optional;
import org.junit.Test;

public class DblibDefaultFileResolverTest {

    @Test
    public void resolveFile() throws Exception {
        final DblibPropertiesFileResolver resolver = new DblibDefaultFileResolver("success");
        final Optional<File> file = resolver.resolveFile("doesnotexist.cfg");
        assertFalse(file.isPresent());
    }

    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final DblibPropertiesFileResolver resolver = new DblibDefaultFileResolver("success");
        assertEquals("success", resolver.getSuccessfulResolutionMessage());
    }

}