package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DBConfigExceptionTest {

    @Test
    public void testDBConfigExceptionException() {
        assertNotNull(new DBConfigException("JUnit Test"));
    }

    @Test
    public void testDBConfigExceptionString() {
        assertNotNull(new DBConfigException(new Exception("JUnit Test")));
    }

}
