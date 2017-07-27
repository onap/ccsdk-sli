/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
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

package org.onap.ccsdk.sli.core.dblib;

import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import org.onap.ccsdk.sli.core.dblib.config.BaseDBConfiguration;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitorObserver;


public class TerminatingCachedDataSource extends CachedDataSource implements SQLExecutionMonitorObserver {

	public TerminatingCachedDataSource(BaseDBConfiguration jdbcElem) throws DBConfigException {
		super(jdbcElem);
	}

	protected void configure(BaseDBConfiguration jdbcElem) throws DBConfigException {
		// no action
	}

	public long getInterval() {
		return 1000;
	}

	public long getInitialDelay() {
		return 1000;
	}

	public long getExpectedCompletionTime() {
		return 50;
	}

	public void setExpectedCompletionTime(long value) {
		
	}

	public void setInterval(long value) {
		
	}

	public void setInitialDelay(long value) {
		
	}

	public long getUnprocessedFailoverThreshold() {
		return 3;
	}

	public void setUnprocessedFailoverThreshold(long value) {
		
	}
	
	public int compareTo(CachedDataSource ods)
	{
		return 0;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

}
