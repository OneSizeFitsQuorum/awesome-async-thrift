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

public enum RequestType {
  IO("IO"),
  CPU("CPU");

  private final String requestType;

  RequestType(String requestType) {
    this.requestType = requestType;
  }

  public String getRequestType() {
    return requestType;
  }

  public static RequestType parse(String type) throws ConfigurationException {
    for (RequestType requestType : RequestType.values()) {
      if (type.equals(requestType.getRequestType())) {
        return requestType;
      }
    }
    throw new ConfigurationException(String.format("RequestType %s doesn't exist.", type));
  }
}
