/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.netconf;

public enum VnfType {
    VNF("VNF"),
    MOCK("MOCK"),
    VNF_MOCK("MOCK"),
    ;

    String familyType;
    String upperCaseName;
    VnfType(String familyType) {
        this.familyType = familyType;
        this.upperCaseName = name().toUpperCase();
    }

    public VnfType getFamilyType() {
        return VnfType.valueOf(familyType);
    }

    public String getUpperCaseName() {
        return upperCaseName;
    }

    public static VnfType getVnfType(String inSensitiveCaseName){
        String localUpperCaseName = inSensitiveCaseName.toUpperCase();
        for(VnfType vnfType : VnfType.values()){
            if(vnfType.getUpperCaseName().equals(localUpperCaseName)){
                return vnfType;
            }
        }
        throw new IllegalArgumentException(
                "No enum with upperCaseName for this input value:" + inSensitiveCaseName );
    }
}
