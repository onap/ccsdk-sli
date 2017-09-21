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

package org.onap.ccsdk.sli.core.sli.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.l3vpn.svc.part.rev170921.L3vpnSvcBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.l3vpn.svc.part.rev170921.SvcId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.l3vpn.svc.part.rev170921.l3vpn.svc.VpnServicesBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.l3vpn.svc.part.rev170921.l3vpn.svc.vpn.services.VpnSvc;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.l3vpn.svc.part.rev170921.l3vpn.svc.vpn.services.VpnSvcBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.l3vpn.svc.part.rev170921.l3vpn.svc.vpn.services.VpnSvcKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMdsalHelperUtils {

    private static final Logger log = LoggerFactory.getLogger(TestMdsalHelperUtils.class);

    @Test
    public void testL3vpnSvcToBuilder() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("l3vpn-svc.vpn-services.vpn-svc[0].vpn-id", "1");

        L3vpnSvcBuilder svcBuilder = new L3vpnSvcBuilder();

        MdsalHelper.toBuilder(ctx.toProperties(), "", svcBuilder);

        assertThat(svcBuilder.getVpnServices().getVpnSvc().get(0).getKey().getVpnId().getValue(), is("1"));
        log.info(svcBuilder.toString());
    }

    @Test
    public void testL3vpnSvcToProperties() throws SvcLogicException {
        VpnSvcBuilder vpnSvcBuilder = new VpnSvcBuilder();
        vpnSvcBuilder.setKey(new VpnSvcKey(new SvcId("1")));
        List<VpnSvc> vpnSvcList = new ArrayList<>();
        vpnSvcList.add(vpnSvcBuilder.build());
        VpnServicesBuilder vpnServicesBuilder = new VpnServicesBuilder();
        vpnServicesBuilder.setVpnSvc(vpnSvcList);
        L3vpnSvcBuilder l3vpnSvcBuilder = new L3vpnSvcBuilder();
        l3vpnSvcBuilder.setVpnServices(vpnServicesBuilder.build());

        Properties properties = new Properties();

        MdsalHelper.toProperties(properties, "", l3vpnSvcBuilder.build());

        assertThat(properties.getProperty("l3vpn-svc.vpn-services.vpn-svc[0].vpn-id"), is("1"));
        log.info(properties.toString());
    }

    // TODO add more detailed testcases.
}
