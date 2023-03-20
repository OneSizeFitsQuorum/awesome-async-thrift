/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.timecho.awesome.conf;

import com.timecho.awesome.exception.ConfigurationException;

public enum ServerType {
  ASYNC("ASYNC"),
  SYNC("SYNC");

  private final String serverType;

  ServerType(String serverType) {
    this.serverType = serverType;
  }

  public String getServerType() {
    return serverType;
  }

  public static ServerType parse(String type) throws ConfigurationException {
    for (ServerType serverType : ServerType.values()) {
      if (type.equals(serverType.getServerType())) {
        return serverType;
      }
    }
    throw new ConfigurationException(String.format("ServerType %s doesn't exist.", type));
  }
}
