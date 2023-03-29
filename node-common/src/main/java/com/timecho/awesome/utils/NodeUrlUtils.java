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

package com.timecho.awesome.utils;

import com.timecho.aweseme.thrift.TEndPoint;
import com.timecho.awesome.exception.BadNodeUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class NodeUrlUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeUrlUtils.class);

  /**
   * Convert TEndPoint to TEndPointUrl
   *
   * @param endPoint TEndPoint
   * @return TEndPointUrl with format ip:port
   */
  public static String convertTEndPointUrl(TEndPoint endPoint) {
    StringJoiner url = new StringJoiner(":");
    url.add(endPoint.getIp());
    url.add(String.valueOf(endPoint.getPort()));
    return url.toString();
  }

  /**
   * Convert TEndPoints to TEndPointUrls
   *
   * @param endPoints List<TEndPoint>
   * @return TEndPointUrls with format TEndPointUrl_0,TEndPointUrl_1,...,TEndPointUrl_n
   */
  public static String convertTEndPointUrls(List<TEndPoint> endPoints) {
    StringJoiner urls = new StringJoiner(",");
    for (TEndPoint endPoint : endPoints) {
      urls.add(convertTEndPointUrl(endPoint));
    }
    return urls.toString();
  }

  /**
   * Parse TEndPoint from a given TEndPointUrl
   *
   * @param endPointUrl ip:port
   * @return TEndPoint
   * @throws BadNodeUrlException Throw when unable to parse
   */
  public static TEndPoint parseTEndPointUrl(String endPointUrl) throws BadNodeUrlException {
    String[] split = endPointUrl.split(":");
    if (split.length != 2) {
      LOGGER.warn("Illegal endpoint url format: {}", endPointUrl);
      throw new BadNodeUrlException(String.format("Bad endpoint url: %s", endPointUrl));
    }
    String ip = split[0];
    TEndPoint result;
    try {
      int port = Integer.parseInt(split[1]);
      result = new TEndPoint(ip, port);
    } catch (NumberFormatException e) {
      LOGGER.warn("Illegal endpoint url format: {}", endPointUrl);
      throw new BadNodeUrlException(String.format("Bad node url: %s", endPointUrl));
    }
    return result;
  }

  /**
   * Parse TEndPoints from given TEndPointUrls
   *
   * @param endPointUrls List<TEndPointUrl>
   * @return List<TEndPoint>
   * @throws BadNodeUrlException Throw when unable to parse
   */
  public static List<TEndPoint> parseTEndPointUrls(List<String> endPointUrls)
    throws BadNodeUrlException {
    List<TEndPoint> result = new ArrayList<>();
    for (String url : endPointUrls) {
      result.add(parseTEndPointUrl(url));
    }
    return result;
  }

  /**
   * Parse TEndPoints from given TEndPointUrls
   *
   * @param endPointUrls TEndPointUrls
   * @return List<TEndPoint>
   * @throws BadNodeUrlException Throw when unable to parse
   */
  public static List<TEndPoint> parseTEndPointUrls(String endPointUrls) throws BadNodeUrlException {
    return parseTEndPointUrls(Arrays.asList(endPointUrls.split(",")));
  }
}
