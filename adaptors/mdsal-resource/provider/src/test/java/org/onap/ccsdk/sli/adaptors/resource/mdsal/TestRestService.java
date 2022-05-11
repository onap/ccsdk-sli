/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2022 Samsung Electronics
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.resource.mdsal;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;

public class TestRestService {
    @Test
    public void testGetRestConnection() throws Exception {
        RestService service = new RestService("HTTP", "1.1.1.1", "80",
                "user", "pass", "JSON", "JSON");
        Method method = RestService.class.getDeclaredMethod("getRestConnection", String.class, String.class);
        method.setAccessible(true);
        String url = "http" + "://" + "1.1.1.1" + ":" + "80" + "/" + "urlString";
        HttpURLConnection urlConn = (HttpURLConnection) method.invoke(service,
                url, "GET");
        String authStr = "user" + ":" + "pass";
        String encodedAuthStr = new String(Base64.encodeBase64(authStr.getBytes()));
        Assert.assertEquals("GET", urlConn.getRequestMethod());
        Assert.assertEquals(url, urlConn.getURL().toString());
        Assert.assertEquals("application/json", urlConn.getRequestProperty("Accept"));
        Assert.assertEquals("Basic " + encodedAuthStr, urlConn.getRequestProperty("Authentication"));
    }
}
