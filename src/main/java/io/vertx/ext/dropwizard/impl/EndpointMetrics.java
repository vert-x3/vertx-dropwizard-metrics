package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class EndpointMetrics implements ClientMetrics<HttpClientRequestMetric, Timer.Context, HttpClientRequest, HttpClientResponse> {

  final HttpClientReporter reporter;
  final Matcher uriMatcher;
  final Timer queueDelay;
  final Counter queueSize;
  final Counter openConnections;
  final Timer usage;
  final Timer ttfb;
  final Counter inUse;

  public EndpointMetrics(HttpClientReporter reporter, String name, Matcher uriMatcher) {
    this.reporter = reporter;
    this.queueDelay = reporter.timer("endpoint", name, "queue-delay");
    this.queueSize = reporter.counter("endpoint", name, "queue-size");
    this.openConnections = reporter.counter("endpoint", name, "open-netsockets");
    this.usage = reporter.timer("endpoint", name, "usage");
    this.ttfb = reporter.timer("endpoint", name, "ttfb");
    this.inUse = reporter.counter("endpoint", name, "in-use");
    this.uriMatcher = uriMatcher;
  }

  public Timer.Context enqueueRequest() {
    queueSize.inc();
    return queueDelay.time();
  }

  public void dequeueRequest(Timer.Context taskMetric) {
    queueSize.dec();
    taskMetric.stop();
  }

  @Override
  public HttpClientRequestMetric requestBegin(String uri, HttpClientRequest request) {
    inUse.inc();
    return new HttpClientRequestMetric(this, request.method(), request.uri());
  }

  @Override
  public void requestEnd(HttpClientRequestMetric requestMetric) {
    requestMetric.requestEnd = System.nanoTime();
  }

  @Override
  public void requestReset(HttpClientRequestMetric requestMetric) {
    inUse.inc();
  }

  @Override
  public void responseBegin(HttpClientRequestMetric requestMetric, HttpClientResponse response) {
    long waitTime = System.nanoTime() - requestMetric.requestEnd;
    ttfb.update(waitTime, TimeUnit.NANOSECONDS);
  }

  @Override
  public void responseEnd(HttpClientRequestMetric requestMetric, HttpClientResponse response) {
    long duration = reporter.end(requestMetric, response.statusCode(), uriMatcher);
    inUse.dec();
    usage.update(duration, TimeUnit.NANOSECONDS);
  }
}
