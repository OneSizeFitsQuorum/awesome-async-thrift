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

import com.timecho.aweseme.thrift.TEndPoint;

import java.util.Collections;
import java.util.List;

public class CNodeConfig {

  private String cnRpcAddress = "127.0.0.1";
  private int cnRpcPort = 10710;

  private List<TEndPoint> workerDnList =
    Collections.singletonList(new TEndPoint("127.0.0.1", 6667));

  private CNodeServerType cnServerType = CNodeServerType.ASYNC;
  private int cnAsyncServiceSelectorNum = 1;
  private RequestType requestType = RequestType.IO;
  private int dnConcurrentClientNum = 10;
  private int dnRequestNumPerClient = 100;

  private int cnMinWorkerThreadNum = Runtime.getRuntime().availableProcessors();
  private int cnMaxWorkerThreadNum = 65535;

  private int cnAsyncClientManagerSelectorNum = 4;
  private int cnCoreClientNumForEachNode = 200;
  private int cnMaxClientNumForEachNode = 300;

  public String getCnRpcAddress() {
    return cnRpcAddress;
  }

  public void setCnRpcAddress(String cnRpcAddress) {
    this.cnRpcAddress = cnRpcAddress;
  }

  public int getCnRpcPort() {
    return cnRpcPort;
  }

  public void setCnRpcPort(int cnRpcPort) {
    this.cnRpcPort = cnRpcPort;
  }

  public List<TEndPoint> getWorkerDnList() {
    return workerDnList;
  }

  public void setWorkerDnList(List<TEndPoint> workerDnList) {
    this.workerDnList = workerDnList;
  }

  public CNodeServerType getCnServerType() {
    return cnServerType;
  }

  public void setCnServerType(CNodeServerType cnServerType) {
    this.cnServerType = cnServerType;
  }

  public int getCnAsyncServiceSelectorNum() {
    return cnAsyncServiceSelectorNum;
  }

  public void setCnAsyncServiceSelectorNum(int cnAsyncServiceSelectorNum) {
    this.cnAsyncServiceSelectorNum = cnAsyncServiceSelectorNum;
  }

  public RequestType getRequestType() {
    return requestType;
  }

  public void setRequestType(RequestType requestType) {
    this.requestType = requestType;
  }

  public int getDnConcurrentClientNum() {
    return dnConcurrentClientNum;
  }

  public void setDnConcurrentClientNum(int dnConcurrentClientNum) {
    this.dnConcurrentClientNum = dnConcurrentClientNum;
  }

  public int getDnRequestNumPerClient() {
    return dnRequestNumPerClient;
  }

  public void setDnRequestNumPerClient(int dnRequestNumPerClient) {
    this.dnRequestNumPerClient = dnRequestNumPerClient;
  }

  public int getCnMinWorkerThreadNum() {
    return cnMinWorkerThreadNum;
  }

  public void setCnMinWorkerThreadNum(int cnMinWorkerThreadNum) {
    this.cnMinWorkerThreadNum = cnMinWorkerThreadNum;
  }

  public int getCnMaxWorkerThreadNum() {
    return cnMaxWorkerThreadNum;
  }

  public void setCnMaxWorkerThreadNum(int cnMaxWorkerThreadNum) {
    this.cnMaxWorkerThreadNum = cnMaxWorkerThreadNum;
  }

  public int getCnAsyncClientManagerSelectorNum() {
    return cnAsyncClientManagerSelectorNum;
  }

  public void setCnAsyncClientManagerSelectorNum(int cnAsyncClientManagerSelectorNum) {
    this.cnAsyncClientManagerSelectorNum = cnAsyncClientManagerSelectorNum;
  }

  public int getCnCoreClientNumForEachNode() {
    return cnCoreClientNumForEachNode;
  }

  public void setCnCoreClientNumForEachNode(int cnCoreClientNumForEachNode) {
    this.cnCoreClientNumForEachNode = cnCoreClientNumForEachNode;
  }

  public int getCnMaxClientNumForEachNode() {
    return cnMaxClientNumForEachNode;
  }

  public void setCnMaxClientNumForEachNode(int cnMaxClientNumForEachNode) {
    this.cnMaxClientNumForEachNode = cnMaxClientNumForEachNode;
  }
}
