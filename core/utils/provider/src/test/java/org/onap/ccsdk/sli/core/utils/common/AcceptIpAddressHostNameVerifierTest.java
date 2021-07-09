package org.onap.ccsdk.sli.core.utils.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.junit.Test;
import org.mockito.Mockito;

public class AcceptIpAddressHostNameVerifierTest {
    
    @Test
    public void testVerify() {
        HostnameVerifier hv = new AcceptIpAddressHostNameVerifier();
        SSLSession sslSession = Mockito.mock(SSLSession.class);
        
        // Test that IPv4 style address is accepted
        assertTrue(hv.verify("127.0.0.1", sslSession));

        // Test that IPv6 style addresses are also accepted
        assertTrue(hv.verify("2001:db8:3333:4444:5555:6666:7777:8888", sslSession));
        assertTrue(hv.verify("2001:db8:3333:4444:CCCC:DDDD:EEEE:FFFF", sslSession));
        assertTrue(hv.verify("2001:db8::", sslSession));
        assertTrue(hv.verify("::1234:5678", sslSession));
        assertTrue(hv.verify("2001:db8::1234:5678", sslSession));
        assertTrue(hv.verify("::1", sslSession));

        // Test that FQDN is not accepted (since there is no certificate)
        assertFalse(hv.verify("bogus.org", sslSession));
        
        
    }
}
