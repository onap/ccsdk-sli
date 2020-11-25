package org.onap.ccsdk.sli.core.utils.dblib;

import org.onap.ccsdk.sli.core.utils.EnvVarFileResolver;

/**
 * Resolve properties file location based on the default directory name.
 *
 * @deprecated
 *    This class has been replaced by generic version of this class
 *    {@link #SdncConfigEnvVarFileResolver} in common package.
 */
@Deprecated
public class DblibEnvVarFileResolver extends EnvVarFileResolver {

    /**
     * Key for environment variable representing the configuration directory
     */
    private static final String SDNC_CONFIG_DIR_PROP_KEY = "SDNC_CONFIG_DIR";

    public DblibEnvVarFileResolver(final String successMessage) {
        super(successMessage, SDNC_CONFIG_DIR_PROP_KEY);
    }
}
