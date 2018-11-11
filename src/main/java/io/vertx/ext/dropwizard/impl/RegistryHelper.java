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

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class RegistryHelper {

  public static void shutdown(MetricRegistry registry) {
    registry.removeMatching((name, metric) -> true);
  }

  public static ThroughputMeter throughputMeter(MetricRegistry registry, String name) {
    return getOrAdd(registry, name, MetricBuilder.THROUGHPUT_METER);
  }

  public static ThroughputTimer throughputTimer(MetricRegistry registry, String name, MetricRegistry.MetricSupplier<ThroughputTimer> supplier) {
    return getOrAdd(registry, name, new MetricBuilder<ThroughputTimer>() {
      @Override
      public ThroughputTimer newMetric() {
        return supplier.newMetric();
      }

      @Override
      public boolean isInstance(Metric metric) {
        return ThroughputTimer.class.isInstance(metric);
      }
    });
  }

  public static <M extends Metric> M getOrAdd(MetricRegistry registry, String name, MetricBuilder<M> metricBuilder) {
    Metric metric = registry.getMetrics().get(name);
    if (metricBuilder.isInstance(metric)) {
      return (M) metric;
    } else if (metric == null) {
      try {
        return registry.register(name, metricBuilder.newMetric());
      } catch (IllegalArgumentException e) {
        final Metric added = registry.getMetrics().get(name);
        if (metricBuilder.isInstance(added)) {
          return (M) added;
        }
      }
    }
    throw new IllegalArgumentException(name + " is already used for a different type of metric");
  }

  private interface MetricBuilder<T extends Metric> {

    MetricBuilder<ThroughputMeter> THROUGHPUT_METER = new MetricBuilder<ThroughputMeter>() {
      @Override
      public ThroughputMeter newMetric() {
        return new ThroughputMeter();
      }

      @Override
      public boolean isInstance(Metric metric) {
        return ThroughputTimer.class.isInstance(metric);
      }
    };

    T newMetric();

    boolean isInstance(Metric metric);
  }

}
