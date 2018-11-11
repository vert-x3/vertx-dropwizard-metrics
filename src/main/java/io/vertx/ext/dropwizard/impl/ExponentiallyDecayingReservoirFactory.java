package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Reservoir;
import io.vertx.ext.dropwizard.ReservoirFactory;

public class ExponentiallyDecayingReservoirFactory implements ReservoirFactory {
  @Override
  public Reservoir reservoir() {
    return new ExponentiallyDecayingReservoir();
  }
}
