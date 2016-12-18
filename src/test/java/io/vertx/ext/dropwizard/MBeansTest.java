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

package io.vertx.ext.dropwizard;

import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;

/**
 * @author Thomas Segismont
 */
public class MBeansTest extends MetricsTestBase {

  @Override
  protected VertxOptions getOptions() {
    DropwizardMetricsOptions metrics = new DropwizardMetricsOptions()
      .setJmxDomain(name.getMethodName())
      .setEnabled(true)
      .setJmxEnabled(true);
    return super.getOptions().setMetricsOptions(metrics);
  }

  @Test
  public void testDistinctHttpServerMBeans() throws Exception {
    int port1 = 8080, port2 = 8888;
    CountDownLatch listenLatch = new CountDownLatch(2);
    HttpServer server1 = vertx.createHttpServer()
      .requestHandler(req -> req.response().end())
      .listen(port1, onSuccess(server -> listenLatch.countDown()));
    HttpServer server2 = vertx.createHttpServer()
      .requestHandler(req -> req.response().end())
      .listen(port2, onSuccess(server -> listenLatch.countDown()));
    awaitLatch(listenLatch);

    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    assertTrue(mBeanServer.isRegistered(new ObjectName(name.getMethodName(), "name", "\"vertx.http.servers.0.0.0.0:" + port1 + ".requests\"")));
    assertTrue(mBeanServer.isRegistered(new ObjectName(name.getMethodName(), "name", "\"vertx.http.servers.0.0.0.0:" + port2 + ".requests\"")));

    cleanup(server1);
    cleanup(server2);
  }

}
