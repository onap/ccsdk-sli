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
import javax.sql.DataSource;
import org.onap.ccsdk.sli.core.dblib.config.BaseDBConfiguration;
import org.onap.ccsdk.sli.core.dblib.pm.SQLExecutionMonitorObserver;


public class TerminatingCachedDataSource extends CachedDataSource implements SQLExecutionMonitorObserver {

    private static final int DEFAULT_AVAILABLE_CONNECTIONS = 0;
	private static final int DEFAULT_INDEX = -1;

    public TerminatingCachedDataSource(BaseDBConfiguration jdbcElem) throws DBConfigException {
        super(jdbcElem);
    }

    @Override
    protected DataSource configure(BaseDBConfiguration jdbcElem) throws DBConfigException {
        return null;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    protected int getAvailableConnections() {
        return DEFAULT_AVAILABLE_CONNECTIONS;
    }

    @Override
    protected int initializeIndex(BaseDBConfiguration jdbcElem) {
           return DEFAULT_INDEX;
    }

}
