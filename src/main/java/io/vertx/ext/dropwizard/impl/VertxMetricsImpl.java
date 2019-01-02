/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.VertxOptions;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.*;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class VertxMetricsImpl extends AbstractMetrics implements VertxMetrics {

  private final DropwizardMetricsOptions options;
  private final Counter verticles;
  private Handler<Void> doneHandler;
  private final boolean shutdown;
  private final Map<String, HttpClientReporter> clientReporters = new HashMap<>();

  VertxMetricsImpl(MetricRegistry registry, boolean shutdown, VertxOptions options, DropwizardMetricsOptions metricsOptions, String baseName) {
    super(registry, baseName);

    this.options = metricsOptions;
    this.verticles = counter("verticles");
    this.shutdown = shutdown;

    gauge(options::getEventLoopPoolSize, "event-loop-size");
    gauge(options::getWorkerPoolSize, "worker-pool-size");
    if (options.isClustered()) {
      gauge(options::getClusterHost, "cluster-host");
      gauge(options::getClusterPort, "cluster-port");
    }
  }

  DropwizardMetricsOptions getOptions() {
    return options;
  }

  @Override
  String projectName(String name) {
    // Special case for vertx we keep the name as is
    return name;
  }

  @Override
  public void verticleDeployed(Verticle verticle) {
    verticles.inc();
    counter("verticles", verticleName(verticle)).inc();
  }

  @Override
  public void verticleUndeployed(Verticle verticle) {
    verticles.dec();
    counter("verticles", verticleName(verticle)).dec();
  }

  @Override
  public EventBusMetrics createEventBusMetrics() {
    return new EventBusMetricsImpl(this, nameOf("eventbus"), options);
  }

  @Override
  public HttpServerMetrics<?, ?, ?> createHttpServerMetrics(HttpServerOptions options, SocketAddress localAddress) {
    String baseName = MetricRegistry.name(nameOf("http.servers"), TCPMetricsImpl.addressName(localAddress));
    return new HttpServerMetricsImpl(registry, baseName, this.options.getMonitoredHttpServerUris(), localAddress);
  }

  @Override
  public HttpClientMetrics<?, ?, ?, ?, ?> createHttpClientMetrics(HttpClientOptions options) {
    String name = options.getMetricsName();
    String baseName;
    if (name != null && name.length() > 0) {
      baseName = nameOf("http.clients", name);
    } else {
      baseName = nameOf("http.clients");
    }
    HttpClientReporter reporter = clientReporters.computeIfAbsent(baseName, n -> new HttpClientReporter(registry, baseName, null));
    return new HttpClientMetricsImpl(this, reporter, options, this.options.getMonitoredHttpClientUris(), this.options.getMonitoredHttpClientEndpoint());
  }

  synchronized void closed(HttpClientMetricsImpl metrics) {
    HttpClientReporter reporter = metrics.clientReporter;
    if (reporter.decMaxPoolSize(metrics.maxPoolSize)) {
      clientReporters.remove(reporter.baseName);
      reporter.close();
    }
  }

  @Override
  public TCPMetrics<?> createNetServerMetrics(NetServerOptions options, SocketAddress localAddress) {
    String baseName = MetricRegistry.name(nameOf("net.servers"), TCPMetricsImpl.addressName(localAddress));
    return new TCPMetricsImpl(registry, baseName);
  }

  @Override
  public TCPMetrics<?> createNetClientMetrics(NetClientOptions options) {
    String baseName;
    if (options.getMetricsName() != null) {
      baseName = nameOf("net.clients", options.getMetricsName());
    } else {
     baseName = nameOf("net.clients");
    }
    return new TCPMetricsImpl(registry, baseName);
  }

  @Override
  public DatagramSocketMetrics createDatagramSocketMetrics(DatagramSocketOptions options) {
    return new DatagramSocketMetricsImpl(this, nameOf("datagram"));
  }

  @Override
  public PoolMetrics<?> createPoolMetrics(String poolType, String poolName, int maxPoolSize) {
    String baseName = nameOf("pools", poolType, poolName);
    return new PoolMetricsImpl(registry, baseName, maxPoolSize);
  }

  @Override
  public void close() {
    if (shutdown) {
      RegistryHelper.shutdown(registry);
      if (options.getRegistryName() != null) {
        SharedMetricRegistries.remove(options.getRegistryName());
      }
    }
    List<HttpClientReporter> reporters;
    synchronized (this) {
      reporters = new ArrayList<>(clientReporters.values());
    }
    for (HttpClientReporter reporter : reporters) {
      reporter.close();
    }
    if (doneHandler != null) {
      doneHandler.handle(null);
    }
  }

  @Override
  public boolean isMetricsEnabled() {
    return true;
  }

  void setDoneHandler(Handler<Void> handler) {
    this.doneHandler = handler;
  }

  private static String verticleName(Verticle verticle) {
    return verticle.getClass().getName();
  }

}
