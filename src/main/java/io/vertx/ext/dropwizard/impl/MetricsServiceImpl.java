package io.vertx.ext.dropwizard.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.ext.dropwizard.MetricsService;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MetricsServiceImpl implements MetricsService {
  @Override
  public JsonObject getMetricsSnapshot(Measured measured) {
    AbstractMetrics codahaleMetrics = AbstractMetrics.unwrap(measured);
    return codahaleMetrics != null ? codahaleMetrics.metrics() : null;
  }

  @Override
  public String getBaseName(Measured measured) {
    AbstractMetrics codahaleMetrics = AbstractMetrics.unwrap(measured);
    return codahaleMetrics != null ? codahaleMetrics.baseName() : null;
  }
}
