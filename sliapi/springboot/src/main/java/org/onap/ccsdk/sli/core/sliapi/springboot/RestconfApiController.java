package org.onap.ccsdk.sli.core.sliapi.springboot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicLoader;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.onap.ccsdk.sli.core.sli.provider.base.HashMapResolver;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicPropertiesProvider;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicResolver;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceBase;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceImplBase;
import org.onap.ccsdk.sli.core.sliapi.model.ResponseFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

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

		String serviceLogicDirectory = System.getProperty("serviceLogicDirectory");
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
			if (respProps == null) {
				log.info("DG execution returned no properties!");
			} else {
				log.info("DG execution returned properties");
				for (String key : respProps.stringPropertyNames()) {
					log.info("DG returned property " + key + " = " + respProps.getProperty(key));
				}
			}
			resp.setAckFinalIndicator(respProps.getProperty("ack-final-indicator", "Y"));
			resp.setResponseCode(respProps.getProperty("error-code", "200"));
			resp.setResponseMessage(respProps.getProperty("error-message", "Success"));

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
	public Optional<ObjectMapper> getObjectMapper() {
		return Optional.ofNullable(objectMapper);
	}

	@Override
	public Optional<HttpServletRequest> getRequest() {
		return Optional.ofNullable(request);
	}

}
