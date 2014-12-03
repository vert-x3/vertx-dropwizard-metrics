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
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class Registry extends MetricRegistry {

  public void shutdown() {
    removeMatching((name, metric) -> true);
  }

  @Override
  public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {
    return super.register(name, metric);
  }

  @Override
  public Counter counter(String name) {
    return super.counter(name);
  }

  @Override
  public Histogram histogram(String name) {
    return super.histogram(name);
  }

  @Override
  public Meter meter(String name) {
    return super.meter(name);
  }

  @Override
  public Timer timer(String name) {
    return super.timer(name);
  }

}
