package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import io.vertx.core.spi.metrics.PoolMetrics;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PoolMetricsImpl extends AbstractMetrics implements PoolMetrics<Timer.Context, Timer.Context> {

  private final Timer queueDelay;
  private final Counter queueSize;
  private final Timer usage;
  private final Counter inUse;

  public PoolMetricsImpl(MetricRegistry registry, String baseName, int maxSize) {
    super(registry, baseName);
    this.queueSize = counter("queue-size");
    this.queueDelay = timer("queue-delay");
    this.usage = timer("usage");
    this.inUse = counter("in-use");
    if (maxSize > 0) {
      RatioGauge gauge = new RatioGauge() {
        @Override
        protected Ratio getRatio() {
          return Ratio.of(inUse.getCount(), maxSize);
        }
      };
      gauge(gauge, "pool-ratio");
      gauge(() -> maxSize, "max-pool-size");
    }
  }

  @Override
  public Timer.Context enqueue() {
    queueSize.inc();
    return queueDelay.time();
  }

  @Override
  public void dequeue(Timer.Context queueMetric) {
    queueSize.dec();
    queueMetric.stop();
  }

  @Override
  public Timer.Context begin() {
    inUse.inc();
    return usage.time();
  }

  @Override
  public void end(Timer.Context usageMetric) {
    inUse.dec();
    usageMetric.stop();
  }

  @Override
  public void close() {
    removeAll();
  }
}
