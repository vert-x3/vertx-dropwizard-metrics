package io.vertx.ext.dropwizard.impl;

import java.util.LinkedList;
import java.util.List;

import io.vertx.core.http.HttpMethod;

/**
* @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
*/
class HttpRequestMetric {

  final HttpMethod method;
  final String uri;
  long requestBegin;
  final List<String> routes = new LinkedList<>();

  HttpRequestMetric(HttpMethod method, String uri) {
    this.method = method;
    this.uri = uri;
    requestBegin = System.nanoTime();
  }

  String getRoute() {
    return String.join(">", routes);
  }
}
