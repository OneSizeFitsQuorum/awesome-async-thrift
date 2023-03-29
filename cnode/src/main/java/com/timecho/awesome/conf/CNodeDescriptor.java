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

import com.timecho.awesome.exception.BadNodeUrlException;
import com.timecho.awesome.exception.ConfigurationException;
import com.timecho.awesome.utils.NodeUrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class CNodeDescriptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CNodeDescriptor.class);

  private static final CNodeConfig CONF = new CNodeConfig();
  private static final ClientPoolConfig CLIENT_POOL_CONFIG = ClientPoolDescriptor.getInstance().getConf();

  private void loadProperties() {
    URL propertiesUrl;
    String urlString = System.getenv(NodeConstant.CNODE_CONF) +
      File.separatorChar + NodeConstant.CNODE_CONFIG_FILE_NAME;
    try {
      propertiesUrl = new URL("file:" + urlString);
    } catch (MalformedURLException e) {
      LOGGER.error("Can't find config file: {}, use default configuration.", urlString);
      return;
    }

    try (InputStream inputStream = propertiesUrl.openStream()) {
      LOGGER.info("Start to read config file: {}", urlString);
      Properties properties = new Properties();
      properties.load(inputStream);

      CONF.setCnRpcAddress(
        properties
          .getProperty(NodeConstant.CN_RPC_ADDRESS, CONF.getCnRpcAddress())
          .trim());

      CONF.setCnRpcPort(
        Integer.parseInt(
          properties
            .getProperty(
              NodeConstant.CN_RPC_PORT, String.valueOf(CONF.getCnRpcPort()))
            .trim()));

      String dnUrls = properties.getProperty(NodeConstant.WORKER_DN_LIST);
      if (dnUrls != null) {
        try {
          CONF.setWorkerDnList(NodeUrlUtils.parseTEndPointUrls(dnUrls));
        } catch (BadNodeUrlException e) {
          throw new ConfigurationException(e.getMessage());
        }
      }

      CONF.setCnServerType(
        CNodeServerType.parse(
          properties
            .getProperty(NodeConstant.CN_SERVER_TYPE, CONF.getCnServerType().getServerType())
            .trim()));

      CONF.setCnAsyncServiceSelectorNum(
        Integer.parseInt(
          properties
            .getProperty(NodeConstant.CN_ASYNC_SERVICE_SELECTOR_NUM, String.valueOf(CONF.getCnAsyncServiceSelectorNum()))
            .trim()));

      CONF.setRequestType(
        RequestType.parse(
          properties
            .getProperty(NodeConstant.REQUEST_TYPE, CONF.getRequestType().getRequestType())
            .trim()));

      CONF.setDnConcurrentClientNum(
        Integer.parseInt(
          properties
            .getProperty(NodeConstant.DN_CONCURRENT_CLIENT_NUM, String.valueOf(CONF.getDnConcurrentClientNum()))
            .trim()));

      CONF.setDnRequestNumPerClient(
        Integer.parseInt(
          properties
            .getProperty(NodeConstant.DN_REQUEST_NUM_PER_CLIENT, String.valueOf(CONF.getDnRequestNumPerClient()))
            .trim()));

      CONF.setCnMinWorkerThreadNum(
        Integer.parseInt(
          properties
            .getProperty(NodeConstant.CN_MIN_WORKER_THREAD_NUM, String.valueOf(CONF.getCnMinWorkerThreadNum()))
            .trim()));

      CONF.setCnMaxWorkerThreadNum(
        Integer.parseInt(
          properties
            .getProperty(NodeConstant.CN_MAX_WORKER_THREAD_NUM, String.valueOf(CONF.getCnMaxWorkerThreadNum()))
            .trim()));

      CONF.setCnAsyncClientManagerSelectorNum(
        Integer.parseInt(
          properties
            .getProperty(
              NodeConstant.CN_ASYNC_CLIENT_MANAGER_SELECTOR_NUM,
              String.valueOf(CONF.getCnAsyncClientManagerSelectorNum()))
            .trim()));

      CONF.setCnCoreClientNumForEachNode(
        Integer.parseInt(
          properties
            .getProperty(
              NodeConstant.CN_CORE_CLIENT_NUM_FOR_EACH_NODE,
              String.valueOf(CONF.getCnCoreClientNumForEachNode()))
            .trim()));

      CONF.setCnMaxClientNumForEachNode(
        Integer.parseInt(
          properties
            .getProperty(
              NodeConstant.CN_MAX_CLIENT_NUM_FOR_EACH_NODE,
              String.valueOf(CONF.getCnMaxClientNumForEachNode()))
            .trim()));

    } catch (IOException | ConfigurationException e) {
      LOGGER.warn("Error occurs when loading config file, use default config.", e);
    }

    loadClientPoolConfig();
  }

  private void loadClientPoolConfig() {
    CLIENT_POOL_CONFIG.setAsyncSelectorNumOfClientManager(CONF.getCnAsyncClientManagerSelectorNum());
    CLIENT_POOL_CONFIG.setCoreClientNumForEachNode(CONF.getCnCoreClientNumForEachNode());
    CLIENT_POOL_CONFIG.setMaxClientNumForEachNode(CONF.getCnMaxClientNumForEachNode());
  }

  public static String getEnvironmentalVariables() {
    return "\n\t"
      + NodeConstant.CNODE_HOME
      + "="
      + System.getProperty(NodeConstant.CNODE_HOME, "null")
      + ";"
      + "\n\t"
      + NodeConstant.CNODE_CONF
      + "="
      + System.getProperty(NodeConstant.CNODE_CONF, "null")
      + ";"
      + "\n\t"
      + NodeConstant.LOGBACK_FILE
      + "="
      + System.getProperty(NodeConstant.LOGBACK_FILE, "null");
  }

  public CNodeConfig getConf() {
    return CONF;
  }

  private CNodeDescriptor() {
    loadProperties();
  }

  public static CNodeDescriptor getInstance() {
    return CNodeDescriptorHolder.INSTANCE;
  }

  private static class CNodeDescriptorHolder {

    private static final CNodeDescriptor INSTANCE = new CNodeDescriptor();

    private CNodeDescriptorHolder() {
      // Empty constructor
    }
  }
}
