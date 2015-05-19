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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.vertx.core.VertxOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SharedRegistryTest extends MetricsTestBase {

  @Override
  protected VertxOptions getOptions() {
    return new VertxOptions().setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true).setRegistryName("the_name"));
  }

  private MetricRegistry registry;

  @Override
  public void setUp() throws Exception {
    SharedMetricRegistries.clear();
    super.setUp();
  }

  @Test
  public void testRegistration() {
    assertEquals(Collections.singleton("the_name"), SharedMetricRegistries.names());
    registry = SharedMetricRegistries.getOrCreate("the_name");
    assertTrue(registry.getNames().size() > 0);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    Assert.assertEquals(Collections.<String>emptySet(), SharedMetricRegistries.names());
  }
}
