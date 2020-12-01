package org.onap.ccsdk.sli.northbound;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicLoader;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicClassResolver;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicServiceImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.Action;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ActionStatusInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ActionStatusOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.AttachVolumeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.AttachVolumeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.AuditInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.AuditOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.CheckLockInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.CheckLockOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigBackupDeleteInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigBackupDeleteOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigBackupInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigBackupOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigExportInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigExportOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigModifyInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigModifyOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigRestoreInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigRestoreOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigScaleOutInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigScaleOutOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigureInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ConfigureOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.DetachVolumeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.DetachVolumeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.DistributeTrafficInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.DistributeTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.EvacuateInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.EvacuateOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.HealthCheckInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.HealthCheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.LCMService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.LiveUpgradeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.LiveUpgradeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.LockInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.LockOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.MigrateInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.MigrateOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.Payload;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.QueryInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.QueryOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.QuiesceTrafficInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.QuiesceTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.RebootInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.RebootOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.RestartInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.RestartOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ResumeTrafficInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ResumeTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.RollbackInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.RollbackOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.SnapshotInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.SnapshotOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.SoftwareUploadInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.SoftwareUploadOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.StartApplicationInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.StartApplicationOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.StartInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.StartOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.StopApplicationInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.StopApplicationOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.StopInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.StopOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.SyncInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.SyncOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.TerminateInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.TerminateOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.TestInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.TestOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UnlockInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UnlockOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradeBackoutInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradeBackoutOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradeBackupInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradeBackupOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradePostCheckInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradePostCheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradePreCheckInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradePreCheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradeSoftwareInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.UpgradeSoftwareOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.DownloadNESwInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.DownloadNESwOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ActivateNESwInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ActivateNESwOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.ZULU;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.action.identifiers.ActionIdentifiersBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.lcm.rev180329.common.header.CommonHeaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLcmProvider {

	Logger LOG = LoggerFactory.getLogger(LcmProvider.class);
	private LcmProvider provider;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        DataBroker dataBroker = mock(DataBroker.class);
        NotificationPublishService notifyService = mock(NotificationPublishService.class);
        RpcProviderRegistry rpcRegistry = mock(RpcProviderRegistry.class);
        BindingAwareBroker.RpcRegistration<LCMService> rpcRegistration = (BindingAwareBroker.RpcRegistration<LCMService>) mock(BindingAwareBroker.RpcRegistration.class);
        when(rpcRegistry.addRpcImplementation(any(Class.class), any(LCMService.class))).thenReturn(rpcRegistration);


        // Load svclogic.properties and get a SvcLogicStore
        InputStream propStr = TestLcmProvider.class.getResourceAsStream("/svclogic.properties");
        Properties svcprops = new Properties();
        svcprops.load(propStr);

        SvcLogicStore store = SvcLogicStoreFactory.getSvcLogicStore(svcprops);

        assertNotNull(store);

        URL graphUrl = TestLcmProvider.class.getClassLoader().getResource("graphs");

        if (graphUrl == null) {
            fail("Cannot find graphs directory");
        }

        SvcLogicLoader loader = new SvcLogicLoader(graphUrl.getPath(), store);
        loader.loadAndActivate();

        // Create a ServiceLogicService
        SvcLogicServiceImpl svc = new SvcLogicServiceImpl(new SvcLogicPropertiesProviderImpl(),
				new SvcLogicClassResolver());

        // Finally ready to create sliapiProvider
        LcmSliClient client = new LcmSliClient(svc);
        provider = new LcmProvider(dataBroker, notifyService, rpcRegistry, client);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        provider.close();
    }


	@Test
	public void testCheckLock() {
		CheckLockInputBuilder builder = new CheckLockInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.CheckLock);

		try {
			CheckLockOutput results = provider.checkLock(builder.build()).get().getResult();
			LOG.info("CheckLock returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("CheckLock threw exception");
		}

	}

	@Test
	public void testReboot() {
		RebootInputBuilder builder = new RebootInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Reboot);
		builder.setPayload(mock(Payload.class));


		try {
			RebootOutput results = provider.reboot(builder.build()).get().getResult();
			LOG.info("Reboot returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Reboot threw exception");
		}
	}

	@Test
	public void testUpgradeBackup() {
		UpgradeBackupInputBuilder builder = new UpgradeBackupInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.UpgradeBackup);
		builder.setPayload(mock(Payload.class));



		try {
			UpgradeBackupOutput results = provider.upgradeBackup(builder.build()).get().getResult();
			LOG.info("UpgradeBackout returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("CheckLock threw exception");
		}
	}

	@Test
	public void testRollback() {
		RollbackInputBuilder builder = new RollbackInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Rollback);
		builder.setPayload(mock(Payload.class));


		try {
			RollbackOutput results = provider.rollback(builder.build()).get().getResult();
			LOG.info("Rollback returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Rollback threw exception");
		}
	}

	@Test
	public void testSync() {
		SyncInputBuilder builder = new SyncInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Sync);
		builder.setPayload(mock(Payload.class));


		try {
			SyncOutput results = provider.sync(builder.build()).get().getResult();
			LOG.info("Sync returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Sync threw exception");
		}
	}

	@Test
	public void testQuery() {
		QueryInputBuilder builder = new QueryInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Query);


		try {
			QueryOutput results = provider.query(builder.build()).get().getResult();
			LOG.info("Query returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Query threw exception");
		}

	}

	@Test
	public void testConfigExport() {
		ConfigExportInputBuilder builder = new ConfigExportInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ConfigExport);


		try {
			ConfigExportOutput results = provider.configExport(builder.build()).get().getResult();
			LOG.info("ConfigExport returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ConfigExport threw exception");
		}
	}

	@Test
	public void testStopApplication() {

		StopApplicationInputBuilder builder = new StopApplicationInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.StopApplication);
		builder.setPayload(mock(Payload.class));


		try {
			StopApplicationOutput results = provider.stopApplication(builder.build()).get().getResult();
			LOG.info("StopApplication returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("StopApplication threw exception");
		}
	}

	@Test
	public void testSoftwareUpload() {
		SoftwareUploadInputBuilder builder = new SoftwareUploadInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.SoftwareUpload);
		builder.setPayload(mock(Payload.class));


		try {
			SoftwareUploadOutput results = provider.softwareUpload(builder.build()).get().getResult();
			LOG.info("SoftwareUpload returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("SoftwareUpload threw exception");
		}
	}

	@Test
	public void testResumeTraffic() {
		ResumeTrafficInputBuilder builder = new ResumeTrafficInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ResumeTraffic);
		builder.setPayload(mock(Payload.class));


		try {
			ResumeTrafficOutput results = provider.resumeTraffic(builder.build()).get().getResult();
			LOG.info("ResumeTraffic returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ResumeTraffic threw exception");
		}
	}

	@Test
	public void testDistributeTraffic() {
		DistributeTrafficInputBuilder builder = new DistributeTrafficInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.DistributeTraffic);
		builder.setPayload(mock(Payload.class));


		try {
			DistributeTrafficOutput results = provider.distributeTraffic(builder.build()).get().getResult();
			LOG.info("DistributeTraffic returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("DistributeTraffic threw exception");
		}
	}

	@Test
	public void testConfigure() {
		ConfigureInputBuilder builder = new ConfigureInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Configure);
		builder.setPayload(mock(Payload.class));


		try {
			ConfigureOutput results = provider.configure(builder.build()).get().getResult();
			LOG.info("Configure returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Configure threw exception");
		}
	}

	@Test
	public void testActionStatus() {
		ActionStatusInputBuilder builder = new ActionStatusInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ActionStatus);
		builder.setPayload(mock(Payload.class));


		try {
			ActionStatusOutput results = provider.actionStatus(builder.build()).get().getResult();
			LOG.info("ActionStatus returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ActionStatus threw exception");
		}
	}

	@Test
	public void testUpgradePreCheck() {
		UpgradePreCheckInputBuilder builder = new UpgradePreCheckInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.UpgradePreCheck);
		builder.setPayload(mock(Payload.class));


		try {
			UpgradePreCheckOutput results = provider.upgradePreCheck(builder.build()).get().getResult();
			LOG.info("UpgradePreCheck returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("UpgradePreCheck threw exception");
		}
	}

	@Test
	public void testLiveUpgrade() {
		LiveUpgradeInputBuilder builder = new LiveUpgradeInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.LiveUpgrade);
		builder.setPayload(mock(Payload.class));


		try {
			LiveUpgradeOutput results = provider.liveUpgrade(builder.build()).get().getResult();
			LOG.info("LiveUpgrade returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("LiveUpgrade threw exception");
		}
	}

	@Test
	public void testConfigModify() {
		ConfigModifyInputBuilder builder = new ConfigModifyInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ConfigModify);
		builder.setPayload(mock(Payload.class));


		try {
			ConfigModifyOutput results = provider.configModify(builder.build()).get().getResult();
			LOG.info("ConfigModify returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ConfigModify threw exception");
		}
	}

	@Test
	public void testRestart() {
		RestartInputBuilder builder = new RestartInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Restart);
		builder.setPayload(mock(Payload.class));


		try {
			RestartOutput results = provider.restart(builder.build()).get().getResult();
			LOG.info("Restart returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Restart threw exception");
		}
	}

	@Test
	public void testHealthCheck() {
		HealthCheckInputBuilder builder = new HealthCheckInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.HealthCheck);
		builder.setPayload(mock(Payload.class));


		try {
			HealthCheckOutput results = provider.healthCheck(builder.build()).get().getResult();
			LOG.info("HealthCheck returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("HealthCheck threw exception");
		}
	}

	@Test
	public void testLock() {
		LockInputBuilder builder = new LockInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Lock);
		builder.setPayload(mock(Payload.class));


		try {
			LockOutput results = provider.lock(builder.build()).get().getResult();
			LOG.info("Lock returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Lock threw exception");
		}
	}

	@Test
	public void testTerminate() {
		TerminateInputBuilder builder = new TerminateInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Terminate);
		builder.setPayload(mock(Payload.class));


		try {
			TerminateOutput results = provider.terminate(builder.build()).get().getResult();
			LOG.info("Terminate returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Terminate threw exception");
		}
	}

	@Test
	public void testAttachVolume() {
		AttachVolumeInputBuilder builder = new AttachVolumeInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.AttachVolume);
		builder.setPayload(mock(Payload.class));


		try {
			AttachVolumeOutput results = provider.attachVolume(builder.build()).get().getResult();
			LOG.info("AttachVolume returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("AttachVolume threw exception");
		}
	}

	@Test
	public void testMigrate() {
		MigrateInputBuilder builder = new MigrateInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Migrate);
		builder.setPayload(mock(Payload.class));


		try {
			MigrateOutput results = provider.migrate(builder.build()).get().getResult();
			LOG.info("Migrate returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Migrate threw exception");
		}
	}

	@Test
	public void testQuiesceTraffic() {
		QuiesceTrafficInputBuilder builder = new QuiesceTrafficInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.QuiesceTraffic);
		builder.setPayload(mock(Payload.class));


		try {
			QuiesceTrafficOutput results = provider.quiesceTraffic(builder.build()).get().getResult();
			LOG.info("QuiesceTraffic returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("QuiesceTraffic threw exception");
		}
	}

	@Test
	public void testConfigRestore() {
		ConfigRestoreInputBuilder builder = new ConfigRestoreInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ConfigRestore);
		builder.setPayload(mock(Payload.class));


		try {
			ConfigRestoreOutput results = provider.configRestore(builder.build()).get().getResult();
			LOG.info("ConfigRestore returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ConfigRestore threw exception");
		}
	}

	@Test
	public void testUpgradeBackout() {
		UpgradeBackoutInputBuilder builder = new UpgradeBackoutInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.UpgradeBackout);
		builder.setPayload(mock(Payload.class));


		try {
			UpgradeBackoutOutput results = provider.upgradeBackout(builder.build()).get().getResult();
			LOG.info("UpgradeBackout returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("UpgradeBackout threw exception");
		}
	}

	@Test
	public void testEvacuate() {
		EvacuateInputBuilder builder = new EvacuateInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Evacuate);
		builder.setPayload(mock(Payload.class));


		try {
			EvacuateOutput results = provider.evacuate(builder.build()).get().getResult();
			LOG.info("Evacuate returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Evacuate threw exception");
		}
	}

	@Test
	public void testUnlock() {
		UnlockInputBuilder builder = new UnlockInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Unlock);
		builder.setPayload(mock(Payload.class));


		try {
			UnlockOutput results = provider.unlock(builder.build()).get().getResult();
			LOG.info("Unlock returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Unlock threw exception");
		}
	}

	@Test
	public void testConfigBackupDelete() {
		ConfigBackupDeleteInputBuilder builder = new ConfigBackupDeleteInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ConfigBackupDelete);


		try {
			ConfigBackupDeleteOutput results = provider.configBackupDelete(builder.build()).get().getResult();
			LOG.info("ConfigBackupDelete returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ConfigBackupDelete threw exception");
		}
	}

	@Test
	public void testUpgradeSoftware() {
		UpgradeSoftwareInputBuilder builder = new UpgradeSoftwareInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.UpgradeSoftware);
		builder.setPayload(mock(Payload.class));


		try {
			UpgradeSoftwareOutput results = provider.upgradeSoftware(builder.build()).get().getResult();
			LOG.info("UpgradeSoftware returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("UpgradeSoftware threw exception");
		}
	}

	@Test
	public void testDownloadNESw() {
		DownloadNESwInputBuilder builder = new DownloadNESwInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setPnfName("my-pnf");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.DownloadNESw);
		builder.setPayload(mock(Payload.class));


		try {
			DownloadNESwOutput results = provider.downloadNESw(builder.build()).get().getResult();
			LOG.info("DownloadNESw returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("DownloadNESw threw exception");
		}
	}

	@Test
	public void testActivateNESw() {
		ActivateNESwInputBuilder builder = new ActivateNESwInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setPnfName("my-pnf");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ActivateNESw);
		builder.setPayload(mock(Payload.class));


		try {
			ActivateNESwOutput results = provider.activateNESw(builder.build()).get().getResult();
			LOG.info("ActivateNESw returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ActivateNESw threw exception");
		}
	}

	@Test
	public void testStop() {
		StopInputBuilder builder = new StopInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Stop);
		builder.setPayload(mock(Payload.class));


		try {
			StopOutput results = provider.stop(builder.build()).get().getResult();
			LOG.info("Stop returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Stop threw exception");
		}
	}

	@Test
	public void testDetachVolume() {
		DetachVolumeInputBuilder builder = new DetachVolumeInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.DetachVolume);
		builder.setPayload(mock(Payload.class));


		try {
			DetachVolumeOutput results = provider.detachVolume(builder.build()).get().getResult();
			LOG.info("DetachVolume returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("DetachVolume threw exception");
		}
	}

	@Test
	public void testConfigScaleOut() {
		ConfigScaleOutInputBuilder builder = new ConfigScaleOutInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ConfigScaleOut);
		builder.setPayload(mock(Payload.class));


		try {
			ConfigScaleOutOutput results = provider.configScaleOut(builder.build()).get().getResult();
			LOG.info("ConfigScaleOut returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ConfigScaleOut threw exception");
		}
	}

	@Test
	public void testUpgradePostCheck() {
		UpgradePostCheckInputBuilder builder = new UpgradePostCheckInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.UpgradePostCheck);
		builder.setPayload(mock(Payload.class));


		try {
			UpgradePostCheckOutput results = provider.upgradePostCheck(builder.build()).get().getResult();
			LOG.info("UpgradePostCheck returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("UpgradePostCheck threw exception");
		}
	}

	@Test
	public void testTest() {
		TestInputBuilder builder = new TestInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Test);
		builder.setPayload(mock(Payload.class));


		try {
			TestOutput results = provider.test(builder.build()).get().getResult();
			LOG.info("Test returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Test threw exception");
		}
	}

	@Test
	public void testStartApplication() {
		StartApplicationInputBuilder builder = new StartApplicationInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.StartApplication);
		builder.setPayload(mock(Payload.class));


		try {
			StartApplicationOutput results = provider.startApplication(builder.build()).get().getResult();
			LOG.info("StartApplication returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("StartApplication threw exception");
		}
	}

	@Test
	public void testConfigBackup() {
		ConfigBackupInputBuilder builder = new ConfigBackupInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ConfigBackup);
		builder.setPayload(mock(Payload.class));


		try {
			ConfigBackupOutput results = provider.configBackup(builder.build()).get().getResult();
			LOG.info("ConfigBackup returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ConfigBackup threw exception");
		}
	}

	@Test
	public void testRebuild() {
		ConfigBackupInputBuilder builder = new ConfigBackupInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.ConfigBackup);
		builder.setPayload(mock(Payload.class));


		try {
			ConfigBackupOutput results = provider.configBackup(builder.build()).get().getResult();
			LOG.info("ConfigBackup returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("ConfigBackup threw exception");
		}
	}

	@Test
	public void testAudit() {
		AuditInputBuilder builder = new AuditInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Audit);
		builder.setPayload(mock(Payload.class));


		try {
			AuditOutput results = provider.audit(builder.build()).get().getResult();
			LOG.info("Audit returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Audit threw exception");
		}
	}

	@Test
	public void testStart() {
		StartInputBuilder builder = new StartInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Start);
		builder.setPayload(mock(Payload.class));


		try {
			StartOutput results = provider.start(builder.build()).get().getResult();
			LOG.info("Start returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Start threw exception");
		}
	}

	@Test
	public void testSnapshot() {
		SnapshotInputBuilder builder = new SnapshotInputBuilder();

		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
		hdrBuilder.setApiVer("1");
		hdrBuilder.setFlags(null);
		hdrBuilder.setOriginatorId("jUnit");
		hdrBuilder.setRequestId("123");
		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
		builder.setCommonHeader(hdrBuilder.build());

		ActionIdentifiersBuilder aBuilder = new ActionIdentifiersBuilder();
		aBuilder.setServiceInstanceId("SVCID-123");
		aBuilder.setVfModuleId("vf-module-1");
		aBuilder.setVnfcName("my-vnfc");
		aBuilder.setVnfId("123");
		aBuilder.setVserverId("123");
		builder.setActionIdentifiers(aBuilder.build());

		builder.setAction(Action.Snapshot);
		builder.setPayload(mock(Payload.class));


		try {
			SnapshotOutput results = provider.snapshot(builder.build()).get().getResult();
			LOG.info("Snapshot returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
			assert(results.getStatus().getCode() == 400);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Caught exception", e);
			fail("Snapshot threw exception");
		}
	}

}
