/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package org.onap.ccsdk.sli.northbound.dmaapclient;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestA1AdapterPolicyDmaapConsumer {
    private static final String a1AdapterInput =  "{\n" +
		      " \"body\": {\n" +
		      "    \"input\": {\n" +
		      "      \"CommonHeader\": {\n" +
		      "        \"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" +
		      "        \"APIver\": \"1.0\",\n" +
		      "        \"RequestID\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459\",\n" +
		      "        \"SubRequestID\": \"1\",\n" +
		      "        \"RequestTrack\": {},\n" +
		      "        \"Flags\": {}\n      },\n" +
		      "      \"Action\": \"getHealthCheck\",\n" +
		      "      \"Payload\": {\n" +
		      "        \"near-rt-ric-id\": \"near-RT-ric1\",\n" +
		      "        \"policy-type-id\": \"20000\",\n" +
		      "        \"description\": \"parameters to control policy \",\n" +
		      "        \"name\": \"admission_control_policy\",\n" +
		      "        \"policy-type\": \"object\"\n" +
		      "      }\n" +
		      "    },\n" +
		      "    \"version\": \"1.0\",\n" +
		      "    \"rpc-name\": \"getHealthCheck\",\n" +
		      "    \"correlation-id\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459-1\",\n" +
		      "    \"type\": \"request\"\n" +
		      "  }\n" +
		      "}";

	@Test
	public void test() throws Exception {
		Properties props = new Properties();

		A1AdapterPolicyDmaapConsumer consumer = new A1AdapterPolicyDmaapConsumer();
		InputStream propStr = TestA1AdapterPolicyDmaapConsumer.class.getResourceAsStream("/dmaap-consumer-a1Adapter-policy-1.properties");
		props.load(propStr);
		consumer.init(props, "src/test/resources/dmaap-consumer-1.properties");
		consumer.processMsg(a1AdapterInput);
	}

	@Test(expected = InvalidMessageException.class)
	public void testProcessMsgNullMessage() throws Exception {
		A1AdapterPolicyDmaapConsumer consumer = new A1AdapterPolicyDmaapConsumer();
		consumer.processMsg(null);
	}

	@Test
	public void testProcessMsgMissingBody() throws Exception {
		String msg = 	"{\n" +
		      " \"body1\": {\n" +
		      "    \"input\": {\n" +
		      "      \"CommonHeader\": {\n" +
		      "        \"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" +
		      "        \"APIver\": \"1.0\",\n" +
		      "        \"RequestID\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459\",\n" +
		      "        \"SubRequestID\": \"1\",\n" +
		      "        \"RequestTrack\": {},\n" +
		      "        \"Flags\": {}\n      },\n" +
		      "      \"Action\": \"getHealthCheck\",\n" +
		      "      \"Payload\": {\n" +
		      "        \"near-rt-ric-id\": \"near-RT-ric1\",\n" +
		      "        \"policy-type-id\": \"20000\",\n" +
		      "        \"description\": \"parameters to control policy \",\n" +
		      "        \"name\": \"admission_control_policy\",\n" +
		      "        \"policy-type\": \"object\"\n" +
		      "      }\n" +
		      "    },\n" +
		      "    \"version\": \"1.0\",\n" +
		      "    \"rpc-name\": \"getHealthCheck\",\n" +
		      "    \"correlation-id\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459-1\",\n" +
		      "    \"type\": \"request\"\n" +
		      "  }\n" +
		      "}";

		A1AdapterPolicyDmaapConsumer consumer = new A1AdapterPolicyDmaapConsumer();
		consumer.processMsg(msg);
	}

	@Test
	public void testProcessMsgInvalidRPC() throws Exception {
    String msg = 	"{\n" +
		      " \"body\": {\n" +
		      "    \"input\": {\n" +
		      "      \"CommonHeader\": {\n" +
		      "        \"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" +
		      "        \"APIver\": \"1.0\",\n" +
		      "        \"RequestID\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459\",\n" +
		      "        \"SubRequestID\": \"1\",\n" +
		      "        \"RequestTrack\": {},\n" +
		      "        \"Flags\": {}\n      },\n" +
		      "      \"Action\": \"getHealthCheck\",\n" +
		      "      \"Payload\": {\n" +
		      "        \"near-rt-ric-id\": \"near-RT-ric1\",\n" +
		      "        \"policy-type-id\": \"20000\",\n" +
		      "        \"description\": \"parameters to control policy \",\n" +
		      "        \"name\": \"admission_control_policy\",\n" +
		      "        \"policy-type\": \"object\"\n" +
		      "      }\n" +
		      "    },\n" +
		      "    \"version\": \"1.0\",\n" +
		      "    \"rpc-name1\": \"getHealthCheck\",\n" +
		      "    \"correlation-id\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459-1\",\n" +
		      "    \"type\": \"request\"\n" +
		      "  }\n" +
		      "}";

		A1AdapterPolicyDmaapConsumer consumer = new A1AdapterPolicyDmaapConsumer();
		consumer.processMsg(msg);
	}

}
