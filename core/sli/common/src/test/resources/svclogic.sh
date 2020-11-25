#!/bin/bash

###
# ============LICENSE_START=======================================================
# ONAP : CCSDK
# ================================================================================
# Copyright (C) 2017 AT&T Intellectual Property. All rights
# 						reserved.
# ================================================================================
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
# ============LICENSE_END=========================================================
###

MYSQL_JDBC_DRIVER=${MYSQL_JDBC_DRIVER:-/home/ubuntu/mysql-connector-java-5.1.38.1.jar}
SLI_COMMON_TARGETDIR=${SLI_COMMON_TARGETDIR:-/home/ubuntu/opendaylight/plugins}
#SLI_COMMON_TARGETDIR=${SLI_COMMON_TARGETDIR:-/home/ubuntu/git/sdnctl/sli/common/target}
SLI_VERSION=${SLI_VERSION:-1.1.0-SNAPSHOT}
SLI_COMMON_JAR=${SLI_COMMON_JAR:=${SLI_COMMON_TARGETDIR}/sli-common-${SLI_VERSION}.jar}

echo SLI_COMMON_JAR is $SLI_COMMON_JAR

java -cp ${CLASSPATH}:${MYSQL_JDBC_DRIVER}:${SLI_COMMON_JAR} org.onap.ccsdk.sli.core.sli.SvcLogicParser $*
