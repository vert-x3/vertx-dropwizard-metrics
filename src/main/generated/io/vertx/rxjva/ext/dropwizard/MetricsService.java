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
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.core.metrics.Measured;
import io.vertx.rxjava.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
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

  public static MetricsService create(Vertx vertx) { 
    MetricsService ret= MetricsService.newInstance(io.vertx.ext.dropwizard.MetricsService.create((io.vertx.core.Vertx) vertx.getDelegate()));
    return ret;
  }

  /**
   * Will return the metrics that correspond with this measured object, null if no metrics is available.
   * @param o 
   * @return the map of metrics where the key is the name of the metric (excluding the base name) and the value is the json data representing that metric
   */
  public JsonObject getMetricsSnapshot(Measured o) { 
    JsonObject ret = this.delegate.getMetricsSnapshot((io.vertx.core.metrics.Measured) o.getDelegate());
    return ret;
  }

  public String getBaseName(Measured measured) { 
    String ret = this.delegate.getBaseName((io.vertx.core.metrics.Measured) measured.getDelegate());
    return ret;
  }


  public static MetricsService newInstance(io.vertx.ext.dropwizard.MetricsService arg) {
    return new MetricsService(arg);
  }
}
