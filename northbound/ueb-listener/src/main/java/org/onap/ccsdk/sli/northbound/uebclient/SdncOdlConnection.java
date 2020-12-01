/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.northbound.uebclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SdncOdlConnection {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SdncOdlConnection.class);
	
	private HttpURLConnection httpConn = null;
	
	private String url = null;
	private String user = null;
	private String password = null;
	
	private class SdncAuthenticator extends Authenticator {
		
		private String user;
		private String passwd;

		SdncAuthenticator(String user, String passwd) {
			this.user = user;
			this.passwd = passwd;
		}
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, passwd.toCharArray());
		}
		
	}
	
	private SdncOdlConnection() {
		
	}
	
	private SdncOdlConnection(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
		
		try {
			URL sdncUrl = new URL(url);
			Authenticator.setDefault(new SdncAuthenticator(user, password));
		
			this.httpConn = (HttpURLConnection) sdncUrl.openConnection();
		} catch (Exception e) {
			LOG.error("Unable to create http connection", e);
		}
	}
	
	public static  SdncOdlConnection newInstance(String url, String user, String password) throws IOException
	{
		return new SdncOdlConnection(url, user, password);
	}
		

	
	public String send(String method, String contentType, String msg) throws IOException {

		LOG.info("Sending REST " + method + " to " + url);
		LOG.info("Message body:\n" + msg);
		String authStr = user + ":" + password;
		String encodedAuthStr = new String(Base64.encodeBase64(authStr.getBytes()));

		httpConn.addRequestProperty("Authentication", "Basic " + encodedAuthStr);

		httpConn.setRequestMethod(method);
		httpConn.setRequestProperty("Content-Type", contentType);
		httpConn.setRequestProperty("Accept", contentType);

		httpConn.setDoInput(true);
		httpConn.setDoOutput(true);
		httpConn.setUseCaches(false);

		if (httpConn instanceof HttpsURLConnection) {
			HostnameVerifier hostnameVerifier = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			((HttpsURLConnection) httpConn).setHostnameVerifier(hostnameVerifier);
		}

		// Write message
		httpConn.setRequestProperty("Content-Length", "" + msg.length());
		DataOutputStream outStr = new DataOutputStream(httpConn.getOutputStream());
		outStr.write(msg.getBytes());
		outStr.close();

		// Read response
		BufferedReader respRdr;

		LOG.info("Response: " + httpConn.getResponseCode() + " " + httpConn.getResponseMessage());

		if (httpConn.getResponseCode() < 300) {

			respRdr = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
		} else {
			respRdr = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
		}

		StringBuilder respBuff = new StringBuilder();

		String respLn;

		while ((respLn = respRdr.readLine()) != null) {
			respBuff.append(respLn + "\n");
		}
		respRdr.close();

		String respString = respBuff.toString();

		LOG.info("Response body :\n" + respString);

		return respString;

	}
		

}
