/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.chef.impl;

import org.onap.ccsdk.sli.adaptors.chef.ChefAdaptor;
import org.onap.ccsdk.sli.adaptors.chef.chefclient.ChefApiClientFactory;

public class ChefAdaptorFactory {

    private ChefApiClientFactory chefApiClientFactory = new ChefApiClientFactory();
    private org.onap.ccsdk.sli.adaptors.chef.impl.PrivateKeyChecker privateKeyChecker = new org.onap.ccsdk.sli.adaptors.chef.impl.PrivateKeyChecker();

    public ChefAdaptor create() {
        return new org.onap.ccsdk.sli.adaptors.chef.impl.ChefAdaptorImpl(chefApiClientFactory, privateKeyChecker);
    }

}
