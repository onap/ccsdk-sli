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
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdncVFCModel extends SdncBaseModel {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SdncVFCModel.class);
	
	private String vmType = null;
	private String vmCount = null;

	public SdncVFCModel(ISdcCsarHelper sdcCsarHelper, IEntityDetails entityDetails, DBResourceManager jdbcDataSource, SdncUebConfiguration config) throws IOException {

		super(sdcCsarHelper, entityDetails, jdbcDataSource, config);
		
		// extract properties
		addParameter("ecomp_generated_naming", extractBooleanValue (entityDetails, "nfc_naming", "ecomp_generated_naming"));
		addParameter("naming_policy", extractValue (entityDetails, "nfc_naming", "naming_policy"));
		vmCount = extractValue (entityDetails, "service_template_filter", "count"); // need path to vm_count, extracted as service_template_filter#count
		if (vmCount.isEmpty()) {
			vmCount = "0"; // vm_count can not be null
		}
		vmType = extractValue (entityDetails, SdcPropertyNames.PROPERTY_NAME_VMTYPETAG);
		addParameter("vm_type", vmType); // populate vm_type with vm_type_tag value
		addParameter("vm_type_tag", vmType);
		addParameter("nfc_naming_code", extractValue (entityDetails, "nfc_naming_code"));
		addParameter("nfc_function", extractValue (entityDetails, "nfc_function"));
		addParameter("high_availability", extractValue (entityDetails, "high_availablity"));
		addParameter("vm_image_name", extractValue (entityDetails, "vm_image_name"));
		addParameter("vm_flavor_name", extractValue (entityDetails, "vm_flavor_name"));
		addParameter("nfc_naming", extractValue (entityDetails, "nfc_naming"));
		addParameter("min_instances", extractValue (entityDetails, "min_instances"));
		addParameter("max_instances", extractValue (entityDetails, "max_instances"));
	}

	public void insertVFCModelData () throws IOException {
		try {
			cleanUpExistingToscaData("VFC_MODEL", "customization_uuid", getCustomizationUUID());
			LOG.info("Call insertToscaData for VFC_MODEL where customization_uuid = " + getCustomizationUUID());
			insertToscaData(buildSql("VFC_MODEL", model_yaml), null);
		} catch (IOException e) {
			LOG.error("Could not insert Tosca CSAR data into the VFC_MODEL table");
			throw new IOException (e);
		}

	}
	
	public void insertVFCtoNetworkRoleMappingData (IEntityDetails cvfcEntity) throws IOException {		

		// Get the CPs on this VFC - using getEntity
		// For each VFC node, get CP properties to insert into VFC_TO_NETWORK_ROLE_MAPPING
		// VFC_TO_NETWORK_ROLE_MAPPING: vfc_customization_uuid, network_role, network_role_tag, vm_type, ipv4_count, ipv6_count,
		// ipv4_use_dhcp, ipv6_use_dhcp, ipv4_ip_version, ipv6_ip_version, extcp_subnetpool_id
		
		String vfcCustomizationUuid = getCustomizationUUID().replace("\"", "");
		EntityQuery entityQueryCP = EntityQuery.newBuilder(SdcTypes.CP).build();
	    TopologyTemplateQuery topologyTemplateQueryVFC = TopologyTemplateQuery.newBuilder(SdcTypes.CVFC).customizationUUID(vfcCustomizationUuid).build();
	    List<IEntityDetails> cpEntities = sdcCsarHelper.getEntity(entityQueryCP, topologyTemplateQueryVFC, true);
		if (cpEntities == null || cpEntities.isEmpty()) {
			LOG.debug("insertVFCtoNetworkRoleMappingData: Could not find the nested CVFCs for: " + vfcCustomizationUuid);
			return;
		}
		
		// Clean up all VFC_TO_NETWORK_ROLE_MAPPING data for this VFC node
		try {
			cleanUpExistingToscaData("VFC_TO_NETWORK_ROLE_MAPPING", "vfc_customization_uuid", getCustomizationUUID());
		} catch (IOException e) {
			LOG.error("Could not clean up data in VFC_TO_NETWORK_ROLE_MAPPING table ", e);
		}

		// There can be multiple insertions per CP:
		// 		Insert once for each unique IP Version / Subnet Role combination per CP (network_role)
		for (IEntityDetails cpEntity : cpEntities) {
			
			// Extract common parameters 
			Map<String, String> commonParams = new HashMap<String, String>();  // non-IP Version specific parameters
			// Get vm_type from VFC node
			addParameter("vm_type", getVmType(), commonParams);
			
			// Extract non-IP Version specific parameters - outside the ip_requirements block
			String networkRole = extractValue(cpEntity, "network_role").isEmpty() ? "default-network-role" : extractValue(cpEntity, "network_role");  // set default-network-role?
			addParameter("network_role", networkRole, commonParams); // can not be null
			addParameter("network_role_tag", nullCheck(extractValue(cpEntity, "network_role_tag")), commonParams);
			addParameter("extcp_subnetpool_id", nullCheck(extractValue(cpEntity, "subnetpoolid")), commonParams);
			String subinterfaceIndicator = extractBooleanValue (cpEntity, "subinterface_indicator");
			addParameter("subinterface_indicator", subinterfaceIndicator, commonParams);
			
			// Build lists of all IPV4 and IPV6 ip_requirements elements
			ArrayList<Map<String, String>> ipv4PropParamsList = new ArrayList<Map<String, String>>();
			ArrayList<Map<String, String>> ipv6PropParamsList = new ArrayList<Map<String, String>>();
				
			// Extract IP Version specific parameters
			if (cpEntity.getProperties().containsKey("ip_requirements")) {
				
				ArrayList<Map<String, Object>>  ipPropsList = new ArrayList<Map<String, Object>>(); 
				ipPropsList =  (ArrayList<Map<String, Object>>) cpEntity.getProperties().get("ip_requirements").getValue();
				
				if (ipPropsList != null) {
					for (Map<String, Object> ipPropMap :  ipPropsList) {
						//LOG.info("ip_requirements prop map = " + nullCheck(ipPropMap));
		
						String ipVersion = nullCheck(ipPropMap.get("ip_version"));
						if (ipVersion == null) {
							LOG.error("SdncVFCModel: ipVersion not included in ip_requirements element");	
							continue;
						}

						String subnetRole = nullCheck(ipPropMap.get("subnet_role"));
						
						if (ipVersion.contains("4")) {

							// If we have already encountered this subnetRole for IPV4, skip this ip_requirements element
							if (!ipPropParamsMapContainsSubnetRole (ipv4PropParamsList, subnetRole)) {
							
								Map<String, String> ipv4PropParams = new HashMap<String, String>();
								SdncBaseModel.addParameter("ipv4_ip_version", ipVersion, ipv4PropParams);
								SdncBaseModel.addParameter("ipv4_use_dhcp", nullCheck(ipPropMap.get("dhcp_enabled")).contains("true") ? "Y" : "N", ipv4PropParams);
								Map<String, Object> ipCountRequired = (Map<String, Object>)ipPropMap.get("ip_count_required");
								if (ipCountRequired != null && ipCountRequired.get("count") != null) {
									SdncBaseModel.addParameter("ipv4_count", nullCheck(ipCountRequired.get("count")), ipv4PropParams);
								}
								Map<String, Object> floatingIpCountRequired = (Map<String, Object>)ipPropMap.get("floating_ip_count_required");
								if (floatingIpCountRequired != null && floatingIpCountRequired.get("count") != null) {
									SdncBaseModel.addParameter("ipv4_floating_count", nullCheck(floatingIpCountRequired.get("count")), ipv4PropParams);
								}
								SdncBaseModel.addParameter("ipv4_address_plan_name", nullCheck(ipPropMap.get("ip_address_plan_name")), ipv4PropParams);
								SdncBaseModel.addParameter("ipv4_vrf_name", nullCheck(ipPropMap.get("vrf_name")), ipv4PropParams);
								SdncBaseModel.addParameter("subnet_role", nullCheck(ipPropMap.get("subnet_role")), ipv4PropParams);
								
								ipv4PropParamsList.add(ipv4PropParams);
								
							} else {
								LOG.error("SdncVFCModel: Additional V4 ip-requirements element encountered for this subnet_role: ", subnetRole);
							}

						} else if (ipVersion.contains("6")) {

							// If we have already encountered this subnetRole for IPV6, skip this ip_requirements element
							if (!ipPropParamsMapContainsSubnetRole (ipv6PropParamsList, subnetRole)) { 
							
								Map<String, String> ipv6PropParams = new HashMap<String, String>();
								SdncBaseModel.addParameter("ipv6_ip_version", ipVersion, ipv6PropParams);
								SdncBaseModel.addParameter("ipv6_use_dhcp", nullCheck(ipPropMap.get("dhcp_enabled")).contains("true") ? "Y" : "N", ipv6PropParams);
								Map<String, Object> ipCountRequired = (Map<String, Object>)ipPropMap.get("ip_count_required");
								if (ipCountRequired != null && ipCountRequired.get("count") != null) {
									SdncBaseModel.addParameter("ipv6_count", nullCheck(ipCountRequired.get("count")), ipv6PropParams);
								}
								Map<String, Object> floatingIpCountRequired = (Map<String, Object>)ipPropMap.get("floating_ip_count_required");
								if (floatingIpCountRequired != null && floatingIpCountRequired.get("count") != null) {
									SdncBaseModel.addParameter("ipv6_floating_count", nullCheck(floatingIpCountRequired.get("count")), ipv6PropParams);
								}
								SdncBaseModel.addParameter("ipv6_address_plan_name", nullCheck(ipPropMap.get("ip_address_plan_name")), ipv6PropParams);
								SdncBaseModel.addParameter("ipv6_vrf_name", nullCheck(ipPropMap.get("vrf_name")), ipv6PropParams);
								SdncBaseModel.addParameter("subnet_role", nullCheck(ipPropMap.get("subnet_role")), ipv6PropParams);
								
								ipv6PropParamsList.add(ipv6PropParams);
								
							} else {							
								LOG.error("SdncVFCModel: Additional V6 ip-requirements element encountered for this subnetRole: ", subnetRole);							
							}
								
						} else {
							LOG.error("SdncVFCModel: invalid IP version encountered: ", ipVersion);
						}					
						
					} // for each ip-requirements element
					
				} // ipPropsList null check		
			
				// After all Common and IP Version specific parameters are extracted, insert IPV4 and IPV6 data separately
				// Insert IPV4 data
				for (Map<String, String> ipv4PropParams: ipv4PropParamsList) {
					
					Map<String, String> mappingParams = new HashMap<String, String>();	// final list for single insertion
					addParamsToMap(commonParams, mappingParams);
					addParamsToMap(ipv4PropParams, mappingParams);
									
					// Insert ipv4PropParams into VFC_TO_NETWORK_ROLE_MAPPING
					try {
						LOG.info("Call insertToscaData for VFC_TO_NETWORK_ROLE_MAPPING where vfc_customization_uuid = " + getCustomizationUUID());
						addRequiredParameters(mappingParams);
						insertToscaData(SdncBaseModel.getSql("VFC_TO_NETWORK_ROLE_MAPPING", "vfc_customization_uuid", getCustomizationUUID(), "", mappingParams), null);
					} catch (IOException e) {
						LOG.error("Could not insert Tosca CSAR data into the VFC_TO_NETWORK_ROLE_MAPPING table");
						throw new IOException (e);
					}	
	
				}
				
				// Insert IPV6 data
				for (Map<String, String> ipv6PropParams: ipv6PropParamsList) {
					
					Map<String, String> mappingParams = new HashMap<String, String>();	// final list for single insertion
					addParamsToMap(commonParams, mappingParams);
					addParamsToMap(ipv6PropParams, mappingParams);
					
					// Insert ipv6PropParams into VFC_TO_NETWORK_ROLE_MAPPING
					try {
						LOG.info("Call insertToscaData for VFC_TO_NETWORK_ROLE_MAPPING where vfc_customization_uuid = " + getCustomizationUUID());
						addRequiredParameters(mappingParams);
						insertToscaData(SdncBaseModel.getSql("VFC_TO_NETWORK_ROLE_MAPPING", "vfc_customization_uuid", getCustomizationUUID(), "", mappingParams), null);
					} catch (IOException e) {
						LOG.error("Could not insert Tosca CSAR data into the VFC_TO_NETWORK_ROLE_MAPPING table");
						throw new IOException (e);
					}		
				}
		    }
	    }
	}
	
	protected boolean ipPropParamsMapContainsSubnetRole (ArrayList<Map<String, String>> ipPropParamsList, String subnetRole) {
		
		boolean subnetRoleFound = false;
		
		if (subnetRole != null && !subnetRole.isEmpty()) { 
			for (Map<String, String> ipPropMap :  ipPropParamsList) {
				if (ipPropMap.get("subnet_role").contentEquals(subnetRole)) {
					return true;
				}
			}
		}
		return subnetRoleFound;
	}
	
	private void addRequiredParameters (Map<String, String> mappingParams) {
		
		// Add parameters which can not be null if they have not already been added - network_role, ipv4_count, ipv6_count
		String ipvCountDefault = "0";
		if (!mappingParams.containsKey("ipv4_count")) {
			SdncBaseModel.addParameter("ipv4_count", ipvCountDefault, mappingParams);
		}
		if (!mappingParams.containsKey("ipv6_count")) {
			SdncBaseModel.addParameter("ipv6_count", ipvCountDefault, mappingParams);
		}
	}
	
	public void insertVFCRelatedNetworkRoleData (String vfCustomizationUUID, IEntityDetails cvfcEntity) throws IOException {
		
		// Get the CPs on this VFC - using getEntity
		
		String vfcCustomizationUuid = getCustomizationUUID().replace("\"", "");
		// Get the CPs on this VFC - ASDC suggests getNodeTemplateChildren
		//List<NodeTemplate> cpNodesList = sdcCsarHelper.getNodeTemplateChildren(vfcNode);
		EntityQuery entityQueryCP = EntityQuery.newBuilder(SdcTypes.CP).build();
	    TopologyTemplateQuery topologyTemplateQueryVFC = TopologyTemplateQuery.newBuilder(SdcTypes.CVFC).customizationUUID(vfcCustomizationUuid).build();
	    List<IEntityDetails> cpEntities = sdcCsarHelper.getEntity(entityQueryCP, topologyTemplateQueryVFC, true);
		if (cpEntities == null || cpEntities.isEmpty()) {
			LOG.debug("insertVFCRelatedNetworkRoleData: Could not find the nested CVFCs for: " + vfcCustomizationUuid);
			return;
		}
		
		try {
			cleanUpExistingToscaData("VFC_RELATED_NETWORK_ROLE", "vfc_customization_uuid", getCustomizationUUID());
		} catch (IOException e) {
			LOG.error("Could not clean up Tosca CSAR data in the VFC_RELATED_NETWORK_ROLE table");
			throw new IOException (e);
		}

		for (IEntityDetails cpEntity : cpEntities){
			String networkRole = extractValue(cpEntity, "network_role");
			Map<String, String> relatedNetworkRoleParams = new HashMap<String, String>();
			addParameter("vfc_customization_uuid", getCustomizationUUID(), relatedNetworkRoleParams);
			addParameter("vm_type", vmType, relatedNetworkRoleParams);
			addParameter("network_role", networkRole, relatedNetworkRoleParams);
				
			if (cpEntity.getProperties().containsKey("related_networks")) {
				
				Property relatedNetworksProperty = cpEntity.getProperties().get("related_networks");
				List<String> relatedNetworkRoles = relatedNetworksProperty.getLeafPropertyValue("related_network_role");
				
				for (String relatedNetworkRole : relatedNetworkRoles) {
					LOG.debug("CP [" + cpEntity.getName() + "], property [" + "related_network_role" + "] property value: " + relatedNetworkRole);
					
    				try {
    					// Table cleanup for VFC_RELATED_NETWORK_ROLE occurs per vfc
    					// If cp related_network_role, cp network_role and vm_type for this vfc already exist in VFC_RELATED_NETWORK_ROLE,
    					// don't attempt insertion
    					Map<String, String> relatedNetworkRoleParamsCheck = new HashMap<String, String>();
    					addParamsToMap(relatedNetworkRoleParams, relatedNetworkRoleParamsCheck);
    					addParameter("related_network_role", relatedNetworkRole, relatedNetworkRoleParamsCheck);
    					if (checkForExistingToscaData("VFC_RELATED_NETWORK_ROLE", relatedNetworkRoleParamsCheck) == false) {    					
        					LOG.info("Call insertToscaData for VFC_RELATED_NETWORK_ROLE where vfc_customization_uuid = " + getCustomizationUUID());
        					insertToscaData(buildSql("VFC_RELATED_NETWORK_ROLE", "related_network_role", "\"" + relatedNetworkRole + "\"", model_yaml, relatedNetworkRoleParams), null);
    					}
    					
    					// Table cleanup for VNF_RELATED_NETWORK_ROLE occurs per vf (up one level)
    					// Insert same related_network_role data into VNF_RELATED_NETWORK_ROLE
    					Map<String, String> vfRelatedNetworkRoleParamsCheck = new HashMap<String, String>();
    					addParameter("vnf_customization_uuid", vfCustomizationUUID, vfRelatedNetworkRoleParamsCheck);
    					addParameter("network_role", networkRole, vfRelatedNetworkRoleParamsCheck);
    					addParameter("related_network_role", relatedNetworkRole, vfRelatedNetworkRoleParamsCheck);
    					if (checkForExistingToscaData("VNF_RELATED_NETWORK_ROLE", vfRelatedNetworkRoleParamsCheck) == false) {
    						vfRelatedNetworkRoleParamsCheck.remove("related_network_role");
        					LOG.info("Call insertToscaData for VNF_RELATED_NETWORK_ROLE where vnf_customization_uuid = " + vfCustomizationUUID);
        					insertToscaData(buildSql("VNF_RELATED_NETWORK_ROLE", "related_network_role", "\"" + relatedNetworkRole + "\"", model_yaml, vfRelatedNetworkRoleParamsCheck), null);
    					}    					

    				} catch (IOException e) {
    					LOG.error("Could not insert Tosca CSAR data into the VFC_RELATED_NETWORK_ROLE table");
    					throw new IOException (e);
    				}
				}				
			}			
		}
	}

	public String getVmType() {
		return vmType;
	}

	public void setVmType(String vmType) {
		this.vmType = vmType;
	}

	public String getVmCount() {
		return vmCount;
	}

	public void setVmCount(String vmCount) {
		this.vmCount = vmCount;
	}

}
