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
        
        assertEquals(props.getProperty("org.onap.ccsdk.sli.dbtype"),"jdbc");
        String dbUrl = "jdbc:derby:memory:"+System.getenv("MYSQL_DATABASE")+";create=true";
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.url"), dbUrl);
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.driver"), "org.apache.derby.jdbc.EmbeddedDriver");
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.database"), System.getenv("MYSQL_DATABASE"));
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.user"), System.getenv("MYSQL_USER"));
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.password"), System.getenv("MYSQL_PASSWORD"));
    }

    @Test
    public void loadReaderTest() throws IOException {
        InputStream testStr = getClass().getResourceAsStream("/svclogic.properties");
        BufferedReader testReader = new BufferedReader(new InputStreamReader(testStr));

        Properties props = new EnvProperties();
        props.load(testReader);
        
        assertEquals(props.getProperty("org.onap.ccsdk.sli.dbtype"),"jdbc");
        String dbUrl = "jdbc:derby:memory:"+System.getenv("MYSQL_DATABASE")+";create=true";
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.url"), dbUrl);
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.driver"), "org.apache.derby.jdbc.EmbeddedDriver");
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.database"), System.getenv("MYSQL_DATABASE"));
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.user"), System.getenv("MYSQL_USER"));
        assertEquals(props.getProperty("org.onap.ccsdk.sli.jdbc.password"), System.getenv("MYSQL_PASSWORD"));
    }

    @Test
    public void setPropertyTest() {
        Properties props = new EnvProperties();

        props.setProperty("path", "${PATH}");
        assertEquals(props.getProperty("path"), System.getenv("PATH"));
    }
    
}
