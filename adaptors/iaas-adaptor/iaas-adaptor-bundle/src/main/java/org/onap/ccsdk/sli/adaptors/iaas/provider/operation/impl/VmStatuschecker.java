/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl;

import com.att.cdp.exceptions.ResourceNotFoundException;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.Map;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.logging.Msg;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.IdentityURL;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.adaptors.iaas.impl.VMURL;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Outcome;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class VmStatuschecker extends ProviderServerOperation {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(VmStatuschecker.class);
    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    /**
     * to check the status of the VM
     */
    public Server vmStatuschecker(Map<String, String> params, SvcLogicContext ctx)
            throws SvcLogicException, IllegalArgumentException {
        Server server = null;
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);

        try {
            validateParametersExist(params, ProviderAdapter.PROPERTY_INSTANCE_URL,
                    ProviderAdapter.PROPERTY_PROVIDER_NAME);

            String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);

            VMURL vm = VMURL.parseURL(vmUrl);
            if (validateVM(rc, appName, vmUrl, vm))
                return null;

            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();

            Context context = null;
            String tenantName = "Unknown";//to be used also in case of exception
            try {
                context = getContext(rc, vmUrl, identStr);
                if (context != null) {
                    tenantName = context.getTenantName();//this varaible also is used in case of exception
                    server = lookupServer(rc, context, vm.getServerId());
                    logger.debug(Msg.SERVER_FOUND, vmUrl, tenantName, server.getStatus().toString());

                    String statusvm;
                    switch (server.getStatus()) {
                        case DELETED:
                            statusvm = "deleted";
                            break;

                        case RUNNING:
                            statusvm = "running";
                            break;

                        case ERROR:
                            statusvm = "error";
                            break;

                        case READY:
                            statusvm = "ready";
                            break;

                        case PAUSED:
                            statusvm = "paused";
                            break;

                        case SUSPENDED:
                            statusvm = "suspended";
                            break;

                        case PENDING:
                            statusvm = "pending";
                            break;

                        default:
                            statusvm = "default-unknown state-should never occur";
                            break;
                    }


                    String statusofVM = statusvm;
                    context.close();
                    SvcLogicContext svcLogic = rc.getSvcLogicContext();
                    svcLogic.setStatus(Outcome.SUCCESS.toString());
                    svcLogic.setAttribute("org.openecomp.statusofvm", statusofVM);
                    svcLogic.setAttribute(Constants.STATUS_OF_VM, statusofVM);
                    svcLogic.setAttribute(Constants.ATTRIBUTE_ERROR_CODE,
                            Integer.toString(HttpStatus.OK_200.getStatusCode()));
                }
            } catch (ResourceNotFoundException e) {
                String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
                logger.error(msg);
                doFailure(rc, HttpStatus.NOT_FOUND_404, msg);
            } catch (Exception e1) {
                String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, e1,
                        e1.getClass().getSimpleName(), Operation.RESTART_SERVICE.toString(), vmUrl,
                        tenantName);
                logger.error(msg, e1);
                doFailure(rc, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
            }
        } catch (RequestFailedException e) {
            doFailure(rc, e.getStatus(), e.getMessage());
        }

        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.VMSTATUSCHECK_SERVICE.toString(), "App-C IaaS Adapter:VmStatusCheck", Constants.ADAPTER_NAME);
        logOperation(Msg.CHECKING_SERVER, params, context);
        return vmStatuschecker(params, context);
    }
}
