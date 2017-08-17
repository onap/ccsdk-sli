package org.onap.ccsdk.sli.core.dblib.propertiesfileresolver;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Optional;
import org.junit.Test;

public class DblibEnvVarFileResolverTest {
    @Test
    public void resolveFile() throws Exception {
        final DblibPropertiesFileResolver resolver = new DblibEnvVarFileResolver("success");
        final Optional<File> file = resolver.resolveFile("doesnotexist.cfg");
        assertFalse(file.isPresent());
    }

    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final DblibPropertiesFileResolver resolver = new DblibEnvVarFileResolver("success");
        assertEquals("success", resolver.getSuccessfulResolutionMessage());
    }

}