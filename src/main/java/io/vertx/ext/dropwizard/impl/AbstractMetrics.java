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

import com.codahale.metrics.Counter;
import com.codahale.metrics.DerivativeGauge;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.core.spi.metrics.MetricsProvider;
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

  AbstractMetrics(MetricRegistry registry, String baseName) {
    this.registry = registry;
    this.baseName = baseName;
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

  public final MetricRegistry registry() {
    return registry;
  }

  public String baseName() {
    return baseName;
  }

  protected String nameOf(String... names) {
    return MetricRegistry.name(baseName, names);
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
      return registry.histogram(nameOf(names));
    } catch (Exception e) {
      return new Histogram(new ExponentiallyDecayingReservoir());
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
      return registry.timer(nameOf(names));
    } catch (Exception e) {
      return new Timer();
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
      return RegistryHelper.throughputTimer(registry, nameOf(names));
    } catch (Exception e) {
      return new ThroughputTimer();
    }
  }

  protected void remove(String... names) {
    registry.remove(nameOf(names));
  }

  protected void removeAll() {
    registry.removeMatching((name, metric) -> name.startsWith(baseName));
  }
}
