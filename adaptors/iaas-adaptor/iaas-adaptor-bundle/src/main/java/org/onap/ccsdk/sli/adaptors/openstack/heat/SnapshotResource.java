/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.ccsdk.sli.adaptors.openstack.heat;

import com.woorea.openstack.base.client.Entity;
import com.woorea.openstack.base.client.HttpMethod;
import com.woorea.openstack.base.client.OpenStackClient;
import com.woorea.openstack.base.client.OpenStackRequest;
import org.onap.ccsdk.sli.adaptors.openstack.heat.model.CreateSnapshotParams;
import org.onap.ccsdk.sli.adaptors.openstack.heat.model.Snapshot;

public class SnapshotResource {

    private final OpenStackClient client;

    public SnapshotResource(OpenStackClient client) {
        this.client = client;
    }

    public CreateSnapshot create(String stackName, String stackID, CreateSnapshotParams params) {
        return new CreateSnapshot(stackName, stackID, params);
    }

    public RestoreSnapshot restore(String stackName, String stackID, String snapshotID) {
        return new RestoreSnapshot(stackName, stackID, snapshotID);
    }

    public ShowSnapshot show(String stackName, String stackID, String snapshotID) {
        return new ShowSnapshot(stackName, stackID, snapshotID);
    }

    public class CreateSnapshot extends OpenStackRequest<Snapshot> {
        public CreateSnapshot(String stackName, String stackID, CreateSnapshotParams params) {
            super(client, HttpMethod.POST, "/stacks/" + stackName + "/" + stackID + "/snapshots", Entity.json(params),
                    Snapshot.class);
        }
    }

    public class RestoreSnapshot extends OpenStackRequest<Void> {
        public RestoreSnapshot(String stackName, String stackID, String snapshotID) {
            super(client, HttpMethod.POST,
                    "/stacks/" + stackName + "/" + stackID + "/snapshots/" + snapshotID + "/restore", null, Void.class);
        }
    }

    public class ShowSnapshot extends OpenStackRequest<Snapshot> {
        public ShowSnapshot(String stackName, String stackID, String snapshotID) {
            super(client, HttpMethod.GET, "/stacks/" + stackName + "/" + stackID + "/snapshots/" + snapshotID, null,
                    Snapshot.class);
        }
    }
}
