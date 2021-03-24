/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.sli.adaptors.chef.chefclient;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefApiClient;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.ChefApiClientImpl;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.impl.ChefApiHeaderFactory;

public class ChefApiClientFactory {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ChefApiClientFactory.class);

    private HttpClient httpClient = createChefHttpClient();
    private ChefApiHeaderFactory chefApiHeaderFactory = new ChefApiHeaderFactory();

    private HttpClient createChefHttpClient() {
        String trustStoreFileName = "/opt/onap/chef/chefServerSSL.jks";
        char[] trustStoreCreds = "adminadmin".toCharArray();
        try {
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    SSLContexts.custom().loadTrustMaterial(new File(trustStoreFileName), trustStoreCreds).build(),
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | CertificateException
                | IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public ChefApiClient create(String endPoint, String organizations, String userId, String pemPath) {
        return new ChefApiClientImpl(httpClient, endPoint, organizations, (methodName, requestPath, body) -> chefApiHeaderFactory
                .create(methodName, requestPath, body, userId, organizations, pemPath));
    }

    public ChefApiClient create(String endPoint, String organizations) {
        return new ChefApiClientImpl(httpClient, endPoint, organizations, (methodName, requestPath, body) -> ImmutableMap.of());
    }

}
