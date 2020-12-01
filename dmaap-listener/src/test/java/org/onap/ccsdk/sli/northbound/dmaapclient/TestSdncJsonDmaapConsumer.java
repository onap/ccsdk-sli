/*
 * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package org.onap.ccsdk.sli.northbound.dmaapclient;

import org.junit.Test;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Field;

import java.util.Map;
import java.util.Properties;

public class TestSdncJsonDmaapConsumer {
    private static final String DMAAP_LISTENER_PROPERTIES = "dmaap-listener.properties";
    private static final String DMAAP_LISTENER_PROPERTIES_DIR = "src/test/resources";

    @Test(expected = InvalidMessageException.class)
    public void testProcessMsg_shouldThrowException() throws Exception {
        SdncFlatJsonDmaapConsumer consumer = new SdncFlatJsonDmaapConsumer();
        consumer.processMsg(null);
    }
    
    @Test
    public void testProcessMsgFieldMap() throws Exception {
        SdncFlatJsonDmaapConsumer consumer = new SdncFlatJsonDmaapConsumer();

        String DMAAPLISTENERROOT = "DMAAPLISTENERROOT";
        File directory = new File("lib");

        if (! directory.exists()){
            directory.mkdir();
        }

        File file = new File("lib" + "/" + "input.map");
        try{
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("SDNC.endpoint=>http://localhost:8282/restconf/operations");
            bw.close();
        }
        catch (Exception e){
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

        String msg = "{\n" +
                "    \"input\" : {        \n" +
                "    }\n" +
                "}";

        InputStream propStr = TestSdncJsonDmaapConsumer.class.getResourceAsStream("/dmaap-consumer-pserver.properties");
        Properties props = new Properties();

        props.load(propStr);

        consumer.init(props, "src/test/resources/dmaap-consumer-pserver.properties");
        consumer.processMsg(msg);
    }

    @Test(expected = InvalidMessageException.class)
    public void testProcessMsgFieldMapNoSdncEndPoint() throws Exception {
        SdncFlatJsonDmaapConsumer consumer = new SdncFlatJsonDmaapConsumer();

        String DMAAPLISTENERROOT = "DMAAPLISTENERROOT";
        File directory = new File("lib");

        if (! directory.exists()){
            directory.mkdir();
        }

        File file = new File("lib" + "/" + "input.map");
        try{
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("SDNC");
            bw.close();
        }
        catch (Exception e){
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

        String msg = "{\n" +
                "    \"input\" : {        \n" +
                "    }\n" +
                "}";

        InputStream propStr = TestSdncJsonDmaapConsumer.class.getResourceAsStream("/dmaap-consumer-pserver.properties");
        Properties props = new Properties();

        props.load(propStr);

        consumer.init(props, "src/test/resources/dmaap-consumer-pserver.properties");
        consumer.processMsg(msg);
    }

    @Test(expected = InvalidMessageException.class)
    public void testProcessMsgFieldMapNoFieldMap() throws Exception {
        SdncFlatJsonDmaapConsumer consumer = new SdncFlatJsonDmaapConsumer();

        String msg = "{\n" +
                "    \"input\" : {        \n" +
                "    }\n" +
                "}";

        consumer.processMsg(msg);
    }
}
