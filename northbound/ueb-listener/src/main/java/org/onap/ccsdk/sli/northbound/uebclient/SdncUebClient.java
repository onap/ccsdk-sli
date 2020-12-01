/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onap.sdc.utils.DistributionActionResultEnum;

public class SdncUebClient {

	private static final Logger LOG = LoggerFactory.getLogger(SdncUebConfiguration.class);

	public static void main(String[] args) {

		SdncUebConfiguration config = new SdncUebConfiguration();

		long startTm = System.currentTimeMillis();
		int sleepTm = config.getPollingInterval() * 1000;
		long maxWaitTm = config.getClientStartupTimeout() * 1000L;

		boolean keepWaiting = true;
		boolean listenerStarted = false;
		boolean processDownloads = true;

		while (keepWaiting) {
			LOG.info("Initializing ASDC distribution client");

			IDistributionClient client = DistributionClientFactory.createDistributionClient();
			SdncUebCallback cb = new SdncUebCallback(client, config);
			IDistributionClientResult result = client.init(config, cb);
			LOG.info("Initialized ASDC distribution client - results = {}", result.getDistributionMessageResult());

			if (result.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS) {
				if (processDownloads) {
					LOG.info("Scanning for local distribution artifacts before starting client");
					cb.deployDownloadedFiles(null, null, null);
					processDownloads = false;
				}
				LOG.info("Starting client...");
				try {
					IDistributionClientResult start = client.start();
					LOG.info("Client startup result = {}", start.getDistributionMessageResult());

					// Only stop waiting if the result is success
					if (start.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS) {

						keepWaiting = false;
						listenerStarted = true;
					} else {
						LOG.info("SDC returned {} - exitting", start.getDistributionActionResult().toString());
						try {
							client.stop();
						} catch (Exception e1) {
							// Ignore exception on stop
						}
					}
				} catch (Exception e) {
					LOG.info("Client startup failure", e);
				}
			}

			if (System.currentTimeMillis() - startTm < maxWaitTm) {
				keepWaiting = false;
			} else {

				try {
					Thread.sleep(sleepTm);
				} catch (InterruptedException e) {
					// Ignore
				}
			}

		}
		
		if (!listenerStarted) {
			LOG.info("Timed out waiting to connect to SDC");
			System.exit(0);
		}
		
		

	}

}
