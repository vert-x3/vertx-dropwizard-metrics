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

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.test.core.VertxTestBase;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 *
 * Make sure things still work when metrics is not enabled
 *
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class MetricsTestBase extends VertxTestBase {

  private List<Callable<Void>> toClose = new ArrayList<>();

  protected HttpServer createHttpServer() {
    return createHttpServer(new HttpServerOptions());
  }

  protected HttpServer createHttpServer(HttpServerOptions options) {
    HttpServer server = vertx.createHttpServer(options);
    toClose.add(() -> {
      CountDownLatch latch = new CountDownLatch(1);
      server.close(ar -> {
        latch.countDown();
      });
      awaitLatch(latch);
      return null;
    });
    return server;
  }

  protected HttpClient createHttpClient() {
    return createHttpClient(new HttpClientOptions());
  }

  protected HttpClient createHttpClient(HttpClientOptions options) {
    HttpClient client = vertx.createHttpClient(options);
    toClose.add(() -> {
      client.close();
      return null;
    });
    return client;
  }

  protected NetServer createNetServer() {
    return createNetServer(new HttpServerOptions());
  }

  protected NetServer createNetServer(NetServerOptions options) {
    NetServer server = vertx.createNetServer(options);
    toClose.add(() -> {
      CountDownLatch latch = new CountDownLatch(1);
      server.close(ar -> {
        latch.countDown();
      });
      awaitLatch(latch);
      return null;
    });
    return server;
  }

  protected NetClient createNetClient() {
    return createNetClient(new NetClientOptions());
  }

  protected NetClient createNetClient(NetClientOptions options) {
    NetClient client = vertx.createNetClient(options);
    toClose.add(() -> {
      CountDownLatch latch = new CountDownLatch(1);
      client.close();
      awaitLatch(latch);
      return null;
    });
    return client;
  }

  @Override
  protected void tearDown() throws Exception {
    for (Callable<Void> c : toClose) {
      c.call();
    }
    super.tearDown();
  }

  protected void cleanup(HttpServer server) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    if (server != null) {
      server.close(ar -> {
        latch.countDown();
      });
    }
    awaitLatch(latch);
  }

  protected void cleanup(HttpClient client) throws Exception {
    if (client != null) {
      client.close();
    }
  }

  protected void cleanup(NetServer server) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    if (server != null) {
      server.close(ar -> {
        latch.countDown();
      });
    }
    awaitLatch(latch);
  }

  protected void cleanup(NetClient client) throws Exception {
    if (client != null) {
      client.close();
    }
  }
}
