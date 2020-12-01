package org.onap.ccsdk.sli.northbound.dmaapclient;

import static org.junit.Assert.assertEquals;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.junit.Test;

public class MessageRouterHttpClientTest {

    class MockMessageRouterHttpClient extends MessageRouterHttpClient {
        protected Client getClient(Integer connectTimeoutSeconds, Integer readTimeoutMinutes) {
            ClientBuilder clientBuilder = ClientBuilder.newBuilder();
            return clientBuilder.build();
        }
    }

    public MessageRouterHttpClient getClient() {
        Properties properties = new Properties();
        properties.put("username", "my_user");
        properties.put("password", "my_password");
        properties.put("topic", "network_automation");
        properties.put("group", "message_processors");
        properties.put("host", "dmaap-server.com");
        properties.put("id", "machine_one");
        properties.put("fetch", "machine_one");

        MockMessageRouterHttpClient client = new MockMessageRouterHttpClient();
        client.processProperties(properties);
        return client;
    }

    @Test
    public void processMsg() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClient client = getClient();
        client.processMsg(null);
    }

    @Test
    public void isReady() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClient client = getClient();
        assertEquals(true, client.isReady());
    }

    @Test
    public void isRunning() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClient client = getClient();
        assertEquals(false, client.isRunning());
    }

    @Test
    public void buidUrl() throws InvalidMessageException, MalformedURLException, URISyntaxException {
        MessageRouterHttpClient client = getClient();
        assertEquals(new URI(
                "http://dmaap-server.com/events/network_automation/message_processors/machine_one?timeout=15000"),
                client.uri);
    }

    @Test
    public void buidUrlWithFilter() throws InvalidMessageException, MalformedURLException, URISyntaxException {
        Properties properties = new Properties();
        properties.put("username", "my_user");
        properties.put("password", "my_password");
        properties.put("topic", "network_automation");
        properties.put("group", "message_processors");
        properties.put("host", "dmaap-server.com");
        properties.put("id", "machine_one");
        properties.put("filter", "{\"class\":\"Contains\",\"string\":\"hello\",\"value\":\"world\"}");
        properties.put("fetchPause", "3000");
        MessageRouterHttpClient client = new MockMessageRouterHttpClient();
        client.processProperties(properties);
        assertEquals(new URI(
                "http://dmaap-server.com/events/network_automation/message_processors/machine_one?timeout=15000&filter=%7B%22class%22%3A%22Contains%22%2C%22string%22%3A%22hello%22%2C%22value%22%3A%22world%22%7D"),
                client.uri);
    }

    @Test
    public void buildAuthorizationString() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClient client = getClient();
        String authString = client.buildAuthorizationString("Hello", "World");
        assertEquals("Basic SGVsbG86V29ybGQ=", authString);
    }

    @Test
    public void clientFromProperties() throws InvalidMessageException, MalformedURLException, URISyntaxException {
        MessageRouterHttpClient client = new MockMessageRouterHttpClient();
        Properties props = new Properties();
        client.init(props, "src/test/resources/dmaap-consumer-1.properties");
        assertEquals(new URI(
                "http://localhost:3904/events/ccsdk-topic/ccsdk-unittest/ccsdk_unittest?timeout=15000&limit=1000"),
                client.uri);
    }

}
