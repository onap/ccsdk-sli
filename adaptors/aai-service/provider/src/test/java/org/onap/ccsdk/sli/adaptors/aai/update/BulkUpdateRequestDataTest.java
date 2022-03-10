package org.onap.ccsdk.sli.adaptors.aai.update;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.onap.aai.inventory.v24.Subnet;

public class BulkUpdateRequestDataTest {
    @Test
    public void testAddRequestItem() throws JsonProcessingException {

        String action = "patch";
        String uri = "https://localhost/test";
        BulkUpdateRequestItemBody body = new BulkUpdateRequestItemBody();
        body.setOrchestrationStatus("active");
        ObjectMapper mapper = new ObjectMapper();

        String expectedMessage = "{\"operations\": [ { " +
                                    "\"action\": \"patch\"," +
                                    "\"uri\": \"https://localhost/test\"," +
                                    "\"body\": {" +
                                        "\"orchestration-status\": \"active\"}}]}";

        // Create an item 
        BulkUpdateRequestData requestData = new BulkUpdateRequestData();

        requestData.addRequestItem(action, uri, body);

        // Check jackson mapping
        String requestDataStr = mapper.writeValueAsString(requestData);
        assertEquals(expectedMessage.replaceAll("\\s", ""), requestDataStr.replaceAll("\\s", ""));


    }

    @Test
    public void testObjectMapperReadValue() throws JsonProcessingException {
        String action = "patch";
        String uri = "https://localhost/test";
        BulkUpdateRequestItemBody body = new BulkUpdateRequestItemBody();
        body.setOrchestrationStatus("active");
        ObjectMapper mapper = new ObjectMapper();

        String valAsString = "{\"operations\": [ { " +
                                    "\"action\": \"patch\"," +
                                    "\"uri\": \"https://localhost/test\"," +
                                    "\"body\": {" +
                                        "\"orchestration-status\": \"active\"}}]}";

        BulkUpdateRequestData requestData = mapper.readValue(valAsString, BulkUpdateRequestData.class);

        List<BulkUpdateRequestItem> requestItems = requestData.getOperations();
        assertEquals(1, requestItems.size());

        BulkUpdateRequestItem requestItem = requestItems.get(0);
        assertEquals(action, requestItem.getAction());
        assertEquals(uri, requestItem.getUri());
        assertEquals(mapper.writeValueAsString(body).replaceAll("\\s", ""),
            mapper.writeValueAsString(requestItem.getBody()).replaceAll("\\s", ""));
        
    }
}
