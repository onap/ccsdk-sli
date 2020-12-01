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

package org.onap.ccsdk.sli.plugins.grtoolkit.resolver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.onap.ccsdk.sli.plugins.grtoolkit.connection.ConnectionManager;
import org.onap.ccsdk.sli.plugins.grtoolkit.connection.ConnectionResponse;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterActor;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.PropertyKeys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to perform operations on the data shard information returned as JSON
 * from Jolokia.
 *
 * @author Anthony Haddox
 * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
 * @see HealthResolver
 */
public class ShardResolver {
    private final Logger log = LoggerFactory.getLogger(ShardResolver.class);
    private static ShardResolver _shardResolver;

    private String jolokiaClusterPath;
    private String shardManagerPath;
    private String shardPathTemplate;
    private String credentials;
    private String httpProtocol;

    private static final String VALUE = "value";

    private ShardResolver(Properties properties) {
        String port = "true".equals(properties.getProperty(PropertyKeys.CONTROLLER_USE_SSL).trim()) ? properties.getProperty(PropertyKeys.CONTROLLER_PORT_SSL).trim() : properties.getProperty(PropertyKeys.CONTROLLER_PORT_HTTP).trim();
        httpProtocol = "true".equals(properties.getProperty(PropertyKeys.CONTROLLER_USE_SSL).trim()) ? "https://" : "http://";
        jolokiaClusterPath = ":" + port + properties.getProperty(PropertyKeys.MBEAN_CLUSTER).trim();
        shardManagerPath = ":" + port + properties.getProperty(PropertyKeys.MBEAN_SHARD_MANAGER).trim();
        shardPathTemplate = ":" + port + properties.getProperty(PropertyKeys.MBEAN_SHARD_CONFIG).trim();
        credentials = properties.getProperty(PropertyKeys.CONTROLLER_CREDENTIALS).trim();
    }

    public static ShardResolver getInstance(Properties properties) {
        if (_shardResolver == null) {
            _shardResolver = new ShardResolver(properties);
        }
        return _shardResolver;
    }

    private void getMemberStatus(ClusterActor clusterActor) throws IOException {
        log.info("getMemberStatus(): Getting member status for {}", clusterActor.getNode());
        ConnectionResponse response = ConnectionManager.getConnectionResponse(httpProtocol + clusterActor.getNode() + jolokiaClusterPath, ConnectionManager.HttpMethod.GET, null, credentials);
        try {
            JSONObject responseJson = new JSONObject(response.content);
            JSONObject responseValue = responseJson.getJSONObject(VALUE);
            clusterActor.setUp("Up".equals(responseValue.getString("MemberStatus")));
            clusterActor.setUnreachable(false);
        } catch(JSONException e) {
            log.error("getMemberStatus(): Error parsing response from {}", clusterActor.getNode(), e);
            clusterActor.setUp(false);
            clusterActor.setUnreachable(true);
        }
    }

    private void getShardStatus(ClusterActor clusterActor) throws IOException {
        log.info("getShardStatus(): Getting shard status for {}", clusterActor.getNode());
        ConnectionResponse response = ConnectionManager.getConnectionResponse(httpProtocol + clusterActor.getNode() + shardManagerPath, ConnectionManager.HttpMethod.GET, null, credentials);
        try {
            JSONObject responseValue = new JSONObject(response.content).getJSONObject(VALUE);
            JSONArray shardList = responseValue.getJSONArray("LocalShards");

            String pattern = "-config$";
            Pattern r = Pattern.compile(pattern);
            List<String> shards = new ArrayList<>();
            for(int ndx = 0; ndx < shardList.length(); ndx++) {
                shards.add(shardList.getString(ndx));
            }
            shards.parallelStream().forEach(shard -> {
                Matcher m = r.matcher(shard);
                String operationalShardName = m.replaceFirst("-operational");
                String shardConfigPath = String.format(shardPathTemplate, shard);
                String shardOperationalPath = String.format(shardPathTemplate, operationalShardName).replace("Config", "Operational");
                try {
                    extractShardInfo(clusterActor, shard, shardConfigPath);
                    extractShardInfo(clusterActor, operationalShardName, shardOperationalPath);
                } catch(IOException e) {
                    log.error("getShardStatus(): Error extracting shard info for {}", shard);
                }
            });
        } catch(JSONException e) {
            log.error("getShardStatus(): Error parsing response from " + clusterActor.getNode(), e);
        }
    }

    private void extractShardInfo(ClusterActor clusterActor, String shardName, String shardPath) throws IOException {
        log.info("extractShardInfo(): Extracting shard info for {}", shardName);
        String shardPrefix = "";
//        String shardPrefix = clusterActor.getMember() + "-shard-";
        log.debug("extractShardInfo(): Pulling config info for {} from: {}", shardName, shardPath);
        ConnectionResponse response = ConnectionManager.getConnectionResponse(httpProtocol + clusterActor.getNode() + shardPath, ConnectionManager.HttpMethod.GET, null, credentials);
        log.debug("extractShardInfo(): Response: {}", response.content);

        try {
            JSONObject shardValue = new JSONObject(response.content).getJSONObject(VALUE);
            clusterActor.setVoting(shardValue.getBoolean("Voting"));
            if(shardValue.getString("PeerAddresses").length() > 0) {
                clusterActor.getReplicaShards().add(shardName.replace(shardPrefix, ""));
                if(shardValue.getString("Leader").startsWith(clusterActor.getMember())) {
                    clusterActor.getShardLeader().add(shardName.replace(shardPrefix, ""));
                }
            } else {
                clusterActor.getNonReplicaShards().add(shardName.replace(shardPrefix, ""));
            }
            JSONArray followerInfo = shardValue.getJSONArray("FollowerInfo");
            for(int followerNdx = 0; followerNdx < followerInfo.length(); followerNdx++) {
                int commitIndex = shardValue.getInt("CommitIndex");
                int matchIndex = followerInfo.getJSONObject(followerNdx).getInt("matchIndex");
                if(commitIndex != -1 && matchIndex != -1) {
                    int commitsBehind = commitIndex - matchIndex;
                    clusterActor.getCommits().put(followerInfo.getJSONObject(followerNdx).getString("id"), commitsBehind);
                }
            }
        } catch(JSONException e) {
            log.error("extractShardInfo(): Error parsing response from " + clusterActor.getNode(), e);
        }
    }

    public void getControllerHealth(Map<String, ClusterActor> memberMap) {
        memberMap.values().parallelStream().forEach(this::getControllerHealth);
    }

    // Seen ConcurrentAccess issues, probably related to getting the controller health
    private synchronized void getControllerHealth(ClusterActor clusterActor) {
        clusterActor.flush();
        log.info("getControllerHealth(): Gathering info for {}", clusterActor.getNode());
        try {
            // First flush out the old values
            getMemberStatus(clusterActor);
            getShardStatus(clusterActor);
        } catch(IOException e) {
            log.error("getControllerHealth(): Connection Error", e);
            clusterActor.setUnreachable(true);
            clusterActor.setUp(false);
        }
        log.info("getControllerHealth(): MemberInfo:\n{}", clusterActor);
    }
}
