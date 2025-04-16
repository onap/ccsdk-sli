/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.northbound.asdcapi;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.yang.gen.v1.http.xmlns.onap.org.asdc.license.model._1._0.rev160427.vf.license.model.grouping.VfLicenseModel;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.Artifacts;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.ArtifactsBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.VfLicenseModelUpdate;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.VfLicenseModelUpdateInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.VfLicenseModelUpdateInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.VfLicenseModelUpdateOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.VfLicenseModelUpdateOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.VfLicenseModelVersions;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.VfLicenseModelVersionsBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.artifacts.Artifact;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.artifacts.ArtifactBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.artifacts.ArtifactKey;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.vf.license.model.versions.VfLicenseModelVersion;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev170201.vf.license.model.versions.VfLicenseModelVersionBuilder;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
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
 * Defines a base implementation for your provider. This class extends from a helper class
 * which provides storage for the most commonly used components of the MD-SAL. Additionally the
 * base class provides some basic logging and initialization / clean up methods.
 *
 * To use this, copy and paste (overwrite) the following method into the TestApplicationProviderModule
 * class which is auto generated under src/main/java in this project
 * (created only once during first compilation):
 *
 * <pre>

    @Override
    public java.lang.AutoCloseable createInstance() {

         final asdcApiProvider provider = new asdcApiProvider();
         provider.setDataBroker( getDataBrokerDependency() );
         provider.setNotificationService( getNotificationServiceDependency() );
         provider.setRpcRegistry( getRpcRegistryDependency() );
         provider.initialize();
         return new AutoCloseable() {

            @Override
            public void close() throws Exception {
                //TODO: CLOSE ANY REGISTRATION OBJECTS CREATED USING ABOVE BROKER/NOTIFICATION
                //SERVIE/RPC REGISTRY
                provider.close();
            }
        };
    }


    </pre>
 */
@Singleton
@Component(service = VfLicenseModelUpdate.class, immediate = true)
public class AsdcApiProvider implements AutoCloseable, VfLicenseModelUpdate{

    private static final Logger LOG = LoggerFactory.getLogger(AsdcApiProvider.class);

    private static final String ACTIVE_VERSION = "active";

    private static final String APPLICATION_NAME = "asdcApi";

    private final ExecutorService executor;
    private final DataBroker dataBroker;
	private final EntityOwnershipService ownershipService;
    private final AsdcApiSliClient asdcApiSliClient;

    private final Registration rpcRegistration;

    @Inject
    @Activate
    public AsdcApiProvider(@Reference final DataBroker dataBroker,
        @Reference final EntityOwnershipService ownershipService,
        @Reference final RpcProviderService rpcProviderRegistry) {
        this(dataBroker, ownershipService, rpcProviderRegistry, new AsdcApiSliClient(findSvcLogicService()));
    }

    public AsdcApiProvider(final DataBroker dataBroker,
                           final EntityOwnershipService ownershipService,
                           final RpcProviderService rpcRegistry,
                           final AsdcApiSliClient asdcApiSliClient) {
        LOG.info("Creating provider for {}", APPLICATION_NAME);
        executor = Executors.newFixedThreadPool(1);
        this.dataBroker = dataBroker;
        this.ownershipService = ownershipService;
        this.asdcApiSliClient= asdcApiSliClient;
        rpcRegistration = rpcRegistry.registerRpcImplementations(this);
        initialize();
    }

    public void initialize(){
        LOG.info("Initializing {} for {}", this.getClass().getName(), APPLICATION_NAME);

        createContainers();
    }

