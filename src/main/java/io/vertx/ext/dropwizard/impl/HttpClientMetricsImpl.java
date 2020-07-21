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

import com.codahale.metrics.Timer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import io.vertx.core.spi.observability.HttpRequest;
import io.vertx.core.spi.observability.HttpResponse;
import io.vertx.ext.dropwizard.Match;

import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class HttpClientMetricsImpl extends AbstractMetrics implements HttpClientMetrics<HttpClientRequestMetric, WebSocketMetric, Long, Timer.Context> {

  private final VertxMetricsImpl owner;
  private final Matcher uriMatcher;
  private final Matcher endpointMatcher;
  final HttpClientReporter clientReporter;
  final int maxPoolSize;

  HttpClientMetricsImpl(VertxMetricsImpl owner, HttpClientReporter clientReporter, HttpClientOptions options, List<Match> monitoredUris, List<Match> monitoredEndpoints) {
    super(clientReporter.registry, clientReporter.baseName);
    this.owner = owner;
    this.clientReporter = clientReporter;
    this.uriMatcher = new Matcher(monitoredUris);
    this.endpointMatcher = new Matcher(monitoredEndpoints);
    clientReporter.incMaxPoolSize(maxPoolSize = options.getMaxPoolSize());
  }

  @Override
  public ClientMetrics<HttpClientRequestMetric, Timer.Context, HttpRequest, HttpResponse> createEndpointMetrics(SocketAddress remoteAddress, int maxPoolSize) {
    String name = remoteAddress.toString();
    if (endpointMatcher.matches(name) != null) {
      return new EndpointMetrics(clientReporter, name, uriMatcher);
    } else {
      return null;
    }
  }

  @Override
  public void endpointConnected(ClientMetrics<HttpClientRequestMetric, Timer.Context, ?, ?> endpointMetric) {
    if (endpointMetric instanceof EndpointMetrics) {
      ((EndpointMetrics)endpointMetric).openConnections.inc();
    }
  }

  @Override
  public void endpointDisconnected(ClientMetrics<HttpClientRequestMetric, Timer.Context, ?, ?> endpointMetric) {
    if (endpointMetric instanceof EndpointMetrics) {
      ((EndpointMetrics)endpointMetric).openConnections.dec();
    }
  }

  @Override
  public WebSocketMetric connected(WebSocket webSocket) {
    return clientReporter.createWebSocketMetric();
  }

  @Override
  public void disconnected(WebSocketMetric webSocketMetric) {
    clientReporter.disconnect(webSocketMetric);
  }

  @Override
  public Long connected(SocketAddress remoteAddress, String remoteName) {
    return clientReporter.connected(remoteAddress, remoteName);
  }

  @Override
  public void disconnected(Long socketMetric, SocketAddress remoteAddress) {
    clientReporter.disconnected(socketMetric, remoteAddress);
  }

  @Override
  public void bytesRead(Long socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    clientReporter.bytesRead(socketMetric, remoteAddress, numberOfBytes);
  }

  @Override
  public void bytesWritten(Long socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    clientReporter.bytesWritten(socketMetric, remoteAddress, numberOfBytes);
  }

  @Override
  public void exceptionOccurred(Long socketMetric, SocketAddress remoteAddress, Throwable t) {
    clientReporter.exceptionOccurred(socketMetric, remoteAddress, t);
  }

  @Override
  public void close() {
    owner.closed(this);
  }
}
