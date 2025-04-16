package org.onap.ccsdk.sli.northbound;
/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * 	Modifications Copyright © 2018 IBM.
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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.*;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.common.header.CommonHeaderBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.status.StatusBuilder;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a base implementation for your provider. This class extends from a
 * helper class which provides storage for the most commonly used components of
 * the MD-SAL. Additionally the base class provides some basic logging and
 * initialization / clean up methods.
 *
 */
@Singleton
@Component(service = ActionStatus.class, immediate = true)
public class LcmProvider implements AutoCloseable, ActionStatus {

	private class CommonLcmFields {
		private StatusBuilder statusBuilder;
		private CommonHeaderBuilder commonHeaderBuilder;
		private Payload payload;

		public CommonLcmFields(StatusBuilder statusBuilder, CommonHeaderBuilder commonHeaderBuilder) {
			this.statusBuilder = statusBuilder;
			this.commonHeaderBuilder = commonHeaderBuilder;
			this.payload = null;
		}

		public CommonLcmFields(StatusBuilder statusBuilder, CommonHeaderBuilder commonHeaderBuilder, Payload payload) {
			this.statusBuilder = statusBuilder;
			this.commonHeaderBuilder = commonHeaderBuilder;
			this.payload = payload;
		}

		public StatusBuilder getStatusBuilder() {
			return statusBuilder;
		}

		public CommonHeaderBuilder getCommonHeaderBuilder() {
			return commonHeaderBuilder;
		}

