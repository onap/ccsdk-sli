package org.onap.ccsdk.sli.core.utils.dblib;

import org.onap.ccsdk.sli.core.utils.EnvVarFileResolver;

public class DblibEnvVarFileResolver extends EnvVarFileResolver {

    /**
     * Key for environment variable representing the configuration directory
     */
    private static final String SDNC_CONFIG_DIR_PROP_KEY = "SDNC_CONFIG_DIR";

    public DblibEnvVarFileResolver(final String successMessage) {
        super(successMessage, SDNC_CONFIG_DIR_PROP_KEY);
    }
}
