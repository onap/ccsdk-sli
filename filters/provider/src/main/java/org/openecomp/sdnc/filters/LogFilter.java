/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
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

package org.openecomp.sdnc.filters;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;



/**
 * Logs IN request according ECOMP Logging Guidelines at https://tspace.web.att.com/viewer/app/lcfiles/ae5f7751-39da-4c6b-8a83-5836c8c815e1/content
 */
public class LogFilter implements Filter {

    //X-ECOMP is shared between audit and metric
    public static final String BEGIN_TIMESTAMP = "AUDIT-BeginTimestamp";
    public static final String END_TIMESTAMP = "AUDIT-EndTimestamp";
    public static final String REQUEST_ID = "X-ECOMP-RequestID";
    public static final String SERVICE_INSTANCE = "X-ECOMP-ServiceInstanceID";
    public static final String THREAD_ID ="X-ECOMP-ThreadId"; //optional
    public static final String PHYSICAL_SERVER_NAME="X-ECOMP-PhysicalServerName"; //optional
    public static final String SERVICE_NAME="X-ECOMP-ServiceName";
    public static final String PARTNER_NAME="X-ECOMP-PartnerName";
    public static final String STATUS_CODE="AUDIT-StatusCode";
    public static final String RESP_CODE="AUDIT-ResponseCode";
    public static final String RESP_DESC="AUDIT-ResponseDescription";
    public static final String INSTANCE_UUID="AUDIT-InstanceUUID";
    public static final String CATEGORY="AUDIT-INFO";
    public static final String SEVERITY ="AUDIT-Severity"; //optional
    public static final String SERVER_IP="AUDIT-ServerIP"; //by chef node['ip']
    public static final String ELAPSED_TIME="AUDIT-ElapsedTime";
    public static final String SERVER_HOST="AUDIT-Server";//by chef node['fqdn']
    public static final String CLIENT_IP="AUDIT-ClientIPaddress";
    public static final String CLASS="AUDIT-Classname"; //optional
    public static final String UNUSED="AUDIT-Unused"; //empty
    public static final String PROCESS_KEY="AUDIT-ProcessKey"; //optional
    public static final String CUST_1="AUDIT-CustomField1";//optional
    public static final String CUST_2="AUDIT-CustomField2"; //optional
    public static final String CUST_3="AUDIT-CustomField3"; //optional
    public static final String CUST_4="AUDIT-CustomField4"; //optional
    public static final String DETAIL_MESSAGE="AUDIT-DetailMessage";//optional


    private static final Logger log = LoggerFactory.getLogger(LogFilter.class);
    private static final Logger AUDIT = LoggerFactory.getLogger("org.openecomp.sdnc.filters.audit");
    @Override
    public void destroy() {
        }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                final FilterChain filterChain) throws IOException, ServletException {

            long startTime = System.currentTimeMillis();
            try {

                if ( request != null && request instanceof HttpServletRequest ) {
                    pre((HttpServletRequest)request);
                }
                filterChain.doFilter(request, response);


            } finally {

                if (request != null && request instanceof HttpServletRequest ) {
                    post((HttpServletRequest)request,(HttpServletResponse)response,startTime);
                }
                MDC.clear();
            }

        }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        }



    private void pre(HttpServletRequest request) {

        UUID uuid = UUID.randomUUID();
        // check if uuid is in header X-ECOMP-RequestID

        String ecompUUID = request.getHeader(REQUEST_ID);

        if (ecompUUID != null && ecompUUID.length() > 0) {
            try {
                uuid = UUID.fromString(ecompUUID);
                log.info("UUID is ECOMP UUID " + uuid.toString());
            } catch (Exception ex){
                log.warn("Failed to convert ECOMP UUID to java.util.UUID format:" + ecompUUID,ex);
            }
        }
        MDC.put(REQUEST_ID, uuid.toString());

        String userName="unknown";

        /* below returning org.opendaylight.aaa.shiro.realm.TokenAuthRealm$ODLPrincipal@745dfcfe
           if ( request.getUserPrincipal() != null) {
           userName = request.getUserPrincipal().getName();
           }
         */
        // going directly after Authorization header
        if (request.getHeader("Authorization") != null) {
            String authzHeader = request.getHeader("Authorization");
            String usernameAndPassword = new String(Base64.decodeBase64(authzHeader.substring(6).getBytes()));

            int userNameIndex = usernameAndPassword.indexOf(":");
            String username = usernameAndPassword.substring(0, userNameIndex);
            userName = username;

        }


        MDC.put(PARTNER_NAME, userName);
        //just to initilaze for metric logger (outbound calls)
        MDC.put("X-ECOMP-TargetEntity","");
        MDC.put("X-ECOMP-TargetServiceName","");

        MDC.put(SERVICE_NAME,request.getRequestURL().toString());
        MDC.put(SERVICE_INSTANCE,"");

    }


    private void post(HttpServletRequest request,HttpServletResponse response,long startTime) {

        //AUDIT.info("{}|{}|{}{}",request.getRemoteHost(),request.getMethod(),request.getRequestURL().toString(),request.getQueryString());
        //AUDIT.info(request.getRemoteHost() + D + request.getMethod() + D + request.getRequestURL().toString() + D + request.getQueryString());
        //METRIC.info(request.getMethod() + D + response.getStatus() + D + request.getRequestURL().toString() + D + (System.currentTimeMillis() - startTime) + " ms");
        MDC.put(BEGIN_TIMESTAMP,asIso8601(startTime));
        MDC.put(END_TIMESTAMP,asIso8601(System.currentTimeMillis()));
        //MDC.put(REQUEST_ID,"already done above");
        MDC.put(SERVICE_NAME,request.getRequestURL().toString());
        int idx = request.getPathInfo().toString().lastIndexOf(":");
        String instance = "";
        if ( idx != -1 ) {
			instance = request.getPathInfo().substring(idx+1);
		}
        MDC.put(SERVICE_INSTANCE,instance);
        MDC.put(THREAD_ID,"");
        MDC.put(PHYSICAL_SERVER_NAME,"");
        //MDC.put(PARTNER_NAME,"already done above");
        if ( response.getStatus() >= 400 ) {
			MDC.put(STATUS_CODE,"ERROR");
		} else {
			MDC.put(STATUS_CODE,"COMPLETE");
		}

        MDC.put(RESP_CODE,"" + response.getStatus());
        MDC.put(RESP_DESC,"");
        MDC.put(INSTANCE_UUID,"");
        MDC.put(CATEGORY,"");
        MDC.put(SEVERITY,"");
        //MDC.put(SERVER_IP,""); //by chef
        MDC.put(ELAPSED_TIME,"" + (System.currentTimeMillis() - startTime));
        //MDC.put(SERVER_HOST,""); //by chef
        MDC.put(CLIENT_IP,request.getRemoteHost());
        MDC.put(CLASS,"");
        MDC.put(UNUSED,"");
        MDC.put(PROCESS_KEY,"");
        MDC.put(CUST_1,"");
        MDC.put(CUST_2,"");
        MDC.put(CUST_3,"");
        MDC.put(CUST_4,"");
        MDC.put(DETAIL_MESSAGE,request.getMethod());

        AUDIT.info("");
    }

    private String asIso8601(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'hh:mm:ss:SS'+00:00'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    private String asIso8601(long tsInMillis) {
        return asIso8601(new Date(tsInMillis));
    }


}
