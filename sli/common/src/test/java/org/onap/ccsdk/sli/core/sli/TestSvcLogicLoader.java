package org.onap.ccsdk.sli.core.sli;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.junit.Test;

public class TestSvcLogicLoader {

    @Test
    public void testLoadAndActivate() throws IOException, SvcLogicException {
        URL propUrl = ITCaseSvcLogicParser.class.getResource("/svclogic.properties");

        InputStream propStr = ITCaseSvcLogicParser.class.getResourceAsStream("/svclogic.properties");

        Properties props = new Properties();

        props.load(propStr);

        SvcLogicStore store = SvcLogicStoreFactory.getSvcLogicStore(props);

        URL graphUrl = TestSvcLogicLoader.class.getClassLoader().getResource("graphs");

        if (graphUrl == null) {
            fail("Cannot find graphs directory");
        }

        SvcLogicLoader loader = new SvcLogicLoader(graphUrl.getPath(), store);
        loader.loadAndActivate();


    }



}
