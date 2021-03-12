/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.utils.logging;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResolvableErrorEnum;
import com.att.eelf.i18n.EELFResourceManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import org.slf4j.MDC;

import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

/**
 * Logging utilities
 */
public class LoggingUtils {

    private static final EELFLogger errorLogger = EELFManager.getInstance().getErrorLogger();
    private static final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
    private static final EELFLogger metricLogger = EELFManager.getInstance().getMetricsLogger();

    private LoggingUtils() {
        throw new IllegalAccessError("LoggingUtils");
    }


    public static void logErrorMessage(String errorCode, String errorDescription,
            String targetEntity, String targetServiceName, String additionalMessage,
            String className) {
        logError(errorCode, errorDescription, targetEntity, targetServiceName, additionalMessage,
                className);
    }

    public static void logErrorMessage(String targetEntity, String targetServiceName,
            String additionalMessage, String className) {
        logError("", "", targetEntity, targetServiceName, additionalMessage, className);
    }

    public static void logErrorMessage(String targetServiceName, String additionalMessage,
            String className) {
        logError("", "", LoggingConstants.TargetNames.APPC, targetServiceName, additionalMessage,
                className);
    }

    private static void logError(String errorCode, String errorDescription, String targetEntity,
            String targetServiceName, String additionalMessage, String className) {
        populateErrorLogContext(errorCode, errorDescription, targetEntity, targetServiceName,
                className);
        int additionalLength = additionalMessage.length()<100 ? additionalMessage.length() : 100;
        String additionalLog = additionalMessage == null ? "" : additionalMessage.replace("\n", "").substring(0, additionalLength);
        errorLogger.error(additionalLog);
        cleanErrorLogContext();
    }

    public static String formatException(Exception e) {
    	String outStr = EELFResourceManager.format(e);
    	outStr = outStr.replaceAll("[\\n]", "");
    	return outStr;
    }

    public static void logAuditMessage(Instant beginTimeStamp, Instant endTimeStamp, String code,
            String responseDescription, String className) {
        populateAuditLogContext(beginTimeStamp, endTimeStamp, code, responseDescription, className);
        auditLogger.info(EELFResourceManager.format(Msg.APPC_AUDIT_MSG, MDC.get(MDC_SERVICE_NAME),
                MDC.get(LoggingConstants.MDCKeys.TARGET_VIRTUAL_ENTITY),
                MDC.get(LoggingConstants.MDCKeys.PARTNER_NAME), MDC.get(MDC_KEY_REQUEST_ID),
                MDC.get(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP),
                MDC.get(LoggingConstants.MDCKeys.END_TIMESTAMP),
                MDC.get(LoggingConstants.MDCKeys.RESPONSE_CODE)));
        cleanAuditErrorContext();
    }

    public static String formMDCString() {
    	//Remove all newlines, tabs, pipes, escape all commas
    	StringBuilder sb = new StringBuilder();
    	for (String key : LoggingConstants.MDCKeys.MDC_KEYS) {
    		String value = MDC.get(key);

            if (value != null) {
            	value = value.replaceAll("[\\n\\t\\|]", "");
            	value = value.replaceAll(",", "\\\\,");
            }
            else
            	value = "";

            if (sb.length()==0)
            	sb.append(key + "=" + value);
            else
            	sb.append("," + key + "=" + value);
    	}

    	return sb.toString();
    }

    public static String formServerFqdnString() {
    	return (System.getenv("fqdn")!=null) ? System.getenv("fqdn") : "www.appc.com";
    }

    public static void auditInfo(Instant beginTimeStamp, Instant endTimeStamp, String code,
            String responseDescription, String className, EELFResolvableErrorEnum resourceId,
            String... arguments) {
        populateAuditLogContext(beginTimeStamp, endTimeStamp, code, responseDescription, className);
        auditLogger.info(resourceId, arguments);
        cleanAuditErrorContext();
    }

