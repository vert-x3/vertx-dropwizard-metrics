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
import com.codahale.metrics.Timer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.ext.dropwizard.Match;

import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class HttpServerMetricsImpl extends HttpMetricsImpl implements HttpServerMetrics<RequestMetric, WebSocketMetric, Long> {

  private final Matcher uriMatcher;

  HttpServerMetricsImpl(MetricRegistry registry, String baseName, List<Match> monitoredUris, SocketAddress localAddress) {
    super(registry, baseName, localAddress);
    uriMatcher = new Matcher(monitoredUris);
  }

  @Override
  public RequestMetric requestBegin(Long socketMetric, HttpServerRequest request) {
    return new RequestMetric(request.method(), request.uri());
  }

  @Override
  public WebSocketMetric upgrade(RequestMetric requestMetric, ServerWebSocket serverWebSocket) {
    return createWebSocketMetric();
  }

  @Override
  public void responseEnd(RequestMetric requestMetric, HttpServerResponse response) {
    end(requestMetric, response.getStatusCode(), uriMatcher);
  }

  @Override
  public void requestReset(RequestMetric requestMetric) {
  }

  @Override
  public RequestMetric responsePushed(Long socketMetric, HttpMethod method, String uri, HttpServerResponse response) {
    return new RequestMetric(method, uri);
  }

  @Override
  public WebSocketMetric connected(Long socketMetric, ServerWebSocket serverWebSocket) {
    return createWebSocketMetric();
  }

  @Override
  public void disconnected(WebSocketMetric serverWebSocketMetric) {
    disconnect(serverWebSocketMetric);
  }
}
