/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.sli.northbound;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.datachange.rev150519.DataChangeNotification;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.datachange.rev150519.DataChangeNotificationInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.datachange.rev150519.DataChangeNotificationInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.datachange.rev150519.DataChangeNotificationOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.datachange.rev150519.DataChangeNotificationOutputBuilder;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a base implementation for your provider. This class extends from a helper class
 * which provides storage for the most commonly used components of the MD-SAL. Additionally the
 * base class provides some basic logging and initialization / clean up methods.
 *
 */
@Singleton
@Component(service = DataChangeNotification.class, immediate = true)
public class DataChangeProvider implements AutoCloseable, DataChangeNotification {
    private static final Logger LOG = LoggerFactory.getLogger(DataChangeProvider.class);

    private static final String APPLICATION_NAME = "DataChange";

    private final ExecutorService executor;

    private final DataBroker dataBroker;
    private final EntityOwnershipService ownershipService;
    private final Registration rpcRegistration;
    private final DataChangeClient dataChangeClient;
    
	@Inject
	@Activate
	public DataChangeProvider(@Reference final DataBroker dataBroker,
							  @Reference final EntityOwnershipService ownershipService,
							  @Reference final RpcProviderService rpcRegistry) {
		this(dataBroker, ownershipService, rpcRegistry, new DataChangeClient(findSvcLogicService()));
	}
	

    public DataChangeProvider(final DataBroker dataBroker,
							  final EntityOwnershipService ownershipService,
							  final RpcProviderService rpcRegistry,
							  final DataChangeClient dataChangeClient) {

        LOG.info( "Creating provider for {}", APPLICATION_NAME);
        executor = Executors.newFixedThreadPool(1);
		this.dataBroker = dataBroker;
		this.ownershipService = ownershipService;
		rpcRegistration = rpcRegistry.registerRpcImplementations(this);
		this.dataChangeClient = dataChangeClient;
    }

    protected void initializeChild() {
        //Override if you have custom initialization intelligence
    }

    @Override
    public void close() throws Exception {
        LOG.info( "Closing provider for {}", APPLICATION_NAME);
	    executor.shutdown();
	    rpcRegistration.close();
        LOG.info( "Successfully closed provider for {}", APPLICATION_NAME);
    }

	@Override
	public ListenableFuture<RpcResult<DataChangeNotificationOutput>> invoke(
			DataChangeNotificationInput input) {
		final String svcOperation = "data-change-notification";

		Properties parms = new Properties();
		DataChangeNotificationOutputBuilder serviceDataBuilder = new DataChangeNotificationOutputBuilder();

		LOG.info( svcOperation +" called." );

		if(input == null || input.getAaiEventId() == null) {
			LOG.debug("exiting " +svcOperation+ " because of invalid input");
			serviceDataBuilder.setDataChangeResponseCode("403");
			RpcResult<DataChangeNotificationOutput> rpcResult =
				RpcResultBuilder.<DataChangeNotificationOutput> status(true).withResult(serviceDataBuilder.build()).build();
			return Futures.immediateFuture(rpcResult);
		}

		// add input to parms
		LOG.info("Adding INPUT data for "+svcOperation+" input: " + input);
		DataChangeNotificationInputBuilder inputBuilder = new DataChangeNotificationInputBuilder(input);
		MdsalHelper.toProperties(parms, inputBuilder.build());

		// Call SLI sync method
		try
		{
			if (dataChangeClient.hasGraph(APPLICATION_NAME, svcOperation , null, "sync"))
			{
				try
				{
					dataChangeClient.execute(APPLICATION_NAME, svcOperation, null, "sync", serviceDataBuilder, parms);
				}
				catch (Exception e)
				{
					LOG.error("Caught exception executing service logic for "+ svcOperation, e);
					serviceDataBuilder.setDataChangeResponseCode("500");
				}
			} else {
				LOG.error("No service logic active for DataChange: '" + svcOperation + "'");
				serviceDataBuilder.setDataChangeResponseCode("503");
			}
		}
		catch (Exception e)
		{
			LOG.error("Caught exception looking for service logic", e);
			serviceDataBuilder.setDataChangeResponseCode("500");
		}

		String errorCode = serviceDataBuilder.getDataChangeResponseCode();

		if (!("0".equals(errorCode) || "200".equals(errorCode))) {
			LOG.error("Returned FAILED for "+svcOperation+" error code: '" + errorCode + "'");
		} else {
			LOG.info("Returned SUCCESS for "+svcOperation+" ");
		}

		RpcResult<DataChangeNotificationOutput> rpcResult =
				RpcResultBuilder.<DataChangeNotificationOutput> status(true).withResult(serviceDataBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	private static SvcLogicService findSvcLogicService() {
		BundleContext bctx = FrameworkUtil.getBundle(SvcLogicService.class).getBundleContext();

		SvcLogicService svcLogic = null;

		// Get SvcLogicService reference
		ServiceReference sref = bctx.getServiceReference(SvcLogicService.NAME);
		if (sref != null) {
			svcLogic = (SvcLogicService) bctx.getService(sref);

		} else {
			LOG.warn("Cannot find service reference for " + SvcLogicService.NAME);

		}

		return (svcLogic);
	}
}
