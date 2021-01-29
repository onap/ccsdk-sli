/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 							reserved.
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

package org.onap.sdnc.northbound.dataChange;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.northbound.DataChangeProvider;
import org.onap.ccsdk.sli.northbound.DataChangeClient;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.datachange.rev150519.DataChangeNotificationInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.datachange.rev150519.DataChangeNotificationInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.northbound.datachange.rev150519.DataChangeNotificationOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TestDataChange extends AbstractConcurrentDataBrokerTest {

    private DataChangeProvider dataChangeProvider;
    private static final Logger LOG = LoggerFactory.getLogger(DataChangeProvider.class);

    @Before
    public void setUp() throws Exception {
        if (null == dataChangeProvider) {
            DataBroker dataBroker = getDataBroker();
            NotificationPublishService mockNotification = mock(NotificationPublishService.class);
            RpcProviderRegistry mockRpcRegistry = mock(RpcProviderRegistry.class);
            DataChangeClient mockSliClient = mock(DataChangeClient.class);
            dataChangeProvider = new DataChangeProvider(dataBroker, mockNotification, mockRpcRegistry, mockSliClient);
        }
    }

    //Testcase should return error 503 when No service logic active for dataChange.
    @Test
    public void testDataChangeNotification() {

        DataChangeNotificationInputBuilder inputBuilder = new DataChangeNotificationInputBuilder();

        inputBuilder.setAaiEventId("1");


        // TODO: currently initialize SvcLogicServiceClient is failing, need to fix
        java.util.concurrent.Future<RpcResult<DataChangeNotificationOutput>> future = dataChangeProvider
                                                                          .dataChangeNotification(inputBuilder.build());
        RpcResult<DataChangeNotificationOutput> rpcResult = null;
        try {
            rpcResult = future.get();
        } catch (Exception e) {
            fail("Error : " + e);
        }
        LOG.info("result: {}", rpcResult);
        assertEquals("503", rpcResult.getResult().getDataChangeResponseCode());
    }

    //Input parameter validation
    @Test
    public void testDataChangeNotificationInputValidation() {

        java.util.concurrent.Future<RpcResult<DataChangeNotificationOutput>> future = dataChangeProvider
                                                                                      .dataChangeNotification(null);
        RpcResult<DataChangeNotificationOutput> rpcResult = null;
        try {
            rpcResult = future.get();
        } catch (Exception e) {
            fail("Error : " + e);
        }
        LOG.info("result: {}", rpcResult);
        assertEquals("403", rpcResult.getResult().getDataChangeResponseCode());
    }
}
