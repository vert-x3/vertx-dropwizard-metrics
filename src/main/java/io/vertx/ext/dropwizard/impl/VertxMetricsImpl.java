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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.*;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class VertxMetricsImpl extends AbstractMetrics implements VertxMetrics {

  private final DropwizardMetricsOptions options;
  private Handler<Void> doneHandler;
  private final boolean shutdown;
  private final Map<String, HttpClientReporter> clientReporters = new ConcurrentHashMap<>();
  private final Map<String, DropwizardClientMetrics> clientMetrics = new HashMap<>();
  private final Matcher httpClientMonitoredEndpoints;

  public VertxMetricsImpl(MetricRegistry registry, boolean shutdown, VertxOptions options, DropwizardMetricsOptions metricsOptions, String baseName) {
    super(registry, baseName);

    List<Match> monitoredHttpClientEndpoint = metricsOptions.getMonitoredHttpClientEndpoint();
    Matcher monitoredHttpClientMatcher;
    if (monitoredHttpClientEndpoint != null) {
      monitoredHttpClientMatcher = new Matcher(monitoredHttpClientEndpoint);
    } else {
      monitoredHttpClientMatcher = null;
    }

    this.options = metricsOptions;
    this.shutdown = shutdown;
    this.httpClientMonitoredEndpoints = monitoredHttpClientMatcher;

    gauge(options::getEventLoopPoolSize, "event-loop-size");
    gauge(options::getWorkerPoolSize, "worker-pool-size");
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
  public EventBusMetrics createEventBusMetrics() {
    return new EventBusMetricsImpl(this, nameOf("eventbus"), options);
  }

  @Override
  public HttpServerMetrics<?, ?, ?> createHttpServerMetrics(HttpServerOptions options, SocketAddress localAddress) {
    String baseName = MetricRegistry.name(nameOf("http.servers"), TCPMetricsImpl.addressName(localAddress));
    return new HttpServerMetricsImpl(registry, baseName, this.options.getMonitoredHttpServerUris(), this.options.getMonitoredHttpServerRoutes(), localAddress);
  }

  @Override
  public synchronized ClientMetrics<?, ?, ?> createClientMetrics(SocketAddress remoteAddress, String type, String namespace) {
    String baseName;
    if (namespace != null && namespace.length() > 0) {
      baseName = MetricRegistry.name(nameOf(type, "clients", namespace, remoteAddress.toString()));
    } else {
      baseName = MetricRegistry.name(nameOf(type, "clients", remoteAddress.toString()));
    }
    return clientMetrics.compute(baseName, (key, prev) -> {
      if (prev == null) {
        return new DropwizardClientMetrics<>(this, registry, baseName, 1);
      } else {
        return prev.inc();
      }
    });
  }

  @Override
  public HttpClientMetrics<?, ?, ?> createHttpClientMetrics(HttpClientOptions options) {
    String name = options.getMetricsName();
    String baseName;
    if (name != null && name.length() > 0) {
      baseName = nameOf("http.clients", name);
    } else {
      baseName = nameOf("http.clients");
    }
    HttpClientReporter reporter = clientReporters.computeIfAbsent(baseName, n -> new HttpClientReporter(registry, baseName, null));
    return new HttpClientMetricsImpl(this, reporter, options, this.options.getMonitoredHttpClientUris(), httpClientMonitoredEndpoints);
  }

  synchronized void closed(HttpClientMetricsImpl metrics) {
    HttpClientReporter reporter = metrics.clientReporter;
    if (reporter.decMaxPoolSize(1)) {
      clientReporters.remove(reporter.baseName);
      reporter.close();
    }
  }

  synchronized void closed(DropwizardClientMetrics metrics) {
    clientMetrics.compute(metrics.baseName, (key, prev) -> {
      DropwizardClientMetrics instance = prev.dec();
      if (instance.count == 0) {
        instance.removeAll();
        return null;
      } else {
        return instance;
      }
    });
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
  public PoolMetrics<?, ?> createPoolMetrics(String poolType, String poolName, int maxPoolSize) {
    if ("http".equals(poolType) && httpClientMonitoredEndpoints.matches(poolName) == null) {
      return null;
    }
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

  public void setDoneHandler(Handler<Void> handler) {
    this.doneHandler = handler;
  }
}
