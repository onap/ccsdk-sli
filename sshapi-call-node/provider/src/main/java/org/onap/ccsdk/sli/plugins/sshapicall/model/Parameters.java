/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics. All rights
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

package org.onap.ccsdk.sli.plugins.sshapicall.model;

import java.util.Set;

public class Parameters {
    public String sshapiUrl;
    public int sshapiPort;
    public String sshapiUser;
    public String sshapiPassword;
    public String sshKey;
    public long sshExecTimeout;
    public String sshFileParameters;
    public String sshEnvParameters;
    public boolean sshWithRetry;
    public String cmd;
    public String responsePrefix;
    public Format responseType;

    public Set<String> listNameList;
    public boolean convertResponse;
    public AuthType authtype;
}
