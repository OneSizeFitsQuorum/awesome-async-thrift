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
import com.timecho.awesome.client.CountDownLatchAsyncHandler;
import com.timecho.awesome.conf.CNodeDescriptor;
import com.timecho.awesome.service.CNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CNodeRPCSyncServiceProcessor implements ICNodeRPCService.Iface {

  private static final Logger LOGGER = LoggerFactory.getLogger(CNodeRPCSyncServiceProcessor.class);

  private static final List<TEndPoint> WORKERS = CNodeDescriptor.getInstance().getConf().getWorkerDnList();

  @Override
  public long cpuRequest(long n) {
    long z = 1;
    for (int i = 0; i < n; i++) {
      z *= i;
    }
    return z;
  }

  @Override
  public boolean ioRequest() {
    CountDownLatch latch = new CountDownLatch(WORKERS.size());
    for (TEndPoint worker : WORKERS) {
      AsyncDNodeClientManager.getInstance().processIORequest(worker, new CountDownLatchAsyncHandler(latch));
    }
    try {
      latch.await();
    } catch (InterruptedException e) {
      LOGGER.error("Interrupted while waiting for IO request to finish", e);
    }
    return true;
  }

  @Override
  public boolean commitDNode() {
    CNode.getInstance().commitDNode();
    return true;
  }
}
