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
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.ext.dropwizard.Match;

import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class HttpServerMetricsImpl extends HttpMetricsImpl implements HttpServerMetrics<RequestMetric, Timer.Context> {

  HttpServerMetricsImpl(AbstractMetrics metrics, String baseName, List<Match> monitoredUris, SocketAddress localAddress) {
    super(metrics, baseName, localAddress, monitoredUris);
  }

  @Override
  public RequestMetric requestBegin(Timer.Context socketMetric, HttpServerRequest request) {
    return createRequestMetric(request.method().name(), request.uri());
  }

  @Override
  public void responseEnd(RequestMetric requestMetric, HttpServerResponse response) {
    end(requestMetric, response.getStatusCode());
  }
}
