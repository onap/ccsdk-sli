package org.onap.ccsdk.sli.core.utils.dblib;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.onap.ccsdk.sli.core.utils.DefaultFileResolver;

public class DblibDefaultFileResolver extends DefaultFileResolver {

    /**
     * Default path to look for the configuration directory
     */
    private static final Path DEFAULT_DBLIB_PROP_DIR = Paths.get("opt", "sdnc", "data", "properties");

    public DblibDefaultFileResolver(final String successMessage) {
        super(successMessage, DEFAULT_DBLIB_PROP_DIR);
    }
}
