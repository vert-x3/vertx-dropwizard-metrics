/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.dropwizard.tests;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Segismont
 */
public class MBeansTest extends MetricsTestBase {

  @Override
  protected VertxOptions getOptions() {
    DropwizardMetricsOptions metrics = new DropwizardMetricsOptions()
      .setJmxDomain("testDistinctHttpServerMBeans")
      .setEnabled(true)
      .setJmxEnabled(true);
    return super.getOptions().setMetricsOptions(metrics);
  }

  @Test
  public void testDistinctHttpServerMBeans() throws Exception {
    int port1 = 8080, port2 = 8888;
    HttpServer server1 = vertx.createHttpServer()
      .requestHandler(req -> req.response().end());
    server1.listen(port1).await(20, TimeUnit.SECONDS);
    HttpServer server2 = vertx.createHttpServer()
      .requestHandler(req -> req.response().end());
    server2.listen(port2).await(20, TimeUnit.SECONDS);

    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    assertTrue(mBeanServer.isRegistered(new ObjectName("testDistinctHttpServerMBeans", "name", "\"vertx.http.servers.0.0.0.0:" + port1 + ".requests\"")));
    assertTrue(mBeanServer.isRegistered(new ObjectName("testDistinctHttpServerMBeans", "name", "\"vertx.http.servers.0.0.0.0:" + port2 + ".requests\"")));

    cleanup(server1);
    cleanup(server2);
  }

}
