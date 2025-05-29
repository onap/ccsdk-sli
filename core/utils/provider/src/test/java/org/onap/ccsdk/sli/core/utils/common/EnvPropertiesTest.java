package org.onap.ccsdk.sli.core.utils.common;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class EnvPropertiesTest {

    @Test
    public void loadStreamTest() throws IOException {
        InputStream testStr = getClass().getResourceAsStream("/svclogic.properties");

        Properties props = new EnvProperties();
        props.load(testStr);
        
        assertEquals("jdbc", props.getProperty("org.onap.ccsdk.sli.dbtype"));
        String dbUrl = "jdbc:derby:memory:"+System.getenv("MYSQL_DATABASE")+";create=true";
        assertEquals(dbUrl, props.getProperty("org.onap.ccsdk.sli.jdbc.url"));
        assertEquals("org.apache.derby.iapi.jdbc.AutoloadedDriver", props.getProperty("org.onap.ccsdk.sli.jdbc.driver"));
        assertEquals(System.getenv("MYSQL_DATABASE"), props.getProperty("org.onap.ccsdk.sli.jdbc.database"));
        assertEquals(System.getenv("MYSQL_USER"), props.getProperty("org.onap.ccsdk.sli.jdbc.user") );
        assertEquals(System.getenv("MYSQL_PASSWORD"), props.getProperty("org.onap.ccsdk.sli.jdbc.password"));
    }

    @Test
    public void loadReaderTest() throws IOException {
        InputStream testStr = getClass().getResourceAsStream("/svclogic.properties");
        BufferedReader testReader = new BufferedReader(new InputStreamReader(testStr));

        Properties props = new EnvProperties();
        props.load(testReader);
        
        assertEquals("jdbc", props.getProperty("org.onap.ccsdk.sli.dbtype"));
        String dbUrl = "jdbc:derby:memory:"+System.getenv("MYSQL_DATABASE")+";create=true";
        assertEquals(dbUrl, props.getProperty("org.onap.ccsdk.sli.jdbc.url"));
        assertEquals("org.apache.derby.iapi.jdbc.AutoloadedDriver", props.getProperty("org.onap.ccsdk.sli.jdbc.driver"));
        assertEquals(System.getenv("MYSQL_DATABASE"), props.getProperty("org.onap.ccsdk.sli.jdbc.database"));
        assertEquals(System.getenv("MYSQL_USER"), props.getProperty("org.onap.ccsdk.sli.jdbc.user"));
        assertEquals(System.getenv("MYSQL_PASSWORD"), props.getProperty("org.onap.ccsdk.sli.jdbc.password"));
    }

    @Test
    public void setPropertyTest() {
        Properties props = new EnvProperties();

        props.setProperty("path", "${PATH}");
        props.setProperty("dummy", "${UNSET_DUMMY:-dummyvalue}");
        assertEquals(System.getenv("PATH"), props.getProperty("path"));
        assertEquals("dummyvalue", props.getProperty("dummy"));
    }
    
}
