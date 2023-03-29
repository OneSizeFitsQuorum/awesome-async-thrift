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

import com.timecho.awesome.concurrent.ThreadPoolFactory;
import com.timecho.awesome.exception.RPCServiceException;
import org.apache.thrift.TBaseAsyncProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractThriftServiceThread extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractThriftServiceThread.class);
  private TServerTransport serverTransport;
  private TServer poolServer;
  private CountDownLatch threadStopLatch;

  private ExecutorService executorService;

  private final String serviceName;

  private TProtocolFactory protocolFactory;

  // currently, we can reuse the ProtocolFactory instance.
  private static final TCompactProtocol.Factory compactProtocolFactory = new TCompactProtocol.Factory();
  private static final TBinaryProtocol.Factory binaryProtocolFactory = new TBinaryProtocol.Factory();

  private void initProtocolFactory(boolean compress) {
    protocolFactory = getProtocolFactory(compress);
  }

  public abstract TTransportFactory getTTransportFactory();

  public static TProtocolFactory getProtocolFactory(boolean compress) {
    if (compress) {
      return compactProtocolFactory;
    } else {
      return binaryProtocolFactory;
    }
  }

  /** For async ThriftService. */
  protected AbstractThriftServiceThread(
    TBaseAsyncProcessor<?> processor,
    String serviceName,
    String threadsName,
    String bindAddress,
    int port,
    int selectorThreads,
    int minWorkerThreads,
    int maxWorkerThreads,
    int timeoutSecond,
    TServerEventHandler serverEventHandler,
    boolean compress,
    int connectionTimeoutInMS,
    int maxReadBufferBytes) {
    initProtocolFactory(compress);
    this.serviceName = serviceName;
    try {
      serverTransport = openNonblockingTransport(bindAddress, port, connectionTimeoutInMS);
      TThreadedSelectorServer.Args poolArgs =
        initAsyncedSelectorPoolArgs(
          processor,
          threadsName,
          selectorThreads,
          minWorkerThreads,
          maxWorkerThreads,
          timeoutSecond,
          maxReadBufferBytes);
      poolServer = new TThreadedSelectorServer(poolArgs);
      poolServer.setServerEventHandler(serverEventHandler);
    } catch (TTransportException e) {
      catchFailedInitialization(e);
    }
  }

  private TThreadedSelectorServer.Args initAsyncedSelectorPoolArgs(
    TBaseAsyncProcessor<?> processor,
    String threadsName,
    int selectorThreads,
    int minWorkerThreads,
    int maxWorkerThreads,
    int timeoutSecond,
    int maxReadBufferBytes) {
    TThreadedSelectorServer.Args poolArgs =
      new TThreadedSelectorServer.Args((TNonblockingServerTransport) serverTransport);
    poolArgs.maxReadBufferBytes = maxReadBufferBytes;
    poolArgs.selectorThreads(selectorThreads);
    executorService =
      ThreadPoolFactory.createThriftRpcClientThreadPool(
        minWorkerThreads, maxWorkerThreads, timeoutSecond, TimeUnit.SECONDS, threadsName);
    poolArgs.executorService(executorService);
    poolArgs.processor(processor);
    poolArgs.protocolFactory(protocolFactory);
    poolArgs.transportFactory(getTTransportFactory());
    return poolArgs;
  }

  /** For sync ThriftServiceThread */
  protected AbstractThriftServiceThread(
    TProcessor processor,
    String serviceName,
    String threadsName,
    String bindAddress,
    int port,
    int minWorkerThreads,
    int maxWorkerThreads,
    int timeoutSecond,
    TServerEventHandler serverEventHandler,
    boolean compress) {
    initProtocolFactory(compress);
    this.serviceName = serviceName;

    try {
      serverTransport = openTransport(bindAddress, port);
      TThreadPoolServer.Args poolArgs =
        initSyncedPoolArgs(processor, threadsName, minWorkerThreads, maxWorkerThreads, timeoutSecond);
      poolServer = new TThreadPoolServer(poolArgs);
      poolServer.setServerEventHandler(serverEventHandler);
    } catch (TTransportException e) {
      catchFailedInitialization(e);
    }
  }

  private TThreadPoolServer.Args initSyncedPoolArgs(
    TProcessor processor, String threadsName, int minWorkerThreads, int maxWorkerThreads, int timeoutSecond) {
    TThreadPoolServer.Args poolArgs = new TThreadPoolServer.Args(serverTransport);
    poolArgs
      .minWorkerThreads(minWorkerThreads)
      .maxWorkerThreads(maxWorkerThreads)
      .stopTimeoutVal(timeoutSecond);
    executorService = ThreadPoolFactory.createThriftRpcClientThreadPool(poolArgs, threadsName);
    poolArgs.executorService = executorService;
    poolArgs.processor(processor);
    poolArgs.protocolFactory(protocolFactory);
    poolArgs.transportFactory(getTTransportFactory());
    return poolArgs;
  }

  private TServerTransport openTransport(String bindAddress, int port) throws TTransportException {
    return new TServerSocket(new InetSocketAddress(bindAddress, port));
  }

  private TServerTransport openNonblockingTransport(
    String bindAddress, int port, int connectionTimeoutInMS) throws TTransportException {
    return new TNonblockingServerSocket(
      new InetSocketAddress(bindAddress, port), connectionTimeoutInMS);
  }

  public void setThreadStopLatch(CountDownLatch threadStopLatch) {
    this.threadStopLatch = threadStopLatch;
  }

  @Override
  public void run() {
    LOGGER.info("The {} service thread begin to run...", serviceName);
    try {
      poolServer.serve();
    } catch (Exception e) {
      throw new RPCServiceException(
        String.format("%s exit, because ", serviceName), e);
    } finally {
      close();
      if (threadStopLatch == null) {
        LOGGER.debug("Stop Count Down latch is null");
      } else {
        LOGGER.debug("Stop Count Down latch is {}", threadStopLatch.getCount());
      }

      if (threadStopLatch != null && threadStopLatch.getCount() == 1) {
        threadStopLatch.countDown();
      }
      LOGGER.debug(
        "Close TThreadPoolServer and TServerSocket for {}",
        serviceName);
    }
  }

  private void catchFailedInitialization(TTransportException e) throws RPCServiceException {
    close();
    if (threadStopLatch == null) {
      LOGGER.debug("Stop Count Down latch is null");
    } else {
      LOGGER.debug("Stop Count Down latch is {}", threadStopLatch.getCount());
    }
    if (threadStopLatch != null && threadStopLatch.getCount() == 1) {
      threadStopLatch.countDown();
    }
    LOGGER.debug(
      "Close TThreadPoolServer and TServerSocket for {}",
      serviceName);
    throw new RPCServiceException(
      String.format(
        "Failed to start %s, because ", serviceName),
      e);
  }

  public synchronized void close() {
    if (poolServer != null) {
      poolServer.setShouldStop(true);
      poolServer.stop();

      poolServer = null;
    }
    if (serverTransport != null) {
      serverTransport.close();
      serverTransport = null;
    }
    if (threadStopLatch != null && threadStopLatch.getCount() == 1) {
      threadStopLatch.countDown();
    }
  }

  public boolean isServing() {
    if (poolServer != null) {
      return poolServer.isServing();
    }
    return false;
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }
}
