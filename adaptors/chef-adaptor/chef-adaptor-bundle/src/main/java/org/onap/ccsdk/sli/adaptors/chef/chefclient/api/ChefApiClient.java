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

package org.onap.ccsdk.sli.adaptors.chef.chefclient.api;

public interface ChefApiClient {

    org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse get(String path);

    org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse delete(String path);

    org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse post(String path, String body);

    org.onap.ccsdk.sli.adaptors.chef.chefclient.api.ChefResponse put(String path, String body);

}
