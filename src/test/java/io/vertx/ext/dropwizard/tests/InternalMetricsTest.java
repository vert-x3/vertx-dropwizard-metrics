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

package io.vertx.ext.dropwizard.tests;

import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:jtakvori@redhat.com">Joel Takvorian</a>
 */
public class InternalMetricsTest extends MetricsTestBase {

  private MetricsService metricsService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    metricsService = MetricsService.create(vertx);
  }

  @Override
  protected VertxOptions getOptions() {
    return new VertxOptions().
        setMetricsOptions(
            new DropwizardMetricsOptions().
                addMonitoredEventBusHandler(new Match().setType(MatchType.REGEX).setValue(".*")).
                setEnabled(true).
                setJmxEnabled(true));
  }

  @Test
  public void testWebsocketDontProduceEventBusMetrics(TestContext should) throws Exception {
    Buffer serverMin = randomBuffer(500);
    Buffer serverMax = randomBuffer(1000);
    Buffer clientMax = randomBuffer(300);
    Buffer clientMin = randomBuffer(100);

    AtomicBoolean sendMax = new AtomicBoolean(false);
    WebSocketClient client = vertx.createWebSocketClient();
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080));
    server.webSocketHandler(socket -> {
      JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
      assertNoInternal(metrics);
      socket.handler(buff -> {
        if (sendMax.getAndSet(!sendMax.get())) {
          socket.write(serverMax);
        } else {
          socket.write(serverMin);
        }
      });
    }).listen().await(20, TimeUnit.SECONDS);

    Async async = should.async();

    AtomicBoolean complete = new AtomicBoolean(false);
    client
      .connect(8080, "localhost", "/blah")
      .onComplete(should.asyncAssertSuccess(socket -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
        assertNoInternal(metrics);
        socket.write(clientMax);
        socket.handler(buff -> {
          if (!complete.getAndSet(true)) {
            socket.write(clientMin);
          } else {
            socket.closeHandler(done -> {
              async.complete();
            });
            socket.close();
          }
        });
      }));

    async.awaitSuccess(20_000);

    // Make sure there is no eventbus internal metrics
    JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
    assertNoInternal(metrics);

    cleanup(client);
    cleanup(server);
  }

  private void assertNoInternal(JsonObject snapshot) {
    Optional<String> internal = snapshot.stream()
      .map(Map.Entry::getKey)
      .filter(k -> k.contains("__vertx"))
      .findAny();
    assertEquals(Optional.empty(), internal);
  }
}
