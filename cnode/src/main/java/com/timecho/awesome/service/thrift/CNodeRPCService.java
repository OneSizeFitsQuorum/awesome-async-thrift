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

package com.timecho.awesome.service.thrift;

import com.timecho.aweseme.thrift.ICNodeRPCService;
import com.timecho.awesome.conf.CNodeConfig;
import com.timecho.awesome.conf.CNodeDescriptor;
import com.timecho.awesome.conf.CNodeServerType;
import com.timecho.awesome.conf.NodeConstant;
import com.timecho.awesome.service.JMXService;
import org.apache.thrift.TBaseAsyncProcessor;

public class CNodeRPCService extends ThriftService implements CNodeRPCServiceMBean {

  private final static CNodeConfig CONF = CNodeDescriptor.getInstance().getConf();

  private final CNodeServerType serverType;
  private final Object cnProcessor;

  public CNodeRPCService(CNodeServerType serverType) {
    super.mbeanName =
      String.format(
        "%s:%s=%s", this.getClass().getPackage(), NodeConstant.JMX_TYPE, getID().getJmxName());

    this.serverType = serverType;
    switch (serverType) {
      case SYNC:
        this.cnProcessor = new CNodeRPCSyncServiceProcessor();
        super.initSyncServiceImpl();
        break;
      case ASYNC:
      default:
        this.cnProcessor = new CNodeRPCAsyncServiceProcessor();
        super.initAsyncServiceImpl();
        break;
    }
  }


  @Override
  public JMXService.ServiceType getID() {
    return JMXService.ServiceType.CNODE_SERVICE;
  }

  @Override
  public void initTProcessor() {
    switch (serverType) {
      case SYNC:
        this.processor = new ICNodeRPCService.Processor<>((ICNodeRPCService.Iface) cnProcessor);
        break;
      case ASYNC:
      default:
        this.processor = new ICNodeRPCService.AsyncProcessor<>((ICNodeRPCService.AsyncIface) cnProcessor);
        break;
    }
  }

  @Override
  public void initThriftServiceThread() {
    switch (serverType) {
      case SYNC:
        thriftServiceThread =
          new ThriftServiceThread(
            processor,
            getID().getName(),
            JMXService.ServiceType.CNODE_SERVICE.getName(),
            getBindIP(),
            getBindPort(),
            CONF.getCnMaxConcurrentClientNum(),
            CONF.getCnThriftServerAwaitTimeForStopService(),
            new CNodeRPCSyncServiceHandler(),
            CONF.isCnRpcThriftCompressionEnabled());
        break;
      case ASYNC:
      default:
        thriftServiceThread =
          new ThriftServiceThread(
            (TBaseAsyncProcessor<?>) processor,
            getID().getName(),
            JMXService.ServiceType.CNODE_SERVICE.getName(),
            getBindIP(),
            getBindPort(),
            CONF.getCnSelectorNum(),
            CONF.getCnMinConcurrentClientNum(),
            CONF.getCnMaxConcurrentClientNum(),
            CONF.getCnThriftServerAwaitTimeForStopService(),
            new CNodeRPCAsyncServiceHandler((CNodeRPCAsyncServiceProcessor) cnProcessor),
            NodeConstant.ENABLE_THRIFT_COMPRESSION,
            NodeConstant.CONNECTION_TIMEOUT_IN_MS,
            NodeConstant.THRIFT_FRAME_MAX_SIZE);
        break;
    }

    thriftServiceThread.setName(JMXService.ServiceType.CNODE_SERVICE.getName());
  }

  @Override
  public String getBindIP() {
    return CONF.getCnRpcAddress();
  }

  @Override
  public int getBindPort() {
    return CONF.getCnRpcPort();
  }
}