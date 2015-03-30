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
public class Throughput implements Metered, Gauge<Long> {

  private final Reservoir reservoir = new SlidingTimeWindowReservoir(1, TimeUnit.SECONDS);
  private final Meter meter = new Meter();

  public void mark() {
    meter.mark();
    reservoir.update(1);
  }

  public Long getValue() {
    return (long)reservoir.size();
  }

  @Override
  public long getCount() {
    return meter.getCount();
  }

  @Override
  public double getFifteenMinuteRate() {
    return meter.getFifteenMinuteRate();
  }

  @Override
  public double getFiveMinuteRate() {
    return meter.getFiveMinuteRate();
  }

  @Override
  public double getMeanRate() {
    return meter.getMeanRate();
  }

  @Override
  public double getOneMinuteRate() {
    return meter.getOneMinuteRate();
  }
}
