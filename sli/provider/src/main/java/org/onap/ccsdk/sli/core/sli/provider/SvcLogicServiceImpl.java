/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
 * ================================================================================
 *  Modifications Copyright (C) 2018 IBM.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.sli.provider;

import java.util.Properties;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicDblibStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.onap.ccsdk.sli.core.sli.provider.base.AbstractSvcLogicNodeExecutor;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicPropertiesProvider;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceImplBase;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class SvcLogicServiceImpl extends SvcLogicServiceImplBase implements SvcLogicService {

    private static final Logger LOG = LoggerFactory.getLogger(SvcLogicServiceImpl.class);
    protected BundleContext bctx = null;

    public SvcLogicServiceImpl(SvcLogicPropertiesProvider resourceProvider) throws SvcLogicException {
        super(null);
        this.resolver = SvcLogicClassResolver.getInstance();
        properties = resourceProvider.getProperties();
        this.store = getStore();
    }

    public SvcLogicServiceImpl(SvcLogicPropertiesProvider resourceProvider, DbLibService dbSvc)
            throws SvcLogicException {
        super(null);
        this.resolver = SvcLogicClassResolver.getInstance();
        properties = resourceProvider.getProperties();
        this.store = new SvcLogicDblibStore(dbSvc);
    }

    public void registerExecutor(ServiceReference sr) {
        String nodeName = (String) sr.getProperty("nodeType");
        if (nodeName != null) {
            AbstractSvcLogicNodeExecutor executor;
            try {
                executor = (AbstractSvcLogicNodeExecutor) bctx.getService(sr);
            } catch (Exception e) {
                LOG.error("Cannot get service executor for {}", nodeName, e);
                return;
            }
            registerExecutor(nodeName, executor);
        }
    }

    public void unregisterExecutor(ServiceReference sr) {
        String nodeName = (String) sr.getProperty("nodeType");

        if (nodeName != null) {
            unregisterExecutor(nodeName);
        }
    }

    @Override
    public Properties execute(String module, String rpc, String version, String mode, Properties props)
            throws SvcLogicException {
        return (execute(module, rpc, version, mode, props, null));
    }

    @Override
    public Properties execute(String module, String rpc, String version, String mode, Properties props,
            DOMDataBroker domDataBroker) throws SvcLogicException {
        SvcLogicGraph graph = store.fetch(module, rpc, version, mode);

        if (graph == null) {
            Properties retProps = new Properties();
            retProps.setProperty("error-code", "401");
            retProps.setProperty("error-message",
                    "No service logic found for [" + module + "," + rpc + "," + version + "," + mode + "]");
            return (retProps);
        }

        SvcLogicContext ctx = new SvcLogicContext(props);
        ctx.setAttribute(CURRENT_GRAPH, graph.toString());
        // To support legacy code we should not stop populating X-ECOMP-RequestID
        ctx.setAttribute("X-ECOMP-RequestID", MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
        ctx.setDomDataBroker(domDataBroker);
        execute(graph, ctx);
        return (ctx.toProperties());
    }

    @Override
    public SvcLogicStore getStore() throws SvcLogicException {
        // Create and initialize SvcLogicStore object - used to access
        // saved service logic.
        if (store != null) {
            return store;
        }

        try {
            store = SvcLogicStoreFactory.getSvcLogicStore(properties);
        } catch (Exception e) {
            throw new ConfigurationException("Could not get service logic store", e);

        }

        try {
            store.init(properties);
        } catch (SvcLogicException e) {
            throw new ConfigurationException("Could not get service logic store", e);
        }

        return store;
    }

}
