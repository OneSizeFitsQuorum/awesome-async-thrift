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
echo Starting DNode
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
export DNODE_HOME="$(dirname "$0")/.."
export DNODE_CONF=${DNODE_HOME}/conf
export DNODE_LOG_DIR=${DNODE_HOME}/logs
export DNODE_LOG_CONFIG="${DNODE_CONF}/logback-dnode.xml"

mkdir -p "${DNODE_LOG_DIR}"

# ClassPath
CLASSPATH=""
for f in "${DNODE_HOME}"/lib/*.jar; do
  CLASSPATH=${CLASSPATH}":"$f
done
classname=com.timecho.awesome.service.DNode

launch_service() {
    class="$1"
    dnode_params="-DDNODE_HOME=${DNODE_HOME}"
  	dnode_params="$dnode_params -DDNODE_CONF=${DNODE_CONF}"
  	dnode_params="$dnode_params -DDNODE_LOG_DIR=${DNODE_LOG_DIR}"
  	dnode_params="$dnode_params -Dlogback.configurationFile=${DNODE_LOG_CONFIG}"

    exec $NUMACTL "$JAVA" $dnode_params -cp "$CLASSPATH" "$class"

  	return $?

}

# Start up the service
launch_service "$classname"

exit $?
