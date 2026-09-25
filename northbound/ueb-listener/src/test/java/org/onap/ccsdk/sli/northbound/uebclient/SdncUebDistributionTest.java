/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.ccsdk.sli.northbound.uebclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onap.sdc.api.results.DistributionActionResultEnum.SUCCESS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.impl.DistributionClientFactory;

public class SdncUebDistributionTest {

    private static final String NOTIFICATION_TOPIC = "SDC-DISTR-NOTIF-TOPIC-UNITTEST";
    private static final String STATUS_TOPIC = "SDC-DISTR-STATUS-TOPIC-UNITTEST";
    private static final String CONSUMER_GROUP = "ueb-listener-test";
    private static final String CONSUMER_ID = "ueb-listener-test-consumer";
    private static final String DISTRIBUTION_ID = "5b1d9e2c-4f6a-4a3b-9c1d-2e7f8a9b0c1d";
    private static final String SERVICE_NAME = "DistributionTestService";
    private static final String ARTIFACT_NAME = "vf-license-model.xml";
    private static final String ARTIFACT_URL =
            "/sdc/v1/catalog/services/DistributionTestService/1.0/artifacts/" + ARTIFACT_NAME;
    private static final String ARTIFACT_PAYLOAD = "<vf-license-model><vf-id>test-vf</vf-id></vf-license-model>\n";
    private static final String DEPLOY_PATH = "/restconf/operations/ASDC-API:vf-license-model-update";
    private static final Duration TIMEOUT = Duration.ofSeconds(90);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ClassRule
    public static final SharedKafkaTestResource KAFKA = new SharedKafkaTestResource()
            .withBrokerProperty("group.initial.rebalance.delay.ms", "0");

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private final Queue<String> sdcRequests = new ConcurrentLinkedQueue<>();
    private final Queue<String> deployRequests = new ConcurrentLinkedQueue<>();
    private HttpServer sdc;

    @Before
    public void setUp() throws IOException {
        KAFKA.getKafkaTestUtils().createTopic(NOTIFICATION_TOPIC, 1, (short) 1);
        KAFKA.getKafkaTestUtils().createTopic(STATUS_TOPIC, 1, (short) 1);

        sdc = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        sdc.createContext("/", this::handleSdcRequest);
        sdc.start();
    }

    @After
    public void tearDown() {
        sdc.stop(0);
    }

    @Test
    public void notificationIsDownloadedDeployedAndReportedToSdc() throws Exception {
        SdncUebConfiguration config = plaintextKafkaConfiguration(writeListenerProperties());
        IDistributionClient client = DistributionClientFactory.createDistributionClient();
        SdncUebCallback callback = new SdncUebCallback(client, config);

        assertEquals(SUCCESS, client.init(config, callback).getDistributionActionResult());
        startConsumerGroupAtBeginningOfNotificationTopic();
        publishNotification();
        assertEquals(SUCCESS, client.start().getDistributionActionResult());

        List<String> statuses = awaitStatusesUntilComponentDone();

        assertEquals(Arrays.asList("NOTIFIED", "DOWNLOAD_OK", "DEPLOY_OK", "COMPONENT_DONE_OK"), statuses);
        assertTrue(sdcRequests.contains("GET /sdc/v1/artifactTypes"));
        assertTrue(sdcRequests.contains("GET /sdc/v1/distributionKafkaData"));
        assertTrue(sdcRequests.contains("GET " + ARTIFACT_URL));
        assertEquals(1, deployRequests.size());
        assertTrue(deployRequests.peek().contains("<vf-id>test-vf</vf-id>"));
        assertEquals(SUCCESS, client.stop().getDistributionActionResult());
        assertTrue(awaitConsumerGroupEmpty());
    }

    // IConfiguration defaults to SASL_PLAINTEXT and otherwise reads SASL_JAAS_CONFIG from the environment.
    private static SdncUebConfiguration plaintextKafkaConfiguration(String propDir) {
        return new SdncUebConfiguration(propDir) {
            @Override
            public String getKafkaSecurityProtocolConfig() {
                return "PLAINTEXT";
            }

            @Override
            public String getKafkaSaslJaasConfig() {
                return "";
            }
        };
    }

    private String writeListenerProperties() throws IOException {
        String baseUrl = "localhost:" + sdc.getAddress().getPort();
        Properties props = new Properties();
        String prefix = "org.onap.ccsdk.sli.northbound.uebclient.";
        props.setProperty(prefix + "sdc-address", baseUrl);
        props.setProperty(prefix + "use-https", "false");
        props.setProperty(prefix + "consumer-group", CONSUMER_GROUP);
        props.setProperty(prefix + "consumer-id", CONSUMER_ID);
        props.setProperty(prefix + "environment-name", "UNITTEST");
        props.setProperty(prefix + "user", "sdnc");
        props.setProperty(prefix + "password", "sdnc-password");
        props.setProperty(prefix + "sdnc-user", "admin");
        props.setProperty(prefix + "sdnc-passwd", "admin-password");
        props.setProperty(prefix + "asdc-api-base-url", "http://" + baseUrl + "/restconf/operations/");
        props.setProperty(prefix + "asdc-api-namespace", "org:onap:ccsdk");
        props.setProperty(prefix + "spool.incoming", tmp.newFolder("incoming").getAbsolutePath());
        props.setProperty(prefix + "spool.archive", tmp.newFolder("archive").getAbsolutePath());
        props.setProperty(prefix + "polling-interval", "15");
        props.setProperty(prefix + "polling-timeout", "15");
        props.setProperty(prefix + "relevant-artifact-types", "VF_LICENSE");
        props.setProperty(prefix + "activate-server-tls-auth", "false");
        props.setProperty(prefix + "artifact-map", new File("src/test/resources/artifact.map").getAbsolutePath());
        props.setProperty(prefix + "xslt-path-list", new File("src/main/resources/normalizeTagNames.xslt").getAbsolutePath()
                + "," + new File("src/main/resources/removeNs.xslt").getAbsolutePath());

        File propDir = tmp.newFolder("properties");
        try (OutputStream out = new FileOutputStream(new File(propDir, "ueb-listener.properties"))) {
            props.store(out, null);
        }
        return propDir.getAbsolutePath();
    }

