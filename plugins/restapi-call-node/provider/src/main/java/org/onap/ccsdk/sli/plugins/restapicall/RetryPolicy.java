/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * 	Modifications Copyright © 2018 IBM
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

package org.onap.ccsdk.sli.plugins.restapicall;

public class RetryPolicy {
    private String[] hostnames;
    private Integer maximumRetries;
    private int position;
    private int retryCount;

    public RetryPolicy(String[] hostnames, Integer maximumRetries) {
    this.hostnames = hostnames;
    this.maximumRetries = maximumRetries;
    this.position = 0;
    this.retryCount = 0;

    }

    public Integer getMaximumRetries() {
    return maximumRetries;
    }

    public int getRetryCount() {
    return retryCount;
    }

    public Boolean shouldRetry() {
    return retryCount < maximumRetries + 1;
    }

    public String getRetryMessage() {
    return retryCount + " retry attempts were made out of " + maximumRetries + " maximum retry attempts.";
    }
    
    public String getNextHostName() throws RetryException {
    retryCount++;
    position++;
    if (position > hostnames.length - 1) {
        position = 0;
    }
    return hostnames[position];
    }

}
