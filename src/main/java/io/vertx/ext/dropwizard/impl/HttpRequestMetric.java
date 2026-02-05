package io.vertx.ext.dropwizard.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.spi.observability.HttpRequest;

/**
* @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
*/
class HttpRequestMetric {

  HttpMethod method;
  String uri;
  long requestBegin;

  // a string for a single route, a list of string for multiple
  private Object routes;
  // tracks length of resulting routes string
  private int routesLength = 0;

  HttpRequestMetric() {
  }

  HttpRequestMetric init(HttpMethod method, String uri) {
    this.method = method;
    this.uri = uri;
    this.requestBegin = System.nanoTime();
    return this;
  }

  String getRoute() {
    if (routes == null) {
      return null;
    }
    if (routes instanceof String) {
      return (String) routes;
    }
    StringBuilder concatenation = new StringBuilder(routesLength);
    @SuppressWarnings("unchecked") Iterator<String> iterator = ((List<String>) routes).iterator();
    concatenation.append(iterator.next());
    while (iterator.hasNext()) {
      concatenation.append('>').append(iterator.next());
    }
    routes = concatenation.toString();
    return (String) routes;
  }

  // we try to minimize allocations as far as possible. see https://github.com/vert-x3/vertx-dropwizard-metrics/pull/101
  void addRoute(String route) {
    if (route == null) {
      return;
    }
    routesLength += route.length();
    if (routes == null) {
      routes = route;
      return;
    }
    ++routesLength;
    if (routes instanceof LinkedList) {
      //noinspection unchecked
      ((LinkedList<String>) routes).add(route);
      return;
    }
    LinkedList<String> multipleRoutes = new LinkedList<>();
    multipleRoutes.add((String) routes);
    multipleRoutes.add(route);
    routes = multipleRoutes;
  }
}
