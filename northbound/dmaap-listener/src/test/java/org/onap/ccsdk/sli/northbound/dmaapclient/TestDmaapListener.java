package org.onap.ccsdk.sli.northbound.dmaapclient;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDmaapListener {

	private static final String DMAAP_LISTENER_PROPERTIES = "dmaap-listener.properties";
	private static final String DMAAP_LISTENER_PROPERTIES_DIR = "src/test/resources";

	private static final Logger LOG = LoggerFactory.getLogger(TestDmaapListener.class);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		Properties properties = new Properties();
		String propFileName = DMAAP_LISTENER_PROPERTIES;
		String propPath = null;
		String propDir = DMAAP_LISTENER_PROPERTIES_DIR;

		List<SdncDmaapConsumer> consumers = new LinkedList<>();


		propPath = propDir + "/" + propFileName;

		if (propPath != null) {
			properties = DmaapListener.loadProperties(propPath, properties);

			String subscriptionStr = properties.getProperty("subscriptions");

			boolean threadsRunning = false;

			LOG.debug("Dmaap subscriptions : " + subscriptionStr);

			if (subscriptionStr != null) {
				threadsRunning = DmaapListener.handleSubscriptions(subscriptionStr, propDir, properties, consumers);
			}
		}
	}

}
