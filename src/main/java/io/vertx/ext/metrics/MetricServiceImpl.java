package io.vertx.ext.metrics;

import io.vertx.core.Vertx;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MetricServiceImpl implements MetricService {

  private final Map<String, Metric> metrics = new HashMap<>();

  public MetricServiceImpl(Vertx vertx) {
    Objects.requireNonNull(vertx);
  }

  @Override
  public void report(String name, Metric metric) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(metric);
    metrics.put(name, metric);
  }

}
