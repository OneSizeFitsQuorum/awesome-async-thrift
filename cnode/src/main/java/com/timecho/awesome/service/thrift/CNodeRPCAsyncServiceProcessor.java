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
import com.timecho.aweseme.thrift.TEndPoint;
import com.timecho.awesome.client.AsyncDNodeClientManager;
import com.timecho.awesome.client.IOProcessHandler;
import com.timecho.awesome.conf.CNodeDescriptor;
import com.timecho.awesome.service.CNode;
import org.apache.thrift.async.AsyncMethodCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

public class CNodeRPCAsyncServiceProcessor implements ICNodeRPCService.AsyncIface {

  private static final Logger LOGGER = LoggerFactory.getLogger(CNodeRPCAsyncServiceProcessor.class);

  private static final List<TEndPoint> WORKERS = CNodeDescriptor.getInstance().getConf().getWorkerDnList();

  private ThreadPoolExecutor executorService;

  public void setExecutorService(ThreadPoolExecutor executorService) {
    this.executorService = executorService;
  }

  @Override
  public void cpuRequest(long n, AsyncMethodCallback<Long> resultHandler) {
    CompletableFuture<Long> cpuFuture = CompletableFuture.supplyAsync(() -> {
      long z = 1;
      for (int i = 0; i < n; i++) {
        z *= i;
      }
      return z;
    }, executorService);
    cpuFuture.thenAccept(resultHandler::onComplete);
  }

  @Override
  public void ioRequest(AsyncMethodCallback<Boolean> resultHandler) {
    List<CompletableFuture<Void>> ioFutures = new ArrayList<>();
    for (TEndPoint worker : WORKERS) {
      CompletableFuture<Void> ioCallback = new CompletableFuture<>();
      IOProcessHandler ioProcessHandler = new IOProcessHandler(ioCallback);
      CompletableFuture.runAsync(() ->
        AsyncDNodeClientManager.getInstance().processIORequest(worker, ioProcessHandler), executorService);
      ioFutures.add(ioCallback);
    }

    CompletableFuture<Void> allFutures = CompletableFuture.allOf(ioFutures.toArray(new CompletableFuture[0]));
    allFutures.thenRun(() -> resultHandler.onComplete(true));
  }

  @Override
  public void commitDNode(AsyncMethodCallback<Boolean> resultHandler) {
    CompletableFuture<Void> commitFuture =
      CompletableFuture.runAsync(() -> CNode.getInstance().commitDNode(), executorService);
    commitFuture.thenRun(() -> resultHandler.onComplete(true));
  }

  public void handleClientExit() {}
}
