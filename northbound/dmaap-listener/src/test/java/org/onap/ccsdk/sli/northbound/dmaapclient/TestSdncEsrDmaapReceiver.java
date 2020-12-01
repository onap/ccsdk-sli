/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
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

package org.onap.ccsdk.sli.northbound.dmaapclient;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

public class TestSdncEsrDmaapReceiver {
    static String aaiInput = "{\"cambria.partition\":\"AAI\",\n" +
            "    \"event-header\":\n" +
            "    {\n" +
            "      \"severity\":\"NORMAL\",\n" +
            "      \"entity-type\":\"esr-thirdparty-sdnc\",\n" +
            "      \"top-entity-type\":\"esr-thirdparty-sdnc\",\n" +
            "      \"entity-link\":\"aai/v11/external-system/esr-thirdparty-sdnc-list/esr-thirdparty-sdnc/IP-WAN-Controller-1\",\n" +
            "      \"event-type\":\"AAI-EVENT\",\n" +
            "      \"domain\":\"dev\",\n" +
            "      \"action\":\"UPDATE\",\n" +
            "      \"sequence-number\":\"0\",\n" +
            "      \"id\":\"bf4df797-759a-4684-a63c-393b7d40ed55\",\n" +
            "      \"source-name\":\"postman\",\n" +
            "      \"version\":\"v11\",\n" +
            "      \"timestamp\":\"20180104-09:57:58:721\"\n" +
            "    },\n" +
            "    \"entity\":\n" +
            "    {\n" +
            "      \"thirdparty-sdnc-id\":\"IP-WAN-Controller-1\",\n" +
            "      \"relationship-list\":\n" +
            "        {\n" +
            "          \"relationship\":\n" +
            "          [\n" +
            "            {\n" +
            "              \"related-to\":\"pnf\",\n" +
            "      \"relationship-data\":\n" +
            "              [\n" +
            "              {\n" +
            "                \"relationship-value\":\"a8098c1a-f86e-11da-bd1a-00112444be1e\",\n" +
            "                \"relationship-key\":\"pnf.pnf-name\"\n" +
            "              }\n" +
            "              ],\n" +
            "              \"related-link\":\"aai/v11/network/pnfs/pnf/a8098c1a-f86e-11da-bd1a-00112444be1e\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "      \"resource-version\":\"1515059878654\",\n" +
            "      \"location\":\"Core\",\n" +
            "      \"product-name\":\"AC-WAN\",\n" +
            "      \"esr-system-info-list\":\n" +
            "      {\"esr-system-info\":\n" +
            "      [\n" +
            "        {\n" +
            "          \"esr-system-info-id\":\"IP-WAN-Controller-ESR-1\",\n" +
            "          \"system-type\":\"example-system-type-val-12078\",\n" +
            "          \"service-url\":\"https://182.2.61.24:18002\",\n" +
            "          \"ssl-cacert\":\"example-ssl-cacert-val-20589\",\n" +
            "          \"type\":\"WAN\",\n" +
            "          \"ssl-insecure\":true,\n" +
            "          \"system-status\":\"example-system-status-val-23435\",\n" +
            "          \"version\":\"V3R1\",\n" +
            "          \"passive\":true,\n" +
            "          \"password\":\"Admin@12345\",\n" +
            "          \"protocol\":\"RESTCONF\",\n" +
            "          \"ip-address\":\"182.2.61.24\",\n" +
            "          \"cloud-domain\":\"example-cloud-domain-val-76077\",\n" +
            "          \"user-name\":\"admin\",\n" +
            "          \"system-name\":\"IP-WAN-Controller\",\n" +
            "          \"port\":\"18002\",\n" +
            "          \"vendor\":\"IP-WAN\",\n" +
            "          \"resource-version\":\"1515059878666\",\n" +
            "          \"remote-path\":\"example-remotepath-val-5833\",\n" +
            "          \"default-tenant\":\"example-default-tenant-val-71148\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            " }";

    @Test
    public void testProcessMsgInvalidEventType() throws Exception {
        String DMAAPLISTENERROOT = "DMAAPLISTENERROOT";
        File directory = new File("lib");

        if (! directory.exists()){
            directory.mkdir();
        }

        File source = new File("src/main/resources");
        File dest = new File("lib/");
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(DMAAPLISTENERROOT, ".");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
        Properties props = new Properties();
        InputStream propStr = TestSdncEsrDmaapReceiver.class.getResourceAsStream("/dmaap-consumer-esrsysteminfo.properties");

        props.load(propStr);

        SdncAaiDmaapConsumer consumer = new SdncAaiDmaapConsumer();

        consumer.init(props, "src/test/resources/dmaap-consumer-esrsysteminfo.properties");
        consumer.processMsg(aaiInput);
    }
}
