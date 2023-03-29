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

import com.timecho.aweseme.thrift.IDNodeRPCService;
import com.timecho.awesome.conf.DNodeConfig;
import com.timecho.awesome.conf.DNodeDescriptor;
import com.timecho.awesome.conf.NodeConstant;
import com.timecho.awesome.conf.ServiceType;

public class DNodeRPCService extends ThriftService implements DNodeRPCServiceMBean {

  private static final DNodeConfig CONF = DNodeDescriptor.getInstance().getConf();

  private final DNodeRPCServiceProcessor dnProcessor;

  public DNodeRPCService() {
    this.dnProcessor = new DNodeRPCServiceProcessor();
    super.mbeanName =
      String.format(
        "%s:%s=%s", this.getClass().getPackage(), NodeConstant.JMX_TYPE, getID().getJmxName());
    super.initSyncServiceImpl();
  }

  @Override
  public ServiceType getID() {
    return ServiceType.DNODE_SERVICE;
  }

  @Override
  public void initTProcessor() {
    processor = new IDNodeRPCService.Processor<>(dnProcessor);
  }

  @Override
  public void initThriftServiceThread() {
    thriftServiceThread =
      new ThriftServiceThread(
        processor,
        getID().getName(),
        ServiceType.DNODE_SERVICE.getName(),
        getBindIP(),
        getBindPort(),
        CONF.getDnMinWorkerThreadNum(),
        CONF.getDnMaxWorkerThreadNum(),
        NodeConstant.THRIFT_SERVER_AWAIT_TIME_FOR_STOP_SERVICE,
        new DNodeRPCServiceHandler(),
        NodeConstant.IS_ENABLE_THRIFT_COMPRESSION);
    thriftServiceThread.setName(ServiceType.DNODE_SERVICE.getName());
  }

  @Override
  public String getBindIP() {
    return CONF.getDnRpcAddress();
  }

  @Override
  public int getBindPort() {
    return CONF.getDnRpcPort();
  }
}
