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

PROPERTY_DIR=${PROPERTY_DIR:-/opt/sdnc/data/properties}

if [ -f ${PROPERTY_DIR}/dmaap-listener.properties ]
then
	LISTENER=dmaap-listener
else
	LISTENER=ueb-listener
fi

PIDFILE=/tmp/.${LISTENER}-pid
UEBLISTENERROOT=${UEBLISTENERROOT:-/opt/app/ueb-listener}

if [ -f $PIDFILE ]
then
  pid=$(cat $PIDFILE)
  if [ "$pid" != "" ]
  then
    if kill -0 $pid
      then
  	echo "Stopping $LISTENER"
        kill $pid && rm $PIDFILE
        exit 0
      else
        echo "$LISTENER not running"
        exit 1
      fi
  else
      echo "$LISTENER not running"
      exit 1
  fi
fi


