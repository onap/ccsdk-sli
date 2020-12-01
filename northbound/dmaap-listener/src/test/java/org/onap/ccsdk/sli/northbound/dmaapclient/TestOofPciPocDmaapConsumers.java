package org.onap.ccsdk.sli.northbound.dmaapclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestOofPciPocDmaapConsumers {

	private static final String pciChangesFromPolicyToSDNRInput = "{\n" + 
    		"	\"body\": {\n" + 
    		"		\"input\": {\n" + 
    		"			\"CommonHeader\": {\n" + 
    		"				\"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" + 
    		"				\"APIVer\": \"1.0\",\n" + 
    		"				\"RequestID\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459\",\n" + 
    		"				\"SubRequestID\": \"1\",\n" + 
    		"				\"RequestTrack\": {},\n" + 
    		"				\"Flags\": {}\n" + 
    		"			},\n" + 
    		"			\"Action\": \"ModifyConfig\",\n" + 
    		"			\"Payload\": \"{\\\"Configurations\\\":[{\\\"data\\\":{\\\"FAPService\\\":{\\\"alias\\\":\\\"Chn0330\\\",\\\"X0005b9Lte\\\":{\\\"phyCellIdInUse\\\":6,\\\"pnfName\\\":\\\"ncserver23\\\"},\\\"CellConfig\\\":{\\\"LTE\\\":{\\\"RAN\\\":{\\\"Common\\\":{\\\"CellIdentity\\\":\\\"Chn0330\\\"}}}}}}},{\\\"data\\\":{\\\"FAPService\\\":{\\\"alias\\\":\\\"Chn0331\\\",\\\"X0005b9Lte\\\":{\\\"phyCellIdInUse\\\":7,\\\"pnfName\\\":\\\"ncserver23\\\"},\\\"CellConfig\\\":{\\\"LTE\\\":{\\\"RAN\\\":{\\\"Common\\\":{\\\"CellIdentity\\\":\\\"Chn0331\\\"}}}}}}}]}\"\n" + 
    		"		}\n" + 
    		"	},\n" + 
    		"	\"version\": \"1.0\",\n" + 
    		"	\"rpc-name\": \"modifyconfig\",\n" + 
    		"	\"correlation-id\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459-1\",\n" + 
    		"	\"type\": \"request\"\n" + 
    		"}";
	
	private static final String anrChangesFromPolicyToSDNRInput = "{\n" +
            "  \"body\": {\n" +
            "    \"input\": {\n" +
            "      \"CommonHeader\": {\n" +
            "        \"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" +
            "        \"APIVer\": \"1.0\",\n" +
            "        \"RequestID\": \"722ee65a-8afd-48df-bf57-c152ae45bacc\",\n" +
            "        \"SubRequestID\": \"1\",\n" +
            "        \"RequestTrack\": {},\n" +
            "        \"Flags\": {}\n" +
            "      },\n" +
            "\"Action\": \"ModifyConfigANR\",\n" +
            "      \"Payload\": \"{ \\\"Configurations\\\":[ { \\\"data\\\":{ \\\"FAPService\\\":{ \\\"alias\\\":\\\"Cell1\\\", \\\"CellConfig\\\":{ \\\"LTE\\\":{ \\\"RAN\\\":{ \\\"Common\\\":{ \\\"CellIdentity\\\":\\\"1\\\" }, \\\"NeighborListInUse\\\" : { \\\"LTECellNumberOfEntries\\\" : \\\"1\\\" , \\\"LTECell\\\" : [{ \\\"PLMNID\\\" :\\\"plmnid1\\\", \\\"CID\\\":\\\"Chn0001\\\", \\\"PhyCellID\\\":\\\"3\\\", \\\"PNFName\\\":\\\"ncserver01\\\", \\\"Blacklisted\\\":\\\"false\\\"}] } } } } } } } ] }\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"version\": \"1.0\",\n" +
            "  \"rpc-name\": \"modifyconfiganr\",\n" +
            "  \"correlation-id\": \"722ee65a-8afd-48df-bf57-c152ae45bacc-1\",\n" +
            "  \"type\": \"request\"\n" +
            "}\n" +
            "";

	@Before
	public void setUp() throws Exception {
	}
	
	/* ---------- PCI Changes DMAAP messages test cases ------------------- */
	
	@Test
	public void testPCIChangesDmaapRPCMessageBodyResponse() throws Exception {
		Properties props = new Properties();
		
		ObjectMapper oMapper = new ObjectMapper();
		JsonNode pciChangesRootNode = oMapper.readTree(pciChangesFromPolicyToSDNRInput);
		JsonNode body = pciChangesRootNode.get("body");
		JsonNode input = body.get("input");
		JsonNode payload = input.get("Payload");
		String payloadText = payload.asText();
		JsonNode configurationsJsonNode = oMapper.readTree(payloadText);
 
	    String rpcMsgbody = new OofPciPocDmaapConsumers(props).publish("src/main/resources/anr-pci-changes-from-policy-to-sdnr.vt", pciChangesFromPolicyToSDNRInput,configurationsJsonNode, true, false);
        
        JsonNode rootNode;
        try {
        	rootNode = oMapper.readTree(rpcMsgbody);
        } catch (Exception e) {
            throw new InvalidMessageException("Cannot parse json object", e);
        }       

        assertTrue(rootNode.get("input").get("module-name") != null); 
        assertTrue(rootNode.get("input").get("rpc-name") != null); 
        assertTrue(rootNode.get("input").get("mode") != null); 
        assertTrue(rootNode.get("input").get("sli-parameter") != null); 
        
	}
	
	@Test(expected = InvalidMessageException.class)
	public void testPCIChangesDmaapProcessMsgNullMessage() throws Exception {
		OofPciPocDmaapConsumers consumer = new OofPciPocDmaapConsumers();
		consumer.processMsg(null);
	}

	@Test(expected = InvalidMessageException.class)
	public void testPCIChangesDmaapProcessMsgInvalidMessage() throws Exception {
		OofPciPocDmaapConsumers consumer = new OofPciPocDmaapConsumers();
		consumer.processMsg("test");
	}

	@Test
	public void testPCIChangesDmaapProcessMsgMissingActionHeader() throws Exception {
		OofPciPocDmaapConsumers consumer = new OofPciPocDmaapConsumers();
		consumer.processMsg("{\n" + 
				"	\"body\": {\n" + 
				"		\"input\": {\n" + 
				"			\"CommonHeader\": {\n" + 
				"				\"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" + 
				"				\"APIVer\": \"1.0\",\n" + 
				"				\"RequestID\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459\",\n" + 
				"				\"SubRequestID\": \"1\",\n" + 
				"				\"RequestTrack\": {},\n" + 
				"				\"Flags\": {}\n" + 
				"			},\n" + 
				"			\"RenamedAction\": \"ModifyConfig\",\n" + 
				"			\"Payload\": {\n" + 
				"				\"Configurations \": {\n" + 
				"					\"data \": {\n" + 
				"						\"FAPService \": {\n" + 
				"							\"alias\": \"Chn0330\",\n" + 
				"							\"X0005b9Lte\": {\n" + 
				"								\"phyCellIdInUse\": 6,\n" + 
				"								\"pnfName\": \"ncserver23\"\n" + 
				"							},\n" + 
				"							\"CellConfig\": {\n" + 
				"								\"LTE\": {\n" + 
				"									\"RAN\": {\n" + 
				"										\"Common\": {\n" + 
				"											\"CellIdentity\": \"Chn0330\"\n" + 
				"										}\n" + 
				"									}\n" + 
				"								}\n" + 
				"							}\n" + 
				"						}\n" + 
				"					}\n" + 
				"				}\n" + 
				"\n" + 
				"			}\n" + 
				"		}\n" + 
				"	},\n" + 
				"	\"version\": \"1.0\",\n" + 
				"	\"rpc-name\": \"modifyconfig\",\n" + 
				"	\"correlation-id\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459-1\",\n" + 
				"	\"type\": \"request\"\n" + 
				"}");
	}

	@Test
	public void testPCIChangesDmaapProcessMsgInvalidPayloadConfigurations() throws Exception {
		String msg = "{\n" + 
				"	\"body\": {\n" + 
				"		\"input\": {\n" + 
				"			\"CommonHeader\": {\n" + 
				"				\"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" + 
				"				\"APIVer\": \"1.0\",\n" + 
				"				\"RequestID\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459\",\n" + 
				"				\"SubRequestID\": \"1\",\n" + 
				"				\"RequestTrack\": {},\n" + 
				"				\"Flags\": {}\n" + 
				"			},\n" + 
				"			\"Action\": \"ModifyConfig\",\n" + 
				"			\"Payload\": {\n" + 
				"				\"Configurations \": {\n" + 
				"					\"data \": {\n" + 
				"						\"FAPService \": {\n" + 
				"							\"alias\": \"Chn0330\",\n" + 
				"							\"X0005b9Lte\": {\n" + 
				"								\"phyCellIdInUse\": 6,\n" + 
				"								\"pnfName\": \"ncserver23\"\n" + 
				"							},\n" + 
				"							\"CellConfig\": {\n" + 
				"								\"LTE\": {\n" + 
				"									\"RAN\": {\n" + 
				"										\"Common\": {\n" + 
				"											\"CellIdentity\": \"Chn0330\"\n" + 
				"										}\n" + 
				"									}\n" + 
				"								}\n" + 
				"							}\n" + 
				"						}\n" + 
				"					}\n" + 
				"				}\n" + 
				"\n" + 
				"			}\n" + 
				"		}\n" + 
				"	},\n" + 
				"	\"version\": \"1.0\",\n" + 
				"	\"rpc-name\": \"modifyconfig\",\n" + 
				"	\"correlation-id\": \"9d2d790e-a5f0-11e8-98d0-529269fb1459-1\",\n" + 
				"	\"type\": \"request\"\n" + 
				"}";
		
		try {
			OofPciPocDmaapConsumers consumer = new OofPciPocDmaapConsumers();
			consumer.processMsg(msg);

		} catch (final InvalidMessageException e) {
			final String errorMsg = "Configurations is not of Type Array. Could not read configuration changes";
			assertEquals(errorMsg, e.getMessage());
		}
	}
	
	/* ---------- PCI Changes DMAAP messages test cases ------------------- */
	
	/* ---------- ANR Changes DMAAP messages test cases ------------------- */
	
	@Test
	public void testANRChangesDmaapRPCMessageBodyResponse() throws Exception {
		Properties props = new Properties();
		
		ObjectMapper oMapper = new ObjectMapper();
		JsonNode anrChangesRootNode = oMapper.readTree(anrChangesFromPolicyToSDNRInput);
		JsonNode body = anrChangesRootNode.get("body");
		JsonNode input = body.get("input");
		JsonNode payload = input.get("Payload");
		String payloadText = payload.asText();
		JsonNode configurationsJsonNode = oMapper.readTree(payloadText);
		JsonNode configurations = configurationsJsonNode.get("Configurations");
		
		for(JsonNode dataNode:configurations) {
			String rpcMsgbody = new OofPciPocDmaapConsumers(props).publish("src/main/resources/anr-pci-changes-from-policy-to-sdnr.vt", anrChangesFromPolicyToSDNRInput,dataNode, false, true);
	        
	        JsonNode rootNode;
	        try {
	        	rootNode = oMapper.readTree(rpcMsgbody);
	        } catch (Exception e) {
	            throw new InvalidMessageException("Cannot parse json object", e);
	        }       

	        assertTrue(rootNode.get("input").get("module-name") != null); 
	        assertTrue(rootNode.get("input").get("rpc-name") != null); 
	        assertTrue(rootNode.get("input").get("mode") != null); 
	        assertTrue(rootNode.get("input").get("sli-parameter") != null); 
		}
	}
	
	@Test(expected = InvalidMessageException.class)
	public void testANRChangesDmaapProcessMsgNullMessage() throws Exception {
		OofPciPocDmaapConsumers consumer = new OofPciPocDmaapConsumers();
		consumer.processMsg(null);
	}

	@Test(expected = InvalidMessageException.class)
	public void testANRChangesDmaapProcessMsgInvalidMessage() throws Exception {
		OofPciPocDmaapConsumers consumer = new OofPciPocDmaapConsumers();
		consumer.processMsg("test");
	}

	@Test
	public void testANRChangesDmaapProcessMsgMissingActionHeader() throws Exception {
		OofPciPocDmaapConsumers consumer = new OofPciPocDmaapConsumers();
		consumer.processMsg("{\n" +
	            "  \"body\": {\n" +
	            "    \"input\": {\n" +
	            "      \"CommonHeader\": {\n" +
	            "        \"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" +
	            "        \"APIVer\": \"1.0\",\n" +
	            "        \"RequestID\": \"722ee65a-8afd-48df-bf57-c152ae45bacc\",\n" +
	            "        \"SubRequestID\": \"1\",\n" +
	            "        \"RequestTrack\": {},\n" +
	            "        \"Flags\": {}\n" +
	            "      },\n" +
	            "\"NoAction\": \"ModifyConfigANR\",\n" +
	            "      \"Payload\": \"{ \\\"Configurations\\\":[ { \\\"data\\\":{ \\\"FAPService\\\":{ \\\"alias\\\":\\\"Cell1\\\", \\\"CellConfig\\\":{ \\\"LTE\\\":{ \\\"RAN\\\":{ \\\"Common\\\":{ \\\"CellIdentity\\\":\\\"1\\\" }, \\\"NeighborListInUse\\\" : { \\\"LTECellNumberOfEntries\\\" : \\\"1\\\" , \\\"LTECell\\\" : [{ \\\"PLMNID\\\" :\\\"plmnid1\\\", \\\"CID\\\":\\\"Chn0001\\\", \\\"PhyCellID\\\":\\\"3\\\", \\\"PNFName\\\":\\\"ncserver01\\\", \\\"Blacklisted\\\":\\\"false\\\"}] } } } } } } } ] }\"\n" +
	            "    }\n" +
	            "  },\n" +
	            "  \"version\": \"1.0\",\n" +
	            "  \"rpc-name\": \"modifyconfiganr\",\n" +
	            "  \"correlation-id\": \"722ee65a-8afd-48df-bf57-c152ae45bacc-1\",\n" +
	            "  \"type\": \"request\"\n" +
	            "}\n" +
	            "");
	}

	@Test
	public void testANRChangesDmaapProcessMsgInvalidPayloadConfigurations() throws Exception {
		String msg = "{\n" +
	            "  \"body\": {\n" +
	            "    \"input\": {\n" +
	            "      \"CommonHeader\": {\n" +
	            "        \"TimeStamp\": \"2018-11-30T09:13:37.368Z\",\n" +
	            "        \"APIVer\": \"1.0\",\n" +
	            "        \"RequestID\": \"722ee65a-8afd-48df-bf57-c152ae45bacc\",\n" +
	            "        \"SubRequestID\": \"1\",\n" +
	            "        \"RequestTrack\": {},\n" +
	            "        \"Flags\": {}\n" +
	            "      },\n" +
	            "\"Action\": \"ModifyConfigANR\",\n" +
	            "      \"Payload\": \"{ \\\"Configurations\\\":{ { \\\"data\\\":{ \\\"FAPService\\\":{ \\\"alias\\\":\\\"Cell1\\\", \\\"CellConfig\\\":{ \\\"LTE\\\":{ \\\"RAN\\\":{ \\\"Common\\\":{ \\\"CellIdentity\\\":\\\"1\\\" }, \\\"NeighborListInUse\\\" : { \\\"LTECellNumberOfEntries\\\" : \\\"1\\\" , \\\"LTECell\\\" : [{ \\\"PLMNID\\\" :\\\"plmnid1\\\", \\\"CID\\\":\\\"Chn0001\\\", \\\"PhyCellID\\\":\\\"3\\\", \\\"PNFName\\\":\\\"ncserver01\\\", \\\"Blacklisted\\\":\\\"false\\\"}} } } } } } } } ] }\"\n" +
	            "    }\n" +
	            "  },\n" +
	            "  \"version\": \"1.0\",\n" +
	            "  \"rpc-name\": \"modifyconfiganr\",\n" +
	            "  \"correlation-id\": \"722ee65a-8afd-48df-bf57-c152ae45bacc-1\",\n" +
	            "  \"type\": \"request\"\n" +
	            "}\n" +
	            "";
		
		try {
			OofPciPocDmaapConsumers consumer = new OofPciPocDmaapConsumers();
			consumer.processMsg(msg);

		} catch (final InvalidMessageException e) {
			final String errorMsg = "Cannot parse payload value";
			assertEquals(errorMsg, e.getMessage());
		}
	}
	
	/* ---------- ANR Changes DMAAP messages test cases ------------------- */
}
