/*
 * Copyright (c) 2011-2013 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.dropwizard;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.TestUtils;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.Random;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MetricsOptionsTest extends VertxTestBase {

  @Test
  public void testOptions() {
    DropwizardMetricsOptions options = new DropwizardMetricsOptions();

    assertFalse(options.isEnabled());
    assertEquals(options, options.setEnabled(true));
    assertTrue(options.isEnabled());
    assertNull(options.getRegistryName());

    // Test metrics get enabled if jmx is set to true
    options.setEnabled(false);
    assertFalse(options.isJmxEnabled());
    assertEquals(options, options.setJmxEnabled(true));
    assertTrue(options.isJmxEnabled());
    assertTrue(options.isEnabled());

    assertNull(options.getJmxDomain());
    assertEquals("foo", options.setJmxDomain("foo").getJmxDomain());

    assertNull(options.getRegistryName());
    assertEquals("bar", options.setRegistryName("bar").getRegistryName());

    assertNull(options.getConfigPath());
    assertEquals("the_config_file", options.setConfigPath("the_config_file").getConfigPath());
  }

  @Test
  public void testCopyOptions() {
    DropwizardMetricsOptions options = new DropwizardMetricsOptions();

    Random rand = new Random();
    boolean metricsEnabled = rand.nextBoolean();
    boolean jmxEnabled = rand.nextBoolean();
    String jmxDomain = TestUtils.randomAlphaString(100);
    String name = TestUtils.randomAlphaString(100);
    String configPath = TestUtils.randomAlphaString(100);
    options.setEnabled(metricsEnabled);
    options.setJmxEnabled(jmxEnabled);
    options.setJmxDomain(jmxDomain);
    options.setRegistryName(name);
    options.setConfigPath(configPath);
    options = new DropwizardMetricsOptions(options);
    assertEquals(metricsEnabled || jmxEnabled, options.isEnabled());
    assertEquals(jmxEnabled, options.isJmxEnabled());
    assertEquals(jmxDomain, options.getJmxDomain());
    assertEquals(name, options.getRegistryName());
    assertEquals(configPath, options.getConfigPath());
  }

  @Test
  public void testJsonOptions() {
    DropwizardMetricsOptions options = new DropwizardMetricsOptions(new JsonObject());
    assertFalse(options.isEnabled());
    assertFalse(options.isJmxEnabled());
    assertNull(options.getJmxDomain());
    assertNull(options.getRegistryName());
    Random rand = new Random();
    boolean metricsEnabled = rand.nextBoolean();
    boolean jmxEnabled = rand.nextBoolean();
    String jmxDomain = TestUtils.randomAlphaString(100);
    String registryName = TestUtils.randomAlphaString(100);
    String configPath = TestUtils.randomAlphaString(100);
    options = new DropwizardMetricsOptions(new JsonObject().
      put("enabled", metricsEnabled).
      put("registryName", registryName).
      put("jmxEnabled", jmxEnabled).
      put("jmxDomain", jmxDomain).
      put("configPath", configPath)
    );
    assertEquals(metricsEnabled, options.isEnabled());
    assertEquals(registryName, options.getRegistryName());
    assertEquals(jmxEnabled, options.isJmxEnabled());
    assertEquals(jmxDomain, options.getJmxDomain());
  }

  @Test
  public void testFullJsonOptions() throws Exception {

    JsonArray monitoredHttpServerUris = new JsonArray()
      .add(new JsonObject().put("value", "/test/server/1").put("type", "EQUALS"))
      .add(new JsonObject().put("value", "^/server/test/2/.*").put("type", "REGEX"));

    JsonArray monitoredHttpClientUris = new JsonArray()
      .add(new JsonObject().put("value", "/test/client/1").put("type", "EQUALS"))
      .add(new JsonObject().put("value", "^/client/test/2/.*").put("type", "REGEX"));

    JsonArray monitoredEventBusHandlers = new JsonArray()
      .add(new JsonObject().put("value", "test.address.1").put("type", "EQUALS"))
      .add(new JsonObject().put("value", "^test.2.*").put("type", "REGEX"));

    JsonObject config = new JsonObject()
      .put("registryName", "testRegistry")
      .put("jmxEnabled", true)
      .put("jmxDomain", "testJmxDomain")
      .put("monitoredHttpServerUris", monitoredHttpServerUris)
      .put("monitoredHttpClientUris", monitoredHttpClientUris)
      .put("monitoredEventBusHandlers", monitoredEventBusHandlers)
      .put("configPath", "the_config_file");

    DropwizardMetricsOptions options = new DropwizardMetricsOptions(config);

    assertEquals("testRegistry", options.getRegistryName());
    assertTrue(options.isJmxEnabled());
    assertEquals("testJmxDomain", options.getJmxDomain());
    assertEquals("the_config_file", options.getConfigPath());

    assertEquals(2, options.getMonitoredHttpServerUris().size());
    assertEquals("/test/server/1", options.getMonitoredHttpServerUris().get(0).getValue());
    assertEquals(MatchType.EQUALS, options.getMonitoredHttpServerUris().get(0).getType());
    assertEquals("^/server/test/2/.*", options.getMonitoredHttpServerUris().get(1).getValue());
    assertEquals(MatchType.REGEX, options.getMonitoredHttpServerUris().get(1).getType());

    assertEquals(2, options.getMonitoredHttpClientUris().size());
    assertEquals("/test/client/1", options.getMonitoredHttpClientUris().get(0).getValue());
    assertEquals(MatchType.EQUALS, options.getMonitoredHttpClientUris().get(0).getType());
    assertEquals("^/client/test/2/.*", options.getMonitoredHttpClientUris().get(1).getValue());
    assertEquals(MatchType.REGEX, options.getMonitoredHttpClientUris().get(1).getType());

    assertEquals(2, options.getMonitoredEventBusHandlers().size());
    assertEquals("test.address.1", options.getMonitoredEventBusHandlers().get(0).getValue());
    assertEquals(MatchType.EQUALS, options.getMonitoredEventBusHandlers().get(0).getType());
    assertEquals("^test.2.*", options.getMonitoredEventBusHandlers().get(1).getValue());
    assertEquals(MatchType.REGEX, options.getMonitoredEventBusHandlers().get(1).getType());
  }

  @Test
  public void testInvalidAndEmptyMonitoredEntries() throws Exception {
    JsonArray monitoredHttpServerUris = new JsonArray()
      .add(new JsonObject().put("value", "/test/server/1").put("type", "EQUALS"));

    JsonArray monitoredHttpClientUris = new JsonArray()
      .add(new JsonObject().put("value", "/test/client/1").put("type", "EQUALS"))
      .add("Just a string");

    JsonObject config = new JsonObject()
      .put("registryName", "testRegistry")
      .put("jmxEnabled", true)
      .put("jmxDomain", "testJmxDomain")
      .put("monitoredHttpServerUris", monitoredHttpServerUris)
      .put("monitoredHttpClientUris", monitoredHttpClientUris);

    DropwizardMetricsOptions options = new DropwizardMetricsOptions(config);

    assertEquals(1, options.getMonitoredHttpServerUris().size());
    assertEquals("/test/server/1", options.getMonitoredHttpServerUris().get(0).getValue());
    assertEquals(MatchType.EQUALS, options.getMonitoredHttpServerUris().get(0).getType());

    assertEquals(1, options.getMonitoredHttpClientUris().size());
    assertEquals("/test/client/1", options.getMonitoredHttpClientUris().get(0).getValue());
    assertEquals(MatchType.EQUALS, options.getMonitoredHttpClientUris().get(0).getType());

    assertEquals(0, options.getMonitoredEventBusHandlers().size());
  }

}
