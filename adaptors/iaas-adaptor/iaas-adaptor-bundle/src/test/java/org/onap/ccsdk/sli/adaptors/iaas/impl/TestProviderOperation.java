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

package org.onap.ccsdk.sli.adaptors.iaas.impl;

import com.att.cdp.zones.model.ModelObject;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.sli.core.utils.configuration.ConfigurationFactory;
import org.onap.ccsdk.sli.adaptors.iaas.provider.operation.impl.base.ProviderOperation;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.MDC;

import static org.onap.ccsdk.sli.adaptors.iaas.Constants.MDC_SERVICE;

/**
 * This class is used to test methods and functions of the adapter implementation that do not
 * require and do not set up connections to any providers.
 *
 * @since Jan 20, 2016
 * @version $Id$
 */

public class TestProviderOperation extends ProviderOperation {

    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test
     * to change the internal state accordingly.
     *
     * @throws NoSuchFieldException if the field(s) dont exist
     * @throws SecurityException if reflective access is not allowed
     * @throws NoSuchMethodException If the method(s) dont exist
     */
    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {
        Class<?> providerAdapterImplClass = ProviderAdapterImpl.class;
        Class<?> configurationFactoryClass = ConfigurationFactory.class;

        Field providerCacheField = providerAdapterImplClass.getDeclaredField("providerCache");
        providerCacheField.setAccessible(true);

        Field configField = configurationFactoryClass.getDeclaredField("config");
        configField.setAccessible(true);
    }

    /**
     * This test expects a failure because the value to be validated is a null URL
     *
     * @throws RequestFailedException Expected
     */
    @SuppressWarnings("nls")
    @Test(expected = RequestFailedException.class)
    public void testValidateParameterPatternExpectFailNullValue() throws RequestFailedException {
        MDC.put(MDC_SERVICE, "junit");
        String link = null;
        validateVMURL(VMURL.parseURL(link));
    }

    /**
     * This test expects a failure because the value to be validated is an empty URL
     *
     * @throws RequestFailedException Expected
     */
    @SuppressWarnings("nls")
    @Test(expected = RequestFailedException.class)
    public void testValidateParameterPatternExpectFailEmptyValue() throws RequestFailedException {
        MDC.put(MDC_SERVICE, "junit");
        String link = "";
        validateVMURL(VMURL.parseURL(link));
    }

    /**
     * This test expects a failure because the value to be validated is a blank URL
     *
     * @throws RequestFailedException Expected
     */
    @SuppressWarnings("nls")
    @Test(expected = RequestFailedException.class)
    public void testValidateParameterPatternExpectFailBlankValue() throws RequestFailedException {
        MDC.put(MDC_SERVICE, "junit");
        String link = " ";
        validateVMURL(VMURL.parseURL(link));
    }

    /**
     * This test expects a failure because the value to be validated is a bad URL
     *
     * @throws RequestFailedException Expected
     */
    @SuppressWarnings("nls")
    @Test(expected = RequestFailedException.class)
    public void testValidateParameterPatternExpectFailBadURL() throws RequestFailedException {
        MDC.put(MDC_SERVICE, "junit");
        String link = "http://some.host:1234/01d82c08594a4b23a0f9260c94be0c4d/";
        validateVMURL(VMURL.parseURL(link));
    }

    /**
     * This test expects to pass
     *
     * @throws RequestFailedException Un-Expected
     */
    @SuppressWarnings("nls")
    @Test
    public void testValidateParameterPatternValidURL() throws RequestFailedException {
        MDC.put(MDC_SERVICE, "junit");
        String link =
                "http://some.host:1234/v2/01d82c08594a4b23a0f9260c94be0c4d/servers/f888f89f-096b-421e-ba36-34f714071551";
        validateVMURL(VMURL.parseURL(link));
    }

    @Override
    protected ModelObject executeProviderOperation(Map<String, String> params, SvcLogicContext context)
            throws SvcLogicException {
        return null;
    }
}
