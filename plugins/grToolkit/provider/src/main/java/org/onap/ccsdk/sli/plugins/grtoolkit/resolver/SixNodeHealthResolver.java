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
import org.json.JSONObject;

import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.plugins.grtoolkit.connection.ConnectionManager;
import org.onap.ccsdk.sli.plugins.grtoolkit.connection.ConnectionResponse;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.AdminHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterActor;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.ClusterHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.DatabaseHealth;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.FailoverStatus;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.Health;
import org.onap.ccsdk.sli.plugins.grtoolkit.data.SiteHealth;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.plugins.gr.toolkit.rev180926.FailoverInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Implementation of {@code HealthResolver} for a six node controller
 * architecture, where three nodes are located in one data center, and the
 * other three nodes are located in another. The sites are assumed to be in an
 * Active/Standby configuration, with the Active site nodes voting and the
 * Standby site notes non-voting.
 *
 * @author Anthony Haddox
 * @see HealthResolver
 */
public class SixNodeHealthResolver extends HealthResolver {
    private final Logger log = LoggerFactory.getLogger(SixNodeHealthResolver.class);

    /**
     * Constructs the health resolver used by the {@code GrToolkitProvider} to
     * determine the health of the application components.
     *
     * @param map a HashMap containing all of the nodes in the akka cluster
     * @param properties the properties passed ino the provider
     * @param dbLib a reference to the {@code DbLibService} of the provider
     * @see HealthResolver
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     */
    public SixNodeHealthResolver(Map<String, ClusterActor> map, Properties properties, DbLibService dbLib) {
        super(map, properties, dbLib);
        resolveSites();
    }

    /**
     * Implementation of {@code getClusterHealth()}. Uses the
     * {@code ShardResolver} to gather health information about the controller.
     * If 4 of 6 members are healthy, the cluster is deemed healthy.
     *
     * @return an {@code ClusterHealth} object with health of the akka cluster
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     * @see HealthResolver
     * @see ClusterHealth
     * @see ShardResolver
     */
    @Override
    public ClusterHealth getClusterHealth() {
        log.info("getClusterHealth(): Getting cluster health...");
        shardResolver.getControllerHealth(memberMap);
        long healthyMembers = memberMap.values().stream().filter(member -> member.isUp() && ! member.isUnreachable()).count();
        return (healthyMembers > 4) ? new ClusterHealth().withHealth(Health.HEALTHY) : new ClusterHealth().withHealth(Health.FAULTY);
    }

    /**
     * Implementation of {@code getSiteHealth()}. Gathers health information on
     * all of the contollers, then separates the nodes into voting and
     * non-voting sites. Each site is then checked for its health and the
     * result is returned as a List.
     *
     * @return a List of {@code SiteHealth} objects with health of the site
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     * @see HealthResolver
     * @see SiteHealth
     * @see ShardResolver
     */
    @Override
    public List<SiteHealth> getSiteHealth() {
        log.info("getSiteHealth(): Getting site health...");

        // Get cluster health to populate memberMap with necessary values
        getClusterHealth();
        List<ClusterActor> votingActors = memberMap.values().stream().filter(ClusterActor::isVoting).collect(Collectors.toList());
        List<ClusterActor> nonVotingActors = memberMap.values().stream().filter(member -> !member.isVoting()).collect(Collectors.toList());

        SiteHealth votingSiteHealth = getSiteHealth(votingActors).withRole("ACTIVE");
        SiteHealth nonVotingSiteHealth = getSiteHealth(nonVotingActors).withRole("STANDBY");
        return Arrays.asList(votingSiteHealth, nonVotingSiteHealth);
    }

    /**
     * Gathers the site identifier, admin health, and database health of a
     * site.
     *
     * @return a {@code SiteHealth} object with health of the site
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     * @see ClusterActor
     * @see SiteHealth
     * @see ConnectionManager
     */
    public SiteHealth getSiteHealth(List<ClusterActor> actorList) {
        AdminHealth adminHealth = null;
        DatabaseHealth databaseHealth = null;
        String siteId = null;
        int healthyMembers = 0;

        for(ClusterActor actor : actorList) {
            if(actor.isUp() && !actor.isUnreachable()) {
                healthyMembers++;
            }
            if(siteId == null) {
                try {
                    String content = ConnectionManager.getConnectionResponse(httpProtocol + actor.getNode() + ":" + controllerPort + "/restconf/operations/gr-toolkit:site-identifier", ConnectionManager.HttpMethod.POST, null, credentials).content;
                    siteId = new JSONObject(content).getJSONObject(OUTPUT).getString("id");
                } catch(IOException e) {
                    log.error("getSiteHealth(): Error getting site identifier from {}", actor.getNode());
                    log.error("getSiteHealth(): IOException", e);
                }
            }
            if(adminHealth == null) {
                try {
                    boolean isAdminHealthy  = isRemoteComponentHealthy(httpProtocol + actor.getNode() + ":" + controllerPort + "/restconf/operations/gr-toolkit:admin-health");
                    if(isAdminHealthy) {
                        adminHealth = new AdminHealth(Health.HEALTHY, 200);
                    }
                } catch(IOException e) {
                    log.error("getSiteHealth(): Error getting admin health from {}", actor.getNode());
                    log.error("getSiteHealth(): IOException", e);
                }
            }
            if(databaseHealth == null) {
                try {
                    boolean isDatabaseHealthy = isRemoteComponentHealthy(httpProtocol + actor.getNode() + ":" + controllerPort + "/restconf/operations/gr-toolkit:database-health");
                    if(isDatabaseHealthy) {
                        databaseHealth = new DatabaseHealth(Health.HEALTHY);
                    }
                } catch(IOException e) {
                    log.error("getSiteHealth(): Error getting database health from {}", actor.getNode());
                    log.error("getSiteHealth(): IOException", e);
                }
            }
        }

        if(siteId == null) {
            siteId = "UNKNOWN SITE";
        }
        if(adminHealth == null) {
            adminHealth = new AdminHealth(Health.FAULTY, 500);
        }
        if(databaseHealth == null) {
            databaseHealth = new DatabaseHealth(Health.FAULTY);
        }
        SiteHealth health = new SiteHealth()
                                    .withAdminHealth(adminHealth)
                                    .withDatabaseHealth(databaseHealth)
                                    .withId(siteId);
        if(isHealthy(adminHealth.getHealth()) && isHealthy(databaseHealth.getHealth()) && healthyMembers > 1) {
            health.setHealth(Health.HEALTHY);
        }

        return health;
    }

