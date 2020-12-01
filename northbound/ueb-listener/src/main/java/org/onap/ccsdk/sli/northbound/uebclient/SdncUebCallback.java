/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.onap.ccsdk.sli.northbound.uebclient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.XMLConstants;

import org.apache.commons.codec.binary.Base64;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IComponentDoneStatusMessage;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.consumer.INotificationCallback;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.onap.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.utils.ArtifactTypeEnum;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.ccsdk.sli.northbound.uebclient.SdncArtifactMap.SdncArtifactType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class SdncUebCallback implements INotificationCallback {

    private static final Logger LOG = LoggerFactory
            .getLogger(SdncUebCallback.class);

    protected static DBResourceManager jdbcDataSource = null;
	private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
	private static final String COMPONENT_NAME = "SDNC";
	private static final int NUM_PASSES = 2;
		
	// If any ASDC artifact in a distribution fails to download or deploy send SDC event COMPONENT_DONE_ERROR
	// once after the entire distribution is processed.  Otherwise, send COMPONENT_DONE_OK.
	private static boolean COMPONENT_DOWNLOAD_ERROR = false;
	private static boolean COMPONENT_DEPLOY_ERROR = false;
	private static boolean CSAR_ARTIFACT_DEPLOY_ERROR = false;

    private class SdncAuthenticator extends Authenticator {

        private final String user;
        private final String passwd;

        SdncAuthenticator(String user, String passwd) {
            this.user = user;
            this.passwd = passwd;
        }
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, passwd.toCharArray());
        }

    }

    private class DeployableArtifact {
        SdncArtifactType type;
        IArtifactInfo artifactInfo;
        String svcName;
        String resourceName;
        String artifactName;
        String artifactVersion;
        File file;

        public String getArtifactName() {
            return artifactName;
        }



        public String getArtifactVersion() {
            return artifactVersion;
        }


        public SdncArtifactType getType() {
            return type;
        }



        public IArtifactInfo getArtifactInfo() {
            return artifactInfo;
        }


        public File getFile() {
            return file;
        }




        public DeployableArtifact(SdncArtifactType type, String svcName, String resourceName, IArtifactInfo artifactInfo, File file) {
            this.type = type;
            this.artifactInfo = artifactInfo;
			this.svcName = svcName;
			this.resourceName = resourceName;
            this.artifactName = artifactInfo.getArtifactName();
            this.artifactVersion = artifactInfo.getArtifactVersion();
            this.file = file;
        }


        public DeployableArtifact(SdncArtifactType type, String svcName, String resourceName, String artifactName, String artifactVersion, File file) {
            this.type = type;
            this.artifactInfo = null;
			this.svcName = svcName;
			this.resourceName = resourceName;
            this.artifactName = artifactName;
            this.artifactVersion = artifactVersion;
            this.file = file;
        }



        public String getSvcName() {
            return svcName;
        }



        public String getResourceName() {
            return resourceName;
        }

    }

    private final IDistributionClient client;
    protected final SdncUebConfiguration config;

    private LinkedList<DeployableArtifact> deployList[];

    
	private static void setJdbcDataSource() throws IOException {

		String propPath = null;
		String propDir = System.getenv(SDNC_CONFIG_DIR);
		if (propDir == null) {

			propDir = "/opt/sdnc/data/properties";
		}
		propPath = propDir + "/dblib.properties";
		File propFile = new File(propPath);

		if (!propFile.exists()) {

			throw new FileNotFoundException(
					"Missing configuration properties file : "
							+ propFile);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(propFile));

		setJdbcDataSource(new DBResourceManager(props));
	}

	static void setJdbcDataSource(DBResourceManager dbMgr) {

		jdbcDataSource = dbMgr;

		if(jdbcDataSource.isActive()){
			LOG.warn( "DBLIB: JDBC DataSource has been initialized.");
		} else {
			LOG.warn( "DBLIB: JDBC DataSource did not initialize successfully.");
		}
	}

	private static void loadArtifactMap() {

	}

    public SdncUebCallback(IDistributionClient client, SdncUebConfiguration config) {
        this.client = client;
        this.config = config;

		this.deployList = new LinkedList[NUM_PASSES];
		
		for (int i = 0 ; i < NUM_PASSES ; i++) {
			this.deployList[i] = new LinkedList<DeployableArtifact>();
		}
    }

    @Override
	public void activateCallback(INotificationData data) {

        LOG.info("Received notification : ("+data.getDistributionID()+","+data.getServiceName()+","+data.getServiceVersion()+
				","+data.getServiceDescription() +  ")");
        
        COMPONENT_DOWNLOAD_ERROR = false;
        COMPONENT_DEPLOY_ERROR = false;
        CSAR_ARTIFACT_DEPLOY_ERROR = false;
        
    	// TOSCA_TEMPLATE artifact should only be downloaded if TOSCA_CSAR artifact fails due to version non-compliance
    	IArtifactInfo toscaTemplateArtifact = null;

        String incomingDirName = config.getIncomingDir();
        String archiveDirName = config.getArchiveDir();

        File incomingDir = new File(incomingDirName);
        File archiveDir = new File(archiveDirName);

	LOG.debug("IncomingDirName is {}", incomingDirName);

        if (!incomingDir.exists()) {
            incomingDir.mkdirs();
        }


        if (!archiveDir.exists()) {
            archiveDir.mkdirs();
        }

        // Process service level artifacts
        List<IArtifactInfo> artifactList = data.getServiceArtifacts();

        if (artifactList != null) {

            incomingDir = new File(incomingDirName + "/" + escapeFilename(data.getServiceName()));
            if (!incomingDir.exists()) {
                incomingDir.mkdirs();
            }

            archiveDir = new File(archiveDirName + "/" + escapeFilename(data.getServiceName()));
            if (!archiveDir.exists()) {
                archiveDir.mkdirs();
            }
            for (IArtifactInfo curArtifact : artifactList)
            {

                LOG.info("Received artifact " + curArtifact.getArtifactName());
                
        		// If artifact is TOSCA_TEMPLATE, don't handle it.  We will handle if last TOSCA_CSAR ingestion fails.
        		if (curArtifact.getArtifactType().contains("TOSCA_TEMPLATE") || curArtifact.getArtifactName().contains(".yml")) {
        			toscaTemplateArtifact = curArtifact;
        		} else {

        			handleArtifact(data, data.getServiceName(), null, null, curArtifact, incomingDir, archiveDir);
        		}
            }
            
            // After all artifacts have been processed if CSAR_ARTIFACT_DEPLOY_ERROR is true, download and deploy the TOSCA_TEMPLATE artifact
            if (CSAR_ARTIFACT_DEPLOY_ERROR == true) {
            	LOG.info("TOSCA_CSAR artifact deploy error encountered, downloading TOSCA_TEMPLATE artifact: " + toscaTemplateArtifact.getArtifactName());
            	handleArtifact(data, data.getServiceName(), null, null, toscaTemplateArtifact, incomingDir, archiveDir);
            }
        }


        // Process resource level artifacts
        for (IResourceInstance curResource : data.getResources()) {

            LOG.info("Received resource : "+curResource.getResourceName());
            artifactList = curResource.getArtifacts();

            if (artifactList != null) {

                incomingDir = new File(incomingDirName + "/" + escapeFilename(data.getServiceName()) + "/" + escapeFilename(curResource.getResourceName()));
                if (!incomingDir.exists()) {
                    incomingDir.mkdirs();
                }

                archiveDir = new File(archiveDirName + "/" + escapeFilename(data.getServiceName()) + "/" + escapeFilename(curResource.getResourceName()));
                if (!archiveDir.exists()) {
                    archiveDir.mkdirs();
                }
                for (IArtifactInfo curArtifact : artifactList)
                {

                    LOG.info("Received artifact " + curArtifact.getArtifactName());

					handleArtifact(data, data.getServiceName(), curResource.getResourceName(), curResource.getResourceType(), curArtifact, incomingDir, archiveDir);
                }
            }
        }

        deployDownloadedFiles(incomingDir, archiveDir, data);

        // Send Component Status: COMPONENT_DONE_ERROR or COMPONENT_DONE_OK
		LOG.info("Sending Component Status for Distribution: ("+data.getDistributionID()+","+data.getServiceName()+","+data.getServiceVersion()+
			","+data.getServiceDescription() +  ")");
		IDistributionClientResult result = null;
		 if (COMPONENT_DOWNLOAD_ERROR == true || COMPONENT_DEPLOY_ERROR == true) {

			 String errorReason = (COMPONENT_DEPLOY_ERROR == true ? "SDN-C encountered an error deploying an artifact in this distribution" : "");
			 errorReason = (COMPONENT_DOWNLOAD_ERROR == true ? "SDN-C encountered an error downloading an artifact in this distribution" : errorReason);
			 result = client.sendComponentDoneStatus(buildComponentStatusMessage(
						client, data, DistributionStatusEnum.COMPONENT_DONE_ERROR), errorReason);
			 if (result != null) {
			 	LOG.info("Sending Component Status COMPONENT_DONE_ERROR for Distribution result: " + result.getDistributionMessageResult());
			 }
		 } else {
			 
			 result = client.sendComponentDoneStatus(buildComponentStatusMessage(
						client, data, DistributionStatusEnum.COMPONENT_DONE_OK));
			 if (result != null) {
			 	LOG.info("Sending Component Status COMPONENT_DONE_OK for Distribution result: " + result.getDistributionMessageResult());
			 }
		 }
		 
    }


    public void deployDownloadedFiles(File incomingDir, File archiveDir, INotificationData data) {

        if (incomingDir == null) {
	    LOG.debug("incomingDir is null - using {}", config.getIncomingDir());
            incomingDir = new File(config.getIncomingDir());

            if (!incomingDir.exists()) {
                incomingDir.mkdirs();
            }

        } else {
	    LOG.debug("incomingDir is not null - it is {}", incomingDir.getPath());
	}

        if (archiveDir == null) {
            archiveDir = new File(config.getArchiveDir());

            if (!archiveDir.exists()) {
                archiveDir.mkdirs();
            }
        }

        String curFileName = "";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(incomingDir.toPath())) {
            for (Path file: stream) {
                curFileName = file.toString();
            	// Skip TOSCA files (csar and yml) if we are deploying files that were downloaded from ASDC (data is not NULL), 
            	// they have already been deployed.  If they are still in the incoming directory there was an error during ingestion.
            	if (data != null && (curFileName.contains(".csar") || curFileName.contains(".yml"))) {
            		LOG.info("Skipping deploy of file TOSCA file:  "+ curFileName + " it has already been handled");
            		continue;
            	}
            		
                try {
                       handleSuccessfulDownload(null,null, null, null, file.toFile(), archiveDir);
                } catch (Exception x) {
                	COMPONENT_DEPLOY_ERROR = true;
                    LOG.error("Exception in handleSuccessfulDownload: Cannot process spool file "+ curFileName, x);
                }

            }
        } catch (Exception x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            LOG.warn("Cannot process spool file "+ curFileName, x);
        }

        // Deploy scheduled deployments
        /*int numPasses = config.getMaxPasses();

        deployList = new LinkedList[numPasses];

        for (int i = 0 ; i < numPasses ; i++) {
			deployList[i] = new LinkedList<DeployableArtifact>();
        }*/
        for (int pass = 0 ; pass < config.getMaxPasses() ; pass++) {

            if (deployList[pass] != null) {
                while (! deployList[pass].isEmpty()) {
                    DeployableArtifact artifact = deployList[pass].pop();

                    DistributionStatusEnum deployResult = DistributionStatusEnum.DEPLOY_ERROR;


                    try {

                        deployResult = deploySpoolFile(artifact);
                    } catch (Exception e) {
                        LOG.error("Caught exception trying to deploy file", e);
                    }


                    IArtifactInfo artifactInfo = artifact.getArtifactInfo();

					if ((artifactInfo != null) && (data != null)) {
                        IDistributionClientResult deploymentStatus;
                            deploymentStatus = client.sendDeploymentStatus(buildStatusMessage(
                                    client, data, artifactInfo,
                                    deployResult));
                            if (deployResult == DistributionStatusEnum.DEPLOY_ERROR) {
                            	COMPONENT_DEPLOY_ERROR = true;
                            }
                    }

                }
            }
        }
    }

	private void handleArtifact(INotificationData data, String svcName, String resourceName, String resourceType, IArtifactInfo artifact, File incomingDir, File archiveDir) {
		
        // Download Artifact
        IDistributionClientDownloadResult downloadResult = client.download(artifact);

		if (downloadResult == null) {

			handleFailedDownload(data, artifact);
			return;
		}

		byte[] payloadBytes = downloadResult.getArtifactPayload();

		if (payloadBytes == null) {
			handleFailedDownload(data, artifact);
			return;
		}

        boolean writeSucceeded = false;
        File spoolFile = new File(incomingDir.getAbsolutePath() + "/" + artifact.getArtifactName());
        
        // Save zip if TOSCA_CSAR
        if (artifact.getArtifactType().contains("TOSCA_CSAR") || artifact.getArtifactName().contains(".csar")) {

                try(FileOutputStream outFile = new FileOutputStream(incomingDir.getAbsolutePath() + "/" + artifact.getArtifactName())) {
                    outFile.write(payloadBytes, 0, payloadBytes.length);
                    writeSucceeded = true;
                } catch (Exception e) {
                    LOG.error("Unable to save downloaded zip file to spool directory ("+ incomingDir.getAbsolutePath() +")", e);
                }

        } else {

			String payload = new String(payloadBytes);
	
	        try(FileWriter spoolFileWriter = new FileWriter(spoolFile)) {
	            spoolFileWriter.write(payload);
	            writeSucceeded = true;
	        } catch (Exception e) {
	            LOG.error("Unable to save downloaded file to spool directory ("+ incomingDir.getAbsolutePath() +")", e);
	        }
        }


		if (writeSucceeded && (downloadResult.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS)) {
            handleSuccessfulDownload(data, svcName, resourceName, artifact, spoolFile, archiveDir);


        } else {
            handleFailedDownload(data, artifact);
        }

    }

    private void handleFailedDownload(INotificationData data,
            IArtifactInfo relevantArtifact) {
        // Send Download Status
        client.sendDownloadStatus(buildStatusMessage(client, data, relevantArtifact, DistributionStatusEnum.DOWNLOAD_ERROR));
        COMPONENT_DOWNLOAD_ERROR = true;
    }

    private void handleSuccessfulDownload(INotificationData data, String svcName, String resourceName,
            IArtifactInfo artifact, File spoolFile, File archiveDir) {

		if ((data != null) && (artifact != null)) {
            // Send Download Status
            IDistributionClientResult sendDownloadStatus = client
                    .sendDownloadStatus(buildStatusMessage(client, data, artifact, DistributionStatusEnum.DOWNLOAD_OK));
        }

        // If an override file exists, read that instead of the file we just downloaded
        ArtifactTypeEnum artifactEnum = ArtifactTypeEnum.YANG_XML;

        String overrideFileName = config.getOverrideFile();
		if ((overrideFileName != null) && (overrideFileName.length() > 0)) {
            File overrideFile = new File(overrideFileName);

            if (overrideFile.exists()) {
                artifactEnum = ArtifactTypeEnum.YANG_XML;
                spoolFile = overrideFile;
            }

        }

		// If the artifact is a TOSCA artifact, don't schedule a deployment to SDN-C REST intfc, process it in ueb-listener
		if (artifactIsTosca(artifact, spoolFile) == true)
		{
			handleToscaArtifact (data, svcName, resourceName, artifact, spoolFile, archiveDir);
			return;
		}
		
		processSpoolFile (data, svcName, resourceName, artifact, spoolFile, archiveDir);

    }

    protected void processSpoolFile(INotificationData data, String svcName, String resourceName,
            IArtifactInfo artifact, File spoolFile, File archiveDir) {
    	
        // Process spool file
        Document spoolDoc = null;
        File transformedFile = null;

        // Apply XSLTs and get Doc object
        try {
			if (!spoolFile.isDirectory()) {
            transformedFile = applyXslts(spoolFile);
			}
        } catch (Exception e) {
            LOG.error("Caught exception trying to parse XML file", e);
        }

        if (transformedFile != null) {
            try {

                try {

                    DocumentBuilderFactory dbf = DocumentBuilderFactory
                            .newInstance();
                    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    DocumentBuilder db = dbf.newDocumentBuilder();

                    spoolDoc = db.parse(transformedFile);
                } catch (Exception e) {
                    LOG.error(
                            "Caught exception trying to parse transformed XML file "
                                    + transformedFile.getAbsolutePath(), e);
                }

            } catch (Exception e) {
                LOG.error("Caught exception trying to deploy file", e);
            }
        }

        ArtifactTypeEnum artifactEnum = ArtifactTypeEnum.YANG_XML;
        if (spoolDoc != null) {
            // Analyze file type
            SdncArtifactType artifactType = analyzeFileType(artifactEnum,
                    spoolFile, spoolDoc);

            if (artifactType != null) {

                scheduleDeployment(artifactType, svcName, resourceName, artifact, spoolFile.getName(), transformedFile);

            }

            // SDNGC-2660 : Move file to archive directory even if it is an unrecognized type so that
            // we do not keep trying and failing to process it.
            try {
                Path source = spoolFile.toPath();
                Path targetDir = archiveDir.toPath();

                Files.move(source, targetDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.warn("Could not move "+spoolFile.getAbsolutePath()+" to "+archiveDir.getAbsolutePath(), e);
            }
        }
    	
    }

    private void handleToscaArtifact (INotificationData data, String svcName, String resourceName, IArtifactInfo artifact, 
    		File spoolFile, File archiveDir) {
    
    	DistributionStatusEnum deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
		if ((artifact != null && artifact.getArtifactType().contains("TOSCA_TEMPLATE")) || spoolFile.toString().contains(".yml")) {
			deployStatus = processToscaYaml (spoolFile);
		} else if ((artifact != null && artifact.getArtifactType().contains("TOSCA_CSAR")) || spoolFile.toString().contains(".csar")) {
			deployStatus = processToscaCsar (data, svcName, resourceName, artifact, spoolFile, archiveDir);
			// if parser error on CSAR, process the TOSCA_TEMPLATE artifact last
			if (deployStatus.equals(DistributionStatusEnum.DEPLOY_ERROR)) {
				CSAR_ARTIFACT_DEPLOY_ERROR = true;
			}
			
		} else {
			LOG.error("handleToscaArtifact: Encountered unknown TOSCA artifact");
		}
		
		if (deployStatus.equals(DistributionStatusEnum.DEPLOY_OK)) {
			LOG.info("Update to SDN-C succeeded");
			
			try {
				Path source = spoolFile.toPath();
				Path targetDir = archiveDir.toPath();

				Files.move(source, targetDir.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LOG.warn("Could not move "+spoolFile.getAbsolutePath()+" to "+archiveDir.getAbsolutePath(), e);
			}
			
		} else {
			LOG.info("Update to SDN-C failed");
			COMPONENT_DEPLOY_ERROR = true; 
		}
		
		// Send deployment status for ingestion 
		if ((artifact != null) && (data != null)) {
			client.sendDeploymentStatus(buildStatusMessage(client, data, artifact,deployStatus));
		}
    }
    
	protected DistributionStatusEnum processToscaYaml(File spoolFile) {

		return DistributionStatusEnum.DEPLOY_OK;
	}


	private DistributionStatusEnum processToscaCsar(INotificationData data, String svcName, String resourceName, IArtifactInfo artifact, 
    		File spoolFile, File archiveDir) {	
		
		// Use ASDC Dist Client 1.1.5 with TOSCA parsing APIs to extract relevant TOSCA model data

		// TOSCA data extraction flow 1707:
		// Use ASDC dist-client to get yaml string - not yet available
		String model_yaml = null;
		LOG.info("Process TOSCA CSAR file: "+spoolFile.toString());
		ISdcCsarHelper sdcCsarHelper = null;
		DistributionStatusEnum deployStatus = DistributionStatusEnum.DEPLOY_OK;

		try {
			SdcToscaParserFactory factory = SdcToscaParserFactory.getInstance();
			sdcCsarHelper = factory.getSdcCsarHelper(spoolFile.getAbsolutePath());
		} catch (SdcToscaParserException e) {
			LOG.error("Could not create SDC TOSCA Parser ", e);
			return DistributionStatusEnum.DEPLOY_ERROR;
		} 

		// Ingest Service Data - 1707
		Metadata serviceMetadata = sdcCsarHelper.getServiceMetadata();
		SdncServiceModel serviceModel = new SdncServiceModel(sdcCsarHelper, serviceMetadata, jdbcDataSource);
		serviceModel.setFilename(spoolFile.toString().substring(spoolFile.toString().lastIndexOf("/")+1));  // will be csar file name
		serviceModel.setServiceInstanceNamePrefix(SdncBaseModel.extractSubstitutionMappingTypeName(sdcCsarHelper).substring(SdncBaseModel.extractSubstitutionMappingTypeName(sdcCsarHelper).lastIndexOf(".")+1));

		try {
			cleanUpExistingToscaServiceData(serviceModel.getServiceUUID());
			LOG.info("Call insertToscaData for SERVICE_MODEL where service_uuid = " + serviceModel.getServiceUUID());
			insertToscaData(serviceModel.getSql(model_yaml), null);
		} catch (IOException e) {
			LOG.error("Could not insert Tosca CSAR data into the SERVICE_MODEL table ", e);
			return DistributionStatusEnum.DEPLOY_ERROR;
			
		}
		
		// Ingest Network (VL) Data - 1707 / migrate to getEntity - 1908
		EntityQuery vlEntityQuery = EntityQuery.newBuilder(SdcTypes.VL).build();
		TopologyTemplateQuery vlTopologyTemplateQuery = TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE).build();
		List<IEntityDetails> vlEntities = sdcCsarHelper.getEntity(vlEntityQuery, vlTopologyTemplateQuery, false);  // false to not recurse
		if (vlEntities != null && !vlEntities.isEmpty()) {
			for (IEntityDetails vlEntity : vlEntities){

				try {
					SdncNodeModel nodeModel = new SdncNodeModel (sdcCsarHelper, vlEntity, jdbcDataSource, config);
					nodeModel.setServiceUUID(serviceModel.getServiceUUID());

					nodeModel.insertNetworkModelData();
					nodeModel.insertRelatedNetworkRoleData();
				} catch (IOException e) {
					deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
				}
			}
		}
		
		// Ingest Allotted Resource Data - 1707 / migrate to getEntity - 1908
		// Use getEntity to get all VFs in the service filter by metadata Category = Allotted Resource 
		EntityQuery vfEntityQuery = EntityQuery.newBuilder(SdcTypes.VF).build();
		TopologyTemplateQuery vfTopologyTemplateQuery = TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE).build();
		List<IEntityDetails> vfEntities = sdcCsarHelper.getEntity(vfEntityQuery, vfTopologyTemplateQuery, true);
		if (vfEntities != null) {
			for (IEntityDetails vfEntity : vfEntities){

				// If this VF has metadata Category: Allotted Resource, insert it into ALLOTTED_RESOURCE_MODEL table
				String vfCategory = SdncBaseModel.extractValue(sdcCsarHelper, vfEntity.getMetadata(), "category");
				if (vfCategory.contains("Allotted Resource")) {
					
					try {
						SdncARModel arModel = new SdncARModel (sdcCsarHelper, vfEntity, jdbcDataSource, config);
						arModel.insertAllottedResourceModelData ();
						arModel.insertAllottedResourceVfcModelData();
					} catch (IOException e) {
						deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
					}		
				}		
			}
		}

		// Ingest Network (VF) Data - 1707 / migrate to getEntity - 1911
		// Use getEntity to get all VFs in the service 	
		if (vfEntities != null) {
			for (IEntityDetails vfEntity : vfEntities){
			
				SdncVFModel vfNodeModel = null;
				try {
					vfNodeModel = new SdncVFModel (sdcCsarHelper, vfEntity, jdbcDataSource, config);
					vfNodeModel.setServiceUUID(serviceModel.getServiceUUID());
					vfNodeModel.setServiceInvariantUUID(serviceModel.getServiceInvariantUUID());
					vfNodeModel.insertData();
	
				} catch (IOException e) {
					deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
				}			
				
				// For each VF, insert VNF Configuration data
				DistributionStatusEnum vnfConfigDeployStatus = customProcessVnfConfig(sdcCsarHelper, vfNodeModel, jdbcDataSource);
				if (vnfConfigDeployStatus == DistributionStatusEnum.DEPLOY_ERROR) {
					deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
				}
	
			} // VF loop
		}
		

		// Ingest Network (PNF) Data - Dublin/1906
		EntityQuery entityQuery = EntityQuery.newBuilder(SdcTypes.PNF).build();
		TopologyTemplateQuery topologyTemplateQuery = TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE).build();
 
		List<IEntityDetails> pnfs = sdcCsarHelper.getEntity(entityQuery, topologyTemplateQuery, false);
		if (!pnfs.isEmpty()) {
			
			for (IEntityDetails pnf :  pnfs) {
				
				try {
					SdncPNFModel pnfModel = new SdncPNFModel(sdcCsarHelper, pnf, jdbcDataSource, config);
					pnfModel.setServiceUUID(serviceModel.getServiceUUID());
					pnfModel.setServiceInvariantUUID(serviceModel.getServiceInvariantUUID());
					pnfModel.insertData();
					
				} catch (IOException e) {
					deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
				}	
			} // PNF loop	
		}
		
		DistributionStatusEnum complexToscaDeployStatus = customProcessComplexTosca(sdcCsarHelper, config, jdbcDataSource, serviceModel,
				data, svcName, resourceName, artifact, archiveDir);
		if (complexToscaDeployStatus == DistributionStatusEnum.DEPLOY_ERROR) {
			deployStatus = DistributionStatusEnum.DEPLOY_ERROR;
		}
				
		return deployStatus;
	}

	 protected DistributionStatusEnum customProcessVnfConfig(ISdcCsarHelper sdcCsarHelper,
			SdncVFModel vfNodeModel, DBResourceManager jdbcDataSource2) {
		return DistributionStatusEnum.DEPLOY_OK;
	}
	 
	 protected DistributionStatusEnum customProcessComplexTosca(ISdcCsarHelper sdcCsarHelper,
			 SdncUebConfiguration config, DBResourceManager jdbcDataSource2, SdncServiceModel serviceModelINotification,
			 INotificationData data, String svcName, String resourceName, IArtifactInfo artifact, File archiveDir) {
			return DistributionStatusEnum.DEPLOY_OK;
	}	 

	protected void cleanUpExistingToscaData(String tableName, String keyName, String keyValue) throws IOException
     {

            if (jdbcDataSource == null) {
            	 setJdbcDataSource();
            }
             try {
            	int rowCount = 0;
            	CachedRowSet data = jdbcDataSource.getData("SELECT * from " + tableName + " where " + keyName + " = " + keyValue + ";", null, "");
            	while(data.next()) {
     				rowCount ++;
            	}
            	if (rowCount != 0) {
                    LOG.info("cleanUpExistingToscaData: " + keyValue);
               		jdbcDataSource.writeData("DELETE from " + tableName + " where " + keyName + " = " + keyValue + ";", null, null);
            	}

			} catch (SQLException e) {
				LOG.error("Could not clean up existing " + tableName  + " for " + keyValue, e);
			}

     }


	 protected void cleanUpExistingToscaServiceData(String serviceUUID) throws IOException
     {

            if (jdbcDataSource == null) {
            	 setJdbcDataSource();
            }
             try {
            	int rowCount = 0;
            	CachedRowSet data = jdbcDataSource.getData("SELECT * from SERVICE_MODEL where service_uuid = " + serviceUUID + ";", null, "");
            	while(data.next()) {
     				rowCount ++;
            	}
            	if (rowCount != 0) {
                    LOG.info("cleanUpExistingToscaData: " + serviceUUID);
               		jdbcDataSource.writeData("DELETE from NETWORK_MODEL where service_uuid = " + serviceUUID + ";", null, null);
               		jdbcDataSource.writeData("DELETE from SERVICE_MODEL where service_uuid = " + serviceUUID + ";", null, null);
            	}

			} catch (SQLException e) {
				LOG.error("Could not clean up existing NETWORK_MODEL and SERVICE_MODEL for service_UUID " + serviceUUID, e);
			}

     }


	 protected void insertToscaData(String toscaDataString, ArrayList<String> arguments) throws IOException
     {
            LOG.debug("insertToscaData: " + toscaDataString);

            if (jdbcDataSource == null) {
            	 setJdbcDataSource();
            }
             try {

 				jdbcDataSource.writeData(toscaDataString, arguments, null);

			} catch (SQLException e) {
				LOG.error("Could not insert Tosca YAML data into the database ");
				throw new IOException (e);
			}

     }


    private SdncArtifactType analyzeFileType(ArtifactTypeEnum artifactType, File spoolFile, Document spoolDoc) {

        if (artifactType != ArtifactTypeEnum.YANG_XML) {
            LOG.error("Unexpected artifact type - expecting YANG_XML, got "+artifactType);
			return null;
        }

        // Examine outer tag

        try {


            Element root = spoolDoc.getDocumentElement();

            String rootName = root.getTagName();

            if (rootName.contains(":")) {
                String[] rootNameElems = rootName.split(":");
                rootName = rootNameElems[rootNameElems.length - 1];
            }

            if (rootName != null) {
            	SdncArtifactType mapEntry = config.getMapping(rootName);


                if (mapEntry == null) {

                    LOG.error("Unexpected file contents - root tag is "+rootName);
                }
				return mapEntry;
            } else {
                LOG.error("Cannot get root tag from file");
				return null;
            }

        } catch (Exception e) {
            LOG.error("Could not parse YANG_XML file "+spoolFile.getName(), e);
			return null;
        }
    }

    private void scheduleDeployment(SdncArtifactType type, String svcName, String resourceName, IArtifactInfo artifactInfo, String spoolFileName, File spoolFile) {

        if (type.getPass() < deployList.length) {

            if (artifactInfo != null) {
                LOG.debug("Scheduling "+artifactInfo.getArtifactName()+" version "+artifactInfo.getArtifactVersion()+" for deployment");

                deployList[type.getPass()].add(new DeployableArtifact(type, svcName, resourceName, artifactInfo, spoolFile));
            } else {
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");//dd/MM/yyyy
                Date now = new Date();
                String artifactVersion = sdfDate.format(now);
                LOG.debug("Scheduling "+spoolFileName+" version "+artifactVersion+" for deployment");
                String artifactName = spoolFileName;
                if (artifactInfo != null) {
                    artifactName = artifactInfo.getArtifactName();
                }
                deployList[type.getPass()].add(new DeployableArtifact(type, svcName, resourceName, artifactName, artifactVersion, spoolFile));
            }
        } else {
            LOG.info("Pass for type "+type.getTag()+" is "+type.getPass()+" which is not <= "+deployList.length);
        }
    }


    private DistributionStatusEnum deploySpoolFile(DeployableArtifact artifact) {

        DistributionStatusEnum deployResult = DistributionStatusEnum.DEPLOY_OK;

        StringBuffer msgBuffer = new StringBuffer();


        String namespace = config.getAsdcApiNamespace();
        //String namespace = artifact.getType().getNamespace();

        msgBuffer.append("<input xmlns='");
        msgBuffer.append(namespace);
        msgBuffer.append("'>\n");

        String svcName = artifact.getSvcName();
        String resourceName = artifact.getResourceName();
        String artifactName = artifact.getArtifactName();

        if (svcName != null) {
            if (resourceName != null) {
                artifactName = svcName + "/" + resourceName + "/" + artifactName;
            } else {
                artifactName = svcName + "/" + artifactName;
            }
        }

        // don't add artifact name/version for get-path-segments
        if (!artifactName.contains("get-path-segments")) {
            msgBuffer.append("<artifact-name>"+artifactName+"</artifact-name>\n");
            msgBuffer.append("<artifact-version>"+artifact.getArtifactVersion()+"</artifact-version>\n");        	
        } 
        
        try(BufferedReader rdr = new BufferedReader(new FileReader(artifact.getFile()))) {
            
            String curLine = rdr.readLine();

            while (curLine != null) {
            	
                if (!curLine.startsWith("<?")) {
                	
                	// skip get-path-segments tags
                	boolean skipThisLine = false;
                	if (artifactName.contains("get-path-segments")) {
                		if (curLine.contains("<get-path-segments>") || curLine.contains("</get-path-segments>")) {
                			skipThisLine = true;
                		}
                	}

                	if (!skipThisLine) {
                		msgBuffer.append(curLine+"\n");
                	}
                }
                curLine = rdr.readLine();
            }
        } catch (Exception e) {
            LOG.error("Could not process spool file "+artifact.getFile().getName(), e);
			return(DistributionStatusEnum.DEPLOY_ERROR);
        }

        msgBuffer.append("</input>\n");


        byte[] msgBytes = msgBuffer.toString().getBytes();

        Document results = postRestXml(artifact.getType().getRpcUrl(config.getAsdcApiBaseUrl()), msgBytes);

        if (results == null) {

            deployResult = DistributionStatusEnum.DEPLOY_ERROR;
        } else {

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();

            String asdcApiResponseCode = "500";

            try {

                asdcApiResponseCode = xp.evaluate("//asdc-api-response-code[position()=1]/text()", results.getDocumentElement());
            } catch (Exception e) {
                LOG.error("Caught exception retrying to evaluate xpath", e);
            }

            if (asdcApiResponseCode.contains("200")) {
                LOG.info("Update to SDN-C succeeded");
                deployResult = DistributionStatusEnum.DEPLOY_OK;
            } else {
                LOG.info("Update to SDN-C failed (response code "+asdcApiResponseCode+")");

                if (asdcApiResponseCode.contains("409")) {
                    deployResult = DistributionStatusEnum.ALREADY_DEPLOYED;
                } else {

                    deployResult = DistributionStatusEnum.DEPLOY_ERROR;
                }
            }
        }



		return deployResult;
    }





    public static IDistributionStatusMessage buildStatusMessage(
            final IDistributionClient client, final INotificationData data,
            final IArtifactInfo relevantArtifact,
            final DistributionStatusEnum status) {
        IDistributionStatusMessage statusMessage = new IDistributionStatusMessage() {

            @Override
			public long getTimestamp() {
                long currentTimeMillis = System.currentTimeMillis();
                return currentTimeMillis;
            }

            @Override
			public DistributionStatusEnum getStatus() {
                return status;
            }

            @Override
			public String getDistributionID() {
                return data.getDistributionID();
            }

            @Override
			public String getConsumerID() {
                return client.getConfiguration().getConsumerID();
            }

            @Override
			public String getArtifactURL() {
                return relevantArtifact.getArtifactURL();
            }
        };
        return statusMessage;

    }

    public static IComponentDoneStatusMessage buildComponentStatusMessage(
            final IDistributionClient client, final INotificationData data,
            final DistributionStatusEnum status) {
        IComponentDoneStatusMessage statusMessage = new IComponentDoneStatusMessage() {

            @Override
			public long getTimestamp() {
                long currentTimeMillis = System.currentTimeMillis();
                return currentTimeMillis;
            }

            @Override
			public DistributionStatusEnum getStatus() {
                return status;
            }

            @Override
			public String getDistributionID() {
                return data.getDistributionID();
            }

            @Override
			public String getConsumerID() {
                return client.getConfiguration().getConsumerID();
            }

			@Override
			public String getComponentName() {
				// TODO Auto-generated method stub
				return COMPONENT_NAME;
			}
        };
        return statusMessage;

    }

    private HttpURLConnection getRestXmlConnection(String urlString, String method) throws IOException
    {
        URL sdncUrl = new URL(urlString);
        Authenticator.setDefault(new SdncAuthenticator(config.getSdncUser(), config.getSdncPasswd()));

        HttpURLConnection conn = (HttpURLConnection) sdncUrl.openConnection();

        String authStr = config.getSdncUser()+":"+config.getSdncPasswd();
        String encodedAuthStr = new String(Base64.encodeBase64(authStr.getBytes()));

        conn.addRequestProperty("Authentication", "Basic "+encodedAuthStr);

        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/xml");
        conn.setRequestProperty("Accept", "application/xml");

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

		return conn;

    }

    private Document postRestXml(String urlString, byte[] msgBytes) {
        Document response = null;

        try {
			SdncOdlConnection odlConn = SdncOdlConnection.newInstance(urlString, config.getSdncUser(), config.getSdncPasswd());

			String sdncResp = odlConn.send("POST", "application/xml", new String(msgBytes));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            DocumentBuilder db = dbf.newDocumentBuilder();

			response = db.parse(new ByteArrayInputStream(sdncResp.getBytes()));
        } catch (Exception e) {
			LOG.error("Caught exception posting to ODL tier", e);
        }

		return(response);

    }

    private File applyXslts(File srcFile) {

        Document doc = null;


        File inFile = srcFile;
        File outFile = null;

        String xsltPathList = config.getXsltPathList();

		if ((xsltPathList == null) || (xsltPathList.length() == 0)) {
            outFile = inFile;
        } else {

            String[] xsltPaths = xsltPathList.split(",");

            for (String xsltPath : xsltPaths) {
                try{

                    outFile = File.createTempFile("tmp", "xml");
                    TransformerFactory factory = TransformerFactory.newInstance();
                    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);  
                    //factory.setFeature("http://xml.org/sax/features/external-general-entities", false); -- breaks transform
                    //factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);                   
                    Source xslt = new StreamSource(new File(xsltPath));
                    Transformer transformer = factory.newTransformer(xslt);
                    Source text = new StreamSource(inFile);


                    transformer.transform(text, new StreamResult(outFile));

                    inFile = outFile;

                } catch (Exception e) {
                    LOG.error("Caught exception trying to apply XSLT template "+xsltPath, e);

                }

            }
        }

        // After transformations, parse transformed XML


		return outFile;
    }

    private String escapeFilename(String str) {
	if (str == null) {
		str = "";
	}

        StringBuffer retval = new StringBuffer();
	
        for (int i = 0 ; i < str.length() ; i++) {
            char curchar = str.charAt(i);
            if (Character.isJavaIdentifierPart(curchar)) {
                retval.append(curchar);
            }
        }

		return retval.toString();

    }

    private boolean artifactIsTosca(IArtifactInfo artifact, File spoolFile) {
        
		boolean toscaYamlType = false;
		boolean toscaCsarType = false;
        if (artifact != null) {
			String artifactTypeString = artifact.getArtifactType();
			if (artifactTypeString.contains("TOSCA_TEMPLATE")) {
				toscaYamlType = true;
			} else if (artifactTypeString.contains("TOSCA_CSAR")) {
				toscaCsarType = true;
			}
		} else {
			if (spoolFile.toString().contains(".yml")) {
				toscaYamlType = true;
			} else if (spoolFile.toString().contains(".csar")) {
				toscaCsarType = true;
			}
        }
        
        return (toscaYamlType||toscaCsarType ? true : false);
    }

}
