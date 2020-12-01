#!/bin/bash

###
# ============LICENSE_START=======================================================
# openECOMP : SDN-C
# ================================================================================
# Copyright (C) 2017 AT&T Intellectual Property. All rights
# 			reserved.
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

PROPERTY_DIR=${PROPERTY_DIR:-/opt/onap/ccsdk/data/properties}

LISTENER=dmaap-listener



PIDFILE=/tmp/.${LISTENER}-pid
UEBLISTENERROOT=${UEBLISTENERROOT:-/opt/onap/dmaap-listener}
JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-8-oracle}
JAVA_OPTS=${JAVA_OPTS:--Dhttps.protocols=TLSv1.1,TLSv1.2}
JAVA=${JAVA:-${JAVA_HOME}/bin/java}

# Redirect output from script to $LISTENER.out
exec >> ${UEBLISTENERROOT}/logs/$LISTENER.out
exec 2>&1

if [ -f $PIDFILE ]
then
  pid=$(cat $PIDFILE)
  if [ "$pid" != "" ]
  then
    if kill -0 $pid
    then
      echo "$LISTENER already running"
      exit 0
    fi
  fi
fi

if [ ! -d ${UEBLISTENERROOT}/logs ]
then
  mkdir ${UEBLISTENERROOT}/logs
fi

for file in ${UEBLISTENERROOT}/lib/*.jar
do
  LISTENERCLASSPATH=$LISTENERCLASSPATH:$file
done

${JAVA} ${JAVA_OPTS} -Dlog4j.configuration=file:${UEBLISTENERROOT}/lib/log4j.properties -cp ${LISTENERCLASSPATH} org.onap.ccsdk.sli.northbound.dmaapclient.DmaapListener &


echo $! > $PIDFILE

echo "$LISTENER started!"
exit 0
