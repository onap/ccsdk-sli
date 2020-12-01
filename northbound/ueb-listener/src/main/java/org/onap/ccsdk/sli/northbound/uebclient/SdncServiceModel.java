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

package org.onap.ccsdk.sli.northbound.uebclient;

import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdncServiceModel extends SdncBaseModel {

	private String UUID = null;
	private String resourceVendor = null;
	private String resourceVendorRelease = null;
	private String serviceInstanceNamePrefix = null;
	private String filename = null;
	
	public SdncServiceModel(ISdcCsarHelper sdcCsarHelper, Metadata metadata, DBResourceManager jdbcDataSource) {
		
		super(sdcCsarHelper, metadata, jdbcDataSource);
	
		UUID = extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_UUID);
		
		// extract ecompGeneratedNaming and namingPolicy from Service Metadata
		addParameter("ecomp_naming",extractBooleanValue(metadata, "ecompGeneratedNaming"));
		addParameter("naming_policy",extractValue(metadata, "namingPolicy"));

		// extract service topology template input data - ecompGeneratedNaming and namingPolicy moved to Service Metadata
		//addParameter("ecomp_naming",extractBooleanInputDefaultValue(SdcPropertyNames.PROPERTY_NAME_SERVICENAMING_DEFAULT_ECOMPGENERATEDNAMING));
		//addParameter("naming_policy",extractInputDefaultValue(SdcPropertyNames.PROPERTY_NAME_SERVICENAMING_DEFAULT_NAMINGPOLICY));

		// extract resourceVendor and resourceVendorRelease for use in SdncServiceProxy class
		resourceVendor = extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_RESOURCEVENDOR);
		resourceVendorRelease = extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_RESOURCEVENDORRELEASE);
	}

	public String getServiceUUID() {
		return "\"" + UUID + "\"";
	}
	public void setServiceUUID(String serviceUUID) {
		this.UUID = serviceUUID;
	}
	public String getServiceInvariantUUID() {
		return "\"" + invariantUUID + "\"";
	}
	public String getServiceInstanceNamePrefix() {
		return serviceInstanceNamePrefix;
	}
	public void setServiceInstanceNamePrefix(String serviceInstanceNamePrefix) {
		if (serviceInstanceNamePrefix != null && !serviceInstanceNamePrefix.isEmpty()) {
			this.serviceInstanceNamePrefix = serviceInstanceNamePrefix;
			params.put("service_instance_name_prefix", "\"" + serviceInstanceNamePrefix + "\"");
		}
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getSql(String model_yaml) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT into SERVICE_MODEL (service_uuid, model_yaml, filename, ");
		
		int paramCount = 0;
		for (String paramKey :  params.keySet()) {
			paramCount++;
		    sb.append(paramKey);
		    if (paramCount < params.size()) sb.append(", ");
		}
		
		sb.append(") values (" + getServiceUUID() + ", \"" + model_yaml + "\", \"" + filename + "\", ");

		paramCount = 0;
		for (String paramKey :  params.keySet()) {
			paramCount++;
			String paramValue = params.get(paramKey);
		    sb.append(paramValue);
		    if (paramCount < params.size()) sb.append(", ");
		}

		sb.append(");");
		return sb.toString();
	}
	
	public String getResourceVendor() {
		return resourceVendor;
	}

	public void setResourceVendor(String resourceVendor) {
		this.resourceVendor = resourceVendor;
	}

	public String getResourceVendorRelease() {
		return resourceVendorRelease;
	}

	public void setResourceVendorRelease(String resourceVendorRelease) {
		this.resourceVendorRelease = resourceVendorRelease;
	}
	
}
