package io.vertx.ext.dropwizard.impl;

/**
* @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
*/
class RequestMetric {

  final String method;
  final String uri;
  long start;

  RequestMetric(String method, String uri) {
    this.method = (method == null) ? null : method.toLowerCase();
    this.uri = uri;
    start = System.nanoTime();
  }
}
