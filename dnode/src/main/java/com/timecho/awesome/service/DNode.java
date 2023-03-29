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

import com.timecho.awesome.conf.DNodeDescriptor;
import com.timecho.awesome.conf.NodeConstant;
import com.timecho.awesome.conf.ServiceType;
import com.timecho.awesome.exception.StartupException;
import com.timecho.awesome.service.thrift.DNodeRPCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DNode implements DNodeMBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(DNode.class);
  private final String mbeanName =
    String.format(
      "%s:%s=%s",
      this.getClass().getPackage(), NodeConstant.JMX_TYPE, NodeConstant.DNODE);

  private final RegisterManager registerManager = new RegisterManager();

  public static void main(String[] args) {
    LOGGER.info("{} environmental variables: {}",
      NodeConstant.DNODE, DNodeDescriptor.getEnvironmentalVariables());
    LOGGER.info("Activating {}...", NodeConstant.DNODE);
    DNode.getInstance().activate();
    LOGGER.info("{} is successfully started, waiting for CNode's schedule.", NodeConstant.DNODE);
  }

  private void activate() {
    try {
      setUpJMXService();
      setUpRPCService();
    } catch (StartupException e) {
      LOGGER.error("Meet error when startup.", e);
      deactivate();
    }
  }

  private void setUpJMXService() throws StartupException {
    registerManager.register(new JMXService());
    JMXService.registerMBean(this, mbeanName);
    LOGGER.info("Successfully setup {}.", ServiceType.JMX_SERVICE.getName());
  }

  private void setUpRPCService() throws StartupException {
    DNodeRPCService dNodeRPCService = new DNodeRPCService();
    registerManager.register(dNodeRPCService);
    LOGGER.info("Successfully setup {}.", ServiceType.DNODE_SERVICE.getName());
  }

  public void deactivate() {
    LOGGER.warn("Deactivating {}...", NodeConstant.DNODE);
    registerManager.deregisterAll();
    JMXService.deregisterMBean(mbeanName);
    LOGGER.info("{} is deactivated.", NodeConstant.DNODE);
    System.exit(-1);
  }

  private DNode() {
    // Empty constructor
  }

  private static class DNodeHolder {

    private static final DNode INSTANCE = new DNode();

    private DNodeHolder() {
      // Empty constructor
    }
  }

  public static DNode getInstance() {
    return DNodeHolder.INSTANCE;
  }
}