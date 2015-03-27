package io.vertx.ext.dropwizard.impl;

/**
* @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
*/
class RequestMetric {

  private final HttpMetricsImpl httpMetrics;
  final String method;
  final String uri;
  long start;

  RequestMetric(HttpMetricsImpl httpMetrics, String method, String uri) {
    this.httpMetrics = httpMetrics;
    this.method = (method == null) ? null : method.toLowerCase();
    this.uri = uri;
    start = System.nanoTime();
  }
}