    public static void auditWarn(Instant beginTimeStamp, Instant endTimeStamp, String code,
            String responseDescription, String className, EELFResolvableErrorEnum resourceId,
            String... arguments) {
        populateAuditLogContext(beginTimeStamp, endTimeStamp, code, responseDescription, className);
        auditLogger.warn(resourceId, arguments);
        cleanAuditErrorContext();
    }

    public static void logMetricsMessage(Instant beginTimeStamp, Instant endTimeStamp,
            String targetEntity, String targetServiceName, String statusCode, String responseCode,
            String responseDescription, String className) {
        populateMetricLogContext(beginTimeStamp, endTimeStamp, targetEntity, targetServiceName,
                statusCode, responseCode, responseDescription, className);
        metricLogger.info(EELFResourceManager.format(Msg.APPC_METRIC_MSG, MDC.get(MDC_SERVICE_NAME),
                MDC.get(LoggingConstants.MDCKeys.TARGET_VIRTUAL_ENTITY),
                MDC.get(LoggingConstants.MDCKeys.PARTNER_NAME), MDC.get(MDC_KEY_REQUEST_ID),
                MDC.get(LoggingConstants.MDCKeys.TARGET_ENTITY),
                MDC.get(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME),
                MDC.get(LoggingConstants.MDCKeys.ELAPSED_TIME),
                MDC.get(LoggingConstants.MDCKeys.STATUS_CODE)));
        cleanMetricContext();
    }

