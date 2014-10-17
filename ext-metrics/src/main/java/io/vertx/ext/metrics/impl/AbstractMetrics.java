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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.vertx.core.metrics.spi.BaseMetrics;

import static com.codahale.metrics.MetricRegistry.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
abstract class AbstractMetrics implements BaseMetrics {
  private final Registry registry;
  private String baseName;

  AbstractMetrics(Registry registry, String baseName) {
    this.registry = registry;
    this.baseName = baseName;
  }

  protected Registry registry() {
    return registry;
  }

  protected void setBaseName(String baseName) {
    this.baseName = baseName;
  }

  public String baseName() {
    return baseName;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  protected <T> Gauge<T> gauge(Gauge<T> gauge, String... names) {
    return registry.register(name(baseName, names), gauge);
  }

  protected Counter counter(String... names) {
    return registry.counter(name(baseName, names));
  }

  protected Histogram histogram(String... names) {
    return registry.histogram(name(baseName, names));
  }

  protected Meter meter(String... names) {
    return registry.meter(name(baseName, names));
  }

  protected Timer timer(String... names) {
    return registry.timer(name(baseName, names));
  }

  protected void remove(String... names) {
    registry.remove(name(baseName, names));
  }

  protected void removeAll() {
    registry.removeMatching((name, metric) -> name.startsWith(baseName));
  }

  protected static String instanceName(String baseName, Object instance) {
    return name(baseName, "@" + Integer.toHexString(instance.hashCode()));
  }
}
