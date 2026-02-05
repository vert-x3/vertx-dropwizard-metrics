package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.observability.HttpRequest;
import io.vertx.core.spi.observability.HttpResponse;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class EndpointMetrics implements ClientMetrics<HttpClientRequestMetric, HttpRequest, HttpResponse> {

  final HttpClientReporter reporter;
  final Matcher uriMatcher;
  final Counter openConnections;
  final Timer usage;
  final Timer ttfb;
  final Counter inUse;

  public EndpointMetrics(HttpClientReporter reporter, String name, Matcher uriMatcher) {
    this.reporter = reporter;
    this.openConnections = reporter.counter("endpoint", name, "open-netsockets");
    this.usage = reporter.timer("endpoint", name, "usage");
    this.ttfb = reporter.timer("endpoint", name, "ttfb");
    this.inUse = reporter.counter("endpoint", name, "in-use");
    this.uriMatcher = uriMatcher;
  }

  @Override
  public HttpClientRequestMetric init() {
    return new HttpClientRequestMetric(this);
  }

  @Override
  public void requestBegin(HttpClientRequestMetric requestMetric, String uri, HttpRequest request) {
    inUse.inc();
    requestMetric.init(request.method(), request.uri());
  }

  @Override
  public void requestEnd(HttpClientRequestMetric requestMetric, long bytesWritten) {
    requestMetric.requestEnd = System.nanoTime();
  }

  @Override
  public void requestReset(HttpClientRequestMetric requestMetric) {
    inUse.dec();
  }

  @Override
  public void responseBegin(HttpClientRequestMetric requestMetric, HttpResponse response) {
    long waitTime = System.nanoTime() - requestMetric.requestEnd;
    requestMetric.response = response;
    ttfb.update(waitTime, TimeUnit.NANOSECONDS);
  }

  @Override
  public void responseEnd(HttpClientRequestMetric requestMetric, long bytesRead) {
    long duration = reporter.end(requestMetric, requestMetric.response.statusCode(), uriMatcher, null);
    inUse.dec();
    usage.update(duration, TimeUnit.NANOSECONDS);
  }

  @Override
  public void connected() {
    openConnections.inc();
  }

  @Override
  public void disconnected() {
    openConnections.dec();
  }
}