    /**
     * Implementation of {@code tryFailover()}. Performs a preliminary call to
     * {@code getClusterHealth} to populate information about the cluster. If
     * no voting members can be found, the method terminates immediately. The
     * nodes are separated into voting and non-voting sites, and a driving
     * operator is selected from the non-voting nodes to perform requests
     * against. A payload to swap voting between sites is sent to the operator
     * to perform a controller-level failover.
     *
     * @return an {@code SiteHealth} object with health of the site
     * @see org.onap.ccsdk.sli.plugins.grtoolkit.GrToolkitProvider
     * @see HealthResolver
     * @see FailoverStatus
     * @see FailoverInput
     */
    @Override
    public FailoverStatus tryFailover(FailoverInput input) {
        // Get Cluster Health to populate the memberMap with the necessary values
        log.info("tryFailover(): Performing preliminary health check...");
        getClusterHealth();
        FailoverStatus status = new FailoverStatus();
        ConnectionResponse votingResponse = null;
        List<ClusterActor> votingActors = memberMap.values().stream().filter(ClusterActor::isVoting).collect(Collectors.toList());
        List<ClusterActor> nonVotingActors = memberMap.values().stream().filter(member -> !member.isVoting()).collect(Collectors.toList());

        if(nonVotingActors.size() == 0) {
            status.setStatusCode(500);
            status.setMessage("No nonvoting members found. Cannot perform voting switch.");
            return status;
        }

        ClusterActor operator;
        try {
            operator = nonVotingActors.stream().filter(this::isControllerHealthy).findFirst().get();
        } catch(NoSuchElementException e) {
            log.error("tryFailover(): Could not find any healthy members.", e);
            status.setStatusCode(500);
            status.setMessage("Could not find any healthy members.");
            return status;
        }

        // Assuming two 3 node sites, 3 voting and 3 non voting
        if(votingActors.size() < 3 || nonVotingActors.size() < 3) {
            log.warn("tryFailover(): Sites do not contain an equal amount of voting and nonvoting members: Voting: {} | NonVoting: {}", votingActors.size(), nonVotingActors.size());
        }
        log.info("tryFailover(): Swapping voting...");
        try {
            JSONObject votingInput = new JSONObject();
            JSONObject inputBlock = new JSONObject();
            JSONArray votingStateArray = new JSONArray();
            JSONObject memberVotingState;
            for(ClusterActor actor : votingActors) {
                memberVotingState = new JSONObject();
                memberVotingState.put("member-name", actor.getMember());
                memberVotingState.put("voting", false);
                votingStateArray.put(memberVotingState);
            }
            for(ClusterActor actor : nonVotingActors) {
                memberVotingState = new JSONObject();
                memberVotingState.put("member-name", actor.getMember());
                memberVotingState.put("voting", true);
                votingStateArray.put(memberVotingState);
            }
            inputBlock.put("member-voting-state", votingStateArray);
            votingInput.put("input", inputBlock);
            log.debug("tryFailover(): {}", votingInput);
            // Change voting all shards
            votingResponse = ConnectionManager.getConnectionResponse(httpProtocol + operator.getNode() + ":" + controllerPort + "/restconf/operations/cluster-admin:change-member-voting-states-for-all-shards", ConnectionManager.HttpMethod.POST, votingInput.toString(), credentials);
        } catch(IOException e) {
            log.error("tryFailover(): Failure changing voting", e);
        }
        if(votingResponse != null) {
            if(votingResponse.statusCode != 200) {
                status.setStatusCode(votingResponse.statusCode);
                status.setMessage("Failed to swap voting.");
            } else {
                status.setStatusCode(200);
                status.setMessage("Failover complete.");
            }
        } else {
            status.setStatusCode(500);
            status.setMessage("Failed to swap voting.");
        }

        return status;
    }

    /**
     * Implementation of {@code resolveSites()}. Calls
     * {@code resolveSiteForMember()} to resolve which site a member belongs to.
     *
     * @see HealthResolver
     */
    @Override
    public void resolveSites() {
        log.info("Map contains {} entries", memberMap.size());
        memberMap.forEach((key, value) -> resolveSiteForMember(value));
    }

    /**
     * Resolves which site a member belongs to. Members 1-3 are assumed to be
     * <i>Site 1</i> while members 4-6 are assumed to be <i>Site 2</i>.
     *
     * @see HealthResolver
     */
    private void resolveSiteForMember(ClusterActor actor) {
        try {
            int memberNumber = Integer.parseInt(actor.getMember().split("-")[1]);
            if(memberNumber < 4) {
                actor.setSite("Site 1");
            } else {
                actor.setSite("Site 2");
            }
            log.info("resolveSiteForMember(): {} belongs to {}", actor.getNode(), actor.getSite());
        } catch (NumberFormatException e) {
            log.error("resolveSiteForMember(): Could not parse member number for {}. Defaulting to Site 1.", actor.getNode());
            actor.setSite("resolveSiteForMember(): Site 1");
        }
    }
}