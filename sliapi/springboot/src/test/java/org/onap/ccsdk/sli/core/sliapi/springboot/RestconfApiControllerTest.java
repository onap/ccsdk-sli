/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.sli.core.sliapi.springboot;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.sli.core.sliapi.model.ExecuteGraphInput;
import org.onap.ccsdk.sli.core.sliapi.model.ExecutegraphinputInput;
import org.onap.ccsdk.sli.core.sliapi.model.ResponseFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestconfApiControllerTest {

  private static final Logger log = LoggerFactory.getLogger(RestconfApiControllerTest.class);

  @Autowired
  private MockMvc mvc;

  @Test
  public void testHealthcheck() throws Exception {
    String url = "/restconf/operations/SLI-API:healthcheck";

    MvcResult mvcResult =
        mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(""))
            .andReturn();

    assertEquals(200, mvcResult.getResponse().getStatus());
  }

  @Test
  public void testVlbcheck() throws Exception {
    String url = "/restconf/operations/SLI-API:vlbcheck";

    MvcResult mvcResult =
        mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(""))
            .andReturn();

    assertEquals(200, mvcResult.getResponse().getStatus());
  }

  @Test
  public void testExecuteHealthcheck() throws Exception {
    String url = "/restconf/operations/SLI-API:execute-graph";

    ExecuteGraphInput executeGraphInput = new ExecuteGraphInput();
    ExecutegraphinputInput executeGraphData = new ExecutegraphinputInput();

    executeGraphData.setModuleName("sli");
    executeGraphData.setRpcName("healthcheck");
    executeGraphData.setMode("sync");
    executeGraphInput.setInput(executeGraphData);

    String jsonString = mapToJson(executeGraphInput);
    log.error("jsonString is {}", jsonString);

    MvcResult mvcResult =
        mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonString))
            .andReturn();

    assertEquals(200, mvcResult.getResponse().getStatus());

  }

  @Test
  public void testExecuteMissingDg() throws Exception {
    String url = "/restconf/operations/SLI-API:execute-graph";

    ExecuteGraphInput executeGraphInput = new ExecuteGraphInput();
    ExecutegraphinputInput executeGraphData = new ExecutegraphinputInput();

    executeGraphData.setModuleName("sli");
    executeGraphData.setRpcName("noSuchRPC");
    executeGraphData.setMode("sync");
    executeGraphInput.setInput(executeGraphData);

    String jsonString = mapToJson(executeGraphInput);

    log.error("jsonString is {}", jsonString);

    MvcResult mvcResult =
        mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonString))
            .andReturn();

    assertEquals(401, mvcResult.getResponse().getStatus());

  }

  private String mapToJson(Object obj) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(obj);
  }

  private ResponseFields respFromJson(String jsonString) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return (objectMapper.readValue(jsonString, ResponseFields.class));
  }
}
