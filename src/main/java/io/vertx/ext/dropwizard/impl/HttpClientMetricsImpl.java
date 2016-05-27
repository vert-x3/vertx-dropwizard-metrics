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
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class HttpClientMetricsImpl extends AbstractMetrics implements HttpClientMetrics<HttpClientRequestMetric, WebSocketMetric, Timer.Context, EndpointMetric, Timer.Context> {

  private final VertxMetricsImpl owner;
  private final Matcher uriMatcher;
  final HttpClientReporter clientReporter;
  final int maxPoolSize;

  HttpClientMetricsImpl(VertxMetricsImpl owner, HttpClientReporter clientReporter, HttpClientOptions options, List<Match> monitoredUris) {
    super(clientReporter.registry, clientReporter.baseName);
    this.owner = owner;
    this.clientReporter = clientReporter;
    this.uriMatcher = new Matcher(monitoredUris);
    clientReporter.incMaxPoolSize(maxPoolSize = options.getMaxPoolSize());
  }

  @Override
  public EndpointMetric createEndpoint(String host, int port, int maxPoolSize) {
    return new EndpointMetric(clientReporter, host, port);
  }

  @Override
  public void closeEndpoint(String host, int port, EndpointMetric endpointMetric) {
    endpointMetric.close(clientReporter);
  }

  @Override
  public Timer.Context enqueueRequest(EndpointMetric endpointMetric) {
    endpointMetric.queued.inc();
    return endpointMetric.delay.time();
  }

  @Override
  public void dequeueRequest(EndpointMetric endpointMetric, Timer.Context taskMetric) {
    endpointMetric.queued.dec();
    taskMetric.stop();
  }

  @Override
  public void endpointConnected(EndpointMetric endpointMetric, Timer.Context socketMetric) {
    endpointMetric.openConnections.inc();
  }

  @Override
  public void endpointDisconnected(EndpointMetric endpointMetric, Timer.Context socketMetric) {
    endpointMetric.openConnections.dec();
  }

  @Override
  public HttpClientRequestMetric requestBegin(EndpointMetric endpointMetric, Timer.Context socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
    endpointMetric.inUse.inc();
    return new HttpClientRequestMetric(endpointMetric, request.method(), request.uri());
  }

  @Override
  public HttpClientRequestMetric responsePushed(EndpointMetric endpointMetric, Timer.Context socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
    endpointMetric.inUse.inc();
    return requestBegin(endpointMetric, socketMetric, localAddress, remoteAddress, request);
  }

  @Override
  public void requestReset(HttpClientRequestMetric requestMetric) {
    requestMetric.endpointMetric.inUse.dec();
    long duration = clientReporter.end(requestMetric, 0, requestMetric.uri != null && uriMatcher.match(requestMetric.uri));
    requestMetric.endpointMetric.usage.update(duration, TimeUnit.NANOSECONDS);
  }

  @Override
  public void responseEnd(HttpClientRequestMetric requestMetric, HttpClientResponse response) {
    requestMetric.endpointMetric.inUse.dec();
    long duration = clientReporter.end(requestMetric, response.statusCode(), requestMetric.uri != null && uriMatcher.match(requestMetric.uri));
    requestMetric.endpointMetric.usage.update(duration, TimeUnit.NANOSECONDS);
  }

  @Override
  public WebSocketMetric connected(EndpointMetric endpointMetric, Timer.Context socketMetric, WebSocket webSocket) {
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
    owner.closed(this);
  }
}
