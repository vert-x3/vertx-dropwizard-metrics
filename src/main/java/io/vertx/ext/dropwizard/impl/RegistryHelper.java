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

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import io.vertx.ext.dropwizard.ThroughputMeter;
import io.vertx.ext.dropwizard.ThroughputTimer;

import java.util.function.Function;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class RegistryHelper {

  private static final Function<Metric, ThroughputMeter> THROUGHPUT_METER = metric -> {
    if (metric != null) {
      return (ThroughputMeter) metric;
    } else {
      return new ThroughputMeter();
    }
  };

  private static final Function<Metric, ThroughputTimer> THROUGHPUT_TIMER = metric -> {
    if (metric != null) {
      return (ThroughputTimer) metric;
    } else {
      return new ThroughputTimer();
    }
  };

  public static void shutdown(MetricRegistry registry) {
    registry.removeMatching((name, metric) -> true);
  }

  public static ThroughputMeter throughputMeter(MetricRegistry registry, String name) {
    return getOrAdd(registry, name, THROUGHPUT_METER);
  }

  public static ThroughputTimer throughputTimer(MetricRegistry registry, String name) {
    return getOrAdd(registry, name, THROUGHPUT_TIMER);
  }

  public static <M extends Metric> M getOrAdd(MetricRegistry registry, String name, Function<Metric, M> metricProvider) {
    Metric metric = registry.getMetrics().get(name);
    M found = metric != null ? metricProvider.apply(metric) : null;
    if (found != null) {
      return found;
    } else if (metric == null) {
      try {
        return registry.register(name, metricProvider.apply(null));
      } catch (IllegalArgumentException e) {
        metric = registry.getMetrics().get(name);
        found = metricProvider.apply(metric);
        if (found != null) {
          return found;
        }
      }
    }
    throw new IllegalArgumentException(name + " is already used for a different type of metric");
  }
}
