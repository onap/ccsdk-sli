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

import java.io.IOException;
import java.util.List;

import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdncARModel extends SdncBaseModel {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SdncARModel.class);

  	private String type = null;
	private String subcategory = null;

	public SdncARModel(ISdcCsarHelper sdcCsarHelper, IEntityDetails arEntity, DBResourceManager jdbcDataSource, SdncUebConfiguration config) throws IOException {

		super(sdcCsarHelper, arEntity, jdbcDataSource, config);
		
		// extract metadata
		Metadata metadata = arEntity.getMetadata();
		type = extractValue (metadata, SdcPropertyNames.PROPERTY_NAME_TYPE);
		subcategory = extractValue (metadata, "subcategory");
		addParameter("type", extractValue (metadata, SdcPropertyNames.PROPERTY_NAME_TYPE));

		// extract properties
		addParameter("role", extractValue (arEntity, "nf_role"));
		addParameter("type", extractValue (arEntity, "nf_type"));
		addParameter("ecomp_generated_naming", extractBooleanValue (arEntity, "nf_naming", "ecomp_generated_naming"));
		addParameter("naming_policy", extractValue (arEntity, "nf_naming", "naming_policy"));
	}

	public void insertAllottedResourceModelData () throws IOException {
		try {
			cleanUpExistingToscaData("ALLOTTED_RESOURCE_MODEL", "customization_uuid", getCustomizationUUID());
			LOG.info("Call insertToscaData for ALLOTTED_RESOURCE_MODEL where customization_uuid = " + getCustomizationUUID());
			insertToscaData(buildSql("ALLOTTED_RESOURCE_MODEL", model_yaml), null);
		} catch (IOException e) {
			LOG.error("Could not insert Tosca CSAR data into the ALLOTTED_RESOURCE_MODEL table");
			throw new IOException (e);
		}
	}
	
	public void insertAllottedResourceVfcModelData () throws IOException {
		
		// Insert the child VFCs (not CVFC) into VFC_MODEL
		String vfCustomizationUuid = getCustomizationUUID().replace("\"", "");
		EntityQuery vfcEntityQuery = EntityQuery.newBuilder(SdcTypes.VFC).build();
		TopologyTemplateQuery vfcTopologyTemplateQuery = TopologyTemplateQuery.newBuilder(SdcTypes.VF)
				.customizationUUID(vfCustomizationUuid)
				.build();
		List<IEntityDetails> nestedVfcs  = sdcCsarHelper.getEntity(vfcEntityQuery, vfcTopologyTemplateQuery, true);  // true allows for nested search
		if (nestedVfcs == null || nestedVfcs.isEmpty()) {
			LOG.info("Could not find the nested VFCs for: " + vfCustomizationUuid);
		}				
    	
		for (IEntityDetails nestedVfc: nestedVfcs) {
			try {
				SdncVFCModel arVfcModel = new SdncVFCModel (sdcCsarHelper, nestedVfc, jdbcDataSource, config);
				arVfcModel.insertVFCModelData();
			} catch (IOException e) {
				LOG.info("Could not find the nested VFCs for: " + vfCustomizationUuid);
			}	
		}
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
	}

}
