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


public class TestCMNotifyDmaapConsumer {
    private static final String cMNotifyInput =  "{\n" +
    " \"body\": {\n" +
    "    \"input\": {\n" +
    "      \"CommonHeader\": {\n" +
    "        \"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" +
    "        \"APIver\": \"1.0\",\n" +
    "        \"RequestID\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459\",\n" +
    "        \"SubRequestID\": \"1\",\n" +
    "        \"RequestTrack\": {},\n" +
    "        \"Flags\": {}\n      },\n" +
    "      \"Action\": \"nbrlist-change-notification\",\n" +
    "      \"Payload\": {\n" +
     "        \"fap-service-number-of-entries-changed\": 1,\n" +
     "        \"fap-service\": [{ \"alias\": \n" +
     "        \"Chn0001\", \"cid\": \"Chn0001\", \"lte-cell-number-of-entries\": 1,\n" +
     "        \"lte-ran-neighbor-list-in-use-lte-cell-changed\": \n" +
     "        [{ \"plmnid\": \"ran-1\", \"cid\": \"Chn0002\", \"phy-cell-id\": 4,\n" +
     "        \"pnf-name\": \"ncserver1\",\n" +
     "        \"blacklisted\": false }] }] }\n" +
     "     }\n" +
    "    },\n" +
    "    \"version\": \"1.0\",\n" +
    "    \"rpc-name\": \"nbrlist-change-notification\",\n" +
    "    \"correlation-id\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459-1\",\n" +
    "    \"type\": \"request\"\n" +
    "}";


	@Test
	public void test() throws Exception {
		Properties props = new Properties();

		CMNotifyDmaapConsumer consumer = new CMNotifyDmaapConsumer();
		InputStream propStr = TestCMNotifyDmaapConsumer.class.getResourceAsStream("/dmaap-consumer-cMNotify-1.properties");
		props.load(propStr);
		consumer.init(props, "src/test/resources/dmaap-consumer-1.properties");
		consumer.processMsg(cMNotifyInput);
	}

	@Test(expected = InvalidMessageException.class)
	public void testProcessMsgNullMessage() throws Exception {
		CMNotifyDmaapConsumer consumer = new CMNotifyDmaapConsumer();
		consumer.processMsg(null);
	}

	@Test
	public void testProcessMsgMissingBody() throws Exception {
		String msg = 	"{\n" +
    " \"bodyTest\": {\n" +
    "    \"input\": {\n" +
    "      \"CommonHeader\": {\n" +
    "        \"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" +
    "        \"APIver\": \"1.0\",\n" +
    "        \"RequestID\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459\",\n" +
    "        \"SubRequestID\": \"1\",\n" +
    "        \"RequestTrack\": {},\n" +
    "        \"Flags\": {}\n      },\n" +
    "      \"Action\": \"nbrlist-change-notification\",\n" +
    "      \"Payload\": {\n" +
     "        \"fap-service-number-of-entries-changed\": 1,\n" +
     "        \"fap-service\": [{ \"alias\": \n" +
     "        \"Chn0001\", \"cid\": \"Chn0001\", \"lte-cell-number-of-entries\": 1,\n" +
     "        \"lte-ran-neighbor-list-in-use-lte-cell-changed\": \n" +
     "        [{ \"plmnid\": \"ran-1\", \"cid\": \"Chn0002\", \"phy-cell-id\": 4,\n" +
     "        \"pnf-name\": \"ncserver1\",\n" +
     "        \"blacklisted\": false }] }] }\n" +
     "     }\n" +
    "    },\n" +
    "    \"version\": \"1.0\",\n" +
    "    \"rpc-name\": \"nbrlist-change-notification\",\n" +
    "    \"correlation-id\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459-1\",\n" +
    "    \"type\": \"request\"\n" +
    "}";
		CMNotifyDmaapConsumer consumer = new CMNotifyDmaapConsumer();
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
    "      \"Action\": \"nbrlist-change-notification\",\n" +
    "      \"Payload\": {\n" +
     "        \"fap-service-number-of-entries-changed\": 1,\n" +
     "        \"fap-service\": [{ \"alias\": \n" +
     "        \"Chn0001\", \"cid\": \"Chn0001\", \"lte-cell-number-of-entries\": 1,\n" +
     "        \"lte-ran-neighbor-list-in-use-lte-cell-changed\": \n" +
     "        [{ \"plmnid\": \"ran-1\", \"cid\": \"Chn0002\", \"phy-cell-id\": 4,\n" +
     "        \"pnf-name\": \"ncserver1\",\n" +
     "        \"blacklisted\": false }] }] }\n" +
     "     }\n" +
    "    },\n" +
    "    \"version\": \"1.0\",\n" +
    "    \"rpc-nameTest\": \"nbrlist-change-notification\",\n" +
    "    \"correlation-id\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459-1\",\n" +
    "    \"type\": \"request\"\n" +
    "}";

		CMNotifyDmaapConsumer consumer = new CMNotifyDmaapConsumer();
		consumer.processMsg(msg);
	}

}
