package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.*;

import org.junit.Test;

public class DBLibExceptionTest {

    @Test
    public void testDBLibException() {
        assertNotNull(new DBLibException("test"));
    }

}
