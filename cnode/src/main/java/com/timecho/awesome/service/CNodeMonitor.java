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

import com.timecho.awesome.concurrent.threadpool.WrappedThreadPoolExecutor;
import com.timecho.awesome.conf.NodeConstant;
import com.timecho.awesome.conf.ServiceType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CNodeMonitor implements IService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CNodeMonitor.class);

  private static final String CHART_FILE_NAME =
    System.getProperty(NodeConstant.CNODE_HOME) + File.separator + "CNodeThreadCount.png";

  private final long systemStartTime;

  private final AtomicBoolean monitor;
  private final WrappedThreadPoolExecutor executor;
  // Map<timestamp, thread count>
  private final TreeMap<Long, Integer> threadCountMap;

  public CNodeMonitor(long systemStartTime, WrappedThreadPoolExecutor executor) {
    this.systemStartTime = systemStartTime;
    this.monitor = new AtomicBoolean(true);
    this.executor = executor;
    this.threadCountMap = new TreeMap<>();
  }

  @Override
  public void start() {
    CompletableFuture.runAsync(() -> {
      while (monitor.get()) {
        try {
          TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
          LOGGER.warn(e.getMessage());
        }
        threadCountMap.put(System.currentTimeMillis(), executor.getActiveCount());
      }
      drawChart();
    });
  }

  @Override
  public void stop() {
    monitor.set(false);
  }

  @Override
  public ServiceType getID() {
    return ServiceType.CNODE_MONITOR;
  }

  public void drawChart() {
    LOGGER.info("Drawing line chart at {}", CHART_FILE_NAME);

    XYDataset dataset = createDataset();
    JFreeChart chart = createChart(dataset);
    saveChartAsPNG(chart, CHART_FILE_NAME, 800, 600);

    CNode.getInstance().deactivate();
  }

  private XYDataset createDataset() {
    XYSeries series = new XYSeries("Data");
    for (Map.Entry<Long, Integer> entry : threadCountMap.entrySet()) {
      long currentTimeInSec = (entry.getKey() - systemStartTime);
      series.add(currentTimeInSec, entry.getValue());
    }
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(series);
    return dataset;
  }

  private static JFreeChart createChart(XYDataset dataset) {
    JFreeChart chart = ChartFactory.createXYLineChart(
      "CNode Thread Count Line Chart",
      "Running Time",
      "Thread Count",
      dataset,
      PlotOrientation.VERTICAL,
      true,
      true,
      false
    );
    return chart;
  }

  private static void saveChartAsPNG(
    JFreeChart chart, String filename, int width, int height) {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));
    try {
      ImageIO.write(image, "png", new File(filename));
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }
  }
}
