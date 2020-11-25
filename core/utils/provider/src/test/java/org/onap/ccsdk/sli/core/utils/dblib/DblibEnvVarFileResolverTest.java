package org.onap.ccsdk.sli.core.utils.dblib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.File;
import java.util.Optional;
import org.junit.Test;
import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;

public class DblibEnvVarFileResolverTest {
    @Test
    public void resolveFile() throws Exception {
        final PropertiesFileResolver resolver = new DblibEnvVarFileResolver("success");
        final Optional<File> file = resolver.resolveFile("doesnotexist.cfg");
        assertFalse(file.isPresent());
    }

    @Test
    public void getSuccessfulResolutionMessage() throws Exception {
        final PropertiesFileResolver resolver = new DblibEnvVarFileResolver("success");
        assertEquals("success", resolver.getSuccessfulResolutionMessage());
    }

}