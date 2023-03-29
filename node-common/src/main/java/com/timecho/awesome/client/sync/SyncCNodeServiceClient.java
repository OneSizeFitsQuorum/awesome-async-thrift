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

package com.timecho.awesome.client.sync;

import com.timecho.aweseme.thrift.ICNodeRPCService;
import com.timecho.aweseme.thrift.TEndPoint;
import com.timecho.awesome.client.ClientManager;
import com.timecho.awesome.client.ThriftClient;
import com.timecho.awesome.client.factory.ThriftClientFactory;
import com.timecho.awesome.client.property.ThriftClientProperty;
import com.timecho.awesome.rpc.RpcTransportFactory;
import com.timecho.awesome.rpc.TConfigurationConst;
import com.timecho.awesome.rpc.TimeoutChangeableTransport;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import java.net.SocketException;

public class SyncCNodeServiceClient extends ICNodeRPCService.Client
  implements ThriftClient, AutoCloseable {

  private final boolean printLogWhenEncounterException;
  private final TEndPoint endpoint;
  private final ClientManager<TEndPoint, SyncCNodeServiceClient> clientManager;

  public SyncCNodeServiceClient(
    ThriftClientProperty property,
    TEndPoint endpoint,
    ClientManager<TEndPoint, SyncCNodeServiceClient> clientManager)
    throws TTransportException {
    super(
      property
        .getProtocolFactory()
        .getProtocol(
          RpcTransportFactory.INSTANCE.getTransport(
            new TSocket(
              TConfigurationConst.defaultTConfiguration,
              endpoint.getIp(),
              endpoint.getPort(),
              property.getConnectionTimeoutMs()))));
    this.printLogWhenEncounterException = property.isPrintLogWhenEncounterException();
    this.endpoint = endpoint;
    this.clientManager = clientManager;
    getInputProtocol().getTransport().open();
  }

  public int getTimeout() throws SocketException {
    return ((TimeoutChangeableTransport) getInputProtocol().getTransport()).getTimeOut();
  }

  public void setTimeout(int timeout) {
    // the same transport is used in both input and output
    ((TimeoutChangeableTransport) (getInputProtocol().getTransport())).setTimeout(timeout);
  }

  @Override
  public void close() {
    clientManager.returnClient(endpoint, this);
  }

  @Override
  public void invalidate() {
    getInputProtocol().getTransport().close();
  }

  @Override
  public void invalidateAll() {
    clientManager.clear(endpoint);
  }

  @Override
  public boolean printLogWhenEncounterException() {
    return printLogWhenEncounterException;
  }

  @Override
  public String toString() {
    return String.format("SyncDataNodeInternalServiceClient{%s}", endpoint);
  }

  public static class Factory
    extends ThriftClientFactory<TEndPoint, SyncCNodeServiceClient> {

    public Factory(
      ClientManager<TEndPoint, SyncCNodeServiceClient> clientManager,
      ThriftClientProperty thriftClientProperty) {
      super(clientManager, thriftClientProperty);
    }

    @Override
    public void destroyObject(
      TEndPoint endpoint, PooledObject<SyncCNodeServiceClient> pooledObject) {
      pooledObject.getObject().invalidate();
    }

    @Override
    public PooledObject<SyncCNodeServiceClient> makeObject(TEndPoint endpoint)
      throws Exception {
      return new DefaultPooledObject<>(
        SyncThriftClientWithErrorHandler.newErrorHandler(
          SyncCNodeServiceClient.class,
          SyncCNodeServiceClient.class.getConstructor(
            thriftClientProperty.getClass(), endpoint.getClass(), clientManager.getClass()),
          thriftClientProperty,
          endpoint,
          clientManager));
    }

    @Override
    public boolean validateObject(
      TEndPoint endpoint, PooledObject<SyncCNodeServiceClient> pooledObject) {
      return pooledObject.getObject().getInputProtocol().getTransport().isOpen();
    }
  }
}
