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

package com.timecho.awesome.service;

import com.timecho.awesome.conf.NodeConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class JMXService implements IService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JMXService.class);

  public JMXService() {
    // Empty constructor
  }

  /** Function for registering MBean. */
  public static void registerMBean(Object mbean, String name) {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName objectName = new ObjectName(name);
      if (!mbs.isRegistered(objectName)) {
        mbs.registerMBean(mbean, objectName);
      }
    } catch (MalformedObjectNameException
             | InstanceAlreadyExistsException
             | MBeanRegistrationException
             | NotCompliantMBeanException e) {
      LOGGER.error("Failed to registerMBean {}", name, e);
    }
  }

  /** Function for unregistering MBean. */
  public static void deregisterMBean(String name) {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName objectName = new ObjectName(name);
      if (mbs.isRegistered(objectName)) {
        mbs.unregisterMBean(objectName);
      }
    } catch (MalformedObjectNameException
             | MBeanRegistrationException
             | InstanceNotFoundException e) {
      LOGGER.error("Failed to unregisterMBean {}", name, e);
    }
  }

  @Override
  public ServiceType getID() {
    return ServiceType.JMX_SERVICE;
  }

  @Override
  public void start() {
    String jmxPort = System.getProperty(NodeConstant.JMX_PORT);
    if (jmxPort == null) {
      LOGGER.debug("{} JMX port is undefined", this.getID().getName());
    }
  }

  @Override
  public void stop() {
    // Do nothing.
  }

  public enum ServiceType {

    JMX_SERVICE("JMXService", "JMXService"),
    CNODE_SERVICE("CNodeRPCService", "CNodeRPCService"),
    DNODE_SERVICE("DNodeRPCService", "DNodeRPCService");

    private final String name;
    private final String jmxName;

    ServiceType(String name, String jmxName) {
      this.name = name;
      this.jmxName = jmxName;
    }

    public String getName() {
      return name;
    }

    public String getJmxName() {
      return jmxName;
    }

    private static String generateJmxName(String packageName, String jmxName) {
      return String.format("%s:type=%s", packageName, jmxName);
    }
  }
}