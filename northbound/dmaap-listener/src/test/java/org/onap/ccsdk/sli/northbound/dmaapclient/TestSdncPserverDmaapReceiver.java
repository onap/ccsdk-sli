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

public class TestSdncPserverDmaapReceiver {
    private static final String aaiInput = "{\r\n" + 
            "    \"cambria.partition\": \"AAI\",\r\n" + 
            "    \"event-header\": {\r\n" + 
            "        \"severity\": \"NORMAL\",\r\n" + 
            "        \"entity-type\": \"pserver\",\r\n" + 
            "        \"top-entity-type\": \"pserver\",\r\n" + 
            "        \"entity-link\": \"https://aai.com:8443/aai/v11/cloud-infrastructure/pservers/pserver/a3d3d3d3/\",\r\n" + 
            "        \"event-type\": \"AAI-EVENT\",\r\n" + 
            "        \"domain\": \"e2e\",\r\n" + 
            "        \"action\": \"UPDATE\",\r\n" + 
            "        \"sequence-number\": \"0\",\r\n" + 
            "        \"id\": \"20170415000111-1234\",\r\n" + 
            "        \"source-name\": \"testclient\",\r\n" + 
            "        \"version\": \"v11\",\r\n" + 
            "        \"timestamp\": \"20170415-00:01:11:979\"\r\n" + 
            "    },\r\n" + 
            "    \"entity\": {\r\n" + 
            "        \"hostname\": \"host1\",\r\n" + 
            "        \"ptnii-equip-name\": \"lat111\",\r\n" + 
            "        \"equip-type\": \"server\",\r\n" + 
            "        \"equip-vendor\": \"HP\",\r\n" + 
            "        \"equip-model\": \"model1\",\r\n" + 
            "        \"fqdn\": \"l.global.net\",\r\n" + 
            "        \"ipv4-oam-address\": \"12.12.12.12\",\r\n" + 
            "        \"in-maint\": false,\r\n" + 
            "        \"resource-version\": \"11111111111\",\r\n" + 
            "        \"purpose\": \"Gamma\",\r\n" + 
            "        \"relationship-list\": {\r\n" + 
            "            \"relationship\": [\r\n" + 
            "                {\r\n" + 
            "                    \"related-to\": \"complex\",\r\n" + 
            "                    \"relationship-data\": [\r\n" + 
            "                        {\r\n" + 
            "                            \"relationship-value\": \"L1L2L3\",\r\n" + 
            "                            \"relationship-key\": \"complex.physical-location-id\"\r\n" + 
            "                        }\r\n" + 
            "                    ],\r\n" + 
            "                    \"related-link\": \"https://aai.com:8443/aai/v11/cloud-infrastructure/complexes/complex/cmpl1\"\r\n" + 
            "                }\r\n" + 
            "            ]\r\n" + 
            "        },\r\n" + 
            "        \"p-interfaces\": {\r\n" + 
            "            \"p-interface\": []\r\n" + 
            "        },\r\n" + 
            "        \"lag-interfaces\": {\r\n" + 
            "            \"lag-interface\": []\r\n" + 
            "        }\r\n" + 
            "    }\r\n" + 
            "}";
    
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws Exception {
		Properties props = new Properties();
 
	    String rpcMsgbody = new SdncAaiDmaapConsumer(props).publish("src/main/resources/template-pserver.vt", aaiInput);
        
	    ObjectMapper oMapper = new ObjectMapper();
        JsonNode aaiRootNode;
        try {
            aaiRootNode = oMapper.readTree(rpcMsgbody);
        } catch (Exception e) {
            throw new InvalidMessageException("Cannot parse json object", e);
        }       

        assertTrue(aaiRootNode.get("input").get("payload") != null); 
        assertTrue(aaiRootNode.get("input").get("common-header") != null); 
        
	}

	@Test
	public void testProcessMsgFieldMap() throws Exception {


		String DMAAPLISTENERROOT = "DMAAPLISTENERROOT";
		File directory = new File("lib");

		if (! directory.exists()){
			directory.mkdir();
		}

		File source = new File("src/main/resources");
		File dest = new File("lib/");
		try {
			FileUtils.copyDirectory(source, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Map<String, String> env = System.getenv();
			Class<?> cl = env.getClass();
			Field field = cl.getDeclaredField("m");
			field.setAccessible(true);
			Map<String, String> writableEnv = (Map<String, String>) field.get(env);
			writableEnv.put(DMAAPLISTENERROOT, ".");
		} catch (Exception e) {
			throw new IllegalStateException("Failed to set environment variable", e);
		}
		Properties props = new Properties();

		SdncAaiDmaapConsumer consumer = new SdncAaiDmaapConsumer();

		InputStream propStr = TestSdncPserverDmaapReceiver.class.getResourceAsStream("/dmaap-consumer-pserver.properties");


		props.load(propStr);

		consumer.init(props, "src/test/resources/dmaap-consumer-pserver.properties");
		consumer.processMsg(aaiInput);
	}

	@Test(expected = InvalidMessageException.class)
	public void testProcessMsgNullMessage() throws Exception {
		Properties props = new Properties();

		SdncAaiDmaapConsumer consumer = new SdncAaiDmaapConsumer();
		consumer.processMsg(null);
	}

	@Test(expected = InvalidMessageException.class)
	public void testProcessMsgInvalidMessage() throws Exception {
		Properties props = new Properties();

		SdncAaiDmaapConsumer consumer = new SdncAaiDmaapConsumer();
		consumer.processMsg("test");
	}

	@Test
	public void testProcessMsgMissingEventHeader() throws Exception {
		Properties props = new Properties();

		SdncAaiDmaapConsumer consumer = new SdncAaiDmaapConsumer();
		consumer.processMsg("{\n" +
				"    \"input\" : {        \n" +
				"    }\n" +
				"}");
	}

	@Test
	public void testProcessMsgInvalidEventType() throws Exception {
		Properties props = new Properties();

		String msg = "{\"cambria.partition\": \"AAI\",\r\n" +
				"    \"event-header\": {\"event-type\": \"TEST-EVENT\"}}";

		SdncAaiDmaapConsumer consumer = new SdncAaiDmaapConsumer();
		consumer.processMsg(msg);
	}





}
