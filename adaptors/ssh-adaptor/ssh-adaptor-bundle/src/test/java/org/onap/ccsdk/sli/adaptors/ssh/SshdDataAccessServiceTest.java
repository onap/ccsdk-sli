/*
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2022 Samsung Electronics
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

package org.onap.ccsdk.sli.adaptors.ssh;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.adaptors.ssh.sshd.SshdDataAccessService;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SshdDataAccessServiceTest {

    private SshdDataAccessService sshdDataAccessService;
    private DbLibService db;
    CachedRowSet rowset;
    @Before
    public void setUp() throws SQLException {
        sshdDataAccessService = new SshdDataAccessService();
        DbLibServiceMock dbLibServiceMock = new DbLibServiceMock() {
            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }
        };
        sshdDataAccessService.setDbLibService(db);
    }

    @Test
    public void testSetSchema() {
        sshdDataAccessService.setSchema("test");
        assertEquals("test", sshdDataAccessService.getSchema());
    }

    @Test(expected = NullPointerException.class)
    public void testSetDbLibService() {
        sshdDataAccessService.setDbLibService(db);
        assertEquals(false, sshdDataAccessService.getDbLibService().isActive());
    }

    @Test(expected = NullPointerException.class)
    public void testRetrieveConnectionDetails() {
        SshConnectionDetails connectionDetails = new SshConnectionDetails();
        sshdDataAccessService.setDbLibService(db);
        assertTrue(sshdDataAccessService.retrieveConnectionDetails("test", connectionDetails));
    }

    @Test(expected = NullPointerException.class)
    public void testRetrieveConfigFileName() {
        assertEquals(null, sshdDataAccessService.retrieveConfigFileName("test"));
    }
}
