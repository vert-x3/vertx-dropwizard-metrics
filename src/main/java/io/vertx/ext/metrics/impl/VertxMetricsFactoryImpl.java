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

package io.vertx.ext.metrics.impl;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.ext.metrics.MetricsService;
import io.vertx.ext.metrics.MetricsServiceOptions;
import io.vertx.ext.metrics.reporters.JmxReporter;
import io.vertx.core.spi.VertxMetricsFactory;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class VertxMetricsFactoryImpl implements VertxMetricsFactory {

  @Override
  public VertxMetrics metrics(Vertx vertx, VertxOptions options) {
    MetricsOptions baseOptions = options.getMetricsOptions();
    MetricsServiceOptions metricsOptions;
    if (baseOptions instanceof MetricsServiceOptions) {
      metricsOptions = (MetricsServiceOptions) baseOptions;
    } else {
      metricsOptions = new MetricsServiceOptions(baseOptions.toJson());
    }
    Registry registry = new Registry();
    if (metricsOptions.getName() != null) {
      MetricRegistry other = SharedMetricRegistries.add(metricsOptions.getName(), registry);
      if (other != null && other instanceof Registry) {
        registry = (Registry) other;
      }
    }
    VertxMetricsImpl metrics = new VertxMetricsImpl(registry, options, metricsOptions);
    // TODO: Probably should consume metrics through MetricsProvider API, and expose as JMXBeans
    if (metricsOptions.isJmxEnabled()) {
      String jmxDomain = metricsOptions.getJmxDomain();
      if (jmxDomain == null) {
        jmxDomain = "vertx" + "@" + Integer.toHexString(vertx.hashCode());
      }
      JmxReporter reporter = JmxReporter.forRegistry(metrics.registry()).inDomain(jmxDomain).build();
      metrics.setDoneHandler(v -> reporter.stop());
      reporter.start();
    }

    return metrics;
  }
}