		public Payload getPayload() {
			return payload;
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(LcmProvider.class);

	private static final String exceptionMessage = "Caught exception";

	private static final String APPLICATION_NAME = "LCM";

	private final ExecutorService executor;
	private final DOMDataBroker domDataBroker;
	private final LcmSliClient lcmSliClient;

	private final Registration rpcRegistration;

	@Inject
	@Activate
	public LcmProvider(@Reference final DataBroker dataProvider,
	                   @Reference final EntityOwnershipService ownershipService,
					   @Reference final DOMDataBroker domDataBroker,
					   @Reference final RpcProviderService rpcProviderRegistry) {
		this(domDataBroker, rpcProviderRegistry, new LcmSliClient(findSvcLogicService()));
	}

	public LcmProvider(final DOMDataBroker dataBroker, 
			final RpcProviderService rpcProviderRegistry, final LcmSliClient lcmSliClient) {

		LOG.info("Creating provider for {}", APPLICATION_NAME);
		this.executor = Executors.newFixedThreadPool(1);
		this.domDataBroker = dataBroker;
		this.lcmSliClient = lcmSliClient;

		rpcRegistration = rpcProviderRegistry.registerRpcImplementations(
			this,
			(ActivateNESw) this::activateNESw,
			(AttachVolume) this::attachVolume,
			(Audit) this::audit,
			(CheckLock) this::checkLock,
			(ConfigBackup) this::configBackup,
			(ConfigBackupDelete) this::configBackupDelete,
			(ConfigExport) this::configExport,
			(ConfigModify) this::configModify,
			(ConfigRestore) this::configRestore,
			(ConfigScaleOut) this::configScaleOut,
			(Configure) this::configure,
			(DetachVolume) this::detachVolume,
			(DistributeTraffic) this::distributeTraffic,
			(DownloadNESw) this::downloadNESw,
			(Evacuate) this::evacuate,
			(HealthCheck) this::healthCheck,
			(LiveUpgrade) this::liveUpgrade,
			(Lock) this::lock,
			(Migrate) this::migrate,
			(Query) this::query,
			(QuiesceTraffic) this::quiesceTraffic,
			(Reboot) this::reboot,
			(Rebuild) this::rebuild,
			(Restart) this::restart,
			(ResumeTraffic) this::resumeTraffic,
			(Rollback) this::rollback,
			(Snapshot) this::snapshot,
			(SoftwareUpload) this::softwareUpload,
			(Start) this::start,
			(StartApplication) this::startApplication,
			(Stop) this::stop,
			(StopApplication) this::stopApplication,
			(Sync) this::sync,
			(Terminate) this::terminate,
			(Test) this::test,
			(Unlock) this::unlock,
			(UpgradeBackout) this::upgradeBackout,
			(UpgradeBackup) this::upgradeBackup,
			(UpgradePostCheck) this::upgradePostCheck,
			(UpgradePreCheck) this::upgradePreCheck,
			(UpgradeSoftware) this::upgradeSoftware
		);
	}

	@Override
	@PreDestroy
	@Deactivate
	public void close() throws Exception {
		LOG.info("Closing provider for " + APPLICATION_NAME);
		executor.shutdown();
		rpcRegistration.close();
		LOG.info("Successfully closed provider for " + APPLICATION_NAME);
	}

	@Override
	public ListenableFuture<RpcResult<ActionStatusOutput>> invoke(ActionStatusInput input) {
		ActionStatusInputBuilder iBuilder = new ActionStatusInputBuilder(input);
		ActionStatusOutputBuilder oBuilder = new ActionStatusOutputBuilder();

		try {
			CommonLcmFields retval = callDG("action-status", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ActionStatusOutput> rpcResult =
				RpcResultBuilder.<ActionStatusOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	public ListenableFuture<RpcResult<CheckLockOutput>> checkLock(CheckLockInput input) {
		CheckLockInputBuilder iBuilder = new CheckLockInputBuilder(input);
		CheckLockOutputBuilder oBuilder = new CheckLockOutputBuilder();

		try {
			CommonLcmFields retval = callDG("check-lock", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<CheckLockOutput> rpcResult =
				RpcResultBuilder.<CheckLockOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}

	public ListenableFuture<RpcResult<RebootOutput>> reboot(RebootInput input) {
		RebootInputBuilder iBuilder = new RebootInputBuilder(input);
		RebootOutputBuilder oBuilder = new RebootOutputBuilder();

		try {
			CommonLcmFields retval = callDG("reboot", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<RebootOutput> rpcResult =
				RpcResultBuilder.<RebootOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}


	public ListenableFuture<RpcResult<UpgradeBackupOutput>> upgradeBackup(UpgradeBackupInput input) {
		UpgradeBackupInputBuilder iBuilder = new UpgradeBackupInputBuilder(input);
		UpgradeBackupOutputBuilder oBuilder = new UpgradeBackupOutputBuilder();

		try {
			CommonLcmFields retval = callDG("upgrade-backup", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());

		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<UpgradeBackupOutput> rpcResult =
				RpcResultBuilder.<UpgradeBackupOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}


	public ListenableFuture<RpcResult<RollbackOutput>> rollback(RollbackInput input) {
		RollbackInputBuilder iBuilder = new RollbackInputBuilder(input);
		RollbackOutputBuilder oBuilder = new RollbackOutputBuilder();

		try {
			CommonLcmFields retval = callDG("rollback", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
			if (retval.getPayload() != null) {
				oBuilder.setPayload(retval.getPayload());
			}
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<RollbackOutput> rpcResult =
				RpcResultBuilder.<RollbackOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}


	public ListenableFuture<RpcResult<SyncOutput>> sync(SyncInput input) {
		SyncInputBuilder iBuilder = new SyncInputBuilder(input);
		SyncOutputBuilder oBuilder = new SyncOutputBuilder();

		try {
			CommonLcmFields retval = callDG("sync", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<SyncOutput> rpcResult =
				RpcResultBuilder.<SyncOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}


	public ListenableFuture<RpcResult<QueryOutput>> query(QueryInput input) {
		QueryInputBuilder iBuilder = new QueryInputBuilder(input);
		QueryOutputBuilder oBuilder = new QueryOutputBuilder();

		try {
			CommonLcmFields retval = callDG("query", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<QueryOutput> rpcResult =
				RpcResultBuilder.<QueryOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}


	public ListenableFuture<RpcResult<ConfigExportOutput>> configExport(ConfigExportInput input) {
		ConfigExportInputBuilder iBuilder = new ConfigExportInputBuilder(input);
		ConfigExportOutputBuilder oBuilder = new ConfigExportOutputBuilder();

		try {
			CommonLcmFields retval = callDG("config-export", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigExportOutput> rpcResult =
				RpcResultBuilder.<ConfigExportOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	public ListenableFuture<RpcResult<StopApplicationOutput>> stopApplication(StopApplicationInput input) {
		StopApplicationInputBuilder iBuilder = new StopApplicationInputBuilder(input);
		StopApplicationOutputBuilder oBuilder = new StopApplicationOutputBuilder();

		try {
			CommonLcmFields retval = callDG("stop-application", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<StopApplicationOutput> rpcResult =
				RpcResultBuilder.<StopApplicationOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}


	public ListenableFuture<RpcResult<SoftwareUploadOutput>> softwareUpload(SoftwareUploadInput input) {
		SoftwareUploadInputBuilder iBuilder = new SoftwareUploadInputBuilder(input);
		SoftwareUploadOutputBuilder oBuilder = new SoftwareUploadOutputBuilder();

		try {
			CommonLcmFields retval = callDG("software-upload", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<SoftwareUploadOutput> rpcResult =
				RpcResultBuilder.<SoftwareUploadOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	public ListenableFuture<RpcResult<ResumeTrafficOutput>> resumeTraffic(ResumeTrafficInput input) {
		ResumeTrafficInputBuilder iBuilder = new ResumeTrafficInputBuilder(input);
		ResumeTrafficOutputBuilder oBuilder = new ResumeTrafficOutputBuilder();

		try {
			CommonLcmFields retval = callDG("resume-traffic", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ResumeTrafficOutput> rpcResult =
				RpcResultBuilder.<ResumeTrafficOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	public ListenableFuture<RpcResult<DistributeTrafficOutput>> distributeTraffic(DistributeTrafficInput input) {
		DistributeTrafficInputBuilder iBuilder = new DistributeTrafficInputBuilder(input);
		DistributeTrafficOutputBuilder oBuilder = new DistributeTrafficOutputBuilder();

		try {
			CommonLcmFields retval = callDG("distribute-traffic", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<DistributeTrafficOutput> rpcResult =
				RpcResultBuilder.<DistributeTrafficOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	public ListenableFuture<RpcResult<ConfigureOutput>> configure(ConfigureInput input) {
		ConfigureInputBuilder iBuilder = new ConfigureInputBuilder(input);
		ConfigureOutputBuilder oBuilder = new ConfigureOutputBuilder();

		try {
			CommonLcmFields retval = callDG("configure", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigureOutput> rpcResult =
				RpcResultBuilder.<ConfigureOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}



	public ListenableFuture<RpcResult<UpgradePreCheckOutput>> upgradePreCheck(UpgradePreCheckInput input) {
		UpgradePreCheckInputBuilder iBuilder = new UpgradePreCheckInputBuilder(input);
		UpgradePreCheckOutputBuilder oBuilder = new UpgradePreCheckOutputBuilder();

		try {
			CommonLcmFields retval = callDG("upgrade-pre-check", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
			if (retval.getPayload() != null) {
				oBuilder.setPayload(retval.getPayload());
			}
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<UpgradePreCheckOutput> rpcResult =
				RpcResultBuilder.<UpgradePreCheckOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	public ListenableFuture<RpcResult<LiveUpgradeOutput>> liveUpgrade(LiveUpgradeInput input) {
		LiveUpgradeInputBuilder iBuilder = new LiveUpgradeInputBuilder(input);
		LiveUpgradeOutputBuilder oBuilder = new LiveUpgradeOutputBuilder();

		try {
			CommonLcmFields retval = callDG("live-upgrade", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<LiveUpgradeOutput> rpcResult =
				RpcResultBuilder.<LiveUpgradeOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<ConfigModifyOutput>> configModify(ConfigModifyInput input) {
		ConfigModifyInputBuilder iBuilder = new ConfigModifyInputBuilder(input);
		ConfigModifyOutputBuilder oBuilder = new ConfigModifyOutputBuilder();

		try {
			CommonLcmFields retval = callDG("config-modify", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigModifyOutput> rpcResult =
				RpcResultBuilder.<ConfigModifyOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<RestartOutput>> restart(RestartInput input) {
		RestartInputBuilder iBuilder = new RestartInputBuilder(input);
		RestartOutputBuilder oBuilder = new RestartOutputBuilder();

		try {
			CommonLcmFields retval = callDG("restart", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<RestartOutput> rpcResult =
				RpcResultBuilder.<RestartOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<HealthCheckOutput>> healthCheck(HealthCheckInput input) {
		HealthCheckInputBuilder iBuilder = new HealthCheckInputBuilder(input);
		HealthCheckOutputBuilder oBuilder = new HealthCheckOutputBuilder();

		try {
			CommonLcmFields retval = callDG("health-check", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<HealthCheckOutput> rpcResult =
				RpcResultBuilder.<HealthCheckOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<LockOutput>> lock(LockInput input) {
		LockInputBuilder iBuilder = new LockInputBuilder(input);
		LockOutputBuilder oBuilder = new LockOutputBuilder();

		try {
			CommonLcmFields retval = callDG("lock", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<LockOutput> rpcResult =
				RpcResultBuilder.<LockOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<TerminateOutput>> terminate(TerminateInput input) {
		TerminateInputBuilder iBuilder = new TerminateInputBuilder(input);
		TerminateOutputBuilder oBuilder = new TerminateOutputBuilder();

		try {
			CommonLcmFields retval = callDG("terminate", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<TerminateOutput> rpcResult =
				RpcResultBuilder.<TerminateOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<AttachVolumeOutput>> attachVolume(AttachVolumeInput input) {
		AttachVolumeInputBuilder iBuilder = new AttachVolumeInputBuilder(input);
		AttachVolumeOutputBuilder oBuilder = new AttachVolumeOutputBuilder();

		try {
			CommonLcmFields retval = callDG("attach-volume", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<AttachVolumeOutput> rpcResult =
				RpcResultBuilder.<AttachVolumeOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<MigrateOutput>> migrate(MigrateInput input) {
		MigrateInputBuilder iBuilder = new MigrateInputBuilder(input);
		MigrateOutputBuilder oBuilder = new MigrateOutputBuilder();

		try {
			CommonLcmFields retval = callDG("migrate", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<MigrateOutput> rpcResult =
				RpcResultBuilder.<MigrateOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<QuiesceTrafficOutput>> quiesceTraffic(QuiesceTrafficInput input) {
		QuiesceTrafficInputBuilder iBuilder = new QuiesceTrafficInputBuilder(input);
		QuiesceTrafficOutputBuilder oBuilder = new QuiesceTrafficOutputBuilder();

		try {
			CommonLcmFields retval = callDG("quiesce-traffic", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<QuiesceTrafficOutput> rpcResult =
				RpcResultBuilder.<QuiesceTrafficOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<ConfigRestoreOutput>> configRestore(ConfigRestoreInput input) {
		ConfigRestoreInputBuilder iBuilder = new ConfigRestoreInputBuilder(input);
		ConfigRestoreOutputBuilder oBuilder = new ConfigRestoreOutputBuilder();

		try {
			CommonLcmFields retval = callDG("config-restore", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigRestoreOutput> rpcResult =
				RpcResultBuilder.<ConfigRestoreOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<UpgradeBackoutOutput>> upgradeBackout(UpgradeBackoutInput input) {
		UpgradeBackoutInputBuilder iBuilder = new UpgradeBackoutInputBuilder(input);
		UpgradeBackoutOutputBuilder oBuilder = new UpgradeBackoutOutputBuilder();

		try {
			CommonLcmFields retval = callDG("upgrade-backout", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<UpgradeBackoutOutput> rpcResult =
				RpcResultBuilder.<UpgradeBackoutOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<EvacuateOutput>> evacuate(EvacuateInput input) {
		EvacuateInputBuilder iBuilder = new EvacuateInputBuilder(input);
		EvacuateOutputBuilder oBuilder = new EvacuateOutputBuilder();

		try {
			CommonLcmFields retval = callDG("evacuate", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<EvacuateOutput> rpcResult =
				RpcResultBuilder.<EvacuateOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<UnlockOutput>> unlock(UnlockInput input) {
		UnlockInputBuilder iBuilder = new UnlockInputBuilder(input);
		UnlockOutputBuilder oBuilder = new UnlockOutputBuilder();

		try {
			CommonLcmFields retval = callDG("unlock", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<UnlockOutput> rpcResult =
				RpcResultBuilder.<UnlockOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<ConfigBackupDeleteOutput>> configBackupDelete(ConfigBackupDeleteInput input) {
		ConfigBackupDeleteInputBuilder iBuilder = new ConfigBackupDeleteInputBuilder(input);
		ConfigBackupDeleteOutputBuilder oBuilder = new ConfigBackupDeleteOutputBuilder();

		try {
			CommonLcmFields retval = callDG("config-backup-delete", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigBackupDeleteOutput> rpcResult =
				RpcResultBuilder.<ConfigBackupDeleteOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<UpgradeSoftwareOutput>> upgradeSoftware(UpgradeSoftwareInput input) {
		UpgradeSoftwareInputBuilder iBuilder = new UpgradeSoftwareInputBuilder(input);
		UpgradeSoftwareOutputBuilder oBuilder = new UpgradeSoftwareOutputBuilder();

		try {
			CommonLcmFields retval = callDG("upgrade-software", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<UpgradeSoftwareOutput> rpcResult =
				RpcResultBuilder.<UpgradeSoftwareOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<DownloadNESwOutput>> downloadNESw(DownloadNESwInput input) {
		DownloadNESwInputBuilder iBuilder = new DownloadNESwInputBuilder(input);
		DownloadNESwOutputBuilder oBuilder = new DownloadNESwOutputBuilder();

		try {
			CommonLcmFields retval = callDG("download-n-e-sw", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
			if (retval.getPayload() != null) {
				oBuilder.setPayload(retval.getPayload());
			}
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<DownloadNESwOutput> rpcResult =
				RpcResultBuilder.<DownloadNESwOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<ActivateNESwOutput>> activateNESw(ActivateNESwInput input) {
		ActivateNESwInputBuilder iBuilder = new ActivateNESwInputBuilder(input);
		ActivateNESwOutputBuilder oBuilder = new ActivateNESwOutputBuilder();

		try {
			CommonLcmFields retval = callDG("activate-n-e-sw", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
			if (retval.getPayload() != null) {
				oBuilder.setPayload(retval.getPayload());
			}
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ActivateNESwOutput> rpcResult =
				RpcResultBuilder.<ActivateNESwOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<StopOutput>> stop(StopInput input) {
		StopInputBuilder iBuilder = new StopInputBuilder(input);
		StopOutputBuilder oBuilder = new StopOutputBuilder();

		try {
			CommonLcmFields retval = callDG("stop", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<StopOutput> rpcResult =
				RpcResultBuilder.<StopOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<DetachVolumeOutput>> detachVolume(DetachVolumeInput input) {
		DetachVolumeInputBuilder iBuilder = new DetachVolumeInputBuilder(input);
		DetachVolumeOutputBuilder oBuilder = new DetachVolumeOutputBuilder();

		try {
			CommonLcmFields retval = callDG("detach-volume", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<DetachVolumeOutput> rpcResult =
				RpcResultBuilder.<DetachVolumeOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<ConfigScaleOutOutput>> configScaleOut(ConfigScaleOutInput input) {
		ConfigScaleOutInputBuilder iBuilder = new ConfigScaleOutInputBuilder(input);
		ConfigScaleOutOutputBuilder oBuilder = new ConfigScaleOutOutputBuilder();

		try {
			CommonLcmFields retval = callDG("config-scale-out", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigScaleOutOutput> rpcResult =
				RpcResultBuilder.<ConfigScaleOutOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<UpgradePostCheckOutput>> upgradePostCheck(UpgradePostCheckInput input) {
		UpgradePostCheckInputBuilder iBuilder = new UpgradePostCheckInputBuilder(input);
		UpgradePostCheckOutputBuilder oBuilder = new UpgradePostCheckOutputBuilder();

		try {
			CommonLcmFields retval = callDG("upgrade-post-check", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
			if (retval.getPayload() != null) {
				oBuilder.setPayload(retval.getPayload());
			}
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<UpgradePostCheckOutput> rpcResult =
				RpcResultBuilder.<UpgradePostCheckOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<TestOutput>> test(TestInput input) {
		TestInputBuilder iBuilder = new TestInputBuilder(input);
		TestOutputBuilder oBuilder = new TestOutputBuilder();

		try {
			CommonLcmFields retval = callDG("test", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<TestOutput> rpcResult =
				RpcResultBuilder.<TestOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<StartApplicationOutput>> startApplication(StartApplicationInput input) {
		StartApplicationInputBuilder iBuilder = new StartApplicationInputBuilder(input);
		StartApplicationOutputBuilder oBuilder = new StartApplicationOutputBuilder();

		try {
			CommonLcmFields retval = callDG("start-application", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<StartApplicationOutput> rpcResult =
				RpcResultBuilder.<StartApplicationOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<ConfigBackupOutput>> configBackup(ConfigBackupInput input) {
		ConfigBackupInputBuilder iBuilder = new ConfigBackupInputBuilder(input);
		ConfigBackupOutputBuilder oBuilder = new ConfigBackupOutputBuilder();

		try {
			CommonLcmFields retval = callDG("config-backup", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigBackupOutput> rpcResult =
				RpcResultBuilder.<ConfigBackupOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<RebuildOutput>> rebuild(RebuildInput input) {
		RebuildInputBuilder iBuilder = new RebuildInputBuilder(input);
		RebuildOutputBuilder oBuilder = new RebuildOutputBuilder();

		try {
			CommonLcmFields retval = callDG("rebuild", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<RebuildOutput> rpcResult =
				RpcResultBuilder.<RebuildOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<AuditOutput>> audit(AuditInput input) {
		AuditInputBuilder iBuilder = new AuditInputBuilder(input);
		AuditOutputBuilder oBuilder = new AuditOutputBuilder();

		try {
			CommonLcmFields retval = callDG("audit", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<AuditOutput> rpcResult =
				RpcResultBuilder.<AuditOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<StartOutput>> start(StartInput input) {
		StartInputBuilder iBuilder = new StartInputBuilder(input);
		StartOutputBuilder oBuilder = new StartOutputBuilder();

		try {
			CommonLcmFields retval = callDG("start", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<StartOutput> rpcResult =
				RpcResultBuilder.<StartOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	
	public ListenableFuture<RpcResult<SnapshotOutput>> snapshot(SnapshotInput input) {
		SnapshotInputBuilder iBuilder = new SnapshotInputBuilder(input);
		SnapshotOutputBuilder oBuilder = new SnapshotOutputBuilder();

		try {
			CommonLcmFields retval = callDG("snapshot", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (LcmRpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<SnapshotOutput> rpcResult =
				RpcResultBuilder.<SnapshotOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);
	}

	private CommonLcmFields callDG(String rpcName, Object input) throws LcmRpcInvocationException {

		StatusBuilder statusBuilder = new StatusBuilder();

		if (input == null) {
			LOG.debug("Rejecting " +rpcName+ " because of invalid input");
			statusBuilder.setCode(Uint16.valueOf(LcmResponseCode.REJECT_INVALID_INPUT.getValue()));
			statusBuilder.setMessage("REJECT - INVALID INPUT.  Missing input");
			CommonHeaderBuilder hBuilder = new CommonHeaderBuilder();
			hBuilder.setApiVer("1");
			hBuilder.setOriginatorId("unknown");
			hBuilder.setRequestId("unset");
			hBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
			throw new LcmRpcInvocationException(statusBuilder.build(), hBuilder.build());
		}

		CommonHeaderBuilder hBuilder = new CommonHeaderBuilder(((CommonHeader)input).getCommonHeader());

		// add input to parms
		LOG.info("Adding INPUT data for "+ rpcName +" input: " + input.toString());
		Properties inputProps = new Properties();
		MdsalHelper.toProperties(inputProps, input);

		Properties respProps = new Properties();

		// Call SLI sync method
		try
		{
			if (lcmSliClient.hasGraph("LCM", rpcName , null, "sync"))
			{
				try
				{
					respProps = lcmSliClient.execute("LCM", rpcName, null, "sync", inputProps, domDataBroker);
				}
				catch (Exception e)
				{
					LOG.error("Caught exception executing service logic for "+ rpcName, e);
					statusBuilder.setCode(Uint16.valueOf(LcmResponseCode.FAILURE_DG_FAILURE.getValue()));
					statusBuilder.setMessage("FAILURE - DG FAILURE ("+e.getMessage()+")");
					throw new LcmRpcInvocationException(statusBuilder.build(), hBuilder.build());
				}
			} else {
				LOG.error("No service logic active for LCM: '" + rpcName + "'");

				statusBuilder.setCode(Uint16.valueOf(LcmResponseCode.REJECT_DG_NOT_FOUND.getValue()));
				statusBuilder.setMessage("FAILURE - DG not found for action "+rpcName);
				throw new LcmRpcInvocationException(statusBuilder.build(), hBuilder.build());
			}
		}
		catch (Exception e)
		{
			LOG.error("Caught exception looking for service logic", e);

			statusBuilder.setCode(Uint16.valueOf(LcmResponseCode.FAILURE_DG_FAILURE.getValue()));
			statusBuilder.setMessage("FAILURE - Unexpected error looking for DG ("+e.getMessage()+")");
			throw new LcmRpcInvocationException(statusBuilder.build(), hBuilder.build());
		}


		StatusBuilder sBuilder = new StatusBuilder();
		MdsalHelper.toBuilder(respProps, sBuilder);
		MdsalHelper.toBuilder(respProps, hBuilder);

		Payload payload = null;
		String payloadValue = respProps.getProperty("payload");
		if (payloadValue != null) {
			payload = new Payload(payloadValue);
		}


		String statusCode = sBuilder.getCode().toString();

		if (!"400".equals(statusCode)) {
			LOG.error("Returned FAILED for "+rpcName+" error code: '" + statusCode + "'");
		} else {
			LOG.info("Returned SUCCESS for "+rpcName+" ");
		}

		return new CommonLcmFields(sBuilder, hBuilder, payload);

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
