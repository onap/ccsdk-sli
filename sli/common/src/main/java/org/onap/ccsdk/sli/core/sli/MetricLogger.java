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

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author dt5972
 *
 */
public class MetricLogger {

    private static final Logger KARAF = LoggerFactory.getLogger(MetricLogger.class);
    private static final Logger METRIC = LoggerFactory.getLogger("org.onap.ccsdk.sli.core.filters.metric");

    public static final String BEGIN_TIMESTAMP = "X-ECOMP-BeginTimestamp";
    public static final String END_TIMESTAMP = "X-ECOMP-EndTimestamp";
    public static final String REQUEST_ID = "X-ECOMP-RequestID";
    public static final String SERVICE_INSTANCE_ID = "X-ECOMP-ServiceInstanceID";
    public static final String SERVICE_NAME = "X-ECOMP-ServiceName";
    public static final String PARTNER_NAME = "X-ECOMP-PartnerName";
    public static final String TARGET_ENTITY = "X-ECOMP-TargetEntity";
    public static final String TARGET_SERVICE_NAME = "X-ECOMP-TargetServiceName";
    public static final String STATUS_CODE = "X-ECOMP-StatusCode";
    public static final String RESPONSE_CODE = "X-ECOMP-ResponseCode";
    public static final String RESPONSE_DESCRIPTION = "X-ECOMP-ResponseDescription";
    public static final String INSTANCE_UUID = "X-ECOMP-InstanceUUID";
    public static final String CATEGORY_LOG_LEVEL = "X-ECOMP-CategoryLogLevel";
    public static final String SEVERITY = "X-ECOMP-Severity";
    public static final String SERVER_IP_ADDRESS = "X-ECOMP-ServerIpAddress";
    public static final String ELAPSED_TIME = "X-ECOMP-ElapsedTime";
    public static final String SERVER = "X-ECOMP-Server";
    public static final String CLIENT_IP = "X-ECOMP-ClientIp";
    public static final String CLASS_NAME = "X-ECOMP-ClassName";
    public static final String TARGET_VIRTUAL_ENTITY =  "X-ECOMP-TargetVirtualEntity";

    private long beginTimestamp;
    private String lastMsg = null;

    public MetricLogger() {
        beginTimestamp = System.currentTimeMillis();

        try {
            InetAddress localhost = InetAddress.getLocalHost();
            setServerIpAddress(localhost.getHostAddress());
            setServer(localhost.getCanonicalHostName());
        } catch (Exception e) {
            KARAF.error("Could not get localhost", e);
        }

    }


    public String getBeginTimestamp() {
        return MDC.get(BEGIN_TIMESTAMP);
    }

