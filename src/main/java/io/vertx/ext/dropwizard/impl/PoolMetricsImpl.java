package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import io.vertx.core.spi.metrics.PoolMetrics;
import io.vertx.ext.dropwizard.ReservoirFactory;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PoolMetricsImpl extends AbstractMetrics implements PoolMetrics<Timer.Context> {

  private final Timer queueDelay;
  private Counter queueSize;
  private final Timer usage;
  private Counter inUse;

  public PoolMetricsImpl(MetricRegistry registry, String baseName, int maxSize, ReservoirFactory reservoirFactory) {
    super(registry, baseName, reservoirFactory);
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
  public Timer.Context submitted() {
    queueSize.inc();
    return queueDelay.time();
  }

  @Override
  public void rejected(Timer.Context context) {
    queueSize.dec();
    context.stop();
  }

  @Override
  public Timer.Context begin(Timer.Context context) {
    queueSize.dec();
    inUse.inc();
    context.stop();
    return usage.time();
  }

  @Override
  public void end(Timer.Context context, boolean succeeded) {
    inUse.dec();
    context.stop();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {
    removeAll();
  }
}
