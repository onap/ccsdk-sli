/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * =============================================================================
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.netconf.odlconnector;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.Properties;
import org.apache.http.HttpStatus;
import org.onap.ccsdk.sli.adaptors.netconf.HttpClient;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfAdaptorConstants;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfClient;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfClientRestconf;
import org.onap.ccsdk.sli.adaptors.netconf.NetconfConnectionDetails;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class NetconfClientRestconfImpl implements NetconfClient, NetconfClientRestconf {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(NetconfClientRestconfImpl.class);

    private NetconfConnectionDetails connectionDetails;
    private final String appFormat = "application/json";

    public NetconfClientRestconfImpl(){
        //constructor
    }

    //restconf client impl

    @SuppressWarnings("deprecation")
    @Override
    public void configure(String configuration, String deviceMountPointName, String moduleName, String nodeName) throws SvcLogicException {

        logger.info("Configuring device " + deviceMountPointName + " with configuration " + configuration);

        int httpCode = HttpClient.putMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                getModuleConfigurePath(deviceMountPointName, moduleName, nodeName), configuration, appFormat);

        if (httpCode != HttpStatus.SC_OK) {
            logger.error("Configuration request failed. throwing Exception !");
            throw new SvcLogicException("Error configuring node :" + nodeName + ", of Module :" + moduleName +
                    ", in device :" + deviceMountPointName);
        }
    }

    @Override
    public void connect(String deviceMountPointName, String payload) throws SvcLogicException{

        logger.info("Connecting device " + deviceMountPointName);

        int httpCode = HttpClient.postMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                getConnectPath(), payload, appFormat);

        if(httpCode != HttpStatus.SC_NO_CONTENT){
            logger.error("Connect request failed with code " + httpCode + ". throwing Exception !");
            throw new SvcLogicException("Error connecting device :" + deviceMountPointName);
        }
    }

    @Override
    public boolean checkConnection(String deviceMountPointName) throws SvcLogicException {
        logger.info("Checking device " + deviceMountPointName + " connectivity");

        String result = HttpClient.getMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP,
                NetconfAdaptorConstants.CONTROLLER_PORT, getCheckConnectivityPath(deviceMountPointName), appFormat);

        return result != null;
    }

    @Override
    public void disconnect(String deviceMountPointName) throws SvcLogicException {
        logger.info("Disconnecting " + deviceMountPointName);

        int httpCode = HttpClient.deleteMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                getDisconnectPath(deviceMountPointName), appFormat);

        if(httpCode != HttpStatus.SC_OK){
            logger.error("Disconnection of device " + deviceMountPointName + " failed!");
            throw new SvcLogicException("Disconnection of device " + deviceMountPointName + " failed!");
        }
    }

    @Override
    public String getConfiguration(String deviceMountPointName, String moduleName, String nodeName) throws SvcLogicException{
        logger.info("Getting configuration of device " + deviceMountPointName);

        String result = HttpClient.getMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                getModuleConfigurePath(deviceMountPointName, moduleName, nodeName), appFormat);

        if (result == null) {
            logger.error("Configuration request failed. throwing Exception !");
            throw new SvcLogicException("Error getting configuration of node :" + nodeName + ", of Module :" + moduleName +
                    ", in device :" + deviceMountPointName);
        }

        return result;
    }

    //netconf client impl

    @Override
    public void connect(NetconfConnectionDetails connectionDetails) throws SvcLogicException {
        if(connectionDetails == null){
            throw new SvcLogicException("Invalid connection details - null value");
        }
        this.connectionDetails = connectionDetails;
        this.connect(connectionDetails.getHost(), getPayload());
    }

    @Override
    public String exchangeMessage(String message) throws SvcLogicException {
        // TODO implement
        return null;
    }

    @Override
    public void configure(String configuration) throws SvcLogicException {
        if(connectionDetails == null){
            throw new SvcLogicException("Invalid connection details - null value");
        }

        Properties props = connectionDetails.getAdditionalProperties();
        if(props == null || !props.containsKey("module.name") || !props.containsKey("node.name")) {
            throw new SvcLogicException("Invalid properties!");
        }

        String moduleName = props.getProperty("module.name");
        String nodeName = props.getProperty("node.name");
        String deviceMountPointName = connectionDetails.getHost();

        int httpCode = HttpClient.putMethod(NetconfAdaptorConstants.PROTOCOL, NetconfAdaptorConstants.CONTROLLER_IP, NetconfAdaptorConstants.CONTROLLER_PORT,
                getModuleConfigurePath(deviceMountPointName, moduleName, nodeName), configuration, "application/xml");

        if (httpCode != HttpStatus.SC_OK) {
            logger.error("Configuration request failed. throwing Exception !");
            throw new SvcLogicException("Error configuring node :" + nodeName + ", of Module :" + moduleName +
                    ", in device :" + deviceMountPointName);
        }
    }

    @Override
    public String getConfiguration() throws SvcLogicException {
        if(connectionDetails == null){
            throw new SvcLogicException("Invalid connection details - null value");
        }

        Properties props = connectionDetails.getAdditionalProperties();
        if(props == null || !props.containsKey("module.name") || !props.containsKey("node.name")) {
            throw new SvcLogicException("Invalid properties!");
        }

        return this.getConfiguration(connectionDetails.getHost(), props.getProperty("module.name"),
                props.getProperty("node.name"));
    }

    @Override
    public void disconnect() throws SvcLogicException {
        if(connectionDetails == null){
            throw new SvcLogicException("Invalid connection details - null value");
        }
        this.disconnect(connectionDetails.getHost());
    }

    //private methods
    private String getModuleConfigurePath(String deviceMountPointName, String moduleName, String nodeName){

        String deviceSpecificPath = deviceMountPointName + "/yang-ext:mount/" + moduleName + ":" + nodeName;

        return NetconfAdaptorConstants.CONFIGURE_PATH + deviceSpecificPath;
    }

    private String getConnectPath(){

        return NetconfAdaptorConstants.CONNECT_PATH;
    }

    private String getCheckConnectivityPath(String deviceMountPointName) {
        return NetconfAdaptorConstants.CHECK_CONNECTION_PATH + deviceMountPointName;
    }

    private String getDisconnectPath(String deviceMountPointName) {
        return NetconfAdaptorConstants.DISCONNECT_PATH + deviceMountPointName;
    }

    private String getPayload() {
        return "{\n" +
                "    \"config:module\":\n" +
                "        {\n" +
                "        \"type\":\"odl-sal-netconf-connector-cfg:sal-netconf-connector\",\n" +
                "        \"netconf-northbound-ssh\\odl-sal-netconf-connector-cfg:name\":"+connectionDetails.getHost()+",\n" +
                "        \"odl-sal-netconf-connector-cfg:address\":"+connectionDetails.getHost()+",\n" +
                "        \"odl-sal-netconf-connector-cfg:port\":"+connectionDetails.getPort()+",\n" +
                "        \"odl-sal-netconf-connector-cfg:username\":"+connectionDetails.getUsername()+",\n" +
                "        \"odl-sal-netconf-connector-cfg:password\":"+connectionDetails.getPassword()+",\n" +
                "        \"tcp-only\":\"false\",\n" +
                "        \"odl-sal-netconf-connector-cfg:event-executor\":\n" +
                "            {\n" +
                "            \"type\":\"netty:netty-event-executor\",\n" +
                "            \"name\":\"global-event-executor\"\n" +
                "            },\n" +
                "        \"odl-sal-netconf-connector-cfg:binding-registry\":\n" +
                "            {\n" +
                "            \"type\":\"opendaylight-md-sal-binding:binding-broker-osgi-registry\",\n" +
                "            \"name\":\"binding-osgi-broker\"\n" +
                "            },\n" +
                "        \"odl-sal-netconf-connector-cfg:dom-registry\":\n" +
                "            {\n" +
                "            \"type\":\"opendaylight-md-sal-dom:dom-broker-osgi-registry\",\n" +
                "            \"name\":\"dom-broker\"\n" +
                "            },\n" +
                "        \"odl-sal-netconf-connector-cfg:client-dispatcher\":\n" +
                "            {\n" +
                "            \"type\":\"odl-netconf-cfg:netconf-client-dispatcher\",\n" +
                "            \"name\":\"global-netconf-dispatcher\"\n" +
                "            },\n" +
                "        \"odl-sal-netconf-connector-cfg:processing-executor\":\n" +
                "            {\n" +
                "            \"type\":\"threadpool:threadpool\",\n" +
                "            \"name\":\"global-netconf-processing-executor\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }
}
