/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.ccsdk.sli.plugins.restconfdiscovery;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicGraph;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Holder to store callback directed graph info.
 */
class SvcLogicGraphInfo {
    private String module;
    private String rpc;
    private String mode;
    private String version;

    /**
     * Creates an instance of SvcLogicGraphInfo.
     *
     * @param module module name of callback DG
     * @param rpc rpc name of callback DG
     * @param mode mode of callback DG
     * @param version version of callback DG
     */
    public SvcLogicGraphInfo(String module, String rpc, String mode, String version) {
        this.module = module;
        this.rpc = rpc;
        this.mode = mode;
        this.version = version;
    }

    public SvcLogicGraphInfo() {}

    /**
     * Returns module name of callback DG.
     *
     * @return module name of callback DG
     */
    public String module() {
        return module;
    }

    /**
     * Sets module of callback DG.
     *
     * @param module module name of the DG
     */
    public void module(String module) {
        this.module = module;
    }

    /**
     * Returns rpc of callback DG.
     *
     * @return rpc of callback DG
     */
    public String rpc() {
        return rpc;
    }

    /**
     * Sets rpc of callback DG.
     *
     * @param rpc rpc attribute of the DG
     */
    public void rpc(String rpc) {
        this.rpc = rpc;
    }

    /**
     * Returns mode of callback DG.
     *
     * @return mode of callback DG
     */
    public String mode() {
        return mode;
    }

    /**
     * Sets mode of DG.
     *
     * @param mode mode of the DG
     */
    public void mode(String mode) {
        this.mode = mode;
    }

    /**
     * Returns version of callback DG.
     *
     * @return version of callback DG
     */
    public String version() {
        return version;
    }

    /**
     * Sets version of DG.
     *
     * @param version version of the DG
     */
    public void version(String version) {
        this.version = version;
    }

    /**
     * Executes call back DG.
     *
     * @param ctx service logic context
     * @throws SvcLogicException service logic error
     */
    public void executeGraph(SvcLogicContext ctx) throws SvcLogicException {
        SvcLogicService service = findSvcLogicService();
        if (service == null) {
            throw new SvcLogicException("\"Could not get SvcLogicService reference\"");
        }

        SvcLogicStore store = service.getStore();
        if (store != null) {
            SvcLogicGraph subGraph = store.fetch(module, rpc, mode, version);
            if (subGraph != null) {
                ctx.setAttribute("subGraph", subGraph.toString());
                service.execute(subGraph, ctx);
            } else {
                throw new SvcLogicException("Failed to call child [" + module +
                                                    "," + rpc + "," + version +
                                                    "," + mode + "] because" +
                                                    " the" + " graph could" +
                                                    " not be found");
            }
        } else {
            throw new SvcLogicException("\"Could not get SvcLogicStore reference\"");
        }
    }

    private static SvcLogicService findSvcLogicService() throws SvcLogicException {
        Bundle bundle = FrameworkUtil.getBundle(SvcLogicService.class);
        if (bundle == null) {
            throw new SvcLogicException("Cannot find bundle reference for "
                                                + SvcLogicService.NAME);
        }

        BundleContext bctx = bundle.getBundleContext();
        ServiceReference<SvcLogicService> sref = bctx.getServiceReference(
                SvcLogicService.class);
        if (sref  != null) {
            return bctx.getService(sref);
        } else {
            throw new SvcLogicException("Cannot find service reference for "
                                                + SvcLogicService.NAME);
        }
    }
}
