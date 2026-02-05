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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.dropwizard.ThroughputMeter;
import io.vertx.ext.dropwizard.ThroughputTimer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
abstract class HttpMetricsImpl extends AbstractMetrics {

  private ThroughputTimer requests;
  private ThroughputMeter[] responses;
  private final Counter openWebSockets;

  private final Map<HttpMethod, ThroughputTimer> methodRequests;

  public HttpMetricsImpl(MetricRegistry registry, String baseName, SocketAddress localAddress) {
    super(registry, baseName);
    openWebSockets = counter("open-websockets");
    requests = throughputTimer("requests");
    responses = new ThroughputMeter[]{
        throughputMeter("responses-1xx"),
        throughputMeter("responses-2xx"),
        throughputMeter("responses-3xx"),
        throughputMeter("responses-4xx"),
        throughputMeter("responses-5xx")
    };
    methodRequests = new HashMap<>();
    for (HttpMethod method : HttpMethod.values()) {
      methodRequests.put(method, throughputTimer(method.toString().toLowerCase() + "-requests"));
    }
  }

  /**
   * Provides a request metric that to measure http request latency and more.
   *
   * @return the web socket metric to be measured
   */
  protected WebSocketMetric createWebSocketMetric() {
    openWebSockets.inc();
    return null;
  }

  /**
   * Signal end of request.
   *
   * @param metric the request metric
   * @param statusCode the status code, {@code 0} means a reset
   * @param uriMatcher the Matcher instance
   */
  protected long end(HttpRequestMetric metric, int statusCode, Matcher uriMatcher, Matcher routeMatcher) {
    String uriMatch = null;
    String routeMatch = null;
    if (uriMatcher != null && metric.uri != null) {
      uriMatch = uriMatcher.matches(metric.uri);
    }
    String route = metric.getRoute();
    if (routeMatcher != null && route != null) {
      routeMatch = routeMatcher.matches(route);
    }

    long duration = System.nanoTime() - metric.requestBegin;
    int responseStatus = statusCode / 100;

    //
    if (responseStatus >= 1 && responseStatus <= 5) {
      responses[responseStatus - 1].mark();
    }

    // Update generic requests metric
    requests.update(duration, TimeUnit.NANOSECONDS);

    // Update specific method / uri request metrics
    ThroughputTimer throughputTimerForMethod;
    if (metric.method != null && (throughputTimerForMethod = methodRequests.get(metric.method)) != null) {
      throughputTimerForMethod.update(duration, TimeUnit.NANOSECONDS);
      if (uriMatch != null) {
        throughputTimer(metric.method.toString().toLowerCase() + "-requests", uriMatch).update(duration, TimeUnit.NANOSECONDS);
        throughputMeter("responses" + "-" + responseStatus + "xx", uriMatch).mark();
      }
      if (routeMatch != null) {
        throughputTimer(metric.method.toString().toLowerCase() + "-requests", routeMatch).update(duration, TimeUnit.NANOSECONDS);
        throughputMeter("responses" + "-" + responseStatus + "xx", routeMatch).mark();
      }
    } else {
      if (uriMatch != null) {
        throughputTimer("requests", uriMatch).update(duration, TimeUnit.NANOSECONDS);
        throughputMeter("responses" + "-" + responseStatus + "xx", uriMatch).mark();
      }
      if (routeMatch != null) {
        throughputTimer("requests", routeMatch).update(duration, TimeUnit.NANOSECONDS);
        throughputMeter("responses" + "-" + responseStatus + "xx", routeMatch).mark();
      }
    }

    return duration;
  }

  protected void disconnect(WebSocketMetric metric) {
    openWebSockets.dec();
  }
}