    private void createContainers() {

        if (dataBroker != null) {
        final WriteTransaction t = dataBroker.newReadWriteTransaction();

        // Create the vf-model-license-versions and artifacts containers
        t.merge(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(VfLicenseModelVersions.class),
        new VfLicenseModelVersionsBuilder().build());

        t.merge(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Artifacts.class), new ArtifactsBuilder().build());


        try {
            FluentFuture<? extends @NonNull CommitInfo> checkedFuture = t.commit();
            checkedFuture.get();
            LOG.info("Create Containers succeeded!: ");

        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Create Containers Failed: ", e);
        }
        } else {
            LOG.warn("createContainers : cannot find dataBroker to create containers");
        }
    }
    protected void initializeChild() {
        //Override if you have custom initialization intelligence
    }

    @Override
    @PreDestroy
    @Deactivate
    public void close() throws Exception {
        LOG.info( "Closing provider for " + APPLICATION_NAME);
        executor.shutdown();
        rpcRegistration.close();
        LOG.info( "Successfully closed provider for " + APPLICATION_NAME);
    }

    protected boolean artifactVersionExists(String aName, String aVersion) {
        InstanceIdentifier artifactInstanceId =
                InstanceIdentifier.<Artifacts>builder(Artifacts.class)
                .child(Artifact.class, new ArtifactKey(aName, aVersion)).build();
        Optional<Artifact> data = null;
        try(ReadTransaction readTx = dataBroker.newReadOnlyTransaction()) {
            data = (Optional<Artifact>) readTx.read(LogicalDatastoreType.CONFIGURATION, artifactInstanceId).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Caught Exception reading MD-SAL for ["+aName+","+ aVersion+"] " ,e);
            return false;
        }

        return data.isPresent();
    }

    protected void addArtifactVersion(String aName, String aVersion) {


        try {
            ArtifactBuilder aBuilder = new ArtifactBuilder();

            aBuilder.setArtifactName(aName);
            aBuilder.setArtifactVersion(aVersion);

            Artifact artifact = aBuilder.build();

            InstanceIdentifier.Builder<Artifact> aIdBuilder = InstanceIdentifier
                    .<Artifacts> builder(Artifacts.class)
                    .child(Artifact.class, artifact.key());

            InstanceIdentifier<Artifact> path = aIdBuilder.build();

            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();

            tx.merge(LogicalDatastoreType.CONFIGURATION, path,
                    artifact);
            tx.commit().get();
        } catch (Exception e) {
            LOG.error("Caught exception trying to add artifact entry", e);
        }

    }


    private void applyVfLicenseModelUpdate(VfLicenseModelUpdateInput input) {

    String aName = input.getArtifactName();
    String aVersion = input.getArtifactVersion();
    VfLicenseModel vfLicenseModel = input.getVfLicenseModel();


    // Add new version (version = artifact-version)
    try {

        VfLicenseModelVersionBuilder vBuilder = new VfLicenseModelVersionBuilder();
        vBuilder.setArtifactName(aName);
        vBuilder.setArtifactVersion(aVersion);
        vBuilder.setVfLicenseModel(vfLicenseModel);

        VfLicenseModelVersion version = vBuilder.build();

        InstanceIdentifier.Builder<VfLicenseModelVersion> versionIdBuilder = InstanceIdentifier
                .<VfLicenseModelVersions> builder(VfLicenseModelVersions.class)
                .child(VfLicenseModelVersion.class, version.key());

        InstanceIdentifier<VfLicenseModelVersion> path = versionIdBuilder.build();

        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
  tx.merge(LogicalDatastoreType.CONFIGURATION, path,
                version);
        tx.commit().get();
    } catch (Exception e) {
        LOG.error(
                "Caught exception trying to save entry to MD-SAL",
                e);
    }


    // Add "active" version (version = "active")
    try {

        VfLicenseModelVersionBuilder vBuilder = new VfLicenseModelVersionBuilder();
        vBuilder.setArtifactName(aName);
        vBuilder.setArtifactVersion(ACTIVE_VERSION);
        vBuilder.setVfLicenseModel(vfLicenseModel);

        VfLicenseModelVersion version = vBuilder.build();
        InstanceIdentifier.Builder<VfLicenseModelVersion> versionIdBuilder = InstanceIdentifier
                .<VfLicenseModelVersions> builder(VfLicenseModelVersions.class)
                .child(VfLicenseModelVersion.class, version.key());

        InstanceIdentifier<VfLicenseModelVersion> path = versionIdBuilder.build();

        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();

        tx.merge(LogicalDatastoreType.CONFIGURATION, path,
                version);
        tx.commit().get();
    } catch (Exception e) {
        LOG.error(
                "Caught exception trying to save entry to MD-SAL",
                e);
    }

}

@Override
public ListenableFuture<RpcResult<VfLicenseModelUpdateOutput>> invoke(VfLicenseModelUpdateInput input) {
    final String svcOperation = "vf-license-model-update";

    Properties parms = new Properties();

    LOG.info( svcOperation +" called." );

    if(input == null ) {
        LOG.debug("exiting " +svcOperation+ " because of invalid input");
        return null;
    }

    VfLicenseModelUpdateInputBuilder inputBuilder = new VfLicenseModelUpdateInputBuilder(input);

    VfLicenseModelUpdateInput inputVfLic = inputBuilder.build();

    String errorMessage = "Success";
    String errorCode = "200";

    // If this artifact already exists, reject this update
    if (artifactVersionExists(inputVfLic.getArtifactName(), inputVfLic.getArtifactVersion())) {
        errorCode = "409";
        errorMessage = "Artifact version already exists";
    } else {
        // Translate input object into SLI-consumable properties
        LOG.info("Adding INPUT data for "+svcOperation+" input: " + inputVfLic);
        AsdcApiUtil.toProperties(parms, inputVfLic);


        // Call directed graph
        Properties respProps = null;
        try
        {
            if (asdcApiSliClient.hasGraph("ASDC-API", svcOperation , null, "sync"))
            {

                try
                {
                    respProps = asdcApiSliClient.execute("ASDC-API", svcOperation, null, "sync", parms);
                }
                catch (Exception e)
                {
                    LOG.error("Caught exception executing service logic for "+ svcOperation, e);
                }
            } else {
                errorMessage = "No service logic active for ASDC-API: '" + svcOperation + "'";
                errorCode = "503";
            }
        }
        catch (Exception e)
        {
            errorCode = "500";
            errorMessage = e.getMessage();
            LOG.error("Caught exception looking for service logic", e);
        }


        if (respProps != null)
        {
            errorCode = respProps.getProperty("error-code");
            errorMessage = respProps.getProperty("error-message", "");
        }
    }


    if ("200".equals(errorCode)) {
        LOG.info("ASDC update succeeded");

        // Update config tree
        applyVfLicenseModelUpdate(inputVfLic);
        addArtifactVersion(inputVfLic.getArtifactName(), inputVfLic.getArtifactVersion());

    } else {
        LOG.info("ASDC update failed ("+errorCode+" : "+errorMessage);
    }

    // Send response
    VfLicenseModelUpdateOutputBuilder respBuilder = new VfLicenseModelUpdateOutputBuilder();
    respBuilder.setAsdcApiResponseCode(errorCode);
    if (errorMessage != null && errorMessage.length() > 0) {
        respBuilder.setAsdcApiResponseText(errorMessage);
    }

    RpcResult<VfLicenseModelUpdateOutput> rpcResult;


    rpcResult = RpcResultBuilder.<VfLicenseModelUpdateOutput> status(true).withResult(respBuilder.build()).build();



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
