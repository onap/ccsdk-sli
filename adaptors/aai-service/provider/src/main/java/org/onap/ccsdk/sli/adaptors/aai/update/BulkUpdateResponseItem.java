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

package org.onap.ccsdk.sli.adaptors.aai.update;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "action",
	"uri",
	"response-status-code",
	"response-body"
})
@XmlRootElement(name = "response")
public class BulkUpdateResponseItem implements AAIDatum {

	@JsonProperty("action")
	String action;

	@JsonProperty("uri")
	String uri;

	@JsonProperty("response-status-code")
	String responseStatusCode;

	@JsonProperty("response-body")
	String responseBody;

	@JsonProperty("action")
	public String getAction() {
		return action;
	}

	@JsonProperty("action")
	public void setAction(String action) {
		this.action = action;
	}

	@JsonProperty("uri")
	public String getUri() {
		return uri;
	}

	@JsonProperty("uri")
	public void setUri(String uri) {
		this.uri = uri;
	}

	@JsonProperty("response-status-code")
	public String getResponseStatusCode() {
		return responseStatusCode;
	}

	@JsonProperty("response-status-code")
	public void setResponseStatusCode(String responseStatusCode) {
		this.responseStatusCode  = responseStatusCode;
	}

	@JsonProperty("response-body")
	public String getResponseBody() {
		return responseBody;
	}

	@JsonProperty("response-body")
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	
	
    @Override
    public String toString()
    {
        return " [action = "+action+",uri = "+uri+",response-status-code = "+responseStatusCode+",response-body = "+responseBody+"]";
    }

    public String getResourceVersion() {
        return null;
    }

}
