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

package org.onap.ccsdk.sli.core.sliapi.springboot.controllers.swagger;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceBase;
import org.onap.ccsdk.sli.core.sliapi.model.ExecuteGraphInput;
import org.onap.ccsdk.sli.core.sliapi.model.ResponseFields;
import org.onap.ccsdk.sli.core.sliapi.springboot.controllers.swagger.RestconfApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-02-20T12:50:11.207-05:00")

@Controller
public class RestconfApiController implements RestconfApi {

	private final ObjectMapper objectMapper;
	private final HttpServletRequest request;

  @Autowired
  protected SvcLogicServiceBase svc;
	private static final Logger log = LoggerFactory.getLogger(RestconfApiController.class);

	@org.springframework.beans.factory.annotation.Autowired
	public RestconfApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@Override
	public ResponseEntity<ResponseFields> healthcheck() {
		ResponseFields resp = new ResponseFields();

		try {
			log.info("Calling SLI-API:healthcheck DG");
			SvcLogicContext ctxIn = new SvcLogicContext();
			SvcLogicContext ctxOut = svc.execute("sli", "healthcheck", null, "sync", ctxIn);
			Properties respProps = ctxOut.toProperties();

			resp.setAckFinalIndicator(respProps.getProperty("ack-final-indicator", "Y"));
			resp.setResponseCode(respProps.getProperty("error-code", "200"));
			resp.setResponseMessage(respProps.getProperty("error-message", "Success"));
			resp.setContextMemoryJson(propsToJson(respProps, "context-memory"));

			return (new ResponseEntity<>(resp, HttpStatus.OK));
		} catch (Exception e) {
			resp.setAckFinalIndicator("true");
			resp.setResponseCode("500");
			resp.setResponseMessage(e.getMessage());
			log.error("Error calling healthcheck directed graph", e);

		}
		return (new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@Override
	public ResponseEntity<ResponseFields> vlbcheck() {
		ResponseFields resp = new ResponseFields();

		try {
			log.info("Calling SLI-API:vlbcheck DG");
			SvcLogicContext ctxIn = new SvcLogicContext();
			SvcLogicContext ctxOut = svc.execute("sli", "vlbcheck", null, "sync", ctxIn);
			Properties respProps = ctxOut.toProperties();
			resp.setAckFinalIndicator(respProps.getProperty("ack-final-indicator", "Y"));
			resp.setResponseCode(respProps.getProperty("error-code", "200"));
			resp.setResponseMessage(respProps.getProperty("error-message", "Success"));
			resp.setContextMemoryJson(propsToJson(respProps, "context-memory"));

			return (new ResponseEntity<>(resp, HttpStatus.OK));
		} catch (Exception e) {
			resp.setAckFinalIndicator("true");
			resp.setResponseCode("500");
			resp.setResponseMessage(e.getMessage());
			log.error("Error calling vlbcheck directed graph", e);

		}
		return (new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR));
	}


	@Override
	public Optional<ObjectMapper> getObjectMapper() {
		return Optional.ofNullable(objectMapper);
	}

	@Override
	public Optional<HttpServletRequest> getRequest() {
		return Optional.ofNullable(request);
	}

	@Override
	public ResponseEntity<ResponseFields> executeGraph(@Valid ExecuteGraphInput executeGraphInput) {
		SvcLogicContext ctxIn = new SvcLogicContext();
		ResponseFields resp = new ResponseFields();
		String executeGraphInputJson = null;

		try {
			 executeGraphInputJson = objectMapper.writeValueAsString(executeGraphInput);
			 log.info("Input as JSON is "+executeGraphInputJson);
		} catch (JsonProcessingException e) {

			resp.setAckFinalIndicator("true");
			resp.setResponseCode("500");
			resp.setResponseMessage(e.getMessage());
			log.error("Cannot create JSON from input object", e);
			return (new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR));

		}
		JsonObject jsonInput = new Gson().fromJson(executeGraphInputJson, JsonObject.class);
		JsonObject passthroughObj = jsonInput.get("input").getAsJsonObject();

		ctxIn.mergeJson("input", passthroughObj.toString());

		try {
			// Any of these can throw a nullpointer exception
			String calledModule = executeGraphInput.getInput().getModuleName();
			String calledRpc = executeGraphInput.getInput().getRpcName();
			String modeStr = executeGraphInput.getInput().getMode();
			// execute should only throw a SvcLogicException
			SvcLogicContext ctxOut = svc.execute(calledModule, calledRpc, null, modeStr, ctxIn);
			Properties respProps = ctxOut.toProperties();

			resp.setAckFinalIndicator(respProps.getProperty("ack-final-indicator", "Y"));
			resp.setResponseCode(respProps.getProperty("error-code", "200"));
			resp.setResponseMessage(respProps.getProperty("error-message", "SUCCESS"));
			resp.setContextMemoryJson(propsToJson(respProps, "context-memory"));
			return (new ResponseEntity<>(resp, HttpStatus.valueOf(Integer.parseInt(resp.getResponseCode()))));

		} catch (NullPointerException npe) {
			resp.setAckFinalIndicator("true");
			resp.setResponseCode("500");
			resp.setResponseMessage("Check that you populated module, rpc and or mode correctly.");

			return (new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR));
		} catch (SvcLogicException e) {
			resp.setAckFinalIndicator("true");
			resp.setResponseCode("500");
			resp.setResponseMessage(e.getMessage());

			return (new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR));
		}
	}

	public static String propsToJson(Properties props, String root)
	{
		StringBuffer sbuff = new StringBuffer();

		sbuff.append("{ \""+root+"\" : { ");
		boolean needComma = false;
		for (Map.Entry<Object, Object> prop : props.entrySet()) {
			sbuff.append("\""+(String) prop.getKey()+"\" : \""+(String)prop.getValue()+"\"");
			if (needComma) {
				sbuff.append(" , ");
			} else {
				needComma = true;
			}
		}
		sbuff.append(" } }");

		return(sbuff.toString());
	}

}