    private static void populateAuditLogContext(Instant beginTimeStamp, Instant endTimeStamp,
            String code, String responseDescription, String className) {
        populateTimeContext(beginTimeStamp, endTimeStamp);
        populateRequestContext();
        String statusCode = ("100".equals(code) || "400".equals(code))
                ? LoggingConstants.StatusCodes.COMPLETE : LoggingConstants.StatusCodes.ERROR;
        populateResponseContext(statusCode, code, responseDescription);
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME, className != null ? className : "");
        MDC.put(LoggingConstants.MDCKeys.MDC_STRING, LoggingUtils.formMDCString());
    }

    private static void cleanAuditErrorContext() {
    	cleanRequestContext();
        cleanTimeContext();
        cleanResponseContext();
        cleanErrorContext();
        MDC.remove(LoggingConstants.MDCKeys.CLASS_NAME);
    }

    private static void populateErrorLogContext(String errorCode, String errorDescription,
            String targetEntity, String targetServiceName, String className) {
        populateErrorContext(errorCode, errorDescription);
        populateTargetContext(targetEntity, targetServiceName != null ? targetServiceName : "");
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME, className != null ? className : "");
    }

    private static void cleanErrorLogContext() {
        cleanErrorContext();
        cleanTargetContext();
        MDC.remove(LoggingConstants.MDCKeys.CLASS_NAME);
    }

    private static void populateMetricLogContext(Instant beginTimeStamp, Instant endTimeStamp,
            String targetEntity, String targetServiceName, String statusCode, String responseCode,
            String responseDescription, String className) {
    	populateRequestContext();
        populateTimeContext(beginTimeStamp, endTimeStamp);
        populateTargetContext(targetEntity, targetServiceName);
        populateResponseContext(statusCode, responseCode, responseDescription);
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME, className != null ? className : "");
        MDC.put(LoggingConstants.MDCKeys.MDC_STRING, LoggingUtils.formMDCString());
    }

    private static void cleanMetricContext() {
    	cleanRequestContext();
        cleanTimeContext();
        cleanTargetContext();
        cleanResponseContext();
        MDC.remove(LoggingConstants.MDCKeys.CLASS_NAME);
    }

    private static void populateTargetContext(String targetEntity, String targetServiceName) {
        MDC.put(LoggingConstants.MDCKeys.TARGET_ENTITY, targetEntity != null ? targetEntity : "");
        MDC.put(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME,
                targetServiceName != null ? targetServiceName : "");
    }

    private static void cleanTargetContext() {
        MDC.remove(LoggingConstants.MDCKeys.TARGET_ENTITY);
        MDC.remove(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME);
    }

    private static void populateRequestContext() {
    	try {
    		UUID.fromString(MDC.get(MDC_KEY_REQUEST_ID));
    		//reaching here without exception means existing RequestId is
    		//valid UUID as per ECOMP logging standards, no-op
    	} catch (Exception e) {
    		MDC.put(MDC_KEY_REQUEST_ID, UUID.randomUUID().toString());
    	}

    	try {
    		String partnerName = MDC.get(LoggingConstants.MDCKeys.PARTNER_NAME);

    		//ECOMP logging standards require some value for PartnerName.  Default to appc if empty
    		if (partnerName.isEmpty())
    			MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, "appc");
    	} catch (Exception e) {
    		MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, "appc");
    	}

    	try {
    		String serviceName = MDC.get(MDC_SERVICE_NAME);

    		//ECOMP logging standards require some value for ServiceName.  Default to DEFAULT if empty
    		if (serviceName.isEmpty())
    			MDC.put(MDC_SERVICE_NAME, "DEFAULT");
    	} catch (Exception e) {
    		MDC.put(MDC_SERVICE_NAME, "DEFAULT");
    	}
    }

    private static void cleanRequestContext() {
        MDC.remove(MDC_KEY_REQUEST_ID);
        MDC.remove(LoggingConstants.MDCKeys.PARTNER_NAME);
        MDC.remove(MDC_SERVICE_NAME);
    }

    private static void populateTimeContext(Instant beginTimeStamp, Instant endTimeStamp) {
        String beginTime = "";
        String endTime = "";
        String elapsedTime = "";

        if (beginTimeStamp != null && endTimeStamp != null) {
            elapsedTime = String.valueOf(ChronoUnit.MILLIS.between(beginTimeStamp, endTimeStamp));
            beginTime = generateTimestampStr(beginTimeStamp);
            endTime = generateTimestampStr(endTimeStamp);
        }

        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, beginTime);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, endTime);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, elapsedTime);
    }

    public static String generateTimestampStr(Instant timeStamp) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        TimeZone tz = TimeZone.getTimeZone("UTC");
        df.setTimeZone(tz);
        return df.format(Date.from(timeStamp));
    }

    private static void cleanTimeContext() {
        MDC.remove(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP);
        MDC.remove(LoggingConstants.MDCKeys.END_TIMESTAMP);
        MDC.remove(LoggingConstants.MDCKeys.ELAPSED_TIME);
    }

    private static void populateResponseContext(String statusCode, String responseCode,
            String responseDescription) {
        MDC.put(LoggingConstants.MDCKeys.STATUS_CODE, statusCode != null ? statusCode : "");
        MDC.put(LoggingConstants.MDCKeys.RESPONSE_CODE, responseCode);
        MDC.put(LoggingConstants.MDCKeys.RESPONSE_DESCRIPTION,
                responseDescription != null ? responseDescription : "");
    }

    private static void cleanResponseContext() {
        MDC.remove(LoggingConstants.MDCKeys.STATUS_CODE);
        MDC.remove(LoggingConstants.MDCKeys.RESPONSE_CODE);
        MDC.remove(LoggingConstants.MDCKeys.RESPONSE_DESCRIPTION);
    }

    private static void populateErrorContext(String errorCode, String errorDescription) {
    	String pattern = "[1-9]00";
    	//Logging specs mandate errorCode of 100|200|300|400|500|600|700|800|900
    	String errorValue = (errorCode!=null && errorCode.matches(pattern)) ? errorCode : "900";
    	MDC.put(LoggingConstants.MDCKeys.ERROR_CODE, errorValue);
        MDC.put(LoggingConstants.MDCKeys.ERROR_DESCRIPTION, errorDescription);
    }

    private static void cleanErrorContext() {
        MDC.remove(LoggingConstants.MDCKeys.ERROR_CODE);
        MDC.remove(LoggingConstants.MDCKeys.ERROR_DESCRIPTION);
    }

}
