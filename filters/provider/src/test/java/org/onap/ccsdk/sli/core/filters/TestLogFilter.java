/**
 *
 */
package org.onap.ccsdk.sli.core.filters;

import static org.junit.Assert.*;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dt5972
 *
 */
public class TestLogFilter {

    LogFilter logFilter;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        logFilter = new LogFilter();
        logFilter.init(null);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        logFilter.destroy();
    }

    /**
     * Test method for {@link org.onap.ccsdk.sli.core.filters.LogFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
     * @throws ServletException
     * @throws IOException
     */
    @Test
    public void testDoFilter() throws IOException, ServletException {

        // Test failed request with minimal headers
        HttpServletRequest servletReq = mock(HttpServletRequest.class);
        when(servletReq.getRequestURL()).thenReturn(new StringBuffer("SLI-API:healthcheck"));
        when(servletReq.getPathInfo()).thenReturn("/hello:world");
        HttpServletResponse servletResp = mock(HttpServletResponse.class);
        when(servletResp.getStatus()).thenReturn(400);
        FilterChain filterChain = mock(FilterChain.class);
        logFilter.doFilter(servletReq, servletResp, filterChain);

        // Test successful request with valid header
        when(servletReq.getHeader(LogFilter.REQUEST_ID)).thenReturn(UUID.randomUUID().toString());
        when(servletReq.getHeader("Authorization")).thenReturn("Basic "+Base64.encodeBase64String("username:password".getBytes()));
        when(servletResp.getStatus()).thenReturn(200);
        logFilter.doFilter(servletReq, servletResp, filterChain);


    }

}
