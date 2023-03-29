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
import com.timecho.awesome.conf.ServiceType;
import org.apache.thrift.TBaseAsyncProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class CNodeRPCService extends ThriftService implements CNodeRPCServiceMBean {

  private final static CNodeConfig CONF = CNodeDescriptor.getInstance().getConf();

  private static final CNodeServerType serverType = CONF.getCnServerType();
  private final Object cnProcessor;

  public CNodeRPCService() {
    super.mbeanName =
      String.format(
        "%s:%s=%s", this.getClass().getPackage(), NodeConstant.JMX_TYPE, getID().getJmxName());

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
  public ServiceType getID() {
    return ServiceType.CNODE_SERVICE;
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
            ServiceType.CNODE_SERVICE.getName(),
            getBindIP(),
            getBindPort(),
            CONF.getCnMinWorkerThreadNum(),
            CONF.getCnMaxWorkerThreadNum(),
            NodeConstant.THRIFT_SERVER_AWAIT_TIME_FOR_STOP_SERVICE,
            new CNodeRPCSyncServiceHandler(),
            NodeConstant.IS_ENABLE_THRIFT_COMPRESSION);
        break;
      case ASYNC:
      default:
        thriftServiceThread =
          new ThriftServiceThread(
            (TBaseAsyncProcessor<?>) processor,
            getID().getName(),
            ServiceType.CNODE_SERVICE.getName(),
            getBindIP(),
            getBindPort(),
            CONF.getCnAsyncServiceSelectorNum(),
            CONF.getCnMinWorkerThreadNum(),
            CONF.getCnMaxWorkerThreadNum(),
            NodeConstant.THRIFT_SERVER_AWAIT_TIME_FOR_STOP_SERVICE,
            new CNodeRPCAsyncServiceHandler((CNodeRPCAsyncServiceProcessor) cnProcessor),
            NodeConstant.IS_ENABLE_THRIFT_COMPRESSION,
            NodeConstant.CONNECTION_TIMEOUT_IN_MS,
            NodeConstant.THRIFT_FRAME_MAX_SIZE);
        ((CNodeRPCAsyncServiceProcessor) cnProcessor)
          .setExecutorService((ThreadPoolExecutor) this.thriftServiceThread.getExecutorService());
        break;
    }

    thriftServiceThread.setName(ServiceType.CNODE_SERVICE.getName());
  }

  public ExecutorService getExecutorService() {
    return this.thriftServiceThread.getExecutorService();
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
