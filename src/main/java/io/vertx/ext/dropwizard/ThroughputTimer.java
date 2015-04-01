package io.vertx.ext.dropwizard;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;

import java.util.concurrent.TimeUnit;

/**
 * A throughput metric, wraps a {@link Meter} object to provide a one second instant
 * throughput value returned by {@link #getValue()}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ThroughputTimer extends Timer implements Gauge<Long> {

  private final Reservoir reservoir = new SlidingTimeWindowReservoir(1, TimeUnit.SECONDS);

  @Override
  public Long getValue() {
    return (long)reservoir.size();
  }

  @Override
  public void update(long duration, TimeUnit unit) {
    super.update(duration, unit);
    reservoir.update(1);
  }
}
