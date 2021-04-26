/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


public class OperationalStateValidatorTest {

    @Test
    public void testVNFValidResponse() {
        String validResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"101\">\n" +
                "    <data>\n" +
                "        <ManagedElement xmlns=\"urn:org:onap:appc:Test\">\n" +
                "            <managedElementId>1</managedElementId>\n" +
                "            <VnfFunction xmlns=\"urn:org:onap:appc:Test\">\n" +
                "                <id>1</id>\n" +
                "                <ProcessorManagement>\n" +
                "                    <id>1</id>\n" +
                "                    <MatedPair>\n" +
                "                        <id>1</id>\n" +
                "                        <operationalState>ENABLED</operationalState>\n" +
                "                        <PayloadProcessor>\n" +
                "                            <id>processor_0_5</id>\n" +
                "                            <operationalState>ENABLED</operationalState>\n" +
                "                        </PayloadProcessor>\n" +
                "                        <PayloadProcessor>\n" +
                "                            <id>processor_0_7</id>\n" +
                "                            <operationalState>ENABLED</operationalState>\n" +
                "                        </PayloadProcessor>\n" +
                "                    </MatedPair>\n" +
                "                    <SystemController>\n" +
                "                        <id>SC-1</id>\n" +
                "                        <operationalState>ENABLED</operationalState>\n" +
                "                    </SystemController>\n" +
                "                    <SystemController>\n" +
                "                        <id>SC-2</id>\n" +
                "                        <operationalState>ENABLED</operationalState>\n" +
                "                    </SystemController>\n" +
                "                </ProcessorManagement>\n" +
                "            </VnfFunction>\n" +
                "        </ManagedElement>\n" +
                "    </data>\n" +
                "</rpc-reply>";
        OperationalStateValidator operationalStateValidator = OperationalStateValidatorFactory.getOperationalStateValidator(VnfType.VNF);
        assertValidResponse(validResponse, operationalStateValidator);
    }

    void assertInvalidResponse(String response, OperationalStateValidator operationalStateValidator) {
        try {
            operationalStateValidator.validateResponse(response);
            fail("invalid response passed without exception!!!");
        } catch (SvcLogicException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testVNFInvalidResponses() {

        OperationalStateValidator operationalStateValidator = OperationalStateValidatorFactory.getOperationalStateValidator(VnfType.VNF);
        assertInvalidResponse(null, operationalStateValidator);

        assertInvalidResponse("", operationalStateValidator);

        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        assertInvalidResponse(response, operationalStateValidator);

        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"101\">\n" +
                "</rpc-reply>";
        assertInvalidResponse(response, operationalStateValidator);

        response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"101\">\n" +
                "    <data>\n" +
                "        <ManagedElement xmlns=\"urn:org:onap:appc:Test\">\n" +
                "            <managedElementId>1</managedElementId>\n" +
                "            <VnfFunction xmlns=\"urn:org:onap:appc:Test\">\n" +
                "                <id>1</id>\n" +
                "                <ProcessorManagement>\n" +
                "                    <id>1</id>\n" +
                "                    <MatedPair>\n" +
                "                        <id>1</id>\n" +
                "                        <operationalState>ENABLED</operationalState>\n" +
                "                        <PayloadProcessor>\n" +
                "                            <id>processor_0_5</id>\n" +
                "                            <operationalState>ENABLED</operationalState>\n" +
                "                        </PayloadProcessor>\n" +
                "                        <PayloadProcessor>\n" +
                "                            <id>processor_0_7</id>\n" +
                "                            <operationalState>ENABLED</operationalState>\n" +
                "                        </PayloadProcessor>\n" +
                "                    </MatedPair>\n" +
                "                    <SystemController>\n" +
                "                        <id>SC-1</id>\n" +
                "                        <operationalState>ENABLED</operationalState>\n" +
                "                    </SystemController>\n" +
                "                    <SystemController>\n" +
                "                        <id>SC-2</id>\n" +
                "                        <operationalState></operationalState>\n" +
                "                    </SystemController>\n" +
                "                </ProcessorManagement>\n" +
                "            </VnfFunction>\n" +
                "        </ManagedElement>\n" +
                "    </data>\n" +
                "</rpc-reply>";
        assertInvalidResponse(response, operationalStateValidator);
    }

    void assertValidResponse(String response, OperationalStateValidator operationalStateValidator) {
        try {
            operationalStateValidator.validateResponse(response);
        } catch (SvcLogicException e) {
            fail("Got unexpected exception. Validation failed. " + e.getMessage());
        }
    }

    @Test
    public void testMockValidResponse() {
        String response = "valid";
        OperationalStateValidator operationalStateValidator = OperationalStateValidatorFactory.getOperationalStateValidator("mock");
        assertValidResponse(response, operationalStateValidator);

        response = "";
        assertValidResponse(response, operationalStateValidator);

        response = null;
        assertValidResponse(response, operationalStateValidator);
    }

    @Test
    public void testMockInValidResponse() {
        String response = "anything InValid anything.. ";
        OperationalStateValidator operationalStateValidator = OperationalStateValidatorFactory.getOperationalStateValidator(VnfType.MOCK);
        assertInvalidResponse(response, operationalStateValidator);
    }

    @Test
    public void testGetOperationalStateValidatorForInValidVnfType() {
        try{
            OperationalStateValidatorFactory.getOperationalStateValidator("wrongVnfType");
            fail("invalid vnfType without exception!!!");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetOperationalStateValidatorForValidVnfType() {
        String vnfType = VnfType.VNF.name().toLowerCase();
        assertGettingValidatorForValidVnf(vnfType);

        vnfType = VnfType.VNF.name().toUpperCase();
        assertGettingValidatorForValidVnf(vnfType);

        vnfType = VnfType.MOCK.name().toLowerCase();
        assertGettingValidatorForValidVnf(vnfType);

        vnfType = VnfType.MOCK.name().toUpperCase();
        assertGettingValidatorForValidVnf(vnfType);
    }

    void assertGettingValidatorForValidVnf(String vnfType) {
        try{
            OperationalStateValidator operationalStateValidator = OperationalStateValidatorFactory.getOperationalStateValidator(vnfType);
            assertNotNull(operationalStateValidator);
        } catch (Exception e) {
            fail("valid vnfType throw exception!!!");
        }
    }
}
