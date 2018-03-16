/**
 *
 */
package org.onap.ccsdk.sli.core.sli.provider;

import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

/**
 * @author dt5972
 *
 */

public class DummyAdaptor implements SvcLogicAdaptor {

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor#configure(java.lang.String, java.util.Map, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public ConfigStatus configure(String key, Map<String, String> parameters, SvcLogicContext ctx) {

        return ConfigStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor#activate(java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public ConfigStatus activate(String key, SvcLogicContext ctx) {
        return ConfigStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor#deactivate(java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public ConfigStatus deactivate(String key, SvcLogicContext ctx) {
        return ConfigStatus.SUCCESS;
    }

}
