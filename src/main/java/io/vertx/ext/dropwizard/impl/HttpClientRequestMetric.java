package io.vertx.ext.dropwizard.impl;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.spi.observability.HttpResponse;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpClientRequestMetric extends HttpRequestMetric {

  final EndpointMetrics endpointMetric;
  HttpResponse response;
  long requestEnd;

  public HttpClientRequestMetric(EndpointMetrics endpointMetric) {
    this.endpointMetric = endpointMetric;
  }
}