    // The client consumes with auto.offset.reset=latest. Committing offset 0 for its group up front lets the
    // notification be published before start() without racing the partition assignment.
    private void startConsumerGroupAtBeginningOfNotificationTopic() throws Exception {
        try (Admin admin = Admin.create(kafkaClientProperties())) {
            admin.alterConsumerGroupOffsets(CONSUMER_GROUP,
                    Collections.singletonMap(new TopicPartition(NOTIFICATION_TOPIC, 0), new OffsetAndMetadata(0)))
                    .all().get();
        }
    }

    private void publishNotification() throws Exception {
        Properties props = kafkaClientProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(NOTIFICATION_TOPIC, notification())).get();
        }
    }

    private List<String> awaitStatusesUntilComponentDone() throws IOException {
        Properties props = kafkaClientProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        List<String> statuses = new ArrayList<>();
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            TopicPartition statusPartition = new TopicPartition(STATUS_TOPIC, 0);
            consumer.assign(Collections.singletonList(statusPartition));
            consumer.seekToBeginning(Collections.singletonList(statusPartition));
            while (!isComponentDone(statuses) && System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    JsonNode status = MAPPER.readTree(record.value());
                    if (DISTRIBUTION_ID.equals(status.path("distributionID").asText())) {
                        statuses.add(status.path("status").asText());
                    }
                }
            }
        }
        return statuses;
    }

    // Shorter than the consumer session timeout, so only a consumer that leaves the group on stop() passes.
    private static boolean awaitConsumerGroupEmpty() throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        try (Admin admin = Admin.create(kafkaClientProperties())) {
            while (System.nanoTime() < deadline) {
                ConsumerGroupDescription group = admin.describeConsumerGroups(Collections.singletonList(CONSUMER_GROUP))
                        .describedGroups().get(CONSUMER_GROUP).get();
                if (group.members().isEmpty()) {
                    return true;
                }
                Thread.sleep(100);
            }
        }
        return false;
    }

    private static boolean isComponentDone(List<String> statuses) {
        return !statuses.isEmpty() && statuses.get(statuses.size() - 1).startsWith("COMPONENT_DONE");
    }

    private static Properties kafkaClientProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getKafkaConnectString());
        return props;
    }

    private static String notification() {
        ObjectNode artifact = MAPPER.createObjectNode()
                .put("artifactName", ARTIFACT_NAME)
                .put("artifactType", "VF_LICENSE")
                .put("artifactURL", ARTIFACT_URL)
                .put("artifactChecksum", "ZmJlMzZkN2NkZWRjNDUyNDUwZTY2YjE0NmNkMGFiOWI=")
                .put("artifactDescription", "VF license file")
                .put("artifactTimeout", 0)
                .put("artifactUUID", "f8c6e818-aa35-43d2-bc23-fb2c498fb675")
                .put("artifactVersion", "1");
        ObjectNode notification = MAPPER.createObjectNode()
                .put("distributionID", DISTRIBUTION_ID)
                .put("serviceName", SERVICE_NAME)
                .put("serviceVersion", "1.0")
                .put("serviceUUID", "0f3c3b7a-8a1e-4d2b-9f4c-6a5b4c3d2e1f")
                .put("serviceDescription", "distribution test service")
                .put("serviceInvariantUUID", "7e6d5c4b-3a29-4817-a6f5-e4d3c2b1a090")
                .put("workloadContext", "Production");
        notification.putArray("serviceArtifacts").add(artifact);
        notification.putArray("resources");
        return notification.toString();
    }

    private void handleSdcRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        sdcRequests.add(exchange.getRequestMethod() + " " + path);
        String contentType = "application/json";
        String body;
        if ("/sdc/v1/artifactTypes".equals(path)) {
            body = "[\"VF_LICENSE\",\"TOSCA_CSAR\",\"TOSCA_TEMPLATE\"]";
        } else if ("/sdc/v1/distributionKafkaData".equals(path)) {
            body = MAPPER.createObjectNode()
                    .put("kafkaBootStrapServer", KAFKA.getKafkaConnectString())
                    .put("distrNotificationTopicName", NOTIFICATION_TOPIC)
                    .put("distrStatusTopicName", STATUS_TOPIC)
                    .toString();
        } else if (ARTIFACT_URL.equals(path)) {
            contentType = "application/octet-stream";
            body = ARTIFACT_PAYLOAD;
        } else if (DEPLOY_PATH.equals(path)) {
            deployRequests.add(readBody(exchange.getRequestBody()));
            contentType = "application/xml";
            body = "<output xmlns=\"org:onap:ccsdk\"><asdc-api-response-code>200</asdc-api-response-code></output>";
        } else {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String readBody(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
}
