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

import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.vertx.test.core.TestUtils.randomBuffer;

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
  public void testWebsocketDontProduceEventBusMetrics() throws Exception {
    Buffer serverMin = randomBuffer(500);
    Buffer serverMax = randomBuffer(1000);
    Buffer clientMax = randomBuffer(300);
    Buffer clientMin = randomBuffer(100);

    AtomicBoolean sendMax = new AtomicBoolean(false);
    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080));
    server.websocketHandler(socket -> {
      JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
      assertNoInternal(metrics);
      socket.handler(buff -> {
        if (sendMax.getAndSet(!sendMax.get())) {
          socket.write(serverMax);
        } else {
          socket.write(serverMin);
        }
      });
    }).listen(ar -> {
      assertTrue(ar.succeeded());
      AtomicBoolean complete = new AtomicBoolean(false);
      client.webSocket(8080, "localhost", "/blah", onSuccess(socket -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
        assertNoInternal(metrics);
        socket.write(clientMax);
        socket.handler(buff -> {
          if (!complete.getAndSet(true)) {
            socket.write(clientMin);
          } else {
            socket.closeHandler(done -> {
              testComplete();
            });
            socket.close();
          }
        });
      }));
    });

    await();

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
