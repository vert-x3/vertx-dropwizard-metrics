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

package io.vertx.ext.metrics;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CountDownLatch;

/**
 *
 * Make sure things still work when metrics is not enabled
 *
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class MetricsTestBase extends VertxTestBase {

  protected void cleanup(HttpServer server, HttpClient client) throws Exception {
    if (client != null) {
      client.close();
    }
    CountDownLatch latch = new CountDownLatch(1);
    if (server != null) {
      server.close(ar -> {
        latch.countDown();
      });
    }
    awaitLatch(latch);
  }

  protected void cleanup(NetServer server, NetClient client) throws Exception {
    if (client != null) {
      client.close();
    }
    CountDownLatch latch = new CountDownLatch(1);
    if (server != null) {
      server.close(ar -> {
        latch.countDown();
      });
    }
    awaitLatch(latch);
  }

}
