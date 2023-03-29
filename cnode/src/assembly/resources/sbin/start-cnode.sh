#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

echo ----------------------------
echo Starting CNode
echo ----------------------------

# find java in JAVA_HOME
if [ -n "$JAVA_HOME" ]; then
    for java in "$JAVA_HOME"/bin/amd64/java "$JAVA_HOME"/bin/java; do
        if [ -x "$java" ]; then
            JAVA="$java"
            break
        fi
    done
else
    JAVA=java
fi

if [ -z $JAVA ] ; then
    echo Unable to find java executable. Check JAVA_HOME and PATH environment variables.  > /dev/stderr
    exit 1;
fi

# Environmental variables
# shellcheck disable=SC2155
export CNODE_HOME="$(dirname "$0")/.."
export CNODE_CONF=${CNODE_HOME}/conf
export CNODE_LOG_DIR=${CNODE_HOME}/logs
export CNODE_LOG_CONFIG="${CNODE_CONF}/logback-cnode.xml"

mkdir -p "${CNODE_LOG_DIR}"

# ClassPath
CLASSPATH=""
for f in "${CNODE_HOME}"/lib/*.jar; do
  CLASSPATH=${CLASSPATH}":"$f
done
classname=com.timecho.awesome.service.CNode

launch_service() {
    class="$1"
    cnode_params="-DCNODE_HOME=${CNODE_HOME}"
  	cnode_params="$cnode_params -DCNODE_CONF=${CNODE_CONF}"
  	cnode_params="$cnode_params -DCNODE_LOG_DIR=${CNODE_LOG_DIR}"
  	cnode_params="$cnode_params -Dlogback.configurationFile=${CNODE_LOG_CONFIG}"

    exec $NUMACTL "$JAVA" $cnode_params -cp "$CLASSPATH" "$class"

  	return $?

}

# Start up the service
launch_service "$classname"

exit $?
