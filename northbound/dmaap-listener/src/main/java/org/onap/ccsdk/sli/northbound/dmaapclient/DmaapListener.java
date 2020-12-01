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

package org.onap.ccsdk.sli.northbound.dmaapclient;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmaapListener {

    private static final String DMAAP_LISTENER_PROPERTIES = "dmaap-listener.properties";
    private static final String DMAAP_LISTENER_PROPERTIES_DIR = "/opt/onap/ccsdk/data/properties";
    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    private static final Logger LOG = LoggerFactory.getLogger(DmaapListener.class);

    public static void main(String[] args) {

        Properties properties = new Properties();
        String propFileName = DMAAP_LISTENER_PROPERTIES;
        String propPath = null;
        String propDir = System.getProperty(SDNC_CONFIG_DIR);
        if(propDir == null) {
        	propDir = System.getenv(SDNC_CONFIG_DIR);
        	LOG.debug(SDNC_CONFIG_DIR + " read from environment variable with value " + propDir);
        }
        List<SdncDmaapConsumer> consumers = new LinkedList<>();

        if (args.length > 0) {
            propFileName = args[0];
        }

        if (propDir == null) {
            propDir = DMAAP_LISTENER_PROPERTIES_DIR;
        }

        if (!propFileName.startsWith("/")) {
            propPath = propDir + "/" + propFileName;
        }

        if (propPath != null) {
            properties = loadProperties(propPath, properties);

            String subscriptionStr = properties.getProperty("subscriptions");

            boolean threadsRunning = false;

            LOG.debug("Dmaap subscriptions : " + subscriptionStr);

            if (subscriptionStr != null) {
                threadsRunning = handleSubscriptions(subscriptionStr, propDir, properties, consumers);
            }

            while (threadsRunning) {
                threadsRunning = updateThreadState(consumers);
                if (!threadsRunning) {
                    break;
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }

            LOG.info("No listener threads running - exiting");
        }
    }

    private static boolean updateThreadState(List<SdncDmaapConsumer> consumers) {
        boolean threadsRunning = false;
        for (SdncDmaapConsumer consumer : consumers) {
            if (consumer.isRunning()) {
                threadsRunning = true;
            }
        }
        return threadsRunning;
    }

    static Properties loadProperties(String propPath, Properties properties) {
        File propFile = new File(propPath);

        if (!propFile.canRead()) {
            LOG.error("Cannot read properties file " + propPath);
            System.exit(1);
        }

        try (FileInputStream in = new FileInputStream(propFile)) {
            properties.load(in);
        } catch (Exception e) {
            LOG.error("Caught exception loading properties from " + propPath, e);
            System.exit(1);
        }
        return properties;
    }

    static boolean handleSubscriptions(String subscriptionStr, String propDir, Properties properties,
        List<SdncDmaapConsumer> consumers) {
        String[] subscriptions = subscriptionStr.split(";");

        for (String subscription1 : subscriptions) {
            String[] subscription = subscription1.split(":");
            String consumerClassName = subscription[0];
            String propertyPath = subscription[1];

            LOG.debug(String.format("Handling subscription [%s,%s]", consumerClassName, propertyPath));

            if (propertyPath == null) {
                LOG.error(String.format("Invalid subscription (%s) property file missing", subscription1));
                continue;
            }

            if (!propertyPath.startsWith("/")) {
                propertyPath = propDir + "/" + propertyPath;
            }

            Class<?> consumerClass = null;

            try {
                consumerClass = Class.forName(consumerClassName);
            } catch (Exception e) {
                LOG.error("Could not find DMaap consumer class {}", consumerClassName, e);
            }

            if (consumerClass != null) {
                handleConsumerClass(consumerClass, consumerClassName, propertyPath,
                    properties, consumers);
            }
        }
        return !consumers.isEmpty();
    }

    private static boolean handleConsumerClass(Class<?> consumerClass, String consumerClassName, String propertyPath,
        Properties properties, List<SdncDmaapConsumer> consumers) {

        SdncDmaapConsumer consumer = null;

        try {
            consumer = (SdncDmaapConsumer) consumerClass.newInstance();
        } catch (Exception e) {
            LOG.error("Could not create consumer from class " + consumerClassName, e);
        }

        if (consumer != null) {
            LOG.debug(String.format("Initializing consumer %s(%s)", consumerClassName, propertyPath));
            consumer.init(properties, propertyPath);

            if (consumer.isReady()) {
                Thread consumerThread = new Thread(consumer);
                consumerThread.start();
                consumers.add(consumer);

                LOG.info(String.format("Started consumer thread (%s : %s)", consumerClassName,
                    propertyPath));
                return true;
            } else {
                LOG.debug(String.format("Consumer %s is not ready", consumerClassName));
            }
        }
        return false;
    }
}
