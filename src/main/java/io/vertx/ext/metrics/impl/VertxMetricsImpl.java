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

package io.vertx.ext.metrics.impl;

import com.codahale.metrics.Counter;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.VertxOptions;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.spi.metrics.DatagramSocketMetrics;
import io.vertx.core.spi.metrics.EventBusMetrics;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.ext.metrics.MetricsServiceOptions;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class VertxMetricsImpl extends AbstractMetrics implements VertxMetrics {

  static final String BASE_NAME = "vertx";

  private final MetricsServiceOptions options;
  private final Counter timers;
  private final Counter verticles;
  private Handler<Void> doneHandler;

  VertxMetricsImpl(Registry registry, VertxOptions options, MetricsServiceOptions metricsOptions) {
    super(registry, BASE_NAME);

    this.timers = counter("timers");
    this.options = metricsOptions;
    this.verticles = counter("verticles");

    gauge(options::getEventLoopPoolSize, "event-loop-size");
    gauge(options::getWorkerPoolSize, "worker-pool-size");
    if (options.isClustered()) {
      gauge(options::getClusterHost, "cluster-host");
      gauge(options::getClusterPort, "cluster-port");
    }
  }

  @Override
  String projectName(String name) {
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
  public void timerCreated(long id) {
    timers.inc();
  }

  @Override
  public void timerEnded(long id, boolean cancelled) {
    timers.dec();
  }

  @Override
  public EventBusMetrics createMetrics(EventBus eventBus) {
    return new EventBusMetricsImpl(this, nameOf("eventbus"), options);
  }

  @Override
  public HttpServerMetrics<?, ?> createMetrics(HttpServer server, HttpServerOptions options) {
    return new HttpServerMetricsImpl(this, nameOf("http.servers"));
  }

  @Override
  public HttpClientMetrics<?, ?> createMetrics(HttpClient client, HttpClientOptions options) {
    return new HttpClientMetricsImpl(this, instanceName(nameOf("http.clients"), client), options);
  }

  @Override
  public TCPMetrics<?> createMetrics(NetServer server, NetServerOptions options) {
    return new NetServerMetricsImpl(this, nameOf("net.servers"), false);
  }

  @Override
  public TCPMetrics<?> createMetrics(NetClient client, NetClientOptions options) {
    return new NetServerMetricsImpl(this, instanceName(nameOf("net.clients"), client), true);
  }

  @Override
  public DatagramSocketMetrics createMetrics(DatagramSocket socket, DatagramSocketOptions options) {
    return new DatagramSocketMetricsImpl(this, nameOf("datagram"));
  }

  @Override
  public void close() {
    registry().shutdown();
    if (doneHandler != null) {
      doneHandler.handle(null);
    }
  }

  @Override
  public String metricBaseName() {
    return baseName();
  }

  void setDoneHandler(Handler<Void> handler) {
    this.doneHandler = handler;
  }

  private static String verticleName(Verticle verticle) {
    return verticle.getClass().getName();
  }

}
