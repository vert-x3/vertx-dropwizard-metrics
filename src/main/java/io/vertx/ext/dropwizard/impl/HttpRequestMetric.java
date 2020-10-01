package io.vertx.ext.dropwizard.impl;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.spi.observability.HttpResponse;

/**
* @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
*/
class HttpRequestMetric {

  final HttpMethod method;
  final String uri;
  HttpResponse response;
  long requestBegin;

  HttpRequestMetric(HttpMethod method, String uri) {
    this.method = method;
    this.uri = uri;
    requestBegin = System.nanoTime();
  }
}
