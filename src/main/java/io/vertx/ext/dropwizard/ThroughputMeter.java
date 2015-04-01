package io.vertx.ext.dropwizard;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;

import java.util.concurrent.TimeUnit;

/**
 * A throughput metric, wraps a {@link Meter} object to provide a one second instant
 * throughput value returned by {@link #getValue()}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ThroughputMeter extends Meter implements Gauge<Long> {

  private final Reservoir reservoir = new SlidingTimeWindowReservoir(1, TimeUnit.SECONDS);

  @Override
  public Long getValue() {
    return (long)reservoir.size();
  }

  @Override
  public void mark() {
    super.mark();
    reservoir.update(1);
  }
}
