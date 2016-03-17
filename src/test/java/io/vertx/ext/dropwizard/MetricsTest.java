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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.dropwizard.impl.Helper;
import io.vertx.test.core.RepeatRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.test.core.TestUtils.*;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class MetricsTest extends MetricsTestBase {

  private File testDir;
  private MetricsService metricsService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    testDir = Files.createTempDirectory("vertx-test").toFile();
    testDir.deleteOnExit();
    metricsService = MetricsService.create(vertx);
  }

  @Override
  protected VertxOptions getOptions() {
    return new VertxOptions().
        setMetricsOptions(
            new DropwizardMetricsOptions().
                setEnabled(true).
                setJmxEnabled(true).
                addMonitoredEventBusHandler(new Match().setValue("foo")).
                addMonitoredEventBusHandler(new Match().setValue("juu.*").setType(MatchType.REGEX)).
                addMonitoredHttpServerUri(new Match().setValue("/get")).
                addMonitoredHttpServerUri(new Match().setValue("/p.*").setType(MatchType.REGEX))
        );
  }

  @Rule
  public RepeatRule repeatRule = new RepeatRule();

  @Test
  //@Repeat(times = 10)
  public void testHttpMetrics() throws Exception {
    String uri = "/foo/bar";
    Buffer serverMin = randomBuffer(500);
    Buffer serverMax = randomBuffer(1000);
    Buffer clientMax = randomBuffer(300);
    Buffer clientMin = randomBuffer(100);

    int requests = 10;
    AtomicLong expected = new AtomicLong();
    CountDownLatch latch = new CountDownLatch(requests);
    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      expected.incrementAndGet();
      if (expected.get() % 2 == 0) {
        req.response().end(serverMin);
      } else {
        req.response().end(serverMax);
      }
    }).listen(ar -> {
      if (ar.succeeded()) {
        for (int i = 0; i < requests; i++) {
          HttpClientRequest req = client.request(HttpMethod.GET, 8080, "localhost", uri, resp -> {
            // Note, we countdown in the *endHandler* of the resp, as the request metric count is not incremented
            // until *after* the response handler has been called
            resp.endHandler(v -> latch.countDown());
          });
          if (i % 2 == 0) {
            req.end(clientMax);
          } else {
            req.end(clientMin);
          }
        }
      } else {
        fail(ar.cause().getMessage());
      }
    });

    awaitLatch(latch);
    assertEquals(requests, expected.get());

    // Verify http server
    JsonObject metrics = metricsService.getMetricsSnapshot(server);
    assertCount(metrics.getJsonObject("requests"), (long) requests); // requests
    assertNotNull(metrics.getJsonObject("requests").getValue("oneSecondRate"));

    assertMinMax(metrics.getJsonObject("bytes-written"), (long) serverMin.length(), (long) serverMax.length());
    assertMinMax(metrics.getJsonObject("bytes-read"), (long) clientMin.length(), (long) clientMax.length());
    assertCount(metrics.getJsonObject("exceptions"), 0L);

    // Verify http client
    metrics = metricsService.getMetricsSnapshot(client);
    assertCount(metrics.getJsonObject("requests"), (long) requests); // requests
    assertMinMax(metrics.getJsonObject("bytes-written"), (long) clientMin.length(), (long) clientMax.length());
    assertMinMax(metrics.getJsonObject("bytes-read"), (long) serverMin.length(), (long) serverMax.length());
    assertCount(metrics.getJsonObject("exceptions"), 0L);

    cleanup(server, client);
  }

  @Test
  public void testHttpChunkWritesMetrics() throws Exception {
    String uri = "/foo";
    int chunks = 10;
    int max = 1000;
    int min = 50;
    AtomicLong serverWrittenBytes = new AtomicLong();
    AtomicLong clientWrittenBytes = new AtomicLong();
    Random random = new Random();

    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().setChunked(true);
      for (int i = 0; i < chunks; i++) {
        int size = random.nextInt(max - min) + min;
        serverWrittenBytes.addAndGet(size);
        req.response().write(randomBuffer(size));
      }
      req.response().end();
    }).listen(ar -> {
      if (ar.succeeded()) {
        HttpClientRequest req = client.request(HttpMethod.GET, 8080, "localhost", uri, resp -> {
          // Note, we call testComplete() in the *endHandler* of the resp, as the request metric count is not incremented
          // until *after* the response handler has been called
          resp.endHandler(v -> testComplete());
        });
        req.setChunked(true);

        for (int i = 0; i < chunks; i++) {
          int size = random.nextInt(max - min) + min;
          clientWrittenBytes.addAndGet(size);
          req.write(randomBuffer(size));
        }
        req.end();
      } else {
        fail(ar.cause().getMessage());
      }
    });

    await();

    // Gather metrics
    JsonObject metrics = metricsService.getMetricsSnapshot(server);

    // Verify http server
    assertCount(metrics.getJsonObject("requests"), 1L); // requests
    assertMinMax(metrics.getJsonObject("bytes-written"), serverWrittenBytes.get(), serverWrittenBytes.get());
    assertMinMax(metrics.getJsonObject("bytes-read"), clientWrittenBytes.get(), clientWrittenBytes.get());
    assertCount(metrics.getJsonObject("exceptions"), 0L);

    // Verify http client
    metrics = metricsService.getMetricsSnapshot(client);
    assertCount(metrics.getJsonObject("requests"), 1L); // requests
    assertMinMax(metrics.getJsonObject("bytes-written"), clientWrittenBytes.get(), clientWrittenBytes.get());
    assertMinMax(metrics.getJsonObject("bytes-read"), serverWrittenBytes.get(), serverWrittenBytes.get());
    assertCount(metrics.getJsonObject("exceptions"), 0L);

    cleanup(server, client);
  }

  @Test
  public void testHttpMethodAndUriMetrics() throws Exception {
    int requests = 4;
    CountDownLatch latch = new CountDownLatch(requests);

    HttpClient client = vertx.createHttpClient(new HttpClientOptions());

    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().end();
    }).listen(ar -> {
      assertTrue(ar.succeeded());
      client.request(HttpMethod.GET, 8080, "localhost", "/get", resp -> latch.countDown()).end();
      client.request(HttpMethod.POST, 8080, "localhost", "/post", resp -> latch.countDown()).end();
      client.request(HttpMethod.PUT, 8080, "localhost", "/put", resp -> latch.countDown()).end();
      client.request(HttpMethod.DELETE, 8080, "localhost", "/delete", resp -> latch.countDown()).end();
      client.request(HttpMethod.OPTIONS, 8080, "localhost", "/options", resp -> latch.countDown()).end();
      client.request(HttpMethod.HEAD, 8080, "localhost", "/head", resp -> latch.countDown()).end();
      client.request(HttpMethod.TRACE, 8080, "localhost", "/trace", resp -> latch.countDown()).end();
      client.request(HttpMethod.CONNECT, 8080, "localhost", "/connect", resp -> latch.countDown()).end();
      client.request(HttpMethod.PATCH, 8080, "localhost", "/patch", resp -> latch.countDown()).end();
    });

    awaitLatch(latch);

    // This allows the metrics to be captured before we gather them
    vertx.setTimer(100, id -> {
      // Gather metrics
      JsonObject metrics = metricsService.getMetricsSnapshot(server);
      assertCount(metrics.getJsonObject("get-requests"), 1L);
      assertCount(metrics.getJsonObject("get-requests./get"), 1L);
      assertCount(metrics.getJsonObject("post-requests"), 1L);
      assertCount(metrics.getJsonObject("post-requests./post"), 1L);
      assertCount(metrics.getJsonObject("put-requests"), 1L);
      assertCount(metrics.getJsonObject("put-requests./put"), 1L);
      assertCount(metrics.getJsonObject("delete-requests"), 1L);
      assertNull(metrics.getJsonObject("delete-requests./delete"));
      assertCount(metrics.getJsonObject("options-requests"), 1L);
      assertNull(metrics.getJsonObject("options-requests./options"));
      assertCount(metrics.getJsonObject("head-requests"), 1L);
      assertNull(metrics.getJsonObject("head-requests./head"));
      assertCount(metrics.getJsonObject("trace-requests"), 1L);
      assertNull(metrics.getJsonObject("trace-requests./trace"));
      assertCount(metrics.getJsonObject("connect-requests"), 1L);
      assertNull(metrics.getJsonObject("connect-requests./connect"));
      assertCount(metrics.getJsonObject("patch-requests"), 1L);
      assertCount(metrics.getJsonObject("patch-requests./patch"), 1L);
      testComplete();
    });

    await();

    cleanup(server, client);
  }

  @Test
  public void testHttpMetricsResponseCode() throws Exception {
    test(200, "responses-2xx");
    test(300, "responses-3xx");
    test(404, "responses-4xx");
    test(500, "responses-5xx");

  }

  private void test(int code, String metricName) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    Handler<HttpServer> doTest = (server) -> {
      for (Measured measured : Arrays.asList(client, server)) {
        JsonObject metric = metricsService.getMetricsSnapshot(measured);
        JsonObject metrics = metric.getJsonObject(metricName);
        assertNotNull("Was expecting " + metricName + " to be not null", metrics);
        assertEquals("Was expecting " + metricName + " to have count = 0", 0, (int) metrics.getInteger("count"));
      }
      client.request(HttpMethod.GET, 8080, "localhost", "/", resp -> {
        vertx.runOnContext(v -> {
          for (Measured measured : Arrays.asList(client, server)) {
            JsonObject metric = metricsService.getMetricsSnapshot(measured);
            assertEquals(1, (int) metric.getJsonObject(metricName).getInteger("count"));
          }
          latch.countDown();
        });
      }).end();
    };
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().setStatusCode(code).end();
    }).listen(ar -> {
      assertTrue(ar.succeeded());
      doTest.handle(ar.result());
    });
    awaitLatch(latch);
    cleanup(server, client);
  }

  @Test
  public void testHttpMetricsOnClose() throws Exception {
    int requests = 6;
    CountDownLatch latch = new CountDownLatch(requests);

    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8081)).requestHandler(req -> {
      req.response().end();
    }).listen(ar -> {
      assertTrue(ar.succeeded());
      for (int i = 0; i < requests; i++) {
        client.request(HttpMethod.GET, 8081, "localhost", "/some/uri", resp -> {
          latch.countDown();
        }).end();
      }
    });

    awaitLatch(latch);

    client.close();
    server.close(ar -> {
      assertTrue(ar.succeeded());
      testComplete();
    });

    await();

    JsonObject metrics = metricsService.getMetricsSnapshot(server);
    assertNotNull(metrics);
    assertTrue(metrics.isEmpty());

    metrics = metricsService.getMetricsSnapshot(client);
    assertNotNull(metrics);
    assertEquals(0, (int)metrics.getJsonObject("connections.max-pool-size").getInteger("value"));
  }

  @Test
  public void testHttpWebsocketMetrics() throws Exception {
    Buffer serverMin = randomBuffer(500);
    Buffer serverMax = randomBuffer(1000);
    Buffer clientMax = randomBuffer(300);
    Buffer clientMin = randomBuffer(100);

    AtomicBoolean sendMax = new AtomicBoolean(false);
    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080));
    server.websocketHandler(socket -> {
      JsonObject metrics = metricsService.getMetricsSnapshot(server);
      assertEquals(1, (int) metrics.getJsonObject("open-websockets").getInteger("count"));
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
      client.websocket(8080, "localhost", "/blah", socket -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(client);
        assertEquals(1, (int) metrics.getJsonObject("open-websockets").getInteger("count"));
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
      });
    });

    await();

    JsonObject metrics = metricsService.getMetricsSnapshot(server);
    assertEquals(0, (int) metrics.getJsonObject("open-websockets").getInteger("count"));
    String name = "bytes-written";
    assertCount(metrics.getJsonObject(name), 2L);
    assertMinMax(metrics.getJsonObject(name), (long) serverMin.length(), (long) serverMax.length());
    name = "bytes-read";
    assertCount(metrics.getJsonObject(name), 2L);
    assertMinMax(metrics.getJsonObject(name), (long) clientMin.length(), (long) clientMax.length());

    metrics = metricsService.getMetricsSnapshot(client);
    assertEquals(0, (int) metrics.getJsonObject("open-websockets").getInteger("count"));
    name = "bytes-written";
    assertCount(metrics.getJsonObject(name), 2L);
    assertMinMax(metrics.getJsonObject(name), (long) clientMin.length(), (long) clientMax.length());
    name = "bytes-read";
    assertCount(metrics.getJsonObject(name), 2L);
    assertMinMax(metrics.getJsonObject(name), (long) serverMin.length(), (long) serverMax.length());


    cleanup(server, client);
  }

  @Test
  public void testHttpSendFile() throws Exception {
    Buffer content = randomBuffer(10000);
    File file = new File(testDir, "send-file-metrics");
    file.deleteOnExit();
    Files.write(file.toPath(), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().sendFile(file.getAbsolutePath());
    }).listen(ar -> {
      assertTrue(ar.succeeded());
      client.request(HttpMethod.GET, 8080, "localhost", "/file", resp -> {
        resp.bodyHandler(buff -> {
          testComplete();
        });
      }).end();
    });

    await();

    JsonObject metrics = metricsService.getMetricsSnapshot(server);
    assertCount(metrics.getJsonObject("bytes-written"), 1L);
    assertMinMax(metrics.getJsonObject("bytes-written"), (long) content.length(), (long) content.length());

    metrics = metricsService.getMetricsSnapshot(client);
    assertCount(metrics.getJsonObject("bytes-read"), 1L);
    assertMinMax(metrics.getJsonObject("bytes-read"), (long) content.length(), (long) content.length());

    cleanup(server, client);
  }

  @Test
  public void testNetMetrics() throws Exception {
    Buffer serverData = randomBuffer(500);
    Buffer clientData = randomBuffer(300);
    int requests = 13;
    AtomicLong expected = new AtomicLong();
    CountDownLatch latch = new CountDownLatch(requests);
    AtomicInteger actualPort = new AtomicInteger();
    AtomicReference<NetClient> clientRef = new AtomicReference<>();

    NetServer server = vertx.createNetServer(new NetServerOptions().setHost("localhost")).connectHandler(socket -> {
      socket.handler(buff -> {
        expected.incrementAndGet();
        socket.write(serverData);
      });
    }).listen(ar -> {
      assertTrue(ar.succeeded());
      actualPort.set(ar.result().actualPort());
      clientRef.set(vertx.createNetClient(new NetClientOptions()).connect(actualPort.get(), "localhost", ar2 -> {
        assertTrue(ar2.succeeded());
        NetSocket socket = ar2.result();
        socket.handler(buff -> {
          latch.countDown();
          if (latch.getCount() != 0) {
            socket.write(clientData);
          }
        });
        socket.write(clientData);
      }));
    });

    awaitLatch(latch);
    assertEquals(requests, expected.get());

    // Verify net server
    JsonObject metrics = metricsService.getMetricsSnapshot(server);
    assertMinMax(metrics.getJsonObject("bytes-written"), (long) serverData.length(), (long) serverData.length());
    assertMinMax(metrics.getJsonObject("bytes-read"), (long) clientData.length(), (long) clientData.length());

    // Verify net client
    metrics = metricsService.getMetricsSnapshot(clientRef.get());
    assertMinMax(metrics.getJsonObject("bytes-written"), (long) clientData.length(), (long) clientData.length());
    assertMinMax(metrics.getJsonObject("bytes-read"), (long) serverData.length(), (long) serverData.length());

    cleanup(server, clientRef.get());
  }

  @Test
  public void testNetMetricsOnClose() throws Exception {
    int requests = 8;
    CountDownLatch latch = new CountDownLatch(requests);

    NetClient client = vertx.createNetClient(new NetClientOptions());
    NetServer server = vertx.createNetServer(new NetServerOptions().setHost("localhost").setPort(1235).setReceiveBufferSize(50)).connectHandler(socket -> {
      socket.handler(buff -> latch.countDown());
    }).listen(ar -> {
      assertTrue(ar.succeeded());
      client.connect(1235, "localhost", ar2 -> {
        assertTrue(ar2.succeeded());
        for (int i = 0; i < requests; i++) {
          ar2.result().write(randomBuffer(50));
        }
      });
    });

    awaitLatch(latch);

    client.close();
    server.close(ar -> {
      assertTrue(ar.succeeded());
      testComplete();
    });

    await();

    JsonObject metrics = metricsService.getMetricsSnapshot(server);
    assertNotNull(metrics);
    assertTrue(metrics.isEmpty());

    metrics = metricsService.getMetricsSnapshot(client);
    assertNotNull(metrics);
    assertTrue(metrics.isEmpty());

    cleanup(server, client);
  }

  @Test
  public void testDatagramMetrics() throws Exception {
    Buffer clientMax = randomBuffer(1823);
    Buffer clientMin = randomBuffer(123);

    AtomicBoolean complete = new AtomicBoolean(false);
    DatagramSocket datagramSocket = vertx.createDatagramSocket(new DatagramSocketOptions()).listen(1236, "localhost", ar -> {
      assertTrue(ar.succeeded());
      DatagramSocket socket = ar.result();
      socket.handler(packet -> {
        if (complete.getAndSet(true)) {
          testComplete();
        }
      });
      socket.send(clientMin, 1236, "localhost", ds -> {
        assertTrue(ar.succeeded());
      });
      socket.send(clientMax, 1236, "localhost", ds -> {
        assertTrue(ar.succeeded());
      });
    });

    await();

    // Test sender/client (bytes-written)
    JsonObject metrics = metricsService.getMetricsSnapshot(datagramSocket);
    assertCount(metrics.getJsonObject("bytes-written"), 2L);
    assertMinMax(metrics.getJsonObject("bytes-written"), (long) clientMin.length(), (long) clientMax.length());

    // Test server (bytes-read)
    assertCount(metrics.getJsonObject("localhost:1236.bytes-read"), 2L);
    assertMinMax(metrics.getJsonObject("localhost:1236.bytes-read"), (long) clientMin.length(), (long) clientMax.length());

    CountDownLatch latch = new CountDownLatch(1);
    datagramSocket.close(ar -> {
      assertTrue(ar.succeeded());
      JsonObject dMetrics = metricsService.getMetricsSnapshot(datagramSocket);
      assertTrue(dMetrics.isEmpty());
      latch.countDown();
    });

    awaitLatch(latch);
  }

  @Test
  public void testEventBusMetricsWithoutHandler() {
    long send = 12;
    for (int i = 0; i < send; i++) {
      vertx.eventBus().send("foo", "Hello");
    }
    long pub = 7;
    for (int i = 0; i < pub; i++) {
      vertx.eventBus().publish("foo", "Hello");
    }

    JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
    assertCount(metrics.getJsonObject("messages.sent"), send);
    assertCount(metrics.getJsonObject("messages.published"), pub);
    assertCount(metrics.getJsonObject("messages.received"), 19L);
    assertCount(metrics.getJsonObject("messages.delivered"), 0L);
  }

  @Test
  public void testEventBusMetricsWithHandler() throws Exception {
    long messages = 13;

    CountDownLatch latch = new CountDownLatch((int) messages);
    AtomicReference<String> deploymentID = new AtomicReference<>();

    vertx.deployVerticle(new AbstractVerticle() {

      MessageConsumer<Object> consumer;

      @Override
      public void start() throws Exception {
        consumer = vertx.eventBus().consumer("foo").handler(msg -> {
          if (latch.getCount() == 13) {
            // Wait until we have piled the 12 messages on the event loop
            waitUntil(() -> {
              JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
              JsonObject pending = metrics.getJsonObject("messages.pending-local");
              int count = pending.getInteger("count");
              if (count == 12) {
                int messagesPending = metrics.getJsonObject("messages.pending").getInteger("count");
                assertTrue("Was expecting to have at least 12 pending messages: " + metrics, messagesPending >= 12);
                assertEquals(0, (int) metrics.getJsonObject("messages.pending-remote").getInteger("count"));
                return true;
              } else {
                return false;
              }
            });
          }
          latch.countDown();
        });
      }

      @Override
      public void stop() throws Exception {
        consumer.unregister();
      }
    }, ar -> {
      assertTrue(ar.succeeded());
      deploymentID.set(ar.result());
      for (int i = 0; i < messages; i++) {
        vertx.eventBus().send("foo", "Hello");
      }
    });

    // Wait until all messages have been processed
    awaitLatch(latch);

    // Check global metrics
    JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
    assertCount(metrics.getJsonObject("messages.sent"), messages);
    assertTroughput(metrics.getJsonObject("messages.sent"), 1, messages);
    assertCount(metrics.getJsonObject("messages.sent-local"), messages);
    assertTroughput(metrics.getJsonObject("messages.sent-local"), 1, messages);
    assertCount(metrics.getJsonObject("messages.sent-remote"), 0);
    assertTroughput(metrics.getJsonObject("messages.sent-remote"), 0, 0);
    assertCount(metrics.getJsonObject("messages.published"), 0L);
    assertTroughput(metrics.getJsonObject("messages.published"), 0, 0);
    assertCount(metrics.getJsonObject("messages.published-local"), 0);
    assertTroughput(metrics.getJsonObject("messages.published-local"), 0, 0);
    assertCount(metrics.getJsonObject("messages.published-remote"), 0);
    assertTroughput(metrics.getJsonObject("messages.published-remote"), 0, 0);
    assertCount(metrics.getJsonObject("messages.received"), messages);
    assertTroughput(metrics.getJsonObject("messages.received"), 1, messages);
    assertCount(metrics.getJsonObject("messages.received-local"), messages);
    assertTroughput(metrics.getJsonObject("messages.received-local"), 1, messages);
    assertCount(metrics.getJsonObject("messages.received-remote"), 0);
    assertTroughput(metrics.getJsonObject("messages.received-remote"), 0, 0);
    assertCount(metrics.getJsonObject("messages.delivered"), messages);
    assertTroughput(metrics.getJsonObject("messages.delivered"), 1, messages);
    assertCount(metrics.getJsonObject("messages.delivered-local"), messages);
    assertTroughput(metrics.getJsonObject("messages.delivered-local"), 1, messages);
    assertCount(metrics.getJsonObject("messages.delivered-remote"), 0);
    assertTroughput(metrics.getJsonObject("messages.delivered-remote"), 0, 0);
    assertCount(metrics.getJsonObject("messages.pending"), 0);
    assertCount(metrics.getJsonObject("messages.pending-local"), 0);
    assertCount(metrics.getJsonObject("messages.pending-remote"), 0);

    // Check handler metric
    JsonObject handlerMetric = metrics.getJsonObject("handlers.foo");
    assertNotNull(handlerMetric);
    assertEquals(messages, (int)handlerMetric.getInteger("count"));

    // Undeploy
    CountDownLatch undeployLatch = new CountDownLatch(1);
    vertx.undeploy(deploymentID.get(), ar -> {
      undeployLatch.countDown();
    });
    awaitLatch(undeployLatch);

    // Check cleanup
    metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
    handlerMetric = metrics.getJsonObject("handlers.foo");
    assertNull(handlerMetric);
  }

  @Test
  public void testEventBusMetricsHandlerExactMatch() {
    vertx.eventBus().consumer("foo", msg -> {
      vertx.runOnContext(done -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
        JsonObject metric = metrics.getJsonObject("handlers.foo");
        assertEquals(1, (int) metric.getInteger("count"));
        testComplete();
      });
    });
    vertx.eventBus().send("foo", "whatever");
    await();
  }

  @Test
  public void testEventBusMetricsHandlerNoMatch() {
    vertx.eventBus().consumer("bar", msg -> {
      vertx.runOnContext(done -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
        assertNull(metrics.getJsonObject("handlers.bar"));
        testComplete();
      });
    });
    vertx.eventBus().send("bar", "whatever");
    await();
  }

  @Test
  public void testEventBusMetricsHandlerRegexMatch() {
    vertx.eventBus().consumer("juu1234", msg -> {
      vertx.runOnContext(done -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
        JsonObject metric = metrics.getJsonObject("handlers.juu1234");
        assertEquals(1, (int) metric.getInteger("count"));
        testComplete();
      });
    });
    vertx.eventBus().send("juu1234", "whatever");
    await();
  }

  @Test
  public void testEventBusMetricsHandlerMultiMatch() {
    vertx.runOnContext(v -> {
      int size = 3;
      AtomicInteger count = new AtomicInteger();
      for (int i = 0; i < size; i++) {
        vertx.eventBus().consumer("foo", msg -> {
          JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
          JsonObject metric = metrics.getJsonObject("handlers.foo");
          assertEquals(count.get(), (int) metric.getInteger("count"));
          if (count.incrementAndGet() == size) {
            vertx.runOnContext(done -> {
              JsonObject metrics2 = metricsService.getMetricsSnapshot(vertx.eventBus());
              JsonObject metric2 = metrics2.getJsonObject("handlers.foo");
              assertEquals(size, (int) metric2.getInteger("count"));
              testComplete();
            });
          }
        });
      }
      vertx.eventBus().publish("foo", "whatever");
    });
    await();
  }

  @Test
  public void testEventBusMetricsReplyNoHandlers() {
    vertx.eventBus().send("foo", "bar", new DeliveryOptions().setSendTimeout(300), ar -> {
      assertTrue(ar.failed());
      testComplete();
    });

    await();

    JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
    assertCount(metrics.getJsonObject("messages.reply-failures"), 1L);
    assertCount(metrics.getJsonObject("messages.reply-failures." + ReplyFailure.NO_HANDLERS), 1L);
  }

  @Test
  public void testEventBusByteMetrics() {
    startNodes(2);
    vertices[1].eventBus().consumer("the_address", msg -> {
      JsonObject fromMetrics = metricsService.getMetricsSnapshot(vertices[0].eventBus());
      JsonObject toMetrics = metricsService.getMetricsSnapshot(vertices[1].eventBus());
      long written = fromMetrics.getJsonObject("bytes-written").getLong("count");
      long read = toMetrics.getJsonObject("bytes-read").getLong("count");
      assertTrue("Expecting read count " + read + " > 1000", read > 1000);
      assertTrue("Expecting written count " + written + " > 1000", written > 1000);
    }).completionHandler(ar -> {
      assertTrue(ar.succeeded());
      Buffer buffer = Buffer.buffer(new byte[1000]);
      vertices[0].eventBus().send("the_address", buffer);
    });
  }

  @Test
  public void testEventBusMetricsReplyTimeout() {
    vertx.eventBus().consumer("foo").handler(msg -> {});

    vertx.eventBus().send("foo", "bar", new DeliveryOptions().setSendTimeout(300), ar -> {
      assertTrue(ar.failed());
      testComplete();
    });

    await();

    JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
    assertCount(metrics.getJsonObject("messages.reply-failures"), 1L);
    assertCount(metrics.getJsonObject("messages.reply-failures." + ReplyFailure.TIMEOUT), 1L);
  }

  @Test
  public void testEventBusMetricsReplyRecipientFailure() {
    vertx.eventBus().consumer("foo").handler(msg -> msg.fail(1, "blah"));

    vertx.eventBus().send("foo", "bar", new DeliveryOptions(), ar -> {
      assertTrue(ar.failed());
      testComplete();
    });

    await();

    JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
    assertCount(metrics.getJsonObject("messages.reply-failures"), 1L);
    assertCount(metrics.getJsonObject("messages.reply-failures." + ReplyFailure.RECIPIENT_FAILURE), 1L);
  }

  @Test
  public void testVertxMetrics() throws Exception {
    JsonObject metrics = metricsService.getMetricsSnapshot(vertx);
    assertNotNull(metrics.getJsonObject("vertx.event-loop-size"));
    assertNotNull(metrics.getJsonObject("vertx.worker-pool-size"));
    assertNull(metrics.getJsonObject("vertx.cluster-host"));
    assertNull(metrics.getJsonObject("vertx.cluster-port"));
  }

  @Test
  public void testVerticleMetrics() throws Exception {
    int verticles = 5;
    CountDownLatch latch = new CountDownLatch(verticles);
    AtomicReference<String> ref = new AtomicReference<>();
    for (int i = 0; i < 5; i++) {
      vertx.deployVerticle(new AbstractVerticle() {}, ar -> {
        assertTrue(ar.succeeded());
        ref.set(ar.result()); // just use the last deployment id to test undeploy metrics below
        latch.countDown();
      });
    }

    awaitLatch(latch);

    JsonObject metrics = metricsService.getMetricsSnapshot(vertx);
    assertNotNull(metrics);
    assertFalse(metrics.isEmpty());

    assertCount(metrics.getJsonObject("vertx.verticles"), (long) verticles);

    vertx.undeploy(ref.get(), ar -> {
      assertTrue(ar.succeeded());
      assertCount(metricsService.getMetricsSnapshot(vertx).getJsonObject("vertx.verticles"), (long) verticles - 1);
      testComplete();
    });

    await();
  }

  @Test
  public void testTimerMetrics() throws Exception {
    // Timer
    CountDownLatch latch = new CountDownLatch(1);
    vertx.setTimer(300, id -> {
      assertCount(metricsService.getMetricsSnapshot(vertx).getJsonObject("vertx.timers"), 1L);
      latch.countDown();
    });
    awaitLatch(latch);
    waitUntil(() -> getCount(metricsService.getMetricsSnapshot(vertx).getJsonObject("vertx.timers")) == 0);
    assertCount(metricsService.getMetricsSnapshot(vertx).getJsonObject("vertx.timers"), 0L);

    // Periodic
    AtomicInteger count = new AtomicInteger(3);
    vertx.setPeriodic(100, id -> {
      assertCount(metricsService.getMetricsSnapshot(vertx).getJsonObject("vertx.timers"), 1L);
      if (count.decrementAndGet() == 0) {
        vertx.cancelTimer(id);
        testComplete();
      }
    });

    await();

    assertCount(metricsService.getMetricsSnapshot(vertx).getJsonObject("vertx.timers"), 0L);
  }

  @Test
  public void testScheduledMetricConsumer() {
    int messages = 18;
    AtomicInteger count = new AtomicInteger(messages);
    String baseName = metricsService.getBaseName(vertx.eventBus());

    ScheduledMetricsConsumer consumer = new ScheduledMetricsConsumer(vertx).
        filter((name, metric) -> name.startsWith(baseName));

    consumer.start(300, TimeUnit.MILLISECONDS, (name, metric) -> {
      assertTrue(name.startsWith(baseName));
      if (count.get() == 0) {
        if (name.equals(baseName + ".messages.sent")) {
          assertCount((JsonObject) metric, (long) messages);
          testComplete();
        }
      }
    });

    for (int i = 0; i < messages; i++) {
      vertx.eventBus().send("foo", "Hello");
      count.decrementAndGet();
    }

    await();
  }
  
  @Test
  public void testMetricsCleanupedOnVertxClose() throws Exception {
    CountDownLatch latch1 = new CountDownLatch(1);
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080));
    server.requestHandler(req -> {});
    server.listen(onSuccess(res -> {
      latch1.countDown();
    }));
    awaitLatch(latch1);
    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    CountDownLatch latch2 = new CountDownLatch(1);
    NetServer nServer = vertx.createNetServer(new NetServerOptions().setPort(1234));
    nServer.connectHandler(conn -> {});
    nServer.listen(res -> {
      latch2.countDown();
    });
    awaitLatch(latch2);
    NetClient nClient = vertx.createNetClient(new NetClientOptions());
    DatagramSocket sock = vertx.createDatagramSocket(new DatagramSocketOptions());
    EventBus eb = vertx.eventBus();
    assertFalse(metricsService.getMetricsSnapshot(vertx).isEmpty());
    assertFalse(metricsService.getMetricsSnapshot(server).isEmpty());
    assertFalse(metricsService.getMetricsSnapshot(client).isEmpty());
    assertFalse(metricsService.getMetricsSnapshot(nServer).isEmpty());
    assertFalse(metricsService.getMetricsSnapshot(nClient).isEmpty());
    assertFalse(metricsService.getMetricsSnapshot(sock).isEmpty());
    assertFalse(metricsService.getMetricsSnapshot(eb).isEmpty());
    vertx.close(res -> {
      assertTrue(metricsService.getMetricsSnapshot(vertx).isEmpty());
      assertTrue(metricsService.getMetricsSnapshot(server).isEmpty());
      assertTrue(metricsService.getMetricsSnapshot(client).isEmpty());
      assertTrue(metricsService.getMetricsSnapshot(nServer).isEmpty());
      assertTrue(metricsService.getMetricsSnapshot(nClient).isEmpty());
      assertTrue(metricsService.getMetricsSnapshot(sock).isEmpty());
      assertTrue(metricsService.getMetricsSnapshot(eb).isEmpty());
      testComplete();
    });
    await();
    vertx = null;
  }

  @Test
  public void testScaleHttpServers() throws Exception {
    int size = 3;
    List<HttpServer> servers = new ArrayList<>();
    CountDownLatch listenLatch = new CountDownLatch(size);
    for (int i = 0;i < size;i++) {
      int id = i;
      HttpServer server = vertx.createHttpServer();
      server.requestHandler(req -> {
        HttpServerResponse resp = req.response();
        req.response().putHeader("id", "" + id);
        resp.end();
      });
      server.listen(8080, ar -> {
        assertTrue(ar.succeeded());
        listenLatch.countDown();
      });
      servers.add(server);
    }
    awaitLatch(listenLatch);
    HttpClient client = vertx.createHttpClient();
    BitSet bs = new BitSet();
    for (int i = 0;i < size;i++) {
      client.get(8080, "localhost", "/", resp -> {
        assertEquals(200, resp.statusCode());
        int id = Integer.parseInt(resp.getHeader("id"));
        synchronized (bs) {
          bs.set(id);
        }
      }).end();
    }
    waitUntil(() -> bs.cardinality() == 3);
    for (HttpServer server : servers) {
      JsonObject metrics = metricsService.getMetricsSnapshot(server);
      assertEquals(3, (int)metrics.getJsonObject("requests").getInteger("count"));
    }
  }

  @Test
  public void testMultiHttpClients() throws Exception {
    int size = 3;
    CountDownLatch started = new CountDownLatch(1);
    vertx.createHttpServer().requestHandler(req -> {
      req.response().end("done");
    }).listen(8080, "localhost", ar -> {
      assertTrue(ar.succeeded());
      started.countDown();
    });
    CountDownLatch requested = new CountDownLatch(size);
    awaitLatch(started);
    HttpClient[] clients = new HttpClient[size];
    for (int i = 0;i < size;i++) {
      clients[i] = vertx.createHttpClient();
      clients[i].getNow(8080, "localhost", "/", resp -> {
        assertEquals(200, resp.statusCode());
        resp.endHandler(v -> {
          requested.countDown();
        });
      });
    }
    awaitLatch(requested);
    JsonObject metrics = metricsService.getMetricsSnapshot(clients[0]);
    assertEquals(3, (int)metrics.getJsonObject("requests").getInteger("count"));
    assertEquals(15, (int)metrics.getJsonObject("connections.max-pool-size").getInteger("value"));
  }

  private void assertCount(JsonObject metric, long expected) {
    Long actual = getCount(metric);
    assertNotNull(actual);
    String name = metric.getString("name");
    assertEquals(name + " (count)", expected, (long)actual);
  }

  private void assertTroughput(JsonObject metric, double min, double max) {
    Double actual = getThroughput(metric);
    assertNotNull(actual);
    String name = metric.getString("name");
    if (actual < min) {
      fail("Was expecting throughput(" + name + ") " + actual + " >= " + min);
    }
    if (actual > max) {
      fail("Was expecting throughput(" + name + ") " + actual + " <= " + max);
    }
  }

  private Long getCount(JsonObject metric) {
    assertNotNull(metric);
    Long actual = metric.getLong("count");
    return actual;
  }

  private Double getThroughput(JsonObject metric) {
    assertNotNull(metric);
    Double actual = metric.getDouble("oneSecondRate");
    return actual;
  }

  private void assertMinMax(JsonObject metric, Long min, Long max) {
    assertNotNull(metric);
    String name = metric.getString("name");
    if (min != null) {
      assertEquals(name + " (min)", min, metric.getLong("min"));
    }
    if (max != null) {
      assertEquals(name + " (max)", max, metric.getLong("max"));
    }
  }

  @Test
  public void testJsonMetricsTypes() {
    assertMetricType("counter", new Counter());
    assertMetricType("histogram", new Histogram(new SlidingTimeWindowReservoir(10, TimeUnit.SECONDS)));
    assertMetricType("gauge", (Gauge<String>) () -> "whatever");
    assertMetricType("meter", new Meter());
    assertMetricType("timer", new Timer());
  }

  private void assertMetricType(String expectedType, Metric metric) {
    assertEquals(expectedType, Helper.convertMetric(metric, TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS).getString("type"));
  }
}
