package io.vertx.ext.metrics.converter;

import io.vertx.ext.metrics.Counter;
import io.vertx.ext.metrics.Gauge;
import io.vertx.ext.metrics.Meter;

public final class CodahaleConverter {

  private CodahaleConverter() {
  }

  public static Counter convert(com.codahale.metrics.Counter counter) {
    return new Counter(counter.getCount());
  }

  public static Gauge convert(com.codahale.metrics.Gauge gauge) {
    return new Gauge(gauge.getValue());
  }

  public static Meter convert(com.codahale.metrics.Meter meter) {
    return new Meter(meter.getCount(), meter.getFifteenMinuteRate(), meter.getFiveMinuteRate(),
        meter.getOneMinuteRate(), meter.getMeanRate());
  }

}
