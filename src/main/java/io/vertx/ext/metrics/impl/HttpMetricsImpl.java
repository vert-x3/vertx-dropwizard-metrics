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

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
abstract class HttpMetricsImpl extends NetServerMetricsImpl {

  private Timer requests;
  private Meter[] responses;

  public HttpMetricsImpl(AbstractMetrics metrics, String baseName, boolean client) {
    super(metrics, baseName, client);
  }

  @Override
  protected void initialize() {
    super.initialize();
    requests = timer("requests");
    responses = new Meter[]{
        meter("responses-1xx"),
        meter("responses-2xx"),
        meter("responses-3xx"),
        meter("responses-4xx"),
        meter("responses-5xx")
    };
  }

  /**
   * Provides a timed context to measure http request latency.
   *
   * @param method the http method or null if it's not to be recorded
   * @param uri the request uri or path of the request, or null if it's not to be recorded
   * @return the TimedContext to be measured
   */
  protected TimedContext time(String method, String uri) {
    return new TimedContext(method, uri);
  }

  protected class TimedContext {

    private String method;
    private String uri;
    private long start;

    private TimedContext(String method, String uri) {
      this.method = (method == null) ? null : method.toLowerCase();
      this.uri = uri;
      start = System.nanoTime();
    }

    protected void end(int statusCode) {
      if (closed) {
        return;
      }

      long duration = System.nanoTime() - start;
      int responseStatus = statusCode / 100;

      //
      if (responseStatus >= 1 && responseStatus <= 5) {
        responses[responseStatus - 1].mark();
      }

      // Update generic requests metric
      requests.update(duration, TimeUnit.NANOSECONDS);

      // Update specific method / uri request metrics
      if (method != null) {
        timer(method + "-requests").update(duration, TimeUnit.NANOSECONDS);
        if (uri != null) {
          timer(method + "-requests", uri).update(duration, TimeUnit.NANOSECONDS);
        }
      } else if (uri != null) {
        timer("requests", uri).update(duration, TimeUnit.NANOSECONDS);
      }
    }
  }
}
