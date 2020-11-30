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

package org.onap.ccsdk.sli.core.sli;

import java.util.Map;

public interface SvcLogicResource {
	
	public enum QueryStatus {
		SUCCESS,
		NOT_FOUND,
		FAILURE
	}
	
	public QueryStatus isAvailable(String resource, String key, String prefix, SvcLogicContext ctx) throws SvcLogicException;
	
	public QueryStatus exists(String resource, String key, String prefix, SvcLogicContext ctx)  throws SvcLogicException;
	
	public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix, String orderBy, SvcLogicContext ctx)  throws SvcLogicException;
	
	public QueryStatus reserve(String resource, String select, String key, String prefix, SvcLogicContext ctx) throws SvcLogicException;
	
	public QueryStatus save(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms, String prefix, SvcLogicContext ctx) throws SvcLogicException;
	
	public QueryStatus release(String resource, String key, SvcLogicContext ctx)  throws SvcLogicException;
	
	public QueryStatus delete(String resource, String key, SvcLogicContext ctx) throws SvcLogicException;
	
	public QueryStatus notify(String resource, String action, String key, SvcLogicContext ctx) throws SvcLogicException;
	
	public QueryStatus update(String resource, String key, Map<String, String> parms, String prefix, SvcLogicContext ctx) throws SvcLogicException;

}
