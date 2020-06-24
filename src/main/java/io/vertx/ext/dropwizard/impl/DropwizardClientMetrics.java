package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DropwizardClientMetrics<Req, Resp> extends AbstractMetrics implements ClientMetrics<RequestMetric, Timer.Context, Req, Resp> {

  final Timer requests;
  final Timer queueDelay;
  final Counter queueSize;
  final Timer ttfb;
  final Counter inUse;

  public DropwizardClientMetrics(MetricRegistry registry, String baseName) {
    super(registry, baseName);
    this.requests = timer("requests");
    this.queueDelay = timer("queue-delay");
    this.queueSize = counter("queue-size");
    this.ttfb = timer("ttfb");
    this.inUse = counter("in-use");
  }

  @Override
  public Timer.Context enqueueRequest() {
    queueSize.inc();
    return queueDelay.time();
  }

  @Override
  public void dequeueRequest(Timer.Context taskMetric) {
    queueSize.dec();
    taskMetric.stop();
  }

  @Override
  public RequestMetric requestBegin(Req request) {
    inUse.inc();
    RequestMetric metric = new RequestMetric();
    metric.requestBegin = System.nanoTime();
    return metric;
  }

  @Override
  public void requestEnd(RequestMetric metric) {
    metric.requestEnd = System.nanoTime();
  }

  @Override
  public void responseBegin(RequestMetric requestMetric, Resp response) {
    long waitTime = System.nanoTime() - requestMetric.requestEnd;
    ttfb.update(waitTime, TimeUnit.NANOSECONDS);
  }

  @Override
  public void requestReset(RequestMetric requestMetric) {
    inUse.inc();
  }

  @Override
  public void responseEnd(RequestMetric requestMetric, Resp response) {
    long duration = System.nanoTime() - requestMetric.requestBegin;
    inUse.dec();
    requests.update(duration, TimeUnit.NANOSECONDS);
  }

  @Override
  public void close() {
    // Cleanup
    removeAll();
  }
}
