package com.timecho.awesome.conf;

import com.timecho.aweseme.thrift.TDNodeConfiguration;
import com.timecho.awesome.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class DNodeDescriptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DNodeDescriptor.class);

  private static final DNodeConfig CONF = new DNodeConfig();
  private static final ClientPoolConfig CLIENT_POOL_CONFIG = ClientPoolDescriptor.getInstance().getConf();

  private void loadProperties() {
    URL propertiesUrl;
    String urlString = System.getenv(NodeConstant.DNODE_CONF) +
      File.separatorChar + NodeConstant.DNODE_CONFIG_FILE_NAME;
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

      CONF.setDnRpcAddress(
        properties
          .getProperty(NodeConstant.DN_RPC_ADDRESS, CONF.getDnRpcAddress())
          .trim());

      CONF.setDnRpcPort(
        Integer.parseInt(
          properties
            .getProperty(NodeConstant.DN_RPC_PORT, String.valueOf(CONF.getDnRpcPort()))
            .trim()));

      CONF.setDnMinWorkerThreadNum(
        Integer.parseInt(
          properties
            .getProperty(NodeConstant.DN_MIN_WORKER_THREAD_NUM, String.valueOf(CONF.getDnMinWorkerThreadNum()))
            .trim()));

      CONF.setDnMaxWorkerThreadNum(
        Integer.parseInt(
          properties
            .getProperty(NodeConstant.DN_MAX_WORKER_THREAD_NUM, String.valueOf(CONF.getDnMaxWorkerThreadNum()))
            .trim()));

      CONF.setDnAsyncClientManagerSelectorNum(
        Integer.parseInt(
          properties
            .getProperty(
              NodeConstant.DN_ASYNC_CLIENT_MANAGER_SELECTOR_NUM,
              String.valueOf(CONF.getDnAsyncClientManagerSelectorNum()))
            .trim()));

      CONF.setDnCoreClientNumForEachNode(
        Integer.parseInt(
          properties
            .getProperty(
              NodeConstant.DN_CORE_CLIENT_NUM_FOR_EACH_NODE,
              String.valueOf(CONF.getDnCoreClientNumForEachNode()))
            .trim()));

      CONF.setDnMaxClientNumForEachNode(
        Integer.parseInt(
          properties
            .getProperty(
              NodeConstant.DN_MAX_CLIENT_NUM_FOR_EACH_NODE,
              String.valueOf(CONF.getDnMaxClientNumForEachNode()))
            .trim()));

    } catch (IOException e) {
      LOGGER.warn("Error occurs when loading config file, use default config.", e);
    }

    loadClientPoolConfig();
  }

  private void loadClientPoolConfig() {
    CLIENT_POOL_CONFIG.setAsyncSelectorNumOfClientManager(CONF.getDnAsyncClientManagerSelectorNum());
    CLIENT_POOL_CONFIG.setCoreClientNumForEachNode(CONF.getDnCoreClientNumForEachNode());
    CLIENT_POOL_CONFIG.setMaxClientNumForEachNode(CONF.getDnMaxClientNumForEachNode());
  }

  public static void loadTestConfig(TDNodeConfiguration configuration) {
    CONF.setTargetCNode(configuration.getTargetCNode());
    try {
      CONF.setRequestType(RequestType.parse(configuration.getRequestType()));
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
    CONF.setDnClientNum(configuration.getClientNum());
    CONF.setDnRequestNum(configuration.getRequestNum());
  }

  public static String getEnvironmentalVariables() {
    return "\n\t"
      + NodeConstant.DNODE_HOME
      + "="
      + System.getProperty(NodeConstant.DNODE_HOME, "null")
      + ";"
      + "\n\t"
      + NodeConstant.DNODE_CONF
      + "="
      + System.getProperty(NodeConstant.DNODE_CONF, "null")
      + ";"
      + "\n\t"
      + NodeConstant.LOGBACK_FILE
      + "="
      + System.getProperty(NodeConstant.LOGBACK_FILE, "null");
  }

  public DNodeConfig getConf() {
    return CONF;
  }

  private DNodeDescriptor() {
    loadProperties();
  }

  public static DNodeDescriptor getInstance() {
    return DNodeDescriptorHolder.INSTANCE;
  }

  private static class DNodeDescriptorHolder {

    private static final DNodeDescriptor INSTANCE = new DNodeDescriptor();

    private DNodeDescriptorHolder() {
      // Empty constructor
    }
  }
}
