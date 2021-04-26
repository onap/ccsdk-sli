/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Samsung
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.netconf.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfAdaptorConstants;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfConnectionDetails;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

public class TestNetconfDataAccessServiceImpl {
    NetconfDataAccessServiceImpl netconfDataAccessService;
    private String schema;
    private DbLibService dbLibServiceMocked;

    @Before
    public void SetUp() {
        schema = "test-netconf-adaptor";
        dbLibServiceMocked = new DbLibService() {
            @Override
            public CachedRowSet getData(String s, ArrayList<String> arrayList, String s1) throws SQLException {
                CachedRowSet cachedRowSetMocked = Mockito.mock(CachedRowSet.class);
                Mockito.when(cachedRowSetMocked.first()).thenReturn(true);
                Mockito.when(cachedRowSetMocked.getString(NetconfAdaptorConstants.FILE_CONTENT_TABLE_FIELD_NAME)).thenReturn("File_Content");
                return cachedRowSetMocked;
            }

            @Override
            public boolean writeData(String s, ArrayList<String> arrayList, String s1) throws SQLException {
                return false;
            }

            @Override
            public boolean isActive() {
                return false;
            }

            @Override
            public Connection getConnection() throws SQLException {
                return null;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return null;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {

            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {

            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }
        };

        netconfDataAccessService = new NetconfDataAccessServiceImpl();
        netconfDataAccessService.setSchema(schema);
        netconfDataAccessService.setDbLibService(dbLibServiceMocked);
    }

    @Test
    public void testRetrieveConfigFileName() throws IOException {
        String response = netconfDataAccessService.retrieveConfigFileName("test");
        Assert.assertEquals("File_Content", response);
    }

    @Test
    public void testRetrieveNetconfConnectionDetails() throws IOException {
        NetconfConnectionDetails netconfConnectionDetails = new NetconfConnectionDetails();
        boolean response = netconfDataAccessService.retrieveNetconfConnectionDetails("test", netconfConnectionDetails);
        Assert.assertEquals(true, response);
    }

    @Test
    public void testLogDeviceInteraction() throws IOException {
        boolean response = netconfDataAccessService.logDeviceInteraction("test", "",
                                                                         "", "");
        Assert.assertEquals(true, response);
    }
}
