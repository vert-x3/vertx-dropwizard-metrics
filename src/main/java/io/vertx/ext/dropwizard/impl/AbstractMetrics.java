/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.dropwizard.impl;

import com.codahale.metrics.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.core.spi.metrics.MetricsProvider;
import io.vertx.ext.dropwizard.ReservoirFactory;
import io.vertx.ext.dropwizard.ThroughputMeter;
import io.vertx.ext.dropwizard.ThroughputTimer;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Base Codahale metrics object.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class AbstractMetrics implements Metrics {

  public static AbstractMetrics unwrap(Measured measured) {
    MetricsProvider provider = (MetricsProvider) measured;
    Metrics baseMetrics = provider.getMetrics();
    if (baseMetrics instanceof AbstractMetrics) {
      return (AbstractMetrics) baseMetrics;
    }
    return null;
  }

  protected final MetricRegistry registry;
  protected final String baseName;

  protected final ReservoirFactory reservoirFactory;

  AbstractMetrics(MetricRegistry registry, String baseName, ReservoirFactory reservoirFactory) {
    this.registry = registry;
    this.baseName = baseName;
    this.reservoirFactory = reservoirFactory;
  }

  /**
   * Will return the metrics that correspond with a given base name.
   *
   * @return the map of metrics where the key is the name of the metric (excluding the base name) and the value is
   * the json data representing that metric
   */
  public JsonObject metrics(String baseName) {
    Map<String, Object> map = registry.getMetrics().
        entrySet().
        stream().
        filter(e -> e.getKey().startsWith(baseName)).
        collect(Collectors.toMap(
            e -> projectName(e.getKey()),
            e -> Helper.convertMetric(e.getValue(), TimeUnit.SECONDS, TimeUnit.MILLISECONDS)));
    return new JsonObject(map);
  }

  /**
   * Will return the metrics that correspond with this measured object.
   *
   * @see #metrics(String)
   */
  public JsonObject metrics() {
    return metrics(baseName());
  }

  String projectName(String name) {
    String baseName = baseName();
    return name.substring(baseName.length() + 1);
  }

  protected MetricRegistry registry() {
    return registry;
  }

  public String baseName() {
    return baseName;
  }

  protected String nameOf(String... names) {
    return MetricRegistry.name(baseName, names);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  protected <T> Gauge<T> gauge(Gauge<T> gauge, String... names) {
    try {
      return registry.register(nameOf(names), gauge);
    } catch (IllegalArgumentException e) {
      return gauge;
    }
  }

  protected Counter counter(String... names) {
    try {
      return registry.counter(nameOf(names));
    } catch (Exception e) {
      return new Counter();
    }
  }

  protected Histogram histogram(String... names) {
    try {
      return registry.histogram(nameOf(names), () -> new Histogram(reservoirFactory.reservoir()));
    } catch (Exception e) {
      return new Histogram(reservoirFactory.reservoir());
    }
  }

  protected Meter meter(String... names) {
    try {
      return registry.meter(nameOf(names));
    } catch (Exception e) {
      return new Meter();
    }
  }

  protected Timer timer(String... names) {
    try {
      return registry.timer(nameOf(names), () -> new Timer(reservoirFactory.reservoir()));
    } catch (Exception e) {
      return new Timer(reservoirFactory.reservoir());
    }
  }

  protected ThroughputMeter throughputMeter(String... names) {
    try {
      return RegistryHelper.throughputMeter(registry, nameOf(names));
    } catch (Exception e) {
      return new ThroughputMeter();
    }
  }

  protected ThroughputTimer throughputTimer(String... names) {
    try {
      return RegistryHelper.throughputTimer(registry, nameOf(names), () -> new ThroughputTimer(reservoirFactory.reservoir()));
    } catch (Exception e) {
      return new ThroughputTimer(reservoirFactory.reservoir());
    }
  }

  protected void remove(String... names) {
    registry.remove(nameOf(names));
  }

  protected void removeAll() {
    registry.removeMatching((name, metric) -> name.startsWith(baseName));
  }
}
