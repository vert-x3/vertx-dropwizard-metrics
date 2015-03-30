package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Throughput implements Metric, Gauge<Long> {

  private final Reservoir reservoir = new SlidingTimeWindowReservoir(1, TimeUnit.SECONDS);

  public void mark() {
    reservoir.update(1);
  }

  public Long getValue() {
    return (long)reservoir.size();
  }
}
