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

import com.codahale.metrics.RatioGauge;
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
class HttpClientMetricsImpl extends HttpMetricsImpl implements HttpClientMetrics<RequestMetric, WebSocketMetric, Timer.Context> {

  HttpClientMetricsImpl(AbstractMetrics metrics, String baseName, HttpClientOptions options, List<Match> monitoredUris) {
    super(metrics, baseName, null, monitoredUris);
    // max pool size gauge
    int maxPoolSize = options.getMaxPoolSize();
    gauge(() -> maxPoolSize, "connections", "max-pool-size");

    // connection pool ratio
    RatioGauge gauge = new RatioGauge() {
      @Override
      protected Ratio getRatio() {
        return Ratio.of(connections(), maxPoolSize);
      }
    };
    gauge(gauge, "connections", "pool-ratio");
  }

  @Override
  public RequestMetric requestBegin(Timer.Context socketMetric, SocketAddress localAddress, SocketAddress remoteAddress, HttpClientRequest request) {
    return createRequestMetric(request.method(), request.uri());
  }

  @Override
  public void responseEnd(RequestMetric metric, HttpClientResponse response) {
    end(metric, response.statusCode());
  }

  @Override
  public WebSocketMetric connected(Timer.Context socketMetric, WebSocket webSocket) {
    return createWebSocketMetric();
  }

  @Override
  public void disconnected(WebSocketMetric webSocketMetric) {
    disconnect(webSocketMetric);
  }
}
