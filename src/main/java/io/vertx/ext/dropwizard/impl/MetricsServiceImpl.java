package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.MetricRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.ext.dropwizard.MetricsService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MetricsServiceImpl implements MetricsService {

  private final Vertx vertx;

  public MetricsServiceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public String getBaseName(Measured measured) {
    AbstractMetrics codahaleMetrics = AbstractMetrics.unwrap(measured);
    return codahaleMetrics != null ? codahaleMetrics.baseName() : null;
  }

  @Override
  public JsonObject getMetricsSnapshot(Measured measured) {
    AbstractMetrics codahaleMetrics = AbstractMetrics.unwrap(measured);
    return codahaleMetrics != null ? codahaleMetrics.metrics() : null;
  }

  @Override
  public JsonObject getMetricsSnapshot(String baseName) {
    AbstractMetrics codahaleMetrics = AbstractMetrics.unwrap(vertx);
    return codahaleMetrics != null ? codahaleMetrics.metrics(baseName) : null;
  }

  public Set<String> metricsNames() {
    AbstractMetrics codahaleMetrics = AbstractMetrics.unwrap(vertx);
    if (codahaleMetrics != null) {
      MetricRegistry registry = codahaleMetrics.registry;
      return new HashSet<>(registry.getMetrics().keySet());
    }
    return Collections.emptySet();
  }
}
