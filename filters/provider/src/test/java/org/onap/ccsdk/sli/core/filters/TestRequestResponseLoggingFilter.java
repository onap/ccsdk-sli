/**
 *
 */
package org.onap.ccsdk.sli.core.filters;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.UUID;
import java.util.Vector;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dt5972
 *
 */
public class TestRequestResponseLoggingFilter {

    RequestResponseLoggingFilter filter;

    private class DummyServletInputStream extends ServletInputStream {

        InputStream stream;

        public DummyServletInputStream(InputStream stream) {
            this.stream = stream;
        }


        @Override
        public void close() throws IOException {
            super.close();
            stream.close();
        }


        @Override
        public int read() throws IOException {
            return stream.read();
        }



    }

    private class DummyServletOutputStream extends ServletOutputStream {

        OutputStream ostr;

        public DummyServletOutputStream(OutputStream ostr) {
            this.ostr = ostr;
        }

        @Override
        public void write(int b) throws IOException {
            ostr.write(b);
        }

    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        filter = new RequestResponseLoggingFilter();
        filter.init(null);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {}

    /**
     * Test method for {@link org.onap.ccsdk.sli.core.filters.RequestResponseLoggingFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
     * @throws IOException
     * @throws ServletException
     */
    @Test
    public void testDoFilter() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        String msgBody = "hello world";
        InputStream reqInputStream = new ByteArrayInputStream(msgBody.getBytes());
        when(request.getInputStream()).thenReturn(new DummyServletInputStream(reqInputStream));
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURL()).thenReturn(new StringBuffer("/HELLO:world"));
        when(request.getPathInfo()).thenReturn("/hello:world");
        Vector<String> headerList = new Vector<>();
        headerList.add(LogFilter.REQUEST_ID);
        headerList.add("Authorization");
        when(request.getHeaderNames()).thenReturn(headerList.elements());

        when(request.getHeader(LogFilter.REQUEST_ID)).thenReturn(UUID.randomUUID().toString());
        when(request.getHeader("Authorization")).thenReturn("Basic "+Base64.encodeBase64String("username:password".getBytes()));

        HttpServletResponse response = mock(HttpServletResponse.class);
        OutputStream outStr = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outStr));

        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);



    }

}
