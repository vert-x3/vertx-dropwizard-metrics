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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;
import org.junit.Test;

import java.util.Collections;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MetricsSharedRegistryTest extends MetricsTestBase {

  @Override
  protected VertxOptions getOptions() {
    return new VertxOptions().setMetricsOptions(new MetricsOptions().setEnabled(true).setName("the_name"));
  }

  @Test
  public void testRegistration() {
    assertEquals(Collections.singleton("the_name"), SharedMetricRegistries.names());
    MetricRegistry registry = SharedMetricRegistries.getOrCreate("the_name");
    assertTrue(registry.getNames().size() > 0);
  }
}
