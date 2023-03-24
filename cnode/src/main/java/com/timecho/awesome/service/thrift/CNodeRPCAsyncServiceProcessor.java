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
import com.timecho.awesome.client.AsyncDNodeClientPool;
import org.apache.thrift.async.AsyncMethodCallback;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CNodeRPCAsyncServiceProcessor implements ICNodeRPCService.AsyncIface {

  private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);

  @Override
  public void cpuRequest(long n, AsyncMethodCallback<Long> resultHandler) {
    CompletableFuture<Long> cpuFuture = CompletableFuture.supplyAsync(() -> {
      long z = 0;
      for (int i = 0; i < n; i++) {
        z += i;
      }
      return z;
    }, EXECUTOR);
    cpuFuture.thenAccept(resultHandler::onComplete);
  }

  @Override
  public void ioRequest(AsyncMethodCallback<Boolean> resultHandler) {
    CompletableFuture<Boolean> ioFuture = CompletableFuture.supplyAsync(() -> {
      AsyncDNodeClientPool.getInstance().processIORequest();
      return true;
    });
    ioFuture.thenAccept(resultHandler::onComplete);
  }

  public void handleClientExit() {}
}
