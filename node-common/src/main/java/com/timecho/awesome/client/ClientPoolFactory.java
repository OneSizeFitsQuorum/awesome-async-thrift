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

package com.timecho.awesome.client;

import com.timecho.aweseme.thrift.TEndPoint;
import com.timecho.awesome.client.async.AsyncDNodeServiceClient;
import com.timecho.awesome.client.property.ClientPoolProperty;
import com.timecho.awesome.client.property.ThriftClientProperty;
import com.timecho.awesome.client.sync.SyncCNodeServiceClient;
import com.timecho.awesome.concurrent.ThreadName;
import com.timecho.awesome.conf.NodeConstant;
import com.timecho.awesome.conf.ClientPoolConfig;
import com.timecho.awesome.conf.ClientPoolDescriptor;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

public class ClientPoolFactory {

  private static final ClientPoolConfig CONF = ClientPoolDescriptor.getInstance().getConf();

  public static class SyncCNodeServiceClientPoolFactory
    implements IClientPoolFactory<TEndPoint, SyncCNodeServiceClient> {

    @Override
    public KeyedObjectPool<TEndPoint, SyncCNodeServiceClient> createClientPool(
      ClientManager<TEndPoint, SyncCNodeServiceClient> manager) {
      return new GenericKeyedObjectPool<>(
        new SyncCNodeServiceClient.Factory(
          manager,
          new ThriftClientProperty.Builder()
            .setConnectionTimeoutMs(NodeConstant.CONNECTION_TIMEOUT_IN_MS)
            .setRpcThriftCompressionEnabled(NodeConstant.IS_ENABLE_THRIFT_COMPRESSION)
            .build()),
        new ClientPoolProperty.Builder<SyncCNodeServiceClient>()
          .setCoreClientNumForEachNode(CONF.getCoreClientNumForEachNode())
          .setMaxClientNumForEachNode(CONF.getMaxClientNumForEachNode())
          .build()
          .getConfig());
    }
  }

  public static class AsyncDNodeServiceClientPoolFactory
    implements IClientPoolFactory<TEndPoint, AsyncDNodeServiceClient> {

    @Override
    public KeyedObjectPool<TEndPoint, AsyncDNodeServiceClient> createClientPool(
      ClientManager<TEndPoint, AsyncDNodeServiceClient> manager) {
      return new GenericKeyedObjectPool<>(
        new AsyncDNodeServiceClient.Factory(
          manager,
          new ThriftClientProperty.Builder()
            .setConnectionTimeoutMs(NodeConstant.CONNECTION_TIMEOUT_IN_MS)
            .setRpcThriftCompressionEnabled(NodeConstant.IS_ENABLE_THRIFT_COMPRESSION)
            .setSelectorNumOfAsyncClientManager(CONF.getAsyncSelectorNumOfClientManager())
            .build(),
          ThreadName.ASYNC_DNODE_CLIENT_POOL.getName()),
        new ClientPoolProperty.Builder<AsyncDNodeServiceClient>()
          .setCoreClientNumForEachNode(CONF.getCoreClientNumForEachNode())
          .setMaxClientNumForEachNode(CONF.getMaxClientNumForEachNode())
          .build()
          .getConfig());
    }
  }
}
