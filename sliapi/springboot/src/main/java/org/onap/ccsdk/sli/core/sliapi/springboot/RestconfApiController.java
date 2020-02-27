package org.onap.ccsdk.sli.core.sliapi.springboot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.onap.ccsdk.sli.core.sli.*;
import org.onap.ccsdk.sli.core.sli.provider.base.*;
import org.onap.ccsdk.sli.core.sliapi.model.ExecuteGraphInput;
import org.onap.ccsdk.sli.core.sliapi.model.ResponseFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-02-20T12:50:11.207-05:00")

@Controller
public class RestconfApiController implements RestconfApi {

	private final ObjectMapper objectMapper;
	private final HttpServletRequest request;


	private static SvcLogicServiceBase svc;
	private static final Logger log = LoggerFactory.getLogger(RestconfApiController.class);

	@org.springframework.beans.factory.annotation.Autowired
	public RestconfApiController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;

		SvcLogicPropertiesProvider propProvider = new SvcLogicPropertiesProvider() {

			@Override
			public Properties getProperties() {
				Properties props = new Properties();
				String propPath = "src/main/resources/svclogic.properties";
				System.out.println(propPath);
				try (FileInputStream fileInputStream = new FileInputStream(propPath)) {
					props = new Properties();
					props.load(fileInputStream);
				} catch (final IOException e) {
					log.error("Failed to load properties for file: {}", propPath,
							new ConfigurationException("Failed to load properties for file: " + propPath, e));
				}
				return props;
			}
		};

		SvcLogicStore store = null;
		try {
			store = SvcLogicStoreFactory.getSvcLogicStore(propProvider.getProperties());
		} catch (SvcLogicException e) {
			log.error("Cannot create SvcLogicStore", e);
			return;
		}

		String serviceLogicDirectory = System.getProperty("serviceLogicDirectory", "src/main/resources");
		System.out.println("serviceLogicDirectory is " + serviceLogicDirectory);
		SvcLogicLoader loader = new SvcLogicLoader(serviceLogicDirectory, store);

		try {
			loader.loadAndActivate();
		} catch (IOException e) {
			log.error("Cannot load directed graphs", e);
		}
		SvcLogicResolver resolver = new HashMapResolver();

		svc = new SvcLogicServiceImplBase(store, resolver);

	}

	@Override
	public ResponseEntity<ResponseFields> healthcheck() {
		ResponseFields resp = new ResponseFields();

		try {
			log.info("Calling SLI-API:healthcheck DG");
			Properties inputProps = new Properties();
			Properties respProps = svc.execute("sli", "healthcheck", null, "sync", inputProps);

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
			Properties inputProps = new Properties();
			Properties respProps = svc.execute("sli", "vlbcheck", null, "sync", inputProps);

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
		Properties parms = new Properties();
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

		writeResponseToCtx(passthroughObj.toString(), parms, "input");


		try {
			// Any of these can throw a nullpointer exception
			String calledModule = executeGraphInput.getInput().getModuleName();
			String calledRpc = executeGraphInput.getInput().getRpcName();
			String modeStr = executeGraphInput.getInput().getMode();
			// execute should only throw a SvcLogicException
			Properties respProps = svc.execute(calledModule, calledRpc, null, modeStr, parms);

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

	public static void writeResponseToCtx(String resp, Properties ctx, String prefix) {
		JsonParser jp = new JsonParser();
		JsonElement element = jp.parse(resp);
		writeJsonObject(element.getAsJsonObject(), ctx, prefix + ".");
	}

	public static void writeJsonObject(JsonObject obj, Properties ctx, String root) {
		for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
			if (entry.getValue().isJsonObject()) {
				writeJsonObject(entry.getValue().getAsJsonObject(), ctx, root + entry.getKey() + ".");
			} else if (entry.getValue().isJsonArray()) {
				JsonArray array = entry.getValue().getAsJsonArray();
				ctx.put(root + entry.getKey() + "_length", String.valueOf(array.size()));
				Integer arrayIdx = 0;
				for (JsonElement element : array) {
					if (element.isJsonObject()) {
						writeJsonObject(element.getAsJsonObject(), ctx, root + entry.getKey() + "[" + arrayIdx + "].");
					}
					arrayIdx++;
				}
			} else {
				if (entry.getValue() instanceof JsonNull) {
					log.info("Skipping parameter "+entry.getKey()+" with null value");

				} else {
					ctx.put(root + entry.getKey(), entry.getValue().getAsString());
				}
			}
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
