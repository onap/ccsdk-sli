/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2022 Samsung Electronics
 * =============================================================================
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

package org.onap.ccsdk.sli.adaptors.ssh;

import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class DbLibServiceMock implements DbLibService {

    @Override
    public CachedRowSet getData(String statement,ArrayList<String> arguments, String preferredDS) throws SQLException{
        CachedRowSet rowset = Mockito.mock(CachedRowSet.class);
        return rowset;
    }

    @Override
    public boolean writeData(String statement, ArrayList<String> arguments, String preferredDS)
            throws SQLException {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException{
        return null;
    }

    @Override
    public java.io.PrintWriter getLogWriter() throws SQLException{
        return null;
    }

    @Override
    public void setLogWriter(java.io.PrintWriter out) throws SQLException{}

    @Override
    public void setLoginTimeout(int seconds) throws SQLException{}

    @Override
    public int getLoginTimeout() throws SQLException{
        return 1;
    }


}
