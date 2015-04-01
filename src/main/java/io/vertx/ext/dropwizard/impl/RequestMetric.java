package io.vertx.ext.dropwizard.impl;

import io.vertx.core.http.HttpMethod;

/**
* @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
*/
class RequestMetric {

  final HttpMethod method;
  final String uri;
  long start;

  RequestMetric(HttpMethod method, String uri) {
    this.method = method;
    this.uri = uri;
    start = System.nanoTime();
  }
}
