/*
 * Copyright 2026 Red Hat, Inc.
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

import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;
import io.vertx.ext.dropwizard.MetricsService;
import io.vertx.test.core.LinuxOrOsx;
import io.vertx.test.tls.Cert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(LinuxOrOsx.class)
public class QuicMetricsTest extends MetricsTestBase {

  @Test
  public void testSimple() {
    QuicServer server = QuicServer.create(vertx, new QuicServerConfig(), new ServerSSLOptions()
      .setKeyCertOptions(Cert.SERVER_JKS.get())
      .setApplicationLayerProtocols(List.of("test-protocol")));
    server.handler(connection -> {
      connection.handler(stream -> {

      });
    });
    server.listen(1234, "localhost").await();

    QuicClient client = QuicClient.create(vertx, new QuicClientConfig(), new ClientSSLOptions()
      .setTrustAll(true).setApplicationLayerProtocols(List.of("test-protocol")));
    MetricsService metricsService = MetricsService.create(vertx);
    QuicConnection connection = client.connect(SocketAddress.inetSocketAddress(1234, "localhost")).await();
    QuicStream stream = connection.openStream().await();
    stream.write("hello").await();
    assertWaitUntil(() -> metricsService.getMetricsSnapshot(client).getJsonObject("open-streams").getInteger("count") == 1);
    stream.close().await();
    assertWaitUntil(() -> metricsService.getMetricsSnapshot(client).getJsonObject("open-connections").getInteger("count") == 1);
    assertWaitUntil(() -> metricsService.getMetricsSnapshot(client).getJsonObject("bytes-written").getInteger("count") == 5);
    connection.close().await();
    assertWaitUntil(() -> metricsService.getMetricsSnapshot(client).getJsonObject("open-connections").getInteger("count") == 0);
  }

}
