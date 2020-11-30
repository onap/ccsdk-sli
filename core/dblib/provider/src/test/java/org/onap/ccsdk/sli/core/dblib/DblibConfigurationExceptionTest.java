package org.onap.ccsdk.sli.core.dblib;

import static org.junit.Assert.*;

import org.junit.Test;

public class DblibConfigurationExceptionTest {

    @Test
    public void testDblibConfigurationException() {
        assertNotNull(new DblibConfigurationException());
    }

    @Test
    public void testDblibConfigurationExceptionString() {
        assertNotNull(new DblibConfigurationException("JUnit Test"));
    }

    @Test
    public void testDblibConfigurationExceptionStringThrowable() {
        assertNotNull(new DblibConfigurationException("JUnit Test", new Exception("JUnit Test")));
    }

}
