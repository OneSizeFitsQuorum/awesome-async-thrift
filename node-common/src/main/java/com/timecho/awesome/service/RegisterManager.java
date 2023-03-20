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

package com.timecho.awesome.service;

import com.timecho.awesome.thrift.IService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegisterManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterManager.class);
  private final List<IService> iServices;

  public RegisterManager() {
    iServices = new ArrayList<>();
  }

  /** Register service. */
  public void register(IService service) {
    for (IService s : iServices) {
      if (s.getID() == service.getID()) {
        LOGGER.debug("{} has already been registered. skip", service.getID().getName());
        return;
      }
    }
    iServices.add(service);
    service.start();
  }

  /** Stop all service and clear iService list. */
  public void deregisterAll() {
    // we stop JMXServer at last
    Collections.reverse(iServices);
    for (IService service : iServices) {
      try {
        service.stop();
        LOGGER.debug("{} deregistered", service.getID());
      } catch (Exception e) {
        LOGGER.error("Failed to stop {} because:", service.getID().getName(), e);
      }
    }
    iServices.clear();
    LOGGER.info("Deregister all service.");
  }
}
