package org.onap.ccsdk.sli.northbound.dmaapclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.junit.Test;

public class MessageRouterHttpClientJdkTest {
    public MessageRouterHttpClientJdk getClient() throws MalformedURLException {
        Properties properties = new Properties();
        properties.put("username", "my_user");
        properties.put("password", "my_password");
        properties.put("topic", "network_automation");
        properties.put("group", "message_processors");
        properties.put("host", "dmaap-server.com");
        properties.put("id", "machine_one");
        properties.put("fetchPause", "3000");
        MessageRouterHttpClientJdk client = new MessageRouterHttpClientJdk();
        client.processProperties(properties);
        return client;
    }

    @Test
    public void processMsg() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClientJdk client = getClient();
        client.processMsg(null);
    }

    @Test
    public void isReady() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClientJdk client = getClient();
        assertEquals(true, client.isReady());
    }

    @Test
    public void isRunning() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClientJdk client = getClient();
        assertEquals(false, client.isRunning());
    }

    @Test
    public void buidUrl() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClientJdk client = getClient();
        assertEquals(new URL(
                "http://dmaap-server.com/events/network_automation/message_processors/machine_one?timeout=15000"),
                client.url);
    }

    @Test
    public void buidUrlWithFilter() throws InvalidMessageException, MalformedURLException {
        Properties properties = new Properties();
        properties.put("username", "my_user");
        properties.put("password", "my_password");
        properties.put("topic", "network_automation");
        properties.put("group", "message_processors");
        properties.put("host", "dmaap-server.com");
        properties.put("id", "machine_one");
        properties.put("filter", "{\"class\":\"Contains\",\"string\":\"hello\",\"value\":\"world\"}");
        properties.put("fetchPause", "3000");
        MessageRouterHttpClientJdk client = new MessageRouterHttpClientJdk();
        client.processProperties(properties);
        assertEquals(new URL(
                "http://dmaap-server.com/events/network_automation/message_processors/machine_one?timeout=15000&filter=%7B%22class%22%3A%22Contains%22%2C%22string%22%3A%22hello%22%2C%22value%22%3A%22world%22%7D"),
                client.url);
    }

    @Test
    public void buildAuthorizationString() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClientJdk client = getClient();
        String authString = client.buildAuthorizationString("Hello", "World");
        assertEquals("Basic SGVsbG86V29ybGQ=", authString);
    }

    @Test
    public void clientFromProperties() throws InvalidMessageException, MalformedURLException {
        MessageRouterHttpClientJdk client = new MessageRouterHttpClientJdk();
        Properties props = new Properties();
        client.init(props, "src/test/resources/dmaap-consumer-1.properties");
        assertEquals(new URL(
                "http://localhost:3904/events/ccsdk-topic/ccsdk-unittest/ccsdk_unittest?timeout=15000&limit=1000"),
                client.url);
    }

    @Test
    public void buildHttpURLConnection() throws InvalidMessageException, IOException {
        MessageRouterHttpClientJdk client = getClient();
        HttpURLConnection connection = client.buildHttpURLConnection();
        assertEquals("GET", connection.getRequestMethod());
        assertTrue(connection.getRequestProperties().get("Accept").contains("application/json"));
        assertEquals(false, connection.getUseCaches());
        Integer defaultConnectTimeout = Integer.valueOf(client.DEFAULT_CONNECT_TIMEOUT);
        Integer defaultReadTimeout = Integer.valueOf(client.DEFAULT_READ_TIMEOUT);
        assertEquals(defaultConnectTimeout.intValue(), connection.getConnectTimeout());
        assertEquals(defaultReadTimeout.intValue(), connection.getReadTimeout());
    }
}
