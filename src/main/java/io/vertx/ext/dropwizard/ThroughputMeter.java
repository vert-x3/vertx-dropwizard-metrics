package io.vertx.ext.dropwizard;

import com.codahale.metrics.Meter;
import io.vertx.ext.dropwizard.impl.InstantThroughput;

/**
 * A throughput metric, wraps a {@link Meter} object to provide a one second instant
 * throughput value returned by {@link #getValue()}.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ThroughputMeter extends Meter {

  private final InstantThroughput instantThroughput = new InstantThroughput();

  public Long getValue() {
    return instantThroughput.count();
  }

  @Override
  public void mark() {
    super.mark();
    instantThroughput.mark();
  }
}
