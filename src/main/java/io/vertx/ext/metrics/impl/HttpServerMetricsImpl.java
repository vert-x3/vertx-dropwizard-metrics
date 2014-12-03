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

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.metrics.spi.HttpServerMetrics;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class HttpServerMetricsImpl extends HttpMetricsImpl implements HttpServerMetrics {

  private Map<HttpServerResponse, TimedContext> timings;

  HttpServerMetricsImpl(AbstractMetrics metrics, String baseName) {
    super(metrics, baseName, false);
    timings = new WeakHashMap<>();
  }

  @Override
  public void requestBegin(HttpServerRequest request, HttpServerResponse response) {
    timings.put(response, time(request.method().name(), request.uri()));
  }

  @Override
  public void responseEnd(HttpServerResponse response) {

    TimedContext ctx = timings.remove(response);
    if (ctx != null) {
      ctx.stop();
    }
  }
}
