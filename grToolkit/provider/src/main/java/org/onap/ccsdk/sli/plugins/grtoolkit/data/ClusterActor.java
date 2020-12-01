/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A data container with information about an actor in the Akka cluster.
 *
 * @author Anthony Haddox
 */
public class ClusterActor {
    private String node;
    private String member;
    private String site;
    private String akkaPort;
    private boolean voting;
    private boolean up;
    private boolean unreachable;
    private ArrayList<String> shardLeader;
    private ArrayList<String> replicaShards;
    private ArrayList<String> nonReplicaShards;
    private HashMap<String, Integer> commits;

    public static final String SITE_1 = "Site 1";
    public static final String SITE_2 = "Site 2";

    public ClusterActor() {
        node = "";
        member = "";
        site = "";
        voting = false;
        up = false;
        unreachable = false;
        shardLeader = new ArrayList<>();
        replicaShards = new ArrayList<>();
        nonReplicaShards = new ArrayList<>();
        commits = new HashMap<>();
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getAkkaPort() {
        return akkaPort;
    }

    public void setAkkaPort(String akkaPort) {
        this.akkaPort = akkaPort;
    }

    public boolean isVoting() {
        return voting;
    }

    public void setVoting(boolean voting) {
        this.voting = voting;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public boolean isUnreachable() {
        return unreachable;
    }

    public void setUnreachable(boolean unreachable) {
        this.unreachable = unreachable;
    }

    public List<String> getShardLeader() {
        return shardLeader;
    }

    public void setShardLeader(List<String> shardLeader) {
        this.shardLeader = (ArrayList<String>) shardLeader;
    }

    public List<String> getReplicaShards() {
        return replicaShards;
    }

    public void setReplicaShards(List<String> replicaShards) {
        this.replicaShards = (ArrayList<String>) replicaShards;
    }

    public List<String> getNonReplicaShards() {
        return nonReplicaShards;
    }

    public void setNonReplicaShards(List<String> nonReplicaShards) {
        this.nonReplicaShards = (ArrayList<String>) nonReplicaShards;
    }

    public Map<String, Integer> getCommits() {
        return commits;
    }

    public void setCommits(Map<String, Integer> commits) {
        this.commits = (HashMap<String, Integer>) commits;
    }

    public void flush() {
        shardLeader.clear();
        replicaShards.clear();
        nonReplicaShards.clear();
        commits.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ");
        builder.append(this.member);
        builder.append(" ] ");

        builder.append(this.node);
        builder.append(":");
        builder.append(this.akkaPort);
        builder.append(" is");
        if(up)
            builder.append(" Up");
        else
            builder.append(" Down");
        if(unreachable) {
            builder.append(" [ UNREACHABLE ]");
            return builder.toString();
        }

        if(voting)
            builder.append(" (Voting)");

        builder.append("\n");

        for(String l : this.shardLeader) {
            builder.append("\tLeader: ");
            builder.append(l);
            builder.append("\n");
        }

        for(String r : this.replicaShards) {
            builder.append("\tReplicating: ");
            builder.append(r);
            builder.append("\n");
        }

        for(String n : this.nonReplicaShards) {
            builder.append("\tNot replicating: ");
            builder.append(n);
            builder.append("\n");
        }

        for(Map.Entry<String, Integer> entry : commits.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();
            if(value > 0) {
                builder.append("\t");
                builder.append(value);
                builder.append(" commits ahead of ");
                builder.append(key);
                builder.append("\n");
            }
            else if(value < 0) {
                builder.append("\t");
                builder.append(value);
                builder.append(" commits behind ");
                builder.append(key);
                builder.append("\n");
            }
        }

        return builder.toString();
    }
}
