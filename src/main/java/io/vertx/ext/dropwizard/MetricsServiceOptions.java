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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Vert.x metrics service configuration.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject
public class MetricsServiceOptions extends MetricsOptions {

  /**
   * The default value of metrics enabled false
   */
  public static final boolean DEFAULT_METRICS_ENABLED = false;

  /**
   * The default value of JMX enabled = false
   */
  public static final boolean DEFAULT_JMX_ENABLED = false;

  /**
   * The default monitored handlers : empty by default
   */
  public static final List<Match> DEFAULT_MONITORED_HANDLERS = Collections.emptyList();

  private boolean enabled;
  private String name;
  private boolean jmxEnabled;
  private String jmxDomain;
  private List<Match> monitoredHandlers;

  /**
   * Default constructor
   */
  public MetricsServiceOptions() {
    enabled = DEFAULT_METRICS_ENABLED;
    jmxEnabled = DEFAULT_JMX_ENABLED;
    monitoredHandlers = new ArrayList<>(DEFAULT_MONITORED_HANDLERS);
  }

  /**
   * Copy constructor
   *
   * @param other The other {@link io.vertx.ext.dropwizard.MetricsServiceOptions} to copy when creating this
   */
  public MetricsServiceOptions(MetricsServiceOptions other) {
    enabled = other.isEnabled();
    name = other.getName();
    jmxEnabled = other.isJmxEnabled();
    jmxDomain = other.getJmxDomain();
    monitoredHandlers = new ArrayList<>(other.monitoredHandlers);
  }

  /**
   * Create an instance from a {@link io.vertx.core.json.JsonObject}
   *
   * @param json the JsonObject to create it from
   */
  public MetricsServiceOptions(JsonObject json) {
    enabled = json.getBoolean("enabled", DEFAULT_METRICS_ENABLED);
    name = json.getString("name");
    jmxEnabled = json.getBoolean("jmxEnabled", DEFAULT_JMX_ENABLED);
    jmxDomain = json.getString("jmxDomain");
    monitoredHandlers = new ArrayList<>();
    JsonArray handlerAddressesArray = json.getJsonArray("monitoredHandlers");
    if (handlerAddressesArray != null) {
      for (Object o : handlerAddressesArray) {
        monitoredHandlers.add(new Match((JsonObject) o));
      }
    }
  }

  /**
   * Will metrics be enabled on the Vert.x instance?
   *
   * @return true if enabled, false if not.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Set whether metrics will be enabled on the Vert.x instance.
   *
   * @param enable true if metrics enabled, or false if not.
   * @return a reference to this, so the API can be used fluently
   */
  public MetricsServiceOptions setEnabled(boolean enable) {
    this.enabled = enable;
    return this;
  }

  /**
   * An optional name used by the metrics implementation for namespacing or registering the metrics.
   *
   * @return the metrics name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name used by the metrics implementation for namespacing or registering the metrics.
   *
   * @param name the name
   * @return a reference to this, so the API can be used fluently
   */
  public MetricsServiceOptions setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Will JMX be enabled on the Vert.x instance?
   *
   * @return true if enabled, false if not.
   */
  public boolean isJmxEnabled() {
    return jmxEnabled;
  }

  /**
   * Set whether JMX will be enabled on the Vert.x instance.
   *
   * @param jmxEnabled true if JMX enabled, or false if not.
   * @return a reference to this, so the API can be used fluently
   */
  public MetricsServiceOptions setJmxEnabled(boolean jmxEnabled) {
    this.jmxEnabled = jmxEnabled;
    if (jmxEnabled) enabled = true;
    return this;
  }

  /**
   * Get the JMX domain to use when JMX metrics are enabled.
   *
   * @return the JMX domain
   */
  public String getJmxDomain() {
    return jmxDomain;
  }

  /**
   * Set the JMX domain to use when JMX metrics are enabled.
   *
   * @param jmxDomain  the JMX domain
   * @return a reference to this, so the API can be used fluently
   */
  public MetricsServiceOptions setJmxDomain(String jmxDomain) {
    // todo test this
    this.jmxDomain = jmxDomain;
    return this;
  }

  /**
   * @return the list of monitored handlers
   */
  public List<Match> getMonitoredHandlers() {
    return monitoredHandlers;
  }

  /**
   * Add an monitored event bus handler.
   *
   * @param matcher the handler matcher
   * @return a reference to this, so the API can be used fluently
   */
  public MetricsServiceOptions addMonitoredHandler(Match matcher) {
    monitoredHandlers.add(matcher);
    return this;
  }
}
