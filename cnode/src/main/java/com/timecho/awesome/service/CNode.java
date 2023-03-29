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

import com.timecho.awesome.client.AsyncDNodeClientManager;
import com.timecho.awesome.concurrent.threadpool.WrappedThreadPoolExecutor;
import com.timecho.awesome.conf.CNodeConfig;
import com.timecho.awesome.conf.NodeConstant;
import com.timecho.awesome.conf.CNodeDescriptor;
import com.timecho.awesome.conf.ServiceType;
import com.timecho.awesome.exception.StartupException;
import com.timecho.awesome.service.thrift.CNodeRPCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class CNode implements CNodeMBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(CNode.class);
  private final String mbeanName =
    String.format(
      "%s:%s=%s",
      this.getClass().getPackage(), NodeConstant.JMX_TYPE, NodeConstant.CNODE);

  private static final CNodeConfig CONF = CNodeDescriptor.getInstance().getConf();
  private static final long SYSTEM_START_TIME = System.currentTimeMillis();

  private final RegisterManager registerManager = new RegisterManager();
  private CNodeRPCService rpcService;
  private CNodeMonitor monitor;

  private final AtomicInteger committedDNodeNum = new AtomicInteger(0);

  public static void main(String[] args) {
    LOGGER.info("{} environmental variables: {}",
      NodeConstant.CNODE, CNodeDescriptor.getEnvironmentalVariables());
    LOGGER.info("Activating {}...", NodeConstant.CNODE);
    CNode.getInstance().activate();
  }

  private void activate() {
    try {
      setUpJMXService();
      setUpRPCService();
      setUpMonitorService();
    } catch (StartupException e) {
      LOGGER.error("Meet error when startup.", e);
      deactivate();
    }

    logTestConfigurations();
    AsyncDNodeClientManager.getInstance().activateClusterDNodes();
  }

  private void setUpJMXService() throws StartupException {
    registerManager.register(new JMXService());
    JMXService.registerMBean(this, mbeanName);
    LOGGER.info("Successfully setup {}.", ServiceType.JMX_SERVICE.getName());
  }

  private void setUpRPCService() throws StartupException {
    rpcService = new CNodeRPCService();
    registerManager.register(rpcService);
    LOGGER.info("Successfully setup {}.", ServiceType.CNODE_SERVICE.getName());
  }

  private void setUpMonitorService() throws StartupException {
    monitor = new CNodeMonitor(SYSTEM_START_TIME, (WrappedThreadPoolExecutor) rpcService.getExecutorService());
    registerManager.register(monitor);
    LOGGER.info("Successfully setup {}.", ServiceType.CNODE_MONITOR.getName());
  }

  private void logTestConfigurations() {
    LOGGER.info("This test will run in the following configurations:");
    LOGGER.info(String.format("\t %s: %s", NodeConstant.CN_SERVER_TYPE, CONF.getCnServerType()));
    LOGGER.info(String.format("\t %s: %s", NodeConstant.CN_ASYNC_SERVICE_SELECTOR_NUM, CONF.getCnAsyncServiceSelectorNum()));
    LOGGER.info(String.format("\t %s: %s", NodeConstant.REQUEST_TYPE, CONF.getRequestType()));
    LOGGER.info(String.format("\t %s: %s", NodeConstant.DN_CONCURRENT_CLIENT_NUM, CONF.getDnConcurrentClientNum()));
    LOGGER.info(String.format("\t %s: %s", NodeConstant.DN_REQUEST_NUM_PER_CLIENT, CONF.getDnRequestNumPerClient()));
  }


  public void commitDNode() {
    if (committedDNodeNum.incrementAndGet() == CONF.getWorkerDnList().size()) {
      LOGGER.info("All {} DNodes have committed.", committedDNodeNum.get());
      monitor.stop();
    } else {
      LOGGER.info("{} DNodes have committed.", committedDNodeNum.get());
    }
  }

  public void deactivate() {
    LOGGER.warn("Deactivating {}...", NodeConstant.CNODE);
    registerManager.deregisterAll();
    JMXService.deregisterMBean(mbeanName);
    LOGGER.info("{} is deactivated.", NodeConstant.CNODE);
    System.exit(-1);
  }

  private CNode() {
    // Empty constructor
  }

  private static class CNodeHolder {

    private static final CNode INSTANCE = new CNode();

    private CNodeHolder() {
      // Empty constructor
    }
  }

  public static CNode getInstance() {
    return CNodeHolder.INSTANCE;
  }
}