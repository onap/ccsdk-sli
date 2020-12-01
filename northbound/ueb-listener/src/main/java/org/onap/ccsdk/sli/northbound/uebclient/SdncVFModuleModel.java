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

import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;

public class SdncVFModuleModel extends SdncBaseModel {
	
	public SdncVFModuleModel(ISdcCsarHelper sdcCsarHelper, IEntityDetails vfModule, SdncVFModel vfNodeModel) {

		super(sdcCsarHelper, vfModule);
		// override base implementation for setting metadata because properties are called differently for Groups
		customizationUUID = extractValue (vfModule.getMetadata(), "vfModuleModelCustomizationUUID");  
		invariantUUID = extractValue (vfModule.getMetadata(), "vfModuleModelInvariantUUID"); 
		UUID = extractValue (vfModule.getMetadata(), "vfModuleModelUUID"); 
		version = extractValue (vfModule.getMetadata(), "vfModuleModelVersion");
		addParameter("vf_customization_uuid", vfNodeModel.getCustomizationUUIDNoQuotes());
		addParameter("invariant_uuid", invariantUUID);
		addParameter("uuid", UUID);
		addParameter("version", version);
		
		// extract properties
		addParameter("vf_module_type", extractValue(vfModule, SdcPropertyNames.PROPERTY_NAME_VFMODULETYPE));
		addParameter("vf_module_label", extractValue(vfModule, "vf_module_label"));
		addIntParameter("availability_zone_count", extractValue(vfModule, SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT));
		addParameter("ecomp_generated_vm_assignments", extractBooleanValue(vfModule, SdcPropertyNames.PROPERTY_NAME_ECOMPGENERATEDVMASSIGNMENTS));
	}

}
