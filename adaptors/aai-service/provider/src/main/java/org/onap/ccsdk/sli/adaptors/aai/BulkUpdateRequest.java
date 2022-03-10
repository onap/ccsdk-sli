/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights
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
/**
 * @author Dan Timoney
 *
 */
package org.onap.ccsdk.sli.adaptors.aai;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.ccsdk.sli.adaptors.aai.update.BulkUpdateRequestData;
import org.onap.ccsdk.sli.adaptors.aai.update.BulkUpdateRequestItemBody;
import org.onap.ccsdk.sli.adaptors.aai.update.BulkUpdateResponseData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class BulkUpdateRequest extends AAIRequest {

	private final String generic_search_path;

	public static final String FORMAT = "format";

	public BulkUpdateRequest() {
		generic_search_path = "/aai/v25/bulk/single-transaction";
		setRequestObject(new BulkUpdateRequestData());
	}


	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String requestUrl = getTargetUri()+generic_search_path;

		String formatQuery = requestProperties.getProperty(FORMAT);

		if(formatQuery != null) {
			requestUrl = requestUrl +"?format="+formatQuery;
		}
		URL httpReqUrl =	new URL(requestUrl);

		aaiService.LOGwriteFirstTrace(method, httpReqUrl.toString());

		return httpReqUrl;
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return getRequestUrl(method, null);
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		BulkUpdateRequestData bulkUpdateRequest = (BulkUpdateRequestData)requestDatum;
		String jsonText = null;
		try {
			jsonText = mapper.writeValueAsString(bulkUpdateRequest);
		} catch (JsonProcessingException exc) {
			handleException(this, exc);
			return null;
		}
		return jsonText;
	}


	@Override
	public String[] getArgsList() {
		String[] args = {FORMAT};
		return args;
	}


	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return BulkUpdateRequestData.class;
	}


	public static String processPathData(String requestUrl, Properties requestProperties) throws UnsupportedEncodingException {
		return requestUrl;
	}
	
	@Override
	public AAIDatum jsonStringToObject(String jsonData) throws IOException {
		if(jsonData == null) {
			return null;
		}

		AAIDatum response = null;
		ObjectMapper mapper = getObjectMapper();
		response = mapper.readValue(jsonData, BulkUpdateResponseData.class);
		return response;
	}

	protected boolean expectsDataFromPUTRequest() {
		return true;
	}

	public void addUpdate(String action, String uri, BulkUpdateRequestItemBody body) {
		((BulkUpdateRequestData) requestDatum).addRequestItem(action, uri, body);
	}

}
