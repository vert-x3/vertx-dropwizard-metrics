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

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.ext.dropwizard.impl.AbstractMetrics;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 *
 * TODO - support listening to more than one Measured
 *
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ScheduledMetricsConsumer {

  private final Vertx vertx;
  private final AbstractMetrics measured;

  private BiPredicate<String, Object> filter = (name, metric) -> true;

  private volatile long timerId = -1;

  public ScheduledMetricsConsumer(Vertx vertx) {
    this(vertx, vertx);
  }

  public ScheduledMetricsConsumer(Vertx vertx, Measured measured) {
    this.vertx = vertx;
    this.measured = AbstractMetrics.unwrap(measured);
  }

  public ScheduledMetricsConsumer filter(BiPredicate<String, Object> filter) {
    if (timerId != -1) throw new IllegalStateException("Cannot set filter while metrics consumer is running.");
    this.filter = filter;
    return this;
  }

  public void start(long delay, TimeUnit unit, BiConsumer<String, Object> consumer) {
    timerId = vertx.setPeriodic(unit.toMillis(delay), tid -> {
      measured.metrics().getMap().forEach((name, metric) -> {
        System.out.println("maybe " + name);
        if (filter.test(name, metric)) {
          System.out.println("sending " + name);
          consumer.accept(name, metric);
        }
      });
    });
  }

  public void stop() {
    vertx.cancelTimer(timerId);
    timerId = -1;
  }
}
