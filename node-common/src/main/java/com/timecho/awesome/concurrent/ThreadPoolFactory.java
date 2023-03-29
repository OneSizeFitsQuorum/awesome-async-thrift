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

package com.timecho.awesome.concurrent;

import com.timecho.awesome.concurrent.threadpool.WrappedThreadPoolExecutor;
import org.apache.thrift.server.TThreadPoolServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class ThreadPoolFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolFactory.class);
  private static final String NEW_SYNCHRONOUS_QUEUE_THREAD_POOL_LOGGER_FORMAT =
    "new SynchronousQueue thread pool: {}";

  private ThreadPoolFactory() {
    // Empty constructor
  }

  /** Function for creating thrift rpc client thread pool. */
  public static ExecutorService createThriftRpcClientThreadPool(
    TThreadPoolServer.Args args, String poolName) {
    LOGGER.info(NEW_SYNCHRONOUS_QUEUE_THREAD_POOL_LOGGER_FORMAT, poolName);
    SynchronousQueue<Runnable> executorQueue = new SynchronousQueue<>();
    return new WrappedThreadPoolExecutor(
      args.minWorkerThreads,
      args.maxWorkerThreads,
      args.stopTimeoutVal,
      args.stopTimeoutUnit,
      executorQueue,
      new IoTThreadFactory(poolName),
      poolName);
  }

  /** Function for creating thrift rpc client thread pool. */
  public static ExecutorService createThriftRpcClientThreadPool(
    int minWorkerThreads,
    int maxWorkerThreads,
    int stopTimeoutVal,
    TimeUnit stopTimeoutUnit,
    String poolName) {
    LOGGER.info(NEW_SYNCHRONOUS_QUEUE_THREAD_POOL_LOGGER_FORMAT, poolName);
    SynchronousQueue<Runnable> executorQueue = new SynchronousQueue<>();
    return new WrappedThreadPoolExecutor(
      minWorkerThreads,
      maxWorkerThreads,
      stopTimeoutVal,
      stopTimeoutUnit,
      executorQueue,
      new IoTThreadFactory(poolName),
      poolName);
  }
}