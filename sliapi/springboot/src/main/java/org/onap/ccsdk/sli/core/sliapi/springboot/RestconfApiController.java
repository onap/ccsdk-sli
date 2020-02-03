package org.onap.ccsdk.sli.core.sliapi.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import org.onap.ccsdk.sli.core.sli.*;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicClassResolver;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicServiceImpl;
import org.onap.ccsdk.sli.core.sli.provider.base.InMemorySvcLogicStore;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicPropertiesProvider;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicResolver;
import org.onap.ccsdk.sli.core.sliapi.model.ExecuteGraphInput;
import org.onap.ccsdk.sli.core.sliapi.model.HealthcheckInput;
import org.onap.ccsdk.sli.core.sliapi.model.ResponseFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2020-02-20T12:50:11.207-05:00")

@Controller
public class RestconfApiController implements RestconfApi {

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private static SvcLogicService svc;
    Logger log = LoggerFactory.getLogger(RestconfApiController.class);

    @org.springframework.beans.factory.annotation.Autowired
    public RestconfApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;

        SvcLogicPropertiesProvider propProvider = new SvcLogicPropertiesProviderImpl();
        SvcLogicStore store = null;
        try {
            store = SvcLogicStoreFactory.getSvcLogicStore(propProvider.getProperties());
        } catch (SvcLogicException e) {
            log.error("Cannot create SvcLogicStore", e);
            return;
        }

        String serviceLogicDirectory = System.getProperty("serviceLogicDirectory");
        System.out.println("serviceLogicDirectory is "+serviceLogicDirectory);
        SvcLogicLoader loader = new SvcLogicLoader(serviceLogicDirectory, store);

        try {
            loader.loadAndActivate();
        } catch (IOException e) {
            log.error("Cannot load directed graphs", e);
        }
        SvcLogicResolver resolver = new SvcLogicClassResolver();


        try {
            svc = new SvcLogicServiceImpl(new SvcLogicPropertiesProviderImpl(), resolver);
        } catch (SvcLogicException e) {
            log.error("Cannot execute directed graph", e);
        }
    }

    @Override
    public ResponseEntity<ResponseFields> healthcheck() {
        SvcLogicContext ctx = new SvcLogicContext();
        ResponseFields resp = new ResponseFields();


        try {

            log.info("Calling SLI-API:healthcheck DG");
            Properties inputProps = new Properties();
            Properties respProps = svc.execute("sli", "healthcheck" , null, "sync", inputProps);
            if (respProps == null) {
                log.info("DG execution returned no properties!");
            } else {
                log.info("DG execution returned properties");
                for (String key: respProps.stringPropertyNames()) {
                    log.info("DG returned property "+key+" = "+respProps.getProperty(key));
                }
            }
            resp.setAckFinalIndicator(respProps.getProperty("ack-final-indicator", "Y"));
            resp.setResponseCode(respProps.getProperty("error-code", "200"));
            resp.setResponseMessage(respProps.getProperty("error-message", "Success"));

            return(new ResponseEntity<>(resp, HttpStatus.OK));
        }
        catch (Exception e) {
            resp.setAckFinalIndicator("true");
            resp.setResponseCode("500");
            resp.setResponseMessage(e.getMessage());
            log.error("Error calling healthcheck directed graph", e);

        }

        return(new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR));

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
