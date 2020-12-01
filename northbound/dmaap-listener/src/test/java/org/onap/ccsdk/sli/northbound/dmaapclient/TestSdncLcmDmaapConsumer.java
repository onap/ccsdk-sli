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

public class TestSdncLcmDmaapConsumer {
    private static final String lcmInput = 
    				"{\r\n" +
    				"    \"version\": \"lcm-dmaap.version\",\r\n" +
    				"    \"cambria.partition\": \"SDNC\",\r\n" +
    				"    \"correlation-id\": \"lcm-dmaap.correlation-id\",\r\n" +
    				"    \"rpc-name\": \"upgrade-software\",\r\n" +
    				"    \"type\": \"lcm-dmaap.type\",\r\n" +
    				"    \"body\": {\r\n" +
    				"        \"input\": {\r\n" +
    				"            \"common-header\": {\r\n" +
    				"                \"api-ver\": \"lcm-dmaap.api-ver\",\r\n" +
    				"                \"flags\": {\r\n" +
    				"                    \"ttl\": \"lcm-dmaap.flags.mode\",\r\n" +
    				"                    \"force\": \"lcm-dmaap.flags.force\",\r\n" +
    				"                    \"mode\": \"lcm-dmaap.flags.mode\"\r\n" +
    				"                },\r\n" +
    				"                \"originator-id\": \"lcm-dmaap.originator-id\",\r\n" +
    				"                \"request-id\": \"lcm-dmaap.request-id\",\r\n" +
    				"                \"sub-request-id\": \"lcm-dmaap.sub-request-id\",\r\n" +
    				"                \"timestamp\": \"lcm-dmaap.timestamp\"\r\n" +
    				"            },\r\n" +
    				"            \"payload\": \"lcm-dmaap.payload\"\r\n" +
    				"        }\r\n" +
    				"    }\r\n" +
    				"}";
    

	@Test
	public void test() throws Exception {
		Properties props = new Properties();

		SdncLcmDmaapConsumer consumer = new SdncLcmDmaapConsumer();
		InputStream propStr = TestSdncLcmDmaapConsumer.class.getResourceAsStream("/dmaap-consumer-1.properties");
		props.load(propStr);
		consumer.init(props, "src/test/resources/dmaap-consumer-1.properties");
		consumer.processMsg(lcmInput);
	}

	@Test(expected = InvalidMessageException.class)
	public void testProcessMsgNullMessage() throws Exception {
		SdncLcmDmaapConsumer consumer = new SdncLcmDmaapConsumer();
		consumer.processMsg(null);
	}
	
	@Test
	public void testProcessMsgMissingBody() throws Exception {
		String msg = 	"{\r\n" +
				"    \"version\": \"lcm-dmaap.version\",\r\n" +
				"    \"cambria.partition\": \"SDNC\",\r\n" +
				"    \"correlation-id\": \"lcm-dmaap.correlation-id\",\r\n" +
				"    \"rpc-name\": \"upgrade-software\",\r\n" +
				"    \"type\": \"lcm-dmaap.type\",\r\n" +
				"    \"body1\": {\r\n" +
				"        \"input\": {\r\n" +
				"            \"common-header\": {\r\n" +
				"                \"api-ver\": \"lcm-dmaap.api-ver\",\r\n" +
				"                \"flags\": {\r\n" +
				"                    \"ttl\": \"lcm-dmaap.flags.mode\",\r\n" +
				"                    \"force\": \"lcm-dmaap.flags.force\",\r\n" +
				"                    \"mode\": \"lcm-dmaap.flags.mode\"\r\n" +
				"                },\r\n" +
				"                \"originator-id\": \"lcm-dmaap.originator-id\",\r\n" +
				"                \"request-id\": \"lcm-dmaap.request-id\",\r\n" +
				"                \"sub-request-id\": \"lcm-dmaap.sub-request-id\",\r\n" +
				"                \"timestamp\": \"lcm-dmaap.timestamp\"\r\n" +
				"            },\r\n" +
				"            \"payload\": \"lcm-dmaap.payload\"\r\n" +
				"        }\r\n" +
				"    }\r\n" +
				"}";
		
		SdncLcmDmaapConsumer consumer = new SdncLcmDmaapConsumer();
		consumer.processMsg(msg);
	}

	@Test
	public void testProcessMsgInvalidRPC() throws Exception {
		String msg = 	"{\r\n" +
				"    \"version\": \"lcm-dmaap.version\",\r\n" +
				"    \"cambria.partition\": \"SDNC\",\r\n" +
				"    \"correlation-id\": \"lcm-dmaap.correlation-id\",\r\n" +
				"    \"rpc-name1\": \"upgrade-software\",\r\n" +
				"    \"type\": \"lcm-dmaap.type\",\r\n" +
				"    \"body\": {\r\n" +
				"        \"input\": {\r\n" +
				"            \"common-header\": {\r\n" +
				"                \"api-ver\": \"lcm-dmaap.api-ver\",\r\n" +
				"                \"flags\": {\r\n" +
				"                    \"ttl\": \"lcm-dmaap.flags.mode\",\r\n" +
				"                    \"force\": \"lcm-dmaap.flags.force\",\r\n" +
				"                    \"mode\": \"lcm-dmaap.flags.mode\"\r\n" +
				"                },\r\n" +
				"                \"originator-id\": \"lcm-dmaap.originator-id\",\r\n" +
				"                \"request-id\": \"lcm-dmaap.request-id\",\r\n" +
				"                \"sub-request-id\": \"lcm-dmaap.sub-request-id\",\r\n" +
				"                \"timestamp\": \"lcm-dmaap.timestamp\"\r\n" +
				"            },\r\n" +
				"            \"payload\": \"lcm-dmaap.payload\"\r\n" +
				"        }\r\n" +
				"    }\r\n" +
				"}";
		
		SdncLcmDmaapConsumer consumer = new SdncLcmDmaapConsumer();
		consumer.processMsg(msg);
	}

	@Test
	public void testProcessMsgInvalidPartition() throws Exception {
		String msg = 	"{\r\n" +
				"    \"version\": \"lcm-dmaap.version\",\r\n" +
				"    \"cambria.partition\": \"BAD\",\r\n" +
				"    \"correlation-id\": \"lcm-dmaap.correlation-id\",\r\n" +
				"    \"rpc-name\": \"upgrade-software\",\r\n" +
				"    \"type\": \"lcm-dmaap.type\",\r\n" +
				"    \"body\": {\r\n" +
				"        \"input\": {\r\n" +
				"            \"common-header\": {\r\n" +
				"                \"api-ver\": \"lcm-dmaap.api-ver\",\r\n" +
				"                \"flags\": {\r\n" +
				"                    \"ttl\": \"lcm-dmaap.flags.mode\",\r\n" +
				"                    \"force\": \"lcm-dmaap.flags.force\",\r\n" +
				"                    \"mode\": \"lcm-dmaap.flags.mode\"\r\n" +
				"                },\r\n" +
				"                \"originator-id\": \"lcm-dmaap.originator-id\",\r\n" +
				"                \"request-id\": \"lcm-dmaap.request-id\",\r\n" +
				"                \"sub-request-id\": \"lcm-dmaap.sub-request-id\",\r\n" +
				"                \"timestamp\": \"lcm-dmaap.timestamp\"\r\n" +
				"            },\r\n" +
				"            \"payload\": \"lcm-dmaap.payload\"\r\n" +
				"        }\r\n" +
				"    }\r\n" +
				"}";


		SdncLcmDmaapConsumer consumer = new SdncLcmDmaapConsumer();
		consumer.processMsg(msg);
	}
}
