package org.onap.ccsdk.sli.northbound.dmaapclient;

import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;

public class TestSdncOdlConnection {

	@Test
	public void test() {

		try {
			SdncOdlConnection sdncOdlConnection = SdncOdlConnection.newInstance("https://127.0.0.1:8447/aai/v11/network/pnfs/pnf/a8098c1a-f86e-11da-bd1a-00112444be1e", "", "");
			sdncOdlConnection.send("PUT", "application/json", "{\n" +
					"    \"input\" : {        \n" +
					"    }\n" +
					"}");
		} catch (ConnectException e) {
			//Connection exception
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
