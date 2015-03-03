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
import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.TimeUnit;

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

  //----------------------- Convert metrics to JsonObject

  JsonObject convertMetric(Metric metric, TimeUnit rateUnit, TimeUnit durationUnit) {
    if (metric instanceof Gauge) {
      return toJson((Gauge) metric);
    } else if (metric instanceof Counter) {
      return toJson((Counter) metric);
    } else if (metric instanceof Histogram) {
      return toJson((Histogram) metric);
    } else if (metric instanceof Meter) {
      return toJson((Meter) metric, rateUnit);
    } else if (metric instanceof Timer) {
      return toJson((Timer) metric, rateUnit, durationUnit);
    } else {
      throw new IllegalArgumentException("Unknown metric " + metric);
    }
  }

  private static JsonObject toJson(Gauge gauge) {
    return new JsonObject().put("value", gauge.getValue());
  }

  private static JsonObject toJson(Counter counter) {
    return new JsonObject().put("count", counter.getCount());
  }

  private static JsonObject toJson(Histogram histogram) {
    Snapshot snapshot = histogram.getSnapshot();
    JsonObject json = new JsonObject();
    json.put("count", histogram.getCount());

    // Snapshot
    populateSnapshot(json, snapshot, 1);

    return json;
  }

  private JsonObject toJson(Meter meter, TimeUnit rateUnit) {
    JsonObject json = new JsonObject();

    // Meter
    populateMetered(json, meter, rateUnit);

    return json;
  }

  private JsonObject toJson(Timer timer, TimeUnit rateUnit, TimeUnit durationUnit) {
    Snapshot snapshot = timer.getSnapshot();
    JsonObject json = new JsonObject();

    // Meter
    populateMetered(json, timer, rateUnit);

    // Snapshot
    double factor = 1.0 / durationUnit.toNanos(1);
    populateSnapshot(json, snapshot, factor);

    // Duration rate
    String duration = durationUnit.toString().toLowerCase();
    json.put("durationRate", duration);

    return json;
  }

  private static void populateMetered(JsonObject json, Metered meter, TimeUnit rateUnit) {
    double factor = rateUnit.toSeconds(1);
    json.put("count", meter.getCount());
    json.put("meanRate", meter.getMeanRate() * factor);
    json.put("oneMinuteRate", meter.getOneMinuteRate() * factor);
    json.put("fiveMinuteRate", meter.getFiveMinuteRate() * factor);
    json.put("fifteenMinuteRate", meter.getFifteenMinuteRate() * factor);
    String rate = "events/" + rateUnit.toString().toLowerCase();
    json.put("rate", rate);
  }

  private static void populateSnapshot(JsonObject json, Snapshot snapshot, double factor) {
    json.put("min", snapshot.getMin() * factor);
    json.put("max", snapshot.getMax() * factor);
    json.put("mean", snapshot.getMean() * factor);
    json.put("stddev", snapshot.getStdDev() * factor);
    json.put("median", snapshot.getMedian() * factor);
    json.put("75%", snapshot.get75thPercentile() * factor);
    json.put("95%", snapshot.get95thPercentile() * factor);
    json.put("98%", snapshot.get98thPercentile() * factor);
    json.put("99%", snapshot.get99thPercentile() * factor);
    json.put("99.9%", snapshot.get999thPercentile() * factor);
  }
}
