/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

/**
 *
 */
package org.onap.ccsdk.sli.core.sli;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @author dt5972
 *
 */
public class MetricLogger {

    private static final Logger METRIC = LoggerFactory.getLogger("org.onap.ccsdk.sli.core.filters.metric");
    
    //TODO use ONAPLogConstants
    public static final String BEGIN_TIMESTAMP = "InvokeTimestamp";
    public static final String LOG_TIMESTAMP = "LogTimestamp";
    public static final String REQUEST_ID = "RequestID";
    public static final String SERVICE_INSTANCE_ID = "ServiceInstanceID";
    public static final String TARGET_ENTITY = "TargetEntity";
    public static final String TARGET_SERVICE_NAME = "TargetServiceName";
    public static final String STATUS_CODE = "StatusCode";
    public static final String RESPONSE_CODE = "ResponseCode";
    public static final String RESPONSE_DESCRIPTION = "ResponseDesc";
    public static final String INSTANCE_UUID = "InstanceID";
    public static final String ELAPSED_TIME = "ElapsedTime";
    public static final String CLIENT_IP = "ClientIPaddress";
    public static final String TARGET_VIRTUAL_ENTITY = "TargetElement";
    private static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");
    private static final Marker INVOKE = MarkerFactory.getMarker("INVOKE");

    private String lastMsg = null;

    public String getRequestID() {
        return MDC.get(REQUEST_ID);
    }
    
    public MetricLogger() {

    }

    public static String asIso8601(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SS'+00:00'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    public static String asIso8601(long tsInMillis) {
        return MetricLogger.asIso8601(new Date(tsInMillis));
    }

    @Deprecated
    public void logRequest(String svcInstanceId, String svcName, String partnerName, String targetEntity, String targetServiceName, String targetVirtualEntity, String msg) {
        logRequest(svcInstanceId,targetEntity,targetServiceName,targetVirtualEntity,msg);
    }

    public void logRequest(String svcInstanceId, String targetEntity, String targetServiceName, String targetVirtualEntity, String msg) {
        long start = System.currentTimeMillis();
        MDC.put(BEGIN_TIMESTAMP, MetricLogger.asIso8601(start));

        if (svcInstanceId != null) {
            MDC.put(SERVICE_INSTANCE_ID, svcInstanceId);
        }
        if (targetEntity != null) {
            MDC.put(TARGET_ENTITY, targetEntity);
        }

        if (targetServiceName != null) {
            MDC.put(TARGET_SERVICE_NAME, targetServiceName);
        }

        if (targetVirtualEntity != null) {
            MDC.put(TARGET_VIRTUAL_ENTITY, targetVirtualEntity);
        }
        this.lastMsg = msg;
        METRIC.info(INVOKE, "Invoke");
    }
    
    public void logResponse(String statusCode, String responseCode, String responseDescription) {
        long start = System.currentTimeMillis();
        MDC.put(BEGIN_TIMESTAMP, MetricLogger.asIso8601(start));

        if (statusCode != null) {
            MDC.put(STATUS_CODE, statusCode);
        }
        if (responseCode != null) {
            MDC.put(RESPONSE_CODE, responseCode);
        }
        if (responseDescription != null) {
            MDC.put(RESPONSE_DESCRIPTION, formatString(responseDescription));
        }
        long end = System.currentTimeMillis();
        MDC.put(LOG_TIMESTAMP, MetricLogger.asIso8601(end));
        MDC.put(ELAPSED_TIME, Long.toString(end-start));
        METRIC.info(INVOKE_RETURN, formatString(lastMsg));
        resetContext();
    }

    protected String formatString(String str) {
        if (str != null) {
            str = str.replaceAll("\\R", ""); // this will strip all new line characters
            str = str.replaceAll("\\|", "%7C"); // log records should not contain a pipe, encode the pipe character
        }
        return str;
    }

    public static void resetContext() {
        MDC.remove(TARGET_ENTITY);
        MDC.remove(TARGET_SERVICE_NAME);
        MDC.remove(TARGET_VIRTUAL_ENTITY);
        MDC.remove(STATUS_CODE);
        MDC.remove(RESPONSE_CODE);
        MDC.remove(RESPONSE_DESCRIPTION);
    }
}
