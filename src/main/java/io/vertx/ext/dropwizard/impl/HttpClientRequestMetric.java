package io.vertx.ext.dropwizard.impl;

import io.vertx.core.http.HttpMethod;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpClientRequestMetric extends RequestMetric {

  final EndpointMetric endpointMetric;
  long requestEnd;

  public HttpClientRequestMetric(EndpointMetric endpointMetric, HttpMethod method, String uri) {
    super(method, uri);
    this.endpointMetric = endpointMetric;
  }
}
