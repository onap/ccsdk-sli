#!/bin/bash

###
# ============LICENSE_START=======================================================
# ONAP : APPC
# ================================================================================
# Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Copyright (C) 2017 Amdocs
# =============================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# ============LICENSE_END=========================================================
###

ODL_HOME=${ODL_HOME:-/opt/opendaylight/current}
ODL_KARAF_CLIENT=${ODL_KARAF_CLIENT:-${ODL_HOME}/bin/client}
ODL_KARAF_CLIENT_OPTS=${ODL_KARAF_CLIENT_OPTS:-""}
INSTALLERDIR=$(dirname $0)

REPOZIP=${INSTALLERDIR}/${features.boot}-${project.version}.zip

if [ -f ${REPOZIP} ]
then
	unzip -n -d ${ODL_HOME} ${REPOZIP}

fi

COUNT=0
while [ $COUNT -lt 10 ]; do
#	${ODL_KARAF_CLIENT} ${ODL_KARAF_CLIENT_OPTS} feature:repo-add ${features.repositories} 2> /tmp/installErr
        sshpass -pkaraf ssh -o StrictHostKeyChecking=no karaf@localhost -p 8101 "feature:repo-add ${features.repositories}" 2> /tmp/installErr
	cat /tmp/installErr
	if grep -q 'Failed to get the session' /tmp/installErr; then
		sleep 10
	else
		let COUNT=10
	fi
	let COUNT=COUNT+1
done
