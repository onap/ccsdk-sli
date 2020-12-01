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

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.cluster.health.output.MembersBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.member.CommitStatusBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.member.ReplicasBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.member.LeaderBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extends the {@code MembersBuilder} generated from the gr-toolkit.yang model.
 * Uses information from a {@code ClusterActor} to populate the builder fields.
 *
 * @author Anthony Haddox
 * @see ClusterActor
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.cluster.health.output.MembersBuilder
 */
public class MemberBuilder extends MembersBuilder {
    public MemberBuilder(ClusterActor actor) {
        super();
        this.setAddress(actor.getNode());
        this.setRole(actor.getMember());
        this.setVoting(actor.isVoting());
        this.setUp(actor.isUp());
        this.setUnreachable(actor.isUnreachable());
        populateReplicas(actor.getReplicaShards());
        populateCommits(actor.getCommits());
        populateLeader(actor.getShardLeader());
    }

    private void populateLeader(List<String> shardLeader) {
        LeaderBuilder builder;
        this.setLeader(new ArrayList<>());
        for(String leader : shardLeader) {
            builder = new LeaderBuilder();
            builder.setShard(leader);
            this.getLeader().add(builder.build());
        }
    }

    private void populateCommits(Map<String, Integer> commits) {
        CommitStatusBuilder builder;
        this.setCommitStatus(new ArrayList<>());
        for(Map.Entry<String, Integer> entry : commits.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if(value != 0) {
                builder = new CommitStatusBuilder();
                builder.setShard(key);
                builder.setDelta(value);
                this.getCommitStatus().add(builder.build());
            }
        }
    }

    private void populateReplicas(List<String> replicaShards) {
        ReplicasBuilder builder;
        this.setReplicas(new ArrayList<>());
        for(String shard : replicaShards) {
            builder = new ReplicasBuilder();
            builder.setShard(shard);
            this.getReplicas().add(builder.build());
        }
    }
}
