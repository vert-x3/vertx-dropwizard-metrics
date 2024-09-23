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

import com.codahale.metrics.Timer;
import com.codahale.metrics.*;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.http.*;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.Measured;
import io.vertx.core.net.*;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.ext.dropwizard.*;
import io.vertx.ext.dropwizard.impl.AbstractMetrics;
import io.vertx.ext.dropwizard.impl.Helper;
import io.vertx.test.core.RepeatRule;
import io.vertx.test.core.TestUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.vertx.test.core.TestUtils.randomBuffer;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
                addMonitoredEventBusHandler(new Match().setValue("user:.*").setType(MatchType.REGEX).setAlias("user-handlers")).
                addMonitoredHttpServerUri(new Match().setValue("/get")).
                addMonitoredHttpServerUri(new Match().setValue("/p.*").setType(MatchType.REGEX)).
                addMonitoredHttpServerUri(new Match().setValue("/users/.*").setAlias("users").setType(MatchType.REGEX)).
                addMonitoredHttpServerRoute(new Match().setValue(".*").setType(MatchType.REGEX)).
                addMonitoredHttpClientEndpoint(new Match().setValue("localhost:8080")).
                addMonitoredHttpClientUri(new Match().setValue("/books/.*").setAlias("books").setType(MatchType.REGEX))
        );
  }

  @Rule
  public RepeatRule repeatRule = new RepeatRule();

  @Test
  public void testHttpMetrics() throws Exception {
    String uri = "/foo/bar";
    Buffer serverMin = randomBuffer(500);
    Buffer serverMax = randomBuffer(1000);
    Buffer clientMax = randomBuffer(300);
    Buffer clientMin = randomBuffer(100);

    int requests = 10;
    AtomicLong expected = new AtomicLong();
    CountDownLatch latch = new CountDownLatch(requests);
    HttpClientAgent client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      expected.incrementAndGet();
      if (expected.get() % 2 == 0) {
        req.response().end(serverMin);
      } else {
        req.response().end(serverMax);
      }
    });
    server.listen().onComplete(ar -> {
      for (int i = 0; i < requests; i++) {
        Buffer body = i % 2 == 0 ? clientMax : clientMin;
        client.request(HttpMethod.POST, 8080, "localhost", uri).onComplete(onSuccess(req -> {
          req.send(body).onComplete(onSuccess(resp -> {
            // Note, we countdown in the *endHandler* of the resp, as the request metric count is not incremented
            // until *after* the response handler has been called
            resp.endHandler(v -> latch.countDown());
          }));
        }));
      }
    });

    awaitLatch(latch);
    assertEquals(requests, expected.get());

    // Verify http server
    assertCount(() -> metricsService.getMetricsSnapshot(server).getJsonObject("requests"), (long) requests); // requests
    assertNotNull(metricsService.getMetricsSnapshot(server).getJsonObject("requests").getValue("oneSecondRate"));

    assertCount(() -> metricsService.getMetricsSnapshot(server).getJsonObject("bytes-written"), 7500L);
    assertCount(() -> metricsService.getMetricsSnapshot(server).getJsonObject("bytes-read"), 2000L);
    assertCount(() -> metricsService.getMetricsSnapshot(server).getJsonObject("exceptions"), 0L);

    // Verify http client
    assertCount(() -> metricsService.getMetricsSnapshot(client).getJsonObject("requests"), (long) requests); // requests
    assertCount(() -> metricsService.getMetricsSnapshot(client).getJsonObject("bytes-written"), 2000L);
    assertCount(() -> metricsService.getMetricsSnapshot(client).getJsonObject("bytes-read"), 7500L);
    assertCount(() -> metricsService.getMetricsSnapshot(client).getJsonObject("exceptions"), 0L);

    cleanup(client);
    cleanup(server);
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
    CountDownLatch latch = new CountDownLatch(1);

    HttpClientAgent client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().setChunked(true);
      for (int i = 0; i < chunks; i++) {
        int size = random.nextInt(max - min) + min;
        serverWrittenBytes.addAndGet(size);
        req.response().write(randomBuffer(size));
      }
      req.response().end();
    });
    server.listen().onComplete(onSuccess(s -> {
      client
        .request(HttpMethod.GET, 8080, "localhost", uri)
        .onComplete(onSuccess(req -> {
          req.response().onComplete(onSuccess(resp -> {
            // Note, we call testComplete() in the *endHandler* of the resp, as the request metric count is not incremented
            // until *after* the response handler has been called
            resp.endHandler(v1 -> vertx.runOnContext(v2 -> latch.countDown()));
          }));
          req.setChunked(true);
          for (int i = 0; i < chunks; i++) {
            int size = random.nextInt(max - min) + min;
            clientWrittenBytes.addAndGet(size);
            req.write(randomBuffer(size));
          }
          req.end();
        }));
    }));

    awaitLatch(latch);

    // Gather metrics
    JsonObject metrics = metricsService.getMetricsSnapshot(server);

    // Verify http server
    assertCount(metrics.getJsonObject("requests"), 1L); // requests
    assertCount(metrics.getJsonObject("bytes-written"), serverWrittenBytes.get());
    assertCount(metrics.getJsonObject("bytes-read"), clientWrittenBytes.get());
    assertCount(metrics.getJsonObject("exceptions"), 0L);

    // Verify http client
    metrics = metricsService.getMetricsSnapshot(client);
    assertCount(metrics.getJsonObject("requests"), 1L); // requests
    assertCount(metrics.getJsonObject("bytes-written"), clientWrittenBytes.get());
    assertCount(metrics.getJsonObject("bytes-read"), serverWrittenBytes.get());
    assertCount(metrics.getJsonObject("exceptions"), 0L);

    cleanup(client);
    cleanup(server);
  }

  @Test
  public void testHttpMethodAndUriAndRouteMetrics() throws Exception {
    int requests = 13;
    CountDownLatch latch = new CountDownLatch(requests);

    HttpClient client = vertx.createHttpClient(new HttpClientOptions());

    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      String uri = req.uri();
      if (uri.startsWith("/users/")) {
        req.routed("/users/:userId");
      }
      if (uri.startsWith("/internal/users/")) {
        // mimic subrouting behaviour
        req.routed("/internal");
        req.routed("/users/:userId");
      }
      req.response().end();
    });
    server.listen().onComplete(onSuccess(ar -> {
      client.request(HttpMethod.GET, 8080, "localhost", "/get").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.GET, 8080, "localhost", "/users/1").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.GET, 8080, "localhost", "/users/2").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.GET, 8080, "localhost", "/users/3").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.GET, 8080, "localhost", "/internal/users/3").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.POST, 8080, "localhost", "/post").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.PUT, 8080, "localhost", "/put").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.DELETE, 8080, "localhost", "/delete").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.OPTIONS, 8080, "localhost", "/options").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.HEAD, 8080, "localhost", "/head").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.TRACE, 8080, "localhost", "/trace").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.CONNECT, 8080, "localhost", "/connect").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
      client.request(HttpMethod.PATCH, 8080, "localhost", "/patch").onSuccess(req -> req.send().onSuccess(resp -> latch.countDown()));
    }));

    awaitLatch(latch);

    // This allows the metrics to be captured before we gather them
    vertx.setTimer(100, id -> {
      // Gather metrics
      JsonObject metrics = metricsService.getMetricsSnapshot(server);
      assertCount(metrics.getJsonObject("get-requests"), 5L);
      assertCount(metrics.getJsonObject("get-requests./get"), 1L);
      assertCount(metrics.getJsonObject("get-requests.users"), 3L);
      assertCount(metrics.getJsonObject("get-requests./users/:userId"), 3L);
      assertCount(metrics.getJsonObject("get-requests./internal>/users/:userId"), 1L);
      assertCount(metrics.getJsonObject("responses-2xx./get"), 1L);
      assertCount(metrics.getJsonObject("responses-2xx.users"), 3L);
      assertCount(metrics.getJsonObject("responses-2xx./users/:userId"), 3L);
      assertCount(metrics.getJsonObject("responses-2xx./internal>/users/:userId"), 1L);

      assertNull(metrics.getJsonObject("get-requests./users/1"));
      assertNull(metrics.getJsonObject("get-requests./users/2"));
      assertNull(metrics.getJsonObject("get-requests./users/3"));
      assertNull(metrics.getJsonObject("responses-2xx.users/1"));
      assertNull(metrics.getJsonObject("responses-2xx.users/2"));
      assertNull(metrics.getJsonObject("responses-2xx.users/3"));
      assertCount(metrics.getJsonObject("post-requests"), 1L);
      assertCount(metrics.getJsonObject("post-requests./post"), 1L);
      assertCount(metrics.getJsonObject("responses-2xx./post"), 1L);
      assertCount(metrics.getJsonObject("put-requests"), 1L);
      assertCount(metrics.getJsonObject("put-requests./put"), 1L);
      assertCount(metrics.getJsonObject("responses-2xx./put"), 1L);
      assertCount(metrics.getJsonObject("delete-requests"), 1L);
      assertNull(metrics.getJsonObject("delete-requests./delete"));
      assertNull(metrics.getJsonObject("responses-2xx./delete"));
      assertCount(metrics.getJsonObject("options-requests"), 1L);
      assertNull(metrics.getJsonObject("options-requests./options"));
      assertNull(metrics.getJsonObject("responses-2xx./options"));
      assertCount(metrics.getJsonObject("head-requests"), 1L);
      assertNull(metrics.getJsonObject("head-requests./head"));
      assertNull(metrics.getJsonObject("responses-2xx./head"));
      assertCount(metrics.getJsonObject("trace-requests"), 1L);
      assertNull(metrics.getJsonObject("trace-requests./trace"));
      assertNull(metrics.getJsonObject("responses-2xx./trace"));
      assertCount(metrics.getJsonObject("connect-requests"), 1L);
      assertNull(metrics.getJsonObject("connect-requests./connect"));
      assertNull(metrics.getJsonObject("responses-2xx./connect"));
      assertCount(metrics.getJsonObject("patch-requests"), 1L);
      assertCount(metrics.getJsonObject("patch-requests./patch"), 1L);
      assertCount(metrics.getJsonObject("responses-2xx./patch"), 1L);
      testComplete();
    });

    await();

    cleanup(client);
    cleanup(server);
  }

  @Test
  public void testHttpMetricsResponseCode2xx() throws Exception {
    test(200, "responses-2xx");
  }

  @Test
  public void testHttpMetricsResponseCode3xx() throws Exception {
    test(300, "responses-3xx");
  }

  @Test
  public void testHttpMetricsResponseCode4xx() throws Exception {
    test(404, "responses-4xx");
  }

  @Test
  public void testHttpMetricsResponseCode5xx() throws Exception {
    test(500, "responses-5xx");
  }

  private void test(int code, String metricName) throws Exception {
    CountDownLatch closeLatch = new CountDownLatch(2);
    HttpClientAgent client = vertx
      .httpClientBuilder()
      .withConnectHandler(connection -> {
        connection.closeHandler(v -> closeLatch.countDown());
      })
      .build();
    CountDownLatch listenLatch = new CountDownLatch(1);
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().setStatusCode(code).end();
    }).connectionHandler(connection -> {
      connection.closeHandler(v -> closeLatch.countDown());
    });
    server.listen().onComplete(onSuccess(v -> listenLatch.countDown()));
    awaitLatch(listenLatch);
    for (Measured measured : Arrays.asList(server)) {
      assertWaitUntil(() -> metricsService.getMetricsSnapshot(measured) != null);
      JsonObject metric = metricsService.getMetricsSnapshot(measured);
      JsonObject metrics = metric.getJsonObject(metricName);
      assertNotNull("Was expecting " + metricName + " to be not null", metrics);
      assertEquals("Was expecting " + metricName + " to have count = 0", 0, (int) metrics.getInteger("count"));
    }
    client.request(HttpMethod.GET, 8080, "localhost", "/").onComplete(onSuccess(req -> {
      req.send().onComplete(onSuccess(resp -> {

      }));
    }));
    for (Measured measured : Arrays.asList(client)) {
      waitUntil(() -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(measured);
        if (metrics == null) {
          return false;
        }
        JsonObject metric = metrics.getJsonObject(metricName);
        if (metric == null) {
          return false;
        }
        Integer count = metric.getInteger("count");
        return count != null && count == 1;
      });
    }
    client.close();
    awaitLatch(closeLatch);
  }

  @Test
  public void testHttpMetricsOnClose() throws Exception {
    int requests = 6;
    CountDownLatch latch = new CountDownLatch(requests);

    HttpClientAgent client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8081)).requestHandler(req -> {
      req.response().end();
    });
    server.listen().onComplete(ar -> {
      assertTrue(ar.succeeded());
      for (int i = 0; i < requests; i++) {
        client.request(HttpMethod.GET, 8081, "localhost", "/some/uri").onComplete(onSuccess(req -> {
          req.send().onComplete(onSuccess(resp -> {
            latch.countDown();
          }));
        }));
      }
    });

    awaitLatch(latch);

    CountDownLatch closeLatch = new CountDownLatch(1);
    client.close();
    server.close().onComplete(onSuccess(v1 -> {
      vertx.runOnContext(v2 -> closeLatch.countDown());
    }));

    awaitLatch(closeLatch);

    assertWaitUntil(() -> {
      JsonObject metrics = metricsService.getMetricsSnapshot(server);
      return metrics != null && metrics.isEmpty();
    });
    assertWaitUntil(() -> metricsService.getMetricsSnapshot(client).getJsonObject("connections.max-pool-size") == null);
  }

  @Test
  public void testHttpWebSocketMetrics() throws Exception {
    Buffer serverMin = randomBuffer(500);
    Buffer serverMax = randomBuffer(1000);
    Buffer clientMax = randomBuffer(300);
    Buffer clientMin = randomBuffer(100);

    CountDownLatch done = new CountDownLatch(1);

    AtomicBoolean sendMax = new AtomicBoolean(false);
    WebSocketClient client = vertx.createWebSocketClient();
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080));
    server.webSocketHandler(socket -> {
      socket.accept();
      JsonObject metrics = metricsService.getMetricsSnapshot(server);
      assertEquals(1, (int) metrics.getJsonObject("open-websockets").getInteger("count"));
      socket.handler(buff -> {
        if (sendMax.getAndSet(!sendMax.get())) {
          socket.write(serverMax);
        } else {
          socket.write(serverMin);
        }
      });
    });
    server.listen().onComplete(onSuccess(v1 -> {
      AtomicBoolean complete = new AtomicBoolean(false);
      client.connect(8080, "localhost", "/blah").onComplete(onSuccess(socket -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(client);
        assertEquals(1, (int) metrics.getJsonObject("open-websockets").getInteger("count"));
        socket.write(clientMax);
        socket.handler(buff -> {
          if (!complete.getAndSet(true)) {
            socket.write(clientMin);
          } else {
            socket.closeHandler(v2 -> {
              done.countDown();
            });
            socket.close();
          }
        });
      }));
    }));

    awaitLatch(done);

    JsonObject serverMetrics = metricsService.getMetricsSnapshot(server);
    assertEquals(0, (int) serverMetrics.getJsonObject("open-websockets").getInteger("count"));
    assertCount(serverMetrics.getJsonObject("bytes-written"), 1502L);
    // 3 frames : 2 data + 1 close frame (2 bytes)
    assertCount(serverMetrics.getJsonObject("bytes-read"), 402L);

    assertEquals(0, (int) metricsService.getMetricsSnapshot(client).getJsonObject("open-websockets").getInteger("count"));
    assertCount(() -> metricsService.getMetricsSnapshot(client).getJsonObject("bytes-written"), 402L);
    assertCount(() -> metricsService.getMetricsSnapshot(client).getJsonObject("bytes-read"), 1502L);

    cleanup(client);
    cleanup(server);
  }

  @Test
  public void testHttpSendFile() throws Exception {
    Buffer content = randomBuffer(10000);
    File file = new File(testDir, "send-file-metrics");
    file.deleteOnExit();
    Files.write(file.toPath(), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    CountDownLatch latch = new CountDownLatch(1);
    HttpClientAgent client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().sendFile(file.getAbsolutePath());
    });
    server.listen().onComplete(onSuccess(s -> {
      client.request(HttpMethod.GET, 8080, "localhost", "/file")
        .compose(req -> req.send().compose(HttpClientResponse::body))
        .onComplete(onSuccess(buff -> {
          vertx.runOnContext(v -> {
            latch.countDown();
          });
        }));
    }));

    awaitLatch(latch);

    JsonObject metrics = metricsService.getMetricsSnapshot(server);
    assertCount(metrics.getJsonObject("bytes-written"), content.length());

    metrics = metricsService.getMetricsSnapshot(client);
    assertCount(metrics.getJsonObject("bytes-read"), content.length());

    cleanup(client);
    cleanup(server);
  }

  @Test
  public void testHttpClientMetricsName() throws Exception {
    String name = TestUtils.randomAlphaString(10);
    HttpClientAgent namedClient = vertx.createHttpClient(new HttpClientOptions().setMetricsName(name));
    assertEquals(AbstractMetrics.unwrap(namedClient).baseName(), "vertx.http.clients." + name);
    cleanup(namedClient);

    HttpClientAgent unnamedClient = vertx.createHttpClient();
    assertEquals(AbstractMetrics.unwrap(unnamedClient).baseName(), "vertx.http.clients");
    cleanup(unnamedClient);
  }

  @Test
  public void testNamedHttpClientMetrics() throws Exception {
    String name = TestUtils.randomAlphaString(10);
    HttpClient client = vertx.createHttpClient(new HttpClientOptions().setMetricsName(name));
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().end();
    });
    server.listen().onComplete(onSuccess(v -> {
      client.request(HttpMethod.GET, 8080, "localhost", "/file")
        .compose(req -> req.send().compose(HttpClientResponse::body))
        .onComplete(onSuccess(buff -> {
          testComplete();
        }));
    }));

    await();

    String baseName = "vertx.http.clients." + name;
    JsonObject metrics = metricsService.getMetricsSnapshot(baseName);
    assertTrue(metrics.size() > 0);
    assertCount(metrics.getJsonObject(baseName + ".bytes-read"), 0L);

    cleanup(client);

    assertWaitUntil(() -> metricsService.getMetricsSnapshot(baseName).size() == 0);

    cleanup(server);
  }

  @Test
  public void testHttpClientMetricsWithMatchIdentifier() throws Exception {
    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8080)).requestHandler(req -> {
      req.response().end();
    });
    server.listen().onComplete(onSuccess(v -> {
      client.request(HttpMethod.GET, 8080, "localhost", "/books/1")
        .compose(req -> req.send().compose(HttpClientResponse::body))
        .compose(body1 -> client
          .request(HttpMethod.GET, 8080, "localhost", "/books/2").compose(req ->
            req.send().compose(HttpClientResponse::body))
        ).onComplete(onSuccess(body2 -> {
        testComplete();
      }));
    }));

    await();

    String baseName = "vertx.http.clients";
    JsonObject metrics = metricsService.getMetricsSnapshot(baseName);
    assertCount(metrics.getJsonObject(baseName + ".get-requests.books"), 2L);
    assertNull(metrics.getJsonObject(baseName + ".get-requests./books/1"));
    assertNull(metrics.getJsonObject(baseName + ".get-requests./books/2"));

    cleanup(client);
    cleanup(server);
  }

  @Test
  public void testHttpClientMetricsWithMatchEndpoint() throws Exception {
    HttpClient client = vertx.createHttpClient(new HttpClientOptions());
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setHost("localhost").setPort(8081)).requestHandler(req -> {
      req.response().end();
    });
    server
      .listen()
      .compose(s -> client
        .request(HttpMethod.GET, 8081, "localhost", "/")
        .compose(req -> req
          .send()
          .compose(HttpClientResponse::body))
        .onComplete(onSuccess(body2 -> {
        testComplete();
      })));

    await();

    String baseName = "vertx.pools.http";
    JsonObject metrics = metricsService.getMetricsSnapshot(baseName);
    Assert.assertEquals(0, metrics.size());

    cleanup(client);
    cleanup(server);
  }

  @Test
  public void testNetMetrics() throws Exception {
    Buffer serverData = randomBuffer(500);
    Buffer clientData = randomBuffer(300);
    int numRequests = 13;
    AtomicLong expected = new AtomicLong();
    AtomicInteger num = new AtomicInteger();
    CountDownLatch latch = new CountDownLatch(2);
    AtomicInteger actualPort = new AtomicInteger();
    AtomicReference<NetClient> clientRef = new AtomicReference<>();

    NetServer server = vertx.createNetServer(new NetServerOptions().setHost("localhost")).connectHandler(socket -> {
      socket.handler(buff -> {
        assertEquals(300, buff.length());
        socket.write(serverData);
      });
      socket.closeHandler(v -> latch.countDown());
    });
    server.listen().onComplete(onSuccess(s -> {
      actualPort.set(s.actualPort());
      NetClient client = vertx.createNetClient(new NetClientOptions());
      clientRef.set(client);
      client.connect(actualPort.get(), "localhost").onComplete(onSuccess(socket -> {
        AtomicInteger count = new AtomicInteger(numRequests);
        socket.handler(buff -> {
          assertEquals(500, buff.length());
          if (count.decrementAndGet() == 0) {
            socket.close();
          } else {
            socket.write(clientData);
          }
        });
        socket.closeHandler(v -> latch.countDown());
        socket.write(clientData);
      }));
    }));

    awaitLatch(latch);

    // Verify net server
    JsonObject metrics = metricsService.getMetricsSnapshot(server);
    assertCount(metrics.getJsonObject("bytes-written"), numRequests * serverData.length());
    assertCount(metrics.getJsonObject("bytes-read"), numRequests * clientData.length());

    // Verify net client
    metrics = metricsService.getMetricsSnapshot(clientRef.get());
    assertCount(metrics.getJsonObject("bytes-written"), numRequests * clientData.length());
    assertCount(metrics.getJsonObject("bytes-read"), numRequests * serverData.length());

    cleanup(clientRef.get());
    cleanup(server);
  }

  @Test
  public void testNamedNetClientMetrics() throws Exception {
    CountDownLatch latch = new CountDownLatch(2);
    String name = TestUtils.randomAlphaString(10);
    NetClient client = vertx.createNetClient(new NetClientOptions().setMetricsName(name));
    NetServer server = vertx.createNetServer(new NetServerOptions().setHost("localhost").setPort(8080)).connectHandler(socket -> {
      socket.closeHandler(v -> latch.countDown());
      socket.handler(socket::write);
    });
    server.listen().onComplete(onSuccess(v1 -> {
      client.connect(8080, "localhost").onComplete(onSuccess(socket -> {
        socket.closeHandler(v2 -> latch.countDown());
        socket.handler(buff -> {
          socket.close();
        });
        socket.write("whatever");
      }));
    }));

    awaitLatch(latch);

    String baseName = "vertx.net.clients." + name;
    JsonObject metrics = metricsService.getMetricsSnapshot(baseName);
    assertCount(metrics.getJsonObject(baseName + ".bytes-written"), 8L);

    cleanup(client);
    cleanup(server);
  }

  @Test
  public void testNetMetricsOnClose() throws Exception {
    int requests = 8;
    CountDownLatch latch = new CountDownLatch(requests);

    NetClient client = vertx.createNetClient(new NetClientOptions());
    NetServer server = vertx.createNetServer(new NetServerOptions().setHost("localhost").setPort(1235).setReceiveBufferSize(50)).connectHandler(socket -> {
      socket.handler(buff -> latch.countDown());
    });
    server.listen().onComplete(onSuccess(v1 -> {
      client.connect(1235, "localhost").onComplete(onSuccess(so -> {
        for (int i = 0; i < requests; i++) {
          so.write(randomBuffer(50));
        }
      }));
    }));

    awaitLatch(latch);

    CountDownLatch closeLatch = new CountDownLatch(2);
    client.close().onComplete(onSuccess(v1 -> {
      vertx.runOnContext(v2 -> closeLatch.countDown());
    }));
    server.close().onComplete(onSuccess(v1 -> {
      vertx.runOnContext(v2 -> closeLatch.countDown());
    }));
    awaitLatch(closeLatch);

    JsonObject metrics = metricsService.getMetricsSnapshot(server);
    assertNotNull(metrics);
    assertTrue(metrics.isEmpty());

    metrics = metricsService.getMetricsSnapshot(client);
    assertNotNull(metrics);
    assertTrue(metrics.isEmpty());

    cleanup(client);
    cleanup(server);
  }

  @Test
  public void testDatagramMetrics() throws Exception {
    Buffer clientMax = randomBuffer(1823);
    Buffer clientMin = randomBuffer(123);

    AtomicBoolean complete = new AtomicBoolean(false);
    DatagramSocket datagramSocket = vertx.createDatagramSocket(new DatagramSocketOptions());
    datagramSocket.listen(1236, "localhost").onComplete(onSuccess(socket -> {
      socket.handler(packet -> {
        if (complete.getAndSet(true)) {
          testComplete();
        }
      });
      socket.send(clientMin, 1236, "localhost").onComplete(onSuccess(v -> {
      }));
      socket.send(clientMax, 1236, "localhost").onComplete(onSuccess(v -> {
      }));
    }));

    await();

    // Test sender/client (bytes-written)
    JsonObject metrics = metricsService.getMetricsSnapshot(datagramSocket);
    assertCount(metrics.getJsonObject("bytes-written"), 2L);
    assertMinMax(metrics.getJsonObject("bytes-written"), (long) clientMin.length(), (long) clientMax.length());

    // Test server (bytes-read)
    assertCount(metrics.getJsonObject("localhost:1236.bytes-read"), 2L);
    assertMinMax(metrics.getJsonObject("localhost:1236.bytes-read"), (long) clientMin.length(), (long) clientMax.length());

    CountDownLatch latch = new CountDownLatch(1);
    datagramSocket.close().onComplete(onSuccess(v -> {
      latch.countDown();
    }));
    awaitLatch(latch);
    assertWaitUntil(() -> metricsService.getMetricsSnapshot(datagramSocket).isEmpty());
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

    CountDownLatch gate = new CountDownLatch(1);
    CountDownLatch allLatch = new CountDownLatch((int) messages);
    AtomicReference<String> deploymentID = new AtomicReference<>();

    class TheVerticle extends AbstractVerticle {

      MessageConsumer<Object> consumer;

      @Override
      public void start() {
        consumer = vertx.eventBus().consumer("foo").handler(msg -> {
          vertx.runOnContext(v -> allLatch.countDown());
        });
        consumer.pause();
      }

      @Override
      public void stop() {
        consumer.unregister();
      }
    }

    TheVerticle verticle = new TheVerticle();
    vertx.deployVerticle(verticle).onComplete(onSuccess(id -> {
      deploymentID.set(id);
      for (int i = 0; i < messages; i++) {
        vertx.eventBus().send("foo", "Hello");
      }
    }));

    // Wait until we have piled the (n-1) messages on the event loop
    assertWaitUntil(() -> {
      JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
      JsonObject pending = metrics.getJsonObject("messages.pending-local");
      int count = pending.getInteger("count");
      if (count == messages) {
        int messagesPending = metrics.getJsonObject("messages.pending").getInteger("count");
        assertEquals("Was expecting to have at least " + (messages - 1) + " pending messages: " + metrics, messagesPending, messages);
        assertEquals(0, (int) metrics.getJsonObject("messages.pending-remote").getInteger("count"));
        return true;
      } else {
        return false;
      }
    });

    // Open the gate
    verticle.consumer.resume();

    // Wait until all messages have been processed
    awaitLatch(allLatch);

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
    vertx.undeploy(deploymentID.get()).onComplete(onSuccess(v -> {
      undeployLatch.countDown();
    }));
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
  public void testEventBusMetricsHandlerRegexMatchWithIdentifier() throws Exception {

    CountDownLatch allHandlersLatch = new CountDownLatch(3);

    vertx.eventBus().consumer("user:1", msg -> {
      vertx.runOnContext(v -> allHandlersLatch.countDown());
    });

    vertx.eventBus().consumer("user:2", msg -> {
      vertx.runOnContext(v -> allHandlersLatch.countDown());
    });
    vertx.eventBus().send("user:1", "whatever");
    vertx.eventBus().send("user:1", "whatever one more time");
    vertx.eventBus().send("user:2", "whatever");

    awaitLatch(allHandlersLatch);

    // Check global metrics
    JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
    assertCount(metrics.getJsonObject("handlers.user-handlers"), 3L);
    assertNull(metrics.getJsonObject("handlers.user:1"));
    assertNull(metrics.getJsonObject("handlers.user:2"));
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
          int counterValue = count.incrementAndGet();
          assertEquals(counterValue, (int) metric.getInteger("count"));
          if (counterValue == size) {
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
    vertx.eventBus().request("foo", "bar", new DeliveryOptions().setSendTimeout(300)).onComplete(onFailure(err -> {
      testComplete();
    }));

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
    }).completion().onComplete(onSuccess(v -> {
      Buffer buffer = Buffer.buffer(new byte[1000]);
      vertices[0].eventBus().send("the_address", buffer);
    }));
  }

  @Test
  public void testEventBusMetricsReplyTimeout() {
    vertx.eventBus().consumer("foo").handler(msg -> {});

    vertx.eventBus().request("foo", "bar", new DeliveryOptions().setSendTimeout(300)).onComplete(ar -> {
      assertTrue(ar.failed());
    });

    assertCount(() -> metricsService.getMetricsSnapshot(vertx.eventBus()).getJsonObject("messages.reply-failures"), 1L);
    assertCount(() -> metricsService.getMetricsSnapshot(vertx.eventBus()).getJsonObject("messages.reply-failures." + ReplyFailure.TIMEOUT), 1L);
  }

  @Test
  public void testEventBusMetricsReplyRecipientFailure() {
    vertx.eventBus().consumer("foo").handler(msg -> msg.fail(1, "blah"));

    vertx.eventBus().request("foo", "bar", new DeliveryOptions()).onComplete(onFailure(err -> {
    }));

    assertCount(() -> metricsService.getMetricsSnapshot(vertx.eventBus()).getJsonObject("messages.reply-failures"), 1L);
    assertCount(() -> metricsService.getMetricsSnapshot(vertx.eventBus()).getJsonObject("messages.reply-failures." + ReplyFailure.RECIPIENT_FAILURE), 1L);
  }

  @Test
  public void testPendingCount() {
    Context ctx = vertx.getOrCreateContext();
    ctx.runOnContext(v -> {
      EventBus eb = vertx.eventBus();
      MessageConsumer<Object> consumer = eb.consumer("foo");
      consumer.handler(msg -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
        assertCount(metrics.getJsonObject("messages.pending"), 0L);
        assertCount(metrics.getJsonObject("messages.pending-local"), 0L);
        testComplete();
      });
      consumer.pause();
      eb.request("foo", "the_message", new DeliveryOptions().setSendTimeout(30)).onComplete(onFailure(err -> {
        JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
        assertCount(metrics.getJsonObject("messages.pending"), 1L);
        assertCount(metrics.getJsonObject("messages.pending-local"), 1L);
        consumer.resume();
      }));
    });
    await();
  }

  @Test
  public void testDiscardMessage() {
    int num = 10;
    EventBus eb = vertx.eventBus();
    MessageConsumer<Object> consumer = eb.consumer("foo");
    consumer.setMaxBufferedMessages(num);
    consumer.pause();
    consumer.handler(msg -> {
      fail("should not be called");
    });
    for (int i = 0;i < num;i++) {
      eb.send("foo", "the_message-" + i);
    }
    eb.send("foo", "last");
    waitUntil(() -> {
      JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
      return getCount(metrics.getJsonObject("messages.discarded")) == 1L;
    });
    assertCount(metricsService.getMetricsSnapshot(vertx.eventBus()).getJsonObject("messages.pending"), 10L);
    consumer.unregister();
    waitUntil(() -> {
      JsonObject metrics = metricsService.getMetricsSnapshot(vertx.eventBus());
      return getCount(metrics.getJsonObject("messages.discarded")) == 11L;
    });
    assertCount(metricsService.getMetricsSnapshot(vertx.eventBus()).getJsonObject("messages.pending"), 0L);
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
  public void testScheduledMetricConsumer() {
    int messages = 18;
    AtomicInteger count = new AtomicInteger(messages);
    String baseName = metricsService.getBaseName(vertx.eventBus());

    ScheduledMetricsConsumer consumer = new ScheduledMetricsConsumer(vertx).
        filter((name, metric) -> name.startsWith(baseName));

    consumer.start(300, MILLISECONDS, (name, metric) -> {
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
    server.listen().onComplete(onSuccess(res -> {
      latch1.countDown();
    }));
    awaitLatch(latch1);
    HttpClientAgent client = vertx.createHttpClient(new HttpClientOptions());
    CountDownLatch latch2 = new CountDownLatch(1);
    NetServer nServer = vertx.createNetServer(new NetServerOptions().setPort(1234));
    nServer.connectHandler(conn -> {});
    nServer.listen().onComplete(res -> {
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
    vertx.close().onComplete(res -> {
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
      server.listen(8080).onComplete(onSuccess(v -> {
        listenLatch.countDown();
      }));
      servers.add(server);
    }
    awaitLatch(listenLatch);
    HttpClient client = vertx.createHttpClient();
    BitSet bs = new BitSet();
    for (int i = 0;i < size;i++) {
      client.request(HttpMethod.GET, 8080, "localhost", "/").onComplete(onSuccess(req -> {
        req.send().onComplete(onSuccess(resp -> {
          assertEquals(200, resp.statusCode());
          int id = Integer.parseInt(resp.getHeader("id"));
          synchronized (bs) {
            bs.set(id);
          }
        }));
      }));
    }
    assertWaitUntil(() -> bs.cardinality() == 3);
    for (HttpServer server : servers) {
      JsonObject metrics = metricsService.getMetricsSnapshot(server);
      assertEquals(3, (int)metrics.getJsonObject("requests").getInteger("count"));
    }
  }

  @Test
  public void testHttpClientConnectionQueue() throws Exception {
    CountDownLatch started = new CountDownLatch(1);
    List<Runnable> requests = new ArrayList<>();
    vertx.createHttpServer().requestHandler(req -> {
      Context ctx = vertx.getOrCreateContext();
      requests.add(() -> {
        ctx.runOnContext(v -> {
          req.response().end();
        });
      });
    }).listen(8080, "localhost").onComplete(onSuccess(v -> {
      started.countDown();
    }));
    awaitLatch(started);
    HttpClientAgent client = vertx.createHttpClient();
    for (int i = 0;i < 7;i++) {
      client.request(HttpMethod.GET, 8080, "localhost", "/somepath").compose(HttpClientRequest::send);
    }
    assertWaitUntil(() -> requests.size() == 5);
    JsonObject metrics = metricsService.getMetricsSnapshot("vertx.pools.http");
    assertEquals(2, (int)metrics.getJsonObject("vertx.pools.http.localhost:8080.queue-size").getInteger("count"));
    assertEquals(5, (int)metrics.getJsonObject("vertx.pools.http.localhost:8080.queue-delay").getInteger("count"));
    List<Runnable> todo = new ArrayList<>(requests);
    requests.clear();
    todo.forEach(Runnable::run);
    assertWaitUntil(() -> requests.size() == 2);
    metrics = metricsService.getMetricsSnapshot("vertx.pools.http");
    assertEquals(0, (int)metrics.getJsonObject("vertx.pools.http.localhost:8080.queue-size").getInteger("count"));
    assertEquals(7, (int)metrics.getJsonObject("vertx.pools.http.localhost:8080.queue-delay").getInteger("count"));
  }

  @Test
  public void testMultiHttpClients() throws Exception {
    int size = 3;
    CountDownLatch requestsLatch = new CountDownLatch(size);
    CountDownLatch started = new CountDownLatch(1);
    List<Runnable> requests = Collections.synchronizedList(new ArrayList<>());
    vertx.createHttpServer().requestHandler(req -> {
      requests.add(() -> req.response().end());
      requestsLatch.countDown();
    }).listen(8080, "localhost").onComplete(onSuccess(v -> {
      started.countDown();
    }));
    awaitLatch(started);
    HttpClientAgent[] clients = new HttpClientAgent[size];
    CountDownLatch closedLatch = new CountDownLatch(size);
    CountDownLatch responseLatch = new CountDownLatch(size);
    for (int i = 0;i < size;i++) {
      clients[i] = vertx.httpClientBuilder()
        .withConnectHandler(conn -> {
          conn.closeHandler(v -> {
            vertx.runOnContext(clv -> closedLatch.countDown());
          });
        })
        .build();
      clients[i].request(HttpMethod.GET, 8080, "localhost", "/").onComplete(onSuccess(req -> {
        req.send().onComplete(onSuccess(resp -> {
          vertx.runOnContext(rlv -> {
            responseLatch.countDown();
          });
        }));
      }));
    }
    awaitLatch(requestsLatch);
    JsonObject metrics = metricsService.getMetricsSnapshot(clients[0]);
    assertEquals(0, (int)metrics.getJsonObject("endpoint.localhost:8080.usage").getInteger("count"));
    assertEquals(3, (int)metrics.getJsonObject("endpoint.localhost:8080.in-use").getInteger("count"));
    assertEquals(3, (int)metrics.getJsonObject("endpoint.localhost:8080.open-netsockets").getInteger("count"));
    assertEquals(0, (int)metrics.getJsonObject("endpoint.localhost:8080.ttfb").getInteger("count"));
    assertEquals(3, (int)metrics.getJsonObject("connections.max-pool-size").getInteger("value"));
    requests.forEach(Runnable::run);
    awaitLatch(responseLatch);
    metrics = metricsService.getMetricsSnapshot(clients[0]);
    assertEquals(3, (int)metrics.getJsonObject("endpoint.localhost:8080.usage").getInteger("count"));
    assertEquals(0, (int)metrics.getJsonObject("endpoint.localhost:8080.in-use").getInteger("count"));
    assertEquals(3, (int)metrics.getJsonObject("endpoint.localhost:8080.open-netsockets").getInteger("count"));
    assertEquals(3, (int)metrics.getJsonObject("endpoint.localhost:8080.ttfb").getInteger("count"));
    assertEquals(3, (int)metrics.getJsonObject("connections.max-pool-size").getInteger("value"));
    clients[2].close();
    Consumer<Integer> waiter = expected -> {
      assertWaitUntil(() -> metricsService.getMetricsSnapshot(clients[0]).getJsonObject("connections.max-pool-size").getInteger("value").equals(expected));
    };
    waiter.accept(2);
    metrics = metricsService.getMetricsSnapshot(clients[0]);
    assertEquals(3, (int)metrics.getJsonObject("endpoint.localhost:8080.usage").getInteger("count"));
    assertEquals(0, (int)metrics.getJsonObject("endpoint.localhost:8080.in-use").getInteger("count"));
    assertEquals(3, (int)metrics.getJsonObject("endpoint.localhost:8080.ttfb").getInteger("count"));
    clients[1].close();
    waiter.accept(1);
    metrics = metricsService.getMetricsSnapshot(clients[0]);
    assertEquals(3, (int)metrics.getJsonObject("endpoint.localhost:8080.usage").getInteger("count"));
    assertEquals(0, (int)metrics.getJsonObject("endpoint.localhost:8080.in-use").getInteger("count"));
    assertEquals(3, (int)metrics.getJsonObject("endpoint.localhost:8080.ttfb").getInteger("count"));
    CountDownLatch latch = new CountDownLatch(1);
    clients[0].close().onComplete(ar -> latch.countDown());
    awaitLatch(latch);
    metrics = metricsService.getMetricsSnapshot(clients[0]);
    assertNull(metrics.getJsonObject("endpoint.localhost:8080.usage"));
    assertNull(metrics.getJsonObject("endpoint.localhost:8080.in-use"));
    assertNull(metrics.getJsonObject("endpoint.localhost:8080.open-netsockets"));
    assertNull(metrics.getJsonObject("endpoint.localhost:8080.ttfb"));
    assertNull(metrics.getJsonObject("connections.max-pool-size"));
  }

  private void assertCount(JsonObject metric, long expected) {
    Long actual = getCount(metric);
    assertNotNull(actual);
    String name = metric.getString("name");
    assertEquals(name + " (count)", expected, (long)actual);
  }

  private void assertCount(Supplier<JsonObject> supplier, long expected) {
    assertWaitUntil(() -> {
      JsonObject metric = supplier.get();
      Long actual = getCount(metric);
      return actual != null && actual == expected;
    });
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
    assertMetricType(expectedType, Helper.convertMetric(metric, MILLISECONDS, MILLISECONDS));
  }

  private void assertMetricType(String expectedType, JsonObject metric) {
    assertEquals(expectedType, metric.getString("type"));
  }

  @Test
  public void testThreadPoolMetrics() throws Exception {

    int size = 5;
    CountDownLatch done = new CountDownLatch(6);

    WorkerExecutor exec = vertx.createSharedWorkerExecutor("the-executor", size);
    JsonObject metrics = metricsService.getMetricsSnapshot(exec);

    assertMetricType("counter", metrics.getJsonObject("queue-size"));
    assertMetricType("timer", metrics.getJsonObject("queue-delay"));
    assertMetricType("counter", metrics.getJsonObject("in-use"));
    assertMetricType("timer", metrics.getJsonObject("usage"));
    assertMetricType("gauge", metrics.getJsonObject("pool-ratio"));
    assertMetricType("gauge", metrics.getJsonObject("max-pool-size"));

    assertCount(metrics.getJsonObject("usage"), 0);
    assertCount(metrics.getJsonObject("queue-delay"), 0);
    assertCount(metrics.getJsonObject("queue-size"), 0);
    assertCount(metrics.getJsonObject("in-use"), 0);
    assertEquals(metrics.getJsonObject("pool-ratio").getDouble("value"), (Double)0D);
    assertEquals(metrics.getJsonObject("max-pool-size").getInteger("value"), (Integer)5);

    //
    CountDownLatch gate = new CountDownLatch(1);
    CountDownLatch latch = new CountDownLatch(5);
    for (int i = 0; i < size;i++) {
      exec.<Boolean>executeBlocking(() -> {
        latch.countDown();
        return gate.await(10, TimeUnit.SECONDS);
      }, false).onComplete(onSuccess(res -> {
        assertTrue(res);
        vertx.runOnContext(v -> done.countDown());
      }));
    }

    awaitLatch(latch);
    metrics = metricsService.getMetricsSnapshot(exec);
    assertCount(metrics.getJsonObject("usage"), 0);
    assertCount(metrics.getJsonObject("queue-delay"), 5);
    assertCount(metrics.getJsonObject("queue-size"), 0);
    assertCount(metrics.getJsonObject("in-use"), size);
    assertEquals(metrics.getJsonObject("pool-ratio").getDouble("value"), (Double)1D);

    exec.executeBlocking(() -> null, false).onComplete(ar -> vertx.runOnContext(v -> done.countDown()));
    metrics = metricsService.getMetricsSnapshot(exec);
    assertCount(metrics.getJsonObject("usage"), 0);
    assertCount(metrics.getJsonObject("queue-delay"), 5);
    assertCount(metrics.getJsonObject("queue-size"), 1);
    assertCount(metrics.getJsonObject("in-use"), size);
    assertEquals(metrics.getJsonObject("pool-ratio").getDouble("value"), (Double)1D);

    gate.countDown();
    awaitLatch(done);
    metrics = metricsService.getMetricsSnapshot(exec);
    assertCount(metrics.getJsonObject("usage"), 6);
    assertCount(metrics.getJsonObject("queue-delay"), 6);
    assertCount(metrics.getJsonObject("queue-size"), 0);
    assertCount(metrics.getJsonObject("in-use"), 0);
    assertEquals(metrics.getJsonObject("pool-ratio").getDouble("value"), (Double)0D);
  }

  @Test
  public void testThreadPoolMetricsOnClose() throws Exception {
    WorkerExecutor exec = vertx.createSharedWorkerExecutor("the-executor", 10);
    assertTrue(metricsService.getMetricsSnapshot(exec).size() > 0);
    assertTrue(metricsService.getMetricsSnapshot("vertx.pools.worker.vert.x-worker-thread").size() > 0);
    assertTrue(metricsService.getMetricsSnapshot("vertx.pools.worker.vert.x-internal-blocking").size() > 0);
    exec.close();
    assertTrue(metricsService.getMetricsSnapshot(exec).size() == 0);
    assertTrue(metricsService.getMetricsSnapshot("vertx.pools.worker.vert.x-worker-thread").size() > 0);
    assertTrue(metricsService.getMetricsSnapshot("vertx.pools.worker.vert.x-internal-blocking").size() > 0);
    CountDownLatch latch = new CountDownLatch(1);
    vertx.close().onComplete(ar -> latch.countDown());
    awaitLatch(latch);
    assertEquals(new JsonObject(), metricsService.getMetricsSnapshot("vertx.pools.worker.vert.x-worker-thread"));
    assertEquals(new JsonObject(), metricsService.getMetricsSnapshot("vertx.pools.worker.vert.x-internal-blocking"));
  }

  @Test
  public void testClientMetricsReporting() {
    SocketAddress address = SocketAddress.inetSocketAddress(8080, "localhost");
    ClientMetrics metrics = ((VertxInternal) vertx).metricsSPI().createClientMetrics(address, "backend", null);

    // Request
    Object request = new Object();
    Object response = new Object();
    Object requestMetric = metrics.requestBegin("some-uri", request);
    metrics.requestEnd(requestMetric);
    metrics.responseBegin(requestMetric, response);
    JsonObject snapshot = metricsService.getMetricsSnapshot("vertx.backend.clients.localhost:8080");
    assertEquals(0, (int)snapshot.getJsonObject("vertx.backend.clients.localhost:8080.requests").getInteger("count"));
    metrics.responseEnd(requestMetric);
    snapshot = metricsService.getMetricsSnapshot("vertx.backend.clients.localhost:8080");
    assertEquals(1, (int)snapshot.getJsonObject("vertx.backend.clients.localhost:8080.requests").getInteger("count"));
  }

  @Test
  public void testClientMetricsLifecycle() {
    VertxMetrics spi = ((VertxInternal) vertx).metricsSPI();
    SocketAddress address = SocketAddress.inetSocketAddress(8080, "localhost");
    ClientMetrics[] metrics = new ClientMetrics[2];
    for (int i = 0;i < metrics.length;i++) {
      metrics[i] = spi.createClientMetrics(address, "backend", "acme");
    }
    JsonObject snapshot = metricsService.getMetricsSnapshot("vertx.backend.clients.acme.localhost:8080");
    assertTrue(snapshot.size() > 0);
    metrics[0].close();
    snapshot = metricsService.getMetricsSnapshot("vertx.backend.clients.acme.localhost:8080");
    assertTrue(snapshot.size() > 0);
    metrics[1].close();
    snapshot = metricsService.getMetricsSnapshot("vertx.backend.clients.acme.localhost:8080");
    assertTrue(snapshot.size() == 0);
  }
}
