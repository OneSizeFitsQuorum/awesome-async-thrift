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
import com.timecho.awesome.client.sync.SyncCNodeServiceClient;
import com.timecho.awesome.conf.DNodeConfig;
import com.timecho.awesome.conf.DNodeDescriptor;
import com.timecho.awesome.exception.ClientManagerException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SyncCNodeClientManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncCNodeClientManager.class);

  private static final DNodeConfig CONF = DNodeDescriptor.getInstance().getConf();

  private final IClientManager<TEndPoint, SyncCNodeServiceClient> clientManager;

  private SyncCNodeClientManager() {
    clientManager =
      new IClientManager.Factory<TEndPoint, SyncCNodeServiceClient>()
        .createClientManager(
          new ClientPoolFactory.SyncCNodeServiceClientPoolFactory());
  }

  public void ioRequest() {
    final TEndPoint targetCNode = CONF.getTargetCNode();
    try (SyncCNodeServiceClient client = clientManager.borrowClient(targetCNode)) {
      client.ioRequest();
    } catch (TException | ClientManagerException e) {
      LOGGER.error("Error when executing ioRequest", e);
    }
  }

  public void cpuRequest() {
    final TEndPoint targetCNode = CONF.getTargetCNode();
    final int base = 9000_0000;
    final int offset = 1000_0000;
    try (SyncCNodeServiceClient client = clientManager.borrowClient(targetCNode)) {
      // A thread in the CNode will be occupied for about 1000 ms
      client.cpuRequest(base + new Random().nextInt(offset));
    } catch (TException | ClientManagerException e) {
      LOGGER.error("Error when executing ioRequest", e);
    }
  }

  public void commit() {
    final TEndPoint targetCNode = CONF.getTargetCNode();
    try (SyncCNodeServiceClient client = clientManager.borrowClient(targetCNode)) {
      client.commitDNode();
    } catch (TException | ClientManagerException e) {
      LOGGER.error("Error when executing commitDNode", e);
    }
  }

  private static class SyncCNodeClientPoolHolder {

    private static final SyncCNodeClientManager INSTANCE = new SyncCNodeClientManager();

    private SyncCNodeClientPoolHolder() {
      // Empty constructor
    }
  }

  public static SyncCNodeClientManager getInstance() {
    return SyncCNodeClientPoolHolder.INSTANCE;
  }
}
