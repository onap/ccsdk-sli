/*
 * ============LICENSE_START==========================================
 *  org.onap.music
 * ===================================================================
 *  Copyright (c) 2019 IBM.
 * ===================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * ============LICENSE_END=============================================
 * ====================================================================
 */
package org.onap.ccsdk.sli.adaptors.openstack.heat.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class TestSnapshotDetails {

    private SnapshotDetails snapshotDetails;
    private Snapshot snapshot;

    @Before
    public void setUp() {
        snapshotDetails = new SnapshotDetails();
        snapshot = new Snapshot();
    }

    @Test
    public void testGetSnapshot() {
        snapshotDetails.setSnapshot(snapshot);
        assertSame(snapshot, snapshotDetails.getSnapshot());
    }

    @Test
    public void testToString() {
        snapshotDetails.setSnapshot(snapshot);
        assertNotNull(snapshotDetails.toString());
    }

}
