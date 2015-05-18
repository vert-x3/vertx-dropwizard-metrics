package io.vertx.ext.dropwizard;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.vertx.ext.dropwizard.impl.InstantThroughput;

import java.util.concurrent.TimeUnit;

/**
 * A throughput metric, wraps a {@link Meter} object to provide a one second instant
 * throughput value returned by {@link #getValue()}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ThroughputTimer extends Timer {

  private final InstantThroughput instantThroughput = new InstantThroughput();

  public Long getValue() {
    return instantThroughput.count();
  }

  @Override
  public void update(long duration, TimeUnit unit) {
    super.update(duration, unit);
    instantThroughput.mark();
  }
}
