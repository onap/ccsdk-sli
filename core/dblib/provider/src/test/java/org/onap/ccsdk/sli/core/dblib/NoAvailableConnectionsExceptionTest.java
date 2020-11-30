package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NoAvailableConnectionsExceptionTest {

    @Test
    public void testNoAvailableConnectionsException() {
        assertNotNull(new NoAvailableConnectionsException(new Exception("test")));
    }

}
