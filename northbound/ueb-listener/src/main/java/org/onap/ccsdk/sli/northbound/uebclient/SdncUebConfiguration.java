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

package org.onap.ccsdk.sli.northbound.uebclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.onap.sdc.api.consumer.IConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdncUebConfiguration implements IConfiguration{

	private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
	private static final Logger LOG = LoggerFactory
			.getLogger(SdncUebConfiguration.class);

	private String asdcAddress = null;
	private String consumerGroup = null;
	private String consumerID = null;
	private String environmentName = null;
	private String password = null;
	private int pollingInterval = 30;
	private int pollingTimeout = 15;
	private int clientStartupTimeout = 900;
	private List<String> relevantArtifactTypes = null;
	private String user = null;

	private String sdncUser = null;
	private String sdncPasswd = null;
	private String asdcApiBaseUrl = null;
	private String asdcApiNamespace = null;
	private List<String> msgBusAddress = null;

	private SdncArtifactMap artifactMap = SdncArtifactMap.getInstance();

	private String incomingDir = null;

	private String archiveDir = null;

	private String overrideFile = null;

	private boolean activateServerTLSAuth;
	private String keyStorePassword;
	private String keyStorePath;

	private String xsltPathList;

	public SdncUebConfiguration() {
		String propDir = System.getenv(SDNC_CONFIG_DIR);
		if (propDir == null) {

			propDir = "/opt/sdnc/data/properties";
		}
		try {
			init(propDir);
		} catch (Exception e) {
			LOG.error("Cannot initialize SdncUebConfiguration", e);
		}
	}

	public SdncUebConfiguration(String propDir) {
		try {
			init(propDir);
		} catch (Exception e) {
			LOG.error("Cannot initialize SdncUebConfiguration", e);
		}
	}

	public String getAsdcApiNamespace() {
		return asdcApiNamespace;
	}

	public String getXsltPathList() {
		return xsltPathList;
	}

	public String getOverrideFile() {
		return overrideFile;
	}

	public void init(String propDir) throws IOException {
		String propPath;


		propPath = propDir + "/ueb-listener.properties";
		File propFile = new File(propPath);


		if (!propFile.exists()) {

			throw new FileNotFoundException(
					"Missing configuration properties file : "
							+ propFile);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(propFile));

		asdcAddress = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.asdc-address");
		consumerGroup = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.consumer-group");
		consumerID = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.consumer-id");
		environmentName = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.environment-name");
		password = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.password");
		user = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.user");

		sdncUser = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.sdnc-user");
		sdncPasswd = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.sdnc-passwd");
		asdcApiBaseUrl = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.asdc-api-base-url");
		asdcApiNamespace = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.asdc-api-namespace");

		incomingDir = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.spool.incoming");
		archiveDir = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.spool.archive");
		overrideFile = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.override-file");

		String curval = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.polling-interval");
		if ((curval != null) && (curval.length() > 0)) {
			try {
				pollingInterval = Integer.parseInt(curval);
			} catch (Exception e) {
				LOG.warn("Illegal value for org.onap.ccsdk.sli.northbound.uebclient.polling-interval ({}) ", curval, e);
			}
		}

		curval = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.polling-timeout");
		if ((curval != null) && (curval.length() > 0)) {
			try {
				pollingTimeout = Integer.parseInt(curval);
			} catch (Exception e) {
				LOG.warn("Illegal value for org.onap.ccsdk.sli.northbound.uebclient.polling-timeout ({}) ", curval, e);
			}
		}

		curval = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.client-startup-timeout");
		if ((curval != null) && (curval.length() > 0)) {
			try {
				clientStartupTimeout = Integer.parseInt(curval);
			} catch (Exception e) {
				LOG.warn("Illegal value for org.onap.ccsdk.sli.northbound.uebclient.polling-timeout ({}) ", curval, e);
			}
		}
		curval = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.relevant-artifact-types");
		if ((curval != null) && (curval.length() > 0)) {
			String[] artifactTypes = curval.split(",");

			relevantArtifactTypes = new LinkedList<>();

			for (String artifactType : artifactTypes) {

				relevantArtifactTypes.add(artifactType);

			}

		}

		curval = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.activate-server-tls-auth", "false");
		activateServerTLSAuth = "true".equalsIgnoreCase(curval);
		keyStorePath = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.keystore-path");
		keyStorePassword = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.keystore-password");
		xsltPathList = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.xslt-path-list");


		String artifactMapFile = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.artifact-map");
		if (artifactMapFile != null) {
			LOG.info("Loading artifactMapFile {}", artifactMapFile);
			artifactMap.load(artifactMapFile);
		} else {
			LOG.warn("artifact-map is unset");
		}

		msgBusAddress = new LinkedList<>();
		String msgBusAddressStr = props.getProperty("org.onap.ccsdk.sli.northbound.uebclient.msg-bus-address");
		if (msgBusAddressStr != null) {
		    String[] msgBusAddressArray = msgBusAddressStr.split(",");
		    for (int i = 0 ; i < msgBusAddressArray.length ; i++) {
		        msgBusAddress.add(msgBusAddressArray[i]);
		    }
		}


	}

	@Override
	public String getAsdcAddress() {
		return asdcAddress;
	}

	@Override
	public String getConsumerGroup() {
		return consumerGroup;
	}

	@Override
	public String getConsumerID() {
		return consumerID;
	}

	@Override
	public String getEnvironmentName() {
		return environmentName;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public int getPollingInterval() {
		return pollingInterval;
	}

	@Override
	public int getPollingTimeout() {
		return pollingTimeout;
	}

	@Override
	public List<String> getRelevantArtifactTypes() {
		return relevantArtifactTypes;
	}

	public int getClientStartupTimeout() {
		return clientStartupTimeout;
	}

	@Override
	public String getUser() {
		return user;
	}


	public String getSdncUser() {
		return sdncUser;
	}

	public String getSdncPasswd() {
		return sdncPasswd;
	}

	public String getAsdcApiBaseUrl() {
		return asdcApiBaseUrl;
	}

	@Override
	public boolean activateServerTLSAuth() {
		return activateServerTLSAuth;
	}

	@Override
	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	@Override
	public String getKeyStorePath() {
		return keyStorePath;
	}

	public String getIncomingDir() {
		return incomingDir;
	}

	public String getArchiveDir() {
		return archiveDir;
	}

	public int getMaxPasses() {
		return artifactMap.getNumPasses();
	}

	public SdncArtifactMap.SdncArtifactType getMapping(String tag) {
		return artifactMap.getMapping(tag);
	}

	@Override
	public boolean isFilterInEmptyResources() {
		return false;
	}

	@Override
	public Boolean isUseHttpsWithDmaap() {
		return false;
	}

    @Override
    public List<String> getMsgBusAddress() {
        return msgBusAddress;
    }


}
