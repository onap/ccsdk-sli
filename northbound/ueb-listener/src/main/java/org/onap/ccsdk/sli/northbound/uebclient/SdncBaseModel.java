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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.CachedRowSet;

import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.CapabilityAssignments;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdncBaseModel {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SdncBaseModel.class);
	
	protected String customizationUUID = null;
	protected String invariantUUID = null;
	protected String UUID = null;
	protected String model_yaml = null;	
	protected String version = null;	
	protected String name = null;

	protected String PARAM_INVARIANT_UUID_KEY = "invariant_uuid";
	protected String PARAM_CUSTOMIZATION_UUID_KEY = "customization_uuid";
	protected String PARAM_UUID_KEY = "uuid";
	protected String PARAM_VERSION_KEY = "version";
	protected String PARAM_NAME_KEY = "name";
	protected String PARAM_DESCRIPTION_KEY = "description";
	protected String PARAM_TYPE_KEY = "type";
	protected String PARAM_CATEGORY_KEY = "category";

	protected Map<String, String> params = null;
	protected Map<String, String> attributeValueParams = null;
	protected ISdcCsarHelper sdcCsarHelper = null;
	protected static DBResourceManager jdbcDataSource = null;
	protected static SdncUebConfiguration config = null;
	protected IEntityDetails entityDetails = null;

	
	public SdncBaseModel(ISdcCsarHelper sdcCsarHelper, IEntityDetails entityDetails, DBResourceManager jdbcDataSource, SdncUebConfiguration config) throws IOException {
		this (sdcCsarHelper, entityDetails);
		this.sdcCsarHelper = sdcCsarHelper;
		this.entityDetails = entityDetails;
		SdncBaseModel.jdbcDataSource = jdbcDataSource;		
		SdncBaseModel.config = config;
	}

	public SdncBaseModel(ISdcCsarHelper sdcCsarHelper, Metadata metadata, DBResourceManager jdbcDataSource) {

		params = new HashMap<String, String>();
		this.sdcCsarHelper = sdcCsarHelper;
		SdncBaseModel.jdbcDataSource = jdbcDataSource;

		// extract service metadata
		invariantUUID = extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID);
		addParameter(PARAM_INVARIANT_UUID_KEY,invariantUUID);
		addParameter(PARAM_VERSION_KEY,extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_VERSION));
		name = extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_NAME);
		addParameter(PARAM_NAME_KEY,name);
		addParameter(PARAM_DESCRIPTION_KEY,extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_DESCRIPTION));
		addParameter(PARAM_TYPE_KEY,extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_TYPE));
		addParameter(PARAM_CATEGORY_KEY,extractValue(metadata, SdcPropertyNames.PROPERTY_NAME_CATEGORY));
	}
	
	public SdncBaseModel(ISdcCsarHelper sdcCsarHelper, IEntityDetails entityDetails) {

		params = new HashMap<String, String>();
		attributeValueParams = new HashMap<String, String>();
		this.sdcCsarHelper = sdcCsarHelper;
		this.entityDetails = entityDetails;

		// extract common nodeTemplate metadata
		Metadata metadata = entityDetails.getMetadata();
		customizationUUID = extractValue (metadata, SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
		invariantUUID = extractValue (metadata, SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID);
		addParameter(PARAM_INVARIANT_UUID_KEY, invariantUUID);
		UUID = extractValue (metadata, SdcPropertyNames.PROPERTY_NAME_UUID);
		addParameter(PARAM_UUID_KEY, UUID);
		addParameter(PARAM_VERSION_KEY, extractValue (metadata, SdcPropertyNames.PROPERTY_NAME_VERSION));
	}

	
/*	This is the generic approach Shoujit attempted for 18.06 but can't be implemented without parser API to 
 *  get properties with substring match on the name
 * protected void extractRelevantAttributeData(List<Property> propList, SdncUebConfiguration config) {

		//List<Property> propList = nodeTemplate.getPropertiesObjects();
		for (Property prop : propList) {
			String propName = prop.getName();
			Object propValue = prop.getValue();
			
			if (propValue instanceof Map)
			
			LOG.info("Property: propertyName: " + propName + " propertyValue: " + propValue.toString());
			
			// Compare this property name with each config.relevant-attribute-name
			List<String> attributeNames =  config.getRelevantAttributeNames();
			for (String attributeName : attributeNames) {
				if (prop.getName().contains(attributeName))
				addParameter(prop.getName(), prop.getValue().toString(), attributeValueParams);
			}			

		}

	}*/
	
	protected void insertRelevantAttributeData() throws IOException{
		
		insertRelevantAttributeData("");
	}

	protected void insertRelevantAttributeData(String type) throws IOException{
		
		// type can be passed as "group" or taken from the nodeTemplate
		String metadataType = "";
		if (!type.isEmpty()) metadataType = type;
		else {
			Metadata metadata = entityDetails.getMetadata();
			metadataType = extractValue (metadata, PARAM_TYPE_KEY);
		}
		
		// Clean up all attributes for this resource
		try {
			cleanUpExistingToscaData("ATTRIBUTE_VALUE_PAIR", "resource_uuid", getUUID(), "resource_type", "\"" + metadataType + "\"");
		} catch (IOException e) {
			LOG.error("Could not cleanup Tosca CSAR data from the ATTRIBUTE_VALUE_PAIR table");
			throw new IOException (e);
		}
		
		for (String paramName : attributeValueParams.keySet()) {
			String paramValue = attributeValueParams.get(paramName);
			
			Map<String, String> attributeParams = new HashMap<String, String>();
			addParameter("attribute_name", paramName, attributeParams);
			addParameter("attribute_value", paramValue, attributeParams);			
			addParameter("resource_type", metadataType, attributeParams);
			addParameter("resource_customization_uuid", getCustomizationUUID(), attributeParams);
	
			LOG.info("Call insertToscaData for ATTRIBUTE_VALUE_PAIR where resource_uuid = " + getUUID() + " and attribute_name = \"" + paramName + "\"");
			try {
				insertToscaData(buildSql("ATTRIBUTE_VALUE_PAIR", "resource_uuid", getUUID(), model_yaml, attributeParams), null);
			} catch (IOException e) {
				LOG.error("Could not insert Tosca CSAR data into the ATTRIBUTE_VALUE_PAIR table");
				throw new IOException (e);
			}
		}		
	}
	
	protected void insertEntityGroupData (IEntityDetails entityDetails, IEntityDetails targetNode, String groupType) throws IOException {
		
		// Get the Groups on a node - Convert to use getEntity in 19.08
		EntityQuery entityQuery = EntityQuery.newBuilder(groupType).build();
		String customizationUuid = getCustomizationUUIDNoQuotes();
		SdcTypes entitySdcType = SdcTypes.valueOf(extractValue(entityDetails.getMetadata(), SdcPropertyNames.PROPERTY_NAME_TYPE));
		TopologyTemplateQuery topologyTemplateQuery = TopologyTemplateQuery.newBuilder(entitySdcType)
				.customizationUUID(customizationUuid).build();
		List<IEntityDetails> groupList = sdcCsarHelper.getEntity(entityQuery, topologyTemplateQuery, false);
		if (groupList == null) {
			return;
		}

		for (IEntityDetails group : groupList){
			
			// Insert into RESOURCE_GROUP/ATTRIBUTE_VALUE_PAIR and RESOURCE_GROUP_TO_TARGET_NODE_MAPPING
			// RESOURCE_GROUP (group metadata): resource_uuid (CR node UUID), uuid, customization_uuid, invariant_uuid, name, version
			// ATTRIBUTE_VALUE_PAIR (group properties): group_type, group_role, group_function
			// RESOURCE_GROUP_TO_TARGET_NODE_MAPPING: group_uuid, parent_uuid (CR node UUID), target_node_uuid, target_type, table_name

			SdncGroupModel groupModel = new SdncGroupModel (sdcCsarHelper, group, entityDetails, config, jdbcDataSource);	
			String resourceUuid = getUUID();
			groupModel.insertGroupData(resourceUuid);
			
			// insert RESOURCE_GROUP_TO_TARGET_NODE_MAPPING
			try {
				Map<String, String> mappingCleanupParams = new HashMap<String, String>();
				addParameter("group_uuid", groupModel.getUUID(), mappingCleanupParams); 
				addParameter("parent_uuid", extractValue(entityDetails.getMetadata(), SdcPropertyNames.PROPERTY_NAME_UUID), mappingCleanupParams);
				addParameter("target_node_uuid", extractValue(targetNode.getMetadata(), SdcPropertyNames.PROPERTY_NAME_UUID), mappingCleanupParams);
				cleanupExistingToscaData("RESOURCE_GROUP_TO_TARGET_NODE_MAPPING", mappingCleanupParams);
				
				Map<String, String> mappingParams = new HashMap<String, String>();
				addParameter("parent_uuid", extractValue(entityDetails.getMetadata(), SdcPropertyNames.PROPERTY_NAME_UUID), mappingParams);
				addParameter("target_node_uuid", extractValue(targetNode.getMetadata(), SdcPropertyNames.PROPERTY_NAME_UUID), mappingParams);
				String targetType = extractValue(targetNode.getMetadata(), PARAM_TYPE_KEY);
				addParameter("target_type", targetType, mappingParams);
				String tableName = "";
				switch (targetType) {
				case "CVFC":
					tableName = "VFC_MODEL";
					break;
				case "VL":
					tableName = "NETWORK_MODEL";
					break;
				}					
				addParameter("table_name", tableName, mappingParams);  
				LOG.info("Call insertToscaData for RESOURCE_GROUP_TO_TARGET_NODE_MAPPING where group_uuid = " + groupModel.getUUID());
				insertToscaData(buildSql("RESOURCE_GROUP_TO_TARGET_NODE_MAPPING", "group_uuid", groupModel.getUUID(), model_yaml, mappingParams), null);

			} catch (IOException e) {
				LOG.error("Could not insert Tosca CSAR data into the RESOURCE_GROUP_TO_TARGET_NODE_MAPPING");
				throw new IOException (e);
			}			
		}		
	}
	
	protected void insertEntityPolicyData (String nodeTemplateCustomizationUuid, String nodeTemplateUuid, SdcTypes queryType, String targetCustomizationUuid, String targetUuid, String targetType, String policyType) throws IOException {
		
		EntityQuery policyEntityQuery = EntityQuery.newBuilder(policyType).build();
	    TopologyTemplateQuery topologyTemplateQuery = TopologyTemplateQuery.newBuilder(queryType).customizationUUID(nodeTemplateCustomizationUuid).build();
	    List<IEntityDetails> policyEntities = sdcCsarHelper.getEntity(policyEntityQuery, topologyTemplateQuery, false);
		if (policyEntities == null || policyEntities.isEmpty()) {
			LOG.debug("insertPolicyData: Could not find policy data for: " + nodeTemplateCustomizationUuid);
			return;
		}
		
		String resourceUuid = getUUID();

		for (IEntityDetails policyEntity : policyEntities) {
			
			// extract policy metadata
			String policyUuid = extractValue(policyEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_UUID);
			String policyCustomizationUuid = extractValue(policyEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);

			insertResourcePolicyData(policyEntity, resourceUuid);
			insertResourcePolicyToTargetNodeMappingData(policyUuid, nodeTemplateUuid, targetUuid, targetCustomizationUuid, policyCustomizationUuid, targetType);	
		}
	}
	
	public void insertEntityPolicyData (String resourceCustomizationUuid, String resourceUuid, String parentUuid, String policyType, SdcTypes queryType) throws IOException {
		
		EntityQuery policyEntityQuery = EntityQuery.newBuilder(policyType).build();
		TopologyTemplateQuery topologyTemplateQuery;
		if (queryType == SdcTypes.VF) {
			topologyTemplateQuery = TopologyTemplateQuery.newBuilder(queryType).customizationUUID(resourceCustomizationUuid).build();
		} else {
			topologyTemplateQuery = TopologyTemplateQuery.newBuilder(queryType).build();
		}
		
		List<IEntityDetails> policyEntities = sdcCsarHelper.getEntity(policyEntityQuery, topologyTemplateQuery, false);
		if (policyEntities == null || policyEntities.isEmpty()) {
			LOG.debug("insertPolicyData: Could not find policy data for Service/VF: " + resourceUuid);
			return;
		}		
	
		for (IEntityDetails policyEntity : policyEntities) {
			
			// extract policy metadata
			String policyUuid = extractValue(policyEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_UUID);
			String policyCustomizationUuid = extractValue(policyEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);

			insertResourcePolicyData(policyEntity, resourceUuid);
			List<IEntityDetails> targetEntities = policyEntity.getTargetEntities();
			if (targetEntities == null || targetEntities.isEmpty()) {
				LOG.debug("insertPolicyData: Could not find targetEntites for policy: " + policyUuid);
				continue;
			}		
			
			for (IEntityDetails targetEntity : targetEntities) {
		
				String targetUuid = extractValue(targetEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_UUID);
				String targetCustomizationUuid = extractValue(targetEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
				String targetType = extractValue(targetEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_TYPE);
				insertResourcePolicyToTargetNodeMappingData(policyUuid, parentUuid, targetUuid, targetCustomizationUuid, policyCustomizationUuid, targetType);
			}
		}
	}
	
	protected void insertResourcePolicyData (IEntityDetails policyEntity, String resourceUuid) throws IOException {	
		
		// extract policy metadata
		String policyUuid = extractValue(policyEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_UUID);
		String policyInvariantUuid = extractValue(policyEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID);
		String policyCustomizationUuid = extractValue(policyEntity.getMetadata(), SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
		
		// cleanup existing RESOURCE_POLICY data
		Map<String, String> cleanupParams = new HashMap<String, String>();
		addParameter("resource_uuid", resourceUuid, cleanupParams); 
		addParameter("policy_uuid", policyUuid, cleanupParams);
		addParameter("policy_invariant_uuid", policyInvariantUuid, cleanupParams);
		
		// insert into RESOURCE_POLICY
		Map<String, String> policyParams = new HashMap<String, String>();
		addParameter("policy_uuid", policyUuid, policyParams);
		addParameter("policy_customization_uuid", policyCustomizationUuid, policyParams);
		addParameter("policy_invariant_uuid", policyInvariantUuid, policyParams);
		addParameter("policy_name", extractValue(policyEntity.getMetadata(), PARAM_NAME_KEY), policyParams);
		addParameter(PARAM_VERSION_KEY, extractValue(policyEntity.getMetadata(), PARAM_VERSION_KEY), policyParams);
		addParameter("policy_type", policyEntity.getToscaType(), policyParams);
		
		// extract properties
		addParameter("property_type", extractValue(policyEntity, PARAM_TYPE_KEY), policyParams);
		addParameter("property_source", extractValue(policyEntity, "source"), policyParams);
		addParameter("property_name", extractValue(policyEntity, PARAM_NAME_KEY), policyParams);

		// Insert into RESOURCE_POLICY and RESOURCE_POLICY_TO_TARGET_NODE_MAPPING
		// RESOURCE_POLICY: resource_uuid (CR node UUID), uuid, customization_uuid, invariant_uuid, name, version, policy_type, 
		// property_type, property_source, property_name
		
		try {
			
			// insert into RESOURCE_POLICY
			cleanupExistingToscaData("RESOURCE_POLICY", cleanupParams);
			LOG.info("Call insertToscaData for RESOURCE_POLICY where resource_uuid = " + resourceUuid + " and policy_uuid = " + "\"" + policyUuid + "\"" );
			insertToscaData(buildSql("RESOURCE_POLICY", "resource_uuid", resourceUuid, model_yaml, policyParams), null);

		} catch (IOException e) {
			LOG.error("Could not insert Tosca CSAR data into the RESOURCE_POLICY table");
			throw new IOException (e);
		}
	
	}
	
	protected void insertResourcePolicyToTargetNodeMappingData(String policyUuid, String parentUuid, String targetUuid, String targetCustomizationUuid, String policyCustomizationUuid, String targetType) throws IOException {
		
		// insert RESOURCE_POLICY_TO_TARGET_NODE_MAPPING: policy_uuid, parent_uuid (CR node UUID), target_node_uuid, target_type, table_name
		try {
			Map<String, String> mappingCleanupParams = new HashMap<String, String>();
			addParameter("policy_uuid", policyUuid, mappingCleanupParams); 
			addParameter("parent_uuid", parentUuid, mappingCleanupParams);
			addParameter("target_node_uuid", targetUuid, mappingCleanupParams);
			cleanupExistingToscaData("RESOURCE_POLICY_TO_TARGET_NODE_MAPPING", mappingCleanupParams);
			
			Map<String, String> mappingParams = new HashMap<String, String>();
			addParameter("parent_uuid", parentUuid, mappingParams);
			addParameter("target_node_uuid", targetUuid, mappingParams);
			addParameter("target_node_customization_uuid", targetCustomizationUuid, mappingParams);
			addParameter("policy_customization_uuid", policyCustomizationUuid, mappingParams);
			addParameter("target_type", targetType, mappingParams);
			LOG.info("Call insertToscaData for RESOURCE_POLICY_TO_TARGET_NODE_MAPPING where policy_uuid = " + "\"" + policyUuid + "\" and parent_uuid = " + "\"" + parentUuid + "\" and target_node_uuid = " + "\"" + targetUuid + "\"");
			insertToscaData(buildSql("RESOURCE_POLICY_TO_TARGET_NODE_MAPPING", "policy_uuid", "\"" + policyUuid + "\"", model_yaml, mappingParams), null);

		} catch (IOException e) {
			LOG.error("Could not insert Tosca CSAR data into the RESOURCE_POLICY_TO_TARGET_NODE_MAPPING");
			throw new IOException (e);
		}			
	}

		
	protected void insertNodeCapabilitiesData (CapabilityAssignments capabilities) throws IOException {		
		
		// Process the capabilities on the node template
		
		List<CapabilityAssignment> capabilityList = capabilities.getAll();
		
		for (CapabilityAssignment capability : capabilities.getAll()) {
							
			// Insert into NODE_CAPABILITY: 
			// capability_id (generated) 
			// capability_provider_uuid - UUID of this node 
			// capability_provider_customization_uuid - customization UUID of this node
			// capability_name - capability.getName()
			// capability_type - ?

			// Check capability name against relevant capabilities
			boolean capabilityIsRelevant = false;
			/*List<String> relevantCapabilities = config.getRelevantCapabilityNames();
			for (String relevantCapabilityName : relevantCapabilities ) {
				
				if (capability.getName().toLowerCase().contains(relevantCapabilityName.toLowerCase())) {
					capabilityIsRelevant = true;
				}
			}*/
			
			if (capabilityIsRelevant == false){
				continue;
			}
			
			String capabilityProviderUuid = getUUID(); 

			Map<String, String> cleanupParams = new HashMap<String, String>();
			addParameter("capability_provider_uuid", capabilityProviderUuid, cleanupParams);  // node customization UUID
			addParameter("capability_provider_customization_uuid", getCustomizationUUIDNoQuotes(), cleanupParams);  // node customization UUID
			addParameter("capability_name", capability.getName(), cleanupParams);

			Map<String, String> nodeCapabilityParams = new HashMap<String, String>();
			addParameter("capability_provider_customization_uuid", getCustomizationUUIDNoQuotes(), nodeCapabilityParams);  // node customization UUID
			addParameter("capability_name", capability.getName(), nodeCapabilityParams);
			addParameter("capability_type", extractValue(capability, PARAM_TYPE_KEY), nodeCapabilityParams);
			
			// Insert NODE_CAPABILITY data for each capability
			String capabilityId = "";
			try {

				cleanupExistingToscaData("NODE_CAPABILITY", cleanupParams); // will also delete NODE_CAPABILITY_PROPERTY with same capability_id
				LOG.info("Call insertToscaData for NODE_CAPABILITY where capability_provider_uuid = " + capabilityProviderUuid + " and capability_name = \"" + capability.getName() + "\"");
				insertToscaData(buildSql("NODE_CAPABILITY", "capability_provider_uuid", capabilityProviderUuid, model_yaml, nodeCapabilityParams), null);
				
				// Get capabilityId for capability just inserted
				CachedRowSet rowData = getToscaData("NODE_CAPABILITY", nodeCapabilityParams);
				rowData.first();
				int capabilityIdint = rowData.getInt("capability_id");
				capabilityId = String.valueOf(capabilityIdint);
				
			} catch (IOException | SQLException e) {
				LOG.error("Could not insert Tosca CSAR data into the NODE_CAPABILITY table");
				throw new IOException (e);
			}

			insertNodeCapabilityPropertyData (capability, capabilityId);
		}
	}
	
	protected void insertNodeCapabilitiesEntityData (List<CapabilityAssignment> capabilities) throws IOException {		
		
		// Process the capabilities		
		for (CapabilityAssignment capability :  capabilities) {
			
			// Insert into NODE_CAPABILITY: 
			// capability_id (generated) 
			// capability_provider_uuid - UUID of this node 
			// capability_provider_customization_uuid - customization UUID of this node
			// capability_name - capability.getName()
			// capability_type - ?

			// Check capability name against relevant capabilities
			boolean capabilityIsRelevant = false;
			/*List<String> relevantCapabilities = config.getRelevantCapabilityNames();
			for (String relevantCapabilityName : relevantCapabilities ) {
				
				if (capability.getName().toLowerCase().contains(relevantCapabilityName.toLowerCase())) {
					capabilityIsRelevant = true;
				}
			}*/
			
			if (capabilityIsRelevant == false){
				continue;
			}
			
			String capabilityProviderUuid = getUUID(); 

			Map<String, String> cleanupParams = new HashMap<String, String>();
			addParameter("capability_provider_uuid", capabilityProviderUuid, cleanupParams);  // node customization UUID
			addParameter("capability_provider_customization_uuid", getCustomizationUUIDNoQuotes(), cleanupParams);  // node customization UUID
			addParameter("capability_name", capability.getName(), cleanupParams);

			Map<String, String> nodeCapabilityParams = new HashMap<String, String>();
			addParameter("capability_provider_customization_uuid", getCustomizationUUIDNoQuotes(), nodeCapabilityParams);  // node customization UUID
			addParameter("capability_name", capability.getName(), nodeCapabilityParams);
			addParameter("capability_type", extractValue(capability, PARAM_TYPE_KEY), nodeCapabilityParams);
			
			// Insert NODE_CAPABILITY data for each capability
			String capabilityId = "";
			try {

				cleanupExistingToscaData("NODE_CAPABILITY", cleanupParams); // will also delete NODE_CAPABILITY_PROPERTY with same capability_id
				LOG.info("Call insertToscaData for NODE_CAPABILITY where capability_provider_uuid = " + capabilityProviderUuid + " and capability_name = \"" + capability.getName()+ "\"");
				insertToscaData(buildSql("NODE_CAPABILITY", "capability_provider_uuid", capabilityProviderUuid, model_yaml, nodeCapabilityParams), null);
				
				// Get capabilityId for capability just inserted
				CachedRowSet rowData = getToscaData("NODE_CAPABILITY", nodeCapabilityParams);
				rowData.first();
				int capabilityIdint = rowData.getInt("capability_id");
				capabilityId = String.valueOf(capabilityIdint);
				
			} catch (IOException | SQLException e) {
				LOG.error("Could not insert Tosca CSAR data into the NODE_CAPABILITY table");
				throw new IOException (e);
			}

			insertNodeCapabilityPropertyData (capability, capabilityId);
		}
	}
	
	protected void insertNodeCapabilityPropertyData(CapabilityAssignment capability, String capabilityId) throws IOException {
		
		// Insert property name / value into NODE_CAPABILITY_PROPERTY
		LinkedHashMap<String, Property> propertiesMap = capability.getProperties();
		Map<String, String> nodeCapabilityPropertyParams = new HashMap<String, String>();
		
		for (String propertyMapKey : propertiesMap.keySet() ) {
			//LOG.info("property map key = " + propertyMapKey);				
			Property property = propertiesMap.get(propertyMapKey);
			
			addParameter ("capability_property_name", property.getName(), nodeCapabilityPropertyParams);
			addParameter ("capability_property_type", property.getValue().toString(), nodeCapabilityPropertyParams);
			
			try {
				// Data from NODE_CAPABILITY_PROPERTY is cleaned up via cascade delete on NODE_CAPABILITY  
				LOG.info("Call insertToscaData for NODE_CAPABILITY_PROPERTY where capability_id = \"" + capabilityId + "\" and capability_property_name = \"" + property.getName() + "\" and capability_property_type =  \"" + property.getValue().toString() + "\"");
				insertToscaData(buildSql("NODE_CAPABILITY_PROPERTY", "capability_id", capabilityId, model_yaml, nodeCapabilityPropertyParams), null);
			} catch (IOException e) {
				LOG.error("Could not insert Tosca CSAR data into the NODE_CAPABILITY_PROPERTY table");
				throw new IOException (e);
			}
		}

	}

	protected void addParameter (String name, String value) {
		if (value != null && !value.isEmpty()) {
			// check if value already contain quotes
			if (value.startsWith("\"", 0) && value.endsWith("\"")) {
				params.put(name, value);
			} else {
				params.put(name, "\"" + value + "\"");
			}
		}
	}

	protected void addIntParameter (String name, String value) {
		if (value != null && !value.isEmpty()) {
			params.put(name, value);
		}
	}

	public static void addParameter (String name, String value, Map<String, String> params) {
		if (value != null && !value.isEmpty()) {
			// remove any quotes within the string
			String strippedValue = value.replace("\"","");

			// check if value already contain quotes
			if (strippedValue.startsWith("\"", 0) && value.endsWith("\"")) {
				params.put(name, strippedValue);
			} else {
				params.put(name, "\"" + strippedValue + "\"");
			}
		}
	}

	protected String extractValue (Metadata metadata, String name) {
		String value = metadata.getValue(name);
		if (value != null) {
			return value;
		} else {
			return "";
		}	
	}

	protected String extractBooleanValue (Metadata metadata, String name) {
		String value = metadata.getValue(name);
		if (value != null && !value.isEmpty()) {
			return value.contains("true") ? "Y" : "N";
		} else {
			return "";
		}
	}

	public static String extractValue (ISdcCsarHelper sdcCsarHelper, Metadata metadata, String name) {
		String value = metadata.getValue(name);
		if (value != null) {
			return value;
		} else {
			return "";
		}
	}

	protected String extractValue (IEntityDetails  entityDetails, String name) {
		String value = ""; 
		if (entityDetails.getProperties().containsKey(name)) {
			Property property = entityDetails.getProperties().get(name);
			if (property != null && property.getValue() != null) {
				value = property.getValue().toString();
			}
		}
		
		if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
			return value;
		} else {
			return "";
		}
	}

	protected String extractValue (IEntityDetails  entityDetails, String path, String name) {
		String value = ""; 
		
		if (entityDetails.getProperties().containsKey(path)) {
			Property property = entityDetails.getProperties().get(path);
			if (property != null && !property.getLeafPropertyValue(name).isEmpty()) {
				value = property.getLeafPropertyValue(name).get(0);
			}
		}			

		if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
			return value;
		} else {
			return "";
		}
	}

	protected String extractValue (IEntityDetails  entityDetails, String path1, String path2, String name) {
		String value = ""; 
		
		value = extractNestedValue (entityDetails, path1, path2, name);		

		if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
			return value;
		} else {
			return "";
		}
	}
	
	protected String extractValue (IEntityDetails  entityDetails, String path1, String path2, String path3, String name) {
		String value = ""; 
		
		value = extractNestedValue (entityDetails, path1, path2, path3, name);		

		if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
			return value;
		} else {
			return "";
		}
	}
	
	protected String extractBooleanValue (IEntityDetails  entityDetails, String path1, String path2, String name) {
		String value = ""; 
		
		value = extractNestedValue (entityDetails, path1, path2, name);				
		
		if (value != null && !value.isEmpty()) {
			return value.contains("true") ? "Y" : "N";
		} else {
			return "";
		}
	}
	
	protected String extractNestedValue (IEntityDetails  entityDetails, String path1, String path2, String name) {
		String value = ""; 
		
		if (entityDetails.getProperties().containsKey(path1)) {
			Property path1Property = entityDetails.getProperties().get(path1);
			if (path1Property != null) {
				Map<String, Object> path1PropertyValue = (Map<String, Object>) path1Property.getValue();
				if (path1PropertyValue.containsKey(path2)) {
					Map<String, Object> path2PropertyValue = (Map<String, Object>) path1PropertyValue.get(path2);
					if (path2PropertyValue != null && path2PropertyValue.containsKey(name)) {
						value = path2PropertyValue.get(name).toString();
					}
				}
			}
		}			

		return value;
	}

	protected String extractNestedValue (IEntityDetails  entityDetails, String path1, String path2, String path3, String name) {
		String value = ""; 
		
		if (entityDetails.getProperties().containsKey(path1)) {
			Property path1Property = entityDetails.getProperties().get(path1);
			if (path1Property != null) {
				Map<String, Object> path1PropertyValue = (Map<String, Object>) path1Property.getValue();
				if (path1PropertyValue.containsKey(path2)) {
					Map<String, Object> path2PropertyValue = (Map<String, Object>) path1PropertyValue.get(path2);
					if (path2PropertyValue != null && path2PropertyValue.containsKey(path3)) {
						Map<String, Object> path3PropertyValue = (Map<String, Object>) path2PropertyValue.get(path3);
						if (path3PropertyValue != null && path3PropertyValue.containsKey(name)) {
							value = path3PropertyValue.get(name).toString();
						}
					}
				}
			}
		}			

		return value;
	}

	protected String extractIntegerValue (Property property, String name) {
		String value = ""; 
		
		if (!property.getLeafPropertyValue(name).isEmpty()) {
			value = property.getLeafPropertyValue(name).get(0);
		}
		
		if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
			return value;
		} else {
			return "";
		}
	}
	
	protected String extractGetInputValue (IEntityDetails group, IEntityDetails entityDetails, String name) {

		// Extract the property on entityDetails after getting the property name from the group
		String getInputName = extractGetInputName (group, name);
		String entityPropertyValue = "";
		
		if (entityDetails.getProperties().containsKey(getInputName)) {
			Property entityProperty = entityDetails.getProperties().get(getInputName);
			entityPropertyValue = entityProperty.getValue().toString();
		}
		
		if (!entityPropertyValue.isEmpty()) {
			return entityPropertyValue;
		} else {
			return "";
		}
	}

	protected String extractGetInputName (IEntityDetails group, String name) {
		
		String getInputName = name;
		String groupPropertyValue = "";
		if (group.getProperties().containsKey(name)) {
			Property groupProperty = group.getProperties().get(name);
			groupPropertyValue = groupProperty.getValue().toString();
		}
		if (!groupPropertyValue.isEmpty()) {
			int getInputIndex = groupPropertyValue.indexOf("{get_input=");
			if (getInputIndex > -1) {
				getInputName = groupPropertyValue.substring(getInputIndex+11, groupPropertyValue.length()-1);
			}
		}
		
		return getInputName;

	}

	protected String extractValue (CapabilityAssignment capability, String name) {
		String value = "";
		
		if (capability.getProperties().containsKey(name)) {
			Property property = capability.getProperties().get(name);
			value = property.getLeafPropertyValue(name).get(0);
		}			
		
		if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
			return value;
		} else {
			return "";
		}
	}

	protected String extractValue (CapabilityAssignment  capability, String path, String name) {
		String value = ""; 
		
		if (capability.getProperties().containsKey(path)) {
			Property property = capability.getProperties().get(path);
			if (property != null && !property.getLeafPropertyValue(name).isEmpty()) {
				value = property.getLeafPropertyValue(name).get(0);
			}
		}			

		if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
			return value;
		} else {
			return "";
		}
	}

	protected String extractBooleanValue (IEntityDetails entityDetails, String name) {
		String value = null; 
		if (entityDetails.getProperties().containsKey(name)) {
			Property property = entityDetails.getProperties().get(name);
			if (property != null && property.getValue() != null) {
				value = property.getValue().toString();
			}
		}
		
		if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
			return value.contains("true") ? "Y" : "N";
		} else {
			return "";
		}
	}

	protected String extractBooleanValue (IEntityDetails entityDetails, String path, String name) {
		String value = null; 
		if (entityDetails.getProperties().containsKey(path)) {
			Property property = entityDetails.getProperties().get(path);
			if (property != null) {
				if (property != null && !property.getLeafPropertyValue(name).isEmpty()) {
					value = property.getLeafPropertyValue(name).get(0);
				}	
			}
		}
		
		if (value != null && !value.isEmpty() && !value.equalsIgnoreCase("null")) {
			return value.contains("true") ? "Y" : "N";
		} else {
			return "";
		}
	}

	protected Object extractObjectValue (IEntityDetails entityDetails, String name) {
		Object value = ""; 
		if (entityDetails.getProperties().containsKey(name)) {
			Property property = entityDetails.getProperties().get(name);
			if (property != null && property.getValue() != null) {
				value = property.getValue();
			}
		}
		
		if (value != null) {
			return value;
		} else {
			return "";
		}
	}

	public static String extractSubstitutionMappingTypeName (ISdcCsarHelper sdcCsarHelper) {
		String value = sdcCsarHelper.getServiceSubstitutionMappingsTypeName();
		if (value != null) {
			return value;
		} else {
			return "";
		}
	}

	protected String getUUID() {
		return ("\"" + UUID + "\"");
	}
	public String getInvariantUUID() {
		return ("\"" + invariantUUID + "\"");
	}
	public String getCustomizationUUID() {
		return ("\"" + customizationUUID + "\"");
	}
	public String getCustomizationUUIDNoQuotes() {
		return (customizationUUID);
	}
	public void setCustomizationUUID(String customizationUUID) {
		this.customizationUUID = customizationUUID;
	}
	public String getName() {
		return name;
	}
	
	public String buildSql(String tableName, String model_yaml) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT into " + tableName + " (customization_uuid, model_yaml, ");

		int paramCount = 0;
		for (String paramKey :  params.keySet()) {
			paramCount++;
		    sb.append(paramKey);
		    if (paramCount < params.size()) sb.append(", ");
		}

		sb.append(") values (" + getCustomizationUUID() + ", \"" + model_yaml + "\", ");

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
	
	public String buildSql(String tableName, String keyName, String keyValue, String model_yaml, Map<String, String> params) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT into " + tableName + " (" + keyName + ", ");
		
		int paramCount = 0;
		for (String paramKey :  params.keySet()) {
			paramCount++;
		    sb.append(paramKey);
		    if (paramCount < params.size()) sb.append(", ");
		}
		
		sb.append(") values (" + keyValue + ", ");

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
	
	public static String getSql(String tableName, String keyName, String keyValue, String model_yaml, Map<String, String> params) {

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT into " + tableName + " (" + keyName + ", ");

		int paramCount = 0;
		for (String paramKey :  params.keySet()) {
			paramCount++;
		    sb.append(paramKey);
		    if (paramCount < params.size()) sb.append(", ");
		}

		sb.append(") values (" + keyValue + ", ");

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
	
	 protected void insertToscaData(String toscaDataString, ArrayList<String> arguments) throws IOException
     {
            LOG.debug("insertToscaData: " + toscaDataString);

             try {

 				jdbcDataSource.writeData(toscaDataString, arguments, null);

			} catch (SQLException e) {
				LOG.error("Could not insert Tosca data into the database");
				throw new IOException (e);
			}

     }
	 
	protected void cleanUpExistingToscaData(String tableName, String keyName, String keyValue) throws IOException
     {

             try {
            	int rowCount = 0;
            	CachedRowSet data = jdbcDataSource.getData("SELECT * from " + tableName + " where " + keyName + " = " + keyValue + ";", null, "");
            	if (data != null) {
            		while(data.next()) {
     				rowCount ++;
            		}
            		if (rowCount != 0) {
                    	LOG.debug("cleanUpExistingToscaData from: " + tableName + " for " + keyValue);
               			jdbcDataSource.writeData("DELETE from " + tableName + " where " + keyName + " = " + keyValue + ";", null, null);
            		}
		}

			} catch (SQLException e) {
				LOG.error("Could not clean up existing " + tableName  + " for " + keyValue, e);
			}

     }
	
	protected void cleanUpExistingToscaData(String tableName, String key1Name, String key1Value, String key2Name, String key2Value) throws IOException
    {

            try {
           	int rowCount = 0;
           	CachedRowSet data = jdbcDataSource.getData("SELECT * from " + tableName + " where " + key1Name + " = " + key1Value + " AND " + key2Name + " = " + key2Value + ";", null, "");
           	if (data != null) {
           		while(data.next()) {
    				rowCount ++;
           		}
           		if (rowCount != 0) {
                   	LOG.debug("cleanUpExistingToscaData from : " + tableName + " for " + key1Value + " and " + key2Value);
              			jdbcDataSource.writeData("DELETE from " + tableName + " where " + key1Name + " = " + key1Value + " AND " + key2Name + " = " + key2Value + ";", null, null);
           		}
		}

			} catch (SQLException e) {
				LOG.error("Could not clean up existing " + tableName  + " for " + key1Value  + " and " + key2Value, e);
			}

    }
	
	protected boolean cleanupExistingToscaData(String tableName, Map<String, String> keyParams) throws IOException
    {
			return SdncBaseModel.cleanupExistingToscaData(SdncBaseModel.jdbcDataSource, tableName, keyParams);
    }
	
	public static boolean cleanupExistingToscaData(DBResourceManager jdbcDataSource, String tableName, Map<String, String> keyParams) throws IOException
    {
		boolean dataExists = false;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * from " + tableName + " where ");
		
		int paramCount = 0;
		for (String paramKey :  keyParams.keySet()) {
			paramCount++;
			String paramValue = keyParams.get(paramKey);
		    sb.append(paramKey);
		    sb.append(" = ");
		    sb.append(paramValue);
		    if (paramCount < keyParams.size()) sb.append(" AND ");
		}

		sb.append(";");
	
        try {
       	int rowCount = 0;
       	CachedRowSet data = jdbcDataSource.getData(sb.toString(), null, "");
       	while(data.next()) {
				rowCount ++;
				data.deleteRow();
       	}
       	if (rowCount != 0) {
               LOG.debug("cleanupExistingToscaData in " + tableName + ": Data FOUND");
               String deleteStmt = sb.replace(sb.indexOf("SELECT *"), sb.indexOf("SELECT")+8, "DELETE").toString();
       		   jdbcDataSource.writeData(deleteStmt, null, null);
               dataExists = true;
       	}

		} catch (SQLException e) {
			LOG.error("Could not get data in " + tableName, e);
		}

        return dataExists;
    }
	
	protected boolean checkForExistingToscaData(String tableName, Map<String, String> keyParams) throws IOException
    {
			boolean dataExists = false;
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * from " + tableName + " where ");
			
			int paramCount = 0;
			for (String paramKey :  keyParams.keySet()) {
				paramCount++;
				String paramValue = keyParams.get(paramKey);
			    sb.append(paramKey);
			    sb.append(" = ");
			    sb.append(paramValue);
			    if (paramCount < keyParams.size()) sb.append(" AND ");
			}

			sb.append(";");
		
            try {
           	int rowCount = 0;
           	CachedRowSet data = jdbcDataSource.getData(sb.toString(), null, "");
           	while(data.next()) {
    				rowCount ++;
           	}
           	if (rowCount != 0) {
                   LOG.debug("checkForExistingToscaData in " + tableName + ": Data FOUND");
                   dataExists = true;
           	}

			} catch (SQLException e) {
				LOG.error("Could not get data in " + tableName, e);
			}

            return dataExists;
    }
	
	protected CachedRowSet getToscaData(String tableName, Map<String, String> keyParams) throws IOException
    {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * from " + tableName + " where ");
			
			int paramCount = 0;
			for (String paramKey :  keyParams.keySet()) {
				paramCount++;
				String paramValue = keyParams.get(paramKey);
			    sb.append(paramKey);
			    sb.append(" = ");
			    sb.append(paramValue);
			    if (paramCount < keyParams.size()) sb.append(" AND ");
			}

			sb.append(";");
		
			CachedRowSet data = null;
            try {
	           	int rowCount = 0;
	           	data = jdbcDataSource.getData(sb.toString(), null, "");
	           	while(data.next()) {
	    			rowCount ++;
	           	}
	           	if (rowCount == 0) {
	                LOG.info("getToscaData in " + tableName + ": Data NOT found");
	           	}

			} catch (SQLException e) {
				LOG.error("Could not get data in " + tableName, e);
			}

            return data;
    }
	
	protected void addParamsToMap (Map<String, String> fromMap, Map<String, String> toMap) {
		for (String key : fromMap.keySet()) {
		    if (!toMap.containsKey(key)) {
		    	toMap.put(key, fromMap.get(key));
		    }
		}
	}
	
	protected String nullCheck (Object extractedObject) {
		String stringValue = "";
		if (extractedObject != null) {
			return extractedObject.toString();
		}
		return stringValue;
	}

}
