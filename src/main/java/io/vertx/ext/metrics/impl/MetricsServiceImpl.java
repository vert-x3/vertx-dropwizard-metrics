package io.vertx.ext.metrics.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.ext.metrics.MetricsService;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MetricsServiceImpl implements MetricsService {
  @Override
  public Map<String, JsonObject> getMetricsSnapshot(Measured measured) {
    AbstractMetrics codahaleMetrics = AbstractMetrics.unwrap(measured);
    return codahaleMetrics != null ? codahaleMetrics.metrics() : Collections.emptyMap();
  }
}
