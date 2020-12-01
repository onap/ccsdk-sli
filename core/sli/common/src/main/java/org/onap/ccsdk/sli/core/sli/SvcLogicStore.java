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

import java.util.Properties;

public interface SvcLogicStore {
	
	public void init(Properties props) throws SvcLogicException;
	public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException;
	public SvcLogicGraph fetch(String module, String rpc, String version, String mode) throws SvcLogicException;
	public void store(SvcLogicGraph graph) throws SvcLogicException;
	public void delete(String module, String rpc, String version, String mode) throws SvcLogicException;
	public void activate(SvcLogicGraph graph) throws SvcLogicException;
	public void activate(String module, String rpc, String version, String mode) throws SvcLogicException;

}
