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
class HttpClientMetricsImpl extends AbstractMetrics implements HttpClientMetrics<HttpClientRequestMetric, WebSocketMetric, Long, EndpointMetric, Timer.Context> {

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
  public EndpointMetric createEndpoint(String host, int port, int maxPoolSize) {
    String name = host + ":" + port;
    if (endpointMatcher.match(name)) {
      return new EndpointMetric(clientReporter, name);
    } else {
      return null;
    }
  }

  @Override
  public void closeEndpoint(String host, int port, EndpointMetric endpointMetric) {
  }

  @Override
  public Timer.Context enqueueRequest(EndpointMetric endpointMetric) {
    if (endpointMetric != null) {
      endpointMetric.queueSize.inc();
      return endpointMetric.queueDelay.time();
    } else {
      return null;
    }
  }

  @Override
  public void dequeueRequest(EndpointMetric endpointMetric, Timer.Context taskMetric) {
    if (endpointMetric != null) {
      endpointMetric.queueSize.dec();
      taskMetric.stop();
    }
  }

  @Override
  public void endpointConnected(EndpointMetric endpointMetric,Long socketMetric) {
    if (endpointMetric != null) {
      endpointMetric.openConnections.inc();
    }
  }

  @Override
  public void endpointDisconnected(EndpointMetric endpointMetric, Long socketMetric) {
    if (endpointMetric != null) {
      endpointMetric.openConnections.dec();
    }
  }

  @Override
  public HttpClientRequestMetric requestBegin(EndpointMetric endpointMetric, Long socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
    if (endpointMetric != null) {
      endpointMetric.inUse.inc();
    }
    return new HttpClientRequestMetric(endpointMetric, request.method(), request.uri());
  }

  @Override
  public void requestEnd(HttpClientRequestMetric requestMetric) {
    requestMetric.requestEnd = System.nanoTime();
  }

  @Override
  public void responseBegin(HttpClientRequestMetric requestMetric, HttpClientResponse response) {
    long waitTime = System.nanoTime() - requestMetric.requestEnd;
    if (requestMetric.endpointMetric != null) {
      requestMetric.endpointMetric.ttfb.update(waitTime, TimeUnit.NANOSECONDS);
    }
  }

  @Override
  public HttpClientRequestMetric responsePushed(EndpointMetric endpointMetric, Long socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
    if (endpointMetric != null) {
      endpointMetric.inUse.inc();
    }
    return requestBegin(endpointMetric, socketMetric, localAddress, remoteAddress, request);
  }

  @Override
  public void requestReset(HttpClientRequestMetric requestMetric) {
    long duration = clientReporter.end(requestMetric, 0, requestMetric.uri != null && uriMatcher.match(requestMetric.uri));
    if (requestMetric.endpointMetric != null) {
      requestMetric.endpointMetric.inUse.dec();
      requestMetric.endpointMetric.usage.update(duration, TimeUnit.NANOSECONDS);
    }
  }

  @Override
  public void responseEnd(HttpClientRequestMetric requestMetric, HttpClientResponse response) {
    long duration = clientReporter.end(requestMetric, response.statusCode(), requestMetric.uri != null && uriMatcher.match(requestMetric.uri));
    if (requestMetric.endpointMetric != null) {
      requestMetric.endpointMetric.inUse.dec();
      requestMetric.endpointMetric.usage.update(duration, TimeUnit.NANOSECONDS);
    }
  }

  @Override
  public WebSocketMetric connected(EndpointMetric endpointMetric, Long socketMetric, WebSocket webSocket) {
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
  public boolean isEnabled() {
    return clientReporter.isEnabled();
  }

  @Override
  public void close() {
    owner.closed(this);
  }
}
