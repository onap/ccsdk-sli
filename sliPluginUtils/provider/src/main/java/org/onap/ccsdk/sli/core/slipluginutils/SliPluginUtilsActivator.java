/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
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

package org.onap.ccsdk.sli.core.slipluginutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliPluginUtilsActivator implements BundleActivator {
    @SuppressWarnings("rawtypes") private List<ServiceRegistration> registrations = new LinkedList<ServiceRegistration>();

    private static final Logger LOG = LoggerFactory.getLogger(SliPluginUtilsActivator.class);
    private static final String SDNC_ROOT_DIR = "SDNC_CONFIG_DIR";
    private static final String DME2_PROPERTIES_FILE_NAME = "dme2.properties";

    @Override
    public void start(BundleContext ctx) throws Exception {
        SliPluginUtils plugin = new SliPluginUtils(new Properties());
        LOG.info("Registering service " + plugin.getClass().getName());
        registrations.add(ctx.registerService(plugin.getClass().getName(), plugin, null));

        SliStringUtils sliStringUtils_Plugin = new SliStringUtils();
        LOG.info("Registering service " + sliStringUtils_Plugin.getClass().getName());
        registrations.add(ctx.registerService(sliStringUtils_Plugin.getClass().getName(), sliStringUtils_Plugin, null));

        try {
            String path = System.getenv(SDNC_ROOT_DIR) + File.separator + DME2_PROPERTIES_FILE_NAME;
            DME2 dmePlugin = initDme2(path);
            if (dmePlugin != null) {
                LOG.info("Registering service " + dmePlugin.getClass().getName());
                registrations.add(ctx.registerService(dmePlugin.getClass().getName(), dmePlugin, null));
            }
        } catch (Exception e) {
            LOG.error("DME2 plugin could not be started", e);
        }
    }

    public DME2 initDme2(String pathToDmeProperties) {
        Properties dme2properties = new Properties();
        String loadPropertiesErrorMessage = "Couldn't load DME2 properties at path " + pathToDmeProperties;
        File dme2propertiesFile = new File(pathToDmeProperties);

        try {
            dme2properties.load(new FileReader(dme2propertiesFile));
            String proxyUrlProperty = dme2properties.getProperty("proxyUrl");
            String[] proxyUrls = proxyUrlProperty.split(",");
            DME2 dmePlugin = new DME2(dme2properties.getProperty("aafUserName"), dme2properties.getProperty("aafPassword"), dme2properties.getProperty("envContext"), dme2properties.getProperty("routeOffer"), proxyUrls, dme2properties.getProperty("commonServiceVersion"));
            dmePlugin.setPartner(dme2properties.getProperty("partner"));
            return dmePlugin;
        } catch (FileNotFoundException e) {
            LOG.error(loadPropertiesErrorMessage);
        } catch (IOException e) {
            LOG.error(loadPropertiesErrorMessage);
        }
        LOG.error("Couldn't create DME2 plugin");
        return null;
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {
        for (@SuppressWarnings("rawtypes") ServiceRegistration registration : registrations) {
            registration.unregister();
            registration = null;
        }
    }
}
