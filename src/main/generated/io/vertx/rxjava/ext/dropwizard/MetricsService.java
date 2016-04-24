/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.rxjava.ext.dropwizard;

import java.util.Map;
import rx.Observable;
import io.vertx.rxjava.core.metrics.Measured;
import io.vertx.rxjava.core.Vertx;
import java.util.Set;
import io.vertx.core.json.JsonObject;

/**
 * The metrics service mainly allows to return a snapshot of measured objects.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.dropwizard.MetricsService original} non RX-ified interface using Vert.x codegen.
 */

public class MetricsService {

  final io.vertx.ext.dropwizard.MetricsService delegate;

  public MetricsService(io.vertx.ext.dropwizard.MetricsService delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Creates a metric service for a given {@link io.vertx.rxjava.core.Vertx} instance.
   * @param vertx the vertx instance
   * @return the metrics service
   */
  public static MetricsService create(Vertx vertx) { 
    MetricsService ret = MetricsService.newInstance(io.vertx.ext.dropwizard.MetricsService.create((io.vertx.core.Vertx)vertx.getDelegate()));
    return ret;
  }

  /**
   * @param measured the measure object
   * @param measured 
   * @return the base name of the measured object
   */
  public String getBaseName(Measured measured) { 
    String ret = delegate.getBaseName((io.vertx.core.metrics.Measured)measured.getDelegate());
    return ret;
  }

  /**
   * @return the known metrics names by this service
   * @return 
   */
  public Set<String> metricsNames() { 
    Set<String> ret = delegate.metricsNames();
    return ret;
  }

  /**
   * Will return the metrics that correspond with the <code>measured</code> object, null if no metrics is available.<p/>
   *
   * Note: in the case of scaled servers, the JsonObject returns an aggregation of the metrics as the
   * dropwizard backend reports to a single server.
   * @param measured 
   * @return the map of metrics where the key is the name of the metric (excluding the base name unless for the Vert.x object) and the value is the json data representing that metric
   */
  public JsonObject getMetricsSnapshot(Measured measured) { 
    JsonObject ret = delegate.getMetricsSnapshot((io.vertx.core.metrics.Measured)measured.getDelegate());
    return ret;
  }

  /**
   * Will return the metrics that begins with the <code>baseName</code>, null if no metrics is available.<p/>
   *
   * Note: in the case of scaled servers, the JsonObject returns an aggregation of the metrics as the
   * dropwizard backend reports to a single server.
   * @param baseName 
   * @return the map of metrics where the key is the name of the metric and the value is the json data representing that metric
   */
  public JsonObject getMetricsSnapshot(String baseName) { 
    JsonObject ret = delegate.getMetricsSnapshot(baseName);
    return ret;
  }


  public static MetricsService newInstance(io.vertx.ext.dropwizard.MetricsService arg) {
    return arg != null ? new MetricsService(arg) : null;
  }
}
