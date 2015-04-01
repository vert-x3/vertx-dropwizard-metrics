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

import io.vertx.core.http.HttpMethod;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.ThroughputMeter;
import io.vertx.ext.dropwizard.ThroughputTimer;

import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
abstract class HttpMetricsImpl extends NetServerMetricsImpl {

  private ThroughputTimer requests;
  private ThroughputMeter[] responses;
  private Matcher uriMatcher;
  private EnumMap<HttpMethod, ThroughputTimer> methodRequests;

  public HttpMetricsImpl(AbstractMetrics metrics, String baseName, SocketAddress localAdress, List<Match> monitoredUris) {
    super(metrics, baseName, localAdress);
    uriMatcher = new Matcher(monitoredUris);
    requests = throughputTimer("requests");
    responses = new ThroughputMeter[]{
        throughputMeter("responses-1xx"),
        throughputMeter("responses-2xx"),
        throughputMeter("responses-3xx"),
        throughputMeter("responses-4xx"),
        throughputMeter("responses-5xx")
    };
    methodRequests = new EnumMap<>(HttpMethod.class);
    for (HttpMethod method : HttpMethod.values()) {
      methodRequests.put(method, throughputTimer(method.toString().toLowerCase() + "-requests"));
    }
  }

  /**
   * Provides a request metric that to measure http request latency and more.
   *
   * @param method the http method or null if it's not to be recorded
   * @param uri the request uri or path of the request, or null if it's not to be recorded
   * @return the request metric to be measured
   */
  protected RequestMetric createRequestMetric(HttpMethod method, String uri) {
    return new RequestMetric(method, uri);
  }
  
  protected void end(RequestMetric metric, int statusCode) {
    if (closed) {
      return;
    }

    long duration = System.nanoTime() - metric.start;
    int responseStatus = statusCode / 100;

    //
    if (responseStatus >= 1 && responseStatus <= 5) {
      responses[responseStatus - 1].mark();
    }

    // Update generic requests metric
    requests.update(duration, TimeUnit.NANOSECONDS);

    // Update specific method / uri request metrics
    if (metric.method != null) {
      methodRequests.get(metric.method).update(duration, TimeUnit.NANOSECONDS);
      if (metric.uri != null && uriMatcher.match(metric.uri)) {
        throughputTimer(metric.method.toString().toLowerCase() + "-requests", metric.uri).update(duration, TimeUnit.NANOSECONDS);
      }
    } else if (metric.uri != null && uriMatcher.match(metric.uri)) {
      throughputTimer("requests", metric.uri).update(duration, TimeUnit.NANOSECONDS);
    }
  }
}
