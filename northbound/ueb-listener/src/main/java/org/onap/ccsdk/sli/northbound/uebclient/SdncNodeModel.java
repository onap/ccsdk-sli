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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdncNodeModel extends SdncBaseModel {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SdncNodeModel.class);
	
	private String serviceUUID = null;
	private String ecompGeneratedNaming = null;
	private String [] bindingUuids = null; 
	
	// Using ASDC TOSCA Parser 17.07
	public SdncNodeModel(ISdcCsarHelper sdcCsarHelper, IEntityDetails nodeEntity, DBResourceManager jdbcDataSource, SdncUebConfiguration config) throws IOException {
		
		super(sdcCsarHelper, nodeEntity, jdbcDataSource, config);

		// extract properties
		addParameter("ecomp_generated_naming", extractBooleanValue (nodeEntity, "exVL_naming", "ecomp_generated_naming")); // should be extractBooleanValue?
		addParameter("naming_policy", extractValue (nodeEntity, "exVL_naming", "naming_policy"));

		addParameter("network_type", extractValue (nodeEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTYPE));
		addParameter("network_role", extractValue (nodeEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKROLE));
		addParameter("network_scope", extractValue (nodeEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKSCOPE));
		addParameter("network_technology", extractValue (nodeEntity, SdcPropertyNames.PROPERTY_NAME_NETWORKTECHNOLOGY));

		// extract properties - network_assignments
		addParameter("is_shared_network", extractBooleanValue (nodeEntity, "network_assignments", "is_shared_network"));
		addParameter("is_external_network", extractBooleanValue (nodeEntity, "network_assignments", "is_external_network"));
		String trunkNetworkIndicator = extractBooleanValue(nodeEntity, "network_assignments", "is_trunked");
		addParameter("trunk_network_indicator", trunkNetworkIndicator);

		// extract properties - network_assignments - ipv4_subnet_default_assignment
		String useIpv4 = extractBooleanValue(nodeEntity, "network_assignments", "ipv4_subnet_default_assignment", "use_ipv4");
		addParameter("use_ipv4", useIpv4);
		addParameter("ipv4_dhcp_enabled", extractBooleanValue(nodeEntity, "network_assignments", "ipv4_subnet_default_assignment", "dhcp_enabled"));
		addParameter("ipv4_ip_version", extractValue(nodeEntity, "network_assignments", "ipv4_subnet_default_assignment", "ip_version"));
		addParameter("ipv4_cidr_mask", extractValue(nodeEntity, "network_assignments", "ipv4_subnet_default_assignment", "cidr_mask"));
		addParameter("eipam_v4_address_plan", extractValue(nodeEntity, "network_assignments", "ipv4_subnet_default_assignment", "ip_network_address_plan"));
		
		// extract properties - network_assignments - ipv6_subnet_default_assignment
		String useIpv6 = extractBooleanValue(nodeEntity, "network_assignments", "ipv6_subnet_default_assignment", "use_ipv6");
		addParameter("use_ipv6", useIpv6);
		addParameter("ipv6_dhcp_enabled", extractBooleanValue(nodeEntity, "network_assignments", "ipv6_subnet_default_assignment", "dhcp_enabled"));
		addParameter("ipv6_ip_version", extractValue(nodeEntity, "network_assignments", "ipv6_subnet_default_assignment", "ip_version"));
		addParameter("ipv6_cidr_mask", extractValue(nodeEntity, "network_assignments", "ipv6_subnet_default_assignment", "cidr_mask"));
		addParameter("eipam_v6_address_plan", extractValue(nodeEntity, "network_assignments", "ipv6_subnet_default_assignment", "ip_network_address_plan"));
		
		// extract properties - provider_network
		addParameter("is_provider_network", extractBooleanValue (nodeEntity, "provider_network", "is_provider_network"));
		addParameter("physical_network_name", extractValue(nodeEntity, "provider_network", "physical_network_name"));

		// extract properties - network_flows
		addParameter("is_bound_to_vpn", extractBooleanValue (nodeEntity, "network_flows", "is_bound_to_vpn"));

		// extract properties - network_flows - vpn_bindings
		String vpnBindingString = extractValue (nodeEntity, "network_flows", "vpn_binding");
		bindingUuids = vpnBindingString.split(",");

	}

	public String getServiceUUID() {
		return serviceUUID;
	}
	public void setServiceUUID(String serviceUUID) {
		this.serviceUUID = serviceUUID;
	}

	public String getEcompGeneratedNaming() {
		return ecompGeneratedNaming;
	}
	public void setEcompGeneratedNaming(String ecompGeneratedNaming) {
		this.ecompGeneratedNaming = ecompGeneratedNaming;
		if (ecompGeneratedNaming != null && !ecompGeneratedNaming.isEmpty()) {
			params.put("ecomp_generated_naming", "\"" + ecompGeneratedNaming + "\"");			
		}
	}
	public void setComplexResourceUUID(String complexResourceUuid) {
		if (complexResourceUuid != null && !complexResourceUuid.isEmpty()) {
			params.put("complex_resource_uuid", complexResourceUuid);			
		}
	}

	public void setComplexResourceCustomizationUUID(String complexResourceCustomizationUuid) {
		if (complexResourceCustomizationUuid != null && !complexResourceCustomizationUuid.isEmpty()) {
			params.put("complex_resource_customization_uuid", complexResourceCustomizationUuid);			
		}
	}

	public void insertNetworkModelData () throws IOException {
		try {
			// Clean up NETWORK_MODEL data for this customization_uuid and service_uuid? 
			cleanUpExistingToscaData("NETWORK_MODEL", "customization_uuid", getCustomizationUUID());
			cleanUpExistingToscaData("VPN_BINDINGS", "network_customization_uuid", getCustomizationUUID());
			LOG.info("Call insertToscaData for NETWORK_MODEL where customization_uuid = " + getCustomizationUUID());
			insertToscaData(getSql(model_yaml), null);
			insertToscaData(getVpnBindingsSql(), null);
		} catch (IOException e) {
			LOG.error("Could not insert Tosca CSAR data into the NETWORK_MODEL table");
			throw new IOException (e);
		}
	}
	
	public void insertRelatedNetworkRoleData () throws IOException {
		
		if (entityDetails.getProperties().containsKey("network_assignments")) {
			
			Map<String, Object> networkAssignmentsPropertyValue = (Map<String, Object>) entityDetails.getProperties().get("network_assignments").getValue();
			
			if (networkAssignmentsPropertyValue != null && networkAssignmentsPropertyValue.containsKey("related_networks")) {
				
				ArrayList<Map<String, String>> relatedNetworkList = (ArrayList) networkAssignmentsPropertyValue.get("related_networks");
				String networkModelCustomizationUUID = getCustomizationUUID();
			
	    		try {
	    			cleanUpExistingToscaData("RELATED_NETWORK_ROLE", "network_model_customization_uuid", networkModelCustomizationUUID);
	    		} catch (IOException e) {
	    			LOG.error("Could not clean up Tosca CSAR data in the RELATED_NETWORK_ROLE table");
	    			throw new IOException (e);
	    		}
	    		
	    		for (Map<String, String> relatedNetworkValue : relatedNetworkList) {
	    			String relatedNetworkRoleValue = relatedNetworkValue.get("related_network_role");
	            	LOG.debug("Node Template [" + entityDetails.getName() + "], property [" + "related_network_role" + "] property value: " + relatedNetworkRoleValue);
	                
					try {
						// Table cleanup RELATED_NETWORK_ROLE occurs per network
						// If related_network_role for this service already exist in RELATED_NETWORK_ROLE, don't attempt insertion
						Map<String, String> relatedNetworkRoleParamsCheck = new HashMap<String, String>();
						addParameter("related_network_role", relatedNetworkRoleValue, relatedNetworkRoleParamsCheck);
						addParameter("network_model_customization_uuid", networkModelCustomizationUUID, relatedNetworkRoleParamsCheck);
						if (checkForExistingToscaData("RELATED_NETWORK_ROLE", relatedNetworkRoleParamsCheck) == false) { 
							relatedNetworkRoleParamsCheck.remove("related_network_role");
	    					LOG.info("Call insertToscaData for RELATED_NETWORK_ROLE where network_model_customization_uuid = " + networkModelCustomizationUUID);
	    					insertToscaData(buildSql("RELATED_NETWORK_ROLE", "related_network_role", "\"" + relatedNetworkRoleValue + "\"", model_yaml, relatedNetworkRoleParamsCheck), null);
						}
					} catch (IOException e) {
						LOG.debug("Could not insert Tosca CSAR data into the RELATED_NETWORK_ROLE table");
						throw new IOException (e);
					}
	            }
	        }
	        else {
	        	LOG.debug("Node Template [" + entityDetails.getName() + "], property [" + "related_networks" + "] property value: " + null);
	        }
		}
		
	}
	
	public String getSql(String model_yaml) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT into NETWORK_MODEL (service_uuid, customization_uuid, model_yaml, ");
		
		int paramCount = 0;
		for (String paramKey :  params.keySet()) {
			paramCount++;
		    sb.append(paramKey);
		    if (paramCount < params.size()) sb.append(", ");
		}
		
		sb.append(") values (" + serviceUUID + ", " + getCustomizationUUID() + ", \"" + model_yaml + "\", ");

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

	public String getVpnBindingsSql() {
		
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < bindingUuids.length; i++) {
			sb.append("INSERT into VPN_BINDINGS (network_customization_uuid, binding_uuid) values (" + getCustomizationUUID() + ", \"" + bindingUuids[i] + "\"); ");
		}

		return sb.toString();
	}
	
}