    private void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
        MDC.put(BEGIN_TIMESTAMP, MetricLogger.asIso8601(beginTimestamp));
    }

    public String getEndTimestamp() {
        return MDC.get(END_TIMESTAMP);
    }

    private void setEndTimestamp(long endTimestamp) {
        // Set MDC with formatted time stamp
        MDC.put(END_TIMESTAMP, MetricLogger.asIso8601(endTimestamp));

        // Set elapsed time
        setElapsedTime(endTimestamp - beginTimestamp);

    }

    public String getRequestID() {
        return MDC.get(REQUEST_ID);
    }


    public String getServiceInstanceID() {
        return MDC.get(SERVICE_INSTANCE_ID);
    }

    private void setServiceInstanceID(String svcInstanceId) {
        MDC.put(SERVICE_INSTANCE_ID, svcInstanceId);
    }

    public String getServiceName() {
        return MDC.get(SERVICE_NAME);
    }

    private void setServiceName(String svcName) {
        MDC.put(SERVICE_NAME, svcName);
    }

    public String getPartnerName() {
        return MDC.get(PARTNER_NAME);
    }

    private void setPartnerName(String partnerName) {
        MDC.put(PARTNER_NAME, partnerName);
    }

    public String getTargetEntity() {
        return MDC.get(TARGET_ENTITY);
    }

    private void setTargetEntity(String targetEntity) {
        MDC.put(TARGET_ENTITY, targetEntity);
    }

    public String getTargetServiceName() {
        return MDC.get(TARGET_SERVICE_NAME);
    }

    private void setTargetServiceName(String targetServiceName) {
        MDC.put(TARGET_SERVICE_NAME, targetServiceName);
    }

    public String getStatusCode() {
        return MDC.get(STATUS_CODE);
    }

    private void setStatusCode(String statusCode) {
        MDC.put(STATUS_CODE, statusCode);
    }

    public String getResponseCode() {
        return MDC.get(RESPONSE_CODE);
    }

    private void setResponseCode(String responseCode) {
        MDC.put(RESPONSE_CODE, responseCode);
    }

    public String getResponseDescription() {
        return MDC.get(RESPONSE_DESCRIPTION);
    }

    private void setResponseDescription(String responseDesc) {
        MDC.put(RESPONSE_DESCRIPTION, formatString(responseDesc));
    }

    public String getInstanceUUID() {
	return MDC.get(INSTANCE_UUID);
    }

    private void setInstanceUUID(String instanceUUID) {
	MDC.put(INSTANCE_UUID, instanceUUID);
    }

    public String getCategoryLogLevel() {
	return MDC.get(CATEGORY_LOG_LEVEL);
    }

    private void setCategoryLogLevel(String categoryLogLevel) {
	MDC.put(CATEGORY_LOG_LEVEL, categoryLogLevel);
    }

    public String getSeverity() {
        return MDC.get(SEVERITY);
    }

    private void setSeverity(String severity) {
        MDC.put(SEVERITY, severity);
    }

    public String getServerIpAddress() {
        return MDC.get(SERVER_IP_ADDRESS);
    }

    private void setServerIpAddress(String serverIpAddress) {
        MDC.put(SERVER_IP_ADDRESS, serverIpAddress);
    }

    public String getElapsedTime() {
        return MDC.get(ELAPSED_TIME);
    }

    private void setElapsedTime(long elapsedTime) {
        MDC.put(ELAPSED_TIME, Long.toString(elapsedTime));
    }

    public String getServer() {
        return MDC.get(SERVER);
    }

    private void setServer(String server) {
        MDC.put(SERVER, server);
    }

    public String getClientIp() {
        return MDC.get(CLIENT_IP);
    }

    private void setClientIp(String clientIp) {
        MDC.put(CLIENT_IP, clientIp);
    }

    public String getClassName() {
        return MDC.get(CLASS_NAME);
    }

    private void setClassName(String className) {
        MDC.put(CLASS_NAME, className);
    }

    public String getTargetVirtualEntity() {
        return MDC.get(TARGET_VIRTUAL_ENTITY);
    }

    private void setTargetVirtualEntity(String targetVirtualEntity) {
        MDC.put(TARGET_VIRTUAL_ENTITY, targetVirtualEntity);
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

    public void logRequest(String svcInstanceId, String svcName, String partnerName, String targetEntity, String targetServiceName,  String targetVirtualEntity, String msg) {

        setBeginTimestamp(System.currentTimeMillis());

        if (svcInstanceId != null) {
            setServiceInstanceID(svcInstanceId);
        }

        if (svcName != null) {
            setServiceName(svcName);
        }

        if (partnerName != null) {
            setPartnerName(partnerName);
        }

        if (targetEntity != null) {
            setTargetEntity(targetEntity);
        }

        if (targetServiceName != null) {
            setTargetServiceName(targetServiceName);
        }

        if (targetVirtualEntity != null) {
            setTargetVirtualEntity(targetVirtualEntity);
        }

        this.lastMsg = msg;


    }

    public void logResponse(String statusCode, String responseCode, String responseDescription) {
        setEndTimestamp(System.currentTimeMillis());

        setStatusCode(statusCode);
        setResponseCode(responseCode);
        setResponseDescription(responseDescription);

        METRIC.info(formatString(lastMsg));

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
