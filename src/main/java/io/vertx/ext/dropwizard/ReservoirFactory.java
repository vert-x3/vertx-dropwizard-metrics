package io.vertx.ext.dropwizard;

import com.codahale.metrics.Reservoir;

public interface ReservoirFactory {

  Reservoir reservoir();

}
