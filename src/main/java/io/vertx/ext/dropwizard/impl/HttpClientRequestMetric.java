package io.vertx.ext.dropwizard.impl;

import io.vertx.core.http.HttpMethod;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpClientRequestMetric extends HttpRequestMetric {

  final EndpointMetrics endpointMetric;
  long requestEnd;

  public HttpClientRequestMetric(EndpointMetrics endpointMetric, HttpMethod method, String uri) {
    super(method, uri);
    this.endpointMetric = endpointMetric;
  }
}
