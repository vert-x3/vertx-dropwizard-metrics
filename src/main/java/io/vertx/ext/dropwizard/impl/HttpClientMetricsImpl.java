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
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpClientMetrics;
import io.vertx.ext.dropwizard.Match;

import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class HttpClientMetricsImpl extends AbstractMetrics implements HttpClientMetrics<RequestMetric, WebSocketMetric, Timer.Context> {

  private final HttpClientReporter clientReporter;
  private final Matcher uriMatcher;
  private final int maxPoolSize;

  HttpClientMetricsImpl(HttpClientReporter clientReporter, HttpClientOptions options, List<Match> monitoredUris) {
    super(clientReporter.registry, clientReporter.baseName);
    this.clientReporter = clientReporter;
    this.uriMatcher = new Matcher(monitoredUris);
    clientReporter.incMaxPoolSize(maxPoolSize = options.getMaxPoolSize());
  }

  @Override
  public RequestMetric requestBegin(Timer.Context socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
    return clientReporter.createRequestMetric(request.method(), request.uri());
  }

  @Override
  public RequestMetric responsePushed(Timer.Context socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
    return requestBegin(socketMetric, localAddress, remoteAddress, request);
  }

  @Override
  public void requestReset(RequestMetric requestMetric) {
  }

  @Override
  public void responseEnd(RequestMetric metric, HttpClientResponse response) {
    clientReporter.end(metric, response.statusCode(), metric.uri != null && uriMatcher.match(metric.uri));
  }

  @Override
  public WebSocketMetric connected(Timer.Context socketMetric, WebSocket webSocket) {
    return clientReporter.createWebSocketMetric();
  }

  @Override
  public void disconnected(WebSocketMetric webSocketMetric) {
    clientReporter.disconnect(webSocketMetric);
  }

  @Override
  public Timer.Context connected(SocketAddress remoteAddress, String remoteName) {
    return clientReporter.connected(remoteAddress, remoteName);
  }

  @Override
  public void disconnected(Timer.Context socketMetric, SocketAddress remoteAddress) {
    clientReporter.disconnected(socketMetric, remoteAddress);
  }

  @Override
  public void bytesRead(Timer.Context socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    clientReporter.bytesRead(socketMetric, remoteAddress, numberOfBytes);
  }

  @Override
  public void bytesWritten(Timer.Context socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
    clientReporter.bytesWritten(socketMetric, remoteAddress, numberOfBytes);
  }

  @Override
  public void exceptionOccurred(Timer.Context socketMetric, SocketAddress remoteAddress, Throwable t) {
    clientReporter.exceptionOccurred(socketMetric, remoteAddress, t);
  }

  @Override
  public boolean isEnabled() {
    return clientReporter.isEnabled();
  }

  @Override
  public void close() {
    clientReporter.decMaxPoolSize(maxPoolSize);
  }
}
