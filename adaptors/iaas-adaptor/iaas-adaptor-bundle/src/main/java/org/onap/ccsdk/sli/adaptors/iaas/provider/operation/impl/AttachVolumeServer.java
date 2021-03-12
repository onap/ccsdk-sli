/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

import com.att.cdp.exceptions.TimeoutException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.openstack.util.ExceptionMapper;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.VolumeService;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Volume;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import com.woorea.openstack.base.client.OpenStackBaseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.onap.ccsdk.sli.adaptors.iaas.Constants;
import org.onap.ccsdk.sli.adaptors.iaas.Property;
import org.onap.ccsdk.sli.adaptors.iaas.ProviderAdapter;
import org.onap.ccsdk.sli.adaptors.iaas.impl.IdentityURL;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestContext;
import org.onap.ccsdk.sli.adaptors.iaas.impl.RequestFailedException;
import org.onap.ccsdk.sli.adaptors.iaas.impl.VMURL;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.common.enums.Operation;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base.ProviderServerOperation;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.utils.configuration.Configuration;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.core.utils.logging.Msg;

public class AttachVolumeServer extends ProviderServerOperation {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AttachVolumeServer.class);
    private static final Configuration config = ConfigurationFactory.getConfiguration();

    private Server attachVolume(Map<String, String> params, SvcLogicContext ctx) {
        Server server = null;
        RequestContext requestContext = new RequestContext(ctx);
        requestContext.isAlive();
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        String vmUrl = params.get(ProviderAdapter.PROPERTY_INSTANCE_URL);
        String volumeId = params.get(ProviderAdapter.VOLUME_ID);
        String device = params.get(ProviderAdapter.DEVICE);
        if (StringUtils.isBlank(device)) {
            logger.info("Setting device to null");
            device = null;
        }
        VMURL vm = VMURL.parseURL(vmUrl);
        Context context;
        final String volumeStatus = "VOLUME_STATUS";
        final String statusFail = "FAILURE";
        final String statusSuccess = "SUCCESS";
        final String statusNotFound = "CONTEXT_NOT_FOUND";
        String tenantName = "Unknown";// to be used also in case of exception
        try {
            if (validateVM(requestContext, appName, vmUrl, vm)) {
                return null;
            }
            IdentityURL ident = IdentityURL.parseURL(params.get(ProviderAdapter.PROPERTY_IDENTITY_URL));
            String identStr = (ident == null) ? null : ident.toString();
            context = getContext(requestContext, vmUrl, identStr);
            String skipHypervisorCheck = configuration.getProperty(Property.SKIP_HYPERVISOR_CHECK);
            if (skipHypervisorCheck == null && ctx != null) {
                skipHypervisorCheck = ctx.getAttribute(ProviderAdapter.SKIP_HYPERVISOR_CHECK);
            }
            if (context != null) {
                tenantName = context.getTenantName();// this variable also is
                                                        // used in case of
                                                        // exception
                requestContext.reset();
                server = lookupServer(requestContext, context, vm.getServerId());
                logger.debug(Msg.SERVER_FOUND, vmUrl, context.getTenantName(), server.getStatus().toString());
                Context contx = server.getContext();
                if (!"true".equalsIgnoreCase(skipHypervisorCheck)) {
                    // Check of the Hypervisor for the VM Server is UP and reachable
                    checkHypervisor(server);
                }
                ComputeService service = contx.getComputeService();
                if ((volumeId == null || volumeId.isEmpty()) || (device == null || device.isEmpty())) {
                    ctx.setAttribute(volumeStatus, statusFail);
                    doFailure(requestContext, HttpStatus.BAD_REQUEST_400, "Both Device or Volumeid are mandatory");
                }
                VolumeService volumeService = contx.getVolumeService();
                logger.info("collecting volume status for volume -id:" + volumeId);
                //List<Volume> volumes = volumeService.getVolumes();
                Volume volume = new Volume();
                boolean isAttached = false;
                if (validateAttach(service, vm.getServerId(), volumeId, device)) {
                    String msg = "Volume with volume id " + volumeId + " cannot be attached as it already exists";
                    logger.info("Already volumes exists:");
                    ctx.setAttribute(volumeStatus, statusFail);
                    doFailure(requestContext, HttpStatus.METHOD_NOT_ALLOWED_405, msg);
                    isAttached = false;
                } else {
                    volume.setId(volumeId);
                    logger.info("Ready to Attach Volume to the server:");
                    service.attachVolume(server, volume, device);
                    isAttached = true;
                }
                if (isAttached) {
                    if (validateAttach(requestContext, service, vm.getServerId(), volumeId, device)) {
                        ctx.setAttribute(volumeStatus, statusSuccess);
                        doSuccess(requestContext);
                    } else {
                        String msg = "Volume with " + volumeId + " unable  to attach";
                        logger.info("Volume with " + volumeId + " unable to attach");
                        ctx.setAttribute(volumeStatus, statusFail);
                        doFailure(requestContext, HttpStatus.CONFLICT_409, msg);
                    }
                }
                context.close();
            } else {
                ctx.setAttribute(volumeStatus, statusNotFound);
            }
        } catch (ZoneException e) {
            String msg = EELFResourceManager.format(Msg.SERVER_NOT_FOUND, e, vmUrl);
            logger.error(msg);
            ctx.setAttribute(volumeStatus, statusFail);
            doFailure(requestContext, HttpStatus.NOT_FOUND_404, msg);
        } catch (RequestFailedException e) {
            ctx.setAttribute(volumeStatus, statusFail);
            doFailure(requestContext, e.getStatus(), e.getMessage());
        } catch (Exception ex) {
            String msg = EELFResourceManager.format(Msg.SERVER_OPERATION_EXCEPTION, ex, ex.getClass().getSimpleName(),
                    Operation.ATTACHVOLUME_SERVICE.toString(), vmUrl, tenantName);
            ctx.setAttribute(volumeStatus, statusFail);
            try {
                ExceptionMapper.mapException((OpenStackBaseException) ex);
            } catch (ZoneException e1) {
                logger.error(e1.getMessage());
            }
            doFailure(requestContext, HttpStatus.INTERNAL_SERVER_ERROR_500, msg);
        }
        return server;
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        setMDC(Operation.ATTACHVOLUME_SERVICE.toString(), "App-C IaaS Adapter:attachVolume", Constants.ADAPTER_NAME);
        logOperation(Msg.ATTACHINGVOLUME_SERVER, params, context);
        return attachVolume(params, context);
    }

    protected boolean validateAttach(ComputeService ser, String vm, String volumeId, String device)
            throws ZoneException {
        boolean isValid = false;
        Map<String, String> map = ser.getAttachments(vm);
        Iterator<Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> volumes = it.next();
            logger.info("volumes available before attach");
            logger.info("device" + volumes.getKey() + " Values " + volumes.getValue());
            if (StringUtils.isBlank(device)) {
                if (volumes.getValue().equals(volumeId)) {
                    logger.info("Device " + volumes.getKey() + " Volumes " + volumes.getValue());
                    isValid = true;
                }
            } else if (volumes.getKey().equals(device) && (volumes.getValue().equals(volumeId))) {
                logger.info("Device " + volumes.getKey() + " Volumes " + volumes.getValue());
                isValid = true;
            }
        }
        logger.info("AttachVolumeFlag " + isValid);
        return isValid;
    }

    protected boolean validateAttach(RequestContext rc, ComputeService ser, String vm, String volumeId, String device)
            throws ZoneException {
        boolean isValid = false;
        String msg = null;
        config.setProperty(Constants.PROPERTY_RETRY_DELAY, "10");
        config.setProperty(Constants.PROPERTY_RETRY_LIMIT, "30");
        while (rc.attempt()) {
            Map<String, String> map = ser.getAttachments(vm);
            if (map != null && !(map.isEmpty())) {
                Iterator<Entry<String, String>> it = map.entrySet().iterator();
                logger.info("volumes available after attach ");
                while (it.hasNext()) {
                    Map.Entry<String, String> volumes = it.next();
                    logger.info(" devices " + volumes.getKey() + " volumes " + volumes.getValue());
                    if (StringUtils.isBlank(device) && (volumes.getValue().equals(volumeId))) {
                        logger.info("Device " + volumes.getKey() + "Volumes " + volumes.getValue());
                        isValid = true;
                        break;
                    } else if (volumes.getKey().equals(device) && (volumes.getValue().equals(volumeId))) {
                        logger.info("Device " + volumes.getKey() + " Volume " + volumes.getValue());
                        isValid = true;
                        break;
                    }
                }
                if (isValid) {
                    logger.info("AttachVolume " + rc.getAttempts() + " No.of attempts ");
                    break;
                } else {
                    rc.delay();
                }
            }
        }
        if ((rc.getAttempts() == 30) && (!isValid)) {
            msg = EELFResourceManager.format(Msg.CONNECTION_FAILED_RETRY, Long.toString(rc.getRetryDelay()),
                    Integer.toString(rc.getAttempts()), Integer.toString(rc.getRetryLimit()));
            logger.error(msg);
            throw new TimeoutException(msg);
        }
        logger.info("AttachVolume Flag -->" + isValid);
        return isValid;
    }
}
