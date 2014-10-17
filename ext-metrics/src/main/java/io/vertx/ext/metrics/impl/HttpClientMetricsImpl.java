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

import com.codahale.metrics.RatioGauge;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.metrics.spi.HttpClientMetrics;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class HttpClientMetricsImpl extends HttpMetricsImpl implements HttpClientMetrics {

  private Map<HttpClientRequest, TimedContext> timings;

  HttpClientMetricsImpl(AbstractMetrics metrics, String baseName, HttpClientOptions options) {
    super(metrics, baseName, true);
    initialize(options);
  }

  private void initialize(HttpClientOptions options) {
    // request timings
    timings = new WeakHashMap<>();

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
  public void requestBegin(HttpClientRequest request) {
    timings.put(request, time(request.method(), request.uri()));
  }

  @Override
  public void responseEnd(HttpClientRequest request, HttpClientResponse response) {
    TimedContext ctx = timings.remove(request);
    if (ctx != null) {
      ctx.stop();
    }
  }
}
