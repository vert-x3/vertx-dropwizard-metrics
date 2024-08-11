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

package io.vertx.ext.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.impl.FileResolverImpl;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.file.FileResolver;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.ext.dropwizard.impl.VertxMetricsImpl;
import io.vertx.ext.dropwizard.reporters.JmxReporter;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DropwizardVertxMetricsFactory implements VertxMetricsFactory {

  static final String BASE_NAME = "vertx";
  private Logger logger = LoggerFactory.getLogger(DropwizardVertxMetricsFactory.class);

  private MetricRegistry metricRegistry;

  public DropwizardVertxMetricsFactory() {
    this(new MetricRegistry());
  }

  /**
   * Create a metrics factory passing a metric registry used instead of the Dropwizard shared registry.
   *
   * @param metricRegistry the metricRegistry
   */
  public DropwizardVertxMetricsFactory(MetricRegistry metricRegistry) {
    this.metricRegistry = Objects.requireNonNull(metricRegistry);
  }

  @Override
  public VertxMetrics metrics(VertxOptions options) {
    MetricsOptions baseOptions = options.getMetricsOptions();
    DropwizardMetricsOptions metricsOptions;
    if (baseOptions instanceof DropwizardMetricsOptions) {
      metricsOptions = (DropwizardMetricsOptions) baseOptions;
    } else {
      metricsOptions = new DropwizardMetricsOptions(baseOptions.toJson());
    }
    boolean shutdown = true;
    if (metricsOptions.getRegistryName() != null) {
      MetricRegistry other = SharedMetricRegistries.add(metricsOptions.getRegistryName(), metricRegistry);
      if (other != null) {
        metricRegistry = other;
        shutdown = false;
      }
    }
    // Check to see if a config file name has been set, and if it has load it and create new options file from it
    if (metricsOptions.getConfigPath() != null && !metricsOptions.getConfigPath().isEmpty()) {
      FileResolver resolver = new FileResolverImpl();
      JsonObject loadedFromFile;
      try {
        loadedFromFile = loadOptionsFile(metricsOptions.getConfigPath(), resolver);
      } finally {
        try {
          resolver.close();
        } catch (IOException ignore) {
        }
      }
      if (!loadedFromFile.isEmpty()) {
        metricsOptions = new DropwizardMetricsOptions(loadedFromFile);
      }
    }
    String baseName = metricsOptions.getBaseName() == null ? BASE_NAME : metricsOptions.getBaseName();
    VertxMetricsImpl metrics = new VertxMetricsImpl(metricRegistry, shutdown, options, metricsOptions, baseName);
    // TODO: Probably should consume metrics through MetricsProvider API, and expose as JMXBeans
    if (metricsOptions.isJmxEnabled()) {
      String jmxDomain = metricsOptions.getJmxDomain();
      if (jmxDomain == null) {
        jmxDomain = "vertx" + "@" + Integer.toHexString(options.hashCode());
      }
      JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).inDomain(jmxDomain).build();
      metrics.setDoneHandler(v -> reporter.stop());
      reporter.start();
    }

    return metrics;
  }

  private JsonObject loadOptionsFile(String configPath, FileResolver fileResolver) {
    File file = fileResolver.resolveFile(configPath);
    try (Scanner scanner = new Scanner(file)) {
      scanner.useDelimiter("\\A");
      String metricsConfigString = scanner.next();

      return new JsonObject(metricsConfigString);
    } catch (IOException ioe) {
      logger.error("Error while reading metrics config file", ioe);
    } catch (DecodeException de) {
      logger.error("Error while decoding metrics config file into JSON", de);
    }

    return new JsonObject();
  }

  @Override
  public MetricsOptions newOptions(MetricsOptions options) {
    if (options instanceof DropwizardMetricsOptions) {
      return new DropwizardMetricsOptions((DropwizardMetricsOptions) options);
    } else {
      return VertxMetricsFactory.super.newOptions(options);
    }
  }

  @Override
  public MetricsOptions newOptions() {
    return newOptions((JsonObject) null);
  }

  @Override
  public MetricsOptions newOptions(JsonObject jsonObject) {
    return jsonObject == null ? new DropwizardMetricsOptions() : new DropwizardMetricsOptions(jsonObject);
  }
}
