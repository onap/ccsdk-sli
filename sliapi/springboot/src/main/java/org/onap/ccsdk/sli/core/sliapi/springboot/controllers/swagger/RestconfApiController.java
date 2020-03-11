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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceBase;
import org.onap.ccsdk.sli.core.sliapi.model.ExecuteGraphInput;
import org.onap.ccsdk.sli.core.sliapi.model.ResponseFields;
import org.onap.ccsdk.sli.core.sliapi.model.TestResult;
import org.onap.ccsdk.sli.core.sliapi.model.TestResults;
import org.onap.ccsdk.sli.core.sliapi.springboot.controllers.data.TestResultConfig;
import org.onap.ccsdk.sli.core.sliapi.springboot.controllers.data.TestResultsConfigRepository;
import org.onap.ccsdk.sli.core.sliapi.springboot.controllers.data.TestResultsOperationalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-02-20T12:50:11.207-05:00")

@Controller
@ComponentScan(basePackages = {"org.onap.ccsdk.sli.core.sliapi.springboot.*"})
@EntityScan("org.onap.ccsdk.sli.core.sliapi.springboot.*")
public class RestconfApiController implements RestconfApi {

	private final ObjectMapper objectMapper;
	private final HttpServletRequest request;

    @Autowired
    protected SvcLogicServiceBase svc;

    @Autowired
	private TestResultsConfigRepository testResultsConfigRepository;

    @Autowired
	private TestResultsOperationalRepository testResultsOperationalRepository;

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

	@Override
	public ResponseEntity<Void> deleteTestResult(String testIdentifier) {

		List<TestResultConfig> testResultConfigs = testResultsConfigRepository.findByTestIdentifier(testIdentifier);

		if (testResultConfigs != null) {
			Iterator<TestResultConfig> testResultConfigIterator = testResultConfigs.iterator();
			while (testResultConfigIterator.hasNext()) {
				testResultsConfigRepository.delete(testResultConfigIterator.next());
			}
		}

		return (new ResponseEntity<>(HttpStatus.OK));
	}

	@Override
	public ResponseEntity<Void> deleteTestResults() {

		testResultsConfigRepository.deleteAll();

		return (new ResponseEntity<>(HttpStatus.OK));
	}

	@Override
	public ResponseEntity<TestResults> gETTestResults() {

		TestResults results = new TestResults();

		testResultsOperationalRepository.findAll().forEach(testResult -> {
			TestResult item = null;
			try {
				item = objectMapper.readValue(testResult.getResults(), TestResult.class);
				results.addTestResultsItem(item);
			} catch (JsonProcessingException e) {
				log.error("Could not convert testResult", e);
			}
		});


		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<TestResult> getTestResult(String testIdentifier) {
		List<TestResultConfig> testResultConfigs = testResultsConfigRepository.findByTestIdentifier(testIdentifier);

		if ((testResultConfigs == null) || (testResultConfigs.size() == 0)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			TestResultConfig testResultConfig = testResultConfigs.get(0);
			TestResult testResult = null;
			try {
				testResult = objectMapper.readValue(testResultConfig.getResults(), TestResult.class);
			} catch (JsonProcessingException e) {
				log.error("Cannot convert test result", e);
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}


			return new ResponseEntity<>(testResult, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<TestResults> getTestResults() {
		if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
		} else {
			log.warn("ObjectMapper or HttpServletRequest not configured in default RestconfApi interface so no example is generated");
		}

		TestResults results = new TestResults();

		testResultsConfigRepository.findAll().forEach(testResult -> {
			TestResult item = null;
			try {
				item = objectMapper.readValue(testResult.getResults(), TestResult.class);
				results.addTestResultsItem(item);
			} catch (JsonProcessingException e) {
				log.error("Could not convert testResult", e);
			}
		});


		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<TestResult> pUTTestResult(String testIdentifier, @Valid TestResult testResult) {
		if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
		} else {
			log.warn("ObjectMapper or HttpServletRequest not configured in default RestconfApi interface so no example is generated");
		}

		List<TestResultConfig> testResultConfigs = testResultsConfigRepository.findByTestIdentifier(testIdentifier);
		Iterator<TestResultConfig> testResultIter = testResultConfigs.iterator();
		while (testResultIter.hasNext()) {
			testResultsConfigRepository.delete(testResultIter.next());
		}

		TestResultConfig testResultConfig = null;
		try {
			testResultConfig = new TestResultConfig(testResult.getTestIdentifier(), objectMapper.writeValueAsString(testResult));
			testResultsConfigRepository.save(testResultConfig);
		} catch (JsonProcessingException e) {
			log.error("Could not save test result", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(testResult, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<TestResults> postTestResults(@Valid TestResults testResults) {
		List<TestResult> resultList = testResults.getTestResults();

		Iterator<TestResult> resultIterator = resultList.iterator();

		while (resultIterator.hasNext()) {
			TestResult curResult = resultIterator.next();
			try {
				testResultsConfigRepository.save(new TestResultConfig(curResult.getTestIdentifier(), objectMapper.writeValueAsString(curResult)));
			} catch (JsonProcessingException e) {
				log.error("Could not save test result", e);
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(testResults, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<TestResults> putTestResults(@Valid TestResults testResults) {
		testResultsConfigRepository.deleteAll();

		List<TestResult> resultList = testResults.getTestResults();

		Iterator<TestResult> resultIterator = resultList.iterator();


		while (resultIterator.hasNext()) {
			TestResult curResult = resultIterator.next();
			try {
				testResultsConfigRepository.save(new TestResultConfig(curResult.getTestIdentifier(), objectMapper.writeValueAsString(curResult)));
			} catch (JsonProcessingException e) {
				log.error("Could not save test result", e);
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		return new ResponseEntity<>(testResults, HttpStatus.OK);
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
