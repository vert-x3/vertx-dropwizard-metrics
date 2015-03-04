/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package examples;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.docgen.Source;
import io.vertx.ext.metrics.MetricsService;
import io.vertx.ext.metrics.MetricsServiceOptions;

import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Source
public class MetricsExamples {

  public void setup() {
    Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
        new MetricsServiceOptions().setEnabled(true)
    ));
  }

  public void setupJMX() {
    Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
        new MetricsServiceOptions().setJmxEnabled(true)
    ));
  }

  public void setupJMXWithDomain() {
    Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
        new MetricsServiceOptions().
            setJmxEnabled(true).
            setJmxDomain("mydomain")
    ));
  }

  public void naming1(Vertx vertx, MetricsService metricsService) {
    Map<String, JsonObject> metrics = metricsService.getMetricsSnapshot(vertx);
    metrics.get("vertx.eventbus.handlers");
  }

  public void naming2(Vertx vertx, MetricsService metricsService) {
    EventBus eventBus = vertx.eventBus();
    Map<String, JsonObject> metrics = metricsService.getMetricsSnapshot(eventBus);
    metrics.get("handlers");
  }

  public void example1(Vertx vertx) {
    MetricsService metricsService = MetricsService.create(vertx);
    Map<String, JsonObject> metrics = metricsService.getMetricsSnapshot(vertx);
    metrics.forEach((name, metric) -> {
      System.out.println(name + " : " + metric.encodePrettily());
    });
  }

  public void example3(Vertx vertx) {
    MetricsService metricsService = MetricsService.create(vertx);
    HttpServer server = vertx.createHttpServer();
    // set up server
    Map<String, JsonObject> metrics = metricsService.getMetricsSnapshot(server);
  }
}
