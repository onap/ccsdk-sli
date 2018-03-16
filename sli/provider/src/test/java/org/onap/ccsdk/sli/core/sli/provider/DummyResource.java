/**
 *
 */
package org.onap.ccsdk.sli.core.sli.provider;

import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;

/**
 * @author dt5972
 *
 */
public class DummyResource implements SvcLogicResource {

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicResource#isAvailable(java.lang.String, java.lang.String, java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public QueryStatus isAvailable(String resource, String key, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicResource#exists(java.lang.String, java.lang.String, java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public QueryStatus exists(String resource, String key, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicResource#query(java.lang.String, boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix,
            String orderBy, SvcLogicContext ctx) throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicResource#reserve(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public QueryStatus reserve(String resource, String select, String key, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicResource#save(java.lang.String, boolean, boolean, java.lang.String, java.util.Map, java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public QueryStatus save(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms,
            String prefix, SvcLogicContext ctx) throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicResource#release(java.lang.String, java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public QueryStatus release(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {

        return QueryStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicResource#delete(java.lang.String, java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public QueryStatus delete(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicResource#notify(java.lang.String, java.lang.String, java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public QueryStatus notify(String resource, String action, String key, SvcLogicContext ctx)
            throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.SvcLogicResource#update(java.lang.String, java.lang.String, java.util.Map, java.lang.String, org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public QueryStatus update(String resource, String key, Map<String, String> parms, String prefix,
            SvcLogicContext ctx) throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

}
