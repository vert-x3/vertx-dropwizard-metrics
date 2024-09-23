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

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 *
 * Make sure things still work when metrics is not enabled
 *
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class MetricsTestBase {

  protected Vertx vertx;

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx(getOptions());
  }

  protected VertxOptions getOptions() {
    return new VertxOptions().
      setMetricsOptions(
        new DropwizardMetricsOptions().
          addMonitoredEventBusHandler(new Match().setType(MatchType.REGEX).setValue(".*")).
          setEnabled(true).
          setJmxEnabled(true));
  }

  protected HttpServer createHttpServer() {
    return createHttpServer(new HttpServerOptions());
  }

  protected HttpServer createHttpServer(HttpServerOptions options) {
    HttpServer server = vertx.createHttpServer(options);
    return server;
  }

  protected HttpClient createHttpClient() {
    return createHttpClient(new HttpClientOptions());
  }

  protected HttpClient createHttpClient(HttpClientOptions options) {
    HttpClient client = vertx.createHttpClient(options);
    return client;
  }

  protected NetServer createNetServer() {
    return createNetServer(new HttpServerOptions());
  }

  protected NetServer createNetServer(NetServerOptions options) {
    NetServer server = vertx.createNetServer(options);
    return server;
  }

  protected NetClient createNetClient() {
    return createNetClient(new NetClientOptions());
  }

  protected NetClient createNetClient(NetClientOptions options) {
    NetClient client = vertx.createNetClient(options);
    return client;
  }

  @After
  public void tearDown() throws Exception {
    Vertx v = vertx;
    if (v != null) {
      vertx = null;
      v.close().await(20, TimeUnit.SECONDS);
    }
  }

  protected void cleanup(HttpServer server) throws Exception {
    server.close().await(20, TimeUnit.SECONDS);
  }

  protected void cleanup(HttpClient client) throws Exception {
    if (client != null) {
      client.close();
    }
  }

  protected void cleanup(WebSocketClient client) throws Exception {
    if (client != null) {
      client.close();
    }
  }

  protected void cleanup(NetServer server) throws Exception {
    server.close().await(20, TimeUnit.SECONDS);
  }

  protected void cleanup(NetClient client) throws Exception {
    if (client != null) {
      client.close();
    }
  }

  public static byte randomByte() {
    return (byte) ((int) (Math.random() * 255) - 128);
  }

  public static Buffer randomBuffer(int length) {
    return randomBuffer(length, false, (byte) 0);
  }

  public static Buffer randomBuffer(int length, boolean avoid, byte avoidByte) {
    byte[] line = randomByteArray(length, avoid, avoidByte);
    return Buffer.buffer(line);
  }

  public static byte[] randomByteArray(int length, boolean avoid, byte avoidByte) {
    byte[] line = new byte[length];
    if (avoid) {
      for (int i = 0; i < length; i++) {
        byte rand;
        do {
          rand = randomByte();
        } while (rand == avoidByte);

        line[i] = rand;
      }
    } else {
      ThreadLocalRandom.current().nextBytes(line);
    }
    return line;
  }

  public static void assertWaitUntil(BooleanSupplier supplier) {
    assertWaitUntil(supplier, 10000);
  }

  public static void waitUntil(BooleanSupplier supplier) {
    waitUntil(supplier, 10000);
  }

  public static <T> void waitUntilEquals(T value, Supplier<T> supplier) {
    waitUntil(() -> Objects.equals(value, supplier.get()), 10000);
  }

  public static void assertWaitUntil(BooleanSupplier supplier, long timeout) {
    if (!waitUntil(supplier, timeout)) {
      throw new IllegalStateException("Timed out");
    }
  }

  public static void assertWaitUntil(BooleanSupplier supplier, long timeout, String reason) {
    if (!waitUntil(supplier, timeout)) {
      throw new IllegalStateException("Timed out: " + reason);
    }
  }

  public static boolean waitUntil(BooleanSupplier supplier, long timeout) {
    long start = System.currentTimeMillis();
    while (true) {
      if (supplier.getAsBoolean()) {
        return true;
      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException ignore) {
      }
      long now = System.currentTimeMillis();
      if (now - start > timeout) {
        return false;
      }
    }
  }
}
