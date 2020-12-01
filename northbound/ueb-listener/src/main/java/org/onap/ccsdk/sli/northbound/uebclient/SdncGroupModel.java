/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights
 * 						reserved.
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

import java.io.IOException;

import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdncGroupModel extends SdncBaseModel {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SdncGroupModel.class);

	private static final String groupType = "group_type";
	
	public SdncGroupModel(ISdcCsarHelper sdcCsarHelper, IEntityDetails group, IEntityDetails entityDetails, SdncUebConfiguration config, DBResourceManager jdbcDataSource) throws IOException {

		super(sdcCsarHelper, group);
		
		// Metadata for Resource group is not extracted in base class due to inconsistency in TOSCA model Group object
		Metadata metadata = group.getMetadata();
		params.remove("invariant_uuid"); // remove invariant_uuid which is added by base class, it does not apply for groups
		invariantUUID = extractValue (metadata, "invariantUUID");
		addParameter("group_invariant_uuid", invariantUUID);	
		params.remove("uuid"); // remove uuid which is added by base class, it does not apply for groups
		UUID = extractValue (metadata, "UUID");
		addParameter("group_uuid", UUID);	
		addParameter("group_name", extractValue (metadata, "name"));
		addParameter(groupType, group.getToscaType());
		addParameter("version", extractValue (metadata, "version"));
		
		// extract properties
		addParameter("vfc_parent_port_role", extractValue(group, "vfc_parent_port_role"), attributeValueParams);
		addParameter("subinterface_role", extractValue(group, "subinterface_role"), attributeValueParams);
		
		// relevant complex group properties are extracted and inserted into ATTRIBUTE_VALUE_PAIR 
		addParameter(extractGetInputName (group, groupType), extractGetInputValue(group, entityDetails, groupType), attributeValueParams);
		addParameter(extractGetInputName (group, "group_role"), extractGetInputValue(group, entityDetails, "group_role"), attributeValueParams);
		addParameter(extractGetInputName (group, "group_function"), extractGetInputValue(group, entityDetails, "group_function"), attributeValueParams);
	}
	
	public void insertGroupData(String resourceUuid) throws IOException {
	
		try {
			
			// insert into RESOURCE_GROUP/ATTRIBUTE_VALUE_PAIR
			cleanUpExistingToscaData("RESOURCE_GROUP", "resource_uuid", resourceUuid, "group_uuid", getUUID()) ;
			LOG.info("Call insertToscaData for RESOURCE_GROUP where group_uuid = " + getUUID() + " and resource_uuid = " + resourceUuid);
			insertToscaData(buildSql("RESOURCE_GROUP", "resource_uuid", resourceUuid, model_yaml, params), null);
			insertRelevantAttributeData("group");	

		} catch (IOException e) {
			LOG.error("Could not insert Tosca CSAR data into the RESOURCE_GROUP table");
			throw new IOException (e);
		}

	}

}
