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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdncPNFModel extends SdncBaseModel {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SdncVFModel.class);

	private String vendor = null;
	private String vendorModelDescription = null;
	private String nfNamingCode = null;
	private String serviceUUID = null;
	private String serviceInvariantUUID = null;

	public SdncPNFModel(ISdcCsarHelper sdcCsarHelper, IEntityDetails entityDetails, DBResourceManager jdbcDataSource, SdncUebConfiguration config) throws IOException {

		super(sdcCsarHelper, entityDetails, jdbcDataSource, config);

		// extract metadata
		Metadata metadata = entityDetails.getMetadata();
		addParameter("name", extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_NAME));
		vendor = extractValue (metadata, SdcPropertyNames.PROPERTY_NAME_RESOURCEVENDOR);
		addParameter("vendor", vendor); 
		vendorModelDescription = extractValue (metadata, "description");
		addParameter("vendor_version", extractValue (metadata, SdcPropertyNames.PROPERTY_NAME_RESOURCEVENDORRELEASE)); 
		
		// extract properties
		addParameter("ecomp_generated_naming", extractBooleanValue(entityDetails, "nf_naming#ecomp_generated_naming"));
		addParameter("naming_policy", extractValue(entityDetails, "nf_naming#naming_policy"));
		addParameter("nf_type", extractValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFTYPE));
		addParameter("nf_role", extractValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFROLE));
		nfNamingCode = extractValue(entityDetails, "nf_naming_code");
		addParameter("nf_code", nfNamingCode);
		addParameter("nf_function", extractValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION));
		addIntParameter("avail_zone_max_count", extractValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONEMAXCOUNT));
		addParameter("sdnc_model_name", extractValue(entityDetails, "sdnc_model_name"));
		addParameter("sdnc_model_version", extractValue(entityDetails, "sdnc_model_version"));
		addParameter("sdnc_artifact_name", extractValue(entityDetails, "sdnc_artifact_name"));
		
	}
	
	public void insertData() throws IOException {
		
		insertPNFModelData();
	}
	
	private void insertPNFModelData () throws IOException {

		try {
			cleanUpExistingToscaData("VF_MODEL", "customization_uuid", getCustomizationUUID()) ;
			cleanUpExistingToscaData("SERVICE_MODEL_TO_VF_MODEL_MAPPING", "service_uuid", serviceUUID, "vf_customization_uuid", getCustomizationUUID());
			
			// insert into VF_MODEL/ATTRIBUTE_VALUE_PAIR and SERVICE_MODEL_TO_VF_MODEL_MAPPING
			LOG.info("Call insertToscaData for VF_MODEL where customization_uuid = " + getCustomizationUUID());
			insertToscaData(buildSql("VF_MODEL", model_yaml), null);
			//insertRelevantAttributeData();
			
			Map<String, String> mappingParams = new HashMap<String, String>();
			addParameter("service_invariant_uuid", serviceInvariantUUID, mappingParams);
			addParameter("vf_uuid", getUUID(), mappingParams);
			addParameter("vf_customization_uuid", getCustomizationUUIDNoQuotes(), mappingParams);
			insertToscaData(buildSql("SERVICE_MODEL_TO_VF_MODEL_MAPPING", "service_uuid", serviceUUID, model_yaml, mappingParams), null);

		} catch (IOException e) {
			LOG.error("Could not insert Tosca CSAR data into the VF_MODEL table");
			throw new IOException (e);
		}
		
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVendorModelDescription() {
		return vendorModelDescription;
	}

	public void setVendorModelDescription(String vendorModelDescription) {
		this.vendorModelDescription = vendorModelDescription;
	}

	public String getNfNamingCode() {
		return nfNamingCode;
	}

	public void setNfNamingCode(String nfNamingCode) {
		this.nfNamingCode = nfNamingCode;
	}
	
	public String getServiceUUID() {
		return serviceUUID;
	}
	public void setServiceUUID(String serviceUUID) {
		this.serviceUUID = serviceUUID;
	}

	public String getServiceInvariantUUID() {
		return serviceInvariantUUID;
	}

	public void setServiceInvariantUUID(String serviceInvariantUUID) {
		this.serviceInvariantUUID = serviceInvariantUUID;
	}

}
