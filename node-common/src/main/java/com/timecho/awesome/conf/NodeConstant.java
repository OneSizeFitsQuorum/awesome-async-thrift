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

package com.timecho.awesome.conf;

import java.util.concurrent.TimeUnit;

public class NodeConstant {

  public static final String CN_RPC_ADDRESS = "cn_rpc_address";
  public static final String CN_RPC_PORT = "cn_rpc_port";

  public static final String WORKER_DN_LIST = "worker_dn_list";

  public static final String CN_SERVER_TYPE = "cn_server_type";
  public static final String CN_ASYNC_SERVICE_SELECTOR_NUM = "cn_async_service_selector_num";
  public static final String REQUEST_TYPE = "request_type";
  public static final String DN_CONCURRENT_CLIENT_NUM = "dn_concurrent_client_num";
  public static final String DN_REQUEST_NUM_PER_CLIENT = "dn_request_num_per_client";

  public static final String CN_MIN_WORKER_THREAD_NUM = "cn_min_worker_thread_num";
  public static final String CN_MAX_WORKER_THREAD_NUM = "cn_max_worker_thread_num";

  public static final String CN_ASYNC_CLIENT_MANAGER_SELECTOR_NUM = "cn_async_client_manager_selector_num";
  public static final String CN_CORE_CLIENT_NUM_FOR_EACH_NODE = "cn_core_client_num_for_each_node";
  public static final String CN_MAX_CLIENT_NUM_FOR_EACH_NODE = "cn_max_client_num_for_each_node";

  public static final String DN_RPC_ADDRESS = "dn_rpc_address";
  public static final String DN_RPC_PORT = "dn_rpc_port";

  public static final String DN_MIN_WORKER_THREAD_NUM = "dn_min_worker_thread_num";
  public static final String DN_MAX_WORKER_THREAD_NUM = "dn_max_worker_thread_num";

  public static final String DN_ASYNC_CLIENT_MANAGER_SELECTOR_NUM = "dn_async_client_manager_selector_num";
  public static final String DN_CORE_CLIENT_NUM_FOR_EACH_NODE = "dn_core_client_num_for_each_node";
  public static final String DN_MAX_CLIENT_NUM_FOR_EACH_NODE = "dn_max_client_num_for_each_node";

  public static final String CNODE = "CNode";
  public static final String DNODE = "DNode";

  public static final String CNODE_HOME = "CNODE_HOME";
  public static final String CNODE_CONF = "CNODE_CONF";
  public static final String CNODE_CONFIG_FILE_NAME = "cnode.properties";

  public static final String DNODE_HOME = "DNODE_HOME";
  public static final String DNODE_CONF = "DNODE_CONF";
  public static final String DNODE_CONFIG_FILE_NAME = "dnode.properties";

  public static final String LOGBACK_FILE = "logback.configurationFile";

  public static final int CONNECTION_TIMEOUT_IN_MS = (int) TimeUnit.SECONDS.toMillis(20);
  public static final boolean IS_ENABLE_THRIFT_COMPRESSION = false;
  public static final int THRIFT_SERVER_AWAIT_TIME_FOR_STOP_SERVICE = 60;
  public static final int THRIFT_DEFAULT_BUF_CAPACITY = 1024;
  public static final int THRIFT_FRAME_MAX_SIZE = 536870912;
  public static final int MAX_BUFFER_OVERSIZE_TIME = 5;
  public static final long MIN_SHRINK_INTERVAL = 60_000L;

  public static final String JMX_TYPE = "type";
  public static final String JMX_PORT = "com.sun.management.jmxremote.port";
}
