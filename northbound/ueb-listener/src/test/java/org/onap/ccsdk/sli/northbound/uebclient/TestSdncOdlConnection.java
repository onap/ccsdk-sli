package org.onap.ccsdk.sli.northbound.uebclient;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.INotificationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import static org.mockito.Mockito.mock;

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
