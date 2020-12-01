/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights
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
package org.onap.ccsdk.sli.plugins.grtoolkit.data;

public interface PropertyKeys {
    String RESOLVER = "resolver";
    String SITE_IDENTIFIER = "site.identifier";
    String CONTROLLER_USE_SSL = "controller.useSsl";
    String CONTROLLER_PORT_SSL = "controller.port.ssl";
    String CONTROLLER_PORT_HTTP = "controller.port.http";
    String CONTROLLER_PORT_AKKA = "controller.port.akka";
    String CONTROLLER_CREDENTIALS = "controller.credentials";
    String AKKA_CONF_LOCATION = "akka.conf.location";
    String MBEAN_CLUSTER = "mbean.cluster";
    String MBEAN_SHARD_MANAGER  = "mbean.shardManager";
    String MBEAN_SHARD_CONFIG = "mbean.shard.config";
    String ADM_USE_SSL = "adm.useSsl";
    String ADM_PORT_SSL = "adm.port.ssl";
    String ADM_PORT_HTTP = "adm.port.http";
    String ADM_FQDN = "adm.fqdn";
    String ADM_HEALTHCHECK= "adm.healthcheck";
}
