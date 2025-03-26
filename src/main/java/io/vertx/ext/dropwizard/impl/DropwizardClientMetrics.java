package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.vertx.core.spi.metrics.ClientMetrics;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DropwizardClientMetrics<Req, Resp> extends AbstractMetrics implements ClientMetrics<RequestMetric, Timer.Context, Req, Resp> {

  final VertxMetricsImpl vertxMetrics;
  final Timer requests;
  final Timer queueDelay;
  final Counter queueSize;
  final Timer ttfb;
  final Counter inUse;
  final int count;

  public DropwizardClientMetrics(VertxMetricsImpl vertxMetrics, MetricRegistry registry, String baseName, int count) {
    super(registry, baseName);
    this.vertxMetrics = vertxMetrics;
    this.requests = timer("requests");
    this.queueDelay = timer("queue-delay");
    this.queueSize = counter("queue-size");
    this.ttfb = timer("ttfb");
    this.inUse = counter("in-use");
    this.count = count;
  }

  private DropwizardClientMetrics(DropwizardClientMetrics<Req, Resp> that, int count) {
    super(that.registry, that.baseName);
    this.vertxMetrics = that.vertxMetrics;
    this.requests = that.requests;
    this.queueDelay = that.queueDelay;
    this.queueSize = that.queueSize;
    this.ttfb = that.ttfb;
    this.inUse = that.inUse;
    this.count = count;
  }

  DropwizardClientMetrics<Req, Resp> inc() {
    return new DropwizardClientMetrics<>(this, count + 1);
  }

  DropwizardClientMetrics<Req, Resp> dec() {
    return new DropwizardClientMetrics<>(this, count - 1);
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
  public RequestMetric requestBegin(String uri, Req request) {
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
    inUse.dec();
  }

  @Override
  public void responseEnd(RequestMetric requestMetric) {
    long duration = System.nanoTime() - requestMetric.requestBegin;
    inUse.dec();
    requests.update(duration, TimeUnit.NANOSECONDS);
  }

  @Override
  public void close() {
    vertxMetrics.closed(this);
  }
}
